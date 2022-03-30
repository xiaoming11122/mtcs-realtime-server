package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.RtOpsInfo;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.mapper.RtOpsInfoMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 操作信息表 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-04
 */
@Service
public class RtOpsInfoServiceImpl extends ServiceImpl<RtOpsInfoMapper, RtOpsInfo> implements IRtOpsInfoService {

    /**
     * 检测该任务是不是正在操作中
     * 所有操作中如果没有更新操作，并且有启动或者重启操作，说明是正在操作中的
     *
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public boolean checkUsingVersion(Long taskId, String taskVersion) {
        List<RtOpsInfo> rtOpsInfoList =
                this.baseMapper.selectList(
                        Wrappers.<RtOpsInfo>lambdaQuery()
                                .eq(RtOpsInfo::getTaskId, taskId)
                                .eq(RtOpsInfo::getTaskVersion, taskVersion)
                                .eq(RtOpsInfo::getState, RecordStateEnum.StateUse.getValue()));

        boolean flag = false;

        for (RtOpsInfo rtOpsInfo : rtOpsInfoList) {
            if (OpsNameEnum.Update.getValue().equals(rtOpsInfo.getOpsName())) {
                flag = false;
                break;
            }

            if (OpsNameEnum.StartMark.getValue().equals(rtOpsInfo.getOpsName())
                    || OpsNameEnum.RestartMark.getValue().equals(rtOpsInfo.getOpsName())) {
                flag = true;
            }
        }
        return flag;
    }
}
