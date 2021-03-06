package cn.ffcs.mtcs.realtime.server.core.engine.impl;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.CommonConstants;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.ExeMachineKeyEnum;
import cn.ffcs.mtcs.realtime.server.constants.PseudCodeEnum;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineFactory;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineTool;
import cn.ffcs.mtcs.realtime.server.core.engine.IEngine;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.pojo.bean.flink.FlinkAppParamBean;
import cn.ffcs.mtcs.realtime.server.pojo.bean.flink.FlinkOpsUserInfoBean;
import cn.ffcs.mtcs.realtime.server.pojo.bean.flink.FlinkParam;
import cn.ffcs.mtcs.realtime.server.pojo.bean.flink.FlinkTaskInfoBean;
import cn.ffcs.mtcs.realtime.server.pojo.bean.spark.SparkAppParamBean;
import cn.ffcs.mtcs.realtime.server.pojo.bean.spark.SparkOpsUserInfoBean;
import cn.ffcs.mtcs.realtime.server.pojo.bean.spark.SparkParam;
import cn.ffcs.mtcs.realtime.server.pojo.bean.spark.SparkTaskInfoBean;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.pojo.bo.ExeMachineBo;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import cn.ffcs.mtcs.realtime.server.util.PseudCodeUtil;
import cn.ffcs.mtcs.ssh.common.constants.SshExeType;
import cn.ffcs.mtcs.ssh.common.request.ExeInfo;
import cn.ffcs.mtcs.ssh.common.request.ExecuteMachine;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.SneakyThrows;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description . flink??????
 * @Author Nemo
 * @Date 2020/2/7/007 16:16
 * @Version 1.0
 */
@Component
public class EngineFlink implements IEngine, InitializingBean {

    @Autowired
    IRtExeMachineEnvService rtExeMachineEnvService;

    @Autowired
    IRtTaskInfoService rtTaskInfoService;

    @Autowired
    IRtExeInfoService rtExeInfoService;

    @Autowired
    IRtPluginsInfoService rtPluginsInfoService;

    @Autowired
    IRtFlowPluginRelativeService rtFlowPluginRelativeService;

    @Override
    public void afterPropertiesSet() throws Exception {
        EngineFactory.registerEngine(EngineTypeEnum.Flink.getValue(), this);
        EngineFactory.registerEngine(EngineTypeEnum.FlinkSql.getValue(), this);
    }

    @Override
    public String createExeCommand(DetailTaskInfoBo taskDetailInfoBo) {
        /**
         * ??????????????????
         *
         */
        //List<ExeInfo> exeInfoList = getExeInfoList(taskDetailInfoBo);
        Map<String, Map> exeInfoMap = getExeInfoMap(taskDetailInfoBo);
        return JSON.toJSONString(exeInfoMap);
    }

    private List<ExeInfo> getExeInfoList(DetailTaskInfoBo taskDetailInfoBo) {
        List<ExeInfo> exeInfoList = new ArrayList<>();
        for (ExeMachineBo exeMachineBo : taskDetailInfoBo.getExeMachineBoList()) {
            exeInfoList.add(getExeInfo(exeMachineBo, taskDetailInfoBo));
        }
        return exeInfoList;
    }

    private Map<String, Map> getExeInfoMap(DetailTaskInfoBo taskDetailInfoBo) {
        Map<String, Map> exeInfoMap = new HashMap<>();
        for (ExeMachineBo exeMachineBo : taskDetailInfoBo.getExeMachineBoList()) {
            exeInfoMap.put(String.valueOf(exeMachineBo.getRtExeMachine().getMachineId()),
                    getExeInfoMap(exeMachineBo, taskDetailInfoBo));
        }
        return exeInfoMap;
    }

