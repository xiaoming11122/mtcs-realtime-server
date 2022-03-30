package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description 用于参数封装中的伪代码 .
 * @Author Nemo
 * @Date 2020/2/11/011 10:18
 * @Version 1.0
 */
public enum PseudCodeEnum {

    /**
     * 伪代码
     */
    ExeOperationState(1, "%ExeOperationState%"),
    // 在拼接参数使用到，程序运行参数中
    OpsName(2, "%OpsName%"),
    OpsAttachmentId(3, "%OpsAttachmentId%"),
    // 在拼接命令使用到，启动程序脚本中参数
    SubmitType(4, "%SubmitType%"),
    DirDay(5, "%DirDay%"),
    DirHour(6, "%DirHour%"),
    CodeFlinkAppId(7, "%CodeFlinkAppId%"),
    CodeFlinkJobId(8, "%CodeFlinkJobId%"),
    CodeFlinkJobManager(9, "%CodeFlinkJobManager%"),
    CodeAppId(10, "%CodeAppId%"),
    CodeRunId(11, "%CodeRunId%"),
    CodeMonitorParam(12, "%CodeMonitorParam%"),
    CodeMonitorTimeOut(13, "%CodeMonitorTimeOut%"),
    DirNow(14, "%DirNow%"),
    CodeLastSavepoint(15, "%CodeLastSavepoint%"),
    PluginJarClass(16, "%CodePluginJarClass%"),
    PluginJarShip(17, "%CodePluginJarShip%"),
    ;

    private int code;
    private String value;

    PseudCodeEnum(int code, String value) {
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
