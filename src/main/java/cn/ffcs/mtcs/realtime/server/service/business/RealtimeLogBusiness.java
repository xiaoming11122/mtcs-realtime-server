package cn.ffcs.mtcs.realtime.server.service.business;

import cn.ffcs.common.basic.dao.JschShellDao;
import cn.ffcs.common.basic.security.SecurityFactory;
import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.common.vo.LogTimeAxisVo;
import cn.ffcs.mtcs.realtime.server.constants.*;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.exception.SshExecuteException;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.service.business.logContent.LogContentOps;
import cn.ffcs.mtcs.realtime.server.service.business.logContent.LogContentOpsFactory;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/8/008 11:39
 * @Version 1.0
 */
@Component
@Slf4j
public class RealtimeLogBusiness {

    @Autowired
    private IRtOpsInfoService opsInfoService;

    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;

    @Autowired
    private IRtExeInfoService exeInfoService;

    @Autowired
    private UserPrincipal userPrincipal;

    @Value("${spring.application.name}")
    private String AppServerName;

//    @Autowired
//    private SshServerFeign sshServerFeign;

    @Autowired
    private IDetailTaskInfoBoService detailTaskInfoBoService;

    @Autowired
    private IRtExeMachineEnvService rtExeMachineEnvService;

    @Autowired
    private IRtExeInfoService rtExeInfoService;

    /**
     * 1 日志-时间轴
     *
     * @param taskId
     * @param taskVersion
     * @return
     */
    public List<LogTimeAxisVo> getLogTimeAxis(Long taskId, String taskVersion) {
        Page<RtOpsInfo> rtOpsInfoPage = new Page<>(1, 150);
        opsInfoService.page(
                rtOpsInfoPage,
                Wrappers.<RtOpsInfo>lambdaQuery()
                        .eq(RtOpsInfo::getTaskId, taskId)
                        .eq(RtOpsInfo::getTaskVersion, taskVersion)
                        .eq(RtOpsInfo::getState, RecordStateEnum.StateUse.getValue())
                        .orderByDesc(RtOpsInfo::getOpsId));

        List<RtOpsInfo> rtOpsInfoList = rtOpsInfoPage.getRecords();

        List<LogTimeAxisVo> logTimeAxisVoList = new ArrayList<>();
        for (RtOpsInfo rtOpsInfo : rtOpsInfoList) {
            logTimeAxisVoList.add(getLogTimeAxis(rtOpsInfo));
        }

        return logTimeAxisVoList;
    }

    private LogTimeAxisVo getLogTimeAxis(RtOpsInfo rtOpsInfo) {
        LogTimeAxisVo logTimeAxisVo = new LogTimeAxisVo();
        logTimeAxisVo.setOpsUser(getUserName(rtOpsInfo.getOpsUser()));
        logTimeAxisVo.setOpsName(rtOpsInfo.getOpsName());
        logTimeAxisVo.setOpsNameShow(OpsNameEnum.getShowByValue(rtOpsInfo.getOpsName()));
        logTimeAxisVo.setOpsTime(rtOpsInfo.getCrtTime().format(CommonConstants.dateTimeFormatter));
        logTimeAxisVo.setNormalFlag(isNormal(rtOpsInfo.getOpsName()));
        logTimeAxisVo.setOpsId(rtOpsInfo.getOpsId());
        return logTimeAxisVo;
    }

    @Autowired
    private ISysUserService sysUserService;

    private String getUserName(Long userId) {
        SysUser sysUser = sysUserService.getById(userId);
        return sysUser.getUsername();
    }

    /**
     * @param opsName
     * @return
     */
    private boolean isNormal(String opsName) {
        List<String> exceptionOpsName =
                Arrays.asList(
                        OpsNameEnum.StartException.getValue(),
                        OpsNameEnum.RestartException.getValue(),
                        OpsNameEnum.StopException.getValue(),
                        OpsNameEnum.RunException.getValue());
        return !exceptionOpsName.contains(opsName);
    }

    /**
     * 2 日志-日志内容
     *
     * @param opsId
     * @return
     */
    public Map<String, String> getLogContent(Long opsId, String line) throws ExistException {
        RtOpsInfo rtOpsInfo =
                opsInfoService.getById(opsId);
        Map<String, String> resultMap = new HashMap<>();
        if (null == rtOpsInfo) {
            throw new ExistException("获取日志内容错误，不存在该操作！");
        }

        LogContentOps logContentOps =
                LogContentOpsFactory.getLogContentOps(rtOpsInfo.getOpsName());
        if (logContentOps == null) {
            throw new ExistException("获取日志内容错误，不存在该操作名！");
        }

        //设置起始行数属性
        rtOpsInfo.setOpsAttr(line);
        //added at 20210725
        // return logContentOps.defaultGetContent2(rtOpsInfo);
        return logContentOps.defaultGetContentByLogFile(rtOpsInfo);
    }


    /*public Map<String,String> logDownload(Long opsId) throws ExistException {
        RtOpsAttachment rtOpsAttachment =
                opsAttachmentService.getById(opsId);
        if (rtOpsAttachment != null && rtOpsAttachment.getExeId() != -1) {
            RtExeInfo rtExeInfo =  exeInfoService.getById(rtOpsAttachment.getExeId());
            if (rtExeInfo != null && StringUtils.isNotEmpty(rtExeInfo.getAppId())) {

            }
        }
        LogContentOps logContentOps =
                LogContentOpsFactory.getLogContentOps(rtOpsInfo.getOpsName());
        if (logContentOps == null) {
            throw new ExistException("获取日志内容错误，不存在该操作名！");
        }
        Map<String,String> resultMap=new HashMap<>();
        if (null == rtOpsInfo) {
            throw new ExistException("获取日志内容错误，不存在该操作！");
        }



        //设置起始行数属性
        rtOpsInfo.setOpsAttr(line);
        //added at 20210725
        // return logContentOps.defaultGetContent2(rtOpsInfo);
        return logContentOps.defaultGetContentByLogFile(rtOpsInfo);
    }
*/

