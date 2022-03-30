package cn.ffcs.mtcs;

import cn.ffcs.permission.annon.EnableAutoPermission;
import com.spring4all.swagger.EnableSwagger2Doc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 * @Description .
 * @Author Nemo
 * @Date 2019/11/27/027 10:22
 * @Version 1.0
 */
@EnableEurekaClient
@EnableFeignClients
@EnableSwagger2Doc
@MapperScan({"cn.ffcs.mtcs.**.mapper"})
@EnableCaching
@EnableAsync
@EnableAutoPermission
@SpringBootApplication
public class RealtimeServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RealtimeServerApplication.class, args);
    }
}
