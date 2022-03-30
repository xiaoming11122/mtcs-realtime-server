package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 20:19
 * @Version 1.0
 */
public enum OpsNameEnum {
    /**
     *
     */
    Save(1, "Save", "暂时保存"),
    Modify(2, "Modify", "修改"),
    Audit(3, "Audit", "保存审核"),
    Release(4, "Release", "发布"),
    StartMark(5, "StartMark", "标记启动"),
    Starting(6, "Starting", "启动中"),
    StartException(7, "StartException", "启动异常"),
    RestartMark(8, "RestartMark", "标记重启"),
    Restarting(9, "Restarting", "重启中"),
    RestartException(10, "RestartException", "重启异常"),
    StopMark(11, "StopMark", "标记停止"),
    Stopping(12, "Stopping", "停止中"),
    Stop(13, "Stop", "已停止"),
    StopException(14, "StopException", "停止异常"),
    Running(15, "Running", "运行中"),
    RunException(16, "RunException", "运行异常"),
    MonitorException(16, "MonitorException", "监控等待"),
    Online(17, "Online", "上线"),
    Offline(18, "Offline", "下线"),
    Update(19, "Update", "更新"),
    NoApproved(20, "NoApproved", "审核不通过"),
    Monitoring(21, "Monitoring", "监控中"),
    ExceptionRestarting(22, "ExceptionRestarting", "异常重启中");


    private int code;
    private String value;
    private String show;

    OpsNameEnum(int code, String value, String show) {
        this.code = code;
        this.value = value;
        this.show = show;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getShow() {
        return show;
    }

    public static String getValueByShow(String show) {
        for (OpsNameEnum opsNameEnum : OpsNameEnum.values()) {
            if (opsNameEnum.getShow().equals(show)) {
                return opsNameEnum.getValue();
            }
        }
        return null;
    }

    public static String getShowByValue(String value) {
        for (OpsNameEnum opsNameEnum : OpsNameEnum.values()) {
            if (opsNameEnum.getValue().equals(value)) {
                return opsNameEnum.getShow();
            }
        }
        return null;
    }
}
