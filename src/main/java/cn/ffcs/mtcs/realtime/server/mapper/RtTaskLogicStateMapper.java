package cn.ffcs.mtcs.realtime.server.mapper;

import cn.ffcs.mtcs.realtime.common.entity.RtTaskLogicState;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 任务逻辑状态 Mapper 接口
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
public interface RtTaskLogicStateMapper extends BaseMapper<RtTaskLogicState> {

    /**
     * @param page 翻页对象，可以作为 xml 参数直接使用，传递参数 Page 即自动分页
     * @return
     */
    IPage<RtTaskLogicState> pageRtTaskLogicStateByTaskName(Page<RtTaskLogicState> page,
                                                           @Param("teamId")Long teamId,
                                                           @Param("taskName")String taskName,
                                                           @Param("taskState")String taskState);

    IPage<RtTaskLogicState> pageRtTaskLogicStateByTaskNameOffline(Page<RtTaskLogicState> page,
                                                           @Param("teamId")Long teamId,
                                                           @Param("taskName")String taskName,
                                                           @Param("taskState")String taskState);
}
