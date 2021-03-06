package cn.ffcs.mtcs.realtime.server.core.engine.impl;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.CommonConstants;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.ExeMachineKeyEnum;
import cn.ffcs.mtcs.realtime.server.constants.PseudCodeEnum;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineFactory;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineTool;
import cn.ffcs.mtcs.realtime.server.core.engine.IEngine;
import cn.ffcs.mtcs.realtime.server.pojo.bean.base.AppParamBean;
import cn.ffcs.mtcs.realtime.server.pojo.bean.base.OpsUserInfoBean;
import cn.ffcs.mtcs.realtime.server.pojo.bean.base.Param;
import cn.ffcs.mtcs.realtime.server.pojo.bean.base.TaskInfoBean;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.pojo.bo.ExeMachineBo;
import cn.ffcs.mtcs.realtime.server.service.data.IRtExeInfoService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtExeMachineEnvService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtFlowPluginRelativeService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskInfoService;
import cn.ffcs.mtcs.realtime.server.util.PseudCodeUtil;
import cn.ffcs.mtcs.ssh.common.constants.SshExeType;
import cn.ffcs.mtcs.ssh.common.request.ExeInfo;
import cn.ffcs.mtcs.ssh.common.request.ExecuteMachine;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 16:16
 * @Version 1.0
 */
@Component
public class EngineFlume implements IEngine, InitializingBean {

    @Autowired
    IRtFlowPluginRelativeService rtFlowPluginRelativeService;


    @Autowired
    IRtExeMachineEnvService rtExeMachineEnvService;


    @Autowired
    IRtTaskInfoService rtTaskInfoService;

    @Autowired
    IRtExeInfoService rtExeInfoService;

    @Override
    public void afterPropertiesSet() throws Exception {
        EngineFactory.registerEngine(EngineTypeEnum.Flume.getValue(), this);
    }


    @Override
    public String createExeCommand(DetailTaskInfoBo taskDetailInfoBo) {
        /**
         * ??????????????????
         *
         */
        // List<ExeInfo> exeInfoList = getExeInfoList(taskDetailInfoBo);
        // TODO ?????????
        Map<String, Map> exeInfoMap = getExeInfoMap(taskDetailInfoBo);
        return JSON.toJSONString(exeInfoMap);
    }

    private Map<String, Map> getExeInfoMap(DetailTaskInfoBo taskDetailInfoBo) {
        Map<String, Map> exeInfoMap = new HashMap<>();
        for (ExeMachineBo exeMachineBo : taskDetailInfoBo.getExeMachineBoList()) {
            exeInfoMap.put(String.valueOf(exeMachineBo.getRtExeMachine().getMachineId()),
                    getExeInfoMap(exeMachineBo, taskDetailInfoBo));
        }
        return exeInfoMap;
    }

    private Map<String, ExeInfo> getExeInfoMap(ExeMachineBo exeMachineBo, DetailTaskInfoBo taskDetailInfoBo) {
        Map<String, ExeInfo> exeInfoMap = new HashMap<>();
        EngineTool engineTool = new EngineTool();
        ExecuteMachine executeMachine = getExecuteMachine(exeMachineBo);

        // String exeCommand = getCommand(exeMachineBo, taskDetailInfoBo);
        //???????????????????????????
        Map<String, String> exeCommandMap = new HashMap<>();
        String exeCommand = engineTool.getShCommand(CommonConstants.START_COMMAND, EngineTypeEnum.Flume.getValue(),
                exeMachineBo, taskDetailInfoBo);
        exeCommandMap.put(CommonConstants.EXEC_COMMAND_KEY, exeCommand);
        String stopCommand = engineTool.getShCommand(CommonConstants.STOP_COMMAND, EngineTypeEnum.Flume.getValue(),
                exeMachineBo, taskDetailInfoBo);
        exeCommandMap.put(CommonConstants.STOP_COMMAND_KEY, stopCommand);
        String monitorCommand = engineTool.getShCommand(CommonConstants.MONITOR_COMMAND, EngineTypeEnum.Flume.getValue(),
                exeMachineBo, taskDetailInfoBo);
        exeCommandMap.put(CommonConstants.MONITOR_COMMAND_KEY, monitorCommand);
        String exeType = SshExeType.ExeWithoutPrint.getExeType();
        for (Map.Entry<String, String> m : exeCommandMap.entrySet()) {
            ExeInfo exeInfo = new ExeInfo();
            exeInfo.setExecuteMachine(executeMachine);
            exeInfo.setExeType(exeType);
            String commandType = "";
            switch (m.getKey()) {
                case CommonConstants.EXEC_COMMAND_KEY:
                    commandType = CommonConstants.START_COMMAND;
                    break;
                case CommonConstants.STOP_COMMAND_KEY:
                    commandType = CommonConstants.STOP_COMMAND;
                    break;
                default:
                    commandType = CommonConstants.MONITOR_COMMAND;
            }
            exeInfo.setExeCommand(m.getValue());
            exeInfo.setExeLogFile(engineTool.getAppLogFile(commandType, exeMachineBo, taskDetailInfoBo));
            exeInfoMap.put(m.getKey(), exeInfo);
        }
        return exeInfoMap;
    }

