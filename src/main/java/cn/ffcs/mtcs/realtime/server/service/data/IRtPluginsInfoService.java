package cn.ffcs.mtcs.realtime.server.service.data;

import cn.ffcs.mtcs.realtime.common.entity.RtPluginsInfo;
import cn.ffcs.mtcs.realtime.common.vo.RtPluginInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 插件基础信息表 服务类
 * </p>
 *
 * @author Cbb
 * @since 2020-09-11
 */
public interface IRtPluginsInfoService extends IService<RtPluginsInfo> {

    public List<RtPluginInfoVo> listFlowPluginInfoVo(Long flowId, String flowVersion);

    /**
     * 通过任务信息获取对应流程的插件信息
     * @param taskId 任务ID
     * @param taskVersion 任务版本
     * @return 插件列表
     */
    public List<RtPluginsInfo> listFlowPluginInfoByTask(Long taskId, String taskVersion);

    /**
     * 获取插件信息
     * @param pluginId 插件ID
     * @return 插件配置的Map信息
     */
    List<Map<String, Object>>  getPluginElements(String pluginId);

    /**
     根据引擎获取组件的分类及分类下的插件列表信息
     入参：引擎名称
     返回：包含插件类型的插件列表信息
     */
    List<Map<String, Object>> selectPlugByEngine(String engine);

    //获取插件号
    List<Map<String, Object>>  getPluginInstNo(Map<String, Object> param);

    //获取一级信息
    List<Map<String, Object>>  getPluginOneLevel(Map<String, Object> param);

    //获取其他级别信息
    public List<Map<String, Object>>  getPluginOtherLevel(Map<String, Object> param);

    //取流式SQL脚本命令语法校验配置信息
    public String selectPluginCheck(String pluginId);
}
