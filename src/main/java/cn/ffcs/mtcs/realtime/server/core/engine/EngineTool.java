package cn.ffcs.mtcs.realtime.server.core.engine;

import cn.ffcs.mtcs.realtime.common.entity.RtParamInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtTaskInfo;
import cn.ffcs.mtcs.realtime.server.constants.*;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.pojo.bo.ExeMachineBo;
import cn.ffcs.mtcs.realtime.server.util.PseudCodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nemo
 * @version V1.0
 * @Description: 引擎工具.
 * @date 2020/5/29 16:42
 */
public class EngineTool {

    public static String APP_BASE_PATH = "base_path";

    public static String APP_ENGINE_PATH = "engine_path";

    /**
     * 创建伪代码的键值对
     *
     * @param engine
     * @param exeMachineBo
     * @param taskDetailInfoBo
     * @return
     */
    public Map<String, String> generatePseudCode(String engine,
                                                 ExeMachineBo exeMachineBo,
                                                 DetailTaskInfoBo taskDetailInfoBo) {
        if (EngineTypeEnum.Spark.getValue().equals(engine)) {
            return generateSparkPseudCode(exeMachineBo, taskDetailInfoBo);
        } else if (EngineTypeEnum.Flink.getValue().equals(engine)) {
            return generateFlinkPseudCode(exeMachineBo, taskDetailInfoBo);
        } else if (EngineTypeEnum.Flume.getValue().equals(engine)) {
            return generateFlumePseudCode(exeMachineBo, taskDetailInfoBo);
        }
        return null;
    }

    private Map<String, String> generateSparkPseudCode(ExeMachineBo exeMachineBo,
                                                       DetailTaskInfoBo taskDetailInfoBo) {
        /**
         * 1 提交方式
         * 2 认证
         * 3 资源设置
         * 4 配置
         * 5 默认配置
         * 6 导入jar包
         * 7 设置队列
         * 8 设置files
         * 9 --name
         * 10 --class
         */
        Map<String, String> pseudCodeMap = new HashMap<>();

        Map<String, String> pathMap = generatePath(exeMachineBo, taskDetailInfoBo);
        pseudCodeMap.put(CommonConstants.CodeShellPath,
                pathMap.get(CommonConstants.CodeShellPath));
        pseudCodeMap.put(CommonConstants.CodeLogExePath,
                pathMap.get(CommonConstants.CodeLogExePath));
        pseudCodeMap.put(CommonConstants.CodeLogRunPath,
                pathMap.get(CommonConstants.CodeLogRunPath));

        pseudCodeMap.put(CommonConstants.CodeSparkDir,
                getCodeSparkDir(exeMachineBo, taskDetailInfoBo));

        // 1 提交方式
        pseudCodeMap.put(CommonConstants.CodeSparkSubmit,
                getCodeSparkSubmit(exeMachineBo, taskDetailInfoBo));
        // 2 认证
        pseudCodeMap.put(CommonConstants.CodeSparkAuthType,
                getCodeSparkAuthType(exeMachineBo, taskDetailInfoBo));
        pseudCodeMap.put(CommonConstants.CodeSparkKeytab,
                getCodeSparkKeytab(exeMachineBo, taskDetailInfoBo));
        pseudCodeMap.put(CommonConstants.CodeSparkPrincipal,
                getCodeSparkPrincipal(exeMachineBo, taskDetailInfoBo));
        // 3 资源设置
        pseudCodeMap.put(CommonConstants.CodeSparkResource,
                getCodeSparkResource(exeMachineBo, taskDetailInfoBo));
        // 4 配置
        pseudCodeMap.put(CommonConstants.CodeSparkConf,
                getCodeSparkConf(exeMachineBo, taskDetailInfoBo));
        // 5 默认配置 在模板配置中已经写好了
        // 6 导入jar包
        pseudCodeMap.put(CommonConstants.CodeSparkJars,
                getCodeSparkJars(exeMachineBo, taskDetailInfoBo));
        // 7 设置队列
        pseudCodeMap.put(CommonConstants.CodeSparkQueue,
                getCodeSparkQueue(exeMachineBo, taskDetailInfoBo));
        // 8 设置files 现在是客户端提交不是集群提交，还不需要
        pseudCodeMap.put(CommonConstants.CodeSparkFiles,
                getCodeSparkFiles(exeMachineBo, taskDetailInfoBo));
        // 9 --name
        pseudCodeMap.put(CommonConstants.CodeSparkName,
                getCodeSparkName(exeMachineBo, taskDetailInfoBo));
        // 10 --class
        pseudCodeMap.put(CommonConstants.CodeSparkJarMain,
                getCodeSparkJarMain(exeMachineBo, taskDetailInfoBo));
        // 11 jar
        pseudCodeMap.put(CommonConstants.CodeSparkJar,
                getCodeSparkJar(exeMachineBo, taskDetailInfoBo));
        // 12 param
        pseudCodeMap.put(CommonConstants.CodeSparkParam,
                getCodeSparkParam(exeMachineBo, taskDetailInfoBo));
        return pseudCodeMap;
    }

