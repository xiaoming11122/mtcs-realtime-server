package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtTaskLastState;
import cn.ffcs.mtcs.realtime.server.mapper.RtTaskLastStateMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskLastStateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 任务最新状态表 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
@Service
public class RtTaskLastStateServiceImpl extends ServiceImpl<RtTaskLastStateMapper, RtTaskLastState> implements IRtTaskLastStateService {

}
