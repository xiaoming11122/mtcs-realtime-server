package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtTaskLogicState;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.mapper.RtTaskLogicStateMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtTaskLogicStateService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 任务逻辑状态 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
@Service
public class RtTaskLogicStateServiceImpl extends ServiceImpl<RtTaskLogicStateMapper, RtTaskLogicState> implements IRtTaskLogicStateService {

    @Override
    public IPage<RtTaskLogicState> pageRtTaskLogicStateByTaskName(Page<RtTaskLogicState> page, Long teamId, String taskName, String taskState) {
        return this.baseMapper.pageRtTaskLogicStateByTaskName(page, teamId, taskName, taskState);
    }

    @Override
    public IPage<RtTaskLogicState> pageRtTaskLogicStateByTaskNameOffline(Page<RtTaskLogicState> page, Long teamId, String taskName, String taskState) {
        return this.baseMapper.pageRtTaskLogicStateByTaskNameOffline(page, teamId, taskName, taskState);
    }

    @Override
    public boolean checkLastVersion(Long taskId, String taskVersion, String taskState) {
        List<RtTaskLogicState> rtTaskLogicStateList =
                this.baseMapper.selectList(
                        Wrappers.<RtTaskLogicState>lambdaQuery()
                                .eq(RtTaskLogicState::getTaskId, taskId)
                                .eq(RtTaskLogicState::getTaskState, taskState)
                                .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue())
                                .orderByDesc(RtTaskLogicState::getStateId)
                );

        if (null != rtTaskLogicStateList && rtTaskLogicStateList.get(0).getTaskVersion().equals(taskVersion)) {
            return true;
        } else {
            return false;
        }
    }
}
