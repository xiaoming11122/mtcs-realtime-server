package cn.ffcs.mtcs.realtime.server;

import cn.ffcs.common.basic.security.Security;
import cn.ffcs.common.basic.security.SecurityFactory;
import cn.ffcs.mtcs.realtime.common.request.RtFlowPluginRelativeRequest;
import cn.ffcs.mtcs.realtime.common.request.TaskSaveRequest;
import cn.ffcs.mtcs.realtime.common.vo.DetailTaskVo;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.exception.FeignException;
import cn.ffcs.mtcs.realtime.server.pojo.bean.flink.FlinkParam;
import cn.ffcs.mtcs.realtime.server.util.GenerSecurityPWD;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenerPWD {
    private static String DEFAULT_CRYPT_KEY = "k38d81C!@#dkro22232JAMDGIJGDSe48dk>KUY%%$";

    public static void main(String[] args) throws ExistException {
        String str = "test_password";
        String encode = GenerSecurityPWD.encode(str);
        System.out.println(encode);
        System.out.println("--------------");
        String old = "Vst9T8f3q3w7r75GL4KSwQ==";
        String decode = GenerSecurityPWD.decode(old);
        System.out.println(decode);
        System.out.println("----------");

        ArrayList<String> newList = new ArrayList<>();
        newList.add("CREATE TABLE KafkaTable \n" +
                "(  `id` INT, `a1` STRING, `a2` STRING,  `a3` STRING,     procTime AS PROCTIME()  )\n" +
                " WITH ( \n" +
                " 'connector' = 'kafka', \n" +
                " 'topic' = '##topic_name##', \n" +
                " 'properties.bootstrap.servers' = '##localhost:9092##', \n" +
                " 'properties.group.id' = 'test01', \n" +
                " 'scan.startup.mode' = 'group-offsets',\n" +
                " 'format' = 'csv')\n");
        newList.add("CREATE TABLE MyUserTable (`id` INT,product_name STRING,a1 STRING,a2 STRING,a3 STRING) \n" +
                "WITH ( \n" +
                "'connector' = 'jdbc', \n" +
                "'url' = 'jdbc:mysql://10.11.1.140:3306/ty_test', \n" +
                "'table-name' = 'test_table', \n" +
                "'username' = 'demo_user', \n" +
                "'password' = '******')\n");
        newList.add("INSERT INTO MyUserTable \n" +
                "SELECT a.id,b.name,a.a1,a.a2,a.a3 \n" +
                "FROM KafkaTable a \n" +
                "left join DimProduct FOR SYSTEM_TIME AS OF a.procTime as b \n" +
                " on a.id=b.id ");


        ArrayList<String> oldList = new ArrayList<>();
        oldList.add("CREATE TABLE KafkaTable \n" +
                "(  `id` INT, `a1` STRING, `a2` STRING,  `a3` STRING,     procTime AS PROCTIME()  )\n" +
                " WITH ( \n" +
                " 'connector' = 'kafka', \n" +
                " 'topic' = '##topic_name##', \n" +
                " 'properties.bootstrap.servers' = '##localhost:9092##', \n" +
                " 'properties.group.id' = 'test01', \n" +
                " 'scan.startup.mode' = 'group-offsets',\n" +
                " 'format' = 'csv')\n");
        oldList.add("CREATE TABLE MyUserTable (`id` INT,product_name STRING,a1 STRING,a2 STRING,a3 STRING) \n" +
                "WITH ( \n" +
                "'connector' = 'jdbc', \n" +
                "'url' = 'jdbc:mysql://10.11.1.140:3306/ty_test', \n" +
                "'table-name' = 'mytable', \n" +
                "'username' = 'demo_user', \n" +
                "'password' = 'Vst9T8f3q3zt5BtiD/LAEg==')");
        oldList.add("INSERT INTO MyUserTable \n" +
                "SELECT a.id,b.name,a.a1,a.a2,a.a3 \n" +
                "FROM KafkaTable a \n" +
                "left join DimProduct FOR SYSTEM_TIME AS OF a.procTime as b \n" +
                " on a.id=b.id ");

        // 用户修改密码的话，就加密保存，用户没有修改密码的话，读取数据库密码进行替换
        List<String> plugins = transSecurityExeArg(oldList, newList);
        for (String plugin : plugins) {
            System.out.println(plugin);
        }


//        String appParam = getAppParam();
//        FlinkParam flinkParam = JSONObject.parseObject(appParam, FlinkParam.class);
//        List<String> pluginContentList = flinkParam.getPluginContentList();
//        // System.out.println(pluginContentList);
//        List<String> newPluginContentList = pluginContentList.stream().map(item -> {
//            System.out.println("-----------------");
//            Boolean flag = judgeSecurityPassword(item);
//            System.out.println(flag);
//            if (judgeSecurityPassword(item)) {
//                // 获取到密码进行解密
//                List<String> password = generSecurityPassword(item);
//                System.out.println(password);
//                String decodePassword = SecurityFactory.decode(SecurityFactory.DesRandom, password.get(1), DEFAULT_CRYPT_KEY);
//                String newReplace = password.get(0).replace(password.get(1), decodePassword);
//                String replace = item.replace(password.get(0), newReplace);
//                System.out.println("replace"+replace);
//                return replace;
//            }
//            return item;
//        }).collect(Collectors.toList());
//
//        System.out.println("------代码生成------");
//
//        for (String s : newPluginContentList) {
//            System.out.println(s);
//            System.out.println();
//        }
    }

    private static List<String> transSecurityExeArg(List<String> oldList, List<String> newList) throws ExistException {
        // 获取用户保存在数据库中的密码
        Map<String, String> transParamMap = oldList.stream().filter(item -> {
            // 过滤是否含有密码的插件
            return GenerSecurityPWD.judgeSecurityPassword(item);
        }).collect(Collectors.toMap(item -> {
            String key = GenerSecurityPWD.generSecurityKey(item);
            return key;
        }, item -> {
            // 定位密码当作value
            List<String> password = GenerSecurityPWD.generSecurityPassword(item);
            return password.get(1);
        }));

        System.out.println(transParamMap);

        // 判断修改任务信息是否有进行修改敏感信息，如果有敏感信息，则加密后添加，如果没有敏感信息，则将敏感信息的值用查出的敏感信息替换
        ArrayList<String> relativeRequests = new ArrayList<>();
        for (String item : newList) {
            if (GenerSecurityPWD.judgeSecurityPassword(item)) {
                List<String> password = GenerSecurityPWD.generSecurityPassword(item);
                if (password.get(0).contains("******")) {
                    String key = GenerSecurityPWD.generSecurityKey(item);
                    if (StringUtils.isEmpty(transParamMap.get(key))) {
                        throw new ExistException("该次操作需要重新填写密码进行修改");
                    }
                    String newReplace = password.get(0).replace("******", transParamMap.get(key));
                    relativeRequests.add(item.replace(password.get(0), newReplace));
                } else {
                    String encode = GenerSecurityPWD.encode(password.get(1));
                    String newReplace = password.get(0).replace(password.get(1), encode);
                    relativeRequests.add(item.replace(password.get(0), newReplace));
                }
            } else {
                relativeRequests.add(item);
            }
        }
        return relativeRequests;
    }

    private static Boolean judgeSecurityPassword(String source) {
        String passwordString = ".*'.*password[\\s]*'[\\s]*=[\\s]*'(.*)'";
        Pattern passwordPattern = Pattern.compile(passwordString);
        Matcher passwordMatcher = passwordPattern.matcher(source);
        return passwordMatcher.find();
    }


    private static List<String> generSecurityPassword(String source) {
        List<String> passwordList = new ArrayList<>();
        String passwordString = ".*'.*password[\\s]*'[\\s]*=[\\s]*'(.*)'";
        Pattern passwordPattern = Pattern.compile(passwordString);
        Matcher passwordMatcher = passwordPattern.matcher(source);
        if (passwordMatcher.find()) {
            passwordMatcher.reset();
            if (passwordMatcher.find()) {
                // System.out.println("0:" + passwordMatcher.group(0));
                // System.out.println("1:" + passwordMatcher.group(1));
                passwordList.add(passwordMatcher.group(0));
                passwordList.add(passwordMatcher.group(1));
            }
        }
        return passwordList;
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


    private static String getAppParam() {
        String appParam = "{\n" +
                "    \"appParam\":{\n" +
                "        \"appParam\":{\n" +
                "            \"offset_check_num\":\"5\",\n" +
                "            \"parallelism\":\"20\",\n" +
                "            \"batch_interval\":\"60\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"opsAttachmentId\":\"1261\",\n" +
                "    \"opsName\":\"Starting\",\n" +
                "    \"pluginContentList\":[\n" +
                "        \"CREATE TABLE KafkaTable \\r\\n(  `id` INT, `a1` STRING, `a2` STRING,  `a3` STRING,     procTime AS PROCTIME()  )\\r\\n WITH ( \\r\\n 'connector' = 'kafka', \\r\\n 'topic' = '##topic_name##', \\r\\n 'properties.bootstrap.servers' = '##localhost:9092##', \\r\\n 'properties.group.id' = 'test01', \\r\\n 'scan.startup.mode' = 'group-offsets',\\r\\n 'format' = 'csv')\\r\\n\",\n" +
                "        \"CREATE TABLE MyUserTable (`id` INT,product_name STRING,a1 STRING,a2 STRING,a3 STRING) \\r\\nWITH ( \\r\\n'connector' = 'jdbc', \\r\\n'url' = 'jdbc:mysql://10.11.1.140:3306/ty_test', \\r\\n'table-name' = 'mytable', \\r\\n'username' = 'root', \\r\\n'password' = 'g4J88bxgl2Ns7KMuS4Byxg==')\",\n" +
                "        \"INSERT INTO MyUserTable \\r\\nSELECT a.id,b.name,a.a1,a.a2,a.a3 \\r\\nFROM KafkaTable a \\r\\nleft join DimProduct FOR SYSTEM_TIME AS OF a.procTime as b \\r\\n on a.id=b.id \"\n" +
                "    ],\n" +
                "    \"taskInfo\":{\n" +
                "        \"taskCode\":\"flink2021080601\",\n" +
                "        \"taskId\":10540,\n" +
                "        \"taskVersion\":\"1.0\"\n" +
                "    },\n" +
                "    \"userInfo\":{\n" +
                "        \"teamId\":108,\n" +
                "        \"userId\":197,\n" +
                "        \"userName\":\"wuq\"\n" +
                "    }\n" +
                "}";
        return appParam;
    }

    private static String getNewStr() {
        String newStr = "{\"appParam\":{\"appParam\":{\"parallelism\":\"20\"}},\"opsAttachmentId\":\"1162\",\"opsName\":\"Stopping\",\"pluginContentList\":[\"CREATE TABLE mysql_cdc0730 (\n" +
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
        return newStr;
    }

    private static String getOldStr() {
        String oldStr = "{\"appParam\":{\"appParam\":{\"parallelism\":\"20\"}},\"opsAttachmentId\":\"1162\",\"opsName\":\"Stopping\",\"pluginContentList\":[\"CREATE TABLE mysql_cdc0730 (\n" +
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
                "  'password' = 'qwdvreg522324',\n" +
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
        return oldStr;
    }
}
