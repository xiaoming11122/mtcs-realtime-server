package cn.ffcs.mtcs.realtime.server.exception;

import cn.ffcs.mtcs.common.exception.BaseException;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/9/009 14:39
 * @Version 1.0
 */

public class FeignException extends BaseException {

    private static final long serialVersionUID = 3401148094940529081L;

    public FeignException(String message) {
        super(message);
    }

}