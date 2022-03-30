package cn.ffcs.mtcs.realtime.server.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author 47800
 */
public interface IRtPluginCheckFieldMapper {

    /**
     * 通过className查询
     */
    @Select(" SELECT" +
            " A.plugin_class_name pluginClassName," +
            " A.version," +
            " A.field_name fieldName," +
            " A.engine," +
            " B.check_type checkType" +
            " FROM" +
            " rt_plugin_check_field A" +
            " INNER JOIN rt_plugin_check_field_rule B ON A.id = B.check_field_id " +
    "WHERE A.plugin_class_name = #{pluginClassName}")
    List<Map<String, Object>> selectByClassName(String pluginClassName);

}
