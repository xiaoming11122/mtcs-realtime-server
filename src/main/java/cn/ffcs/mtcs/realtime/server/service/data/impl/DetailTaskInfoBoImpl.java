package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.pojo.bo.ExeMachineBo;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.constants.ExeParamTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/11/011 11:49
 * @Version 1.0
 */
@Component
public class DetailTaskInfoBoImpl implements IDetailTaskInfoBoService {


    /**
     * 获取任务包含所有的配置信息
     *
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public DetailTaskInfoBo getTaskDetailInfoBo(Long taskId, String taskVersion) {
        RtTaskInfo rtTaskInfo = getRtTaskInfo(taskId, taskVersion);
        RtFlowInfo rtFlowInfo = getRtFlowInfo(taskId, taskVersion);
        List<ExeMachineBo> exeMachineBoList = getExeMachineBoList(taskId, taskVersion);
        List<RtParamInfo> appParamList = getAppParamInfoList(taskId, taskVersion);
        List<RtParamInfo> runParamList = getRunParamInfoList(taskId, taskVersion);
        List<RtParamInfo> configParamList = getConfigParamInfoList(taskId, taskVersion);

        DetailTaskInfoBo taskDetailInfoBo =
                new DetailTaskInfoBo(
                        rtTaskInfo,
                        rtFlowInfo,
                        exeMachineBoList,
                        appParamList,
                        runParamList,
                        configParamList
                );
        return taskDetailInfoBo;
    }

    @Autowired
    private IRtTaskInfoService taskInfoService;

    private RtTaskInfo getRtTaskInfo(Long taskId, String taskVersion) {
        RtTaskInfo rtTaskInfo = taskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskId, taskId)
                        .eq(RtTaskInfo::getTaskVersion, taskVersion),
                false);

        return rtTaskInfo;
    }


    @Autowired
    private IRtFlowInfoService flowInfoService;

    private RtFlowInfo getRtFlowInfo(Long taskId, String taskVersion) {
        RtFlowInfo rtFlowInfo =
                flowInfoService.getOne(
                        Wrappers.<RtFlowInfo>lambdaQuery()
                                .eq(RtFlowInfo::getFlowId, taskId)
                                .eq(RtFlowInfo::getFlowVersion, taskVersion),
                        false);
        return rtFlowInfo;
    }

    @Autowired
    private IRtTaskMachineRelativeService taskMachineRelativeService;

    @Autowired
    private IRtExeMachineService exeMachineService;

    @Autowired
    private IRtExeMachineEnvService exeMachineEnvService;


    private List<ExeMachineBo> getExeMachineBoList(Long taskId, String taskVersion) {
        List<RtTaskMachineRelative> rtTaskMachineRelativeList =
                taskMachineRelativeService.lambdaQuery()
                        .eq(RtTaskMachineRelative::getTaskId, taskId)
                        .eq(RtTaskMachineRelative::getTaskVersion, taskVersion)
                        .list();

        List<ExeMachineBo> exeMachineBoList = new ArrayList<>();
        for (RtTaskMachineRelative rtTaskMachineRelative : rtTaskMachineRelativeList) {
            //增加机器ID为-1的公共的配置,公共配置放置前，优先级底
            RtExeMachine rtExeMachineCommon = exeMachineService.getById(-1);
            List<RtExeMachineEnv> rtExeMachineEnvList  = new ArrayList<>();
            if (rtExeMachineCommon != null) {
                List<RtExeMachineEnv> rtExeMachineEnvListCommon = getRtExeMachineEnvList(rtExeMachineCommon.getMachineId());
                rtExeMachineEnvList.addAll(rtExeMachineEnvListCommon);
            }
            RtExeMachine rtExeMachine = exeMachineService.getById(rtTaskMachineRelative.getMachineId());
            List<RtExeMachineEnv> rtExeMachineEnvListMachine = getRtExeMachineEnvList(rtExeMachine.getMachineId());
            rtExeMachineEnvList.addAll(rtExeMachineEnvListMachine);

            exeMachineBoList.add(new ExeMachineBo(rtExeMachine, rtExeMachineEnvList));
        }
        return exeMachineBoList;
    }

    private List<RtExeMachineEnv> getRtExeMachineEnvList(Long machineId) {
        List<RtExeMachineEnv> rtExeMachineEnvList =
                exeMachineEnvService.lambdaQuery()
                        .eq(RtExeMachineEnv::getMachineId, machineId)
                        .eq(RtExeMachineEnv::getState, RecordStateEnum.StateUse.getValue())
                        .list();
        return rtExeMachineEnvList;
    }


    private List<RtParamInfo> getAppParamInfoList(Long taskId, String taskVersion) {
        return getRtParamInfoList(taskId, taskVersion, ExeParamTypeEnum.Application.getValue());
    }

    private List<RtParamInfo> getRunParamInfoList(Long taskId, String taskVersion) {
        return getRtParamInfoList(taskId, taskVersion, ExeParamTypeEnum.Run.getValue());
    }

    private List<RtParamInfo> getConfigParamInfoList(Long taskId, String taskVersion) {
        return getRtParamInfoList(taskId, taskVersion, ExeParamTypeEnum.Config.getValue());
    }

    @Autowired
    private IRtParamInfoService paramInfoService;

    private List<RtParamInfo> getRtParamInfoList(Long taskId, String taskVersion, String paramType) {
        List<RtParamInfo> rtParamInfoList =
                paramInfoService.lambdaQuery()
                        .eq(RtParamInfo::getTaskId, taskId)
                        .eq(RtParamInfo::getTaskVersion, taskVersion)
                        .eq(RtParamInfo::getParamType, paramType)
                        .eq(RtParamInfo::getState, RecordStateEnum.StateUse.getValue())
                        .list();

        return rtParamInfoList;
    }


}
