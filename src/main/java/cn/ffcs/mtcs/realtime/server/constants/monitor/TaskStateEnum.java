package cn.ffcs.mtcs.realtime.server.constants.monitor;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/24/024 9:36
 * @Version 1.0
 */
public enum TaskStateEnum {
    /**
     * 参数类型
     */
    NEW(1, "NEW"),
    NEW_SAVING(2, "NEW_SAVING"),
    SUBMITTED(3, "SUBMITTED"),
    ACCEPTED(4, "ACCEPTED"),
    RUNNING(5, "RUNNING"),
    FINISHED(6, "FINISHED"),
    FAILED(7, "FAILED"),
    KILLED(8, "KILLED"),
    STOPPED(9, "STOPPED");;


    private int code;
    private String value;

    TaskStateEnum(int code, String value) {
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
