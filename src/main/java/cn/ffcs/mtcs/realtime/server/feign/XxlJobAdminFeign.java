package cn.ffcs.mtcs.realtime.server.feign;

import cn.ffcs.mtcs.realtime.server.core.job.XxlJobInfo;
import cn.ffcs.mtcs.realtime.server.feign.fallback.XxlJobAdminFeignFallback;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/23/023 16:07
 * @Version 1.0
 */
@FeignClient(value = "MTCS-XXL-JOB-ADMIN", fallback = XxlJobAdminFeignFallback.class)
public interface XxlJobAdminFeign {

    @PostMapping("/jobinfo/add1")
    ReturnT<String> add(@RequestBody XxlJobInfo jobInfo);

    @PostMapping("/jobinfo/start1")
    ReturnT<String> start(@RequestParam(value = "id") int id);

    @PostMapping("/jobinfo/stop1")
    ReturnT<String> pause(@RequestParam(value = "id") int id);

    @PostMapping("/jobinfo/update1")
    ReturnT<String> update1(@RequestBody XxlJobInfo jobInfo);

}
