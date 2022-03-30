package cn.ffcs.mtcs.realtime.server.core.ops.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtOpsInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtTaskLastState;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.OpsTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsLogic;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsOperation;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsUpdate;
import cn.ffcs.mtcs.realtime.server.core.ops.OpsRegister;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskLastStateService;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/27/027 11:42
 * @Version 1.0
 */
@Component
@Slf4j
public class OpsUpdateImpl implements IOpsUpdate {

    @Autowired
    private IRtTaskLastStateService taskLastStateService;

    @Autowired
    private OpsRegister opsRegister;

    @Autowired
    private IOpsLogic opsLogic;

    @Autowired
    private IOpsOperation opsOperation;

    /**
     * 1 更新
     * 只有程序在处于停止类型的状态时才能进行更新
     * 刚上线的任务，也不支持更新操作
     *
     * @param principal
     * @param taskId
     * @param taskVersionOld
     * @param taskVersionNew
     * @param engineName
     * @return
     */
    @Override
    public boolean update(UserPrincipal principal,
                          Long taskId, String taskVersionOld, String taskVersionNew,
                          String engineName) throws ExistException {
        RtTaskLastState rtTaskLastState =
                taskLastStateService.getOne(
                        Wrappers.<RtTaskLastState>lambdaQuery()
                                .eq(RtTaskLastState::getTaskId, taskId)
                                .eq(RtTaskLastState::getTaskVersion, taskVersionOld)
                                .eq(RtTaskLastState::getState, RecordStateEnum.StateUse.getValue()),
                        false);

        List<String> preOpsList = Arrays.asList(
                OpsNameEnum.StartException.getValue(),
                OpsNameEnum.RestartException.getValue(),
                OpsNameEnum.Stop.getValue(),
                OpsNameEnum.RunException.getValue(),
                OpsNameEnum.Online.getValue());

        if (null == rtTaskLastState || !preOpsList.contains(rtTaskLastState.getTaskState())) {
            throw new ExistException("该任务的状态无法进行更新操作！");
        }

        /**
         * 1 注册更新操作
         * 2 进行下线
         * 3 进行上线
         * 4 进行重启
         */
        // 1 注册更新操作
        boolean flag = true;

        // 1.1 注册操作
        RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                principal,
                taskId, taskVersionOld,
                OpsTypeEnum.Ops.getValue(), OpsNameEnum.Update.getValue());

        // 2 进行下线
        flag = flag && opsLogic.offline(principal, taskId, taskVersionOld);

        // 3 进行上线
       // flag = flag && opsLogic.online(principal, taskId, taskVersionNew);

        // 4 进行重启
        // 不进行重启操作，在前台进行重启操作
        /*flag = flag && opsOperation.restart(principal,
                taskId, taskVersionNew,
                engineName,
                -1L);*/

        return flag;
    }
}
