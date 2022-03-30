package cn.ffcs.mtcs.realtime.server.core.ops;

import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/27/027 11:41
 * @Version 1.0
 */

public interface IOpsUpdate {
    /**
     * 1 更新
     * 只有程序在处于停止类型的状态时才能进行更新
     * 刚上线的任务，也不支持更新操作
     *
     * @param principal
     * @param taskId
     * @param taskVersionOld
     * @param taskVersionNew
     * @param engineName
     * @return
     */
    boolean update(UserPrincipal principal,
                   Long taskId, String taskVersionOld, String taskVersionNew,
                   String engineName) throws ExistException;
}
