package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 20:14
 * @Version 1.0
 */
public enum OpsTypeEnum {
    /**
     *
     */
    // Save Modify Audit Release
    Data(1, "Data"),
    // Online Offline
    Logic(2, "Logic"),
    // StartMark StartException
    // RestartMark RestartException
    // StopMark Stop StopException
    // RunException
    Mark(3, "Mark"),
    // Starting Restarting Stopping Running Update
    Ops(4, "Ops");

    private int code;
    private String value;

    OpsTypeEnum(int code, String value) {
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
