package cn.ffcs.mtcs.realtime.server.core.state.impl;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.*;
import cn.ffcs.mtcs.realtime.server.constants.monitor.YarnStateEnum;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineFactory;
import cn.ffcs.mtcs.realtime.server.core.engine.IEngine;
import cn.ffcs.mtcs.realtime.server.core.engine.impl.EngineFlink;
import cn.ffcs.mtcs.realtime.server.core.state.IMonitorState;
import cn.ffcs.mtcs.realtime.server.core.state.MonitorStateTool;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.feign.SshServerFeign;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.pojo.bo.ExeMachineBo;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import cn.ffcs.mtcs.realtime.server.util.PseudCodeUtil;
import cn.ffcs.mtcs.realtime.server.util.RealTimeSsh;
import cn.ffcs.mtcs.ssh.common.constants.SshExeType;
import cn.ffcs.mtcs.ssh.common.request.ExeInfo;
import cn.ffcs.mtcs.ssh.common.request.ExecuteMachine;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static cn.ffcs.mtcs.realtime.server.constants.CommonConstants.EXEC_RESULT_APP_ID_KEY;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/5/005 10:32
 * @Version 1.0
 */
@Component
@Slf4j
public class MonitorStateImpl implements IMonitorState {

    @Autowired
    private IRtAppStateService appStateService;

    @Autowired
    private IRtExeInfoService exeInfoService;

    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;

    @Autowired
    private IRtOpsInfoService opsInfoService;

    @Autowired
    private IDetailTaskInfoBoService detailTaskInfoBoService;

   /* @Autowired
    private SshServerFeign sshServerFeign;*/

    @Autowired
    private UserPrincipal userPrincipal;

    @Value("${spring.application.name}")
    private String AppServerName;

    @Value("${exe.count.default}")
    private int EXE_COUNT_DEFAULT;

    @Autowired
    private MonitorStateTool monitorStateTool;

    @Autowired
    private IService<RtExeMetaInfo> exeMetaInfoService;

