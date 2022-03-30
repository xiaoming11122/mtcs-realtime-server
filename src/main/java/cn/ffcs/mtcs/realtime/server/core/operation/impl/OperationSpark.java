package cn.ffcs.mtcs.realtime.server.core.operation.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.KafkaContents;
import cn.ffcs.mtcs.realtime.server.core.kafka.KafkaSender;
import cn.ffcs.mtcs.realtime.server.core.operation.IOperation;
import cn.ffcs.mtcs.realtime.server.core.operation.OperationFactory;
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
 * @Date 2020/2/9/009 14:54
 * @Version 1.0
 */
@Component
public class OperationSpark implements IOperation, InitializingBean {

    @Autowired
    private KafkaSender kafkaSender;

    @Value("${spring.application.name}")
    private String AppServerName;

    @Value("receive.ssh.server.topic")
    private String receiveSshServerTopic;


    @Override
    public void afterPropertiesSet() throws Exception {
        OperationFactory.registerOperation(EngineTypeEnum.Spark.getValue(), this);
    }

    /**
     * 启动
     *
     * @param principal
     * @param rtOpsAttachment
     * @return
     */
    @Override
    public boolean start(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        /**
         * 1 封装SSH服务调用的参数
         * 2 异步调用，将消息发送到kafka中
         */
        SshParamMsgRequest sshParamMsgRequest =
                getSshParamMsgRequest(principal, rtOpsAttachment);
        kafkaSender.sendToSshServer(JSON.toJSONString(sshParamMsgRequest));
        return true;
    }


    /**
     * 重启
     *
     * @param principal
     * @param rtOpsAttachment
     * @return
     */
    @Override
    public boolean restart(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        /**
         * 1 封装SSH服务调用的参数
         * 2 异步调用，将消息发送到kafka中
         */
        SshParamMsgRequest sshParamMsgRequest =
                getSshParamMsgRequest(principal, rtOpsAttachment);
        kafkaSender.sendToSshServer(JSON.toJSONString(sshParamMsgRequest));
        return true;
    }

    /**
     * 停止
     *
     * @param principal
     * @param rtOpsAttachment
     * @return
     */
    @Override
    public boolean stop(UserPrincipal principal, RtOpsAttachment rtOpsAttachment) {
        /**
         * 1 封装SSH服务调用的参数
         * 2 异步调用，将消息发送到kafka中
         */
        SshParamMsgRequest sshParamMsgRequest =
                getSshParamMsgRequest(principal, rtOpsAttachment);
        kafkaSender.sendToSshServer(JSON.toJSONString(sshParamMsgRequest));
        return true;
    }


    private SshParamRequest getSshParamRequest(UserPrincipal principal,
                                               RtOpsAttachment rtOpsAttachment) {
        SshParamRequest sshParamRequest = new SshParamRequest();
        sshParamRequest.setExeServer(AppServerName);
        sshParamRequest.setExeUser(principal.getUserId());
        sshParamRequest.setExeInfoList(JSON.parseArray(rtOpsAttachment.getOpsCommand(), ExeInfo.class));
        sshParamRequest.setExeParams(rtOpsAttachment.getOpsParam());
        return sshParamRequest;
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