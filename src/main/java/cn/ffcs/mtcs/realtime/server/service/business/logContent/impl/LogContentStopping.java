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
public class LogContentStopping extends LogContentOps implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        LogContentOpsFactory.registerOperation(
                OpsNameEnum.Stopping.getValue(), this);
    }

    @Override
    public String getLogContent(RtOpsInfo rtOpsInfo) throws ExistException {
        RtOpsInfo preRtOpsInfo = getLatestEffectiveOps(rtOpsInfo);
        return defaultGetContent(preRtOpsInfo);
    }

    @Override
    public String getLogExeMachine(RtOpsInfo rtOpsInfo) throws ExistException {
        RtOpsInfo preRtOpsInfo = getLatestEffectiveOps(rtOpsInfo);
        return defaultGetExeMachine(preRtOpsInfo);
    }

    @Override
    public String getLogExeParam(RtOpsInfo rtOpsInfo) throws ExistException {
        RtOpsInfo preRtOpsInfo = getLatestEffectiveOps(rtOpsInfo);
        return defaultGetExeParam(preRtOpsInfo);
    }

}
