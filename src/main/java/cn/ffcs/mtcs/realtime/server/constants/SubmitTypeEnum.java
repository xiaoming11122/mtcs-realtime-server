package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/20/020 16:47
 * @Version 1.0
 */
public enum SubmitTypeEnum {
    /**
     *
     */
    Yarn(1, "yarn");

    private int code;
    private String value;

    SubmitTypeEnum(int code, String value) {
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
