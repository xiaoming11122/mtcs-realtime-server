package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtPluginsInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtPluginsType;
import cn.ffcs.mtcs.realtime.common.vo.RtPluginInfoVo;
import cn.ffcs.mtcs.realtime.common.vo.RtPluginTypeVo;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.mapper.RtPluginsTypeMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtPluginsInfoService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtPluginsTypeService;
import cn.ffcs.mtcs.realtime.server.util.PojoTrans;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 插件类型表 服务实现类
 * </p>
 *
 * @author hh
 * @since 2020-09-11
 */
@Service
public class RtPluginsTypeServiceImpl extends ServiceImpl<RtPluginsTypeMapper, RtPluginsType> implements IRtPluginsTypeService {

    @Autowired
    private IRtPluginsInfoService pluginsInfoService;

    /**
     * 获取所有有效的插件分类及对应分类的插件信息
     */
    @Override
    public List<RtPluginTypeVo> listPluginTypeListVoByEngine(String engine){
        List<RtPluginTypeVo> pluginTypeVoList = new ArrayList<>();
        //查询插件分类
        List<RtPluginsType> pluginsTypeList =  this.list(new QueryWrapper<RtPluginsType>()
                .lambda()
                .eq(RtPluginsType::getEngine, engine)
                .eq(RtPluginsType::getState, RecordStateEnum.StateUse.getValue())
                .orderByAsc(RtPluginsType::getSort));
        for (RtPluginsType rtPluginsType : pluginsTypeList) {
            //根据分类查询插件列表
            List<RtPluginsInfo> pluginsInfoList = pluginsInfoService.list(new QueryWrapper<RtPluginsInfo>().lambda()
                                                                            .eq(RtPluginsInfo::getState,RecordStateEnum.StateUse.getValue())
                                                                            .eq(RtPluginsInfo::getTypeId, rtPluginsType.getTypeId())
                                                                            .eq(RtPluginsInfo::getEngine,engine)
                                                                            .orderByAsc(RtPluginsInfo::getSort));
            //转换分类VO
            RtPluginTypeVo pluginTypeVo = PojoTrans.poToVo(rtPluginsType,RtPluginTypeVo.class);
            List<RtPluginInfoVo> pluginInfoVoList = new ArrayList<>();
            //遍历转换插件信息VO
            for (RtPluginsInfo rtPluginsInfo : pluginsInfoList) {
                RtPluginInfoVo pluginInfoVo = PojoTrans.poToVo(rtPluginsInfo,RtPluginInfoVo.class);
                pluginInfoVoList.add(pluginInfoVo);
            }
            pluginTypeVo.setPluginList(pluginInfoVoList);
            pluginTypeVoList.add(pluginTypeVo);
        }

        return pluginTypeVoList;
    }
}
