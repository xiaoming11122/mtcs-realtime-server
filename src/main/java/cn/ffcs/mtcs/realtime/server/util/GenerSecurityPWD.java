package cn.ffcs.mtcs.realtime.server.util;

import cn.ffcs.common.basic.security.SecurityFactory;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.flink.sql.parser.impl.FlinkSqlParserImpl;
import org.apache.flink.sql.parser.validate.FlinkSqlConformance;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.calcite.avatica.util.Quoting.BACK_TICK;

public class GenerSecurityPWD {
    private static String DEFAULT_CRYPT_KEY = "k38d81C!@#dkro22232JAMDGIJGDSe48dk>KUY%%$";

    /**
     * 校验执行参数是否符合规范
     *
     * @param sql
     * @param pluginName
     * @return
     * @throws DataOpsException
     */
    public static String parseFlinkSql(String sql, String pluginName) throws DataOpsException {
        if (sql != null && !sql.isEmpty()) {
            try {
                SqlParser parser = SqlParser.create(sql, SqlParser.configBuilder()
                        .setParserFactory(FlinkSqlParserImpl.FACTORY)
                        .setQuoting(BACK_TICK)
                        .setUnquotedCasing(Casing.TO_LOWER)   //字段名统一转化为小写
                        .setQuotedCasing(Casing.UNCHANGED)
                        .setConformance(FlinkSqlConformance.DEFAULT)
                        .build()
                );
                List<SqlNode> sqlNodeList = parser.parseStmtList().getList();
                // 打印规范后的sql
//                if (sqlNodeList != null && !sqlNodeList.isEmpty()) {
//                    for (SqlNode sqlNode : sqlNodeList) {
//                        sqlList.add(sqlNode.toString());
//                    }
//                }
            } catch (SqlParseException e) {
                return "插件: " + pluginName + " " + e.getPos() + " 格式错误";
            }
        }
        return "";
    }

    /**
     * 密码加密
     *
     * @param password
     * @return
     */
    public static String encode(String password) {
        return SecurityFactory.encode(SecurityFactory.DesRandom, password, DEFAULT_CRYPT_KEY);
    }

    /**
     * 密码解密
     *
     * @param securityPassword
     * @return
     */
    public static String decode(String securityPassword) {
        return SecurityFactory.decode(SecurityFactory.DesRandom, securityPassword, DEFAULT_CRYPT_KEY);
    }

    /**
     * 判断是否有密码
     *
     * @param source
     * @return
     */
    public static Boolean judgeSecurityPassword(String source) {
        String passwordString = ".*'.*password[\\s]*'[\\s]*=[\\s]*'(.*)'";
        Pattern passwordPattern = Pattern.compile(passwordString);
        Matcher passwordMatcher = passwordPattern.matcher(source);
        return passwordMatcher.find();
    }

    /**
     * 获取密码 返回例子如下：['password' = 'g4J88bxgl2Ns7KMuS4Byxg==', g4J88bxgl2Ns7KMuS4Byxg==]
     *
     * @param source
     * @return
     */
    public static List<String> generSecurityPassword(String source) {
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

    /**
     * 生成密码唯一key
     *
     * @param source
     * @return
     */
    public static String generSecurityKey(String source) {
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

    public static void main(String[] args) {
        String pluginInfo = "CREATE TABLE mysqlcdcfrom1 (\n" +
                "   id varchar,\n" +
                "   a1 varchar,\n" +
                "   a2 varchar,\n" +
                "   a3 varchar,\n" +
                "   PRIMARY KEY (id) NOT ENFORCED\n" +
                ") WITH (\n" +
                "  'connector' = 'mysql-cdc',\n" +
                "  'hostname' = '10.11.1.140',\n" +
                "  'port' = '3306',\n" +
                "  'username' = 'web_app',\n" +
                "  'server-id' = '1234567',\n" +
                "  'password' = '******',\n" +
                "  'database-name' = 'test',\n" +
                "  'table-name' = 'table_1_from',\n" +
                "  'debezium.snapshot.mode' = 'schema_only',\n" +
                "  'scan.incremental.snapshot.enabled' = 'false'\n" +
                ")";
        System.out.println(GenerSecurityPWD.generSecurityPassword(pluginInfo));
        System.out.println(GenerSecurityPWD.judgeSecurityPassword(pluginInfo));
        System.out.println(GenerSecurityPWD.encode("0fKyj/KqYcH4nyZBUR9+haH+GfKZ2xp5"));
    }
}
