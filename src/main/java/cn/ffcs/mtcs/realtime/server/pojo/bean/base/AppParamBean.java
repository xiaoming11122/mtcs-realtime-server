package cn.ffcs.mtcs.realtime.server.pojo.bean.base;

import cn.ffcs.mtcs.realtime.common.entity.RtParamInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/12/012 11:18
 * @Version 1.0
 */
@Data
public class AppParamBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, String> appParam;

    public AppParamBean(List<RtParamInfo> appParamInfoList) {
        Map<String, String> appParam = new HashMap<>();
        for (RtParamInfo rtParamInfo : appParamInfoList) {
            appParam.put(rtParamInfo.getParamKey(), rtParamInfo.getParamValue());
        }
        this.appParam = appParam;
    }
}
