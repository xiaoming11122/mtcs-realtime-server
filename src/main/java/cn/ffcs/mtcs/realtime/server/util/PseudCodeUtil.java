package cn.ffcs.mtcs.realtime.server.util;

import java.util.Map;

/**
 * @author Nemo
 * @version V1.0
 * @Description: 伪代码替换工具.
 * @date 2020/5/29 15:59
 */
public class PseudCodeUtil {

    /**
     * 根据rule替换掉source中伪码
     *
     * @param source
     * @param rule
     * @return
     */
    public static String replace(String source, Map<String, String> rule) {
        for (Map.Entry<String, String> entry : rule.entrySet()) {
            source.replace(entry.getKey(), entry.getValue());
        }
        return source;
    }

    public static String replace(String source, String pseudCode, String value) {
        return source.replace(pseudCode, value);
    }


    public static String replaceAll(String source, Map<String, String> rule) {
        System.out.println("-------- shell template \n" + source);
        System.out.println("--------");
        for (Map.Entry<String, String> entry : rule.entrySet()) {
            System.out.println("-------- key \n" + entry.getKey());
            System.out.println("-------- value \n" + entry.getValue());
            System.out.println("--------");
            source = source.replaceAll(entry.getKey(), entry.getValue());
        }
        return source;
    }

    public static String replaceAll(String source, String pseudCode, String value) {
        return source.replaceAll(pseudCode, value);
    }


}
