package cn.ffcs.mtcs.realtime.server.service.business.logContent;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nemo
 * @version V1.0
 * @Description: .
 * @date 2020/7/13 18:07
 */
public class LogContentOpsFactory {
    public static Map<String, LogContentOps> opsMap = new ConcurrentHashMap<>();

    public static LogContentOps getLogContentOps(String opsName) {
        return opsMap.get(opsName);
    }

    public static void registerOperation(String opsName, LogContentOps logContentClass) {
        Assert.notNull(opsName, "opsName can't be null");
        Assert.notNull(logContentClass, "logContentClass can't be null");
        opsMap.put(opsName, logContentClass);
    }
}
