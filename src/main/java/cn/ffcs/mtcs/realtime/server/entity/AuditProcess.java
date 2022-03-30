package cn.ffcs.mtcs.realtime.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

public class AuditProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(
            value = "audit_id",
            type = IdType.AUTO
    )
    private Long auditId;

    private String auditType;

    private String insId;

    private String insVersion;

    private String preInsProperty;

    private String insName;

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getAuditType() {
        return auditType;
    }

    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }

    public String getInsId() {
        return insId;
    }

    public void setInsId(String insId) {
        this.insId = insId;
    }

    public String getInsVersion() {
        return insVersion;
    }

    public void setInsVersion(String insVersion) {
        this.insVersion = insVersion;
    }

    public String getPreInsProperty() {
        return preInsProperty;
    }

    public void setPreInsProperty(String preInsProperty) {
        this.preInsProperty = preInsProperty;
    }

    public String getInsName() {
        return insName;
    }

    public void setInsName(String insName) {
        this.insName = insName;
    }
}
