package cn.ffcs.mtcs.realtime.server.core.monitor.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.XxlJobExeHandlerEnum;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineFactory;
import cn.ffcs.mtcs.realtime.server.core.job.SchedulerXxlJob;
import cn.ffcs.mtcs.realtime.server.core.job.XxlJobInfo;
import cn.ffcs.mtcs.realtime.server.core.monitor.IMonitor;
import cn.ffcs.mtcs.realtime.server.core.monitor.MonitorFactory;
import cn.ffcs.mtcs.realtime.server.service.data.IRtMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/20/020 10:57
 * @Version 1.0
 */
@Component
@Slf4j
public class MonitorFlink implements IMonitor, InitializingBean {

    @Autowired
    private IRtMonitorService monitorService;

    @Autowired
    private SchedulerXxlJob schedulerXxlJob;


    @Override
    public void afterPropertiesSet() throws Exception {
        MonitorFactory.registerMonitor(EngineTypeEnum.Flink.getValue(), this);
        MonitorFactory.registerMonitor(EngineTypeEnum.FlinkSql.getValue(), this);
    }

    /**
     * 1 开始监控
     * 2 重启监控
     * 3 停止监控
     * 4 运行监控
     *
     * @param rtMonitor
     */
    @Override
    public boolean start(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.FlinkAppStart.getValue());
    }

    @Override
    public boolean restart(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.FlinkAppRestart.getValue());
    }

    @Override
    public boolean stop(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.FlinkAppStop.getValue());
    }

    @Override
    public boolean run(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.FlinkAppRun.getValue());
    }

    private boolean commonStep(RtMonitor rtMonitor, String exeHandler) {

        Integer xxlJobId = rtMonitor.getXxlJobId();


            /**在未找到xxljobid的情况下才进行job的创建，否则使用之前的job
             * 1 填写监控表
             * 2 注册任务调度
             */
            // 1 填写监控表
            XxlJobInfo xxlJobInfo =
                    schedulerXxlJob.createXxlJobInfo(
                            rtMonitor,
                            exeHandler);
        if (xxlJobId == null) {
            // 2 添加一个调度
            log.info("添加前jobid={}", xxlJobId);
            xxlJobId = schedulerXxlJob.addScheduler(xxlJobInfo);
            log.info("添加后jobid={}", xxlJobId);
            // 3 更新xxlJobId到监控表
            rtMonitor.setXxlJobId(xxlJobId);
            monitorService.updateById(rtMonitor);

        } else {
            log.info("添加前jobid={}", xxlJobInfo.getId());
            xxlJobInfo.setId(xxlJobId);
            log.info("添加后jobid={}", xxlJobId);
            log.info("Handler={}",xxlJobInfo.getExecutorHandler());
            schedulerXxlJob.updateScheduler(xxlJobInfo);
        }
        // 4 启动调度
        return schedulerXxlJob.startScheduler(xxlJobId);
    }
}