    private String getCodeSparkDir(ExeMachineBo exeMachineBo,
                                   DetailTaskInfoBo taskDetailInfoBo) {
        return exeMachineBo.getExeMachineEnv()
                .get(ExeMachineKeyEnum.StreamingSparkDir.getValue());
    }

    private String getCodeSparkSubmit(ExeMachineBo exeMachineBo,
                                      DetailTaskInfoBo taskDetailInfoBo) {
        // 提交方式先用伪码代替，在具体的操作中再去替换该伪码
        return PseudCodeEnum.SubmitType.getValue();
    }

    private String getCodeSparkAuthType(ExeMachineBo exeMachineBo,
                                        DetailTaskInfoBo taskDetailInfoBo) {
        return exeMachineBo.getExeMachineEnv()
                .get(ExeMachineKeyEnum.StreamingSparkAuthType.getValue());
    }

    private String getCodeSparkKeytab(ExeMachineBo exeMachineBo,
                                      DetailTaskInfoBo taskDetailInfoBo) {
        return exeMachineBo.getExeMachineEnv()
                .get(ExeMachineKeyEnum.Keytab.getValue());
    }


    private String getCodeSparkPrincipal(ExeMachineBo exeMachineBo,
                                         DetailTaskInfoBo taskDetailInfoBo) {
        return exeMachineBo.getExeMachineEnv()
                .get(ExeMachineKeyEnum.Principal.getValue());
    }


    private String getCodeSparkResource(ExeMachineBo exeMachineBo,
                                        DetailTaskInfoBo taskDetailInfoBo) {
        StringBuffer data = new StringBuffer();
        for (RtParamInfo rtParamInfo : taskDetailInfoBo.getRunParamInfoList()) {
            data.append("--").append(rtParamInfo.getParamKey())
                    .append(" ").append(rtParamInfo.getParamValue())
                    .append(" ");
        }
        return data.toString();
    }

    private String getCodeSparkConf(ExeMachineBo exeMachineBo,
                                    DetailTaskInfoBo taskDetailInfoBo) {
        StringBuffer data = new StringBuffer();
        for (RtParamInfo rtParamInfo : taskDetailInfoBo.getConfigParamInfoList()) {
            data.append("--conf ")
                    .append(rtParamInfo.getParamKey()).append("=").append(rtParamInfo.getParamValue())
                    .append(" ");
        }
        return data.toString();
    }

    private String getCodeSparkJars(ExeMachineBo exeMachineBo,
                                    DetailTaskInfoBo taskDetailInfoBo) {
        return exeMachineBo.getExeMachineEnv()
                .get(ExeMachineKeyEnum.StreamingSparkJars.getValue());
    }

    private String getCodeSparkQueue(ExeMachineBo exeMachineBo,
                                     DetailTaskInfoBo taskDetailInfoBo) {
        return exeMachineBo.getExeMachineEnv()
                .get(ExeMachineKeyEnum.StreamingSparkQueue.getValue());
    }


    private String getCodeSparkFiles(ExeMachineBo exeMachineBo,
                                     DetailTaskInfoBo taskDetailInfoBo) {
        return exeMachineBo.getExeMachineEnv()
                .get(ExeMachineKeyEnum.StreamingSparkFiles.getValue());
    }

