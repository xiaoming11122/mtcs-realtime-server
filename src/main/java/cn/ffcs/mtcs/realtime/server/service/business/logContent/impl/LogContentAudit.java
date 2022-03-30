package cn.ffcs.mtcs.realtime.server.service.business.logContent.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtOpsInfo;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.service.business.logContent.LogContentOps;
import cn.ffcs.mtcs.realtime.server.service.business.logContent.LogContentOpsFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author Nemo
 * @version V1.0
 * @Description: .
 * @date 2020/7/13 19:04
 */
@Component
@Slf4j
public class LogContentAudit extends LogContentOps implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        LogContentOpsFactory.registerOperation(
                OpsNameEnum.Audit.getValue(), this);
    }

    @Override
    public String getLogContent(RtOpsInfo rtOpsInfo) {
        return defaultGetNullContent(rtOpsInfo);
    }

    @Override
    public String getLogExeMachine(RtOpsInfo rtOpsInfo) throws ExistException {
        return defaultGetNullExeMachine(rtOpsInfo);
    }

    @Override
    public String getLogExeParam(RtOpsInfo rtOpsInfo) throws ExistException {
        return defaultGetNullExeMachine(rtOpsInfo);
    }
}
