package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/26/026 13:37
 * @Version 1.0
 */
public enum RestartTypeEnum {
    /**
     *
     */
    Auto(1, "auto", "自动处理"),
    Manual(2, "manual", "手工处理");


    private int code;
    private String value;
    private String show;

    RestartTypeEnum(int code, String value, String show) {
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
        for (RestartTypeEnum restartTypeEnum : RestartTypeEnum.values()) {
            if (restartTypeEnum.getShow().equals(show)) {
                return restartTypeEnum.getValue();
            }
        }
        return null;
    }

    public static String getShowByValue(String value) {
        for (RestartTypeEnum restartTypeEnum : RestartTypeEnum.values()) {
            if (restartTypeEnum.getValue().equals(value)) {
                return restartTypeEnum.getShow();
            }
        }
        return null;
    }
}
