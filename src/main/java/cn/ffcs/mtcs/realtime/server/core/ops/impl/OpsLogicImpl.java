package cn.ffcs.mtcs.realtime.server.core.ops.impl;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.OpsTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.constants.TaskLogicStateEnum;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineFactory;
import cn.ffcs.mtcs.realtime.server.core.engine.IEngine;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsLogic;
import cn.ffcs.mtcs.realtime.server.core.ops.OpsRegister;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 19:20
 * @Version 1.0
 */
@Component
public class OpsLogicImpl implements IOpsLogic {

    @Autowired
    private IRtTaskLogicStateService taskLogicStateService;

    @Autowired
    private IRtExeMetaInfoService exeMetaInfoService;

    @Autowired
    private OpsRegister opsRegister;

    @Autowired
    private IDetailTaskInfoBoService taskDetailInfoBoService;

    @Autowired
    private IRtTaskLastStateService taskLastStateService;

    @Autowired
    private IRtOpsInfoService opsInfoService;

    /**
     * 1 上线
     *
     * @param principal   当前用户信息
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @return
     */
    @Override
    public boolean online(UserPrincipal principal, Long taskId, String taskVersion) throws ExistException {

        /**
         * 1 获取任务当前状态
         * 2 刚创建
         *      1 注册逻辑表
         *      2 注册执行元信息表
         * 3 下线转为上线
         *      1 修改逻辑表为上线
         * 4 注册操作动作
         */

        boolean flag = true;

        // 1 获取任务当前状态
        String taskState = getTaskState(taskId, taskVersion);
        // 2 只要发布和下线的状态，才能执行上线
        if (OpsNameEnum.Release.getValue().equals(taskState)) {
            // 0 解决任务上线时因为什么原因，导致在逻辑表中创建了多个记录
            RtTaskLogicState preRtTaskLogicState =
                    taskLogicStateService.getOne(
                            Wrappers.<RtTaskLogicState>lambdaQuery()
                                    .eq(RtTaskLogicState::getTaskId, taskId)
                                    .eq(RtTaskLogicState::getTaskVersion, taskVersion)
                                    .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue()),
                            false);
            if (preRtTaskLogicState != null) {
                throw  new ExistException("已经存在该任务的逻辑状态记录！");
            }

            // 1 注册逻辑表
            RtTaskLogicState rtTaskLogicStateNew =
                    createRtTaskLogicState(principal, taskId, taskVersion);
            flag = flag && taskLogicStateService.save(rtTaskLogicStateNew);
            // 2 注册执行元信息表
            RtExeMetaInfo rtExeMetaInfo = createRtExeMetaInfo(principal, taskId, taskVersion);
            flag = flag && exeMetaInfoService.save(rtExeMetaInfo);
        } else if (OpsNameEnum.Offline.getValue().equals(taskState)) {
            // 查询最新的一个在线任务
            RtTaskLogicState rtTaskLogicState =
                    getLastRtTaskLogicState(taskId, TaskLogicStateEnum.Online.getValue());
            // 1 如果有在线的任务，那么就建议进行修改操作
            if (null != rtTaskLogicState && !rtTaskLogicState.getTaskVersion().equals(taskVersion)) {
                throw new ExistException("该任务存在在用版本，建议进行修改操作！");
            } else {
                RtTaskLogicState rtTaskLogicStateOffline =
                        getRtTaskLogicState(taskId, taskVersion);
                rtTaskLogicStateOffline.setTaskState(TaskLogicStateEnum.Online.getValue());
                rtTaskLogicStateOffline.setMdfyUser(principal.getUserId());
                rtTaskLogicStateOffline.setMdfyTime(LocalDateTime.now());
                flag = flag && taskLogicStateService.updateById(rtTaskLogicStateOffline);
            }
        } else {
            throw new ExistException("只有发布和下线的状态，才能执行上线");
        }

        opsRegister.registerOps(principal,
                taskId, taskVersion,
                OpsTypeEnum.Logic.getValue(), OpsNameEnum.Online.getValue());

