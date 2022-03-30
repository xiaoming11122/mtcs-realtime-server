package cn.ffcs.mtcs.realtime.server.core.ops;

import cn.ffcs.mtcs.realtime.common.request.TaskSaveRequest;
import cn.ffcs.mtcs.realtime.common.vo.DetailTaskVo;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.exception.FeignException;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 19:04
 * @Version 1.0
 */
public interface IOpsData {
    /**
     * 1 保存任务信息
     * 2 修改任务信息
     * 3 删除任务信息(无）
     * 4 查询任务详细信息
     */

    /**
     * 1 保存任务信息
     *
     * @param principal
     * @param taskSaveRequest
     * @return
     * @throws FeignException
     * @throws DataOpsException
     */
    DetailTaskVo saveDetailTaskVo(UserPrincipal principal, TaskSaveRequest taskSaveRequest) throws FeignException, DataOpsException;

    /**
     * 2 修改任务信息
     *
     * @param principal
     * @param taskSaveRequest
     * @return
     * @throws FeignException
     * @throws DataOpsException
     * @throws ExistException
     */
    DetailTaskVo modifyDetailTaskVo(UserPrincipal principal, TaskSaveRequest taskSaveRequest) throws FeignException, DataOpsException, ExistException;

    /**
     * 审核任务
     *
     * @param principal
     * @param taskSaveRequest
     * @return
     * @throws FeignException
     * @throws ExistException
     * @throws DataOpsException
     */
    DetailTaskVo auditTask(UserPrincipal principal, TaskSaveRequest taskSaveRequest) throws FeignException, ExistException, DataOpsException;

    /**
     * 审核回退任务
     *
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     * @throws FeignException
     */
    boolean auditTaskCallback(UserPrincipal principal, Long taskId, String taskVersion,String auditProcessId) throws FeignException, DataOpsException;

    /**
     * 发布任务
     *
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     * @throws FeignException
     */
    boolean releaseTask(UserPrincipal principal, Long taskId, String taskVersion) throws FeignException;

    /**
     * 3 删除任务信息(无）
     */

    /**
     * 4 查询任务详细信息
     *
     * @param taskId
     * @param taskVersion
     * @return
     * @throws FeignException
     */
    DetailTaskVo getDetailTaskVo(Long taskId, String taskVersion) throws FeignException;

    /**
     * 查询任务详细信息，密码加密处理
     * @param taskId
     * @param taskVersion
     * @return
     * @throws FeignException
     */
    DetailTaskVo getShowDetailTaskVo(Long taskId, String taskVersion) throws FeignException;
}