    private String getCodeSparkName(ExeMachineBo exeMachineBo,
                                    DetailTaskInfoBo taskDetailInfoBo) {
        return taskDetailInfoBo.getRtTaskInfo().getTaskCode();
    }

    private String getCodeSparkJarMain(ExeMachineBo exeMachineBo,
                                       DetailTaskInfoBo taskDetailInfoBo) {
        return exeMachineBo.getExeMachineEnv()
                .get(ExeMachineKeyEnum.StreamingSparkJarMain.getValue());
    }

    private String getCodeSparkJar(ExeMachineBo exeMachineBo,
                                   DetailTaskInfoBo taskDetailInfoBo) {
        return exeMachineBo.getExeMachineEnv()
                .get(ExeMachineKeyEnum.StreamingSparkJar.getValue());
    }

    private String getCodeSparkParam(ExeMachineBo exeMachineBo,
                                     DetailTaskInfoBo taskDetailInfoBo) {
        return PseudCodeEnum.OpsAttachmentId.getValue();
    }

    /**
     * 获取flink模板伪码
     *
     * @param exeMachineBo
     * @param taskDetailInfoBo
     * @return
     */
    private Map<String, String> generateFlinkPseudCode(ExeMachineBo exeMachineBo,
                                                       DetailTaskInfoBo taskDetailInfoBo) {

        Map<String, String> pseudCodeMap = new HashMap<>();

        Map<String, String> pathMap = generatePath(exeMachineBo, taskDetailInfoBo);
        pseudCodeMap.put(CommonConstants.CodeShellPath,
                pathMap.get(CommonConstants.CodeShellPath));
        pseudCodeMap.put(CommonConstants.CodeLogExePath,
                pathMap.get(CommonConstants.CodeLogExePath));
        pseudCodeMap.put(CommonConstants.CodeLogRunPath,
                pathMap.get(CommonConstants.CodeLogRunPath));

        //flink相关配置
        pseudCodeMap.put(CommonConstants.CodeFlinkConf,
                exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.FlinkConf.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkCustomJar,
                exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.StreamingFlinkCustomJar.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkDir,
                exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.FlinkDir.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkJar,
                exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.StreamingFlinkJar.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkJarMain,
                exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.StreamingFlinkJarMain.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkJars,
                exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.StreamingFlinkJars.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkName,
                exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.StreamingFlinkName.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkParam,
                PseudCodeEnum.OpsAttachmentId.getValue());

        /*  pseudCodeMap.put(CommonConstants.CodeRunId, */

        return pseudCodeMap;
    }

    /**
     * 获取flume模板伪码
     *
     * @param exeMachineBo
     * @param taskDetailInfoBo
     * @return
     */
    private Map<String, String> generateFlumePseudCode(ExeMachineBo exeMachineBo,
                                                       DetailTaskInfoBo taskDetailInfoBo) {

        Map<String, String> pseudCodeMap = new HashMap<>();

        Map<String, String> pathMap = generatePath(exeMachineBo, taskDetailInfoBo);
        pseudCodeMap.put(CommonConstants.CodeShellPath,
                pathMap.get(CommonConstants.CodeShellPath));
        pseudCodeMap.put(CommonConstants.CodeLogExePath,
                pathMap.get(CommonConstants.CodeLogExePath));
        pseudCodeMap.put(CommonConstants.CodeLogRunPath,
                pathMap.get(CommonConstants.CodeLogRunPath));

        //flume相关配置
        pseudCodeMap.put(CommonConstants.CodeFlumeDir,
                exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.FlumeDir.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlumeParam,
                PseudCodeEnum.OpsAttachmentId.getValue());

        return pseudCodeMap;
    }

    /**
     * @param basePath
     * @param commandType
     * @param pathType
     * @return
     */
    private String getAppPath(String basePath, String commandType, String pathType) {
        return String.format("%s/%s/%s/%s", basePath, commandType, pathType, PseudCodeEnum.DirDay.getValue());
    }

