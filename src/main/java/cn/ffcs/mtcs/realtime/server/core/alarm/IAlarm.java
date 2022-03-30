package cn.ffcs.mtcs.realtime.server.core.alarm;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/26/026 13:15
 * @Version 1.0
 */
public interface IAlarm {

    boolean nullState(String message);

    boolean startException(String message);

    boolean restartException(String message);

    boolean stopException(String message);

    boolean runException(String message);
}
