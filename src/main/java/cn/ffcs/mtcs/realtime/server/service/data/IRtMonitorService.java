package cn.ffcs.mtcs.realtime.server.service.data;

import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 监控信息表 服务类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
public interface IRtMonitorService extends IService<RtMonitor> {

    boolean updateRtMonitor(RtMonitor rtMonitor, String stateResult);
}
