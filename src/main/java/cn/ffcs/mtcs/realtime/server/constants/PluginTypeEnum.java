package cn.ffcs.mtcs.realtime.server.constants;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 16:34
 * @Version 1.0
 */
public enum PluginTypeEnum {

    CustomPlugin("customPlugin", "自定义插件");

    private String code;
    private String value;

    private PluginTypeEnum(String code, String engineName) {
        this.code = code;
        this.value = engineName;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