    private Map<String, String> generatePathPseudCode(String basePath, String commandType) {
        Map<String, String> pseudCodeMap = new HashMap<>();
        pseudCodeMap.put(CommonConstants.CodeShellPath,
                getAppShellPath(basePath, commandType));
        pseudCodeMap.put(CommonConstants.CodeLogExePath,
                getAppLogPath(basePath, commandType));
        return pseudCodeMap;
    }

    /**
     * 获取shell路径
     *
     * @param basePath
     * @param commandType
     * @return
     */
    public String getAppShellPath(String basePath, String commandType) {
        return getAppPath(basePath, commandType, "shell");
    }


    public String getAppLogPath(String basePath, String commandType) {
        return getAppPath(basePath, commandType, "logs");
    }

    public String replaceExeMachineEnvCode(String source, Map<String, String> rule) {
        for (Map.Entry<String, String> entry : rule.entrySet()) {
            String ruleKey = entry.getKey();
            if (ruleKey.substring(0, 1).equalsIgnoreCase("%")
                    && ruleKey.substring(ruleKey.length() - 1).equalsIgnoreCase("%")) {
                source = source.replaceAll(entry.getKey(), entry.getValue());
            } else {
                source = source.replaceAll('%' + entry.getKey() + '%', entry.getValue());
            }

        }
        return source;
    }

    public String getAppLogFile(String commandType, ExeMachineBo exeMachineBo, DetailTaskInfoBo taskDetailInfoBo) {
        String basePath = exeMachineBo.getExeMachineEnv().get(EngineTool.APP_BASE_PATH);
        return getAppLogPath(basePath, commandType) + "/" + generateFileName(taskDetailInfoBo) + ".log";
    }

