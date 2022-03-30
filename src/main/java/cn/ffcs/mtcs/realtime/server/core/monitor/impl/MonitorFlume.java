package cn.ffcs.mtcs.realtime.server.core.monitor.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.XxlJobExeHandlerEnum;
import cn.ffcs.mtcs.realtime.server.core.job.SchedulerXxlJob;
import cn.ffcs.mtcs.realtime.server.core.job.XxlJobInfo;
import cn.ffcs.mtcs.realtime.server.core.monitor.IMonitor;
import cn.ffcs.mtcs.realtime.server.core.monitor.MonitorFactory;
import cn.ffcs.mtcs.realtime.server.service.data.IRtMonitorService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/20/020 10:58
 * @Version 1.0
 */
@Component
public class MonitorFlume  implements IMonitor, InitializingBean {


    @Autowired
    private IRtMonitorService monitorService;

    @Autowired
    private SchedulerXxlJob schedulerXxlJob;

    @Override
    public void afterPropertiesSet() throws Exception {
        MonitorFactory.registerMonitor(EngineTypeEnum.Flume.getValue(), this);
    }

    @Override
    public boolean start(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.FlumeStart.getValue());
    }

    @Override
    public boolean restart(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.FlumeRestart.getValue());
    }


    @Override
    public boolean stop(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.FlumeStop.getValue());
    }

    @Override
    public boolean run(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.FlumeRun.getValue());
    }

    private boolean commonStep(RtMonitor rtMonitor, String exeHandler) {
        /**
         * 1 填写监控表
         * 2 注册任务调度
         */
        // 1 填写监控表
        XxlJobInfo xxlJobInfo =
                schedulerXxlJob.createXxlJobInfo(
                        rtMonitor,
                        exeHandler);

        // 2 添加一个调度
        int xxlJobId = schedulerXxlJob.addScheduler(xxlJobInfo);

        // 3 更新xxlJobId到监控表
        rtMonitor.setXxlJobId(xxlJobId);
        monitorService.updateById(rtMonitor);

        // 4 启动调度
        return schedulerXxlJob.startScheduler(xxlJobId);
    }
}
