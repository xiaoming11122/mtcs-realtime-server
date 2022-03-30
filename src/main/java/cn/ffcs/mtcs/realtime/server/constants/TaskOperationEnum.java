package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/10/010 16:51
 * @Version 1.0
 */
public enum TaskOperationEnum {
    /**
     *
     */
    Start(1, "markStart"),
    Restart(2, "markRestart"),
    Stop(3, "markStop"),
    Update(4, "update");


    private int code;
    private String value;

    TaskOperationEnum(int code, String value) {
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
