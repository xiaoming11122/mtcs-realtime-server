package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 19:43
 * @Version 1.0
 */
public enum TaskLogicStateEnum {

    /**
     *
     */
    Online(1, "online"),
    Offline(2, "offline"),
    Root(3, "root"),
    Child(4, "child");


    private int code;
    private String value;

    TaskLogicStateEnum(int code, String value) {
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
