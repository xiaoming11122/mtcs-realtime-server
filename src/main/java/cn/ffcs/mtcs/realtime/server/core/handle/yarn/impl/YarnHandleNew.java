package cn.ffcs.mtcs.realtime.server.core.handle.yarn.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtTaskInfo;
import cn.ffcs.mtcs.realtime.server.constants.monitor.YarnStateEnum;
import cn.ffcs.mtcs.realtime.server.core.alarm.IAlarm;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.IYarnHandle;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.YarnHandleFactory;
import cn.ffcs.mtcs.realtime.server.core.job.SchedulerXxlJob;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsMark;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsOperation;
import cn.ffcs.mtcs.realtime.server.service.data.IRtMonitorService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsAttachmentService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsInfoService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskInfoService;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/2/002 10:43
 * @Version 1.0
 */
@Component
@Slf4j
public class YarnHandleNew implements IYarnHandle, InitializingBean {

    @Autowired
    private IRtMonitorService monitorService;

    @Autowired
    private SchedulerXxlJob schedulerXxlJob;

    @Autowired
    private IOpsMark opsMark;

    @Autowired
    private IOpsOperation opsOperation;

    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;

    @Autowired
    private IRtOpsInfoService opsInfoService;

    @Autowired
    private IRtTaskInfoService taskInfoService;

    @Autowired
    private UserPrincipal userPrincipal;

    @Autowired
    private IAlarm alarm;

    @Override
    public void afterPropertiesSet() throws Exception {
        YarnHandleFactory.registerYarnState(YarnStateEnum.NEW.getValue(), this);
    }

    @Override
    public boolean startHandle(RtMonitor rtMonitor, String engineName) {
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());
        RtTaskInfo rtTaskInfo = taskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskId, rtOpsInfo.getTaskId())
                        .eq(RtTaskInfo::getTaskVersion, rtOpsInfo.getTaskVersion()),
                false);

        /**
         * 1 判断现在是第几次监控
         * 2 如果已经到了最后一次，进行告警，程序还没有监控起来，需要人工接入
         * 3 没有到最后一次，则不做任何处理,更新监控表
         */
        // monitorCount默认值为0
        int monitorCount = rtMonitor.getMonitorCount();

        if (monitorCount >= rtMonitor.getMonitorNum()) {
            /**
             * 1 停止调度
             * 2 更新监控表中的相关信息
             * 3 修改任务状态为运行中
             * 4 告警
             */
            // 1 停止任务调度
            schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
            // 2 更新监控表
            rtMonitor.setMonitorActual(YarnStateEnum.NEW.getValue());
            monitorService.updateById(rtMonitor);
            // 3 修改实时任务的状态为正在运行
            opsOperation.run(userPrincipal,
                    rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion(),
                    engineName);
            // 4 告警
            String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                    + rtTaskInfo.getTaskName() + "任务，启动的状态为NEW，需要介入排查！";
            log.warn(alarmMessage);
            alarm.startException(alarmMessage);
        } else {
           rtMonitor.setMonitorActual(YarnStateEnum.NEW.getValue());
            monitorService.updateById(rtMonitor);
        }

        return true;
    }

    @Override
    public boolean restartHandle(RtMonitor rtMonitor, String engineName) {
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());
        RtTaskInfo rtTaskInfo = taskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskId, rtOpsInfo.getTaskId())
                        .eq(RtTaskInfo::getTaskVersion, rtOpsInfo.getTaskVersion()),
                false);

        /**
         * 1 判断现在是第几次监控
         * 2 如果已经到了最后一次，进行告警，程序还没有监控起来，需要人工接入
         * 3 没有到最后一次，则不做任何处理,更新监控表
         */
        // monitorCount默认值为0
        int monitorCount = rtMonitor.getMonitorCount();

        if (monitorCount >= rtMonitor.getMonitorNum()) {
            /**
             * 1 停止调度
             * 2 更新监控表中的相关信息
             * 3 修改任务状态为运行中
             * 4 告警
             */
            // 1 停止任务调度
            schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
            // 2 更新监控表
            rtMonitor.setMonitorActual(YarnStateEnum.NEW.getValue());
            monitorService.updateById(rtMonitor);
            // 3 修改实时任务的状态为正在运行
            opsOperation.run(userPrincipal,
                    rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion(),
                    engineName);
            // 4 告警
            String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                    + rtTaskInfo.getTaskName() + "任务，重启的状态为NEW，需要介入排查！";
            log.warn(alarmMessage);
            alarm.startException(alarmMessage);
        } else {
           rtMonitor.setMonitorActual(YarnStateEnum.NEW.getValue());
            monitorService.updateById(rtMonitor);
        }

        return true;
    }

    @Override
    public boolean stopHandle(RtMonitor rtMonitor, String engineName) {
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());
        RtTaskInfo rtTaskInfo = taskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskId, rtOpsInfo.getTaskId())
                        .eq(RtTaskInfo::getTaskVersion, rtOpsInfo.getTaskVersion()),
                false);

        /**
         * 1 判断现在是第几次监控
         * 2 如果已经到了最后一次，进行告警，程序还没有监控起来，需要人工接入
         * 3 没有到最后一次，则不做任何处理,更新监控表
         */
        // monitorCount默认值为0
        int monitorCount = rtMonitor.getMonitorCount();

        if (monitorCount >= rtMonitor.getMonitorNum()) {
            /**
             * 1 停止调度
             * 2 更新监控表中的相关信息
             * 3 修改任务状态为运行中
             * 4 告警
             */
            // 1 停止任务调度
            schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
            // 2 更新监控表
            rtMonitor.setMonitorActual(YarnStateEnum.NEW.getValue());
            monitorService.updateById(rtMonitor);
            // 3 修改实时任务的状态为正在运行
            opsMark.stopException(userPrincipal,
                    rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
            // 4 告警
            String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                    + rtTaskInfo.getTaskName() + "任务，停止异常，程序还没有运行起来，就要执行了停止！";
            log.warn(alarmMessage);
            XxlJobLogger.log(alarmMessage);
            alarm.startException(alarmMessage);
        } else {
           rtMonitor.setMonitorActual(YarnStateEnum.NEW.getValue());
            monitorService.updateById(rtMonitor);
        }

        return true;
    }

    @Override
    public boolean runHandle(RtMonitor rtMonitor, String engineName) {
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());
        RtTaskInfo rtTaskInfo = taskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskId, rtOpsInfo.getTaskId())
                        .eq(RtTaskInfo::getTaskVersion, rtOpsInfo.getTaskVersion()),
                false);

        // 1 更新监控表
        rtMonitor.setMonitorActual(YarnStateEnum.NEW.getValue());
        monitorService.updateById(rtMonitor);
        // 2 告警
        String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                + rtTaskInfo.getTaskName() + "任务，状态为NEW，需要介入排查！";
        log.warn(alarmMessage);
        alarm.startException(alarmMessage);

        return true;
    }
}
