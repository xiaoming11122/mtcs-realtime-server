package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtExeMachineEnv;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.mapper.RtExeMachineEnvMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtExeMachineEnvService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 实时执行机信息的附属环境 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
@Service
public class RtExeMachineEnvServiceImpl extends ServiceImpl<RtExeMachineEnvMapper, RtExeMachineEnv> implements IRtExeMachineEnvService {

    @Override
    public List<RtExeMachineEnv> getRtExeMachineEnvList(Long machineId) {
        List<RtExeMachineEnv> rtExeMachineEnvList =
                this.lambdaQuery()
                        .eq(RtExeMachineEnv::getMachineId, machineId)
                        .eq(RtExeMachineEnv::getState, RecordStateEnum.StateUse.getValue())
                        .list();
        return rtExeMachineEnvList;
    }

    @Override
    public Map<String, String> getRtExeMachineEnvMap(Long machineId) {
        Map<String, String> exeMachineEnvMap = new HashMap<>();
        List<RtExeMachineEnv>  rtExeMachineEnvList = getRtExeMachineEnvList(machineId);
        if (rtExeMachineEnvList != null) {
            for (RtExeMachineEnv rtExeMachineEnv: rtExeMachineEnvList) {
                exeMachineEnvMap.put(rtExeMachineEnv.getEnvKey(), rtExeMachineEnv.getEnvValue());
            }
        }
        return exeMachineEnvMap;
    }
}
