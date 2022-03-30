package cn.ffcs.mtcs.realtime.server.pojo.bean.flink;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/12/012 11:19
 * @Version 1.0
 */
@Data
public class FlinkTaskInfoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taskId;
    private String taskVersion;
    private String taskCode;

    public FlinkTaskInfoBean(Long taskId, String taskVersion, String taskCode) {
        this.taskId = taskId;
        this.taskVersion = taskVersion;
        this.taskCode = taskCode;
    }
}