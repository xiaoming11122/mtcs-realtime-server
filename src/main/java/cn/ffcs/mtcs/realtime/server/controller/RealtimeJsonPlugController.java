package cn.ffcs.mtcs.realtime.server.controller;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.server.service.data.IRtPluginCheckFieldService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 47800
 */
//@EnableResourceServer
//@EnableGlobalMethodSecurity
@RestController
@Slf4j
@Api(value = "json格式校验", tags = "json格式校验")
public class RealtimeJsonPlugController {

    private final IRtPluginCheckFieldService service;

    @Autowired
    public RealtimeJsonPlugController (@Qualifier("IRtPluginCheckFieldServiceImpl") IRtPluginCheckFieldService service) {
        this.service = service;
    }

    @ApiOperation(value = "json格式流式插件采集值有效性开发", notes = "json格式流式插件采集值有效性开发")
    @PostMapping("/jsonPluginCheck")
    public RetDataMsg<String> jsonPluginCheck(@RequestBody String json){
        List<JSONObject> obj = JSONObject.parseArray(json, JSONObject.class);
        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(service.checkPlugin(obj));
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");

        return retDataMsg;
    }

    @ApiOperation(value = "rt_etl_plugins_cfg_info; 生成json信息", notes = "rt_etl_plugins_cfg_info; 生成json信息")
    @PostMapping("/infoToJson")
    public RetDataMsg<JSONArray> intoToJson(int flowId, String flowVersion){
        RetDataMsg<JSONArray> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(service.infoToJson(flowId, flowVersion));
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");

        return retDataMsg;
    }

    @ApiOperation(value = "json插入rt_exe_machine_env", notes = "json插入rt_exe_machine_env")
    @PostMapping("/insertJsonToRtExeMachineEnv")
    public RetDataMsg<Integer> insertJsonToRtExeMachineEnv(@RequestBody String json) {
        RetDataMsg<Integer> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(service.insertJsonToRtExeMachineEnvByJson(json));
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("插入成功！");

        return retDataMsg;
    }

    @ApiOperation(value = "json转为kafka数据源json", notes = "json转为kafka数据源json")
    @PostMapping("/jsonToKafkaJson")
    public RetDataMsg<String> jsonToKafkaJson(@RequestBody String json) {
        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(service.jsonToKafkaJson(json));
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("插入成功！");

        return retDataMsg;
    }
}
