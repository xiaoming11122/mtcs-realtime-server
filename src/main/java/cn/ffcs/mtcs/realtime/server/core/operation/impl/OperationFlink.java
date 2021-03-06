package cn.ffcs.mtcs.realtime.server.core.operation.impl;

import cn.ffcs.mtcs.common.response.RetMsg;
import cn.ffcs.mtcs.realtime.common.entity.RtExeInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.KafkaContents;
import cn.ffcs.mtcs.realtime.server.core.kafka.KafkaSender;
import cn.ffcs.mtcs.realtime.server.core.operation.IOperation;
import cn.ffcs.mtcs.realtime.server.core.operation.OperationFactory;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.service.business.AsyncRealtimeOperation;
import cn.ffcs.mtcs.realtime.server.service.data.IRtExeInfoService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsAttachmentService;
import cn.ffcs.mtcs.realtime.server.util.RealTimeSsh;
import cn.ffcs.mtcs.ssh.common.entity.SshExeInfo;
import cn.ffcs.mtcs.ssh.common.request.ExeInfo;
import cn.ffcs.mtcs.ssh.common.request.SshParamMsgRequest;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.ffcs.mtcs.realtime.server.constants.CommonConstants.*;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/9/009 14:55
 * @Version 1.0
 */
@Component
@Slf4j
public class OperationFlink implements IOperation, InitializingBean {

    @Autowired
    private KafkaSender kafkaSender;

    @Value("${spring.application.name}")
    private String AppServerName;

    @Value("receive.ssh.server.topic")
    private String receiveSshServerTopic;

    @Value("${execute.fail.state}")
    private String EXECUTE_FAIL_STATE;

    @Value("${exe.count.default}")
    private int EXE_COUNT_DEFAULT;

    @Autowired
    IRtExeInfoService rtExeInfoService;

    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;


    @Autowired
    private AsyncRealtimeOperation asyncRealtimeOperation;


    @Override
    public void afterPropertiesSet() throws Exception {
        OperationFactory.registerOperation(EngineTypeEnum.Flink.getValue(), this);
        OperationFactory.registerOperation(EngineTypeEnum.FlinkSql.getValue(), this);
    }

    private String getJobIdFromLog(String logcontent) {
        String jobId = "";
        String pattern = "(Job has been submitted with JobID )(\\w*)";
        // ?????? Pattern ??????
        Pattern r = Pattern.compile(pattern);
        // ???????????? matcher ??????
        Matcher m = r.matcher(logcontent);
        if (m.find()) {
            jobId = m.group(2);
        }
        return jobId;
    }

    private String getApplicationIdFromLog(String logcontent) {
        String applicationId = "";
        String pattern = "(Submitted application )(\\w*)";
        // ?????? Pattern ??????
        Pattern r = Pattern.compile(pattern);
        // ???????????? matcher ??????
        Matcher m = r.matcher(logcontent);
        if (m.find()) {
            applicationId = m.group(2);
        }
        return applicationId;
    }

    private String getJobManager(String logcontent) {
        String jobManager = "";
        String pattern = "(Found Web Interface )(\\S*)";
        // ?????? Pattern ??????
        Pattern r = Pattern.compile(pattern);
        // ???????????? matcher ??????
        Matcher m = r.matcher(logcontent);
        if (m.find()) {
            jobManager = m.group(2);
        }
        return jobManager;
    }

