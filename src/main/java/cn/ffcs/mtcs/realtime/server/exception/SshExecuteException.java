package cn.ffcs.mtcs.realtime.server.exception;

import cn.ffcs.mtcs.common.exception.BaseException;

/**
 * @Description TODO.
 * @Author Nemo
 * @Date 2019/9/2/002 18:05
 * @Version 1.0
 */
public class SshExecuteException extends BaseException {

    private static final long serialVersionUID = 3401148094940529081L;


    public SshExecuteException(String message) {
        super(message);
    }

}
