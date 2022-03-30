package cn.ffcs.mtcs.realtime.server.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/24/024 10:58
 * @Version 1.0
 */
@Component
public class BasicAuth {

    @Value("${xxl.job.authorization}")
    private String XxlJobAuthorization;

    public String basicAuth(){
        return XxlJobAuthorization;
    }
}