    /**
     * ??????????????????????????????????????????json
     *
     * @param taskDetailInfoBo
     * @return
     */
    @Override
    public String createExeParam(UserPrincipal principal, DetailTaskInfoBo taskDetailInfoBo) {

        OpsUserInfoBean userInfoBean =
                new OpsUserInfoBean(
                        principal.getUserId(),
                        principal.getUsername(),
                        principal.getDefaultTeamId());

        TaskInfoBean taskInfoBean =
                new TaskInfoBean(
                        taskDetailInfoBo.getRtTaskInfo().getTaskId(),
                        taskDetailInfoBo.getRtTaskInfo().getTaskVersion(),
                        taskDetailInfoBo.getRtTaskInfo().getTaskCode());

        String opsName = PseudCodeEnum.OpsName.getValue();

        String opsAttachmentId = PseudCodeEnum.OpsAttachmentId.getValue();

        AppParamBean appParamBean =
                new AppParamBean(taskDetailInfoBo.getAppParamInfoList());
        //String plugins = taskDetailInfoBo.getRtFlowInfo().getFlowValue();
        //??????????????????????????????????????????????????????????????????
        List<String> pluginContentList =
                getFlowPluginContentList(String.valueOf(taskDetailInfoBo.getRtFlowInfo().getFlowId()),
                        taskDetailInfoBo.getRtFlowInfo().getFlowVersion());
        Param param =
                new Param(
                        userInfoBean,
                        taskInfoBean,
                        opsName, opsAttachmentId,
                        appParamBean, pluginContentList);

        return JSON.toJSONString(param);
    }

    private List<ExeInfo> getExeInfoList(DetailTaskInfoBo taskDetailInfoBo) {
        List<ExeInfo> exeInfoList = new ArrayList<>();
        for (ExeMachineBo exeMachineBo : taskDetailInfoBo.getExeMachineBoList()) {
            exeInfoList.add(getExeInfo(exeMachineBo, taskDetailInfoBo));
        }
        return exeInfoList;
    }

    private List<String> getFlowPluginContentList(String flowId, String flowVersion) {
        List<String> pluginContentList = new ArrayList<>();
        List<RtFlowPluginRelative> flowPluginRelativeList = rtFlowPluginRelativeService.lambdaQuery().eq(RtFlowPluginRelative::getFlowId, flowId)
                .eq(RtFlowPluginRelative::getFlowVersion, flowVersion).list();
        for (RtFlowPluginRelative rtFlowPluginRelative : flowPluginRelativeList) {
            pluginContentList.add(rtFlowPluginRelative.getPluginContent());

        }
        return pluginContentList;

    }

    private ExeInfo getExeInfo(ExeMachineBo exeMachineBo, DetailTaskInfoBo taskDetailInfoBo) {
        ExeInfo exeInfo = new ExeInfo();

        ExecuteMachine executeMachine = getExecuteMachine(exeMachineBo);
        String exeType = SshExeType.ExeWithoutPrint.getExeType();
        String exeCommand = getCommand(exeMachineBo, taskDetailInfoBo);

        exeInfo.setExecuteMachine(executeMachine);
        exeInfo.setExeType(exeType);
        exeInfo.setExeCommand(exeCommand);

        return exeInfo;
    }


    private ExecuteMachine getExecuteMachine(ExeMachineBo exeMachineBo) {
        ExecuteMachine executeMachine = new ExecuteMachine();
        executeMachine.setIp(exeMachineBo.getRtExeMachine().getIpAddress());
        executeMachine.setPort(exeMachineBo.getRtExeMachine().getConnectPort());
        executeMachine.setUser(exeMachineBo.getRtExeMachine().getUserName());
        executeMachine.setPassword(exeMachineBo.getRtExeMachine().getPassWord());
        return executeMachine;
    }