        return flag;
    }


    private RtTaskLogicState createRtTaskLogicState(UserPrincipal principal,
                                                    Long taskId, String taskVersion) {
        RtTaskLogicState rtTaskLogicState = new RtTaskLogicState();
        rtTaskLogicState.setTaskId(taskId);
        rtTaskLogicState.setTaskVersion(taskVersion);
        rtTaskLogicState.setTaskState(TaskLogicStateEnum.Online.getValue());
        rtTaskLogicState.setNodeType("");
        rtTaskLogicState.setStateDesc("");
        rtTaskLogicState.setCrtUser(principal.getUserId());
        rtTaskLogicState.setCrtTeam(principal.getDefaultTeamId());
        rtTaskLogicState.setCrtTime(LocalDateTime.now());
        rtTaskLogicState.setMdfyUser(principal.getUserId());
        rtTaskLogicState.setMdfyTime(LocalDateTime.now());
        rtTaskLogicState.setState(RecordStateEnum.StateUse.getValue());
        return rtTaskLogicState;
    }

    private RtExeMetaInfo createRtExeMetaInfo(UserPrincipal principal, Long taskId, String taskVersion) {
        RtExeMetaInfo rtExeMetaInfo = new RtExeMetaInfo();
        // 1 获取任务信息
        // 2 获取流程信息
        // 3 获取执行机信息
        // 4 获取应用参数信息
        // 5 获取运行参数信息
        // 6 获取运行参数配置信息
        DetailTaskInfoBo taskDetailInfoBo =
                taskDetailInfoBoService.getTaskDetailInfoBo(taskId, taskVersion);

        rtExeMetaInfo.setTaskId(taskId);
        rtExeMetaInfo.setTaskVersion(taskVersion);


        rtExeMetaInfo.setExeMachine(JSON.toJSONString(taskDetailInfoBo.getRtExeMachineList()));

        String engine = taskDetailInfoBo.getRtFlowInfo().getEngine();
        IEngine iEngine = EngineFactory.getIEngine(engine);
        String uuid = java.util.UUID.randomUUID().toString();
        rtExeMetaInfo.setRunId(String.format("%s-%s-%s", engine, taskId, uuid ));

        rtExeMetaInfo.setExeCommand(iEngine.createExeCommand(taskDetailInfoBo));
        // 这个信息和ssh服务中的exeParam是一致的
        rtExeMetaInfo.setExeParam(iEngine.createExeParam(principal, taskDetailInfoBo));

        rtExeMetaInfo.setRestartType(taskDetailInfoBo.getRtTaskInfo().getHandleType());
        rtExeMetaInfo.setRestartInterval(taskDetailInfoBo.getRtTaskInfo().getRestartInterval());
        rtExeMetaInfo.setRestartNum(taskDetailInfoBo.getRtTaskInfo().getRestartNum());

        rtExeMetaInfo.setState(RecordStateEnum.StateUse.getValue());

        return rtExeMetaInfo;
    }

    /**
     * 查询最新的一个在线任务
     *
     * @param taskId
     * @param taskState
     * @return
     */
    private RtTaskLogicState getLastRtTaskLogicState(Long taskId, String taskState) {
        RtTaskLogicState rtTaskLogicState =
                taskLogicStateService.getOne(
                        Wrappers.<RtTaskLogicState>lambdaQuery()
                                .eq(RtTaskLogicState::getTaskId, taskId)
                                .eq(RtTaskLogicState::getTaskState, taskState)
                                .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue())
                                .orderByDesc(RtTaskLogicState::getStateId),
                        false);
        return rtTaskLogicState;
    }


    /**
     * 2 下线
     *
     * @param principal   当前用户信息
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @return
     */
    @Override
    public boolean offline(UserPrincipal principal, Long taskId, String taskVersion) throws ExistException {
        /**
         * 1 获取任务当前状态
         * 2 修改逻辑表为下线
         * 3 注册操作动作
         */

        boolean flag = true;

        String taskState = getTaskState(taskId, taskVersion);

        if (OpsNameEnum.Online.getValue().equals(taskState)
                || OpsNameEnum.StartException.getValue().equals(taskState)
                || OpsNameEnum.RestartException.getValue().equals(taskState)
                || OpsNameEnum.RunException.getValue().equals(taskState)
                || OpsNameEnum.Stop.getValue().equals(taskState)) {

            boolean usingVersionFlag =
                    checkUsingVersion(taskId, taskVersion, TaskLogicStateEnum.Online.getValue());

            if (usingVersionFlag) {
                // 获取所有的需要被下线的任务
                List<RtTaskLogicState> rtTaskLogicStateList =
                        getRtTaskLogicStateList(taskId, taskVersion, TaskLogicStateEnum.Online.getValue());
                for (RtTaskLogicState rtTaskLogicState : rtTaskLogicStateList) {
                    rtTaskLogicState.setTaskState(TaskLogicStateEnum.Offline.getValue());
                    rtTaskLogicState.setMdfyUser(principal.getUserId());
                    rtTaskLogicState.setMdfyTime(LocalDateTime.now());
                    flag = flag && taskLogicStateService.updateById(rtTaskLogicState);
                    flag = flag &&
                            (null != opsRegister.registerOps(principal,
                                    rtTaskLogicState.getTaskId(), rtTaskLogicState.getTaskVersion(),
                                    OpsTypeEnum.Logic.getValue(), OpsNameEnum.Offline.getValue()));
                }
            } else {
                // 直接向该任务下线
                RtTaskLogicState rtTaskLogicState =
                        getLastRtTaskLogicState(taskId, taskVersion, TaskLogicStateEnum.Online.getValue());
                rtTaskLogicState.setTaskState(TaskLogicStateEnum.Offline.getValue());
                rtTaskLogicState.setMdfyUser(principal.getUserId());
                rtTaskLogicState.setMdfyTime(LocalDateTime.now());
                flag = flag && taskLogicStateService.updateById(rtTaskLogicState);
                flag = flag &&
                        (null != opsRegister.registerOps(principal,
                                rtTaskLogicState.getTaskId(), rtTaskLogicState.getTaskVersion(),
                                OpsTypeEnum.Logic.getValue(), OpsNameEnum.Offline.getValue()));
            }
        } else {
            throw new ExistException("当前任务状态无法进行下线操作！");
        }
        return flag;
    }

    /**
     * 检测是不是在用的任务
     *
     * @param taskId
     * @param taskVersion
     * @param taskState
     * @return
     */
    @Override
    public boolean checkUsingVersion(Long taskId, String taskVersion, String taskState) {
        List<RtTaskLogicState> rtTaskLogicStateList =
                taskLogicStateService.lambdaQuery()
                        .eq(RtTaskLogicState::getTaskId, taskId)
                        .eq(RtTaskLogicState::getTaskState, taskState)
                        .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue())
                        .orderByAsc(RtTaskLogicState::getStateId)
                        .list();

        for (RtTaskLogicState rtTaskLogicState : rtTaskLogicStateList) {
            if (opsInfoService.checkUsingVersion(rtTaskLogicState.getTaskId(), rtTaskLogicState.getTaskVersion())) {
                if (taskVersion.equals(rtTaskLogicState.getTaskVersion())) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        if (rtTaskLogicStateList.get(rtTaskLogicStateList.size() - 1).getTaskVersion().equals(taskVersion)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 获取所有需要被下线的任务
     *
     * @param taskId
     * @param taskVersion
     * @param taskState
     * @return
     */
    private List<RtTaskLogicState> getRtTaskLogicStateList(Long taskId, String taskVersion,
                                                           String taskState) {
        RtTaskLogicState nowRtTaskLogicState = getLastRtTaskLogicState(taskId, taskVersion, taskState);

        List<RtTaskLogicState> rtTaskLogicStateList = getRtTaskLogicStateList(taskId, taskState);

        List<RtTaskLogicState> resultList = new ArrayList<>();
        for (RtTaskLogicState rtTaskLogicState : rtTaskLogicStateList) {
            if (rtTaskLogicState.getStateId() > nowRtTaskLogicState.getStateId()) {
                resultList.add(rtTaskLogicState);
            }
            if (rtTaskLogicState.getStateId().equals(nowRtTaskLogicState.getStateId())) {
                resultList.add(rtTaskLogicState);
                // 是因为逆序了，所以后面的不用检测了，其实后面应该也没有元素了
                break;
            }
        }
        return resultList;
    }

    /**
     * 获取任务在线或者下线的所有版本， 并且逆序排序
     *
     * @param taskId
     * @param taskState
     * @return
     */
    private List<RtTaskLogicState> getRtTaskLogicStateList(Long taskId, String taskState) {
        return taskLogicStateService.lambdaQuery()
                .eq(RtTaskLogicState::getTaskId, taskId)
                .eq(RtTaskLogicState::getTaskState, taskState)
                .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue())
                .orderByDesc(RtTaskLogicState::getStateId)
                .list();
    }


    private RtTaskLogicState getLastRtTaskLogicState(Long taskId, String taskVersion, String taskState) {
        RtTaskLogicState rtTaskLogicState =
                taskLogicStateService.getOne(
                        Wrappers.<RtTaskLogicState>lambdaQuery()
                                .eq(RtTaskLogicState::getTaskId, taskId)
                                .eq(RtTaskLogicState::getTaskVersion, taskVersion)
                                .eq(RtTaskLogicState::getTaskState, taskState)
                                .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        return rtTaskLogicState;
    }


    /**
     * 获取任务最新的状态
     *
     * @param taskId
     * @param taskVersion
     * @return
     */
    private String getTaskState(Long taskId, String taskVersion) {
        RtTaskLastState rtTaskLastStateInfo =
                taskLastStateService.getOne(
                        Wrappers.<RtTaskLastState>lambdaQuery()
                                .eq(RtTaskLastState::getTaskId, taskId)
                                .eq(RtTaskLastState::getTaskVersion, taskVersion)
                                .eq(RtTaskLastState::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        return rtTaskLastStateInfo.getTaskState();
    }

    private RtTaskLogicState getRtTaskLogicState(Long taskId, String taskVersion) {
        RtTaskLogicState rtTaskLogicState =
                taskLogicStateService.getOne(
                        Wrappers.<RtTaskLogicState>lambdaQuery()
                                .eq(RtTaskLogicState::getTaskId, taskId)
                                .eq(RtTaskLogicState::getTaskVersion, taskVersion)
                                .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        return rtTaskLogicState;
    }
}
