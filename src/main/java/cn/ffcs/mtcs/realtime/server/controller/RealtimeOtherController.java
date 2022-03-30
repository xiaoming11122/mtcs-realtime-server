package cn.ffcs.mtcs.realtime.server.controller;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.common.vo.ParamDictionaryVo;
import cn.ffcs.mtcs.realtime.common.vo.base.ExeMachineVo;
import cn.ffcs.mtcs.realtime.server.feign.UserServerFeign;
import cn.ffcs.mtcs.realtime.server.service.business.RealtimeOtherBusiness;
import cn.ffcs.mtcs.realtime.server.vo.EngineTypeVo;
import cn.ffcs.permission.user.UserSecurity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/15/015 17:43
 * @Version 1.0
 */
//@EnableResourceServer
//@EnableGlobalMethodSecurity
@RestController
@Slf4j
@Api(value = "实时创建、修改页面中接口", tags = "实时创建、修改页面中接口")
public class RealtimeOtherController {

    /**
     * 1 引擎下拉框
     * 2 执行器复选下拉框
     * 3 处理方式下拉框
     * 4 获取应用参数字典表
     * 5 获取执行参数字典表
     */

    @Autowired
    private RealtimeOtherBusiness realtimeOtherBusiness;

    @Autowired
    private UserServerFeign userServerFeign;

    /**
     * 1 引擎下拉框
     *
     * @return
     */
    @ApiOperation(value = "引擎下拉框", notes = "引擎下拉框")
    @GetMapping("/listEngine")
    public RetDataMsg<List<EngineTypeVo>> listEngine() {
        //Map<String, String> data = realtimeOtherBusiness.listEngine();
        List<EngineTypeVo>  data = realtimeOtherBusiness.listEngineInfo();
        RetDataMsg<List<EngineTypeVo>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }


    /**
     * 2 执行器复选下拉框
     *
     * @param engine
     * @return
     */
    @ApiOperation(value = "执行器复选下拉框", notes = "执行器复选下拉框")
    @GetMapping("/listExeMachine")
    public RetDataMsg<List<ExeMachineVo>> listExeMachine(@RequestParam String engine) {
        String token = UserSecurity.getToken();
        System.out.println("------------token : " + token);

        Long nowTeam = userServerFeign.getPrincipal().getDefaultTeamId();

        List<ExeMachineVo> data =
                realtimeOtherBusiness.listExeMachine(nowTeam, engine);

        RetDataMsg<List<ExeMachineVo>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 3 处理方式下拉框
     *
     * @return
     */
    @ApiOperation(value = "处理方式下拉框", notes = "处理方式下拉框")
    @GetMapping("/listExceptionHandleType")
    public RetDataMsg<Map<String, String>> listExceptionHandleType() {
        Map<String, String> data =
                realtimeOtherBusiness.listExceptionHandleType();

        RetDataMsg<Map<String, String>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 4 获取应用参数字典表
     *
     * @param engine
     * @return
     */
    @ApiOperation(value = "获取应用参数字典表", notes = "获取应用参数字典表")
    @GetMapping("/listAppParamDic")
    public RetDataMsg<List<ParamDictionaryVo>> listAppParamDic(@RequestParam String engine) {
        List<ParamDictionaryVo> data =
                realtimeOtherBusiness.getAppParamDic(engine);

        RetDataMsg<List<ParamDictionaryVo>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 5 获取执行参数字典表
     *
     * @param engine
     * @return
     */
    @ApiOperation(value = "获取执行参数字典表", notes = "获取执行参数字典表")
    @GetMapping("/listExecuteParamDic")
    public RetDataMsg<List<ParamDictionaryVo>> listExecuteParamDic(@RequestParam String engine) {
        List<ParamDictionaryVo> data =
                realtimeOtherBusiness.getExecuteParamDic(engine);

        RetDataMsg<List<ParamDictionaryVo>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }
}
