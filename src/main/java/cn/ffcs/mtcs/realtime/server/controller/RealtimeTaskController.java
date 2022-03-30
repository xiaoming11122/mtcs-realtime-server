package cn.ffcs.mtcs.realtime.server.controller;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.common.response.RetMsg;
import cn.ffcs.mtcs.realtime.common.request.TaskAuditRequest;
import cn.ffcs.mtcs.realtime.common.request.TaskCallBackRequest;
import cn.ffcs.mtcs.realtime.common.request.TaskSaveRequest;
import cn.ffcs.mtcs.realtime.common.vo.TaskInfoOfflineVo;
import cn.ffcs.mtcs.realtime.common.vo.TaskInfoOnlineVo;
import cn.ffcs.mtcs.realtime.common.vo.TaskInfoOtherVo;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.exception.FeignException;
import cn.ffcs.mtcs.realtime.server.service.business.RealtimeTaskBusiness;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/15/015 17:45
 * @Version 1.0
 */
//@EnableResourceServer
//@EnableGlobalMethodSecurity
@RestController
@Slf4j
@Api(value = "实时主页面中接口", tags = "实时主页面中接口")
public class RealtimeTaskController {

    /**
     * 1 新增任务
     * <p>
     * 2 在用-任务列表
     * <p>
     * 3 下线-任务列表
     * <p>
     * 4 其他-任务列表
     */

    @Autowired
    private RealtimeTaskBusiness realtimeTaskBusiness;

    /**
     * 1 新增任务
     * 保存任务元信息，
     * 其中包含任务基本信息、流程信息、执行信息
     * 该保存的是开发版本
     *
     * @param taskSaveRequest 任务元信息
     * @return
     */
    @ApiOperation(value = "新增任务", notes = "新增任务")
    @PostMapping("/saveTaskInfo")
    public RetMsg saveTaskInfo(
            @RequestBody TaskSaveRequest taskSaveRequest) throws ExistException, FeignException, DataOpsException {
        Boolean flag = realtimeTaskBusiness.saveTaskAll(taskSaveRequest);
        RetMsg r = new RetMsg();
        if (!flag) {
            r.setSuccess(false);
            r.setMsg("保存任务失败！");
            return r;
        }
        r.setSuccess(true);
        r.setMsg("保存任务成功！");
        return r;
    }

    /**
     * 审核任务
     * 状态为待审核
     *
     * @param taskSaveRequest 任务元信息
     * @return
     * @throws ExistException
     * @throws FeignException
     */
    @ApiOperation(value = "审核任务", notes = "审核任务")
    @PostMapping("/auditTask")
    public RetMsg auditTask(
            @RequestBody TaskSaveRequest taskSaveRequest) throws ExistException, FeignException, DataOpsException {
        RetMsg r = new RetMsg();
        Boolean flag = realtimeTaskBusiness.auditTask(taskSaveRequest);
        if (!flag) {
            r.setSuccess(false);
            r.setMsg("审核任务保存失败");
            return r;
        }
        r.setSuccess(true);
        r.setMsg("审核任务保存成功");
        return r;
    }

    /**
     * 审核任务，回滚
     * 状态为待审核
     *
     * @param
     * @return
     * @throws ExistException
     * @throws FeignException
     * @throws DataOpsException
     */
    @ApiOperation(value = "审核任务", notes = "审核任务")
    @PostMapping("/auditTaskCallback")
    public RetMsg auditTaskCallback(
            @RequestBody TaskCallBackRequest taskCallBackRequest) throws DataOpsException, ExistException, FeignException {
        RetMsg r = new RetMsg();
        Boolean flag = realtimeTaskBusiness.auditTaskCallback(
                taskCallBackRequest.getTaskId(),
                taskCallBackRequest.getTaskVersion(),
                taskCallBackRequest.getAuditId());
        if (!flag) {
            r.setSuccess(false);
            r.setMsg("审核任务保存失败！");
            return r;
        }
        r.setSuccess(true);
        r.setMsg("审核任务保存成功！");
        return r;
    }

    /**
     * 发布任务
     * 状态为发布
     *
     * @param taskAuditRequest
     * @return
     * @throws ExistException
     * @throws FeignException
     */
    @ApiOperation(value = "发布任务", notes = "发布任务")
    @PostMapping("/releaseTask")
    public RetMsg releaseTask(@RequestBody TaskAuditRequest taskAuditRequest) throws ExistException, FeignException {
        RetMsg r = new RetMsg();
        Boolean flag = realtimeTaskBusiness.releaseTask(
                taskAuditRequest.getTaskId(),
                taskAuditRequest.getTaskVersion());
        if (!flag) {
            r.setSuccess(false);
            r.setMsg("发布任务保存失败！");
            return r;
        }
        r.setSuccess(true);
        r.setMsg("发布任务保存成功！");
        return r;
    }

    /**
     * 检测任务名是否重复
     *
     * @param taskName
     * @return
     * @throws ExistException
     */
    @ApiOperation(value = "检测任务名是否重复", notes = "检测任务名是否重复")
    @PostMapping("/checkSameName")
    public RetDataMsg<Boolean> checkSameName(@RequestParam String taskName) throws ExistException {
        Boolean data = realtimeTaskBusiness.checkSameName(taskName);

        RetDataMsg<Boolean> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("检测任务名成功！");
        return retDataMsg;
    }