    private ExeInfo getExeInfo(ExeMachineBo exeMachineBo, DetailTaskInfoBo taskDetailInfoBo) {
        ExeInfo exeInfo = new ExeInfo();
        EngineTool engineTool = new EngineTool();
        ExecuteMachine executeMachine = getExecuteMachine(exeMachineBo);
        String exeType = SshExeType.ExeWithoutPrint.getExeType();
        //String exeCommand = getCommand(exeMachineBo, taskDetailInfoBo);
        //???????????????????????????
        String exeCommand = engineTool.getShCommand(CommonConstants.START_COMMAND, EngineTypeEnum.Flink.getValue(),
                exeMachineBo, taskDetailInfoBo);
        String stopCommand = engineTool.getShCommand(CommonConstants.STOP_COMMAND, EngineTypeEnum.Flink.getValue(),
                exeMachineBo, taskDetailInfoBo);
        String monitorCommand = engineTool.getShCommand(CommonConstants.MONITOR_COMMAND, EngineTypeEnum.Flink.getValue(),
                exeMachineBo, taskDetailInfoBo);
        Map<String, String> exeMap = new HashMap<>();
        exeMap.put(CommonConstants.EXEC_COMMAND_KEY, exeCommand);
        exeMap.put(CommonConstants.STOP_COMMAND_KEY, stopCommand);
        exeMap.put(CommonConstants.MONITOR_COMMAND_KEY, monitorCommand);
        exeInfo.setExecuteMachine(executeMachine);
        exeInfo.setExeType(exeType);
        exeInfo.setExeCommand(JSON.toJSONString(exeMap));
        return exeInfo;
    }

