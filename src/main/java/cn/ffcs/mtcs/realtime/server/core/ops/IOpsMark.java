package cn.ffcs.mtcs.realtime.server.core.ops;

import cn.ffcs.mtcs.user.common.domain.UserPrincipal;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 19:15
 * @Version 1.0
 */
public interface IOpsMark {
    /**
     * 1 启动
     * 2 重启
     * 3 停止
     */


    /**
     * 1 启动
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean markStart(UserPrincipal principal,
                      Long taskId, String taskVersion);

    /**
     * 启动异常
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean startException(UserPrincipal principal,
                           Long taskId, String taskVersion);

    /**
     * 2 重启
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean markRestart(UserPrincipal principal,
                        Long taskId, String taskVersion);

    /**
     * 重启
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean restartException(UserPrincipal principal,
                             Long taskId, String taskVersion);

    /**
     * 3 标记停止
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean markStop(UserPrincipal principal,
                     Long taskId, String taskVersion);

    /**
     * 停止异常
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean stopException(UserPrincipal principal,
                          Long taskId, String taskVersion);

    /**
     * 真实停止
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean stop(UserPrincipal principal,
                 Long taskId, String taskVersion);

    /**
     * 运行异常
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean runException(UserPrincipal principal,
                         Long taskId, String taskVersion);

    /**
     * 运行异常
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean monitorException(UserPrincipal principal,
                         Long taskId, String taskVersion);
}