    /**
     * ??????
     *
     * @param principal
     * @param rtOpsAttachment
     * @return
     */
    @Override
    public boolean start(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        /**
         * 1 ??????SSH?????????????????????
         * 2 ??????shell??????
         */
        boolean execResult = false;
        log.debug("OpsCommand:{}", rtOpsAttachment.getOpsCommand());
        SshParamMsgRequest sshParamMsgRequest =
                getSshParamMsgRequest(principal, rtOpsAttachment);
        //added by wuq at 20210324 ?????????????????????ssh??????
        SshParamRequest sshParamRequest = new SshParamRequest();
        sshParamRequest.setExeServer(sshParamMsgRequest.getExeServer());
        sshParamRequest.setExeUser(sshParamMsgRequest.getExeUser());
        sshParamRequest.setExeInfoList(sshParamMsgRequest.getExeInfoList());
        sshParamRequest.setExeParams(sshParamMsgRequest.getExeParams());
        log.debug("rtOpsAttachment:{},?????????????????????", rtOpsAttachment.getId());
        asyncRealtimeOperation.start(sshParamRequest,rtOpsAttachment);
        log.debug("rtOpsAttachment:{}????????????", rtOpsAttachment.getId());
        return true;
        /*try {
            RetMsg retMsg = RealTimeSsh.executeReturnMsg(sshParamRequest, EXE_COUNT_DEFAULT);
            RtOpsAttachment rtOpsAttachmentR;
            //d
            int runNum = 0;
            while (true) {
                try {
                    //?????????2???????????????
                    Thread.sleep(2 * 1000);
                    runNum ++;
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                rtOpsAttachmentR = opsAttachmentService.getById(rtOpsAttachment.getId());
                //???????????????????????????????????????????????????????????? exeId ????????????0
                if (rtOpsAttachmentR.getExeId() != null && rtOpsAttachmentR.getExeId() >= 0) {
                    if (StringUtils.isNotEmpty(rtOpsAttachment.getExeLogFile())) {
                        //??????????????????appid???jobid??????
                        String cmd = "cat " + rtOpsAttachment.getExeLogFile();
                        RetMsg retMsgCmd = RealTimeSsh.sshExeCmd(sshParamRequest.getExeInfoList(), cmd, EXE_COUNT_DEFAULT);

                        execResult = retMsgCmd.getSuccess();
                        String execMsg = retMsgCmd.getMsg();
                        log.debug("execMsg2:{}", execMsg);
                        String jobId = getJobIdFromLog(execMsg);
                        String applicationId = getApplicationIdFromLog(execMsg);
                        String jobManager = getJobManager(execMsg);
                        log.debug("rtOpsAttachmentId:{},execResult:{}", rtOpsAttachment.getId(),
                                execResult);
                        if (execResult) {

                            //RtOpsAttachment rtOpsAttachmentR = opsAttachmentService.getById(rtOpsAttachment.getId());

                            Map<String, String> execInfoMap = new HashMap<>();
                            execInfoMap.put(EXEC_RESULT_APP_ID_KEY, applicationId);
                            execInfoMap.put(EXEC_RESULT_JOB_ID_KEY, jobId);
                            execInfoMap.put(EXEC_RESULT_WEB_URL_KEY, jobManager);
                            log.debug("rtOpsAttachmentId:{},execInfoMap:{}", rtOpsAttachment.getId(),
                                    execInfoMap);
                            rtOpsAttachmentR.setExeResult(JSON.toJSONString(execInfoMap));
                            opsAttachmentService.saveOrUpdate(rtOpsAttachmentR);
                            RtExeInfo selectRtExeInfo = rtExeInfoService.getById(rtOpsAttachmentR.getExeId());
                            selectRtExeInfo.setAppId(applicationId);
                            selectRtExeInfo.setExeResultParam(JSON.toJSONString(execInfoMap));
                            rtExeInfoService.saveOrUpdate(selectRtExeInfo);

               *//* //?????????????????????????????????appid???jobid,???????????????
                log.debug("rtOpsAttachment.exeId:{}", rtOpsAttachmentR.getExeId());
                if (rtOpsAttachmentR.getExeId() != null && rtOpsAttachmentR.getExeId() > 0) {
                    log.debug("??????????????????????????????exeid:{}",rtOpsAttachmentR.getExeId()  );
                    RtExeInfo selectRtExeInfo = rtExeInfoService.getById(rtOpsAttachmentR.getExeId());
                    Map<String, String> execInfoMap = new HashMap<>();
                    execInfoMap.put(APPLICATIONID_KEY, applicationId);
                    execInfoMap.put(FLINK_JOBID_KEY, jobId);
                    execInfoMap.put(FLINK_JOBMANAGER_KEY,jobManager);
                    log.info("execInfoMap:" + execInfoMap);
                    selectRtExeInfo.setAppId(applicationId);
                    selectRtExeInfo.setExeResultParam(JSON.toJSONString(execInfoMap));
                    rtExeInfoService.saveOrUpdate(selectRtExeInfo);
                }*//*

                        }
                    }
                    break;

                } else  if (runNum >= 60) {
                        log.info("rtOpsAttachment id:{},???????????????????????????????????????????????????????????????{}????????????",
                                rtOpsAttachment.getId(), runNum);
                        break;

                }

            }


            return execResult;
        } catch (DataOpsException e) {
            e.printStackTrace();
        }

        return false;*/
    }

