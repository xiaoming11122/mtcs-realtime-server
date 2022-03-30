package cn.ffcs.mtcs.realtime.server.pojo.bo;

import cn.ffcs.mtcs.realtime.common.entity.RtExeMachine;
import cn.ffcs.mtcs.realtime.common.entity.RtFlowInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtParamInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtTaskInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/11/011 11:43
 * @Version 1.0
 */
@Data
public class DetailTaskInfoBo {

    private RtTaskInfo rtTaskInfo;

    private RtFlowInfo rtFlowInfo;

    private List<ExeMachineBo> exeMachineBoList;

    private List<RtParamInfo> appParamInfoList;

    private List<RtParamInfo> runParamInfoList;

    private List<RtParamInfo> configParamInfoList;

    public DetailTaskInfoBo() {
    }

    public DetailTaskInfoBo(RtTaskInfo rtTaskInfo, RtFlowInfo rtFlowInfo, List<ExeMachineBo> exeMachineBoList, List<RtParamInfo> appParamInfoList, List<RtParamInfo> runParamInfoList, List<RtParamInfo> configParamInfoList) {
        this.rtTaskInfo = rtTaskInfo;
        this.rtFlowInfo = rtFlowInfo;
        this.exeMachineBoList = exeMachineBoList;
        this.appParamInfoList = appParamInfoList;
        this.runParamInfoList = runParamInfoList;
        this.configParamInfoList = configParamInfoList;
    }

    public List<RtExeMachine> getRtExeMachineList() {
        List<RtExeMachine> rtExeMachineList = new ArrayList<>();
        for (ExeMachineBo exeMachineBo : exeMachineBoList) {
            rtExeMachineList.add(exeMachineBo.getRtExeMachine());
        }
        return rtExeMachineList;
    }
}
