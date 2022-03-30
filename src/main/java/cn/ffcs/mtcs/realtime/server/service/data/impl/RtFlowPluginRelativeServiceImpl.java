package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtFlowPluginRelative;
import cn.ffcs.mtcs.realtime.server.mapper.RtFlowPluginRelativeMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtFlowPluginRelativeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 流程插件关系表 服务实现类
 * </p>
 *
 * @author hh
 * @since 2020-09-11
 */
@Service
public class RtFlowPluginRelativeServiceImpl extends ServiceImpl<RtFlowPluginRelativeMapper, RtFlowPluginRelative> implements IRtFlowPluginRelativeService {
    @Override
    public List<RtFlowPluginRelative> getFlowPluginRelativeByTaskId(Long taskId, String taskVersion) {
        return this.baseMapper.getFlowPluginRelativeByTaskId(taskId, taskVersion);
    }

}
