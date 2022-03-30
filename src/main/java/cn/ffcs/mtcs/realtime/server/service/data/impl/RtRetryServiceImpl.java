package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtRetry;
import cn.ffcs.mtcs.realtime.server.mapper.RtRetryMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtRetryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 重启次数记录表 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
@Service
public class RtRetryServiceImpl extends ServiceImpl<RtRetryMapper, RtRetry> implements IRtRetryService {

    @Override
    public boolean updateRtRetry(RtRetry rtRetry) {
        int failCount = rtRetry.getFailCount() + 1;
        rtRetry.setFailCount(failCount);
        rtRetry.setCrtTime(LocalDateTime.now());
        return this.updateById(rtRetry);
    }
}
