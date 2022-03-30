package cn.ffcs.mtcs.realtime.server.feign.fallback;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.server.feign.SshServerFeign;
import cn.ffcs.mtcs.realtime.server.feign.UserServerFeign;
import cn.ffcs.mtcs.realtime.server.pojo.bean.login.LoginInfo;
import cn.ffcs.mtcs.realtime.server.pojo.bean.login.OauthToken;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2019/12/2/002 11:07
 * @Version 1.0
 */
@Component
@Slf4j
public class UserServerFeignFallback implements UserServerFeign {

    @Override
    public UserPrincipal getPrincipal() {
        log.debug("realtime调用user服务失败！");
        return null;
    }

    @Override
    public RetDataMsg<OauthToken> login(LoginInfo loginInfo) {
        log.debug("realtime调用user服务失败！");
        return null;
    }

}