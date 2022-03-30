package cn.ffcs.mtcs.realtime.server.service.business;

import cn.ffcs.mtcs.realtime.common.entity.RtTaskLastState;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.core.ops.*;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskLastStateService;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/8/008 11:50
 * @Version 1.0
 */
@Component
public class RealtimeOperationBusiness {

    /**
     * 1 上线
     * 2 下线
     * 3 启动
     * 4 重启
     * 5 停止
     * 6 更新
     */

    @Autowired
    private IOpsLogic opsLogic;

    @Autowired
    private IOpsMark opsMark;

    @Autowired
    private IOpsOperation opsOperation;

    @Autowired
    private IOpsUpdate opsUpdate;

    @Autowired
    private OpsRegister opsRegister;

    /**
     * 1 任务上线
     *
     * @param principal   用户信息
     * @param taskId
     * @param taskVersion
     * @return
     */
    public boolean onlineOperation(UserPrincipal principal,
                                   Long taskId, String taskVersion) throws ExistException {
        return opsLogic.online(principal, taskId, taskVersion);
    }

    /**
     * 2 任务下线
     *
     * @param principal
     * @param taskId      任务ID
     * @param taskVersion 任务ID
     * @return
     */
    public boolean offlineOperation(UserPrincipal principal,
                                    Long taskId, String taskVersion) throws ExistException {
        return opsLogic.offline(principal, taskId, taskVersion);
    }

    /**
     * 3 启动任务
     *
     * @param principal
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @param engineName  引擎名
     * @return
     */
    public boolean startOperation(UserPrincipal principal,
                                  Long taskId, String taskVersion,
                                  String engineName) throws ExistException {
        RtTaskLastState rtTaskLastState = getRtTaskLastState(taskId, taskVersion);

        List<String> preStateList = Arrays.asList(
                OpsNameEnum.Online.getValue(),
                OpsNameEnum.StartException.getValue());

        if (!preStateList.contains(rtTaskLastState.getTaskState())) {
            throw new ExistException("任务当前的状态无法进行启动操作！状态为Online或者是StartException才可以操作启动操作！");
        }


        /**
         * 1 标记启动
         * 2 启动中
         */
        boolean flag = opsMark.markStart(principal, taskId, taskVersion);
        if (engineName.equalsIgnoreCase(EngineTypeEnum.Spark.getValue())) {
            flag = flag && opsOperation.start(principal, taskId, taskVersion, engineName);
        } else {
            //rtTaskLastState状态有做变更需要取最新的记录进行处理
            rtTaskLastState = getRtTaskLastState(taskId, taskVersion);
            flag = flag && opsOperation.appStart(principal, taskId, taskVersion, engineName, rtTaskLastState);
        }

        return flag;
    }


    /**
     * 4 重启任务
     *
     * @param principal
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @param engineName  引擎名
     * @return
     */
    public boolean restartOperation(
            UserPrincipal principal,
            Long taskId, String taskVersion,
            String engineName) throws ExistException {

        RtTaskLastState rtTaskLastState = getRtTaskLastState(taskId, taskVersion);

        List<String> preStateList = Arrays.asList(
                OpsNameEnum.Online.getValue(),
                OpsNameEnum.RestartException.getValue(),
                OpsNameEnum.RunException.getValue(),
                OpsNameEnum.Stop.getValue());

        if (!preStateList.contains(rtTaskLastState.getTaskState())) {
            throw new ExistException("任务当前的状态无法进行重启操作！");
        }

        /**
         * 1 标记重启
         * 2 重启中
         */
        boolean flag = opsMark.markRestart(principal, taskId, taskVersion);
        if (engineName.equalsIgnoreCase(EngineTypeEnum.Spark.getValue())) {
            flag = flag && opsOperation.restart(principal, taskId, taskVersion, engineName, -1L);
        } else {
            //rtTaskLastState状态有做变更需要取最新的记录进行处理
            rtTaskLastState = getRtTaskLastState(taskId, taskVersion);
            flag = flag && opsOperation.appRestart(principal, taskId, taskVersion, engineName, rtTaskLastState, -1L);
        }

        return flag;
    }

    /**
     * 5 停止任务
     *
     * @param principal
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @param engineName  引擎名
     * @return
     */
    public boolean stopOperation(
            UserPrincipal principal,
            Long taskId, String taskVersion,
            String engineName) throws ExistException {

        RtTaskLastState rtTaskLastState = getRtTaskLastState(taskId, taskVersion);

        List<String> preStateList = Arrays.asList(
                OpsNameEnum.Running.getValue());

        if (!preStateList.contains(rtTaskLastState.getTaskState())) {
            throw new ExistException("任务当前的状态无法进行停止操作！");
        }

        Long exeId = opsRegister.getRunningExeId(taskId, taskVersion);
        if (exeId == -1L) {
            throw new ExistException("该程序未执行成功，无法停止！");
        }


        /**
         * 1 标记停止
         * 2 停止中
         */
        boolean flag = opsMark.markStop(principal, taskId, taskVersion);
        if (engineName.equalsIgnoreCase(EngineTypeEnum.Spark.getValue())) {
            flag = flag && opsOperation.stop(principal, taskId, taskVersion, engineName);
        } else {
            //rtTaskLastState状态有做变更需要取最新的记录进行处理
            rtTaskLastState = getRtTaskLastState(taskId, taskVersion);
            flag = flag && opsOperation.appStop(principal, taskId, taskVersion, engineName, rtTaskLastState);
        }
        return flag;
    }

    /**
     * 6 更新任务
     *
     * @param taskId         旧任务ID
     * @param taskVersionOld 旧任务版本
     * @param taskVersionNew 新任务版本
     * @return
     */
    public boolean updateOperation(
            UserPrincipal principal,
            Long taskId,
            String taskVersionOld, String taskVersionNew,
            String engineName) throws ExistException {
        return opsUpdate.update(principal, taskId, taskVersionOld, taskVersionNew, engineName);
    }

    @Autowired
    private IRtTaskLastStateService taskLastStateService;

    private RtTaskLastState getRtTaskLastState(Long taskId, String taskVersion) throws ExistException {
        RtTaskLastState rtTaskLastState =
                taskLastStateService.getOne(
                        Wrappers.<RtTaskLastState>lambdaQuery()
                                .eq(RtTaskLastState::getTaskId, taskId)
                                .eq(RtTaskLastState::getTaskVersion, taskVersion)
                                .eq(RtTaskLastState::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        if (null == rtTaskLastState) {
            throw new ExistException("没有该任务的最新状态！");
        }
        return rtTaskLastState;
    }
}
