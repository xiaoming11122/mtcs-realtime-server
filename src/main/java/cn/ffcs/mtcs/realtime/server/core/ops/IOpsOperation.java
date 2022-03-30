package cn.ffcs.mtcs.realtime.server.core.ops;

import cn.ffcs.mtcs.realtime.common.entity.RtTaskLastState;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 19:05
 * @Version 1.0
 */
public interface IOpsOperation {
    /**
     * 1 启动中
     * 2 重启中
     * 3 停止中
     * 4 运行
     */

    /**
     * 1 启动中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @return
     */
    boolean start(UserPrincipal principal,
                  Long taskId, String taskVersion,
                  String engineName);

    /**
     * 2 重启中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @param retryId     用于自动重启时，重启的次数计数
     * @return
     */
    boolean restart(UserPrincipal principal,
                    Long taskId, String taskVersion,
                    String engineName,
                    Long retryId);

    /**
     * 3 停止中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @return
     */
    boolean stop(UserPrincipal principal,
                 Long taskId, String taskVersion,
                 String engineName);

    /**
     * 4 运行中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @return
     */
    boolean run(UserPrincipal principal,
                Long taskId, String taskVersion,
                String engineName);

    /**
     * 1 启动中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @param rtTaskLastState
     * @return
     */
    boolean appStart(UserPrincipal principal,
                  Long taskId, String taskVersion,
                  String engineName, RtTaskLastState rtTaskLastState);


    /**
     * 2 重启中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @param retryId     用于自动重启时，重启的次数计数
     * @return
     */
    boolean appRestart(UserPrincipal principal,
                    Long taskId, String taskVersion,
                    String engineName, RtTaskLastState rtTaskLastState,
                    Long retryId);


    /**
     * 3 停止中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @return
     */
    boolean appStop(UserPrincipal principal,
                 Long taskId, String taskVersion,
                 String engineName, RtTaskLastState rtTaskLastState);

    /**
     * 4 运行中
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @param engineName
     * @return
     */
    boolean appRun(UserPrincipal principal,
                Long taskId, String taskVersion,
                String engineName, RtTaskLastState rtTaskLastState);
}
