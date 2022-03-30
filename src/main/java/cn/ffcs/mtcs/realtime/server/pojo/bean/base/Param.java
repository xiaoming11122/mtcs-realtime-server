package cn.ffcs.mtcs.realtime.server.pojo.bean.base;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/12/012 11:18
 * @Version 1.0
 */
@Data
public class Param implements Serializable {

    private static final long serialVersionUID = 1L;

    private OpsUserInfoBean userInfo;
    private TaskInfoBean taskInfo;
    private String opsName;
    /**
     * 是因为使用字符容易替换伪代码
     */
    private String opsAttachmentId;
    private AppParamBean appParam;
    private String plugins;
    private List<String> pluginContentList;

    public Param(OpsUserInfoBean userInfo,
                 TaskInfoBean taskInfo,
                 String opsName, String opsAttachmentId,
                 AppParamBean appParam, String plugins) {
        this.userInfo = userInfo;
        this.taskInfo = taskInfo;
        this.opsName = opsName;
        this.opsAttachmentId = opsAttachmentId;
        this.appParam = appParam;
        this.plugins = plugins;
    }

    public Param(OpsUserInfoBean userInfo,
                 TaskInfoBean taskInfo,
                 String opsName, String opsAttachmentId,
                 AppParamBean appParam, List<String> pluginContentList) {
        this.userInfo = userInfo;
        this.taskInfo = taskInfo;
        this.opsName = opsName;
        this.opsAttachmentId = opsAttachmentId;
        this.appParam = appParam;
        this.pluginContentList = pluginContentList;
    }
}