    private Map<String, ExeInfo> getExeInfoMap(ExeMachineBo exeMachineBo, DetailTaskInfoBo taskDetailInfoBo) {
        Map<String, ExeInfo> exeInfoMap = new HashMap<>();
        EngineTool engineTool = new EngineTool();
        ExecuteMachine executeMachine = getExecuteMachine(exeMachineBo);

        //String exeCommand = getCommand(exeMachineBo, taskDetailInfoBo);
        //???????????????????????????
        Map<String, String> exeCommandMap = new HashMap<>();
        String exeCommand = engineTool.getShCommand(CommonConstants.START_COMMAND, EngineTypeEnum.Flink.getValue(),
                exeMachineBo, taskDetailInfoBo);
        exeCommandMap.put(CommonConstants.EXEC_COMMAND_KEY, exeCommand);
        String stopCommand = engineTool.getShCommand(CommonConstants.STOP_COMMAND, EngineTypeEnum.Flink.getValue(),
                exeMachineBo, taskDetailInfoBo);
        exeCommandMap.put(CommonConstants.STOP_COMMAND_KEY, stopCommand);
        String monitorCommand = engineTool.getShCommand(CommonConstants.MONITOR_COMMAND, EngineTypeEnum.Flink.getValue(),
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

    private ExecuteMachine getExecuteMachine(ExeMachineBo exeMachineBo) {
        ExecuteMachine executeMachine = new ExecuteMachine();
        executeMachine.setIp(exeMachineBo.getRtExeMachine().getIpAddress());
        executeMachine.setPort(exeMachineBo.getRtExeMachine().getConnectPort());
        executeMachine.setUser(exeMachineBo.getRtExeMachine().getUserName());
        executeMachine.setPassword(exeMachineBo.getRtExeMachine().getPassWord());
        return executeMachine;
    }

    /**
     * ??????????????????
     *
     * @param exeMachineBo     ???????????????????????????
     * @param taskDetailInfoBo ????????????
     * @return ????????????????????????shell??????
     */
    private String getCommand(ExeMachineBo exeMachineBo, DetailTaskInfoBo taskDetailInfoBo) {
        /**
         * 1 ?????????????????????Map
         * 2 ???????????????
         * 3 ??????????????????????????????
         * 4 ??????????????????????????????????????????????????????????????????????????????????????????
         *    ????????????/start
         *          /start/shell
         *          /start/log
         *          /restart
         *          /restart/shell
         *          /restart/log
         *          /stop
         *          /stop/shell
         *          /stop/log
         *          /monitor
         *          /monitor/shell
         *          /monitor/log
         */
        EngineTool engineTool = new EngineTool();

        //????????????
        String shellTemplate =
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.StreamingFlinkShellTemplate.getValue());

        // windows???????????????linux???????????????????????????
        shellTemplate = shellTemplate.replaceAll("\r\n", "\n");
        String shell = PseudCodeUtil.replaceAll(shellTemplate, exeMachineBo.getExeMachineEnv());


        //????????????
        String shellDir = exeMachineBo.getExeMachineEnv().get(EngineTool.APP_BASE_PATH)
                + "/start/shell"
                + "/" + PseudCodeEnum.DirDay.getValue()
                + "/" + PseudCodeEnum.DirHour.getValue();

        String logExeDir = exeMachineBo.getExeMachineEnv().get(EngineTool.APP_BASE_PATH)
                + "/start/log"
                + "/" + PseudCodeEnum.DirDay.getValue()
                + "/" + PseudCodeEnum.DirHour.getValue();


        String fileName = engineTool.generateFileName(taskDetailInfoBo);

        String shellPath = shellDir + "/" + fileName + ".sh";
        String logExePath = logExeDir + "/" + fileName + ".log";

        // ??????shell??????
        StringBuffer commandBuffer = new StringBuffer();
        // ??????shell???????????????
        commandBuffer.append("sh ")
                .append(exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + shellDir).append(";");
        // ??????logExe??????
        commandBuffer.append("sh ")
                .append(exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeMkdirShell.getValue()))
                .append(" " + logExePath).append(";");

        // ??????shell??????
        commandBuffer.append("echo -e \"" + shell + "\" > " + shellPath + ";");
        commandBuffer.append(" chmod 755 " + shellPath + ";");
        commandBuffer.append("nohup sh " + shellPath + " > " + logExePath);

        return commandBuffer.toString();
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

    /**
     * ??????????????????????????????????????????json
     *
     * @param taskDetailInfoBo
     * @return
     */
    @Override
    public String createExeParam(UserPrincipal principal, DetailTaskInfoBo taskDetailInfoBo) {
        FlinkOpsUserInfoBean userInfoBean =
                new FlinkOpsUserInfoBean(
                        principal.getUserId(),
                        principal.getUsername(),
                        principal.getDefaultTeamId());

        FlinkTaskInfoBean taskInfoBean =
                new FlinkTaskInfoBean(
                        taskDetailInfoBo.getRtTaskInfo().getTaskId(),
                        taskDetailInfoBo.getRtTaskInfo().getTaskVersion(),
                        taskDetailInfoBo.getRtTaskInfo().getTaskCode());

        String opsName = PseudCodeEnum.OpsName.getValue();

        String opsAttachmentId = PseudCodeEnum.OpsAttachmentId.getValue();

        FlinkAppParamBean appParamBean =
                new FlinkAppParamBean(taskDetailInfoBo.getAppParamInfoList());

        //String plugins = taskDetailInfoBo.getRtFlowInfo().getFlowValue();
        //??????????????????????????????????????????????????????????????????
        List<String> pluginContentList =
                getFlowPluginContentList(String.valueOf(taskDetailInfoBo.getRtFlowInfo().getFlowId()),
                        taskDetailInfoBo.getRtFlowInfo().getFlowVersion());


        FlinkParam flinkParam =
                new FlinkParam(
                        userInfoBean,
                        taskInfoBean,
                        opsName, opsAttachmentId,
                        appParamBean, pluginContentList);

        return JSON.toJSONString(flinkParam);
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

    @Override
    public String transParamPseudCode(String exeParam, String... params) {
        String opsName = params[0];
        String opsAttachmentId = params[1];
        return exeParam.replaceAll(PseudCodeEnum.OpsName.getValue(), opsName)
                .replaceAll(PseudCodeEnum.OpsAttachmentId.getValue(), opsAttachmentId);
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
            if (rtExeMachineEnv.getEnvKey().equals(ExeMachineKeyEnum.StreamingFlinkStopShellTemplate.getValue())) {
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
        Map<String, String> pseudCodeMap = engineTool.generateFlinkStopPseudCode(machineEnvMap, rtTaskInfo);

        // windows???????????????linux???????????????????????????
        stopCommandTemplate = stopCommandTemplate.replaceAll("\r\n", "\n");
        String shell = PseudCodeUtil.replaceAll(stopCommandTemplate, pseudCodeMap);
        //??????flink??????????????????????????????
        Map<String, String> pathMap = engineTool.generateFlinkStopDirPath(machineEnvMap);

        String shellDir = pathMap.get(ExeMachineKeyEnum.StrealtimeFlinkStopShellDir.getValue());
        String logExeDir = pathMap.get(ExeMachineKeyEnum.StreamingFlinkStopExeLogDir.getValue());

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

        Map<String, String> resultParam = JSON.parseObject(rtExeInfo.getExeResultParam(), HashMap.class);

        String stopCommandPreFormat = commandBuffer.toString();
        String command = stopCommandPreFormat.replaceAll(PseudCodeEnum.DirDay.getValue(), LocalDateTime.now().format(CommonConstants.dirDateTimeFormatter))
                .replaceAll(PseudCodeEnum.DirHour.getValue(), String.valueOf(LocalDateTime.now().getHour()))
                .replaceAll(PseudCodeEnum.OpsAttachmentId.getValue(), String.valueOf(rtOpsAttachment.getId()))
                .replaceAll(PseudCodeEnum.CodeFlinkAppId.getValue(), resultParam.get(ExeMachineKeyEnum.CodeFlinkAppId.getValue()))
                .replaceAll(PseudCodeEnum.CodeFlinkJobId.getValue(), resultParam.get(ExeMachineKeyEnum.CodeFlinkJobId.getValue()))
                .replaceAll(PseudCodeEnum.CodeFlinkJobManager.getValue(), resultParam.get(ExeMachineKeyEnum.CodeFlinkJobManager.getValue()));

        //??????ssh????????????
        List stopExeInfoList = getExeStopInfoList(command, executeMachine);

        return JSON.toJSONString(stopExeInfoList);
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

    //??????exeStopList ???????????????ssh
    private List<ExeInfo> getExeStopInfoList(String command, RtExeMachine exeMachine) {
        List<ExeInfo> exeInfoList = new ArrayList<>();
        exeInfoList.add(getOpsAttachmentCommand(command, exeMachine));
        return exeInfoList;
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


    @Override
    public Map<String, String> getPluginJarPseudCodeMap(String command, String basePluginPath, Long taskId, String taskVersion) {
        Map<String, String> pluginJarPseudCodeMap = new HashMap<>();
        List<RtPluginsInfo> pluginsInfoList = rtPluginsInfoService
                .listFlowPluginInfoByTask(taskId, taskVersion);
        StringBuffer sbClassPluginJarPath = new StringBuffer();
        StringBuffer sbShipPluginJarPath = new StringBuffer();
        if (pluginsInfoList != null) {
            for (RtPluginsInfo rtPluginsInfo : pluginsInfoList) {

                if (StringUtils.isNotEmpty(rtPluginsInfo.getPluginsJar())) {
                    String pluginsJar = rtPluginsInfo.getPluginsJar();
                    //???????????????????????????????????????????????????????????????{BASE_PATH}
                    if (StringUtils.isNotEmpty(basePluginPath)) {
                        pluginsJar = pluginsJar.replace("{BASE_PATH}", basePluginPath);
                    }
                    sbClassPluginJarPath.append(" -C file://").append(pluginsJar).append(" ");
                    sbShipPluginJarPath.append(" -yt ").append(pluginsJar).append(" ");
                }
            }

        }
        if (sbClassPluginJarPath.length() > 0) {
            pluginJarPseudCodeMap.put(PseudCodeEnum.PluginJarClass.getValue(), sbClassPluginJarPath.toString());
        }
        if (sbShipPluginJarPath.length() > 0) {
            pluginJarPseudCodeMap.put(PseudCodeEnum.PluginJarShip.getValue(), sbShipPluginJarPath.toString());
        }
        return pluginJarPseudCodeMap;
    }

}
