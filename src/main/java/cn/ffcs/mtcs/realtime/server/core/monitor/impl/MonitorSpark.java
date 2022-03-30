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
 * @Date 2020/2/20/020 10:57
 * @Version 1.0
 */
@Component
public class MonitorSpark implements IMonitor, InitializingBean {

    @Autowired
    private IRtMonitorService monitorService;

    @Autowired
    private SchedulerXxlJob schedulerXxlJob;

    @Override
    public void afterPropertiesSet() throws Exception {
        MonitorFactory.registerMonitor(EngineTypeEnum.Spark.getValue(), this);
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
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.SparkStart.getValue());
    }

    @Override
    public boolean restart(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.SparkRestart.getValue());
    }

    @Override
    public boolean stop(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.SparkStop.getValue());
    }

    @Override
    public boolean run(RtMonitor rtMonitor) {
        return commonStep(rtMonitor, XxlJobExeHandlerEnum.SparkRun.getValue());
    }


    private boolean commonStep(RtMonitor rtMonitor, String exeHandler) {
        /**
         * 1 创建调度信息
         * 2 添加一个调度
         * 3 将调度信息更新到监控表
         * 4 启动调度
         */
        // 1 创建调度信息
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
