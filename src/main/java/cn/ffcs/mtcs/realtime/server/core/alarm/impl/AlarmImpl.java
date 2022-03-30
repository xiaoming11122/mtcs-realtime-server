package cn.ffcs.mtcs.realtime.server.core.alarm.impl;

import cn.ffcs.mtcs.realtime.server.core.alarm.IAlarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/11/011 15:58
 * @Version 1.0
 */
@Component
@Slf4j
public class AlarmImpl implements IAlarm {
    @Override
    public boolean nullState(String message) {
        log.warn("alarm : " + message);
        return false;
    }

    @Override
    public boolean startException(String message) {
        log.warn("alarm : " + message);
        return false;
    }

    @Override
    public boolean restartException(String message) {
        log.warn("alarm : " + message);
        return false;
    }

    @Override
    public boolean stopException(String message) {
        log.warn("alarm : " + message);
        return false;
    }

    @Override
    public boolean runException(String message) {
        log.warn("alarm : " + message);
        return false;
    }
}
