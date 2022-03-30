package cn.ffcs.mtcs.realtime.server.service.data;

import cn.ffcs.mtcs.realtime.common.entity.RtRetry;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 重启次数记录表 服务类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
public interface IRtRetryService extends IService<RtRetry> {

    boolean updateRtRetry(RtRetry rtRetry);
}
