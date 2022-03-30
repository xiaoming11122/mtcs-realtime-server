package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.SysUser;
import cn.ffcs.mtcs.realtime.server.mapper.SysUserMapper;
import cn.ffcs.mtcs.realtime.server.service.data.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-04-02
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

}
