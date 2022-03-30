package cn.ffcs.mtcs.realtime.server.mapper;

import cn.ffcs.mtcs.realtime.common.entity.RtPluginsInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import feign.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 插件基础信息表 Mapper 接口
 * </p>
 *
 * @author hh
 * @since 2020-09-11
 */
public interface RtPluginsInfoMapper extends BaseMapper<RtPluginsInfo> {

    public List<RtPluginsInfo> listFlowPluginInfo(Long flowId,String flowVersion);

    public List<RtPluginsInfo> listFlowPluginInfoByTask(Long taskId,String taskVersion);


    //获取插件信息
    List<Map<String, Object>>  getPluginElements(String pluginId);

    List<Map<String, Object>> selectPlugByEngine(String engine);

    List<Map<String, Object>> getPluginInstNo(Map<String, Object> param);

    List<Map<String, Object>> getPluginOneLevel(Map<String, Object> param);

    List<Map<String, Object>> getPluginOtherLevel(Map<String, Object> param);

    String selectPluginCheck(String pluginId);
}
