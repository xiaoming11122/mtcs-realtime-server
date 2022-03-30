package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.server.mapper.IRTetlPluginsCfgInfoMapper;
import cn.ffcs.mtcs.realtime.server.mapper.IRTexeMachineEnvMapper;
import cn.ffcs.mtcs.realtime.server.mapper.IRtPluginCheckFieldMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtPluginCheckFieldService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author 47800
 */
@Service
public class IRtPluginCheckFieldServiceImpl implements IRtPluginCheckFieldService {

    private final IRtPluginCheckFieldMapper mapper;

    private final IRTetlPluginsCfgInfoMapper irTetlPluginsCfgInfoMapper;

    private final IRTexeMachineEnvMapper irTexeMachineEnvMapper;

    @Autowired
    public IRtPluginCheckFieldServiceImpl(IRtPluginCheckFieldMapper mapper, IRTetlPluginsCfgInfoMapper irTetlPluginsCfgInfoMapper,
                                          IRTexeMachineEnvMapper irTexeMachineEnvMapper) {
        this.mapper = mapper;
        this.irTetlPluginsCfgInfoMapper = irTetlPluginsCfgInfoMapper;
        this.irTexeMachineEnvMapper = irTexeMachineEnvMapper;
    }


    @Override
    public String checkPlugin(List<JSONObject> json) {
        JSONObject info = (JSONObject) json.get(0).get("instBaseInfo");
        String pluginClassName = info.getString("pluginClassName");
        if (pluginClassName == null || "".equals(pluginClassName)) {
            return "pluginClassName为空！";
        }
        List<Map<String, Object>> checkList = mapper.selectByClassName(pluginClassName);
        for (Map<String, Object> map : checkList) {
            for (JSONObject jsonList : json) {
                JSONObject query = jsonList.getJSONObject("instBaseInfo");
                String fieldName = map.get("fieldName").toString();
                Object checkField = query.get(fieldName);
                String checkType = map.get("checkType").toString();
                if ("NOT_NULL".equals(checkType) && checkField == null) {
                    return fieldName + "为空！校验失败!";
                } else if ("NUMBER".equals(checkType) && !isInteger(String.valueOf(checkField))) {
                    return fieldName + "不为数值型！校验失败!";
                }
            }
        }
        return "JSON格式有效";
    }

    static class JsonObject {
        public String key;

        public String value;

        public int cfgKeyId;

        public List<JsonObject> sonList = new ArrayList<>();
    }

