package cn.ffcs.mtcs.realtime.server.core.handle.task.impl;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.constants.monitor.TaskStateEnum;
import cn.ffcs.mtcs.realtime.server.core.alarm.IAlarm;
import cn.ffcs.mtcs.realtime.server.core.handle.task.ITaskHandle;
import cn.ffcs.mtcs.realtime.server.core.handle.task.TaskHandleFactory;
import cn.ffcs.mtcs.realtime.server.core.job.SchedulerXxlJob;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsMark;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsOperation;
import cn.ffcs.mtcs.realtime.server.service.data.*;
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
 * @Date 2020/3/2/002 14:38
 * @Version 1.0
 */
@Component
@Slf4j
public class TaskHandleRunning implements ITaskHandle, InitializingBean {

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
    private IRtTaskLastStateService taskLastStateInfoService;

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
        TaskHandleFactory.registerTaskState(TaskStateEnum.RUNNING.getValue(), this);
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
         * 1 停止监控
         * 2 更新监控表
         * 3 修改任务状态为，正在运行
         */

        // 1 停止任务调度
        schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
        // 2 更新监控表中获取到的状态
        rtMonitor.setMonitorActual(TaskStateEnum.RUNNING.getValue());
        monitorService.updateById(rtMonitor);
        // 3 修改实时任务的状态为正在运行
        opsOperation.run(userPrincipal,
                rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion(),
                engineName);
        // 4 告警
        String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                + rtTaskInfo.getTaskName() + "任务，启动成功！";
        log.warn(alarmMessage);
        alarm.startException(alarmMessage);

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
         * 1 停止监控
         * 2 更新监控表
         * 3 修改任务状态为，正在运行
         */

        // 1 停止任务调度
        schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
        // 2 更新监控表
        rtMonitor.setMonitorActual(TaskStateEnum.RUNNING.getValue());
        monitorService.updateById(rtMonitor);
        // 3 修改实时任务的状态为正在运行
        opsOperation.run(userPrincipal,
                rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion(),
                engineName);
        // 4 告警
        String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                + rtTaskInfo.getTaskName() + "任务，重启成功！";
        log.warn(alarmMessage);
        alarm.startException(alarmMessage);

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
            rtMonitor.setMonitorActual(TaskStateEnum.RUNNING.getValue());
            monitorService.updateById(rtMonitor);
            // 3 修改实时任务的状态为正在运行
            opsMark.stopException(userPrincipal,
                    rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
            // 4 告警
            String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                    + rtTaskInfo.getTaskName() + "任务，停止异常，程序还在执行！";
            log.warn(alarmMessage);
            XxlJobLogger.log(alarmMessage);
            alarm.startException(alarmMessage);
        } else {
            rtMonitor.setMonitorActual(TaskStateEnum.RUNNING.getValue());
            monitorService.updateById(rtMonitor);
        }

        return true;
    }

    @Override
    public boolean runHandle(RtMonitor rtMonitor, String engineName) {

        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());
        rtMonitor.setMonitorActual(TaskStateEnum.RUNNING.getValue());
        monitorService.updateById(rtMonitor);

        //任务是运行状态，运行状态需要更新  RtTaskLastState
        RtTaskLastState rtTaskLastState =
                taskLastStateInfoService.getOne(
                        Wrappers.<RtTaskLastState>lambdaQuery()
                                .eq(RtTaskLastState::getTaskId, rtOpsInfo.getTaskId())
                                .eq(RtTaskLastState::getTaskVersion, rtOpsInfo.getTaskVersion())
                                .eq(RtTaskLastState::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        rtTaskLastState.setTaskState(OpsNameEnum.Running.getValue());
        taskLastStateInfoService.updateById(rtTaskLastState);
        return true;
    }


}
