package cn.ffcs.mtcs.realtime.server.feign;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.server.feign.fallback.SshServerFeignFallback;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description .
 * @Author Nemo
 * @Date 2019/11/28/028 17:05
 * @Version 1.0
 */
@FeignClient(name = "MTCS-SSH-SERVER", fallback = SshServerFeignFallback.class)
public interface SshServerFeign {
    /**
     *
     * @param paramRequest
     * @return
     */
    @PostMapping("/ssh")
    RetDataMsg<String> ssh(@RequestBody SshParamRequest paramRequest);
}
