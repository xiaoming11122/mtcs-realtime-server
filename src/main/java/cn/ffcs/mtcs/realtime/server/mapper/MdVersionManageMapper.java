package cn.ffcs.mtcs.realtime.server.mapper;


import cn.ffcs.mtcs.version.common.entity.MdVersionManage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 元数据注册表 Mapper 接口
 * </p>
 *
 * @author Nemo
 * @since 2020-03-16
 */
public interface MdVersionManageMapper extends BaseMapper<MdVersionManage> {

    IPage<MdVersionManage> pageMdVersionManageByTaskName(Page<MdVersionManage> page,
                                                         @Param("teamId") Long teamId,
                                                         @Param("mdType") Long mdType,
                                                         @Param("versionStateList") List<String> versionStateList,
                                                         @Param("taskName") String taskName);

    IPage<MdVersionManage> pageMdVersionManageNew(Page<MdVersionManage> page,
                                                         @Param("teamId") Long teamId,
                                                         @Param("mdType") Long mdType,
                                                         @Param("taskName") String taskName);
}