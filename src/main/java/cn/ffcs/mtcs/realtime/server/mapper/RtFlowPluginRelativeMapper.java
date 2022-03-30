package cn.ffcs.mtcs.realtime.server.mapper;

import cn.ffcs.mtcs.realtime.common.entity.RtFlowPluginRelative;
import cn.ffcs.mtcs.realtime.common.entity.RtPluginsInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 流程插件关系表 Mapper 接口
 * </p>
 *
 * @author hh
 * @since 2020-09-11
 */
public interface RtFlowPluginRelativeMapper extends BaseMapper<RtFlowPluginRelative> {
    List<RtFlowPluginRelative> getFlowPluginRelativeByTaskId(Long taskId, String taskVersion);
}