    /**
     * @param rtMonitor
     * @return
     */
    @Override
    public RtAppState getRtAppState(RtMonitor rtMonitor,String engineType) {
        /**
         * 1 ??????RtAppState
         * 2 ??????ssh????????????
         * 3 ??????id?????????RtAppState
         */
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        //rtOpsAttachment??????exeid???????????????????????????????????????ExeId???????????????????????????????????????

      /*  if (rtOpsAttachment.getExeId() == -1) {
            return null;
        }*/
        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());
        RtExeInfo rtExeInfo = exeInfoService.getById(rtOpsAttachment.getExeId());
        DetailTaskInfoBo detailTaskInfoBo =
                detailTaskInfoBoService.getTaskDetailInfoBo(
                        rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
        RtExeMetaInfo rtExeMetaInfo = getRtExeMetaInfo(rtMonitor.getTaskId(), rtMonitor.getTaskVersion());

        // ??????????????????????????????
        RtAppState firstrtAppState =
                createRtAppState(
                        rtMonitor,
                        rtOpsAttachment, rtOpsInfo, rtExeInfo,
                        detailTaskInfoBo);
        //??????????????????????????????????????????????????????????????????IP??????????????????????????????????????????????????????
        // ????????????????????????????????????????????????????????????????????????
        if (rtExeInfo != null) {
            RtExeMachine executeMachine = getRtExeMachineByExeInfo(rtExeInfo.getExeMachine());

            ExeMachineBo exeMachineBo = getExeMachineBo(executeMachine, detailTaskInfoBo);
            boolean isSaveExeInfo = false;
            if (StringUtils.isNotEmpty(rtOpsAttachment.getExeResult())) {
                if (StringUtils.isEmpty(rtExeInfo.getAppId())) {
                    rtExeInfo.setAppId(getAppIdFormRtOpsAttachment(rtOpsAttachment));
                    isSaveExeInfo = true;
                }
                Map<String,String> excResultMap = JSON.parseObject(rtOpsAttachment.getExeResult(), HashMap.class);
                Map<String,String> exeResultParamAll = new HashMap<>();
                if (StringUtils.isNotEmpty(rtExeInfo.getExeResultParam())) {
                    Map<String,String> exeResultParamMap = JSON.parseObject(rtExeInfo.getExeResultParam(), HashMap.class);
                    exeResultParamAll.putAll(exeResultParamMap);
                }
                exeResultParamAll.putAll(excResultMap);
                rtExeInfo.setExeResultParam(rtOpsAttachment.getExeResult());
                isSaveExeInfo = isSaveExeInfo || (exeResultParamAll.size() > 0);
                if (isSaveExeInfo) {
                    exeInfoService.saveOrUpdate(rtExeInfo);
                }

            }
            //??????????????????
            if (StringUtils.isEmpty(firstrtAppState.getAppId()) && StringUtils.isNotEmpty(rtExeInfo.getAppId())) {
                firstrtAppState.setAppId(rtExeInfo.getAppId());
                log.debug("AppStateId={},??????????????????AppState???appid??????{}",firstrtAppState.getId(),
                        firstrtAppState.getAppId() );
                appStateService.updateById(firstrtAppState);
            }
            try {
                //?????????????????????????????????rtAppState???????????????
                monitorSsh(exeMachineBo, firstrtAppState, rtExeMetaInfo, rtOpsAttachment, rtOpsInfo, engineType);

            } catch (DataOpsException e) {
                throw new RuntimeException("??????shell????????????", e);
            }

        }
        log.info("---- ?????????????????????????????????????????????????????????rtAppState");
        //???????????????,?????????????????????????????????
        RtAppState finalRtAppState;
        int runNum = 0;
        while (true) {
            try {
                //?????????2???????????????
                Thread.sleep(2 * 1000);
                runNum ++;
                //????????????rtAppState??????????????????ProcessState??????10C ??????????????????
                finalRtAppState = getRtAppState(firstrtAppState.getId());
                if (RecordStateEnum.StateCompleted.getValue().equalsIgnoreCase(finalRtAppState.getProcessState())
                     || runNum >= 60) {
                    if (runNum >= 60) {
                        log.info("??????id={},???????????????????????????????????????{}????????????",rtMonitor.getMonitorId(), runNum);
                    }
                    break;
                }



            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        if (rtOpsAttachment.getExeId() == -1) {
            return null;
        } else {
            return finalRtAppState;
        }
    }

    private RtExeMachine getRtExeMachineByExeInfo(String exeMachineJson) {
        if (exeMachineJson.startsWith("[")) {
            List<RtExeMachine> rtExeMachineList = JSON.parseArray(exeMachineJson, RtExeMachine.class);
            Random random = new Random();
            return rtExeMachineList.get(random.nextInt(rtExeMachineList.size()));
        } else {
            return JSON.parseObject(exeMachineJson, RtExeMachine.class);
        }
    }


    private RtExeMachine getRtExeMachineByExeMetaInfo(RtExeMetaInfo rtExeMetaInfo) {
        RtExeMachine resultRtExeMachine = null;
        if (StringUtils.isNotEmpty(rtExeMetaInfo.getExeMachine())) {
            List<RtExeMachine> rtExeMachineList = JSON.parseArray(rtExeMetaInfo.getExeMachine(), RtExeMachine.class);
            if (rtExeMetaInfo.getRunMachineId() != null) {
                if (rtExeMachineList != null) {
                    for (RtExeMachine rtExeMachine : rtExeMachineList) {
                        if (rtExeMachine.getMachineId().equals(rtExeMetaInfo.getRunMachineId())) {
                            resultRtExeMachine = rtExeMachine;
                            break;
                        }
                    }
                }

            } else {
                Random random = new Random();
                resultRtExeMachine = rtExeMachineList.get(random.nextInt(rtExeMachineList.size()));
            }
        }
        return resultRtExeMachine;
    }

    private String getAppIdFormRtOpsAttachment(RtOpsAttachment rtOpsAttachment) {
        String resultAppid = "";
        //??????????????????????????????????????????applicationId???jobid?????????map??????
        String exeResult = rtOpsAttachment.getExeResult();
        if (StringUtils.isNotEmpty(exeResult)) {
            Map<String, String> exeResultMap = JSON.parseObject(exeResult, Map.class);
            if (exeResultMap != null && exeResultMap.containsKey(EXEC_RESULT_APP_ID_KEY)) {
                resultAppid = exeResultMap.get(EXEC_RESULT_APP_ID_KEY);
            }
        }
        return resultAppid;
    }

    private RtAppState createRtAppState(RtMonitor rtMonitor,
                                        RtOpsAttachment rtOpsAttachment,
                                        RtOpsInfo rtOpsInfo, RtExeInfo rtExeInfo,
                                        DetailTaskInfoBo detailTaskInfoBo) {

        RtAppState rtAppState = new RtAppState();
        //rtAppState.setId();
        rtAppState.setTaskId(rtOpsInfo.getTaskId());
        rtAppState.setTaskVersion(rtOpsInfo.getTaskVersion());
        rtAppState.setEngine(detailTaskInfoBo.getRtFlowInfo().getEngine());
        rtAppState.setExeMachine(JSON.toJSONString(detailTaskInfoBo.getExeMachineBoList()));
        rtAppState.setOpsAttachmentId(rtOpsAttachment.getId());
        rtAppState.setMonitorId(rtMonitor.getMonitorId());
        rtAppState.setMonitorCount(rtMonitor.getMonitorCount() + 1);

        if (rtExeInfo != null) {
            rtAppState.setPid(rtExeInfo.getPid());
            rtAppState.setAppId(rtExeInfo.getAppId());
        } else {
            rtAppState.setAppId(getAppIdFormRtOpsAttachment(rtOpsAttachment));
        }
        //??????rtAppState???????????????10R,???????????????????????????????????????????????????10C
        rtAppState.setProcessState(RecordStateEnum.StateRunning.getValue());
        //rtAppState.setAppState();
        rtAppState.setCrtTime(LocalDateTime.now());
        rtAppState.setState(RecordStateEnum.StateUse.getValue());

        appStateService.save(rtAppState);
        return rtAppState;
    }

    private RtAppState getRtAppState(Long id) {
        return appStateService.getById(id);
    }


    private ExeMachineBo getExeMachineBo(RtExeMachine executeMachine, DetailTaskInfoBo detailTaskInfoBo) {
        List<ExeMachineBo> exeMachineBoList = detailTaskInfoBo.getExeMachineBoList();
        for (ExeMachineBo exeMachineBo : exeMachineBoList) {
            if (contrastExeMachine(executeMachine, exeMachineBo)) {
                return exeMachineBo;
            }
        }
        return null;
    }

    private boolean contrastExeMachine(RtExeMachine executeMachine, ExeMachineBo exeMachineBo) {
        log.debug("----------------------------");
        log.debug(executeMachine.toString());
        log.debug("----------------------------");
        log.debug(exeMachineBo.toString());
        log.debug("----------------------------");

        if (executeMachine.getMachineId().equals(exeMachineBo.getRtExeMachine().getMachineId())
                && executeMachine.getIpAddress().trim().equals(exeMachineBo.getRtExeMachine().getIpAddress().trim())) {
            return true;
        }
        return false;
    }

    private boolean monitorSsh(ExeMachineBo exeMachineBo, RtAppState rtAppState,RtExeMetaInfo rtExeMetaInfo,RtOpsAttachment rtOpsAttachment,RtOpsInfo rtOpsInfo,String engineType) throws DataOpsException {
        SshParamRequest sshParamRequest = createSshParamRequest(exeMachineBo, rtAppState,rtExeMetaInfo,rtOpsAttachment,rtOpsInfo,engineType);
        log.debug("--------------");
        log.debug(sshParamRequest.toString());
        log.debug("--------------");
        //????????????????????????????????????jar???
        //sshServerFeign.ssh(sshParamRequest);
        return RealTimeSsh.execute(sshParamRequest, EXE_COUNT_DEFAULT);
        //return true;
    }

    private SshParamRequest createSshParamRequest(ExeMachineBo exeMachineBo,
                                                  RtAppState rtAppState,
                                                  RtExeMetaInfo rtExeMetaInfo,RtOpsAttachment rtOpsAttachment,RtOpsInfo rtOpsInfo,String engineType) {
        SshParamRequest sshParamRequest = new SshParamRequest();
        sshParamRequest.setExeServer(AppServerName);
        sshParamRequest.setExeUser(userPrincipal.getUserId());
        sshParamRequest.setExeInfoList(getExeInfoList(exeMachineBo, rtAppState,rtExeMetaInfo,rtOpsAttachment,rtOpsInfo,engineType));
        // ?????????????????????
        sshParamRequest.setExeParams("");
        return sshParamRequest;
    }

    /**
     * ??????????????????????????????
     * @param exeMachineBo ??????????????????
     * @param rtAppState   Appstate??????
     * @param rtExeMetaInfo ???????????????
     * @param rtOpsAttachment ?????????????????????
     * @param rtOpsInfo ????????????
     * @param engineType ???????????????
     * @return ?????????????????????????????????
     */
    private List<ExeInfo> getExeInfoList(ExeMachineBo exeMachineBo,
                                         RtAppState rtAppState,
                                         RtExeMetaInfo rtExeMetaInfo,RtOpsAttachment rtOpsAttachment,RtOpsInfo rtOpsInfo,String engineType) {

        Map<String,Map> commandMap = new HashMap<>();
        if (StringUtils.isNotEmpty(rtExeMetaInfo.getExeCommand())) {
            commandMap = JSON.parseObject(rtExeMetaInfo.getExeCommand(), Map.class);
        }

        //???????????????ID
        String exeMetaId= String.valueOf(rtExeMetaInfo.getRunMachineId()) ;
        //???????????????????????????
        Map<Object,Map> execCommandmap = commandMap.get(exeMetaId);
        Map<String,Object> execCommandmap2= execCommandmap.get(CommonConstants.MONITOR_COMMAND_KEY);
        JSONArray jArray = new JSONArray();
        jArray.add(execCommandmap2);
        String execCommand = jArray.toString();

        //????????????
        IEngine iEngine = EngineFactory.getIEngine(engineType);
        Map<String, String> commandPseudCodeMap = new HashMap<>();
        commandPseudCodeMap.put(PseudCodeEnum.SubmitType.getValue(), SubmitTypeEnum.Yarn.getValue());
        commandPseudCodeMap.put(PseudCodeEnum.OpsAttachmentId.getValue(), String.valueOf(rtOpsAttachment.getId()));
        commandPseudCodeMap.put(PseudCodeEnum.CodeRunId.getValue(),rtExeMetaInfo.getRunId());
        //????????????????????????????????????Appstateid
        commandPseudCodeMap.put(PseudCodeEnum.CodeMonitorParam.getValue(),String.valueOf(rtAppState.getId()));
        commandPseudCodeMap.put(PseudCodeEnum.CodeMonitorTimeOut.getValue(),String.valueOf(rtExeMetaInfo.getRestartInterval()/4*1000));
        String command = iEngine.transCommandPseudCode(
                execCommand,commandPseudCodeMap);

        List<ExeInfo> exeInfoList =JSON.parseArray(command, ExeInfo.class);
        /*if(exeInfoList!=null&&exeInfoList.size()>0){
            for (ExeInfo exeInfo :exeInfoList){
                exeInfo.setExeType(SshExeType.ExeAndPrint.getExeType());
            }
        }

        //?????????????????? ??????????????????????????????
       /* RtOpsInfo lastRtOpsInfo =
                opsInfoService.getOne(
                        Wrappers.<RtOpsInfo>lambdaQuery()
                                .eq(RtOpsInfo::getTaskId, rtOpsInfo.getTaskId())
                                .eq(RtOpsInfo::getTaskVersion, rtOpsInfo.getTaskVersion())
                                .orderByDesc(RtOpsInfo::getOpsId),
                        false);
        if (OpsTypeEnum.Ops.getValue().equals(lastRtOpsInfo.getOpsType())
                &&!OpsNameEnum.Monitoring.getValue().equals(lastRtOpsInfo.getOpsName())){
            RtOpsInfo RtOpsInfo_monitor=rtOpsInfo;
            //??????????????????
            RtOpsInfo_monitor.setOpsId(null);
            RtOpsInfo_monitor.setOpsType(OpsTypeEnum.Ops.getValue());
            RtOpsInfo_monitor.setOpsName(OpsNameEnum.Monitoring.getValue());
            opsInfoService.save(RtOpsInfo_monitor);
            System.out.println(RtOpsInfo_monitor.getOpsId());

            RtOpsAttachment rtOpsAttachment_monitor=rtOpsAttachment;
            if(exeInfoList!=null&&exeInfoList.size()>0){
                ExeInfo exeInfo = exeInfoList.get(0);
                rtOpsAttachment_monitor.setExeLogFile(exeInfo.getExeLogFile());//????????????????????????
            }
            rtOpsAttachment_monitor.setOpsCommand(command);
            rtOpsAttachment_monitor.setOpsId(RtOpsInfo_monitor.getOpsId());
            rtOpsAttachment_monitor.setId(null);
            opsAttachmentService.save(rtOpsAttachment_monitor);
        }*/






       /* ExeInfo exeInfo = new ExeInfo();
        exeInfo.setExecuteMachine(getExecuteMachine(exeMachineBo.getRtExeMachine()));
        // ??????????????????????????????????????????
        // ????????????????????????????????????????????????
        //exeInfo.setExecuteMachine();
        exeInfo.setExeCommand(getCommand(exeMachineBo, rtAppState,restartInterval));*/
        return exeInfoList;
    }

    private ExecuteMachine getExecuteMachine(RtExeMachine rtExeMachine) {
        ExecuteMachine executeMachine = new ExecuteMachine();
        executeMachine.setIp(rtExeMachine.getIpAddress());
        executeMachine.setPort(rtExeMachine.getConnectPort());
        executeMachine.setUser(rtExeMachine.getUserName());
        executeMachine.setPassword(rtExeMachine.getPassWord());
        return executeMachine;
    }

    private String getCommand(ExeMachineBo exeMachineBo, RtAppState rtAppState,Integer restartInterval) {
        /** 1.0V
         * 1 ??????shell????????????
         * 2 ??????shell??????
         * 3 ???shell???????????????????????????
         * 4 ??????shell??????
         */

        /**  2.0V
         * 1 ?????????????????????Map
         * 2 ???????????????
         * 3 ??????????????????????????????
         */
        Map<String, String> pseudCodeMap =
                monitorStateTool.generatePseudCode(exeMachineBo, rtAppState);

        String shellTemplate =
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.RealtimeMonitorShellTemplate.getValue());
        // windows???????????????linux???????????????????????????
        shellTemplate = shellTemplate.replaceAll("\r\n", "\n");
        String shell = PseudCodeUtil.replaceAll(shellTemplate, pseudCodeMap);

        shell = shell.replaceAll(CommonConstants.CodeMonitorTimeOut,String.valueOf(restartInterval/4*1000));

        Map<String, String> pathMap =
                monitorStateTool.generateDirPath(exeMachineBo, rtAppState);
        String shellDir = pathMap.get(ExeMachineKeyEnum.RealtimeMonitorShellDir.getValue());
        String logExeDir = pathMap.get(ExeMachineKeyEnum.RealtimeMonitorExeLogDir.getValue());
        String logRunDir = pathMap.get(ExeMachineKeyEnum.RealtimeMonitorRunLogDir.getValue());
        String fileName = monitorStateTool.generateFileName(rtAppState);

        String shellPath = shellDir + "/" + fileName + ".sh";
        String logExePath = logExeDir + "/" + fileName + ".log";
        String logRunPath = logRunDir + "/" + fileName + ".log";

        StringBuffer commandBuffer = new StringBuffer();
        // ??????shell???????????????
        commandBuffer.append("sh ")
                .append(exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + shellDir).append(";");
        // ??????logExe??????
        commandBuffer.append("sh ")
                .append(exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + logExeDir).append(";");
        // ??????logRun??????
        commandBuffer.append("sh ")
                .append(exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + logRunDir).append(";");
        // ??????shell??????
        commandBuffer.append("echo -e \"" + shell + "\" > " + shellPath + ";");
        commandBuffer.append(" chmod 755 " + shellPath + ";");
        commandBuffer.append("sh " + shellPath + " > " + logRunPath);

        return commandBuffer.toString();
    }


    private RtExeMetaInfo getRtExeMetaInfo(Long taskId, String taskVersion) {
        RtExeMetaInfo rtExeMetaInfo =
                exeMetaInfoService.getOne(
                        Wrappers.<RtExeMetaInfo>lambdaQuery()
                                .eq(RtExeMetaInfo::getTaskId, taskId)
                                .eq(RtExeMetaInfo::getTaskVersion, taskVersion)
                                .eq(RtExeMetaInfo::getState, RecordStateEnum.StateUse.getValue()),
                        false);

        return rtExeMetaInfo;
    }


}
