package cn.ffcs.mtcs.realtime.server.core.ops.impl;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.OpsTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.constants.monitor.MonitorContents;
import cn.ffcs.mtcs.realtime.server.core.job.SchedulerXxlJob;
import cn.ffcs.mtcs.realtime.server.core.monitor.IMonitor;
import cn.ffcs.mtcs.realtime.server.core.monitor.MonitorFactory;
import cn.ffcs.mtcs.realtime.server.core.operation.IOperation;
import cn.ffcs.mtcs.realtime.server.core.operation.OperationFactory;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsOperation;
import cn.ffcs.mtcs.realtime.server.core.ops.OpsRegister;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.service.data.IRtMonitorService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsAttachmentService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsInfoService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskLastStateService;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/20/020 10:43
 * @Version 1.0
 */
@Component
@Slf4j
public class OpsOperationImpl implements IOpsOperation {
    /**
     * 1 启动中
     * 2 重启中
     * 3 停止中
     * 4 运行
     */

    @Autowired
    private OpsRegister opsRegister;

    @Autowired
    private IRtMonitorService monitorService;

    @Autowired
    private IRtOpsInfoService opsInfoService;

    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;

    @Autowired
    private IRtMonitorService rtMonitorService;

    @Autowired
    private SchedulerXxlJob schedulerXxlJob;

    @Autowired
    private IRtTaskLastStateService taskLastStateService;

    /**
     * 1 启动中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @return
     */
    @Override
    public boolean start(UserPrincipal principal,
                         Long taskId, String taskVersion,
                         String engineName) {
        /**
         * 1 注册操作
         * 2 注册操作附属信息
         * 3 调用SSH服务，运行程序
         * 4 开启监控
         */
        boolean flag = true;

        // 1.1 注册操作
        RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                principal,
                taskId, taskVersion,
                OpsTypeEnum.Ops.getValue(), OpsNameEnum.Starting.getValue());

        // 1.2 注册操作附属信息
        RtOpsAttachment rtOpsAttachment = opsRegister.registerOpsAttachment(
                String.valueOf(rtOpsInfo.getOpsId()),
                taskId, taskVersion,
                OpsNameEnum.Starting.getValue(),engineName);

        // 2 调用SSH服务，运行程序
        IOperation iOperation = OperationFactory.getIOperation(engineName);
        flag = flag && iOperation.start(principal, rtOpsAttachment);


        IMonitor iMonitor = MonitorFactory.getIMonitor(engineName);
        // 3.1 注册监控信息
        RtMonitor rtMonitor = null;
        if (engineName.equalsIgnoreCase(EngineTypeEnum.Spark.getValue())) {
            rtMonitor = createRtMonitor(rtOpsAttachment, OpsNameEnum.Starting.getValue());
        } else {
            rtMonitor = createRtMonitor(taskId,taskVersion, rtOpsAttachment, OpsNameEnum.Starting.getValue(), null);
        }
        // 3.2 开启监控
        // 这个结果只是代表开启了监控
        flag = flag && iMonitor.start(rtMonitor);

