package cn.ffcs.mtcs.realtime.server.core.ops;

import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 19:04
 * @Version 1.0
 */
public interface IOpsLogic {
    /**
     * 1 上线
     * 2 下线
     */

    /**
     * 1 上线
     *
     * @param principal   当前用户信息
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @return
     */
    boolean online(UserPrincipal principal,
                   Long taskId, String taskVersion) throws ExistException;

    /**
     * 2 下线
     *
     * @param principal   当前用户信息
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @return
     */
    boolean offline(UserPrincipal principal,
                    Long taskId, String taskVersion) throws ExistException;

    /**
     * 检测是不是在用的任务
     *
     * @param taskId
     * @param taskVersion
     * @param taskState
     * @return
     */
    boolean checkUsingVersion(Long taskId, String taskVersion, String taskState);
}
