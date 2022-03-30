package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtAppState;
import cn.ffcs.mtcs.realtime.server.mapper.RtAppStateMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtAppStateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 任务状态信息 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
@Service
public class RtAppStateServiceImpl extends ServiceImpl<RtAppStateMapper, RtAppState> implements IRtAppStateService {

}
