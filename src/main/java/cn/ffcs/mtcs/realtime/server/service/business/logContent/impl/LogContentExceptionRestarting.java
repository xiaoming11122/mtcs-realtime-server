package cn.ffcs.mtcs.realtime.server.service.business.logContent.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtExeInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtExeMachine;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsInfo;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.pojo.bo.ExeMachineBo;
import cn.ffcs.mtcs.realtime.server.service.business.logContent.LogContentOps;
import cn.ffcs.mtcs.realtime.server.service.business.logContent.LogContentOpsFactory;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
public class LogContentExceptionRestarting extends LogContentOps implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        LogContentOpsFactory.registerOperation(
                OpsNameEnum.ExceptionRestarting.getValue(), this);
    }

    @Override
    public String getLogContent(RtOpsInfo rtOpsInfo) throws ExistException {
        RtOpsInfo preRtOpsInfo = getLatestEffectiveOps(rtOpsInfo);

        RtOpsAttachment rtOpsAttachment = getRtOpsAttachment(preRtOpsInfo.getOpsId());
        // -1L 为默认值
        if (-1L == rtOpsAttachment.getExeId()) {
            return "该操作未能执行，无运行日志输出！";
        }

        RtExeInfo rtExeInfo = getRtExeInfo(rtOpsAttachment.getExeId());
        // todo 待升级，暂时恢复测试数据
        // 升级中，通过ssh去读取文件，将获取到数据暂时写入到redis中
        if (StringUtils.isEmpty(rtExeInfo.getExeLogFile())) {
            // throw new ExistException("获取日志内容错误, 获取该操作日志文件失败！") ;
            return "获取日志内容错误, 获取该操作日志文件失败！";
        }

        //获取执行机信息
        DetailTaskInfoBo detailTaskInfoBo =
                getDetailTaskInfoBo(rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());

        RtExeMachine executeMachine = getRtExeMachineByExeInfo(rtExeInfo.getExeMachine());
        ExeMachineBo exeMachineBo = getExeMachineBo(executeMachine, detailTaskInfoBo);

        String logContent = ssh(exeMachineBo, rtExeInfo);
        return logContent;
    }

    @Override
    public String getLogExeMachine(RtOpsInfo rtOpsInfo) throws ExistException {
        return defaultGetExeMachine(rtOpsInfo);
    }

    @Override
    public String getLogExeParam(RtOpsInfo rtOpsInfo) throws ExistException {
        return defaultGetExeParam(rtOpsInfo);
    }
}