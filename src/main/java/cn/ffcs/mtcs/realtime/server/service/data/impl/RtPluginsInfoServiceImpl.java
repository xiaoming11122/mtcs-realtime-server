package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtPluginsInfo;
import cn.ffcs.mtcs.realtime.common.vo.RtPluginInfoVo;
import cn.ffcs.mtcs.realtime.server.mapper.RtPluginsInfoMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtPluginsInfoService;
import cn.ffcs.mtcs.realtime.server.util.PojoTrans;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 插件基础信息表 服务实现类
 * </p>
 *
 * @author hh
 * @since 2020-09-11
 */
@Service
public class RtPluginsInfoServiceImpl extends ServiceImpl<RtPluginsInfoMapper, RtPluginsInfo> implements IRtPluginsInfoService {

    /**
     * 根据流程id，版本查询使用的插件列表
     * @param flowId
     * @param flowVersion
     * @return
     */
    @Override
    public List<RtPluginInfoVo> listFlowPluginInfoVo(Long flowId,String flowVersion){
        List<RtPluginsInfo> pluginsInfoList = this.baseMapper.listFlowPluginInfo(flowId,flowVersion);
        List<RtPluginInfoVo> pluginInfoVoList = new ArrayList<>();
        for (RtPluginsInfo rtPluginsInfo : pluginsInfoList) {
            RtPluginInfoVo pluginVo = PojoTrans.poToVo(rtPluginsInfo,RtPluginInfoVo.class);
            pluginInfoVoList.add(pluginVo);
        }
        return pluginInfoVoList;
    }


    @Override
    public List<RtPluginsInfo> listFlowPluginInfoByTask(Long taskId, String taskVersion){
        return this.baseMapper.listFlowPluginInfoByTask(taskId, taskVersion);
    }

    //获取插件信息
    @Override
    public List<Map<String, Object>>  getPluginElements(String pluginId){
        return this.baseMapper.getPluginElements(pluginId);
    }

    @Override
    public List<Map<String, Object>> selectPlugByEngine(String engine) {
        return this.baseMapper.selectPlugByEngine(("").equals(engine)? null : engine);
    }

    //获取插件信息
    @Override
    public List<Map<String, Object>>  getPluginInstNo(Map<String, Object> param){
        return this.baseMapper.getPluginInstNo(param);
    }

    //获取一层级别信息
    @Override
    public List<Map<String, Object>>  getPluginOneLevel(Map<String, Object> param){
        return this.baseMapper.getPluginOneLevel(param);
    }

    //获取其他级别信息
    @Override
    public List<Map<String, Object>>  getPluginOtherLevel(Map<String, Object> param){
        return this.baseMapper.getPluginOtherLevel(param);
    }

    //取流式SQL脚本命令语法校验配置信息
    @Override
    public String selectPluginCheck(String pluginId){
        return this.baseMapper.selectPluginCheck(pluginId);
    }




}
