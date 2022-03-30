package cn.ffcs.mtcs.realtime.server.core.engine;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 16:25
 * @Version 1.0
 */
public class EngineFactory {

    public static Map<String, IEngine> engineMap = new ConcurrentHashMap<>();

    public static IEngine getIEngine(String engineName) {
        return engineMap.get(engineName);
    }

    public static void registerEngine(String engineName, IEngine engineClass) {
        Assert.notNull(engineName,"engineName can't be null");
        Assert.notNull(engineClass,"engineClass can't be null");
        engineMap.put(engineName, engineClass);
    }
}
