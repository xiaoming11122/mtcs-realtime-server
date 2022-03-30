package cn.ffcs.mtcs.realtime.server.util;

import cn.ffcs.mtcs.basis.util.Base64Util;
import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.server.exception.FeignException;
import cn.ffcs.mtcs.realtime.server.feign.UserServerFeign;
import cn.ffcs.mtcs.realtime.server.pojo.bean.login.LoginInfo;
import cn.ffcs.mtcs.realtime.server.pojo.bean.login.OauthToken;
import cn.ffcs.permission.user.UserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/24/024 10:58
 * @Version 1.0
 */
@Component
public class TokenAuth {

    @Value("${admin.user.name}")
    private String AdminUserName;

    @Value("${admin.user.password}")
    private String AdminUserPassword;

    @Autowired
    private UserServerFeign userServerFeign;

    public String tokenAuth() throws FeignException {
        String tokenPrefix = "Bearer ";
        String tokenSuffix = UserSecurity.getToken();
        String token;
        if (!tokenSuffix.equals("无法找到登录用户")) {
            token = tokenPrefix + tokenSuffix;
            System.out.println("----认证信息不为空！" + token);
            return tokenPrefix + tokenSuffix;
        } else {
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setUsername(AdminUserName);
            String password = new String(Base64Util.decodeBase64V2(AdminUserPassword));
            loginInfo.setPassword(password);
            /*token = tokenPrefix + "7d55cf18-95e5-421c-9508-c2b68c5d029c";
            System.out.println("认证信息为空，采用系统默认用户！" + token);
            return token;*/
            RetDataMsg<OauthToken> oauthToken = userServerFeign.login(loginInfo);
            if (oauthToken.getSuccess()) {
                token = tokenPrefix + oauthToken.getData().getAccessToken();
                System.out.println("----认证信息为空，采用系统默认用户！" + token);
                return token;
            } else {
                throw new FeignException("feign认证时调用用户服务失败");
            }
        }
    }
}