        // 返回这个值也只能说明，一切准备工作是成功的
        // 具体请成功没有需要通过每次的监测值对比看出
        return flag;
    }


    /**
     * 2 重启中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @param retryId
     * @return
     */
    @Override
    public boolean restart(UserPrincipal principal,
                           Long taskId, String taskVersion,
                           String engineName,
                           Long retryId) {
        /**
         * 1 注册操作
         * 2 注册操作附属信息
         * 3 调用SSH服务，运行程序
         * 4 开启监控
         */
        boolean flag = true;

        // 1.1 注册操作
        RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                principal,
                taskId, taskVersion,
                OpsTypeEnum.Ops.getValue(), OpsNameEnum.Restarting.getValue());

        // 1.2 注册操作附属信息
        RtOpsAttachment rtOpsAttachment = opsRegister.registerOpsAttachment(
                String.valueOf(rtOpsInfo.getOpsId()),
                taskId, taskVersion,
                OpsNameEnum.Restarting.getValue(),engineName);

        // 1.3 注册重试表
        RtRetry rtRetry = new RtRetry();
        if (null != retryId && retryId != -1L) {
            rtRetry = opsRegister.getRetry(retryId);
        } else {
            rtRetry = opsRegister.registerRtRetry(
                    rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion(),
                    OpsNameEnum.Restarting.getValue(),
                    rtOpsAttachment.getRestartNum());
        }

        // 2 调用SSH服务，运行程序
        IOperation iOperation = OperationFactory.getIOperation(engineName);
        flag = flag && iOperation.restart(principal, rtOpsAttachment);


        IMonitor iMonitor = MonitorFactory.getIMonitor(engineName);
        // 3.1 注册监控信息
        RtMonitor rtMonitor;
        if (engineName.equalsIgnoreCase(EngineTypeEnum.Spark.getValue())) {
            rtMonitor = createRtMonitor(rtOpsAttachment, OpsNameEnum.Restarting.getValue());
        } else {
            rtMonitor = createRtMonitor(taskId,taskVersion, rtOpsAttachment, OpsNameEnum.Restarting.getValue(), null);
        }

        // 3.2 开启监控
        // 这个结果只是代表开启了监控
        flag = flag && iMonitor.restart(rtMonitor);

        // 返回这个值也只能说明，一切准备工作是成功的
        // 具体请成功没有需要通过每次的监测值对比看出
        return flag;
    }

    /**
     * 3 停止中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @return
     */
    @Override
    public boolean stop(UserPrincipal principal, Long taskId, String taskVersion, String engineName) {

        boolean flag = true;


        // 如果存在运行运行监控，则需要停掉运行监控
        flag = closeRunListen(taskId, taskVersion);


        /**
         * 1 注册操作
         * 2 注册操作附属信息
         * 3 调用SSH服务，运行程序
         * 4 开启监控
         */
        // 1.1 注册操作
        RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                principal,
                taskId, taskVersion,
                OpsTypeEnum.Ops.getValue(), OpsNameEnum.Stopping.getValue());

        // 1.2 注册操作附属信息
        RtOpsAttachment rtOpsAttachment = opsRegister.registerOpsAttachment(
                String.valueOf(rtOpsInfo.getOpsId()),
                taskId, taskVersion,
                OpsNameEnum.Stopping.getValue(),engineName);

        // 2 调用SSH服务，运行程序
        IOperation iOperation = OperationFactory.getIOperation(engineName);
        flag = flag && iOperation.stop(principal, rtOpsAttachment);


        IMonitor iMonitor = MonitorFactory.getIMonitor(engineName);
        // 3.1 注册监控信息
        RtMonitor rtMonitor = createRtMonitor(rtOpsAttachment, OpsNameEnum.Stopping.getValue());
        // 3.2 开启监控
        // 这个结果只是代表开启了监控
        flag = flag && iMonitor.stop(rtMonitor);

        // 返回这个值也只能说明，一切准备工作是成功的
        // 具体请成功没有需要通过每次的监测值对比看出
        return flag;
    }


    private boolean closeRunListen(Long taskId, String taskVersion) {
        RtOpsInfo rtOpsInfo =
                opsInfoService.getOne(
                        Wrappers.<RtOpsInfo>lambdaQuery()
                                .eq(RtOpsInfo::getTaskId, taskId)
                                .eq(RtOpsInfo::getTaskVersion, taskVersion)
                                .eq(RtOpsInfo::getOpsName, OpsNameEnum.Running.getValue())
                                .eq(RtOpsInfo::getState, RecordStateEnum.StateUse.getValue())
                                .orderByDesc(RtOpsInfo::getOpsId),
                        false);
        if (null == rtOpsInfo) {
            return true;
        }

        RtOpsAttachment rtOpsAttachment =
                opsAttachmentService.getOne(
                        Wrappers.<RtOpsAttachment>lambdaQuery()
                                .eq(RtOpsAttachment::getOpsId, rtOpsInfo.getOpsId())
                                .eq(RtOpsAttachment::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        if (null == rtOpsAttachment) {
            return true;
        }

        RtMonitor rtMonitor =
                rtMonitorService.getOne(
                        Wrappers.<RtMonitor>lambdaQuery()
                                .eq(RtMonitor::getOpsAttachmentId, rtOpsAttachment.getId())
                                .eq(RtMonitor::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        if (null == rtMonitor) {
            return true;
        }

        int xxlJobId = rtMonitor.getXxlJobId();

        return schedulerXxlJob.stopScheduler(xxlJobId);
    }

    /**
     * 4 运行中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @return
     */
    @Override
    public boolean run(UserPrincipal principal, Long taskId, String taskVersion, String engineName) {
        /**
         * 1 注册操作
         * 2 注册操作附属信息
         * 3 开启监控
         */
        boolean flag = true;

        // 1.1 注册操作
        RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                principal,
                taskId, taskVersion,
                OpsTypeEnum.Ops.getValue(), OpsNameEnum.Running.getValue());

        // 1.2 注册操作附属信息
        RtOpsAttachment rtOpsAttachment = opsRegister.registerOpsAttachment(
                String.valueOf(rtOpsInfo.getOpsId()),
                taskId, taskVersion,
                OpsNameEnum.Running.getValue(),engineName);

        IMonitor iMonitor = MonitorFactory.getIMonitor(engineName);
        // 3.1 注册监控信息
        RtMonitor rtMonitor = createRtMonitor(rtOpsAttachment, OpsNameEnum.Running.getValue());
        // 3.2 开启监控
        // 这个结果只是代表开启了监控
        flag = flag && iMonitor.run(rtMonitor);

        // 返回这个值也只能说明，一切准备工作是成功的
        // 具体请成功没有需要通过每次的监测值对比看出
        return flag;
    }

    private RtMonitor createRtMonitor(RtOpsAttachment rtOpsAttachment, String opsName) {
        return createRtMonitor(null,null,rtOpsAttachment, -1L, opsName, null);
    }

    private RtMonitor createRtMonitor(Long taskId, String taskVersion,
                                      RtOpsAttachment rtOpsAttachment, String opsName, Integer xxlJobId) {
        return createRtMonitor(taskId, taskVersion, rtOpsAttachment, -1L, opsName, xxlJobId);
    }

    private RtMonitor createRtMonitor(Long taskId, String taskVersion,
                                      RtOpsAttachment rtOpsAttachment, Long retryId,
                                      String opsName ,Integer xxlJobId) {
        RtMonitor rtMonitor = new RtMonitor();
        //rtMonitor.setMonitorId();
        rtMonitor.setOpsAttachmentId(rtOpsAttachment.getId());
        rtMonitor.setTaskId(taskId);
        rtMonitor.setTaskVersion(taskVersion);
        rtMonitor.setRetryId(retryId);
        // 这样监控的目的是必能能包含实际发生的时间点，单位秒
        int restartInterval =
                (null == rtOpsAttachment.getRestartInterval()) ?
                        MonitorContents.TimeInterval : rtOpsAttachment.getRestartInterval();
        int monitorNum = (null == rtOpsAttachment.getRestartNum()) ?
                MonitorContents.MonitorMaxCount : rtOpsAttachment.getRestartNum();

        if (opsName.equals(OpsNameEnum.Starting.getValue())) {
            rtMonitor.setMonitorInterval(restartInterval);
            rtMonitor.setMonitorNum(monitorNum);
        } else if (opsName.equals(OpsNameEnum.Restarting.getValue())) {
            rtMonitor.setMonitorInterval(restartInterval);
            rtMonitor.setMonitorNum(monitorNum);
        } else if (opsName.equals(OpsNameEnum.Stopping.getValue())) {
            rtMonitor.setMonitorInterval(restartInterval);
            rtMonitor.setMonitorNum(monitorNum);
        } else if (opsName.equals(OpsNameEnum.Running.getValue())) {
            rtMonitor.setMonitorInterval(restartInterval);
            rtMonitor.setMonitorNum(monitorNum);
        }

        // todo 监控参数
        rtMonitor.setMonitorParam(JSON.toJSONString(rtOpsAttachment));
        // todo
        rtMonitor.setMonitorType("");
        // todo
        rtMonitor.setMonitorExpect("");
        //rtMonitor.setXxlJobId();
        rtMonitor.setMonitorCount(0);
        //rtMonitor.setMonitorActual();
        //rtMonitor.setMonitorTime();
        if (xxlJobId != null) {
            rtMonitor.setXxlJobId(xxlJobId);
        }
        rtMonitor.setState(RecordStateEnum.StateUse.getValue());

        monitorService.save(rtMonitor);
        return rtMonitor;
    }



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

    @Override
    public boolean appStart(UserPrincipal principal, Long taskId, String taskVersion, String engineName,
                            RtTaskLastState  rtTaskLastState) {
        /**
         * 1 注册操作
         * 2 注册操作附属信息
         * 3 调用SSH服务，运行程序
         * 4 开启监控
         */
        boolean flag = true;

        // 1.1 注册操作
        RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                principal,
                taskId, taskVersion,
                OpsTypeEnum.Ops.getValue(), OpsNameEnum.Starting.getValue());

        // 1.2 注册操作附属信息,包含模板的伪码信息
        RtOpsAttachment rtOpsAttachment = opsRegister.registerOpsAttachment(
                String.valueOf(rtOpsInfo.getOpsId()),
                taskId, taskVersion,
                OpsNameEnum.Starting.getValue(),engineName);

        //设置操作的日志信息
       /* rtOpsInfo.setExeLogFile(rtOpsAttachment.getExeLogFile());
        opsInfoService.updateById(rtOpsInfo);*/

        // 2 调用SSH服务，运行程序
        IOperation iOperation = OperationFactory.getIOperation(engineName);
        flag = flag && iOperation.start(principal, rtOpsAttachment);


        IMonitor iMonitor = MonitorFactory.getIMonitor(engineName);
        RtMonitor rtMonitor;
        if (engineName.equalsIgnoreCase(EngineTypeEnum.Spark.getValue())) {
            rtMonitor = createRtMonitor(rtOpsAttachment, OpsNameEnum.Starting.getValue());
        } else {
            rtMonitor = createRtMonitor(taskId,taskVersion, rtOpsAttachment, OpsNameEnum.Starting.getValue(),
                    rtTaskLastState.getXxlJobId());
        }

        // 3.2 开启监控
        // 这个结果只是代表开启了监控
        flag = flag && iMonitor.start(rtMonitor);
       //获取xxljobid
        rtMonitor = monitorService.getById(rtMonitor.getMonitorId());
        if (rtTaskLastState.getXxlJobId() == null && rtMonitor.getXxlJobId() != null) {
            rtTaskLastState.setXxlJobId(rtMonitor.getXxlJobId());
        }
        rtTaskLastState.setMonitorId(rtMonitor.getMonitorId());
        rtTaskLastState.setOpsId(rtOpsInfo.getOpsId());
        taskLastStateService.updateById(rtTaskLastState);
        // 返回这个值也只能说明，一切准备工作是成功的
        // 具体请成功没有需要通过每次的监测值对比看出
        return flag;
    }

    @Override
    public boolean appRestart(UserPrincipal principal, Long taskId, String taskVersion, String engineName,
                              RtTaskLastState rtTaskLastState, Long retryId) {
        /**
         * 1 注册操作
         * 2 注册操作附属信息
         * 3 调用SSH服务，运行程序
         * 4 开启监控
         */
        boolean flag = true;

        // 1.1 注册操作
        RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                principal,
                taskId, taskVersion,
                OpsTypeEnum.Ops.getValue(), OpsNameEnum.Restarting.getValue());

        // 1.2 注册操作附属信息
        RtOpsAttachment rtOpsAttachment = opsRegister.registerOpsAttachment(
                String.valueOf(rtOpsInfo.getOpsId()),
                taskId, taskVersion,
                OpsNameEnum.Restarting.getValue(),engineName);

        //设置操作的日志信息
       /* rtOpsInfo.setExeLogFile(rtOpsAttachment.getExeLogFile());
        opsInfoService.updateById(rtOpsInfo);
*/
        // 1.3 注册重试表
        RtRetry rtRetry = new RtRetry();
        if (null != retryId && retryId != -1L) {
            rtRetry = opsRegister.getRetry(retryId);
        } else {
            rtRetry = opsRegister.registerRtRetry(
                    rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion(),
                    OpsNameEnum.Restarting.getValue(),
                    rtOpsAttachment.getRestartNum());
        }

        // 2 调用SSH服务，运行程序
        IOperation iOperation = OperationFactory.getIOperation(engineName);
        flag = flag && iOperation.restart(principal, rtOpsAttachment);


        IMonitor iMonitor = MonitorFactory.getIMonitor(engineName);
        // 3.1 注册监控信息
        RtMonitor rtMonitor;
        if (engineName.equalsIgnoreCase(EngineTypeEnum.Spark.getValue())) {
            rtMonitor = createRtMonitor(rtOpsAttachment, OpsNameEnum.Restarting.getValue());
        } else {
            rtMonitor = createRtMonitor(taskId, taskVersion, rtOpsAttachment, OpsNameEnum.Restarting.getValue(),
                    rtTaskLastState.getXxlJobId());
        }


        // 3.2 开启监控
        // 这个结果只是代表开启了监控
        flag = flag && iMonitor.restart(rtMonitor);
        //获取xxljobid
        rtMonitor = monitorService.getById(rtMonitor.getMonitorId());
        if (rtTaskLastState.getXxlJobId() == null && rtMonitor.getXxlJobId() != null) {
            rtTaskLastState.setXxlJobId(rtMonitor.getXxlJobId());
        }

        rtTaskLastState.setMonitorId(rtMonitor.getMonitorId());
        taskLastStateService.updateById(rtTaskLastState);
        // 返回这个值也只能说明，一切准备工作是成功的
        // 具体请成功没有需要通过每次的监测值对比看出
        return flag;
    }

    @Override
    public boolean appStop(UserPrincipal principal, Long taskId, String taskVersion, String engineName,
                           RtTaskLastState rtTaskLastState) {
        boolean flag = true;


        // 如果存在运行运行监控，则需要停掉运行监控
/*        flag = closeRunListen(taskId, taskVersion);*/


        /**
         * 1 注册操作
         * 2 注册操作附属信息
         * 3 调用SSH服务，运行程序
         * 4 开启监控
         */
        // 1.1 注册操作
        RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                principal,
                taskId, taskVersion,
                OpsTypeEnum.Ops.getValue(), OpsNameEnum.Stopping.getValue());

        // 1.2 注册操作附属信息
        RtOpsAttachment rtOpsAttachment = opsRegister.registerOpsAttachment(
                String.valueOf(rtOpsInfo.getOpsId()),
                taskId, taskVersion,
                OpsNameEnum.Stopping.getValue(),engineName);


        //设置操作的日志信息
        /*rtOpsInfo.setExeLogFile(rtOpsAttachment.getExeLogFile());
        opsInfoService.updateById(rtOpsInfo);*/

        // 2 调用SSH服务，运行程序
        IOperation iOperation = OperationFactory.getIOperation(engineName);
        flag = flag && iOperation.stop(principal, rtOpsAttachment);


        IMonitor iMonitor = MonitorFactory.getIMonitor(engineName);
        // 3.1 注册监控信息
        RtMonitor rtMonitor;
        if (engineName.equalsIgnoreCase(EngineTypeEnum.Spark.getValue())) {
            rtMonitor = createRtMonitor(rtOpsAttachment, OpsNameEnum.Stopping.getValue());
        } else {
            rtMonitor = createRtMonitor(taskId, taskVersion, rtOpsAttachment, OpsNameEnum.Stopping.getValue(),
                    rtTaskLastState.getXxlJobId());
        }
        if (rtTaskLastState.getXxlJobId() == null && rtMonitor.getXxlJobId() != null) {
            rtTaskLastState.setXxlJobId(rtMonitor.getXxlJobId());
        }
        rtTaskLastState.setMonitorId(rtMonitor.getMonitorId());
        taskLastStateService.updateById(rtTaskLastState);
        // 3.2 开启监控
        // 这个结果只是代表开启了监控
        flag = flag && iMonitor.stop(rtMonitor);
        //获取xxljobid
        rtMonitor = monitorService.getById(rtMonitor.getMonitorId());
        if (rtTaskLastState.getXxlJobId() == null && rtMonitor.getXxlJobId() != null) {
            rtTaskLastState.setXxlJobId(rtMonitor.getXxlJobId());
        }
        rtTaskLastState.setMonitorId(rtMonitor.getMonitorId());
        taskLastStateService.updateById(rtTaskLastState);
        // 返回这个值也只能说明，一切准备工作是成功的
        // 具体请成功没有需要通过每次的监测值对比看出
        return flag;
    }

    @Override
    public boolean appRun(UserPrincipal principal, Long taskId, String taskVersion, String engineName,
                          RtTaskLastState rtTaskLastState) {
        /**
         * 1 注册操作
         * 2 注册操作附属信息
         * 3 开启监控
         */
        boolean flag = true;

        // 1.1 注册操作
        RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                principal,
                taskId, taskVersion,
                OpsTypeEnum.Ops.getValue(), OpsNameEnum.Running.getValue());

        // 1.2 注册操作附属信息
        RtOpsAttachment rtOpsAttachment = opsRegister.registerOpsAttachment(
                String.valueOf(rtOpsInfo.getOpsId()),
                taskId, taskVersion,
                OpsNameEnum.Running.getValue(),engineName);

        IMonitor iMonitor = MonitorFactory.getIMonitor(engineName);
        // 3.1 注册监控信息
        RtMonitor rtMonitor;
        if (engineName.equalsIgnoreCase(EngineTypeEnum.Spark.getValue())) {
            rtMonitor = createRtMonitor(rtOpsAttachment, OpsNameEnum.Running.getValue());
        } else {
            rtMonitor = createRtMonitor(taskId, taskVersion, rtOpsAttachment, OpsNameEnum.Running.getValue(),
                    rtTaskLastState.getXxlJobId());
        }
        if (rtTaskLastState.getXxlJobId() == null && rtMonitor.getXxlJobId() != null) {
            rtTaskLastState.setXxlJobId(rtMonitor.getXxlJobId());
        }
        rtTaskLastState.setMonitorId(rtMonitor.getMonitorId());
        taskLastStateService.updateById(rtTaskLastState);
        // 3.2 开启监控
        // 这个结果只是代表开启了监控
        flag = flag && iMonitor.run(rtMonitor);

        // 返回这个值也只能说明，一切准备工作是成功的
        // 具体请成功没有需要通过每次的监测值对比看出
        return flag;
    }
}
