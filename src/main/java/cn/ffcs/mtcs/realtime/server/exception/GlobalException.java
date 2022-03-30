package cn.ffcs.mtcs.realtime.server.exception;

import cn.ffcs.mtcs.common.response.RetMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 陈张圣添加
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalException {
    /**
     * ExistException异常全局捕获
     *
     * @param e
     * @return
     */
    @ExceptionHandler(ExistException.class)
    @ResponseBody
    public RetMsg error(ExistException e) {
        e.printStackTrace();
        RetMsg r = new RetMsg();
        r.setSuccess(false);
        r.setMsg(e.getMessage());
        return r;
    }

    /**
     * FeignException异常全局捕获
     *
     * @param e
     * @return
     */
    @ExceptionHandler(FeignException.class)
    @ResponseBody
    public RetMsg error(FeignException e) {
        e.printStackTrace();
        RetMsg r = new RetMsg();
        r.setSuccess(false);
        r.setMsg(e.getMessage());
        return r;
    }

    /**
     * DataOpsException异常全局捕获
     *
     * @param e
     * @return
     */
    @ExceptionHandler(DataOpsException.class)
    @ResponseBody
    public RetMsg error(DataOpsException e) {
        e.printStackTrace();
        RetMsg r = new RetMsg();
        r.setSuccess(false);
        r.setMsg(e.getMessage());
        return r;
    }

    /**
     * SshExecuteException异常全局捕获
     *
     * @param e
     * @return
     */
    @ExceptionHandler(SshExecuteException.class)
    @ResponseBody
    public RetMsg error(SshExecuteException e) {
        e.printStackTrace();
        RetMsg r = new RetMsg();
        r.setSuccess(false);
        r.setMsg(e.getMessage());
        return r;
    }

}
