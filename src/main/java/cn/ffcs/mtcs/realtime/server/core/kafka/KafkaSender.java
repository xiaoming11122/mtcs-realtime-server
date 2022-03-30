package cn.ffcs.mtcs.realtime.server.core.kafka;

import cn.ffcs.mtcs.realtime.server.constants.KafkaContents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/20/020 14:44
 * @Version 1.0
 */
@Component
@Slf4j
public class KafkaSender {

  /*  @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${send.ssh.server.topic}")
    private String topic;*/


    /**
     * 向ssh服务发送消息
     *
     * @param message
     */
    public void sendToSshServer(String message) {

       /* System.out.println("topic=====" + topic);

        // 发送消息
        ListenableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, "key", message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                // todo 待升级 失败的话将消息放入数据库中
            }

            @Override
            public void onSuccess(SendResult<String, String> stringObjectSendResult) {
                // todo 待升级 业务处理
                // 不做任何处理
            }
        });*/
    }


}
