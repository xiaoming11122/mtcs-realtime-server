package cn.ffcs.mtcs.realtime.server;


import cn.ffcs.mtcs.basis.util.Base64Util;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nemo
 * @version V1.0
 * @Description: .
 * @date 2020/7/10 15:04
 */
public class Base64UtilTest {
    public static void main(String[] args) {


       String data = "TYIlVJ1g$t5k&Mg# ";
       String encode = new String(Base64Util.encodeBase64V2(data.getBytes()));
       System.out.println(encode);

        System.out.println(Base64Util.decodeBase64V2("QmlnZGF0YUAwNzI3"));

        System.out.println(Base64Util.decodeBase64V2(encode));

        String old = "{\"appParam\":{\"appParam\":{\"parallelism\":\"20\"}},\"opsAttachmentId\":\"1162\",\"opsName\":\"Stopping\",\"pluginContentList\":[\"CREATE TABLE mysql_cdc0730 (\n" +
                "   id varchar,\n" +
                "   a1 varchar,\n" +
                "   a2 varchar,\n" +
                "   a3 varchar\n" +
                ") WITH (\n" +
                "  'connector' = 'mysql-cdc',\n" +
                "  'hostname' = '10.31.80.82',\n" +
                "  'port' = 'root',\n" +
                "  'username' = 'root',\n" +
                "  'server-id' = 'root',\n" +
                "  'password' = '1234',\n" +
                "  'database-name' = 'cz01',\n" +
                "  'table-name' = '******',\n" +
                "  'debezium.snapshot.mode' = 'initial'\n" +
                ")\",\"CREATE TABLE KafkaTable0730 ( `id` STRING, `a1` STRING, `a2` STRING,  `a3` STRING,  PRIMARY KEY (id) NOT ENFORCED\n" +
                ") WITH (\n" +
                "  'connector' = 'upsert-kafka',\n" +
                "  'topic' = 'topic_20210730',\n" +
                "  'properties.bootstrap.servers' = 'nm-bigdata-031032020.ctc.local:9092',\n" +
                "  'properties.security.protocol' = 'SASL_PLAINTEXT',\n" +
                "  'properties.sasl.mechanism' = 'GSSAPI',\n" +
                "  'properties.sasl.kerberos.service.name' = 'kafka',\n" +
                "  'properties.group.id' = 'topic_20210730_group1',\n" +
                "  'key.format' = 'json',\n" +
                "  'value.format' = 'json'\n" +
                ")\n" +
                "\",\"insert into KafkaTable0730 select id,a1,a2,a3 from mysql_cdc0730 \"],\"taskInfo\":{\"taskCode\":\"flink_2021080502\",\"taskId\":10059,\"taskVersion\":\"2.0\"},\"userInfo\":{\"teamId\":2,\"userId\":23,\"userName\":\"chenqq\"}}";


        String str = "{\"appParam\":{\"appParam\":{\"parallelism\":\"20\"}},\"opsAttachmentId\":\"1162\",\"opsName\":\"Stopping\",\"pluginContentList\":[\"CREATE TABLE mysql_cdc0730 (\n" +
                "   id varchar,\n" +
                "   a1 varchar,\n" +
                "   a2 varchar,\n" +
                "   a3 varchar\n" +
                ") WITH (\n" +
                "  'connector' = 'mysql-cdc',\n" +
                "  'hostname' = '10.31.80.82',\n" +
                "  'port' = 'root',\n" +
                "  'username' = 'root',\n" +
                "  'server-id' = 'root',\n" +
                "  'password' = 'root',\n" +
                "  'database-name' = 'cz01',\n" +
                "  'table-name' = '******',\n" +
                "  'debezium.snapshot.mode' = 'initial'\n" +
                ")\",\"CREATE TABLE KafkaTable0730 ( `id` STRING, `a1` STRING, `a2` STRING,  `a3` STRING,  PRIMARY KEY (id) NOT ENFORCED\n" +
                ") WITH (\n" +
                "  'connector' = 'upsert-kafka',\n" +
                "  'topic' = 'topic_20210730',\n" +
                "  'properties.bootstrap.servers' = 'nm-bigdata-031032020.ctc.local:9092',\n" +
                "  'properties.security.protocol' = 'SASL_PLAINTEXT',\n" +
                "  'properties.sasl.mechanism' = 'GSSAPI',\n" +
                "  'properties.sasl.kerberos.service.name' = 'kafka',\n" +
                "  'properties.group.id' = 'topic_20210730_group1',\n" +
                "  'key.format' = 'json',\n" +
                "  'value.format' = 'json'\n" +
                ")\n" +
                "\",\"insert into KafkaTable0730 select id,a1,a2,a3 from mysql_cdc0730 \"],\"taskInfo\":{\"taskCode\":\"flink_2021080502\",\"taskId\":10059,\"taskVersion\":\"2.0\"},\"userInfo\":{\"teamId\":2,\"userId\":23,\"userName\":\"chenqq\"}}";
        String key = generSecurityKey(str);

        String value = null;
        String passwordString = ".*'.*password[\\s]*'[\\s]*=[\\s]*'(.*)'";
        Pattern passwordPattern = Pattern.compile(passwordString);
        Matcher passwordMatcher = passwordPattern.matcher(str);
        if (passwordMatcher.find()) {
            passwordMatcher.reset();
            while (passwordMatcher.find()) {
                // System.out.println("0:" + m.group(0));
                // System.out.println("1:" + m.group(1));
                value = passwordMatcher.group(1);
            }
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(key, value);
        System.out.println("----" + map + "----");

        String replaceString = ".*'.*password[\\s]*'[\\s]*=[\\s]*'(.*)'";
        Pattern replacePattern = Pattern.compile(replaceString);
        Matcher replaceMatcher = replacePattern.matcher(old);
        String replaceKey = null;
        if (replaceMatcher.find()) {
            replaceMatcher.reset();
            while (replaceMatcher.find()) {
                // System.out.println("0:" + m.group(0));
                // System.out.println("1:" + m.group(1));
                replaceKey = replaceMatcher.group(0);
            }
        }
        System.out.println("replaceKey" + replaceKey);
        if (!StringUtils.isEmpty(replaceKey) && replaceKey.contains("******")) {
            System.out.println("--------------------------");
            String reKey = generSecurityKey(str);
            System.out.println("reKey：" + reKey + "     newKey：" + map.get(key));
            String newReplace = replaceKey.replace("******", map.get(key));
            System.out.println("newReplace" + newReplace);
            String s = old.replace(replaceKey, newReplace);
            System.out.println(s);
            System.out.println("--------------------------");
        }

//        String data = "Nemo2019!@#$";
//        String encode = new String(Base64Util.encodeBase64V2(data.getBytes()));
//        System.out.println("encode : " + encode);
//        String decode = new String(Base64Util.decodeBase64V2(encode));
//        System.out.println("decode : " + decode);
//        System.out.println(Double.valueOf("22457325090"));
    }


    private static String generSecurityKey(String source) {
        // 定位hostname+连接方式+username当作key
        StringBuffer key = new StringBuffer();
        // 获取connector判断是mysql-cdc还是jdbc
        String connectorString = ".*'.*connector[\\s]*'[\\s]*=[\\s]*'(.*)'";
        Pattern connectorPattern = Pattern.compile(connectorString);
        Matcher connectorMatcher = connectorPattern.matcher(source);
        String connector = null;
        if (connectorMatcher.find()) {
            connectorMatcher.reset();
            while (connectorMatcher.find()) {
                // System.out.println("0:" + m.group(0));
                // System.out.println("1:" + m.group(1));
                connector = connectorMatcher.group(1);
                key.append(connector);
            }
        }
        if ("mysql-cdc".equals(connector)) {
            // 获取的是hostname
            String hostNameString = ".*'.*hostname[\\s]*'[\\s]*=[\\s]*'(.*)'";
            Pattern hostNamePattern = Pattern.compile(hostNameString);
            Matcher hostNameMatcher = hostNamePattern.matcher(source);
            if (hostNameMatcher.find()) {
                hostNameMatcher.reset();
                while (hostNameMatcher.find()) {
                    // System.out.println("0:" + m.group(0));
                    // System.out.println("1:" + m.group(1));
                    String hostname = hostNameMatcher.group(1);
                    key.append(hostname);
                }
            }
        }
        if ("jdbc".equals(connector)) {
            // 获取的是url
            String urlString = ".*'.*url[\\s]*'[\\s]*=[\\s]*'(.*)'";
            Pattern urlPattern = Pattern.compile(urlString);
            Matcher urlMatcher = urlPattern.matcher(source);
            if (urlMatcher.find()) {
                urlMatcher.reset();
                while (urlMatcher.find()) {
                    // System.out.println("0:" + m.group(0));
                    // System.out.println("1:" + m.group(1));
                    String url = urlMatcher.group(1);
                    key.append(url);
                }
            }
        }
        // 获取用户名信息
        String userNameString = ".*'.*username[\\s]*'[\\s]*=[\\s]*'(.*)'";
        Pattern userNamePattern = Pattern.compile(userNameString);
        Matcher userNameMatcher = userNamePattern.matcher(source);
        if (userNameMatcher.find()) {
            userNameMatcher.reset();
            while (userNameMatcher.find()) {
                // System.out.println("0:" + m.group(0));
                // System.out.println("1:" + m.group(1));
                String username = userNameMatcher.group(1);
                key.append(username);
            }
        }
        if (StringUtils.isEmpty(key.toString())) {
            // 异常处理
        }
        return key.toString();
    }
}