    /**
     * 3 日志-执行机
     *
     * @param opsId
     * @return
     */
    public String getLogExeMachine(Long opsId) throws ExistException {
        RtOpsInfo rtOpsInfo =
                opsInfoService.getById(opsId);
        if (null == rtOpsInfo) {
            throw new ExistException("获取日志内容错误，不存在该操作！");
        }

        LogContentOps logContentOps =
                LogContentOpsFactory.getLogContentOps(rtOpsInfo.getOpsName());
        if (logContentOps == null) {
            throw new ExistException("获取日志内容错误，不存在该操作名！");
        }
        return logContentOps.getLogExeMachine(rtOpsInfo);

    }

    /**
     * 4 日志-执行json
     *
     * @param opsId
     * @return
     */
    public String getLogExeParam(Long opsId) throws ExistException {
        RtOpsInfo rtOpsInfo =
                opsInfoService.getById(opsId);
        if (null == rtOpsInfo) {
            throw new ExistException("获取日志内容错误，不存在该操作！");
        }

        LogContentOps logContentOps =
                LogContentOpsFactory.getLogContentOps(rtOpsInfo.getOpsName());
        if (logContentOps == null) {
            throw new ExistException("获取日志内容错误，不存在该操作名！");
        }
        return logContentOps.securityGetExeParam(rtOpsInfo);
    }

    private static String DEFAULT_CRYPT_KEY = "k38d81C!@#dkro22232JAMDGIJGDSe48dk>KUY%%$";

    private static Integer SSH_TIME_OUT = 60000;

    /**
     * 7 下载
     *
     * @param opsId
     * @param response
     * @return
     */
    public Boolean logDownload(Long opsId, HttpServletResponse response) throws SshExecuteException, ExistException {
        /**
         * 1. 通过opsId得到taskId rt_ops_attachment
         * 2. 通过taskid得到machineId  rt_task_machine_relative
         * 3. 通过machineId得到配置信息 krb5conf keytab  rt_exe_machine_env
         * 4. 生成ssh得到日志文件
         * 5. 日志文件写出下载
         */
        RtOpsAttachment rtOpsAttachment = opsAttachmentService.getOne(Wrappers.<RtOpsAttachment>lambdaQuery()
                .eq(RtOpsAttachment::getOpsId, opsId));
        if (ObjectUtils.isNull(rtOpsAttachment)) {
            throw new ExistException("操作信息不存在");
        }
        DetailTaskInfoBo taskDetailInfoBo = detailTaskInfoBoService.getTaskDetailInfoBo(rtOpsAttachment.getTaskId(), rtOpsAttachment.getTaskVersion());
        if (ObjectUtils.isNull(taskDetailInfoBo)) {
            throw new ExistException("任务信息不存在");
        }
        RtExeMachine rtExeMachine = taskDetailInfoBo.getExeMachineBoList().get(0).getRtExeMachine();
        if (ObjectUtils.isNull(rtExeMachine)) {
            throw new ExistException("执行机信息不存在");
        }
        String decodePassword = SecurityFactory.decode(SecurityFactory.DesRandom, rtExeMachine.getPassWord(), DEFAULT_CRYPT_KEY);

        JschShellDao jschShellDao = null;
        try {
            jschShellDao = new JschShellDao();
            jschShellDao.connect(rtExeMachine.getIpAddress(), Integer.parseInt(rtExeMachine.getConnectPort()),
                    rtExeMachine.getUserName(), decodePassword, SSH_TIME_OUT);
            String cmd = "";
            // 得到 keytab principal
            if (!EngineTypeEnum.Flume.getValue().equals(taskDetailInfoBo.getRtFlowInfo().getEngine())) {
                RtExeInfo rtExeInfo = rtExeInfoService.getOne(Wrappers.<RtExeInfo>lambdaQuery().eq(RtExeInfo::getOpsAttachmentId, rtOpsAttachment.getId()));
                List<RtExeMachineEnv> rtExeMachineEnvList = rtExeMachineEnvService.getRtExeMachineEnvList(rtExeMachine.getMachineId());
                Map<String, String> map = rtExeMachineEnvList.stream().collect(Collectors.toMap(RtExeMachineEnv::getEnvKey, RtExeMachineEnv::getEnvValue));
                String keytab = map.get(ExeMachineKeyEnum.Keytab.getValue());
                String principal = map.get(ExeMachineKeyEnum.Principal.getValue());
                cmd = "kinit -kt " + keytab + " " + principal + "\n" + "yarn logs -applicationId " + rtExeInfo.getAppId() + "\n";
            } else {
                cmd = "cat " + rtOpsAttachment.getExeLogFile() + "\n";
            }
            log.debug("cmd={}", cmd);

            JSONObject excute = jschShellDao.excute(cmd);
            String fileName = rtOpsAttachment.getExeLogFile().substring(rtOpsAttachment.getExeLogFile().lastIndexOf("/") + 1);
            // 设置强制下载不打开
            response.setContentType("application/force-download");
            // 设置文件名
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream os = null;
            try {
                os = response.getOutputStream();
                os.write(excute.get("stdMsg").toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                throw new ExistException("日志数据写出异常，请再次重试");
            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        } finally {
            if (jschShellDao != null) {
                jschShellDao.close();
            }
        }
        return true;
    }
}
