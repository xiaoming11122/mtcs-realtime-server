package cn.ffcs.mtcs.realtime.server.service.business;

import cn.ffcs.mtcs.common.response.RetMsg;
import cn.ffcs.mtcs.realtime.common.entity.RtExeInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.realtime.common.entity.RtTaskLastState;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.service.data.IRtExeInfoService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsAttachmentService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskLastStateService;
import cn.ffcs.mtcs.realtime.server.util.RealTimeSsh;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.ffcs.mtcs.realtime.server.constants.CommonConstants.*;

@Service
@Slf4j
public class AsyncRealtimeOperation {


    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;

    @Autowired
    private IRtTaskLastStateService taskLastStateService;


    @Autowired
    IRtExeInfoService rtExeInfoService;

    @Value("${exe.count.default}")
    private int EXE_COUNT_DEFAULT;

    /**
     * 从日志信息中获取jobId
     * @param logcontent 日志内容
     * @return jobid
     */
    private String getJobIdFromLog(String logcontent) {
        String jobId = "";
        String pattern = "(Job has been submitted with JobID )(\\w*)";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(logcontent);
        if (m.find()) {
            jobId = m.group(2);
        }
        return jobId;
    }

    private String getApplicationIdFromLog(String logcontent) {
        String applicationId = "";
        String pattern = "(Submitted application )(\\w*)";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(logcontent);
        if (m.find()) {
            applicationId = m.group(2);
        }
        return applicationId;
    }

    private String getJobManager(String logcontent) {
        String jobManager = "";
        String pattern = "(Found Web Interface )(\\S*)";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(logcontent);
        if (m.find()) {
            jobManager = m.group(2);
        }
        return jobManager;
    }

    private String getSavePointPathInfo(String logcontent) {
        String savePointInfo = "";
        String pattern = "(Savepoint completed. Path: )(\\S*)";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(logcontent);
        if (m.find()) {
            savePointInfo = m.group(2);
        }
        return savePointInfo;
    }

