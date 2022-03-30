package cn.ffcs.mtcs.realtime.server.core.handle.task;

import cn.ffcs.mtcs.realtime.server.core.handle.yarn.IYarnHandle;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/2/002 14:39
 * @Version 1.0
 */
public class TaskHandleFactory {
    public static Map<String, ITaskHandle> taskStateMap = new ConcurrentHashMap<>();

    public static ITaskHandle getITaskHandle(String taskState) {
        return taskStateMap.get(taskState);
    }

    public static void registerTaskState(String taskState, ITaskHandle handleClass) {
        Assert.notNull(taskState, "taskState can't be null");
        Assert.notNull(handleClass, "monitorClass can't be null");
        taskStateMap.put(taskState, handleClass);
    }
}
