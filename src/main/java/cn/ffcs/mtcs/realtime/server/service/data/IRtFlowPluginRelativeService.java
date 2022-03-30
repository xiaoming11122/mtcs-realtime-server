package cn.ffcs.mtcs.realtime.server.service.data;

import cn.ffcs.mtcs.realtime.common.entity.RtFlowPluginRelative;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 流程插件关系表 服务类
 * </p>
 *
 * @author Cbb
 * @since 2020-09-11
 */
public interface IRtFlowPluginRelativeService extends IService<RtFlowPluginRelative> {
    /**
     * 通过任务ID和版本获取关联的插件信息
     * @param taskId 任务id
     * @param taskVersion 任务版本
     * @return 流程和插件关联列表
     */
    public List<RtFlowPluginRelative> getFlowPluginRelativeByTaskId(Long taskId, String taskVersion);


}