    @Override
    public boolean restart(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        /**
         * 1 ??????SSH?????????????????????
         * 2 ?????????????????????????????????kafka???
         */

        /*SshParamMsgRequest sshParamMsgRequest =
                getSshParamMsgRequest(principal, rtOpsAttachment);
        //kafkaSender.sendToSshServer(JSON.toJSONString(sshParamMsgRequest));
        //added by wuq at 20210324 ?????????????????????ssh??????
        SshParamRequest sshParamRequest = new SshParamRequest();
        sshParamRequest.setExeServer(sshParamMsgRequest.getExeServer());
        sshParamRequest.setExeUser(sshParamMsgRequest.getExeUser());
        sshParamRequest.setExeInfoList(sshParamMsgRequest.getExeInfoList());
        sshParamRequest.setExeParams(sshParamMsgRequest.getExeParams());
        try {
            return RealTimeSsh.execute(sshParamRequest, EXE_COUNT_DEFAULT);
        } catch (DataOpsException e) {
            e.printStackTrace();
        }*/

        return start(principal, rtOpsAttachment);
    }

    @Override
    public boolean stop(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        /**
         * 1 ??????SSH?????????????????????
         * 2 ?????????????????????????????????kafka???
         */
        SshParamMsgRequest sshParamMsgRequest =
                getSshParamMsgRequest(principal, rtOpsAttachment);
        //kafkaSender.sendToSshServer(JSON.toJSONString(sshParamMsgRequest));
        //added by wuq at 20210324 ?????????????????????ssh??????
        SshParamRequest sshParamRequest = new SshParamRequest();
        sshParamRequest.setExeServer(sshParamMsgRequest.getExeServer());
        sshParamRequest.setExeUser(sshParamMsgRequest.getExeUser());
        sshParamRequest.setExeInfoList(sshParamMsgRequest.getExeInfoList());
        sshParamRequest.setExeParams(sshParamMsgRequest.getExeParams());
        try {
            log.debug("rtOpsAttachment:{},?????????????????????", rtOpsAttachment.getId());
            asyncRealtimeOperation.stop(sshParamRequest,rtOpsAttachment);
            log.debug("rtOpsAttachment:{}????????????", rtOpsAttachment.getId());
            //return RealTimeSsh.execute(sshParamRequest, EXE_COUNT_DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getLocalizedMessage());
        }
        return true;
    }


    private SshParamMsgRequest getSshParamMsgRequest(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        SshParamMsgRequest sshParamMsgRequest = new SshParamMsgRequest();
        sshParamMsgRequest.setExeServer(AppServerName);
        sshParamMsgRequest.setExeUser(principal.getUserId());
        sshParamMsgRequest.setExeInfoList(JSON.parseArray(rtOpsAttachment.getOpsCommand(), ExeInfo.class));
        sshParamMsgRequest.setExeParams(rtOpsAttachment.getOpsParam());
        sshParamMsgRequest.setTopicName(receiveSshServerTopic);
        return sshParamMsgRequest;
    }

    private SshParamMsgRequest getSshParamMsgRequest(UserPrincipal principal, String opsCommand, String opsParam) {
        SshParamMsgRequest sshParamMsgRequest = new SshParamMsgRequest();
        sshParamMsgRequest.setExeServer(AppServerName);
        sshParamMsgRequest.setExeUser(principal.getUserId());
        sshParamMsgRequest.setExeInfoList(JSON.parseArray(opsCommand, ExeInfo.class));
        sshParamMsgRequest.setExeParams(opsParam);
        sshParamMsgRequest.setTopicName(receiveSshServerTopic);
        return sshParamMsgRequest;
    }

}
