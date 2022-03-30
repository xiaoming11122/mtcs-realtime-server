package cn.ffcs.mtcs.realtime.server.service.data;

import cn.ffcs.mtcs.realtime.common.entity.RtPluginsType;
import cn.ffcs.mtcs.realtime.common.vo.RtPluginTypeVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 插件类型表 服务类
 * </p>
 *
 * @author Cbb
 * @since 2020-09-11
 */
public interface IRtPluginsTypeService extends IService<RtPluginsType> {
    public List<RtPluginTypeVo> listPluginTypeListVoByEngine(String engine);
}
