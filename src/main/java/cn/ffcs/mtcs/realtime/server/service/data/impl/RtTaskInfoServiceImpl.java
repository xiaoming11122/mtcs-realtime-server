package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtTaskInfo;
import cn.ffcs.mtcs.realtime.server.mapper.RtTaskInfoMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 实时任务元信息表 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
@Service
public class RtTaskInfoServiceImpl extends ServiceImpl<RtTaskInfoMapper, RtTaskInfo> implements IRtTaskInfoService {

}
