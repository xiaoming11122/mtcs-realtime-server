package cn.ffcs.mtcs.realtime.server.core.state;

import cn.ffcs.mtcs.realtime.common.entity.RtAppState;
import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;

/**
 * @Description 获取程序的状态.
 * @Author Nemo
 * @Date 2020/2/25/025 14:46
 * @Version 1.0
 */
public interface IMonitorState {

    /**
     *
     * @param rtMonitor
     * @return
     */
    RtAppState getRtAppState(RtMonitor rtMonitor,String engineType);
}
