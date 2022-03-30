package cn.ffcs.mtcs.realtime.server.controller;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.common.request.TaskSaveRequest;
import cn.ffcs.mtcs.realtime.common.vo.DetailTaskVo;
import cn.ffcs.mtcs.realtime.common.vo.TaskVersionVo;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.exception.FeignException;
import cn.ffcs.mtcs.realtime.server.service.business.RealtimeDetailBusiness;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/15/015 17:24
 * @Version 1.0
 */

//@EnableResourceServer
//@EnableGlobalMethodSecurity
@RestController
@Slf4j
@Api(value = "实时详细页面接口", tags = "实时详细页面接口")
public class RealtimeDetailController {

    /**
     * 1 获取任务版本列表
     * 2 获取任务详细包含所有信息
     * 3 修改
     */

    @Autowired
    private RealtimeDetailBusiness realtimeDetailBusiness;

    /**
     * 1 获取任务版本列表
     *
     * @param taskId 任务Id
     * @return
     */
    @ApiOperation(value = "获取任务版本列表", notes = "获取任务版本列表(分页)")
    @GetMapping("/listTaskVersion")
    public RetDataMsg<TaskVersionVo> listTaskVersion(@RequestParam Long taskId) throws FeignException {
        TaskVersionVo taskVersionVO =
                realtimeDetailBusiness.getTaskVersionAll(taskId);

        RetDataMsg<TaskVersionVo> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(taskVersionVO);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");

        return retDataMsg;
    }

    /**
     * 2 获取任务详细信息
     *
     * @param taskId      任务id
     * @param taskVersion 任务版本
     * @return
     */
    @ApiOperation(value = "获取任务详细信息", notes = "获取任务详细信息")
    @GetMapping("/getDetailTaskInfo")
    public RetDataMsg<DetailTaskVo> getDetailTaskInfo(
            @RequestParam Long taskId,
            @RequestParam String taskVersion) throws FeignException {

        DetailTaskVo detailTaskVO =
                realtimeDetailBusiness.getDetailTaskInfo(taskId, taskVersion);

        RetDataMsg<DetailTaskVo> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(detailTaskVO);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");

        return retDataMsg;
    }

    /**
     * 3 修改任务元信息
     *
     * @param taskSaveRequest
     * @return
     */
    @ApiOperation(value = "修改任务元信息", notes = "修改任务元信息")
    @PostMapping("/updateTaskInfo")
    RetDataMsg<String> modifyTaskInfo(@RequestBody TaskSaveRequest taskSaveRequest) throws FeignException, DataOpsException, ExistException {
        String data = realtimeDetailBusiness.modifyTaskInfo(taskSaveRequest);

        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("");
        return retDataMsg;
    }
}
