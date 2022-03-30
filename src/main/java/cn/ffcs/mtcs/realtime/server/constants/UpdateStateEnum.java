package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/16/016 16:30
 * @Version 1.0
 */
public enum UpdateStateEnum {
    /**
     * 参数类型
     */
    Init(1, "init"),
    Doing(2, "doing"),
    Finish(3, "finish");


    private int code;
    private String value;

    UpdateStateEnum(int code, String value) {
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
