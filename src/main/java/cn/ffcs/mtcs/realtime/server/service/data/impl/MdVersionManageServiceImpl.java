package cn.ffcs.mtcs.realtime.server.service.data.impl;


import cn.ffcs.mtcs.realtime.server.mapper.MdVersionManageMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IMdVersionManageService;
import cn.ffcs.mtcs.version.common.entity.MdVersionManage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 元数据注册表 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-16
 */
@Service
public class MdVersionManageServiceImpl extends ServiceImpl<MdVersionManageMapper, MdVersionManage> implements IMdVersionManageService {

    @Override
    public IPage<MdVersionManage> pageMdVersionManageByTaskName(Page<MdVersionManage> page, Long teamId, Long mdType, List<String> versionStateList, String taskName) {
        return this.baseMapper.pageMdVersionManageByTaskName(page, teamId, mdType, versionStateList, taskName);
    }


    @Override
    public IPage<MdVersionManage> pageMdVersionManageNew(Page<MdVersionManage> page, Long teamId, Long mdType, String taskName) {
        return this.baseMapper.pageMdVersionManageNew(page, teamId, mdType, taskName);
    }
}
