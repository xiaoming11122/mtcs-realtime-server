package cn.ffcs.mtcs.realtime.server.pojo.bean.spark;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/12/012 11:02
 * @Version 1.0
 */
@Data
public class SparkParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private SparkOpsUserInfoBean userInfo;
    private SparkTaskInfoBean taskInfo;
    private String opsName;
    /**
     * 是因为使用字符容易替换伪代码
     */
    private String opsAttachmentId;
    private Map<String, String> appParam;
    private String plugins;

    public SparkParam(SparkOpsUserInfoBean userInfo,
                      SparkTaskInfoBean taskInfo,
                      String opsName, String opsAttachmentId,
                      SparkAppParamBean appParam, String plugins) {
        this.userInfo = userInfo;
        this.taskInfo = taskInfo;
        this.opsName = opsName;
        this.opsAttachmentId = opsAttachmentId;
        this.appParam = appParam.getAppParam();
        this.plugins = plugins;
    }
}
