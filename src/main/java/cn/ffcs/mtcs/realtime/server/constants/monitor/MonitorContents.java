package cn.ffcs.mtcs.realtime.server.constants.monitor;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/2/002 16:16
 * @Version 1.0
 */
public interface MonitorContents {

    int Partition = 2;

    int TimeInterval = 120;

    int MonitorCount = 2 * Partition;

    int MonitorMinCount = 1;

    int MonitorMaxCount = Integer.MAX_VALUE - 1;

    int MonitorStopCount = 4 * Partition;

}
