package cn.ffcs.mtcs.realtime.server.core.ops.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtOpsInfo;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.OpsTypeEnum;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsMark;
import cn.ffcs.mtcs.realtime.server.core.ops.OpsRegister;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 21:29
 * @Version 1.0
 */
@Component
public class OpsMarkImpl implements IOpsMark {

    @Autowired
    private OpsRegister opsRegister;

    /**
     * 1 启动
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean markStart(UserPrincipal principal, Long taskId, String taskVersion) {
        return common(principal, taskId, taskVersion, OpsNameEnum.StartMark.getValue());
    }

    /**
     * 启动异常
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean startException(UserPrincipal principal, Long taskId, String taskVersion) {
        return common(principal, taskId, taskVersion, OpsNameEnum.StartException.getValue());
    }

    /**
     * 2 重启
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean markRestart(UserPrincipal principal, Long taskId, String taskVersion) {
        return common(principal, taskId, taskVersion, OpsNameEnum.RestartMark.getValue());
    }

    /**
     * 重启
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean restartException(UserPrincipal principal, Long taskId, String taskVersion) {
        return common(principal, taskId, taskVersion, OpsNameEnum.RestartException.getValue());
    }

    /**
     * 3 停止
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean markStop(UserPrincipal principal, Long taskId, String taskVersion) {
        return common(principal, taskId, taskVersion, OpsNameEnum.StopMark.getValue());
    }

    /**
     * 停止异常
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean stopException(UserPrincipal principal, Long taskId, String taskVersion) {
        return common(principal, taskId, taskVersion, OpsNameEnum.StopException.getValue());
    }

    /**
     * 真实停止
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean stop(UserPrincipal principal, Long taskId, String taskVersion) {
        return common(principal, taskId, taskVersion, OpsNameEnum.Stop.getValue());
    }

    /**
     * 运行异常
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean runException(UserPrincipal principal, Long taskId, String taskVersion) {
        return common(principal, taskId, taskVersion, OpsNameEnum.RunException.getValue());
    }

    /**
     * 监控异常
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean monitorException(UserPrincipal principal, Long taskId, String taskVersion) {
        return common(principal, taskId, taskVersion, OpsNameEnum.MonitorException.getValue());
    }

    private boolean common(UserPrincipal principal,
                           Long taskId, String taskVersion,
                           String opsName) {
        RtOpsInfo rtOpsInfo =
                opsRegister.registerOps(
                        principal,
                        taskId, taskVersion,
                        OpsTypeEnum.Mark.getValue(), opsName);

        if (null == rtOpsInfo) {
            return false;
        } else {
            return true;
        }
    }
}
