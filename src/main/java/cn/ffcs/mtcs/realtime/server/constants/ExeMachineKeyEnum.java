package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/11/011 16:28
 * @Version 1.0
 */
public enum ExeMachineKeyEnum {
    /**
     * 参数类型
     */
    // 认证信息
    Krb5conf(0, "krb5conf", ""),
    Keytab(1, "keytab", ""),
    Principal(2, "principal", ""),

    // 执行文件夹
    RealtimeShellDir(5, "realtime_shell_dir", ""),
    RealtimeRunLogDir(6, "realtime_run_log_dir", ""),
    RealtimeExeLogDir(7, "realtime_exe_log_dir", ""),
    // spark安装目录
    StreamingSparkDir(7, "streaming_spark_dir", ""),
    // spark执行认证类型
    StreamingSparkAuthType(8, "streaming_spark_auth_type", ""),
    // 执行脚本模板
    StreamingSparkShellTemplate(8, "streaming_spark_shell_template", ""),
    // spark执行队列
    StreamingSparkQueue(8, "streaming_spark_queue", ""),
    // spark执行的依赖包和相关文件
    StreamingSparkJars(8, "streaming_spark_jars", ""),
    StreamingSparkFiles(8, "streaming_spark_files", ""),
    // spark执行jar包和主函数
    StreamingSparkJar(8, "streaming_spark_jar", ""),
    StreamingSparkJarMain(4, "streaming_spark_jar_main", ""),


    // flink执行jar包和主函数
    StreamingFlinkJar(9, "streaming_flink_jar", ""),
    StreamingFlinkJars(9, "streaming_flink_jars", ""),
    StreamingFlinkJarMain(3, "streaming_flink_jar_main", ""),
    StreamingFlinkName(3, "streaming_flink_name", ""),
    StreamingFlinkParam(3, "streaming_flink_param", ""),
    FlinkConf(3, "flink_conf", ""),
    FlinkDir(3, "flink_dir", ""),
    FlumeDir(3, "flume_dir", ""),

    StreamingFlinkCustomJar(3, "streaming_flink_custom_jar", ""),
    StreamingFlinkShellTemplate(3, "streaming_flink_shell_template", ""),
    // flume
    StreamingFlumeShellTemplate(3, "streaming_flume_shell_template", ""),

    //flink停止脚本模板
    StreamingFlinkStopShellTemplate(3, "streaming_flink_stop_shell_template", ""),

    StrealtimeFlinkStopShellDir(3, "streaming_flink_stop_shell_dir", ""),

    StreamingFlinkStopExeLogDir(3, "streaming_flink_stop_exe_log_dir", ""),

    CodeFlinkJobId(3, "CodeFlinkJobId", ""),

    CodeFlinkJobManager(3, "CodeFlinkJobManager", ""),

    CodeFlinkAppId(3, "CodeFlinkAppId", ""),


    //flume停止脚本模板
    StreamingFlumeStopShellTemplate(3, "streaming_flume_stop_shell_template", ""),

    StrealtimeFlumeStopShellDir(3, "streaming_flume_stop_shell_dir", ""),

    StreamingFlumeStopExeLogDir(3, "streaming_flume_stop_exe_log_dir", ""),


    CodeAppId(3, "CodeAppId", ""),



    // 监控文件夹
    RealtimeMonitorShellDir(10, "realtime_monitor_shell_dir", ""),
    RealtimeMonitorRunLogDir(11, "realtime_monitor_run_log_dir", ""),
    RealtimeMonitorExeLogDir(12, "realtime_monitor_exe_log_dir", ""),
    // 监控shell模板
    RealtimeMonitorShellTemplate(15, "realtime_monitor_shell_template", ""),
    // 监控jar包和主函数
    RealtimeMonitorJarLib(13, "realtime_monitor_jar_lib", ""),
    RealtimeMonitorJar(13, "realtime_monitor_jar", ""),
    RealtimeMonitorMain(14, "realtime_monitor_main", ""),


    RealtimeMkdirShell(16, "realtime_mkdir_shell", ""),


    PluginJarsPath(16, "plugin_jars_path", "");


    private int code;
    private String value;
    private String show;

    ExeMachineKeyEnum(int code, String value, String show) {
        this.code = code;
        this.value = value;
        this.show = show;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getShow() {
        return show;
    }
}
