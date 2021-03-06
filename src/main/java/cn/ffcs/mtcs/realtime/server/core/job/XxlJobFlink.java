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
    // ??????????????????
    //XxlJobLogger.log("XXL-JOB, Hello World.");

    @XxlJob("flinkStart")
    public ReturnT<String> flinkStart(String param) throws Exception {

        log.info("--------flinkStart, param:{} ", param);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(param));

        // 1 ????????????
        RtAppState rtAppState = getRtAppState(rtMonitor);

        // 2 ????????????
        //      ??????????????????????????????????????????
        //      ???????????????????????????????????????????????????????????????????????????
        boolean checkFlag = checkMonitor(rtMonitor, rtAppState, OpsNameEnum.StartException.getValue());
        if (!checkFlag) {
            return ReturnT.FAIL;
        }

        // 2 ??????
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
     * ???????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????????????????????????????????????????
     * ??????????????????????????????????????????????????????????????????
     *
     * @param action          ??????????????????
     * @param rtMonitorId     ??????ID
     * @param actionException ??????????????????
     * @return ????????????
     * @throws ExistException ????????????
     */
    private ReturnT<String> stateTransformation(String action, String rtMonitorId, String actionException) throws ExistException {
        // param ???rtMonitor???Id
        log.debug("action: {}, rtMonitorId:{}", action, rtMonitorId);
        RtMonitor rtMonitor = monitorService.getById(Long.parseLong(rtMonitorId));
        //????????????????????????????????????????????????????????????
        log.debug("??????xxljob??????xxlJobId:{}", rtMonitor.getXxlJobId());
        schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        RtExeMetaInfo exeMetaInfo = getRtExeMetaInfo(rtMonitor.getTaskId(), rtMonitor.getTaskVersion());
        RtExeMachine exeMachine = getExeMachine(rtOpsAttachment.getExeMachine(), exeMetaInfo.getRunMachineId());

        Integer restartTimeLimit = rtOpsAttachment.getRestartNum();
        if (restartTimeLimit == null || restartTimeLimit < 0) {
            restartTimeLimit = 0;
        }
        boolean resultFlag = true;
        //??????????????????
        StringBuilder logStringBuffer = new StringBuilder();
        // 1 ????????????
        String logInfo = String.format("[%s]??????id:%s,?????????????????????????????????????????????",
                LocalDateTime.now(), rtMonitorId);
        log.debug(logInfo);
        logStringBuffer.append(logInfo).append("\\n");
        //??????????????????
        //sshExeLog(exeMachine, logStringBuffer, rtOpsAttachment.getExeLogFile());
        logStringBuffer.setLength(0);
        RtAppState rtAppState = getRtAppState(rtMonitor);
        logInfo = String.format("[%s]??????id:%s,??????????????????",
                LocalDateTime.now(), rtMonitorId);
        logStringBuffer.append(logInfo).append("\\n");
        //??????????????????
        //sshExeLog(exeMachine, logStringBuffer, rtOpsAttachment.getExeLogFile());
        logStringBuffer.setLength(0);

        String monitorActual;
        String executorHandler = "";
        int errorCount = rtMonitor.getMonitorErrorCount() == null ? 0 : rtMonitor.getMonitorErrorCount();
        //????????????????????????
        errorCount = Math.max(errorCount, 0);
        boolean isContinueRunJob = true;


        //rtAppState???????????????????????????????????????????????????
        if (rtAppState == null) {
            if (RestartTypeEnum.Auto.getValue().equals(rtOpsAttachment.getRestartType())) {
                errorCount++;
                if (errorCount <= restartTimeLimit) {
                    logInfo = String.format("[%s]??????id:%s,????????????????????????%s?????????????????????%s??????????????????",
                            LocalDateTime.now(), rtMonitorId, errorCount,
                            restartTimeLimit);
                    log.info(logInfo);
                    logStringBuffer.append(logInfo).append("\\n");
                    isContinueRunJob = true;
                    executorHandler = action;
                    monitorActual = OpsNameEnum.ExceptionRestarting.getValue();
                    resultFlag = retryExecAction(action, rtOpsAttachment);
                } else {
                    logInfo = String.format("[%s]??????id:%s,????????????????????????%s??????????????????%s???????????????",
                            LocalDateTime.now(), rtMonitorId, errorCount,
                            restartTimeLimit);
                    log.info(logInfo);
                    logStringBuffer.append(logInfo).append("\\n");
                    isContinueRunJob = false;
                    monitorActual = actionException;
                }

            } else {
                //?????????????????????
                logInfo = String.format("[%s]??????id:%s,?????????????????????",
                        LocalDateTime.now(), rtMonitorId);
                logStringBuffer.append(logInfo).append("\\n");
                isContinueRunJob = false;
                monitorActual = actionException;
                logInfo = String.format("[%s]??????id:%s,?????????????????????",
                        LocalDateTime.now(), rtMonitorId);
                logStringBuffer.append(logInfo).append("\\n");

                if (rtOpsAttachment != null) {
                    //????????????????????????
                    //??????????????????RtOpsAttachment
                    RtOpsAttachment thisRtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
                    logInfo = String.format("[%s]??????id:%s???appid:%s,???????????????????????????",
                            LocalDateTime.now(), rtMonitorId, thisRtOpsAttachment.getExeResult());
                    logStringBuffer.append(logInfo).append("\\n");
                    String stopCommand = opsRegister.getOpsCommand(EngineTypeEnum.Flink.getValue(),
                            OpsNameEnum.Stopping.getValue(), exeMetaInfo, thisRtOpsAttachment,
                            thisRtOpsAttachment.getExeId());
                    log.debug("??????id:{},???????????????????????????stopCommand:{}", rtMonitorId, stopCommand);
                    thisRtOpsAttachment.setOpsCommand(stopCommand);
                    retryExecAction(XxlJobExeHandlerEnum.FlinkAppStop.getValue(), thisRtOpsAttachment);
                    log.debug("??????id:{},???????????????????????????", rtMonitorId);
                }

            }
        } else if (RecordStateEnum.StateCompleted.getValue().equalsIgnoreCase(
                rtAppState.getProcessState())) {
            //????????????????????????????????????????????????????????????
            logInfo = String.format("[%s]??????id:%s,???????????????,?????????????????????!", LocalDateTime.now(),
                    rtMonitorId);
            log.info(logInfo);
            //logStringBuffer.append(logInfo).append("\\n");
            String appState = StringUtils.isNotEmpty(rtAppState.getAppState()) ? rtAppState.getAppState() :
                    "FAILED";
            if (StringUtils.isEmpty(rtAppState.getAppState())) {
                logInfo = String.format("[%s]??????id:%s,???????????????????????????,????????????FAILED",
                        LocalDateTime.now(), rtMonitorId);

                log.info(logInfo);
                logStringBuffer.append(logInfo).append("\\n");
            }
            monitorActual = appState;
            executorHandler = action;

            switch (appState) {
                case "RUNNING":
                    //RUNNING ???????????????????????????????????????????????????????????????????????????
                    if (XxlJobExeHandlerEnum.FlinkAppStop.getValue().equalsIgnoreCase(action)) {
                        monitorActual = OpsNameEnum.StopException.getValue();
                    } else {
                        //????????????????????????
                        monitorActual = OpsNameEnum.Running.getValue();
                        executorHandler = XxlJobExeHandlerEnum.FlinkAppRun.getValue();
                    }
                    break;
                case "FAILED":
                    //????????????
                    if (XxlJobExeHandlerEnum.FlinkAppStop.getValue().equalsIgnoreCase(action)) {
                        monitorActual = OpsNameEnum.Stop.getValue();
                        isContinueRunJob = false;
                    } else {
                        monitorActual = OpsNameEnum.RunException.getValue();
                        //?????????????????????????????????????????????
                        if (RestartTypeEnum.Auto.getValue().equals(rtOpsAttachment.getRestartType())) {
                            if (rtMonitor.getMonitorErrorCount() != null && rtMonitor.getMonitorErrorCount() > restartTimeLimit) {
                                // ????????????
                                logInfo = String.format("[%s]??????id:%s,??????????????????%s????????????????????????",
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
                        //??????????????????1
                        errorCount = (rtMonitor.getMonitorErrorCount() == null ? 0 : rtMonitor.getMonitorErrorCount());
                        errorCount++;
                    }

                    break;
                case "FINISHED":
                case "KILLED":
                    //??????
                    //????????????
                    monitorActual = OpsNameEnum.Stop.getValue();
                    isContinueRunJob = false;
                    break;
                default:
            }
            //????????????????????????
            if (isContinueRunJob && !executorHandler.equalsIgnoreCase(XxlJobExeHandlerEnum.FlinkAppRun.getValue())) {
                retryExecAction(action, rtOpsAttachment);
            }

        } else {
            logInfo = String.format("[%s]??????id:%s,?????????????????????,????????????!", LocalDateTime.now(),
                    rtMonitorId);
            log.info(logInfo);
            logStringBuffer.append(logInfo).append("\\n");
            ;
            //???????????????????????????
            monitorActual = OpsNameEnum.MonitorException.getValue();
            executorHandler = action;
            //??????????????????
            //????????????
            isContinueRunJob = rtMonitor.getMonitorErrorCount() == null || rtMonitor.getMonitorErrorCount() <= Integer.MAX_VALUE;
            resultFlag = false;

        }


        rtMonitor.setMonitorErrorCount(errorCount);

        rtMonitor.setMonitorTime(LocalDateTime.now());
        rtMonitor.setMonitorActual(monitorActual);
        monitorService.updateById(rtMonitor);

        noRepRegisterOps(monitorActual, rtMonitor.getTaskId(), rtMonitor.getTaskVersion());
        //??????????????????????????????????????????
        RtTaskLastState rtTaskLastStateInfo = getRtTaskLastState(rtMonitor.getTaskId(), rtMonitor.getTaskVersion());
        rtTaskLastStateInfo.setOpsTime(LocalDateTime.now());
        rtTaskLastStateInfo.setTaskState(monitorActual);
        taskLastStateService.updateById(rtTaskLastStateInfo);

        if (isContinueRunJob) {
            //????????????????????????
            logInfo = String.format("[%s]??????id:%s,??????????????????????????????", LocalDateTime.now(), rtMonitorId);
            log.info(logInfo);
            XxlJobInfo xxlJobInfo = schedulerXxlJob.createXxlJobInfo(rtMonitor, executorHandler);
            xxlJobInfo.setId(rtMonitor.getXxlJobId());
            schedulerXxlJob.updateScheduler(xxlJobInfo);
            //????????????
            schedulerXxlJob.startScheduler(rtMonitor.getXxlJobId());
        } else {
            //?????? monitorActual
            logInfo = String.format("[%s]??????id:%s???????????????", LocalDateTime.now(), rtMonitorId);
            log.info(logInfo);

            logStringBuffer.append(logInfo).append("\\n");

        }
        //?????????????????????
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
                log.error("??????????????????????????????");
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
            //???????????????????????????????????????
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
        //??????????????????monitor_id
        log.info("--------flinkAppStart, param :{} ", param);

        return stateTransformation(XxlJobExeHandlerEnum.FlinkAppStart.getValue(),
                param, OpsNameEnum.StartException.getValue());

    }

    /**
     * @param action          ????????????
     * @param rtOpsAttachment ??????????????????
     * @return ????????????
     */
    private boolean retryExecAction(String action, RtOpsAttachment rtOpsAttachment) {
        boolean resultFlag = false;
        log.debug("????????????????????????:{}", action);
        IOperation iOperation = OperationFactory.getIOperation(EngineTypeEnum.Flink.getValue());
        if (action.equalsIgnoreCase(XxlJobExeHandlerEnum.FlinkAppRun.getValue())) {
            log.info("iOperation.start ????????????");
            resultFlag = iOperation.start(userPrincipal, rtOpsAttachment);
        } else if (action.equalsIgnoreCase(XxlJobExeHandlerEnum.FlinkAppRestart.getValue())) {
            log.info("iOperation.restart ????????????");
            resultFlag = iOperation.restart(userPrincipal, rtOpsAttachment);
        } else if (action.equalsIgnoreCase(XxlJobExeHandlerEnum.FlinkAppStop.getValue())) {
            log.info("iOperation.stop ????????????");
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
            throw new ExistException("?????????????????????????????????");
        }
        return rtTaskLastState;
    }

    private boolean checkMonitor(RtMonitor rtMonitor, RtAppState rtAppState, String exceptionOpsName) {
        //??????????????????
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        Integer restartTimeLimit = rtOpsAttachment.getRestartNum();
        if (restartTimeLimit == null || restartTimeLimit < 0) {
            restartTimeLimit = 0;
        }
        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());

        boolean flag = true;
        //rtAppState??????????????????????????????????????????
        String alarmMessage = "";
        if (null == rtAppState || null == rtAppState.getAppState()
                || !RecordStateEnum.StateCompleted.getValue().equalsIgnoreCase(rtAppState.getProcessState())) {
            // ????????????????????????????????????
            if (exceptionOpsName.equals(OpsNameEnum.MonitorException.getValue())) {
                //?????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (rtMonitor.getMonitorErrorCount() != null && rtMonitor.getMonitorErrorCount() > Integer.MAX_VALUE) {
                    // ????????????
                    schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
                }
                //??????????????????
                opsMark.monitorException(userPrincipal,
                        rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
            } else if (exceptionOpsName.equals(OpsNameEnum.RunException.getValue())) {
                //??????????????????????????????????????????????????????????????????????????????
                if (rtMonitor.getMonitorErrorCount() != null && rtMonitor.getMonitorErrorCount() > restartTimeLimit) {
                    // ????????????
                    alarmMessage = String.format("??????????????????%s????????????????????????", restartTimeLimit);
                    log.info("??????????????????{}????????????????????????", restartTimeLimit);
                    schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
                }
                //??????????????????
                opsMark.startException(userPrincipal,
                        rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
            } else {
                // ????????????
                schedulerXxlJob.stopScheduler(rtMonitor.getXxlJobId());
                // ?????????????????????????????????
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
            // todo ?????????????????????????????????????????????
            alarmMessage = alarmMessage + "??????Flink????????????????????????";
            log.warn(alarmMessage);
            XxlJobLogger.log(alarmMessage);
            alarm.nullState(alarmMessage);

            flag = false;
        } else {
            //?????????????????????????????????????????????????????????0
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


        // 1 ????????????
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

        // 1 ????????????
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

        // 1 ????????????
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
