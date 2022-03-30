package cn.ffcs.mtcs.realtime.server.core.operation;

import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 15:40
 * @Version 1.0
 */
public interface IOperation {

    /**
     * 1 启动
     * 2 重启
     * 3 停止
     */


    /**
     * 启动
     *
     * @param principal
     * @param rtOpsAttachment
     * @return
     */
    boolean start(UserPrincipal principal, RtOpsAttachment rtOpsAttachment);

    /**
     * 重启
     *
     * @param principal
     * @param rtOpsAttachment
     * @return
     */
    boolean restart(UserPrincipal principal, RtOpsAttachment rtOpsAttachment);


    /**
     * 停止
     *
     * @param principal
     * @param rtOpsAttachment
     * @return
     */
    boolean stop(UserPrincipal principal, RtOpsAttachment rtOpsAttachment);

}