    private String getCommand(ExeMachineBo exeMachineBo, DetailTaskInfoBo taskDetailInfoBo) {
        /**
         * 1 ?????????????????????Map
         * 2 ???????????????
         * 3 ??????????????????????????????
         */
        EngineTool engineTool = new EngineTool();
        Map<String, String> pseudCodeMap =
                engineTool.generatePseudCode(
                        EngineTypeEnum.Flume.getValue(),
                        exeMachineBo, taskDetailInfoBo);

        String shellTemplate =
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.StreamingFlumeShellTemplate.getValue());
        // windows???????????????linux???????????????????????????
        shellTemplate = shellTemplate.replaceAll("\r\n", "\n");
        String shell = PseudCodeUtil.replaceAll(shellTemplate, pseudCodeMap);

        Map<String, String> pathMap =
                engineTool.generateDirPath(exeMachineBo, taskDetailInfoBo);
        String shellDir = pathMap.get(ExeMachineKeyEnum.RealtimeShellDir.getValue());
        String logExeDir = pathMap.get(ExeMachineKeyEnum.RealtimeExeLogDir.getValue());
        String logRunDir = pathMap.get(ExeMachineKeyEnum.RealtimeRunLogDir.getValue());
        String fileName = engineTool.generateFileName(taskDetailInfoBo);

        String shellPath = shellDir + "/" + fileName + ".sh";
        String logExePath = logExeDir + "/" + fileName + ".log";
        String logRunPath = logRunDir + "/" + fileName + ".log";

        // ??????shell??????
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
        commandBuffer.append("nohup sh " + shellPath + " > " + logRunPath);

