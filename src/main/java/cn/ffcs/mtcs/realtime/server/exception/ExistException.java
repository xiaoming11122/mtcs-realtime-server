package cn.ffcs.mtcs.realtime.server.exception;

import cn.ffcs.mtcs.common.exception.BaseException;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/20/020 17:06
 * @Version 1.0
 */
public class ExistException extends BaseException {

    private static final long serialVersionUID = 3401148094940529081L;

    public ExistException(String message) {
        super(message);
    }

}
