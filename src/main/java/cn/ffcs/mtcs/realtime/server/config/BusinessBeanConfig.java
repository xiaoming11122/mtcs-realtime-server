package cn.ffcs.mtcs.realtime.server.config;

import cn.ffcs.common.basic.util.StringUtils;
import cn.ffcs.mtcs.realtime.server.core.job.XxlJobInfo;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/23/023 20:31
 * @Version 1.0
 */
@Configuration
public class BusinessBeanConfig {

    @Value("${kafka.jaas.krb5conf.path}")
    private String KafkaJaasKrb5confPath;

    @Value("${kafka.jaas.file.path}")
    private String KafkaJaasFilePath;

    @Value("${kafka.auth.flag}")
    private String kafkaAuthFlag;

    @Value("${log.maxline}")
    private String maxline;

    @Bean
    public XxlJobInfo xxlJobInfo() {
        return new XxlJobInfo();
    }

    @Bean
    public int kafkaAuth() {
        // 对kafka进行认证
        System.out.println("==============");
        System.out.println("kafkaAuth : " + kafkaAuthFlag);
        System.out.println("KafkaJaasKrb5confPath : " + KafkaJaasKrb5confPath);
        System.out.println("KafkaJaasFilePath : " + KafkaJaasFilePath);
        if (!StringUtils.isNull(kafkaAuthFlag) && kafkaAuthFlag.equals("true")){
            System.setProperty("java.security.krb5.conf", KafkaJaasKrb5confPath);
            System.setProperty("java.security.auth.login.config", KafkaJaasFilePath);
        }
        return 0;
    }

    //数据库配置 日志每次取几行
    public String getmaxline(){
        System.out.println("maxline : " + maxline);
        return  maxline;
    }

    @Bean
    @ConfigurationProperties(prefix = "realtime.system.user")
    public UserPrincipal userPrincipal() {
        return new UserPrincipal();
    }
}
