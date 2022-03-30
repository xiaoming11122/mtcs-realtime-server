package cn.ffcs.mtcs;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.druid.sql.SQLUtils.toSQLString;

public class test {

    public static void main(String[] args) {

        String msg = "/data/program/ty-streaming/flink/apps/hdfs/flink_conf\n" +
                "Will shutdown mtcsRealtimeApp: 93988\n" +
                "2021-08-07 17:37:43,742 INFO  org.apache.hadoop.security.UserGroupInformation              [] - Login successful for user hdfs/nm-bigdata-031080086.ctc.local@CHINATELECOM.ZSTACK using keytab file /data/program/ty-streaming/flink/apps/hdfs/flink_conf/hdfs.keytab\n" +
                "\n" +
                "2021-08-07 17:37:47,482 WARN  org.apache.flink.yarn.configuration.YarnLogConfigUtil        [] - The configuration directory ('/data/program/ty-streaming/flink/apps/hdfs/flink_conf') already contains a LOG4J config file.If you want to use logback, then please delete or rename the log configuration file.\n" +
                "2021-08-07 17:37:47,784 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - No path for the flink jar passed. Using the location of class org.apache.flink.yarn.YarnClusterDescriptor to locate the jar\n" +
                "2021-08-07 17:37:48,009 INFO  org.apache.hadoop.conf.Configuration                         [] - found resource resource-types.xml at file:/usr/local/hadoop-client-3.2.1_cth3_1.0.0/etc/hadoop/resource-types.xml\n" +
                "2021-08-07 17:37:48,073 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - Cluster specification: ClusterSpecification{masterMemoryMB=1024, taskManagerMemoryMB=1024, slotsPerTaskManager=1}\n" +
                "2021-08-07 17:37:48,533 WARN  org.apache.hadoop.hdfs.shortcircuit.DomainSocketFactory      [] - The short-circuit local reads feature cannot be used because libhadoop cannot be loaded.\n" +
                "2021-08-07 17:37:48,898 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:49,152 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:49,306 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:49,504 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:49,657 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:49,806 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:50,374 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:50,535 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:50,815 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:51,125 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:51,279 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:51,413 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:51,564 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:51,705 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:51,962 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:52,504 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:52,642 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:52,776 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:52,911 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:53,063 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:53,197 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:53,331 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:53,865 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:53,995 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:54,138 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:54,266 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:54,429 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:54,567 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:54,758 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:55,666 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:55,819 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:55,835 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - Adding keytab /data/program/ty-streaming/flink/apps/hdfs/flink_conf/hdfs.keytab to the AM container local resource bucket\n" +
                "2021-08-07 17:37:55,955 INFO  org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient [] - SASL encryption trust check: localHostTrusted = false, remoteHostTrusted = false\n" +
                "2021-08-07 17:37:56,387 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - Adding delegation token to the AM container.\n" +
                "2021-08-07 17:37:56,403 INFO  org.apache.hadoop.hdfs.DFSClient                             [] - Created token for hdfs: HDFS_DELEGATION_TOKEN owner=hdfs/nm-bigdata-031080086.ctc.local@CHINATELECOM.ZSTACK, renewer=yarn, realUser=, issueDate=1628329076395, maxDate=1628933876395, sequenceNumber=2426, masterKeyId=118 on ha-hdfs:ctyunns\n" +
                "2021-08-07 17:37:56,426 INFO  org.apache.hadoop.mapreduce.security.TokenCache              [] - Got dt for hdfs://ctyunns; Kind: HDFS_DELEGATION_TOKEN, Service: ha-hdfs:ctyunns, Ident: (token for hdfs: HDFS_DELEGATION_TOKEN owner=hdfs/nm-bigdata-031080086.ctc.local@CHINATELECOM.ZSTACK, renewer=yarn, realUser=, issueDate=1628329076395, maxDate=1628933876395, sequenceNumber=2426, masterKeyId=118)\n" +
                "2021-08-07 17:37:56,426 INFO  org.apache.flink.yarn.Utils                                  [] - Attempting to obtain Kerberos security token for HBase\n" +
                "2021-08-07 17:37:56,427 INFO  org.apache.flink.yarn.Utils                                  [] - HBase is not available (not packaged with this application): ClassNotFoundException : \"org.apache.hadoop.hbase.HBaseConfiguration\".\n" +
                "2021-08-07 17:37:56,434 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - Submitting application master application_1625569338580_1210\n" +
                "2021-08-07 17:37:56,677 INFO  org.apache.hadoop.yarn.client.api.impl.YarnClientImpl        [] - Submitted application application_1625569338580_1210\n" +
                "2021-08-07 17:37:56,677 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - Waiting for the cluster to be allocated\n" +
                "2021-08-07 17:37:56,679 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - Deploying cluster, current state ACCEPTED\n" +
                "2021-08-07 17:38:05,776 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - YARN application has been deployed successfully.\n" +
                "2021-08-07 17:38:05,777 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - Found Web Interface nm-bigdata-031032021.ctc.local:8080 of application 'application_1625569338580_1210'.\n" +
                "Job has been submitted with JobID 6ed334f98111d18e7ab328ce1f040b9a\n" +
                "job id:6ed334f98111d18e7ab328ce1f040b9a";
        msg = "Suspending job \"0d98d9653c4f67f051ccfca4b55763bd\" with a savepoint.\n" +
                "Savepoint completed. Path: hdfs://ctyunns/apps/flink/savepoints/savepoint-0d98d9-95720119ba9e";
        String pattern = "(Job has been submitted with JobID )(\\w*)";
        pattern = "(Savepoint completed. Path: )(\\S*)";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(msg);
        if (m.find( )) {
            System.out.println("Found value: " + m.group(0) );
            System.out.println("Found value: " + m.group(1) );
            System.out.println("Found value: " + m.group(2) );
        } else {
            System.out.println("NO MATCH");
        }

        pattern = "(Submitted application )(\\w*)";
        r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        m = r.matcher(msg);
        if (m.find( )) {
            System.out.println("Found value: " + m.group(0) );
            System.out.println("Found value: " + m.group(1) );
            System.out.println("Found value: " + m.group(2) );
        } else {
            System.out.println("NO MATCH");
        }
        pattern = "(Found Web Interface )(\\S*)";
        r = Pattern.compile(pattern);
        m = r.matcher(msg);
        if (m.find( )) {
            System.out.println("Found value: " + m.group(0) );
            System.out.println("Found value: " + m.group(1) );
            System.out.println("Found value: " + m.group(2) );
        } else {
            System.out.println("NO MATCH");
        }


        msg.substring(msg.indexOf("Job has been submitted with JobID"));

        StringBuffer sb = new StringBuffer();
        String dest = sb.append("aaaaaa:").append("\n").append("bbbbbb").toString();
      System.out.println(dest);


/*        String pluginsJson="[CREATE TABLE KafkaTable \\r\\n(  `id` INT, `a1` STRING, `a2` STRING,  `a3` STRING,    " +
                " procTime AS PROCTIME()  )\\r\\n WITH ( \\r\\n 'connector' = 'kafka', \\r\\n 'topic' = '##topic_name##'," +
                " \\r\\n 'properties.bootstrap.servers' = '##localhost:9092##', " +
                "\\r\\n 'properties.group.id' = 'test01', \\r\\n " +
                "'scan.startup.mode' = 'group-offsets',\\r\\n 'format' = 'csv')\\r\\n," +
                "CREATE TABLE KafkaTable \\r\\n(  `id` INT, `a1` STRING, `a2` STRING,  `a3` STRING,     procTime AS PROCTIME()  )\\r\\n WITH ( \\r\\n 'connector' = 'kafka', \\r\\n 'topic' = '##topic_name##', \\r\\n 'properties.bootstrap.servers' = '##localhost:9092##', \\r\\n 'properties.group.id' = 'test01', \\r\\n 'scan.startup.mode' = 'group-offsets',\\r\\n 'format' = 'csv')\\r\\n,INSERT INTO MyUserTable \\r\\nSELECT a.id,b.name,a.a1,a.a2,a.a3 \\r\\nFROM KafkaTable a \\r\\nleft join DimProduct FOR SYSTEM_TIME AS OF a.procTime as b \\r\\n on a.id=b.id ,INSERT INTO MyUserTable \\r\\nSELECT a.id,b.name,a.a1,a.a2,a.a3 \\r\\nFROM KafkaTable a \\r\\nleft join DimProduct FOR SYSTEM_TIME AS OF a.procTime as b \\r\\n on a.id=b.id ]";

        List<JSONObject> pluginList = (List<JSONObject>) JSON.parse(pluginsJson);
        for (int i = 0; i < pluginList.size(); i++) {

        }*/

      /*  try {
            String str="select GROUP_CONCAT( ";
            System.out.println(checkSqlFormat(str));
        }catch (ParserException e){
            System.out.println(e.getMessage());
        }*/
    }


    //SQL语句校验
    public static boolean checkSqlFormat(String sql) {
        boolean result=true;
        List<SQLStatement> statementList = null;
        SQLStatementParser parser = null;
        try {
            parser = SQLParserUtils.createSQLStatementParser(sql, "mysql");
            statementList = parser.parseStatementList();
        } catch (ParserException e) {
            result=false;
            System.out.println("SQL转换中发生了错误：" + e.getMessage());
        }
        return result;
    }


    public static String sqlFormat(String sql) {
        List<SQLStatement> statementList = null;
        SQLStatementParser parser = null;
        try {
            parser = SQLParserUtils.createSQLStatementParser(sql, "mysql");
            statementList = parser.parseStatementList();
        } catch (ParserException e) {
            System.out.println("SQL转换中发生了错误：" + e.getMessage());
            throw e;
        }
        return toSQLString(statementList, "mysql");
    }
}
