package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/23/023 20:33
 * @Version 1.0
 */
public enum XxlJobExeHandlerEnum {
    /**
     * 这个必须要和core.job.XxlJob...处理类中的配置一致
     */
    SparkStart(1, "sparkStart"),
    SparkRestart(2, "sparkRestart"),
    SparkStop(3, "sparkStop"),
    SparkRun(4, "sparkRun"),
    FlinkStart(5, "flinkStart"),
    FlinkRestart(6, "flinkRestart"),
    FlinkStop(7, "flinkStop"),
    FlinkRun(8, "flinkRun"),
    FlumeStart(9, "flumeStart"),
    FlumeRestart(10, "flumeRestart"),
    FlumeStop(11, "flumeStop"),
    FlumeRun(12, "flumeRun"),
    FlinkAppStart(5, "flinkAppStart"),
    FlinkAppRestart(6, "flinkAppRestart"),
    FlinkAppRun(8, "flinkAppRun"),
    FlinkAppStop(7, "flinkAppStop"),;


    private int code;
    private String value;

    XxlJobExeHandlerEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
