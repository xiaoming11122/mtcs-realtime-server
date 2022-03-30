package cn.ffcs.mtcs.realtime.server.exception;

import cn.ffcs.mtcs.common.exception.BaseException;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/3/27/027 11:24
 * @Version 1.0
 */
public class DataOpsException extends BaseException {

    private static final long serialVersionUID = 3401148094940529081L;

    public DataOpsException(String message) {
        super(message);
    }

}