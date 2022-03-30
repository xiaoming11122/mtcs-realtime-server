package cn.ffcs.mtcs.realtime.server.controller;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.common.response.RetMsg;
import cn.ffcs.mtcs.realtime.common.vo.LogTimeAxisVo;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.exception.SshExecuteException;
import cn.ffcs.mtcs.realtime.server.service.business.RealtimeLogBusiness;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/15/015 17:30
 * @Version 1.0
 */
//@EnableResourceServer
//@EnableGlobalMethodSecurity
@RestController
@Slf4j
@Api(value = "实时日志页面接口", tags = "实时日志页面接口")
public class RealtimeLogController {

    @Autowired
    private RealtimeLogBusiness realtimeLogBusiness;

    /**
     * 1 日志-时间轴
     *
     * @param taskId      任务id
     * @param taskVersion 任务版本
     * @return
     */
    @ApiOperation(value = "获取任务运行时间轴", notes = "获取任务运行时间轴(分页)")
    @GetMapping("/getLogTimeAxis")
    public RetDataMsg<List<LogTimeAxisVo>> getLogTimeAxis(@RequestParam Long taskId,
                                                          @RequestParam String taskVersion) {
        List<LogTimeAxisVo> logTimeAxisVoList =
                realtimeLogBusiness.getLogTimeAxis(taskId, taskVersion);

        RetDataMsg<List<LogTimeAxisVo>> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(logTimeAxisVoList);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 2 日志-日志内容
     *
     * @param opsId 任务执行id
     * @return
     */
    @ApiOperation(value = "获取任务运行时日志内容", notes = "获取任务运行时日志内容")
    @GetMapping("/getLogContent")
    public RetDataMsg<String> getLogContent(@RequestParam Long opsId, @RequestParam String line) throws ExistException {
        Map<String, String> resultMap = realtimeLogBusiness.getLogContent(opsId, line);
        String data = resultMap.get("msg");
        String success = resultMap.get("success");
        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        retDataMsg.setObj(success.equals("true") ? true : false);
        return retDataMsg;
    }

    /**
     * 3 日志-执行机
     *
     * @param opsId 任务执行id
     * @return
     */
    @ApiOperation(value = "获取任务运行时执行机", notes = "获取任务运行时执行机")
    @GetMapping("/getLogExeMachine")
    public RetDataMsg<String> getLogExeMachine(@RequestParam Long opsId) throws ExistException {
        String data = realtimeLogBusiness.getLogExeMachine(opsId);

        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(data);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 4 日志-执行json
     *
     * @param opsId 任务执行id
     * @return
     */
    @ApiOperation(value = "获取任务运行时执行参数", notes = "获取任务运行时执行参数")
    @GetMapping("/getLogExeParam")
    public RetDataMsg<String> getLogExeParam(@RequestParam Long opsId) throws ExistException {
        String data = realtimeLogBusiness.getLogExeParam(opsId);

        String destStr = "";

        destStr = StringEscapeUtils.unescapeJavaScript(data);
        RetDataMsg<String> retDataMsg = new RetDataMsg<>();
        retDataMsg.setData(destStr);
        retDataMsg.setSuccess(true);
        retDataMsg.setMsg("查询成功！");
        return retDataMsg;
    }

    /**
     * 7 下载
     * todo 待升级 暂时不支持下载
     *
     * @param opsId 任务执行id
     * @return
     */
    @ApiOperation(value = "下载任务运行时日志", notes = "下载任务运行时日志")
    @GetMapping("/logDownload")
    public RetMsg logDownload(@RequestParam Long opsId, HttpServletResponse response) throws SshExecuteException, ExistException {
        RetMsg r = new RetMsg();
        Boolean flag = realtimeLogBusiness.logDownload(opsId, response);
        if (!flag) {
            r.setSuccess(false);
            r.setMsg("发布任务保存失败！");
            return r;
        }
        r.setSuccess(true);
        r.setMsg("发布任务保存成功！");
        return r;
    }

   /* @GetMapping("/logDownload")
    public void downloadPluginLogDetail(HttpServletRequest request, HttpServletResponse response) {


    }*/

    /*@GetMapping("/download")
    public String fileDownLoad(HttpServletResponse response, @RequestParam Long opsId){
        File file = new File(downloadFilePath +'/'+ fileName);
        if(!file.exists()){
            return "下载文件不存在";
        }
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName );

        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
            byte[] buff = new byte[1024];
            OutputStream os  = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            log.error("{}",e);
            return "下载失败";
        }
        return "下载成功";
    }
*/
}
