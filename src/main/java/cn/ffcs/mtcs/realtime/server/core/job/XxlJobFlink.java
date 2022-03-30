package cn.ffcs.mtcs.realtime.server.core.job;

import cn.ffcs.common.basic.dao.JschShellDao;
import cn.ffcs.common.basic.security.SecurityFactory;
import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.*;
import cn.ffcs.mtcs.realtime.server.core.alarm.IAlarm;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.IYarnHandle;
import cn.ffcs.mtcs.realtime.server.core.handle.yarn.YarnHandleFactory;
import cn.ffcs.mtcs.realtime.server.core.operation.IOperation;
import cn.ffcs.mtcs.realtime.server.core.operation.OperationFactory;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsMark;
import cn.ffcs.mtcs.realtime.server.core.ops.OpsRegister;
import cn.ffcs.mtcs.realtime.server.core.state.IMonitorState;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/23/023 15:49
 * @Version 1.0
 */
@Component
@Slf4j
public class XxlJobFlink {

    @Autowired
    private IRtMonitorService monitorService;

    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;

    @Autowired
    private IMonitorState monitorState;

    @Autowired
    private IAlarm alarm;

    @Autowired
    private OpsRegister opsRegister;

    @Autowired
    private IRtOpsInfoService opsInfoService;

    @Autowired
    private SchedulerXxlJob schedulerXxlJob;

    @Autowired
    private IOpsMark opsMark;

    @Autowired
    private UserPrincipal userPrincipal;

    @Autowired
    private IRtTaskLastStateService taskLastStateService;


    @Autowired
    private IService<RtExeMetaInfo> exeMetaInfoService;


    @Autowired
    private IRtTaskInfoService taskInfoService;

    private static String DEFAULT_CRYPT_KEY = "k38d81C!@#dkro22232JAMDGIJGDSe48dk>KUY%%$";


    private static Integer SSH_TIME_OUT = 60000;
    // 日志输出方式
    //XxlJobLogger.log("XXL-JOB, Hello World.");

