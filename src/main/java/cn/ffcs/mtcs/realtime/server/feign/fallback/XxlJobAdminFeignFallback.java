package cn.ffcs.mtcs.realtime.server.feign.fallback;

import cn.ffcs.mtcs.realtime.server.core.job.XxlJobInfo;
import cn.ffcs.mtcs.realtime.server.feign.XxlJobAdminFeign;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/23/023 16:07
 * @Version 1.0
 */
@Component
public class XxlJobAdminFeignFallback implements XxlJobAdminFeign {
    @Override
    public ReturnT<String> add(XxlJobInfo jobInfo) {
        return null;
    }

    @Override
    public ReturnT<String> start(int id) {
        return null;
    }

    @Override
    public ReturnT<String> pause(int id) {
        return null;
    }

    @Override
    public ReturnT<String> update1(XxlJobInfo jobInfo) {
        return null;
    }
}
