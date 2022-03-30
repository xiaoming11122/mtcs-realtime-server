package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.common.entity.PfTeam;
import cn.ffcs.mtcs.realtime.server.mapper.PfTeamMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IPfTeamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 团队表 服务实现类
 * </p>
 *
 * @author Nemo
 * @since 2020-04-02
 */
@Service
public class PfTeamServiceImpl extends ServiceImpl<PfTeamMapper, PfTeam> implements IPfTeamService {

}
