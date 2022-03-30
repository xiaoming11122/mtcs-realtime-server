package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 19:49
 * @Version 1.0
 */
public enum RecordStateEnum {

    /**
     *
     */
    StateUse(1, "10A"),
    StateNoUse(2, "10X"),
    StateRunning(3, "10R"),
    StateCompleted(4,"10C");

    /**
     *     public static String STATE_NORMAL = "10A";
     *     public static String STATE_DELETE = "10X";
     *     public static String STATE_LOST_EFFECT = "10B";
     *     public static String STATE_MODIFYING = "10U";
     *     public static String STATE_DELETEING = "10D";
     *     public static String STATE_CREATING = "10N";
     *     public static String STATE_REFRESHING = "10R";
     */


    private int code;
    private String value;

    RecordStateEnum(int code, String value) {
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
