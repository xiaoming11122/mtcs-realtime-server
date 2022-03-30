package cn.ffcs.mtcs.realtime.server.core.handle.yarn.impl;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.RestartTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.monitor.YarnStateEnum;
import cn.ffcs.mtcs.realtime.server.core.alarm.IAlarm;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.IYarnHandle;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.YarnHandleFactory;
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
public class YarnHandleFinished implements IYarnHandle, InitializingBean {

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
    private IRtRetryService retryService;

    @Autowired
    private UserPrincipal userPrincipal;

    @Autowired
    private IAlarm alarm;

    @Override
    public void afterPropertiesSet() throws Exception {
        YarnHandleFactory.registerYarnState(YarnStateEnum.FINISHED.getValue(), this);
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
         * 3 修改任务状态为启动异常
         * 4 告警
         */

        // 1 停止任务调度
        schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
        // 2 更新监控表
        rtMonitor.setMonitorActual(YarnStateEnum.FINISHED.getValue());
        monitorService.updateById(rtMonitor);
        // 3 修改实时任务的状态为启动异常
        opsMark.startException(userPrincipal,
                rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
        // 4 告警
        String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                + rtTaskInfo.getTaskName() + "任务，启动异常！";
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

        RtRetry rtRetry = retryService.getById(rtMonitor.getRetryId());

        /**
         * 获取重启类型标记
         *   如果是自动重启，
         *          停止任务调度
         *          更新监控表中的相关信息
         *          将重试表中的失败次数加1
         *          重新执行一次重启操作
         *   如果不是自动重启，
         *          停止任务调度
         *          更新监控表中的相关信息
         *          将任务状态修改启动异常
         *          告警任务重启异常
         */

        if (RestartTypeEnum.Auto.getValue().equals(rtOpsAttachment.getRestartType())) {
            // 1 停止任务调度
            schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
            // 2 更新监控表中的相关信息
            rtMonitor.setMonitorActual(YarnStateEnum.FINISHED.getValue());
            monitorService.updateById(rtMonitor);
            // 3 更新,将重试表中的失败次数加1
            int failCount = rtRetry.getFailCount() + 1;
            rtRetry.setFailCount(failCount);
            retryService.updateRtRetry(rtRetry);
            if (failCount >= rtRetry.getRetryNum()) {
                opsMark.restartException(userPrincipal,
                        rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());

                // todo 进行告警，在生产环境上才能测试
                String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                        + rtTaskInfo.getTaskName() + "任务，重启异常！";
                log.warn(alarmMessage);
                XxlJobLogger.log(alarmMessage);
                alarm.restartException(alarmMessage);
            } else {
                // 4 重新执行一次重启操作
                opsOperation.restart(userPrincipal,
                        rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion(),
                        EngineTypeEnum.Spark.getValue(),
                        rtRetry.getId());
            }
        } else {
            // 1 停止任务调度
            schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
            // 2 更新监控表中的相关信息
            rtMonitor.setMonitorActual(YarnStateEnum.FINISHED.getValue());
            monitorService.updateById(rtMonitor);
            // 3 将任务状态修改启动异常
            opsMark.restartException(userPrincipal, rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
            // 4 告警任务启动异常
            // todo 进行告警，在生产环境上才能测试
            String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                    + rtTaskInfo.getTaskName() + "任务，重启异常！";
            log.warn(alarmMessage);
            XxlJobLogger.log(alarmMessage);
            alarm.restartException(alarmMessage);
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

        // 1 停止任务调度
        schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
        // 2 更新监控表
        rtMonitor.setMonitorActual(YarnStateEnum.FINISHED.getValue());
        monitorService.updateById(rtMonitor);
        // 3 修改实时任务的状态为停止
        opsMark.stop(userPrincipal, rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
        // 4 告警
        String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                + rtTaskInfo.getTaskName() + "任务，停止成功！";
        log.warn(alarmMessage);
        alarm.startException(alarmMessage);

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

        // 1 停止任务调度
        schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
        // 2 将任务标记为运行异常
        opsMark.runException(userPrincipal, rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
        // 3 运行异常告警
        String alarmMessage = rtOpsInfo.getOpsTeam() + "团队的"
                + rtTaskInfo.getTaskName() + "任务，运行异常导致任务停止！";
        log.warn(alarmMessage);
        XxlJobLogger.log(alarmMessage);
        alarm.restartException(alarmMessage);
        // 4 更新任务监控表
        rtMonitor.setMonitorActual(YarnStateEnum.FINISHED.getValue());
        monitorService.updateById(rtMonitor);
        // 5 根据重启类型，判断是否需要进行重启
        if (RestartTypeEnum.Auto.getValue().equals(rtOpsAttachment.getRestartType())) {
            opsOperation.restart(userPrincipal,
                    rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion(),
                    EngineTypeEnum.Spark.getValue(),
                    -1L);
        }

        return true;
    }
}
