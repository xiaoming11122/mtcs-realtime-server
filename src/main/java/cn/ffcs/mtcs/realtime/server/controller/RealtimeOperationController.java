package cn.ffcs.mtcs.realtime.server.controller;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.common.response.RetMsg;
import cn.ffcs.mtcs.realtime.server.core.job.XxlJobFlume;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.feign.UserServerFeign;
import cn.ffcs.mtcs.realtime.server.service.business.RealtimeOperationBusiness;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/15/015 17:39
 * @Version 1.0
 */
//@EnableResourceServer
//@EnableGlobalMethodSecurity
@RestController
@Slf4j
@Api(value = "实时操作接口", tags = "实时操作接口")
public class RealtimeOperationController {

    @Autowired
    private UserServerFeign userServerFeign;

    @Autowired
    private RealtimeOperationBusiness realtimeOperationBusiness;

    @Autowired
    private XxlJobFlume xxlJobFlume;

    @PostMapping("/testXxlJob/{monitorId}")
    public RetDataMsg<String> testXxlJob(@PathVariable("monitorId") String monitorId) throws Exception {

        xxlJobFlume.flumeRun(monitorId);
        String data = "成功";
        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("");
        return retDataMsg;
    }


    /**
     * 1 任务上线
     *
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @return
     */
    @ApiOperation(value = "任务上线", notes = "任务上线")
    @PostMapping("/onlineOperation")
    public RetMsg onlineOperation(@RequestParam Long taskId,
                                  @RequestParam String taskVersion) throws ExistException {
        UserPrincipal principal = userServerFeign.getPrincipal();
        boolean flag = realtimeOperationBusiness.onlineOperation(principal, taskId, taskVersion);
        RetMsg r = new RetMsg();
        if (!flag) {
            r.setSuccess(false);
            r.setMsg("任务上线失败！");
            return r;
        }
        r.setSuccess(true);
        r.setMsg("任务上线成功！");
        return r;
    }

    /**
     * 2 任务下线
     *
     * @param taskId      任务ID
     * @param taskVersion 任务ID
     * @return
     */
    @ApiOperation(value = "任务下线", notes = "任务下线")
    @PostMapping("/offlineOperation")
    public RetMsg offlineOperation(@RequestParam Long taskId,
                                   @RequestParam String taskVersion) throws ExistException {
        UserPrincipal principal = userServerFeign.getPrincipal();
        boolean flag = realtimeOperationBusiness.offlineOperation(principal, taskId, taskVersion);
        RetMsg r = new RetMsg();
        if (!flag) {
            r.setSuccess(false);
            r.setMsg("任务下线失败！");
            return r;
        }
        r.setSuccess(true);
        r.setMsg("任务下线成功！");
        return r;
    }

    /**
     * 3 启动任务
     *
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @param engineName  引擎名
     * @return
     */
    @ApiOperation(value = "启动任务", notes = "启动任务")
    @PostMapping("/startOperation")
    public RetMsg startOperation(@RequestParam Long taskId,
                                 @RequestParam String taskVersion,
                                 @RequestParam String engineName) throws ExistException {
        UserPrincipal principal = userServerFeign.getPrincipal();
        boolean flag = realtimeOperationBusiness.startOperation(principal, taskId, taskVersion, engineName);
        RetMsg r = new RetMsg();
        if (!flag) {
            r.setSuccess(false);
            r.setMsg("启动任务失败！");
            return r;
        }
        r.setSuccess(true);
        r.setMsg("启动任务成功！");
        return r;
    }

    /**
     * 4 重启任务
     *
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @param engineName  引擎名
     * @return
     */
    @ApiOperation(value = "重启任务", notes = "重启任务")
    @PostMapping("/restartOperation")
    public RetDataMsg<String> restartOperation(@RequestParam Long taskId,
                                               @RequestParam String taskVersion,
                                               @RequestParam String engineName) throws ExistException {
        UserPrincipal principal = userServerFeign.getPrincipal();
        boolean flag =
                realtimeOperationBusiness.restartOperation(
                        principal,
                        taskId, taskVersion,
                        engineName);
        String data = null;
        if (flag) {
            data = "重启任务成功！";
        } else {
            data = "重启任务失败！";
        }

        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("");
        return retDataMsg;
    }

    /**
     * 5 停止任务
     *
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @param engineName  引擎名
     * @return
     */
    @ApiOperation(value = "停止任务", notes = "停止任务")
    @PostMapping("/stopOperation")
    public RetDataMsg<String> stopOperation(@RequestParam Long taskId,
                                            @RequestParam String taskVersion,
                                            @RequestParam String engineName) throws ExistException {
        UserPrincipal principal = userServerFeign.getPrincipal();
        boolean flag =
                realtimeOperationBusiness.stopOperation(
                        principal,
                        taskId, taskVersion,
                        engineName);
        String data = null;
        if (flag) {
            data = "停止任务成功！";
        } else {
            data = "停止任务失败！";
        }

        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("");
        return retDataMsg;
    }

    /*@ApiOperation(value = "停止任务", notes = "停止任务")
    @PostMapping("/flinkAppStart")
    public RetDataMsg<String> flinkAppStart(@RequestParam String rtMonitorId) throws Exception {
        UserPrincipal principal = userServerFeign.getPrincipal();
        xxlJobFlink.flinkAppStart(rtMonitorId);
        String data = null;


        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("");
        return retDataMsg;
    }*/


    /**
     * 6 更新任务
     *
     * @param taskId         任务ID
     * @param taskVersionOld 任务旧版本
     * @param taskVersionNew 任务新版本
     * @param engineName     引擎名
     * @return
     */
    @ApiOperation(value = "更新任务", notes = "更新任务")
    @PostMapping("/updateOperation")
    RetDataMsg<String> updateOperation(
            @RequestParam Long taskId,
            @RequestParam String taskVersionOld,
            @RequestParam String taskVersionNew,
            @RequestParam String engineName) throws ExistException {
        UserPrincipal principal = userServerFeign.getPrincipal();
        boolean flag =
                realtimeOperationBusiness.updateOperation(
                        principal,
                        taskId, taskVersionOld, taskVersionNew,
                        engineName);
        String data = null;
        if (flag) {
            data = "更新任务线成功！";
        } else {
            data = "更新任务失败！";
        }

        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("");
        return retDataMsg;
    }

    /**
     * 7 监控任务
     *
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @param engineName  引擎名
     * @return
     */
    @ApiOperation(value = "监听任务", notes = "监听任务")
    @PostMapping("/listenOperation")
    public RetDataMsg<String> taskOperationListen(
            @RequestParam Long taskId,
            @RequestParam String taskVersion,
            @RequestParam String engineName) {
        String data = "监控成功！";
        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("");
        return retDataMsg;
    }
}