    private RtTaskLastState getTaskLastState(Long taskId, String taskVersion) {
        RtTaskLastState rtTaskLastState =
                taskLastStateService.getOne(
                        Wrappers.<RtTaskLastState>lambdaQuery()
                                .eq(RtTaskLastState::getTaskId, taskId)
                                .eq(RtTaskLastState::getTaskVersion, taskVersion)
                                .eq(RtTaskLastState::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        return rtTaskLastState;
    }

    @Async("taskExecutor")
    public CompletableFuture<Boolean> start(SshParamRequest sshParamRequest, RtOpsAttachment rtOpsAttachment) {
        /**
         * 1 封装SSH服务调用的参数
         * 2 直接shell调用
         */
        boolean execResult = false;

        try {
            RetMsg retMsg = RealTimeSsh.executeReturnMsg(sshParamRequest, EXE_COUNT_DEFAULT);
            RtOpsAttachment rtOpsAttachmentR;
            //d
            int runNum = 0;
            while (true) {
                try {
                    //总共在3分钟内处理
                    Thread.sleep(3 * 1000);
                    runNum++;
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                rtOpsAttachmentR = opsAttachmentService.getById(rtOpsAttachment.getId());
                //查看应用的任务执行是否提交完成，如果完成 exeId 会大等于0
                if (rtOpsAttachmentR.getExeId() != null && rtOpsAttachmentR.getExeId() >= 0) {
                    if (StringUtils.isNotEmpty(rtOpsAttachment.getExeLogFile())) {
                        //从日志中获取appid及jobid信息
                        String cmd = "cat " + rtOpsAttachment.getExeLogFile();
                        RetMsg retMsgCmd = RealTimeSsh.sshExeCmd(sshParamRequest.getExeInfoList(), cmd, EXE_COUNT_DEFAULT);

                        execResult = retMsgCmd.getSuccess();
                        String execMsg = retMsgCmd.getMsg();
                        String jobId = getJobIdFromLog(execMsg);
                        String applicationId = getApplicationIdFromLog(execMsg);
                        String jobManager = getJobManager(execMsg);
                        log.debug("rtOpsAttachmentId:{},execResult:{}", rtOpsAttachment.getId(),
                                execResult);
                        if (execResult) {
                            Map<String, String> execInfoMap = new HashMap<>();
                            execInfoMap.put(EXEC_RESULT_APP_ID_KEY, applicationId);
                            execInfoMap.put(EXEC_RESULT_JOB_ID_KEY, jobId);
                            execInfoMap.put(EXEC_RESULT_WEB_URL_KEY, jobManager);
                            log.debug("rtOpsAttachmentId:{},execInfoMap:{}", rtOpsAttachment.getId(),
                                    execInfoMap);
                            String execInfoMapJson = JSON.toJSONString(execInfoMap);
                            rtOpsAttachmentR.setExeResult(execInfoMapJson);
                            opsAttachmentService.saveOrUpdate(rtOpsAttachmentR);
                            RtTaskLastState rtTaskLastState = getTaskLastState(rtOpsAttachmentR.getTaskId(),
                                    rtOpsAttachmentR.getTaskVersion());
                            rtTaskLastState.setExeResult(execInfoMapJson);
                            taskLastStateService.updateById(rtTaskLastState);

                            RtExeInfo selectRtExeInfo = rtExeInfoService.getById(rtOpsAttachmentR.getExeId());
                            selectRtExeInfo.setAppId(applicationId);
                            selectRtExeInfo.setExeResultParam(JSON.toJSONString(execInfoMap));
                            rtExeInfoService.saveOrUpdate(selectRtExeInfo);
                        }
                    }
                    break;

                } else if (runNum >= 60) {
                    log.info("rtOpsAttachment id:{},获取停止命令未执行完成，检测达到{}次！退出",
                            rtOpsAttachment.getId(), runNum);
                    break;

                }

            }


            return CompletableFuture.completedFuture(execResult);
        } catch (DataOpsException e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(false);
    }


    @Async("taskExecutor")
    public CompletableFuture<Boolean> stop(SshParamRequest sshParamRequest, RtOpsAttachment rtOpsAttachment) {
        /**
         * 1 封装SSH服务调用的参数
         * 2 直接shell调用
         */
        boolean execResult = false;

        try {
            RetMsg retMsg = RealTimeSsh.executeReturnMsg(sshParamRequest, EXE_COUNT_DEFAULT);
            int runNum = 0;
            while (true) {
                try {
                    //总共在2分钟内处理
                    Thread.sleep(2 * 1000);
                    runNum++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (StringUtils.isNotEmpty(rtOpsAttachment.getExeLogFile())) {
                    //从日志中获取appid及jobid信息
                    String cmd = "cat " + rtOpsAttachment.getExeLogFile();
                    RetMsg retMsgCmd = RealTimeSsh.sshExeCmd(sshParamRequest.getExeInfoList(), cmd, EXE_COUNT_DEFAULT);

                    execResult = retMsgCmd.getSuccess();
                    String execMsg = retMsgCmd.getMsg();
                    String savePointPathInfo = getSavePointPathInfo(execMsg);

                    log.debug("rtOpsAttachmentId:{},execResult:{}", rtOpsAttachment.getId(),
                            execResult);
                    if (execResult && StringUtils.isNotEmpty(savePointPathInfo)) {
                        RtTaskLastState rtTaskLastState = getTaskLastState(rtOpsAttachment.getTaskId(),
                                rtOpsAttachment.getTaskVersion());
                        rtTaskLastState.setSavepoint(savePointPathInfo);
                        taskLastStateService.updateById(rtTaskLastState);
                        break;
                    }


                }
                if (runNum >= 60) {
                    log.info("rtOpsAttachment id:{},获取提交执行状态长时候未执行完成，检测达到{}次！退出",
                            rtOpsAttachment.getId(), runNum);
                    break;

                }

            }


            return CompletableFuture.completedFuture(execResult);
        } catch (DataOpsException e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(false);
    }
}
