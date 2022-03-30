package cn.ffcs.mtcs.realtime.server.pojo.bean.login;

import lombok.Data;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/24/024 15:26
 * @Version 1.0
 */
@Data
public class OauthToken {
    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private String expiresIn;
    private String scope;
}
