package cn.ffcs.mtcs.realtime.server.core.monitor;

import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/20/020 10:47
 * @Version 1.0
 */
public interface IMonitor {
    /**
     * 1 启动监控
     * 2 重启监控
     * 3 停止监控
     * 4 运行监控
     * @param rtOpsAttachment
     */

    /**
     * 1 启动监控
     * 程序启动之后，进行监控该程序启动没有
     *
     * @param rtMonitor
     * @return
     */
    boolean start(RtMonitor rtMonitor);

    /**
     * 2 重启监控
     * 程序重启之后，进行监控该程序重启了没有
     *
     * @param rtMonitor
     * @return
     */
    boolean restart(RtMonitor rtMonitor);

    /**
     * 3 停止监控
     * 程序停止之后，进行监控该程序停止了没有
     *
     * @param rtMonitor
     * @return
     */
    boolean stop(RtMonitor rtMonitor);

    /**
     * 4 运行监控
     * 程序运行中，进行监控该程序是否还是在运行中
     *
     * @param rtMonitor
     * @return
     */
    boolean run(RtMonitor rtMonitor);

}
