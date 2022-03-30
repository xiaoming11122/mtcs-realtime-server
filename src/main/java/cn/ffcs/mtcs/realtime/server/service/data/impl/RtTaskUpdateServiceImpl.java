package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtTaskUpdate;
import cn.ffcs.mtcs.realtime.server.mapper.RtTaskUpdateMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskUpdateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 实时任务更新标记表 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
@Service
public class RtTaskUpdateServiceImpl extends ServiceImpl<RtTaskUpdateMapper, RtTaskUpdate> implements IRtTaskUpdateService {

}
