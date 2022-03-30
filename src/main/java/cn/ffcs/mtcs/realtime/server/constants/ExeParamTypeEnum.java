package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 19:52
 * @Version 1.0
 */
public enum ExeParamTypeEnum {

    /**
     * 参数类型
     */
    Application(1, "application"),
    Run(2, "run"),
    Config(3, "config");


    private int code;
    private String value;

    ExeParamTypeEnum(int code, String value) {
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
