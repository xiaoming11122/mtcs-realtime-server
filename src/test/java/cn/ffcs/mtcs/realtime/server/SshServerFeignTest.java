//package cn.ffcs.mtcs.realtime.server;
//
//import cn.ffcs.mtcs.common.response.RetDataMsg;
//import cn.ffcs.mtcs.realtime.server.feign.SshServerFeign;
//import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
//import com.alibaba.fastjson.JSON;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
///**
// * @Description .
// * @Author Nemo
// * @Date 2020/3/24/024 20:11
// * @Version 1.0
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class SshServerFeignTest {
//
//    @Autowired
//    private SshServerFeign sshServerFeign;
//
//    @Test
//    public void test() {
//        String json = "{\n" +
//                "    \"exeServer\": \"mtcs-ssh-server\",\n" +
//                "    \"exeUser\": 1,\n" +
//                "    \"exeInfoList\": [\n" +
//                "        {\n" +
//                "            \"executeMachine\": {\n" +
//                "                \"ip\": \"192.168.254.40\",\n" +
//                "                \"port\": \"22\",\n" +
//                "                \"user\": \"root\",\n" +
//                "                \"password\": \"bigdata@2019\"\n" +
//                "            },\n" +
//                "            \"exeType\": \"exeAndPrint\",\n" +
//                "            \"exeCommand\": \"echo '**result markStart**';echo 'hello';echo '**result end**'\",\n" +
//                "            \"localPath\": \"value\",\n" +
//                "            \"remotePath\": \"value\",\n" +
//                "            \"fileMode\": \"value\"\n" +
//                "        }\n" +
//                "    ],\n" +
//                "    \"exeParams\": \"nemo\"\n" +
//                "}";
//        System.out.println("---- test markStart");
//        SshParamRequest sshParamRequest = JSON.parseObject(json, SshParamRequest.class);
//        RetDataMsg<String> retDataMsg = sshServerFeign.ssh(sshParamRequest);
//        System.out.println(JSON.toJSONString(retDataMsg));
//    }
//}