    /**
     * @param commandType      start,stop,monitor
     * @param engineName       引擎名称
     * @param exeMachineBo     执行机器信息
     * @param taskDetailInfoBo 任务信息
     * @return 执行命令
     */
    public String getShCommand(String commandType, String engineName,
                               ExeMachineBo exeMachineBo, DetailTaskInfoBo taskDetailInfoBo) {
        /**
         * 1 将伪码和值构成Map
         * 2 获取到模板
         * 3 将模板中的伪代码替换
         * 4 为了减少配置，一些生成执行目录信息的日志目录基于基础路径固定
         *    基础路径/start
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
        String basePath = exeMachineBo.getExeMachineEnv().get(EngineTool.APP_BASE_PATH);
        //执行命令
        String shellDir = getAppShellPath(basePath, commandType);
        //日志信息
        String logExeDir = getAppLogPath(basePath, commandType);

        String fileName = generateFileName(taskDetailInfoBo);


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

        //执行命令的模板
        String shellTemplate =
                exeMachineBo.getExeMachineEnv()
                        .get(String.format("%s_shell_template", commandType));

        // windows操作系统和linux操作系统换行符不同
        shellTemplate = shellTemplate.replaceAll("\r\n", "\n");
        String shellPath = shellDir + "/" + fileName + ".sh";
        //String logExePath = logExeDir + "/" + fileName + ".log";
        String logExePath = getAppLogFile(commandType, exeMachineBo, taskDetailInfoBo);
        //模板内容的伪码替换
        Map<String, String> pseudCodeMap = new HashMap<>();
        pseudCodeMap.put(CommonConstants.CodeShellPath, shellPath);
        pseudCodeMap.put(CommonConstants.CodeLogExePath, logExePath);
        pseudCodeMap.put(CommonConstants.CodeParam,
                PseudCodeEnum.OpsAttachmentId.getValue());
        pseudCodeMap.put(CommonConstants.CodeRunId,
                PseudCodeEnum.CodeRunId.getValue());
        // TODO 陈张圣添加，设置文件夹路径
        pseudCodeMap.put(CommonConstants.CodeBasePath, exeMachineBo.getExeMachineEnv().get(EngineTool.APP_ENGINE_PATH));
        pseudCodeMap.putAll(exeMachineBo.getExeMachineEnv());
        String shell = replaceExeMachineEnvCode(shellTemplate, pseudCodeMap);
        // 创建shell脚本
        commandBuffer.append("echo -e \"" + shell + "\" > " + shellPath + ";");
        commandBuffer.append(" chmod 755 " + shellPath + ";");
        if (EngineTypeEnum.Flink.getValue().equalsIgnoreCase(engineName)) {
            commandBuffer.append("sh " + shellPath + " > " + logExePath);
        } else {
            commandBuffer.append("nohup sh " + shellPath + " > " + logExePath);
        }

        return commandBuffer.toString();
    }

    public Map<String, String> generateDirPath(ExeMachineBo exeMachineBo,
                                               DetailTaskInfoBo taskDetailInfoBo) {

        String shellDir = exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeShellDir.getValue())
                + "/" + PseudCodeEnum.DirDay.getValue()
                + "/" + PseudCodeEnum.DirHour.getValue();

        String logExeDir = exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeExeLogDir.getValue())
                + "/" + PseudCodeEnum.DirDay.getValue()
                + "/" + PseudCodeEnum.DirHour.getValue();

        String logRunDir = exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.RealtimeRunLogDir.getValue())
                + "/" + PseudCodeEnum.DirDay.getValue()
                + "/" + PseudCodeEnum.DirHour.getValue();

        Map<String, String> pathMap = new HashMap<>();
        pathMap.put(ExeMachineKeyEnum.RealtimeShellDir.getValue(), shellDir);
        pathMap.put(ExeMachineKeyEnum.RealtimeExeLogDir.getValue(), logExeDir);
        pathMap.put(ExeMachineKeyEnum.RealtimeRunLogDir.getValue(), logRunDir);

        return pathMap;
    }

    public String generateFileName(DetailTaskInfoBo taskDetailInfoBo) {
        String fileName = taskDetailInfoBo.getRtTaskInfo().getTaskId()
                + "-" + taskDetailInfoBo.getRtTaskInfo().getTaskVersion()
                + "-" + taskDetailInfoBo.getRtTaskInfo().getTaskCode()
                + "-" + PseudCodeEnum.OpsAttachmentId.getValue()
                + "_" + PseudCodeEnum.DirNow.getValue();
        return fileName;
    }

    public Map<String, String> generatePath(ExeMachineBo exeMachineBo,
                                            DetailTaskInfoBo taskDetailInfoBo) {
        Map<String, String> dirPathMap =
                generateDirPath(exeMachineBo, taskDetailInfoBo);
        String shellDir = dirPathMap.get(ExeMachineKeyEnum.RealtimeShellDir.getValue());
        String logExeDir = dirPathMap.get(ExeMachineKeyEnum.RealtimeExeLogDir.getValue());
        String logRunDir = dirPathMap.get(ExeMachineKeyEnum.RealtimeRunLogDir.getValue());
        String fileName = generateFileName(taskDetailInfoBo);

        String shellPath = shellDir + "/" + fileName + ".sh";
        String logExePath = logExeDir + "/" + fileName + ".log";
        String logRunPath = logRunDir + "/" + fileName + ".log";

        Map<String, String> pathMap = new HashMap<>();
        pathMap.put(CommonConstants.CodeShellPath, shellPath);
        pathMap.put(CommonConstants.CodeLogExePath, logExePath);
        pathMap.put(CommonConstants.CodeLogRunPath, logRunPath);

        return pathMap;
    }


    /******** flink停止脚本伪码 **********/
    /**
     * 获取flink模板伪码
     *
     * @param rtTaskInfo
     * @param machineEnvMap
     * @return
     */
    public Map<String, String> generateFlinkStopPseudCode(Map<String, String> machineEnvMap, RtTaskInfo rtTaskInfo) {

        Map<String, String> pseudCodeMap = new HashMap<>();

        Map<String, String> pathMap = generateFlinkStopPath(machineEnvMap, rtTaskInfo);
        pseudCodeMap.put(CommonConstants.CodeShellPath,
                pathMap.get(CommonConstants.CodeShellPath));
        pseudCodeMap.put(CommonConstants.CodeLogExePath,
                pathMap.get(CommonConstants.CodeLogExePath));

        //flink相关配置
        pseudCodeMap.put(CommonConstants.CodeFlinkConf,
                machineEnvMap.get(ExeMachineKeyEnum.FlinkConf.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkCustomJar,
                machineEnvMap.get(ExeMachineKeyEnum.StreamingFlinkCustomJar.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkDir,
                machineEnvMap.get(ExeMachineKeyEnum.FlinkDir.getValue()));

        pseudCodeMap.put(CommonConstants.CodeFlinkParam,
                PseudCodeEnum.OpsAttachmentId.getValue());

        pseudCodeMap.put(PseudCodeEnum.CodeFlinkAppId.getValue(),
                PseudCodeEnum.CodeFlinkAppId.getValue());

        pseudCodeMap.put(PseudCodeEnum.CodeFlinkJobId.getValue(),
                PseudCodeEnum.CodeFlinkJobId.getValue());

        pseudCodeMap.put(PseudCodeEnum.CodeFlinkJobManager.getValue(),
                PseudCodeEnum.CodeFlinkJobManager.getValue());

        return pseudCodeMap;
    }

    public Map<String, String> generateFlinkStopPath(Map<String, String> machineEnvMap,
                                                     RtTaskInfo rtTaskInfo) {
        Map<String, String> dirPathMap =
                generateFlinkStopDirPath(machineEnvMap);
        String shellDir = dirPathMap.get(ExeMachineKeyEnum.StrealtimeFlinkStopShellDir.getValue());
        String logExeDir = dirPathMap.get(ExeMachineKeyEnum.StreamingFlinkStopExeLogDir.getValue());
        String fileName = generateStopFileName(rtTaskInfo);

        String shellPath = shellDir + "/" + fileName + ".sh";
        String logExePath = logExeDir + "/" + fileName + ".log";

        Map<String, String> pathMap = new HashMap<>();
        pathMap.put(CommonConstants.CodeShellPath, shellPath);
        pathMap.put(CommonConstants.CodeLogExePath, logExePath);

        return pathMap;
    }


    public Map<String, String> generateFlinkStopDirPath(Map<String, String> machineEnvMap) {

        String shellDir = machineEnvMap.get(ExeMachineKeyEnum.StrealtimeFlinkStopShellDir.getValue())
                + "/" + PseudCodeEnum.DirDay.getValue()
                + "/" + PseudCodeEnum.DirHour.getValue();

        String logExeDir = machineEnvMap.get(ExeMachineKeyEnum.StreamingFlinkStopExeLogDir.getValue())
                + "/" + PseudCodeEnum.DirDay.getValue()
                + "/" + PseudCodeEnum.DirHour.getValue();


        Map<String, String> pathMap = new HashMap<>();
        pathMap.put(ExeMachineKeyEnum.StrealtimeFlinkStopShellDir.getValue(), shellDir);
        pathMap.put(ExeMachineKeyEnum.StreamingFlinkStopExeLogDir.getValue(), logExeDir);

        return pathMap;
    }

    /******** flume停止脚本伪码 **********/
    /**
     * 获取flume模板伪码
     *
     * @param rtTaskInfo
     * @param machineEnvMap
     * @return
     */
    public Map<String, String> generateFlumeStopPseudCode(Map<String, String> machineEnvMap, RtTaskInfo rtTaskInfo) {

        Map<String, String> pseudCodeMap = new HashMap<>();

        Map<String, String> pathMap = generateFlumeStopPath(machineEnvMap, rtTaskInfo);
        pseudCodeMap.put(CommonConstants.CodeShellPath,
                pathMap.get(CommonConstants.CodeShellPath));
        pseudCodeMap.put(CommonConstants.CodeLogExePath,
                pathMap.get(CommonConstants.CodeLogExePath));


        pseudCodeMap.put(PseudCodeEnum.CodeAppId.getValue(),
                PseudCodeEnum.CodeAppId.getValue());

        return pseudCodeMap;
    }


    public Map<String, String> generateFlumeStopPath(Map<String, String> machineEnvMap,
                                                     RtTaskInfo rtTaskInfo) {
        Map<String, String> dirPathMap =
                generateFlumeStopDirPath(machineEnvMap);
        String shellDir = dirPathMap.get(ExeMachineKeyEnum.StrealtimeFlumeStopShellDir.getValue());
        String logExeDir = dirPathMap.get(ExeMachineKeyEnum.StreamingFlumeStopExeLogDir.getValue());
        String fileName = generateStopFileName(rtTaskInfo);

        String shellPath = shellDir + "/" + fileName + ".sh";
        String logExePath = logExeDir + "/" + fileName + ".log";

        Map<String, String> pathMap = new HashMap<>();
        pathMap.put(CommonConstants.CodeShellPath, shellPath);
        pathMap.put(CommonConstants.CodeLogExePath, logExePath);

        return pathMap;
    }


    public Map<String, String> generateFlumeStopDirPath(Map<String, String> machineEnvMap) {

        String shellDir = machineEnvMap.get(ExeMachineKeyEnum.StrealtimeFlumeStopShellDir.getValue())
                + "/" + PseudCodeEnum.DirDay.getValue()
                + "/" + PseudCodeEnum.DirHour.getValue();

        String logExeDir = machineEnvMap.get(ExeMachineKeyEnum.StreamingFlumeStopExeLogDir.getValue())
                + "/" + PseudCodeEnum.DirDay.getValue()
                + "/" + PseudCodeEnum.DirHour.getValue();


        Map<String, String> pathMap = new HashMap<>();
        pathMap.put(ExeMachineKeyEnum.StrealtimeFlumeStopShellDir.getValue(), shellDir);
        pathMap.put(ExeMachineKeyEnum.StreamingFlumeStopExeLogDir.getValue(), logExeDir);

        return pathMap;
    }


    public String generateStopFileName(RtTaskInfo rtTaskInfo) {
        String fileName = rtTaskInfo.getTaskId()
                + "-" + rtTaskInfo.getTaskVersion()
                + "-" + rtTaskInfo.getTaskCode()
                + "-" + PseudCodeEnum.OpsAttachmentId.getValue();
        return fileName;
    }


    /**
     * 解析插件，拼装jar所在目录
     * 用于各引擎获取插件需要使用的jar包目录
     *
     * @param exeMachineBo
     * @param taskDetailInfoBo
     * @param shell
     * @return
     */
    public String parsePlugins(ExeMachineBo exeMachineBo, DetailTaskInfoBo taskDetailInfoBo, String shell) {
        String pluginsJson = taskDetailInfoBo.getRtFlowInfo().getFlowValue();
        List<JSONObject> pluginList = (List<JSONObject>) JSON.parse(pluginsJson);
        //插件目录shell数组  格式:('path1' 'path2' 'path3')
        String pluginJarsArr = "";
        //遍历插件
        for (int i = 0; i < pluginList.size(); i++) {
            JSONObject instBaseInfo = (JSONObject) pluginList.get(i).get("instBaseInfo");
            //插件code
            String pluginCode = (String) instBaseInfo.get("code");
            //插件版本
            String pluginVersion = (String) instBaseInfo.get("version");

            //插件执行机上的目录
            String pluginJarsPath = exeMachineBo.getExeMachineEnv().get(ExeMachineKeyEnum.PluginJarsPath.getValue());

            if (i == 0) {
                //默认添加插件目录下的common目录
                pluginJarsArr = "('" + pluginJarsPath + "/common'";
            }

            //自定义插件
            if (pluginCode.equals(PluginTypeEnum.CustomPlugin.getCode())) {

                String customPluginJarPaht = (String) instBaseInfo.get("path");

                if (i == pluginList.size() - 1) {
                    pluginJarsArr = pluginJarsArr + " " + "'" + customPluginJarPaht + "'" + ")";
                } else {
                    pluginJarsArr = pluginJarsArr + " " + "'" + customPluginJarPaht + "'";
                }
                continue;
            }

            if (i == pluginList.size() - 1) {
                pluginJarsArr = pluginJarsArr + " " + "'" + pluginJarsPath + "/" + pluginCode + "/" + pluginVersion + "'" + ")";
            } else {
                pluginJarsArr = pluginJarsArr + " " + "'" + pluginJarsPath + "/" + pluginCode + "/" + pluginVersion + "'";
            }
        }

        shell = shell.replaceAll(CommonConstants.CodePluginJarsPath, pluginJarsArr);
        return shell;
    }

}
