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
         * 1 创建RtAppState
         * 2 使用ssh服务运行
         * 3 通过id来获取RtAppState
         */
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getById(rtMonitor.getOpsAttachmentId());
        //rtOpsAttachment中的exeid由各个实时的应用插入，如果ExeId为空，说明应用还未成功执行

      /*  if (rtOpsAttachment.getExeId() == -1) {
            return null;
        }*/
        RtOpsInfo rtOpsInfo = opsInfoService.getById(rtOpsAttachment.getOpsId());
        RtExeInfo rtExeInfo = exeInfoService.getById(rtOpsAttachment.getExeId());
        DetailTaskInfoBo detailTaskInfoBo =
                detailTaskInfoBoService.getTaskDetailInfoBo(
                        rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());
        RtExeMetaInfo rtExeMetaInfo = getRtExeMetaInfo(rtMonitor.getTaskId(), rtMonitor.getTaskVersion());

        // 部分信息，不包含状态
        RtAppState firstrtAppState =
                createRtAppState(
                        rtMonitor,
                        rtOpsAttachment, rtOpsInfo, rtExeInfo,
                        detailTaskInfoBo);
        //执行的机器列表是在实时应用处登记，包含具体的IP，以下功能是反查具体的执行主机信息，
        // 实时应用在哪台执行，需要到哪台监控去执行监控代码
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
            //重新赋值一次
            if (StringUtils.isEmpty(firstrtAppState.getAppId()) && StringUtils.isNotEmpty(rtExeInfo.getAppId())) {
                firstrtAppState.setAppId(rtExeInfo.getAppId());
                log.debug("AppStateId={},重新更新状态AppState的appid的值{}",firstrtAppState.getId(),
                        firstrtAppState.getAppId() );
                appStateService.updateById(firstrtAppState);
            }
            try {
                //调用该监控代码将会进行rtAppState状态的更新
                monitorSsh(exeMachineBo, firstrtAppState, rtExeMetaInfo, rtOpsAttachment, rtOpsInfo, engineType);

            } catch (DataOpsException e) {
                throw new RuntimeException("执行shell脚本出错", e);
            }

        }
        log.info("---- 手工写入状态，等待监控代码获取信息更新rtAppState");
        //数据不同步,需要周期性调用获取状态
        RtAppState finalRtAppState;
        int runNum = 0;
        while (true) {
            try {
                //总共在2分钟内处理
                Thread.sleep(2 * 1000);
                runNum ++;
                //循环获取rtAppState的状态，直到ProcessState返回10C 表示处理完成
                finalRtAppState = getRtAppState(firstrtAppState.getId());
                if (RecordStateEnum.StateCompleted.getValue().equalsIgnoreCase(finalRtAppState.getProcessState())
                     || runNum >= 60) {
                    if (runNum >= 60) {
                        log.info("监控id={},长时候未执行完成，检测达到{}次！退出",rtMonitor.getMonitorId(), runNum);
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
        //应用执行结果的返回值，如包含applicationId，jobid等，为map结构
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
        //更新rtAppState处理状态为10R,注意在监控代码中执行完成需要更新为10C
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
        //不通过服务方式，直接调用jar包
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
        // 因为不需要参数
        sshParamRequest.setExeParams("");
        return sshParamRequest;
    }

    /**
     * 获取监控的命令信息等
     * @param exeMachineBo 执行机器信息
     * @param rtAppState   Appstate状态
     * @param rtExeMetaInfo 执行元信息
     * @param rtOpsAttachment 执行操作的信息
     * @param rtOpsInfo 操作信息
     * @param engineType 引擎类别等
     * @return 执行信息包含监控命令等
     */
    private List<ExeInfo> getExeInfoList(ExeMachineBo exeMachineBo,
                                         RtAppState rtAppState,
                                         RtExeMetaInfo rtExeMetaInfo,RtOpsAttachment rtOpsAttachment,RtOpsInfo rtOpsInfo,String engineType) {

        Map<String,Map> commandMap = new HashMap<>();
        if (StringUtils.isNotEmpty(rtExeMetaInfo.getExeCommand())) {
            commandMap = JSON.parseObject(rtExeMetaInfo.getExeCommand(), Map.class);
        }

        //当前执行机ID
        String exeMetaId= String.valueOf(rtExeMetaInfo.getRunMachineId()) ;
        //取该机器下对应命令
        Map<Object,Map> execCommandmap = commandMap.get(exeMetaId);
        Map<String,Object> execCommandmap2= execCommandmap.get(CommonConstants.MONITOR_COMMAND_KEY);
        JSONArray jArray = new JSONArray();
        jArray.add(execCommandmap2);
        String execCommand = jArray.toString();

        //伪码转换
        IEngine iEngine = EngineFactory.getIEngine(engineType);
        Map<String, String> commandPseudCodeMap = new HashMap<>();
        commandPseudCodeMap.put(PseudCodeEnum.SubmitType.getValue(), SubmitTypeEnum.Yarn.getValue());
        commandPseudCodeMap.put(PseudCodeEnum.OpsAttachmentId.getValue(), String.valueOf(rtOpsAttachment.getId()));
        commandPseudCodeMap.put(PseudCodeEnum.CodeRunId.getValue(),rtExeMetaInfo.getRunId());
        //监控的执行包的输入参数为Appstateid
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

        //记录操作信息 上一条不为监控时插入
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
            //记录操作信息
            RtOpsInfo_monitor.setOpsId(null);
            RtOpsInfo_monitor.setOpsType(OpsTypeEnum.Ops.getValue());
            RtOpsInfo_monitor.setOpsName(OpsNameEnum.Monitoring.getValue());
            opsInfoService.save(RtOpsInfo_monitor);
            System.out.println(RtOpsInfo_monitor.getOpsId());

            RtOpsAttachment rtOpsAttachment_monitor=rtOpsAttachment;
            if(exeInfoList!=null&&exeInfoList.size()>0){
                ExeInfo exeInfo = exeInfoList.get(0);
                rtOpsAttachment_monitor.setExeLogFile(exeInfo.getExeLogFile());//设置日志文件路径
            }
            rtOpsAttachment_monitor.setOpsCommand(command);
            rtOpsAttachment_monitor.setOpsId(RtOpsInfo_monitor.getOpsId());
            rtOpsAttachment_monitor.setId(null);
            opsAttachmentService.save(rtOpsAttachment_monitor);
        }*/






       /* ExeInfo exeInfo = new ExeInfo();
        exeInfo.setExecuteMachine(getExecuteMachine(exeMachineBo.getRtExeMachine()));
        // 修改这个执行信息中的相关信息
        // 主要是用使用这个里面的执行机信息
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
         * 1 生成shell脚本路径
         * 2 拼接shell脚本
         * 3 将shell脚本置为可执行脚本
         * 4 运行shell脚本
         */

        /**  2.0V
         * 1 将伪码和值构成Map
         * 2 获取到模板
         * 3 将模板中的伪代码替换
         */
        Map<String, String> pseudCodeMap =
                monitorStateTool.generatePseudCode(exeMachineBo, rtAppState);

        String shellTemplate =
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.RealtimeMonitorShellTemplate.getValue());
        // windows操作系统和linux操作系统换行符不同
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
        // 创建shell脚本文件夹
        commandBuffer.append("sh ")
                .append(exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + shellDir).append(";");
        // 创建logExe件夹
        commandBuffer.append("sh ")
                .append(exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + logExeDir).append(";");
        // 创建logRun件夹
        commandBuffer.append("sh ")
                .append(exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + logRunDir).append(";");
        // 创建shell脚本
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
