package cn.ffcs.mtcs.realtime.server.service.data;

import cn.ffcs.mtcs.realtime.common.entity.RtExeMachineEnv;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 实时执行机信息的附属环境 服务类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
public interface IRtExeMachineEnvService extends IService<RtExeMachineEnv> {
    /**
     * 根据machineId获取附属环境信息
     * @param machineId
     * @return
     */
    public List<RtExeMachineEnv> getRtExeMachineEnvList(Long machineId);


    /**
     * 根据machineId获取附属环境信息，结果为map格式
     * @param machineId
     * @return
     */
    public Map<String, String> getRtExeMachineEnvMap(Long machineId);
}
