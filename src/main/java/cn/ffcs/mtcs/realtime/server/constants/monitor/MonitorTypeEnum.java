package cn.ffcs.mtcs.realtime.server.constants.monitor;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/24/024 9:25
 * @Version 1.0
 */
public enum MonitorTypeEnum {
    /**
     *
     */
    Yarn(1, "Yarn"),
    Flink(2, "Flink"),
    Process(3, "Process"),
    YarnMixture(4, "YarnMixture"),
    FlinkMixture(5, "YarnMixture");

    private int code;
    private String value;

    MonitorTypeEnum(int code, String value) {
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
