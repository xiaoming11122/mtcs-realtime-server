package cn.ffcs.mtcs.realtime.server.service.data;

import cn.ffcs.mtcs.realtime.common.entity.RtOpsInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 操作信息表 服务类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
public interface IRtOpsInfoService extends IService<RtOpsInfo> {

    /**
     * 检测该任务是不是正在操作中
     * 所有操作中如果没有更新操作，并且有启动或者重启操作，说明是正在操作中的
     *
     * @param taskId
     * @param taskVersion
     * @return
     */
    boolean checkUsingVersion(Long taskId, String taskVersion);

}
