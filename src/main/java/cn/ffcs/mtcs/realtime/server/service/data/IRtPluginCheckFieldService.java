package cn.ffcs.mtcs.realtime.server.service.data;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author 47800
 */
public interface IRtPluginCheckFieldService {
    /**
     * 校验json格式
     * @param json
     * @return
     */
    String checkPlugin(List<JSONObject> json);

    /**
     * 表格转json
     * @param flowId
     * @param flowVersion
     * @return
     */
    JSONArray infoToJson(int flowId, String flowVersion);

    /**
     * 插入rt_exe_machine_env表
     * @param json
     * @return
     */
    int insertJsonToRtExeMachineEnvByJson(String json);

    /**
     * json转为kafka数据源json
     * @param json
     * @return
     */
    String jsonToKafkaJson(String json);

}