        return commandBuffer.toString();
    }

    @Override
    public String transCommandPseudCode(String command, String... params) {
        String submitType = params[0];
        String opsAttachmentId = params[1];
        return command.replaceAll(PseudCodeEnum.DirDay.getValue(), LocalDateTime.now().format(CommonConstants.dirDateTimeFormatter))
                .replaceAll(PseudCodeEnum.DirHour.getValue(), String.valueOf(LocalDateTime.now().getHour()))
                .replaceAll(PseudCodeEnum.SubmitType.getValue(), submitType)
                .replaceAll(PseudCodeEnum.OpsAttachmentId.getValue(), opsAttachmentId);

    }


    @Override
    public String transParamPseudCode(String param, String... params) {
        String opsName = params[0];
        String opsAttachmentId = params[1];
        return param.replaceAll(PseudCodeEnum.OpsName.getValue(), opsName)
                .replaceAll(PseudCodeEnum.OpsAttachmentId.getValue(), opsAttachmentId);
    }

    @Override
    public String transCommandPseudCode(String command, Map<String, String> pseudCodeMap) {
        //????????????????????????????????????????????????
        String commandResult = command.replaceAll(PseudCodeEnum.DirDay.getValue(), LocalDateTime.now().format(CommonConstants.dirDateTimeFormatter))
                .replaceAll(PseudCodeEnum.DirHour.getValue(), String.valueOf(LocalDateTime.now().getHour()))
                .replaceAll(PseudCodeEnum.DirNow.getValue(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        for (Map.Entry<String, String> m : pseudCodeMap.entrySet()) {
            if (StringUtils.isNotEmpty(m.getValue())) {
                commandResult = commandResult.replaceAll(m.getKey(), m.getValue());
            } else {
                commandResult = commandResult.replaceAll(m.getKey(), "");
            }
        }
        return commandResult;
    }

    private RtExeMachine getRtExeMachineByExeInfo(String exeMachineJson) {
        if (exeMachineJson.startsWith("[")) {
            List<RtExeMachine> rtExeMachineList = JSON.parseArray(exeMachineJson, RtExeMachine.class);
            Random random = new Random();
            return rtExeMachineList.get(random.nextInt(rtExeMachineList.size()));
        } else /*if (exeMachineJson.startsWith("{"))*/ {
            return JSON.parseObject(exeMachineJson, RtExeMachine.class);
        }
    }

    //??????opsAttachment command????????????
    private ExeInfo getOpsAttachmentCommand(String command, RtExeMachine exeMachine) {
        ExeInfo exeInfo = new ExeInfo();

        ExecuteMachine executeMachine = new ExecuteMachine();
        executeMachine.setIp(exeMachine.getIpAddress());
        executeMachine.setPassword(exeMachine.getPassWord());
        executeMachine.setPort(exeMachine.getConnectPort());
        executeMachine.setUser(exeMachine.getUserName());

        String exeType = SshExeType.ExeWithoutPrint.getExeType();

        exeInfo.setExecuteMachine(executeMachine);
        exeInfo.setExeType(exeType);
        exeInfo.setExeCommand(command);

        return exeInfo;
    }

    //??????exeStopList ???????????????ssh
    private List<ExeInfo> getExeStopInfoList(String command, RtExeMachine exeMachine) {
        List<ExeInfo> exeInfoList = new ArrayList<>();
        exeInfoList.add(getOpsAttachmentCommand(command, exeMachine));
        return exeInfoList;
    }

    @Override
    public String createStopCommand(RtExeMetaInfo rtExeMetaInfo, RtOpsAttachment rtOpsAttachment, String... params) {
        String exeId = params[0];

        //?????????????????????
        RtExeMachine executeMachine = getRtExeMachineByExeInfo(rtExeMetaInfo.getExeMachine());
        //?????????????????????????????????
        List<RtExeMachineEnv> rtExeMachineEnvList = rtExeMachineEnvService.getRtExeMachineEnvList(executeMachine.getMachineId());
        //????????????map
        Map<String, String> machineEnvMap = new HashMap<>();
        //????????????
        String stopCommandTemplate = "";
        //????????????????????????????????????map
        for (RtExeMachineEnv rtExeMachineEnv : rtExeMachineEnvList) {
            machineEnvMap.put(rtExeMachineEnv.getEnvKey(), rtExeMachineEnv.getEnvValue());
            if (rtExeMachineEnv.getEnvKey().equals(ExeMachineKeyEnum.StreamingFlumeStopShellTemplate.getValue())) {
                stopCommandTemplate = rtExeMachineEnv.getEnvValue();
            }
        }
        //??????????????????
        RtTaskInfo rtTaskInfo = rtTaskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskId, rtExeMetaInfo.getTaskId())
                        .eq(RtTaskInfo::getTaskVersion, rtExeMetaInfo.getTaskVersion()),
                false);

        EngineTool engineTool = new EngineTool();
        //??????????????????
        Map<String, String> pseudCodeMap = engineTool.generateFlumeStopPseudCode(machineEnvMap, rtTaskInfo);

        // windows???????????????linux???????????????????????????
        stopCommandTemplate = stopCommandTemplate.replaceAll("\r\n", "\n");
        String shell = PseudCodeUtil.replaceAll(stopCommandTemplate, pseudCodeMap);
        //??????flume??????????????????????????????
        Map<String, String> pathMap = engineTool.generateFlumeStopDirPath(machineEnvMap);

        String shellDir = pathMap.get(ExeMachineKeyEnum.StrealtimeFlumeStopShellDir.getValue());
        String logExeDir = pathMap.get(ExeMachineKeyEnum.StreamingFlumeStopExeLogDir.getValue());

        String shellPath = pseudCodeMap.get(CommonConstants.CodeShellPath);
        String logExePath = pseudCodeMap.get(CommonConstants.CodeLogExePath);

        // ??????shell??????
        StringBuffer commandBuffer = new StringBuffer();
        // ??????shell???????????????
        commandBuffer.append("sh ")
                .append(machineEnvMap.get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + shellDir).append(";");
        // ??????logExe??????
        commandBuffer.append("sh ")
                .append(machineEnvMap.get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + logExeDir).append(";");
        // ??????shell??????
        commandBuffer.append("echo -e \"" + shell + "\" > " + shellPath + ";");
        commandBuffer.append(" chmod 755 " + shellPath + ";");
        commandBuffer.append("nohup sh " + shellPath + " > " + logExePath);


        //??????????????????
        RtExeInfo rtExeInfo = rtExeInfoService.getById(exeId);

        String stopCommandPreFormat = commandBuffer.toString();
        String command = stopCommandPreFormat.replaceAll(PseudCodeEnum.DirDay.getValue(), LocalDateTime.now().format(CommonConstants.dirDateTimeFormatter))
                .replaceAll(PseudCodeEnum.DirHour.getValue(), String.valueOf(LocalDateTime.now().getHour()))
                .replaceAll(PseudCodeEnum.OpsAttachmentId.getValue(), String.valueOf(rtOpsAttachment.getId()))
                .replaceAll(PseudCodeEnum.CodeAppId.getValue(), rtExeInfo.getAppId());

        //??????ssh????????????
        List stopExeInfoList = getExeStopInfoList(command, executeMachine);

        return JSON.toJSONString(stopExeInfoList);
    }

    @Override
    public  Map<String, String> getPluginJarPseudCodeMap(String command, String basePluginPath, Long taskId, String taskVersion) {
        return new HashMap<>();
    }
}
