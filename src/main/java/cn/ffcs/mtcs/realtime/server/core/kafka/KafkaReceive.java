package cn.ffcs.mtcs.realtime.server.core.kafka;

import cn.ffcs.mtcs.realtime.server.constants.KafkaContents;
import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/20/020 14:44
 * @Version 1.0
 */
@Component
@Slf4j
public class KafkaReceive {

   /* @KafkaListener(topics = {"#{'${receive.ssh.server.topic}'}"})
    public void listen(ConsumerRecord<String, String> record) {


    }*/
}
