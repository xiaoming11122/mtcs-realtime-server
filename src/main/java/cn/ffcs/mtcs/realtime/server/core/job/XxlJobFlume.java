package cn.ffcs.mtcs.realtime.server.core.job;

import cn.ffcs.mtcs.realtime.common.entity.RtAppState;
import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsInfo;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.core.alarm.IAlarm;
import cn.ffcs.mtcs.realtime.server.core.handle.task.ITaskHandle;
import cn.ffcs.mtcs.realtime.server.core.handle.task.TaskHandleFactory;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.IYarnHandle;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.YarnHandleFactory;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsMark;
import cn.ffcs.mtcs.realtime.server.core.state.IMonitorState;
import cn.ffcs.mtcs.realtime.server.service.data.IRtMonitorService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsAttachmentService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsInfoService;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
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
public class XxlJobFlume {

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

    @XxlJob("flumeStart")
    public ReturnT<String> flinkStart(String param) throws Exception {
        //RtMonitor rtMonitorOld = JSON.parseObject(param, RtMonitor.class);
        // param 是rtMonitor的Id
        System.out.println("--------flumeStart, param : " + param);
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
        ITaskHandle taskHandle = TaskHandleFactory.getITaskHandle(rtAppState.getAppState().toUpperCase());
        taskHandle.startHandle(rtMonitor, EngineTypeEnum.Flume.getValue());
        return ReturnT.SUCCESS;
    }


    private boolean checkMonitor(RtMonitor rtMonitor, RtAppState rtAppState, String exceptionOpsName) {

        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());

        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());

        boolean flag = true;
        if (null == rtAppState || null == rtAppState.getAppState()) {
            // 其实在监控表中需要一个连续失败的计数字段
            if (rtMonitor.getMonitorCount() > rtOpsAttachment.getRestartNum()) {
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
                    opsMark.stopException(userPrincipal,
                            rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
                }
            }
            // todo 进行告警，在生产环境上才能测试
            String alarmMessage = "获取Flume程序状态为空告警";
            log.warn(alarmMessage);
            XxlJobLogger.log(alarmMessage);
            alarm.nullState(alarmMessage);
            flag = false;
        }
        //执行成功后，需要把之前的监控次数置0，待处理******

        rtMonitor.setMonitorCount(rtMonitor.getMonitorCount() + 1);
        rtMonitor.setMonitorTime(LocalDateTime.now());
        monitorService.updateById(rtMonitor);

        return flag;
    }


    @XxlJob("flumeRestart")
    public ReturnT<String> flinkRestart(String param) throws Exception {
        System.out.println("--------flumeRestart, param : " + param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));


        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.RestartException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        // 2 处理
        ITaskHandle taskHandle = TaskHandleFactory.getITaskHandle(rtAppState.getAppState().toUpperCase());
        taskHandle.restartHandle(rtMonitor, EngineTypeEnum.Flume.getValue());
        return ReturnT.SUCCESS;
    }

    @XxlJob("flumeStop")
    public ReturnT<String> flumeStop(String param) throws Exception {
        System.out.println("--------flumeStop, param : " + param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));

        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.StopException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        ITaskHandle tastHandle = TaskHandleFactory.getITaskHandle(rtAppState.getAppState().toUpperCase());
        tastHandle.stopHandle(rtMonitor, EngineTypeEnum.Flume.getValue());

        return ReturnT.SUCCESS;
    }

    @XxlJob("flumeRun")
    public ReturnT<String> flumeRun(String param) throws Exception {
        System.out.println("--------flumeRun, param : " + param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));

        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.RunException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        ITaskHandle tastHandle = TaskHandleFactory.getITaskHandle(rtAppState.getAppState().toUpperCase());
        tastHandle.runHandle(rtMonitor, EngineTypeEnum.Flume.getValue());


        return ReturnT.SUCCESS;
    }


    private RtAppState getRtAppState(RtMonitor rtMonitor) {
        return monitorState.getRtAppState(rtMonitor,"flume");
    }
}
