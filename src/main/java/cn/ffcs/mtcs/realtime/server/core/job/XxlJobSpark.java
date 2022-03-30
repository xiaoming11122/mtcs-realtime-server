package cn.ffcs.mtcs.realtime.server.core.job;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.monitor.MonitorContents;
import cn.ffcs.mtcs.realtime.server.core.alarm.IAlarm;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.IYarnHandle;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.YarnHandleFactory;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsMark;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsOperation;
import cn.ffcs.mtcs.realtime.server.core.state.IMonitorState;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/23/023 15:49
 * @Version 1.0
 */
@Component
@Slf4j
public class XxlJobSpark {

    @Autowired
    private IRtMonitorService monitorService;

    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;

    @Autowired
    private IMonitorState monitorState;

    @Autowired
    private IAlarm alarm;

    @Autowired
    private IRtOpsInfoService opsInfoService;

    @Autowired
    private SchedulerXxlJob schedulerXxlJob;

    @Autowired
    private IOpsMark opsMark;

    @Autowired
    private UserPrincipal userPrincipal;


    // 日志输出方式
    //XxlJobLogger.log("XXL-JOB, Hello World.");

    @XxlJob("sparkStart")
    public ReturnT<String> sparkStart(String param) throws Exception {
        //RtMonitor rtMonitorOld = JSON.parseObject(param, RtMonitor.class);
        // param 是rtMonitor的Id
        System.out.println("--------sparkRestart, param : " + param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));

        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        // 2 进行检测
        //      检测状态，如果为空进行告警，
        //      检查监控的次数，如果多次监控都为空，则不再进行监控
        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.StartException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        // 2 处理
        IYarnHandle yarnHandle = YarnHandleFactory.getIYarnHandle(rtAppState.getAppState().toUpperCase());
        yarnHandle.startHandle(rtMonitor, EngineTypeEnum.Spark.getValue());
        return ReturnT.SUCCESS;
    }


    private boolean checkMonitor(RtMonitor rtMonitor, RtAppState rtAppState, String exceptionOpsName) {

        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());

        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());

        boolean flag = true;
        if (null == rtAppState || null == rtAppState.getAppState()) {
            // 其实在监控表中需要一个连续失败的计数字段
            if (rtMonitor.getMonitorCount() > rtMonitor.getMonitorNum()) {
                // 停止监控
                schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
                // 将状态修改为相应的异常
                if (exceptionOpsName.equals(OpsNameEnum.StartException.getValue())) {
                    opsMark.startException(userPrincipal,
                            rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
                } else if (exceptionOpsName.equals(OpsNameEnum.RestartException.getValue())) {
                    opsMark.restartException(userPrincipal,
                            rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
                } else if (exceptionOpsName.equals(OpsNameEnum.StopException.getValue())) {
                    opsMark.stopException(userPrincipal,
                            rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
                } else if (exceptionOpsName.equals(OpsNameEnum.RunException.getValue())) {
                    opsMark.runException(userPrincipal,
                            rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
                }
            }
            // todo 进行告警，在生产环境上才能测试
            String alarmMessage = "获取Spark程序状态为空告警";
            log.warn(alarmMessage);
            XxlJobLogger.log(alarmMessage);
            alarm.nullState(alarmMessage);
            flag = false;
        }

        rtMonitor.setMonitorCount(rtMonitor.getMonitorCount() + 1);
        rtMonitor.setMonitorTime(LocalDateTime.now());
        monitorService.updateById(rtMonitor);

        return flag;
    }


    @XxlJob("sparkRestart")
    public ReturnT<String> sparkRestart(String param) throws Exception {
        System.out.println("--------sparkStart, param : " + param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));


        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.RestartException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        IYarnHandle yarnHandle = YarnHandleFactory.getIYarnHandle(rtAppState.getAppState().toUpperCase());
        yarnHandle.restartHandle(rtMonitor, EngineTypeEnum.Spark.getValue());

        return ReturnT.SUCCESS;
    }

    @XxlJob("sparkStop")
    public ReturnT<String> sparkStop(String param) throws Exception {
        System.out.println("--------sparkStop, param : " + param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));

        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.StopException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        IYarnHandle yarnHandle = YarnHandleFactory.getIYarnHandle(rtAppState.getAppState().toUpperCase());
        yarnHandle.stopHandle(rtMonitor, EngineTypeEnum.Spark.getValue());

        return ReturnT.SUCCESS;
    }

    @XxlJob("sparkRun")
    public ReturnT<String> sparkRun(String param) throws Exception {
        System.out.println("--------sparkRun, param : " + param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));

        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.RunException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        IYarnHandle yarnHandle = YarnHandleFactory.getIYarnHandle(rtAppState.getAppState().toUpperCase());
        yarnHandle.runHandle(rtMonitor, EngineTypeEnum.Spark.getValue());

        return ReturnT.SUCCESS;
    }


    private RtAppState getRtAppState(RtMonitor rtMonitor) {
        return monitorState.getRtAppState(rtMonitor,"spark");
    }

}
