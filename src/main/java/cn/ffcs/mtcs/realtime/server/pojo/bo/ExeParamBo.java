package cn.ffcs.mtcs.realtime.server.pojo.bo;

import lombok.Data;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/11/011 18:47
 * @Version 1.0
 */
@Data
public class ExeParamBo {

    private String startType;

    private String processInfo;

    private String plugins;

    public ExeParamBo() {
    }

    public ExeParamBo(String startType, String processInfo, String plugins) {
        this.startType = startType;
        this.processInfo = processInfo;
        this.plugins = plugins;
    }
}
