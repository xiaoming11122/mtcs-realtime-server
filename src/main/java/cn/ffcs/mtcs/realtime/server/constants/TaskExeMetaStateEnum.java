package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 17:12
 * @Version 1.0
 */
public enum TaskExeMetaStateEnum {
    /**
     *
     */
    Init(1, "初始化"),
    Start(2, "启动"),
    Run(3, "运行"),
    Restart(4, "重启"),
    Stopping(5, "停止中"),
    Stop(6, "停止");


    private int code;
    private String value;

    private TaskExeMetaStateEnum(int code, String value) {
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
