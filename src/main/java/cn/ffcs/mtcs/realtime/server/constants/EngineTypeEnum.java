package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 16:34
 * @Version 1.0
 */
public enum EngineTypeEnum {
    /**
     * 引擎类型
     */
    Spark(1, "spark"),
    Flink(2, "flink"),
    Flume(3, "flume"),
    FlinkSql(4, "flinksql");

    private int code;
    private String value;

    private EngineTypeEnum(int code, String engineName) {
        this.code = code;
        this.value = engineName;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
