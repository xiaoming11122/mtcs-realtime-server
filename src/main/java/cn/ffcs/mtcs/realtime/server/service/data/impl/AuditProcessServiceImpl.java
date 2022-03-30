package cn.ffcs.mtcs.realtime.server.service.data.impl;

import cn.ffcs.mtcs.realtime.server.entity.AuditProcess;
import cn.ffcs.mtcs.realtime.server.mapper.AuditProcessMapper;
import cn.ffcs.mtcs.realtime.server.service.data.IAuditProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AuditProcessServiceImpl extends ServiceImpl<AuditProcessMapper, AuditProcess> implements IAuditProcessService {

}
