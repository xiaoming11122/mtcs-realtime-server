package cn.ffcs.mtcs.realtime.server.service.data;

import cn.ffcs.mtcs.realtime.common.entity.RtTaskLogicState;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 任务逻辑状态 服务类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
public interface IRtTaskLogicStateService extends IService<RtTaskLogicState> {
    IPage<RtTaskLogicState> pageRtTaskLogicStateByTaskName(Page<RtTaskLogicState> page,
                                                           Long teamId,
                                                           String taskName,
                                                           String taskState);

    IPage<RtTaskLogicState> pageRtTaskLogicStateByTaskNameOffline(Page<RtTaskLogicState> page,
                                                           Long teamId,
                                                           String taskName,
                                                           String taskState);

    boolean checkLastVersion(Long taskId, String taskVersion, String taskState);


}
