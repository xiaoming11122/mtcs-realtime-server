package cn.ffcs.mtcs.realtime.server.core.handle.yarn;

import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/2/002 10:10
 * @Version 1.0
 */
public interface IYarnHandle {

    boolean startHandle(RtMonitor rtMonitor, String engineName);

    boolean restartHandle(RtMonitor rtMonitor, String engineName);

    boolean stopHandle(RtMonitor rtMonitor, String engineName);

    boolean runHandle(RtMonitor rtMonitor, String engineName);

}
