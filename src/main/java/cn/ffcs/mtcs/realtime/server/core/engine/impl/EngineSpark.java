package cn.ffcs.mtcs.realtime.server.core.engine.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtExeMetaInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.realtime.server.constants.*;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineTool;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.pojo.bean.spark.SparkAppParamBean;
import cn.ffcs.mtcs.realtime.server.pojo.bean.spark.SparkOpsUserInfoBean;
import cn.ffcs.mtcs.realtime.server.pojo.bean.spark.SparkParam;
import cn.ffcs.mtcs.realtime.server.pojo.bean.spark.SparkTaskInfoBean;
import cn.ffcs.mtcs.realtime.server.pojo.bo.ExeMachineBo;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineFactory;
import cn.ffcs.mtcs.realtime.server.core.engine.IEngine;
import cn.ffcs.mtcs.realtime.server.util.PseudCodeUtil;
import cn.ffcs.mtcs.ssh.common.constants.SshExeType;
import cn.ffcs.mtcs.ssh.common.request.ExeInfo;
import cn.ffcs.mtcs.ssh.common.request.ExecuteMachine;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 16:15
 * @Version 1.0
 */
@Component
public class EngineSpark implements IEngine, InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        EngineFactory.registerEngine(EngineTypeEnum.Spark.getValue(), this);
    }

    /**
     * 创建执行是的脚本
     *
     * @param taskDetailInfoBo
     * @return
     */
    @Override
    public String createExeCommand(DetailTaskInfoBo taskDetailInfoBo) {
        /**
         * 生成运行脚本
         *
         */
        List<ExeInfo> exeInfoList = getExeInfoList(taskDetailInfoBo);
        return JSON.toJSONString(exeInfoList);
    }

    private List<ExeInfo> getExeInfoList(DetailTaskInfoBo taskDetailInfoBo) {
        List<ExeInfo> exeInfoList = new ArrayList<>();
        for (ExeMachineBo exeMachineBo : taskDetailInfoBo.getExeMachineBoList()) {
            exeInfoList.add(getExeInfo(exeMachineBo, taskDetailInfoBo));
        }
        return exeInfoList;
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
         * 1 将伪码和值构成Map
         * 2 获取到模板
         * 3 将模板中的伪代码替换
         */
        EngineTool engineTool = new EngineTool();
        Map<String, String> pseudCodeMap =
                engineTool.generatePseudCode(
                        EngineTypeEnum.Spark.getValue(),
                        exeMachineBo, taskDetailInfoBo);

        String shellTemplate =
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.StreamingSparkShellTemplate.getValue());
        // windows操作系统和linux操作系统换行符不同
        shellTemplate = shellTemplate.replaceAll("\r\n", "\n");
        String shell = PseudCodeUtil.replaceAll(shellTemplate, pseudCodeMap);

        //解析plugins，jars加入插件目录
        shell = engineTool.parsePlugins(exeMachineBo,taskDetailInfoBo,shell);


        Map<String, String> pathMap =
                engineTool.generateDirPath(exeMachineBo, taskDetailInfoBo);
        String shellDir = pathMap.get(ExeMachineKeyEnum.RealtimeShellDir.getValue());
        String logExeDir = pathMap.get(ExeMachineKeyEnum.RealtimeExeLogDir.getValue());
        String logRunDir = pathMap.get(ExeMachineKeyEnum.RealtimeRunLogDir.getValue());
        String fileName = engineTool.generateFileName(taskDetailInfoBo);

        String shellPath = shellDir + "/" + fileName + ".sh";
        String logExePath = logExeDir + "/" + fileName + ".log";
        String logRunPath = logRunDir + "/" + fileName + ".log";

        // 创建shell脚本
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
        commandBuffer.append("nohup sh " + shellPath + " > " + logRunPath);

        return commandBuffer.toString();
    }

    /**
     * 创建程序启动、重启时的参数，json
     *
     * @param taskDetailInfoBo
     * @return
     */
    @Override
    public String createExeParam(UserPrincipal principal, DetailTaskInfoBo taskDetailInfoBo) {
        SparkOpsUserInfoBean userInfoBean =
                new SparkOpsUserInfoBean(
                        principal.getUserId(),
                        principal.getUsername(),
                        principal.getDefaultTeamId());

        SparkTaskInfoBean taskInfoBean =
                new SparkTaskInfoBean(
                        taskDetailInfoBo.getRtTaskInfo().getTaskId(),
                        taskDetailInfoBo.getRtTaskInfo().getTaskVersion(),
                        taskDetailInfoBo.getRtTaskInfo().getTaskCode());

        String opsName = PseudCodeEnum.OpsName.getValue();

        String opsAttachmentId = PseudCodeEnum.OpsAttachmentId.getValue();

        SparkAppParamBean appParamBean =
                new SparkAppParamBean(taskDetailInfoBo.getAppParamInfoList());

        String plugins = taskDetailInfoBo.getRtFlowInfo().getFlowValue();

        SparkParam sparkParam =
                new SparkParam(
                        userInfoBean,
                        taskInfoBean,
                        opsName, opsAttachmentId,
                        appParamBean, plugins);

        return JSON.toJSONString(sparkParam);
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
        return null;
    }

    @Override
    public String transParamPseudCode(String exeParam, String... params) {
        String opsName = params[0];
        String opsAttachmentId = params[1];
        return exeParam.replaceAll(PseudCodeEnum.OpsName.getValue(), opsName)
                .replaceAll(PseudCodeEnum.OpsAttachmentId.getValue(), opsAttachmentId);
    }

    @Override
    public String createStopCommand(RtExeMetaInfo rtExeMetaInfo, RtOpsAttachment rtOpsAttachment,String... param) {
        String command = transCommandPseudCode(
                        rtExeMetaInfo.getExeCommand(),
                        SubmitTypeEnum.Yarn.getValue(),
                        String.valueOf(rtOpsAttachment.getId()));
        return command;
    }

    @Override
    public  Map<String, String> getPluginJarPseudCodeMap(String command, String basePluginPath, Long taskId, String taskVersion) {
        return new HashMap<>();
    }
}
