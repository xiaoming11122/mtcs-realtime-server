package cn.ffcs.mtcs.realtime.server.core.monitor;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/20/020 10:58
 * @Version 1.0
 */
public class MonitorFactory {
    public static Map<String, IMonitor> engineMap = new ConcurrentHashMap<>();

    public static IMonitor getIMonitor(String engineName) {
        return engineMap.get(engineName);
    }

    public static void registerMonitor(String engineName, IMonitor monitorClass) {
        Assert.notNull(engineName, "engineName can't be null");
        Assert.notNull(monitorClass, "monitorClass can't be null");
        engineMap.put(engineName, monitorClass);
    }
}
