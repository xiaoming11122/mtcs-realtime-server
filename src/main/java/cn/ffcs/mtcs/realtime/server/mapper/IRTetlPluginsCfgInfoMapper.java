package cn.ffcs.mtcs.realtime.server.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author chenhy
 */
public interface IRTetlPluginsCfgInfoMapper {
    /**
     * 查询最上级
     * @return
     */
    @Select("SELECT cfg_key_id cfgKeyId, cfg_key cfgKey, cfg_value cfgValue FROM rt_etl_plugins_cfg_info WHERE up_cfg_key_id IS NULL AND flow_id = #{flowId} AND flow_version = #{flowVersion}")
    List<Map<String, Object>> selectFatherKeyValue(int flowId, String flowVersion );

    /**
     * 查询子级，通过父级id
     * @param cfgKeyId
     * @return
     */
    @Select("SELECT cfg_key_id cfgKeyId, up_cfg_key_id upCfgKeyId, cfg_key cfgKey, cfg_value cfgValue FROM rt_etl_plugins_cfg_info WHERE up_cfg_key_id = #{cfgKeyId}  AND flow_id = #{flowId} AND flow_version = #{flowVersion}")
    List<Map<String, Object>> selectSonKeyValue(@Param("cfgKeyId") Integer cfgKeyId, @Param("flowId") int flowId, @Param("flowVersion")String flowVersion );
}
