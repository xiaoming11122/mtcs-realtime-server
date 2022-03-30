package cn.ffcs.mtcs.realtime.server.config;

import cn.ffcs.mtcs.realtime.server.exception.FeignException;
import cn.ffcs.mtcs.realtime.server.util.BasicAuth;
import cn.ffcs.mtcs.realtime.server.util.TokenAuth;
import cn.ffcs.permission.fegin.FeginManager;
import cn.ffcs.permission.fegin.ILocalFegin;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/20/020 19:39
 * @Version 1.0
 */

@Component
public class FeignConfigComplex implements ILocalFegin {

    @Autowired
    private BasicAuth basicAuth;

    @Autowired
    private TokenAuth tokenAuth;

    @PostConstruct
    public void  init(){
        FeginManager.getInstance().add(this);
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String url = requestTemplate.url();
        if (url.contains("/jobinfo")) {
            System.out.println("------------feign调用进入xxlJob认证！");
            requestTemplate.header("Authorization", basicAuth.basicAuth());
        } else if (url.contains("/login")) {
            System.out.println("------------feign调用进入登入认证！");
            return;
        } else {
            //requestTemplate.header("Authorization", new String[]{"Bearer " + TokenUtil.getToken()});
            System.out.println("------------feign调用进入token认证！");
            //requestTemplate.header("Authorization", "Bearer 7d55cf18-95e5-421c-9508-c2b68c5d029c");
            try {
                requestTemplate.header("Authorization", tokenAuth.tokenAuth());
            } catch (FeignException e) {
                e.printStackTrace();
            }
        }
    }

}