    @Override
    public JSONArray infoToJson(int flowId, String flowVersion) {
        List<Map<String, Object>> mapList = irTetlPluginsCfgInfoMapper.selectFatherKeyValue(flowId, flowVersion);
        List<JsonObject> jsonObjectFather = new ArrayList<>();
        if (mapList.size() > 0) {
            for (Map<String, Object> map : mapList) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.key = String.valueOf(map.get("cfgKey"));
                jsonObject.value = String.valueOf(map.get("cfgValue"));
                jsonObject.cfgKeyId = (Integer) map.get("cfgKeyId");
                jsonObjectFather.add(jsonObject);
            }
            addJsonArr(jsonObjectFather, flowId, flowVersion);
        }
        return JSONArray.parseArray(JSON.toJSONString(jsonObjectFather));
    }

    @Override
    @SuppressWarnings("all")
    public int insertJsonToRtExeMachineEnvByJson(String json) {
        int flag = 0;
        List<JSONObject> jsonArray = JSONArray.parseArray(json, JSONObject.class);
        JSONObject o = jsonArray.get(0);
        JSONArray pluginTemplate = o.getJSONArray("plugin_template");
        JSONArray machineArr = o.getJSONArray("machine");
        for (Object object : machineArr) {
            int machineId = (int) object;
            for (Object dataObj : pluginTemplate) {
                JSONObject data = (JSONObject) dataObj;
                if (data != null) {
                    Map<String, String> map = (Map<String, String>) data.get("data");
                    String type = data.getString("type");
                    Set<Map.Entry<String, String>> set = map.entrySet();
                    for (Map.Entry<String, String> s : set) {
                        if ("filecontent".equals(type)) {
                            flag = irTexeMachineEnvMapper.insertByJson(machineId, s.getKey(), s.getKey(), Base64.encodeBase64(s.getValue().getBytes(StandardCharsets.UTF_8)).toString());
                        } else {
                            flag = irTexeMachineEnvMapper.insertByJson(machineId, s.getKey(), s.getKey(), s.getValue());
                        }
                    }
                }
            }
        }
        return flag;
    }

    @SuppressWarnings("all")
    @Override
    public String jsonToKafkaJson(String json) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        String type = jsonObject.getString("type");
        JSONObject propertyJson = jsonObject.getJSONObject("property");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE TABLE");
        if ("kafka".equals(type) || type.contains("kafka")) {
            stringBuilder.append(" KafkaTable \n\r (");
            String tableName = jsonObject.getString("table_name");
            String bootstrapServers = propertyJson.getString("properties.bootstrap.servers");
            String groupId = propertyJson.getString("properties.group.id");
            String scanStartupMode = propertyJson.getString("scan.startup.mode");
            String format = propertyJson.getString("format");
            JSONArray fieldArr = jsonObject.getJSONArray("fields");
            for (Object o : fieldArr) {
                Map<String, String> fieldsMap = (Map<String, String>) o;
                stringBuilder.append("`")
                        .append(fieldsMap.get("field_name"))
                        .append("`").append(" ")
                        .append(fieldsMap.get("type").toUpperCase())
                        .append(", ");
            }
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(", "));
            stringBuilder.append(" )\n\rWITH(\n\r'connector' = 'kafka',\n\r'topic' = '##")
                    .append(tableName)
                    .append("##',\n\r'properties.bootstrap.servers' = '##")
                    .append(bootstrapServers)
                    .append("##',\n\r'properties.group.id' = '")
                    .append(groupId)
                    .append("',\n\r'scan.startup.mode' = '")
                    .append(scanStartupMode)
                    .append("',\n\r'format' = '")
                    .append(format)
                    .append("')");
        } else if ("jdbc".equals(type) || type.contains("jdbc")) {
            stringBuilder.append(" MyUserTable \n\r (");
            String url = propertyJson.getString("url");
            String mysqlTableName = propertyJson.getString("table-name");
            String userName = propertyJson.getString("username");
            String passWord = propertyJson.getString("password");
            JSONArray fieldArr = jsonObject.getJSONArray("fields");
            for (Object o : fieldArr) {
                Map<String, String> fieldsMap = (Map<String, String>) o;
                stringBuilder.append("`")
                        .append(fieldsMap.get("field_name"))
                        .append("`").append(" ")
                        .append(fieldsMap.get("type").toUpperCase())
                        .append(", ");
            }
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(", "));
            stringBuilder.append(" )\n\rWITH(\n\r'connector' = 'jdbc',\n\r'url' = '")
                    .append(url)
                    .append("',\n\r'table-name' = '")
                    .append(mysqlTableName)
                    .append("',\n\r'username' = '##")
                    .append(userName)
                    .append("##',\n\r'password' = '##")
                    .append(passWord)
                    .append("##')");
        }
        return stringBuilder.toString();
    }

    protected void addJsonArr(List<JsonObject> jsonObjectFather, int flowId, String flowVersion) {
        List<Map<String, Object>> listMap;
        if (jsonObjectFather.size() > 0) {
            for (JsonObject list : jsonObjectFather) {
                int cfgKeyId = list.cfgKeyId;
                listMap = irTetlPluginsCfgInfoMapper.selectSonKeyValue(cfgKeyId, flowId, flowVersion);
                if (listMap.size() > 0) {
                    for (Map<String, Object> m : listMap) {
                        JsonObject sonList = new JsonObject();
                        sonList.key = String.valueOf(m.get("cfgKey"));
                        sonList.value = String.valueOf(m.get("cfgValue"));
                        sonList.cfgKeyId = (Integer) m.get("cfgKeyId");
                        list.sonList.add(sonList);
                    }
                }
                addJsonArr(list.sonList, flowId, flowVersion);
            }
        }
    }


    /**
     * 判断是否为int类型
     *
     * @return flag
     */
    protected Boolean isInteger(String str) {
        boolean flag = true;
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }
}
