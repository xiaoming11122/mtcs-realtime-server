package cn.ffcs.mtcs.realtime.server.controller;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.common.vo.RtPluginTypeVo;
import cn.ffcs.mtcs.realtime.server.service.data.IRtPluginsTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@EnableResourceServer
//@EnableGlobalMethodSecurity
@RestController
@Slf4j
@Api(value = "插件相关接口", tags = "插件相关接口")
public class RealtimePluginController {

    @Autowired
    private IRtPluginsTypeService pluginsTypeService;

    /**
     * 获取插件分类信息(包含插件信息)
     *
     */
    @ApiOperation(value = "获取插件分类信息(包含插件信息)", notes = "获取插件分类信息(包含插件信息)")
    @GetMapping("/listPluginTypeInfo")
    public RetDataMsg<List<RtPluginTypeVo>> listPluginTypeInfo(@RequestParam(required = false) String engine){

        List<RtPluginTypeVo> pluginTypeVoList = pluginsTypeService.listPluginTypeListVoByEngine(engine);

        RetDataMsg<List<RtPluginTypeVo>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(pluginTypeVoList);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");


        return retDataMsg;
    }
}
