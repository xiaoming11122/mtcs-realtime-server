package cn.ffcs.mtcs.realtime.server.service.data;


import cn.ffcs.mtcs.version.common.entity.MdVersionManage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 元数据注册表 服务类
 * </p>
 *
 * @author Nemo
 * @since 2020-03-16
 */
public interface IMdVersionManageService extends IService<MdVersionManage> {

    IPage<MdVersionManage> pageMdVersionManageByTaskName(Page<MdVersionManage> page,
                                                         Long teamId,
                                                         Long mdType,
                                                         List<String> versionStateList,
                                                         String taskName);

    public IPage<MdVersionManage> pageMdVersionManageNew(Page<MdVersionManage> page, Long teamId, Long mdType, String taskName) ;

}
