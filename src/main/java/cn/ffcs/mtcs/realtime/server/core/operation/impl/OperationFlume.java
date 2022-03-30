package cn.ffcs.mtcs.realtime.server.core.operation.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.core.kafka.KafkaSender;
import cn.ffcs.mtcs.realtime.server.core.operation.IOperation;
import cn.ffcs.mtcs.realtime.server.core.operation.OperationFactory;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.util.RealTimeSsh;
import cn.ffcs.mtcs.ssh.common.request.ExeInfo;
import cn.ffcs.mtcs.ssh.common.request.SshParamMsgRequest;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/9/009 14:55
 * @Version 1.0
 */
@Component
public class OperationFlume implements IOperation, InitializingBean {

    @Autowired
    private KafkaSender kafkaSender;

    @Value("${spring.application.name}")
    private String AppServerName;

    @Value("${exe.count.default}")
    private int EXE_COUNT_DEFAULT;


    @Value("receive.ssh.server.topic")
    private String receiveSshServerTopic;

    @Override
    public void afterPropertiesSet() throws Exception {
        OperationFactory.registerOperation(EngineTypeEnum.Flume.getValue(), this);
    }

    @Override
    public boolean start(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        /**
         * 1 封装SSH服务调用的参数
         * 2 异步调用，将消息发送到kafka中
         */
        SshParamMsgRequest sshParamMsgRequest =
                getSshParamMsgRequest(principal, rtOpsAttachment);
        //kafkaSender.sendToSshServer(JSON.toJSONString(sshParamMsgRequest));
        //added by wuq at 20210324 更改为直接调用ssh方式
        SshParamRequest sshParamRequest = new SshParamRequest();
        sshParamRequest.setExeServer(sshParamMsgRequest.getExeServer());
        sshParamRequest.setExeUser(sshParamMsgRequest.getExeUser());
        sshParamRequest.setExeInfoList(sshParamMsgRequest.getExeInfoList());
        sshParamRequest.setExeParams(sshParamMsgRequest.getExeParams());
        try {
            return RealTimeSsh.execute(sshParamRequest, EXE_COUNT_DEFAULT);
        } catch (DataOpsException e) {
            e.printStackTrace();
        }


        return true;
    }

    @Override
    public boolean restart(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        return start(principal, rtOpsAttachment);
    }

    @Override
    public boolean stop(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        SshParamMsgRequest sshParamMsgRequest =
                getSshParamMsgRequest(principal, rtOpsAttachment);
        //kafkaSender.sendToSshServer(JSON.toJSONString(sshParamMsgRequest));
        //added by wuq at 20210324 更改为直接调用ssh方式
        SshParamRequest sshParamRequest = new SshParamRequest();
        sshParamRequest.setExeServer(sshParamMsgRequest.getExeServer());
        sshParamRequest.setExeUser(sshParamMsgRequest.getExeUser());
        sshParamRequest.setExeInfoList(sshParamMsgRequest.getExeInfoList());
        sshParamRequest.setExeParams(sshParamMsgRequest.getExeParams());
        try {
            return RealTimeSsh.execute(sshParamRequest, EXE_COUNT_DEFAULT);
        } catch (DataOpsException e) {
            e.printStackTrace();
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
}
