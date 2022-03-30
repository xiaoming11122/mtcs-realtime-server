package cn.ffcs.mtcs.realtime.server.pojo.bean.spark;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/12/012 11:00
 * @Version 1.0
 */
@Data
public class SparkOpsUserInfoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userName;
    private Long teamId;

    public SparkOpsUserInfoBean(Long userId, String userName, Long teamId) {
        this.userId = userId;
        this.userName = userName;
        this.teamId = teamId;
    }
}
