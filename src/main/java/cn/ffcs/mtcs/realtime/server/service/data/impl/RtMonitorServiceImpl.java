package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;
import cn.ffcs.mtcs.realtime.server.mapper.RtMonitorMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtMonitorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 监控信息表 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
@Service
public class RtMonitorServiceImpl extends ServiceImpl<RtMonitorMapper, RtMonitor> implements IRtMonitorService {

    @Override
    public boolean updateRtMonitor(RtMonitor rtMonitor, String stateResult) {
        int monitorCount = rtMonitor.getMonitorCount() + 1;
        rtMonitor.setMonitorCount(monitorCount);
        rtMonitor.setMonitorActual(stateResult.toUpperCase());
        rtMonitor.setMonitorTime(LocalDateTime.now());
        return this.updateById(rtMonitor);
    }

}