    /**
     * 2 在用-任务列表
     * 列出在线的任务
     *
     * @param taskName 任务英文名或者是任务英文名
     * @param current  页数
     * @param size     每页包含的记录数
     * @return
     */
    @ApiOperation(value = "在用-任务列表", notes = "在用-任务列表")
    @GetMapping("/listOnlineTask")
    public RetDataMsg<IPage<TaskInfoOnlineVo>> listOnlineTask(
            @RequestParam(required = false) String taskName,
            @RequestParam Long current, @RequestParam Integer size) throws FeignException, ExistException {

        //当前页数、每页数量
        IPage<TaskInfoOnlineVo> data;
        if (taskName == null) {
            data = realtimeTaskBusiness.listTaskInfoOnline(current, size);
        } else {
            // 这种情况下，默认
            data = realtimeTaskBusiness.listTaskInfoOnlineByName(taskName, current, size);
        }

        RetDataMsg<IPage<TaskInfoOnlineVo>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 3 下线-任务列表
     * 列出下线的任务
     *
     * @param taskName 任务英文名或者是任务英文名
     * @param current  页数
     * @param size     每页包含的记录数
     * @return
     */
    @ApiOperation(value = "下线-任务列表", notes = "下线-任务列表")
    @GetMapping("/listOfflineTask")
    public RetDataMsg<IPage<TaskInfoOfflineVo>> listOfflineTask(
            @RequestParam(required = false) String taskName,
            @RequestParam Long current, @RequestParam Integer size) throws ExistException {

        IPage<TaskInfoOfflineVo> data;
        if (taskName == null) {
            data = realtimeTaskBusiness.listTaskInfoOffline(current, size);
        } else {
            data = realtimeTaskBusiness.listTaskInfoOfflineByName(taskName, current, size);
        }

        RetDataMsg<IPage<TaskInfoOfflineVo>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 4 其他-任务列表
     * 列出其他的任务
     *
     * @param taskName 任务英文名或者是任务英文名
     * @param current  页数
     * @param size     每页包含的记录数
     * @return
     */
    @ApiOperation(value = "其他-任务列表", notes = "其他-任务列表")
    @GetMapping("/listOtherTask")
    public RetDataMsg<IPage<TaskInfoOtherVo>> listOtherTask(
            @RequestParam(required = false) String taskName,
            @RequestParam Long current, @RequestParam Integer size) throws FeignException, ExistException {

        IPage<TaskInfoOtherVo> data;
        if (taskName == null) {
            data = realtimeTaskBusiness.listTaskInfoOther(current, size);
        } else {
            data = realtimeTaskBusiness.listTaskInfoOtherByName(taskName, current, size);
        }

        RetDataMsg<IPage<TaskInfoOtherVo>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 根据插件ID获取插件信息
     *
     * @param pluginId 插件id
     * @return
     */
    @ApiOperation(value = "根据插件ID获取插件信息", notes = "根据插件ID获取插件信息")
    @GetMapping("/getPluginElements")
    public RetDataMsg<JSONObject> getPluginElements(@RequestParam String pluginId) throws ExistException {
        JSONObject data = realtimeTaskBusiness.getPluginElements(pluginId);
        RetDataMsg<JSONObject> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 4 引擎组件下的分类信息
     *
     * @param engine 引擎名
     * @return
     */
    @ApiOperation(value = "引擎组件下的分类信息", notes = "引擎组件下的分类信息")
    @PostMapping("/plugInfo")
    public RetDataMsg<List<Map<String, Object>>> getPlug(
            @RequestParam(required = false) String engine) {
        RetDataMsg<List<Map<String, Object>>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(realtimeTaskBusiness.getPlugInfo(engine));
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 根据实例ID、版本号获取插件配置信息
     *
     * @param flowId      实例ID
     * @param flowVersion 版本号
     * @return
     */
    @ApiOperation(value = "根据实例ID、版本号获取插件配置信息", notes = "根据实例ID、版本号获取插件配置信息")
    @GetMapping("/getPluginCfgInfo")
    public RetDataMsg<JSONObject> getPluginCfgInfo(@RequestParam String flowId, @RequestParam String flowVersion) throws ExistException {
        JSONObject data = realtimeTaskBusiness.getPluginCfgInfo(flowId, flowVersion);
        RetDataMsg<JSONObject> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }


    /**
     * 流程插件链接顺序有效性校验
     *
     * @param jsonStr 节点数组
     * @return
     * @throws ExistException
     * @throws FeignException
     */
    @ApiOperation(value = "流程插件链接顺序有效性校验", notes = "流程插件链接顺序有效性校验")
    @PostMapping("/checkPluginNoteSort")
    public RetDataMsg<String> checkPluginNoteSort(@RequestBody String jsonStr) throws ExistException, FeignException, DataOpsException {
        String data = realtimeTaskBusiness.checkPluginNoteSort(jsonStr);
        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("获取流程插件链接顺序有效性校验成功！");
        retDataMsg.setObj("校验通过！".equals(data) ? true : false);
        return retDataMsg;
    }


}
