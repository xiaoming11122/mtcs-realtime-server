package cn.ffcs.mtcs.realtime.server.core.handle.yarn;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/2/002 14:39
 * @Version 1.0
 */
public class YarnHandleFactory {
    public static Map<String, IYarnHandle> yarnStateMap = new ConcurrentHashMap<>();

    public static IYarnHandle getIYarnHandle(String yarnState) {
        return yarnStateMap.get(yarnState);
    }

    public static void registerYarnState(String yarnState, IYarnHandle handleClass) {
        Assert.notNull(yarnState, "engineName can't be null");
        Assert.notNull(handleClass, "monitorClass can't be null");
        yarnStateMap.put(yarnState, handleClass);
    }
}