    @XxlJob("flinkStart")
    public ReturnT<String> flinkStart(String param) throws Exception {

        log.info("--------flinkStart, param:{} ", param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));

        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        // 2 进行检测
        //      检测状态，如果为空进行告警，
        //      检查监控的次数，如果多次监控都为空，则不再进行监控
        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.StartException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        // 2 处理
        IYarnHandle yarnHandle = YarnHandleFactory.getIYarnHandle(rtAppState.getAppState().toUpperCase());
        yarnHandle.startHandle(rtMonitor, EngineTypeEnum.Flink.getValue());
        return ReturnT.SUCCESS;
    }

    private RtExeMachine getExeMachine(String exeMachineInfo, Long machineId) {
        RtExeMachine rtExeMachineResult = null;
        if (StringUtils.isNotEmpty(exeMachineInfo)) {
            List<RtExeMachine> rtExeMachineList = JSONObject.parseArray(exeMachineInfo, RtExeMachine.class);
            if (rtExeMachineList.size() > 0) {
                if (machineId != null && machineId >= 0) {
                    for (RtExeMachine rtExeMachine : rtExeMachineList) {
                        if (rtExeMachine.getMachineId().intValue() == machineId) {
                            rtExeMachineResult = rtExeMachine;
                            break;
                        }
                    }
                }
                if (rtExeMachineResult == null) {
                    Random rand = new Random();
                    rtExeMachineResult = rtExeMachineList.get(rand.nextInt(rtExeMachineList.size()));
                }
            }
        }
        return rtExeMachineResult;

    }


    /**
     * 主要包含监控和重新执行操作两种动作
     * 监控异常情况下（如网络问题，本地监控程序问题）则一直重新执行监控
     * 监控正常情况下，返回状态为空则认为是执行异常
     *
     * @param action          执行操作动作
     * @param rtMonitorId     监控ID
     * @param actionException 执行异常信息
     * @return 执行结果
     * @throws ExistException 异常信息
     */
    private ReturnT<String> stateTransformation(String action, String rtMonitorId, String actionException) throws ExistException {
        // param 是rtMonitor的Id
        log.debug("action: {}, rtMonitorId:{}", action, rtMonitorId);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(rtMonitorId));
        //先停调度，后续再根据实际情况确定是否重启
        log.debug("先停xxljob监控xxlJobId:{}", rtMonitor.getXxlJobId());
        schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        RtExeMetaInfo exeMetaInfo = getRtExeMetaInfo(rtMonitor.getTaskId(), rtMonitor.getTaskVersion());
        RtExeMachine exeMachine = getExeMachine(rtOpsAttachment.getExeMachine(), exeMetaInfo.getRunMachineId());

        Integer restartTimeLimit = rtOpsAttachment.getRestartNum();
        if (restartTimeLimit == null || restartTimeLimit < 0) {
            restartTimeLimit = 0;
        }
        boolean resultFlag = true;
        //保存日志信息
        StringBuilder logStringBuffer = new StringBuilder();
        // 1 获取状态
        String logInfo = String.format("[%s]监控id:%s,开始获取状态，需要等待返回结果",
                LocalDateTime.now(), rtMonitorId);
        log.debug(logInfo);
        logStringBuffer.append(logInfo).append("\\n");
        //输出日志信息
        //sshExeLog(exeMachine, logStringBuffer, rtOpsAttachment.getExeLogFile());
        logStringBuffer.setLength(0);
        RtAppState rtAppState = getRtAppState(rtMonitor);
        logInfo = String.format("[%s]监控id:%s,获取状态结束",
                LocalDateTime.now(), rtMonitorId);
        logStringBuffer.append(logInfo).append("\\n");
        //输出日志信息
        //sshExeLog(exeMachine, logStringBuffer, rtOpsAttachment.getExeLogFile());
        logStringBuffer.setLength(0);

        String monitorActual;
        String executorHandler = "";
        int errorCount = rtMonitor.getMonitorErrorCount() == null ? 0 : rtMonitor.getMonitorErrorCount();
        //继续执行监控标识
        errorCount = Math.max(errorCount, 0);
        boolean isContinueRunJob = true;


        //rtAppState为空，实时任务对应的应用没成功执行
        if (rtAppState == null) {
            if (RestartTypeEnum.Auto.getValue().equals(rtOpsAttachment.getRestartType())) {
                errorCount++;
                if (errorCount <= restartTimeLimit) {
                    logInfo = String.format("[%s]监控id:%s,应用操作异常达到%s次，未超过限制%s！重新执行！",
                            LocalDateTime.now(), rtMonitorId, errorCount,
                            restartTimeLimit);
                    log.info(logInfo);
                    logStringBuffer.append(logInfo).append("\\n");
                    isContinueRunJob = true;
                    executorHandler = action;
                    monitorActual = OpsNameEnum.ExceptionRestarting.getValue();
                    resultFlag = retryExecAction(action, rtOpsAttachment);
                } else {
                    logInfo = String.format("[%s]监控id:%s,应用操作异常达到%s次，超过限制%s！停止执行",
                            LocalDateTime.now(), rtMonitorId, errorCount,
                            restartTimeLimit);
                    log.info(logInfo);
                    logStringBuffer.append(logInfo).append("\\n");
                    isContinueRunJob = false;
                    monitorActual = actionException;
                }

            } else {
                //异常非自动处理
                logInfo = String.format("[%s]监控id:%s,应用操作异常！",
                        LocalDateTime.now(), rtMonitorId);
                logStringBuffer.append(logInfo).append("\\n");
                isContinueRunJob = false;
                monitorActual = actionException;
                logInfo = String.format("[%s]监控id:%s,执行终止操作！",
                        LocalDateTime.now(), rtMonitorId);
                logStringBuffer.append(logInfo).append("\\n");

                if (rtOpsAttachment != null) {
                    //尝试再次停止操作
                    //重新获取最新RtOpsAttachment
                    RtOpsAttachment thisRtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
                    logInfo = String.format("[%s]监控id:%s，appid:%s,尝试重新执行停止！",
                            LocalDateTime.now(), rtMonitorId, thisRtOpsAttachment.getExeResult());
                    logStringBuffer.append(logInfo).append("\\n");
                    String stopCommand = opsRegister.getOpsCommand(EngineTypeEnum.Flink.getValue(),
                            OpsNameEnum.Stopping.getValue(), exeMetaInfo, thisRtOpsAttachment,
                            thisRtOpsAttachment.getExeId());
                    log.debug("监控id:{},异常发起停止命令，stopCommand:{}", rtMonitorId, stopCommand);
                    thisRtOpsAttachment.setOpsCommand(stopCommand);
                    retryExecAction(XxlJobExeHandlerEnum.FlinkAppStop.getValue(), thisRtOpsAttachment);
                    log.debug("监控id:{},停止命令执行完成！", rtMonitorId);
                }

            }
        } else if (RecordStateEnum.StateCompleted.getValue().equalsIgnoreCase(
                rtAppState.getProcessState())) {
            //监控正常，但无返回值，则认为任务执行异常
            logInfo = String.format("[%s]监控id:%s,已成功执行,对结果信息处理!", LocalDateTime.now(),
                    rtMonitorId);
            log.info(logInfo);
            //logStringBuffer.append(logInfo).append("\\n");
            String appState = StringUtils.isNotEmpty(rtAppState.getAppState()) ? rtAppState.getAppState() :
                    "FAILED";
            if (StringUtils.isEmpty(rtAppState.getAppState())) {
                logInfo = String.format("[%s]监控id:%s,监控返回的状态为空,状态转为FAILED",
                        LocalDateTime.now(), rtMonitorId);

                log.info(logInfo);
                logStringBuffer.append(logInfo).append("\\n");
            }
            monitorActual = appState;
            executorHandler = action;

            switch (appState) {
                case "RUNNING":
                    //RUNNING 如果手工发起的是停止操作，说明未停掉，需要继续监控
                    if (XxlJobExeHandlerEnum.FlinkAppStop.getValue().equalsIgnoreCase(action)) {
                        monitorActual = OpsNameEnum.StopException.getValue();
                    } else {
                        //启动和重启操作，
                        monitorActual = OpsNameEnum.Running.getValue();
                        executorHandler = XxlJobExeHandlerEnum.FlinkAppRun.getValue();
                    }
                    break;
                case "FAILED":
                    //执行失败
                    if (XxlJobExeHandlerEnum.FlinkAppStop.getValue().equalsIgnoreCase(action)) {
                        monitorActual = OpsNameEnum.Stop.getValue();
                        isContinueRunJob = false;
                    } else {
                        monitorActual = OpsNameEnum.RunException.getValue();
                        //启动或重启的操作，需要继续重启
                        if (RestartTypeEnum.Auto.getValue().equals(rtOpsAttachment.getRestartType())) {
                            if (rtMonitor.getMonitorErrorCount() != null && rtMonitor.getMonitorErrorCount() > restartTimeLimit) {
                                // 停止监控
                                logInfo = String.format("[%s]监控id:%s,启动异常达到%s次！，监控退出！",
                                        LocalDateTime.now(), rtMonitorId, restartTimeLimit);

                                log.info(logInfo);
                                logStringBuffer.append(logInfo).append("\\n");
                                ;
                                isContinueRunJob = false;
                            } else {
                                isContinueRunJob = true;
                            }

                        } else {
                            isContinueRunJob = false;
                        }
                        //出错计数累加1
                        errorCount = (rtMonitor.getMonitorErrorCount() == null ? 0 : rtMonitor.getMonitorErrorCount());
                        errorCount++;
                    }

                    break;
                case "FINISHED":
                case "KILLED":
                    //被杀
                    //执行完成
                    monitorActual = OpsNameEnum.Stop.getValue();
                    isContinueRunJob = false;
                    break;
                default:
            }
            //需要重新执行命令
            if (isContinueRunJob && !executorHandler.equalsIgnoreCase(XxlJobExeHandlerEnum.FlinkAppRun.getValue())) {
                retryExecAction(action, rtOpsAttachment);
            }

        } else {
            logInfo = String.format("[%s]监控id:%s,未正常执行完成,继续执行!", LocalDateTime.now(),
                    rtMonitorId);
            log.info(logInfo);
            logStringBuffer.append(logInfo).append("\\n");
            ;
            //监控异常，继续监控
            monitorActual = OpsNameEnum.MonitorException.getValue();
            executorHandler = action;
            //监控异常情况
            //继续监控
            isContinueRunJob = rtMonitor.getMonitorErrorCount() == null || rtMonitor.getMonitorErrorCount() <= Integer.MAX_VALUE;
            resultFlag = false;

        }


        rtMonitor.setMonitorErrorCount(errorCount);

        rtMonitor.setMonitorTime(LocalDateTime.now());
        rtMonitor.setMonitorActual(monitorActual);
        monitorService.updateById(rtMonitor);

        noRepRegisterOps(monitorActual, rtMonitor.getTaskId(), rtMonitor.getTaskVersion());
        //此处更新主要是获取最新的时间
        RtTaskLastState rtTaskLastStateInfo = getRtTaskLastState(rtMonitor.getTaskId(), rtMonitor.getTaskVersion());
        rtTaskLastStateInfo.setOpsTime(LocalDateTime.now());
        rtTaskLastStateInfo.setTaskState(monitorActual);
        taskLastStateService.updateById(rtTaskLastStateInfo);

        if (isContinueRunJob) {
            //先停调度，再重启
            logInfo = String.format("[%s]监控id:%s,先停止，再重启操作！", LocalDateTime.now(), rtMonitorId);
            log.info(logInfo);
            XxlJobInfo xxlJobInfo = schedulerXxlJob.createXxlJobInfo(rtMonitor, executorHandler);
            xxlJobInfo.setId(rtMonitor.getXxlJobId());
            schedulerXxlJob.updateScheduler(xxlJobInfo);
            //重新启动
            schedulerXxlJob.startScheduler(rtMonitor.getXxlJobId());
        } else {
            //新增 monitorActual
            logInfo = String.format("[%s]监控id:%s停止操作！", LocalDateTime.now(), rtMonitorId);
            log.info(logInfo);

            logStringBuffer.append(logInfo).append("\\n");

        }
        //日志跨机器输出
        sshExeLog(exeMachine, logStringBuffer, rtOpsAttachment.getExeLogFile());
        if (resultFlag) {
            return ReturnT.SUCCESS;
        } else {
            return ReturnT.FAIL;
        }
    }


    private RtExeMetaInfo getRtExeMetaInfo(Long taskId, String taskVersion) {
        return exeMetaInfoService.getOne(
                Wrappers.<RtExeMetaInfo>lambdaQuery()
                        .eq(RtExeMetaInfo::getTaskId, taskId)
                        .eq(RtExeMetaInfo::getTaskVersion, taskVersion)
                        .eq(RtExeMetaInfo::getState, RecordStateEnum.StateUse.getValue()),
                false);
    }

    private void sshExeLog(RtExeMachine executeMachine, StringBuilder logSb, String logFile) {
        if (executeMachine != null && logSb.length() > 0 && StringUtils.isNotEmpty(logFile)) {
            JschShellDao jschShellDao = null;
            try {
                jschShellDao = new JschShellDao();
                String password = executeMachine.getPassWord();
                String decodePassword = SecurityFactory.decode(SecurityFactory.DesRandom, password, DEFAULT_CRYPT_KEY);
                jschShellDao.connect(executeMachine.getIpAddress(), Integer.parseInt(executeMachine.getConnectPort()),
                        executeMachine.getUserName(), decodePassword, SSH_TIME_OUT);
                String cmd = "echo -e \"" + logSb.toString() + "\" >> " + logFile;

                log.debug("cmd={}", cmd);
                jschShellDao.excute(cmd);
                jschShellDao.close();
            } catch (Exception e) {
                log.error("执行跨机器日志异常！");
            } finally {
                if (jschShellDao != null) {
                    jschShellDao.close();
                }
            }
        }

    }

    private void noRepRegisterOps(String opsName, Long taskId, String taskVersion) {
        boolean registerOps = true;
        RtOpsInfo lastRtOpsInfo =
                opsInfoService.getOne(
                        Wrappers.<RtOpsInfo>lambdaQuery()
                                .eq(RtOpsInfo::getTaskId, taskId)
                                .eq(RtOpsInfo::getTaskVersion, taskVersion)
                                .eq(RtOpsInfo::getState, RecordStateEnum.StateUse.getValue())
                                .orderByDesc(RtOpsInfo::getOpsId),
                        false);
        if (opsName.equalsIgnoreCase(lastRtOpsInfo.getOpsName())) {
            //上一次状态一样，不进行插入
            registerOps = false;
        }
        if (registerOps) {
            RtOpsInfo rtOpsInfo = opsRegister.registerOps(
                    userPrincipal,
                    taskId, taskVersion,
                    OpsTypeEnum.Mark.getValue(), opsName);
            if (rtOpsInfo == null) {
                registerOps = false;
            }
        }
    }

    @XxlJob("flinkAppStart")
    public ReturnT<String> flinkAppStart(String param) throws Exception {
        //输入参数为：monitor_id
        log.info("--------flinkAppStart, param :{} ", param);

        return stateTransformation(XxlJobExeHandlerEnum.FlinkAppStart.getValue(),
                param, OpsNameEnum.StartException.getValue());

    }

    /**
     * @param action          执行动作
     * @param rtOpsAttachment 动作附属信息
     * @return 执行结果
     */
    private boolean retryExecAction(String action, RtOpsAttachment rtOpsAttachment) {
        boolean resultFlag = false;
        log.debug("需要重新执行命名:{}", action);
        IOperation iOperation = OperationFactory.getIOperation(EngineTypeEnum.Flink.getValue());
        if (action.equalsIgnoreCase(XxlJobExeHandlerEnum.FlinkAppRun.getValue())) {
            log.info("iOperation.start 开始启动");
            resultFlag = iOperation.start(userPrincipal, rtOpsAttachment);
        } else if (action.equalsIgnoreCase(XxlJobExeHandlerEnum.FlinkAppRestart.getValue())) {
            log.info("iOperation.restart 开始启动");
            resultFlag = iOperation.restart(userPrincipal, rtOpsAttachment);
        } else if (action.equalsIgnoreCase(XxlJobExeHandlerEnum.FlinkAppStop.getValue())) {
            log.info("iOperation.stop 开始启动");
            resultFlag = iOperation.stop(userPrincipal, rtOpsAttachment);
        }
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return resultFlag;
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

    private boolean checkMonitor(RtMonitor rtMonitor, RtAppState rtAppState, String exceptionOpsName) {
        //需要重点优化
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        Integer restartTimeLimit = rtOpsAttachment.getRestartNum();
        if (restartTimeLimit == null || restartTimeLimit < 0) {
            restartTimeLimit = 0;
        }
        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());

        boolean flag = true;
        //rtAppState为空，没监控到信息，动作异常
        String alarmMessage = "";
        if (null == rtAppState || null == rtAppState.getAppState()
                || !RecordStateEnum.StateCompleted.getValue().equalsIgnoreCase(rtAppState.getProcessState())) {
            // 监控异常，未获取到监控值
            if (exceptionOpsName.equals(OpsNameEnum.MonitorException.getValue())) {
                //监控异常情况下，需要监控任务继续执行后续的监控，直到达到监控出错次数，
                if (rtMonitor.getMonitorErrorCount() != null && rtMonitor.getMonitorErrorCount() > Integer.MAX_VALUE) {
                    // 停止监控
                    schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
                }
                //设置监控异常
                opsMark.monitorException(userPrincipal,
                        rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
            } else if (exceptionOpsName.equals(OpsNameEnum.RunException.getValue())) {
                //启动异常情况下，需要继续重启，直到达到重启出错次数，
                if (rtMonitor.getMonitorErrorCount() != null && rtMonitor.getMonitorErrorCount() > restartTimeLimit) {
                    // 停止监控
                    alarmMessage = String.format("启动异常达到%s次！，监控退出！", restartTimeLimit);
                    log.info("启动异常达到{}次！，监控退出！", restartTimeLimit);
                    schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
                }
                //设置监控异常
                opsMark.startException(userPrincipal,
                        rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
            } else {
                // 停止监控
                schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
                // 将状态修改为相应的异常
                if (exceptionOpsName.equals(OpsNameEnum.StartException.getValue())) {
                    opsMark.startException(userPrincipal,
                            rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
                } else if (exceptionOpsName.equals(OpsNameEnum.RestartException.getValue())) {
                    opsMark.restartException(userPrincipal,
                            rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
                } else if (exceptionOpsName.equals(OpsNameEnum.StopException.getValue())) {
                    opsMark.stopException(userPrincipal,
                            rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
                } else if (exceptionOpsName.equals(OpsNameEnum.RunException.getValue())) {
                    opsMark.runException(userPrincipal,
                            rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
                }
            }

            int errorCount = (rtMonitor.getMonitorErrorCount() == null ? 0 : rtMonitor.getMonitorErrorCount());
            rtMonitor.setMonitorErrorCount(errorCount + 1);
            // todo 进行告警，在生产环境上才能测试
            alarmMessage = alarmMessage + "获取Flink程序状态为空告警";
            log.warn(alarmMessage);
            XxlJobLogger.log(alarmMessage);
            alarm.nullState(alarmMessage);

            flag = false;
        } else {
            //执行成功后，需要把之前的监控出错次数置0
            rtMonitor.setMonitorErrorCount(0);
        }
        rtMonitor.setMonitorCount(rtMonitor.getMonitorCount() + 1);
        rtMonitor.setMonitorTime(LocalDateTime.now());
        monitorService.updateById(rtMonitor);

        return flag;
    }


    @XxlJob("flinkRestart")
    public ReturnT<String> flinkRestart(String param) throws Exception {
        log.info("--------flinkRestart, param :{} ", param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));


        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.RestartException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        IYarnHandle yarnHandle = YarnHandleFactory.getIYarnHandle(rtAppState.getAppState().toUpperCase());
        yarnHandle.restartHandle(rtMonitor, EngineTypeEnum.Flink.getValue());

        return ReturnT.SUCCESS;
    }

    @XxlJob("flinkAppRestart")
    public ReturnT<String> flinkAppRestart(String param) throws Exception {
        log.info("--------flinkAppRestart, param :{} ", param);

        return stateTransformation(XxlJobExeHandlerEnum.FlinkAppRestart.getValue(), param, OpsNameEnum.RestartException.getValue());

    }

    @XxlJob("flinkStop")
    public ReturnT<String> flinkStop(String param) throws Exception {
        log.info("--------flinkStop, param : {}", param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));

        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.StopException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        IYarnHandle yarnHandle = YarnHandleFactory.getIYarnHandle(rtAppState.getAppState().toUpperCase());
        yarnHandle.stopHandle(rtMonitor, EngineTypeEnum.Flink.getValue());

        return ReturnT.SUCCESS;
    }

    @XxlJob("flinkAppStop")
    public ReturnT<String> flinkAppStop(String param) throws Exception {
        log.info("--------flinkAppStop, param :{} ", param);
        return stateTransformation(XxlJobExeHandlerEnum.FlinkAppStop.getValue(), param, OpsNameEnum.StopException.getValue());

    }


    @XxlJob("flinkRun")
    public ReturnT<String> flinkRun(String param) throws Exception {
        log.info("--------flinkRun, param :{} ", param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));

        // 1 获取状态
        RtAppState rtAppState = getRtAppState(rtMonitor);

        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.MonitorException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        IYarnHandle yarnHandle = YarnHandleFactory.getIYarnHandle(rtAppState.getAppState().toUpperCase());
        yarnHandle.runHandle(rtMonitor, EngineTypeEnum.Flink.getValue());

        return ReturnT.SUCCESS;
    }

    @XxlJob("flinkAppRun")
    public ReturnT<String> flinkAppRun(String param) throws Exception {

        log.info("--------flinkAppRun, param :{} ", param);
        return stateTransformation(XxlJobExeHandlerEnum.FlinkAppRun.getValue(), param, OpsNameEnum.MonitorException.getValue());


    }

    private RtAppState getRtAppState(RtMonitor rtMonitor) {
        return monitorState.getRtAppState(rtMonitor, EngineTypeEnum.Flink.getValue());
    }
}
