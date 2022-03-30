package cn.ffcs.mtcs.realtime.server.feign;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.server.feign.fallback.UserServerFeignFallback;
import cn.ffcs.mtcs.realtime.server.pojo.bean.login.LoginInfo;
import cn.ffcs.mtcs.realtime.server.pojo.bean.login.OauthToken;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Description .
 * @Author Nemo
 * @Date 2019/12/2/002 10:55
 * @Version 1.0
 */
@FeignClient(value = "USER-SERVER", fallback = UserServerFeignFallback.class)
public interface UserServerFeign {
    @RequestMapping(
            method = {RequestMethod.GET},
            value = {"/getPrincipal"}
    )
    UserPrincipal getPrincipal();

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/login")
    RetDataMsg<OauthToken> login(@RequestBody LoginInfo loginInfo);
}
