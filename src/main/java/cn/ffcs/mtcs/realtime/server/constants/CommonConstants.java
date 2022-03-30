package cn.ffcs.mtcs.realtime.server.constants;

import java.time.format.DateTimeFormatter;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/4/9/009 15:21
 * @Version 1.0
 */
public interface CommonConstants {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter dirDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * shell脚本中的伪码 spark
     */
    String CodeShellPath = "%CodeShellPath%";
    String CodeLogExePath = "%CodeLogExePath%";
    String CodeLogRunPath = "%CodeLogRunPath%";

    String CodeSparkDir = "%CodeSparkDir%";

    String CodeSparkSubmit = "%CodeSparkSubmit%";

    // none, kerberos
    String CodeSparkAuthType = "%CodeSparkAuthType%";
    String CodeSparkKeytab = "%CodeSparkKeytab%";
    String CodeSparkPrincipal = "%CodeSparkPrincipal%";
    String CodeSparkResource = "%CodeSparkResource%";
    String CodeSparkConf = "%CodeSparkConf%";
    String CodeSparkJars = "%CodeSparkJars%";
    String CodeSparkQueue = "%CodeSparkQueue%";
    String CodeSparkFiles = "%CodeSparkFiles%";
    String CodeSparkName = "%CodeSparkName%";
    String CodeSparkJarMain = "%CodeSparkJarMain%";
    String CodeSparkJar = "%CodeSparkJar%";
    String CodeSparkParam = "%CodeSparkParam%";

    /**
     * shell脚本中的伪码 flink
     */
    String CodeFlink = "";
    String CodeFlinkDir = "%CodeFlinkDir%";
    String CodeFlinkConf = "%CodeFlinkConf%";
    String CodeFlinkJars = "%CodeFlinkJars%";
    String CodeFlinkCustomJar = "%CodeFlinkCustomJar%";
    String CodeFlinkName = "%CodeFlinkName%";
    String CodeFlinkJarMain = "%CodeFlinkJarMain%";
    String CodeFlinkJar = "%CodeFlinkJar%";
    String CodeFlinkParam = "%CodeFlinkParam%";
    String CodeRunId = "%CodeRunId%";

    /**
     * shell脚本中的伪码 flume
     */
    String CodeBasePath = "%CodeBasePath%";
    String CodeFlumeDir = "%CodeFlumeDir%";
    String CodeFlumeParam = "%CodeFlumeParam%";
    String CodeAppId = "%CodeAppId%";
    /**
     * shell脚本中的伪代码 yarn monitor
     */
    String CodeMonitorLib = "%CodeMonitorLib%";
    String CodeMonitorJar = "%CodeMonitorJar%";
    String CodeMonitorMain = "%CodeMonitorMain%";
    String CodeMonitorParam = "%CodeMonitorParam%";
    String CodeMonitorTimeOut = "%CodeMonitorTimeOut%";

    /**
     *
     */
    String CodePluginJarsPath = "%CodePluginJarsPath%";
    String CodeParam = "%CodeParam%";

    String EXEC_COMMAND_KEY = "exec_command";
    String STOP_COMMAND_KEY = "stop_command";
    String MONITOR_COMMAND_KEY = "monitor_command";
    String START_COMMAND = "start";
    String STOP_COMMAND = "stop";
    String MONITOR_COMMAND = "monitor";

    String EXEC_RESULT_APP_ID_KEY = "AppId";
    String EXEC_RESULT_JOB_ID_KEY = "JobId";
    String EXEC_RESULT_WEB_URL_KEY = "WebUrl";

}
