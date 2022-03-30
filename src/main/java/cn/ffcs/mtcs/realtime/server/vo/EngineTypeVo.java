package cn.ffcs.mtcs.realtime.server.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel("执行引擎类型")
public class EngineTypeVo implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("引擎编码")
    private String engineCode;
    @ApiModelProperty("引擎名称")
    private String engineName;
    @ApiModelProperty("是否json校验")
    private boolean isCheckJson;

    public String getEngineCode() {
        return engineCode;
    }

    public void setEngineCode(String engineCode) {
        this.engineCode = engineCode;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public boolean isCheckJson() {
        return isCheckJson;
    }

    public void setCheckJson(boolean checkJson) {
        isCheckJson = checkJson;
    }
}
