package cn.ffcs.mtcs.realtime.server.core.operation;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/9/009 14:56
 * @Version 1.0
 */
public class OperationFactory {
    public static Map<String, IOperation> engineMap = new ConcurrentHashMap<>();

    public static IOperation getIOperation(String engineName) {
        return engineMap.get(engineName);
    }

    public static void registerOperation(String engineName, IOperation operationClass) {
        Assert.notNull(engineName, "engineName can't be null");
        Assert.notNull(operationClass, "operationClass can't be null");
        engineMap.put(engineName, operationClass);
    }
}
