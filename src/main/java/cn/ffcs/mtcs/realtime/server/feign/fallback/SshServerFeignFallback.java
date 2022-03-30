package cn.ffcs.mtcs.realtime.server.feign.fallback;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.server.feign.SshServerFeign;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2019/11/28/028 17:05
 * @Version 1.0
 */
@Component
@Slf4j
public class SshServerFeignFallback implements SshServerFeign {
    @Override
    public RetDataMsg<String> ssh(SshParamRequest paramRequest) {
        log.debug("realtime调用ssh服务失败！");

        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setSuccess(false);
        retDataMsg.setMsg("服务调用失败！");
        retDataMsg.setData(null);
        return retDataMsg;
    }
}
