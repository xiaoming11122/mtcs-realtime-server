package cn.ffcs.mtcs.realtime.server.pojo.bo;

import cn.ffcs.mtcs.realtime.common.entity.RtExeMachine;
import cn.ffcs.mtcs.realtime.common.entity.RtExeMachineEnv;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/11/011 15:22
 * @Version 1.0
 */
@Data
public class ExeMachineBo {

    private RtExeMachine rtExeMachine;

    private Map<String, String> exeMachineEnv;

    public ExeMachineBo() {
    }

    public ExeMachineBo(RtExeMachine rtExeMachine, List<RtExeMachineEnv> rtExeMachineEnvList) {
        this.rtExeMachine = rtExeMachine;

        Map<String, String> exeMachineEnv = new HashMap<>();
        for (RtExeMachineEnv rtExeMachineEnv : rtExeMachineEnvList) {
            exeMachineEnv.put(rtExeMachineEnv.getEnvKey(), rtExeMachineEnv.getEnvValue());
        }
        this.exeMachineEnv = exeMachineEnv;
    }
}
