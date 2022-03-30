package cn.ffcs.mtcs.realtime.server.service.business.logContent;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.config.BusinessBeanConfig;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.pojo.bean.flink.FlinkParam;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.pojo.bo.ExeMachineBo;
import cn.ffcs.mtcs.realtime.server.service.data.IDetailTaskInfoBoService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtExeInfoService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsAttachmentService;
import cn.ffcs.mtcs.realtime.server.service.data.IRtOpsInfoService;
import cn.ffcs.mtcs.realtime.server.util.RealTimeSsh;
import cn.ffcs.mtcs.ssh.common.constants.SshExeType;
import cn.ffcs.mtcs.ssh.common.request.ExeInfo;
import cn.ffcs.mtcs.ssh.common.request.ExecuteMachine;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Nemo
 * @version V1.0
 * @Description: .
 * @date 2020/7/13 18:01
 */
@Slf4j
public abstract class LogContentOps {

    public abstract String getLogContent(RtOpsInfo rtOpsInfo) throws ExistException;

    public abstract String getLogExeMachine(RtOpsInfo rtOpsInfo) throws ExistException;

    public abstract String getLogExeParam(RtOpsInfo rtOpsInfo) throws ExistException;


    @Autowired
    private IRtOpsInfoService opsInfoService;

    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;

    @Autowired
    private IRtExeInfoService exeInfoService;

    @Autowired
    private IDetailTaskInfoBoService detailTaskInfoBoService;

    @Autowired
    private BusinessBeanConfig businessBeanConfig;


    @Autowired
    private IService<RtExeMetaInfo> exeMetaInfoService;


    private RtExeMetaInfo getRtExeMetaInfo(Long taskId, String taskVersion) {
        RtExeMetaInfo rtExeMetaInfo =
                exeMetaInfoService.getOne(
                        Wrappers.<RtExeMetaInfo>lambdaQuery()
                                .eq(RtExeMetaInfo::getTaskId, taskId)
                                .eq(RtExeMetaInfo::getTaskVersion, taskVersion)
                                .eq(RtExeMetaInfo::getState, RecordStateEnum.StateUse.getValue()),
                        false);

        return rtExeMetaInfo;
    }

    protected String defaultGetNullContent(RtOpsInfo rtOpsInfo) {
        return "该操作没有新运行日志输出！";
    }

    public String defaultGetContent(RtOpsInfo rtOpsInfo) throws ExistException {
        RtOpsAttachment rtOpsAttachment = getRtOpsAttachment(rtOpsInfo.getOpsId());


        if (rtOpsAttachment == null) {
            return "该操作无日志输出！";
        }
        // -1L 为默认值
        if (-1L == rtOpsAttachment.getExeId()) {
            return "该操作正在执行中，还未有日志内容！";
        }

        //改成从rtOpsAttachment 获取file
        RtExeInfo rtExeInfo = new RtExeInfo();
        rtExeInfo.setExeMachine(rtOpsAttachment.getExeMachine());
        rtExeInfo.setExeLogFile(rtOpsAttachment.getExeLogFile());
        rtExeInfo.setExeParam(rtOpsAttachment.getOpsParam());

        // todo 待升级，暂时恢复测试数据
        // 升级中，通过ssh去读取文件，将获取到数据暂时写入到redis中
        if (StringUtils.isEmpty(rtExeInfo.getExeLogFile())) {
            // throw new ExistException("获取日志内容错误, 获取该操作日志文件失败！");
            return "获取日志内容错误, 获取该操作日志文件失败！";
        }

        //获取执行机信息
        DetailTaskInfoBo detailTaskInfoBo =
                getDetailTaskInfoBo(rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());

        RtExeMachine executeMachine = getRtExeMachineByExeInfo(rtExeInfo.getExeMachine());
        ExeMachineBo exeMachineBo = getExeMachineBo(executeMachine, detailTaskInfoBo);

        //设置起始行数属性
        rtExeInfo.setOpsAttr(rtOpsInfo.getOpsAttr());
        String logContent = ssh(exeMachineBo, rtExeInfo);

        return logContent;
    }

    protected String defaultGetNullExeMachine(RtOpsInfo rtOpsInfo) {
        return "该操作无执行机信息！";
    }

    protected String defaultGetExeMachine(RtOpsInfo rtOpsInfo) throws ExistException {
        String exeMachineInfo = "无有效数据";
        RtOpsAttachment rtOpsAttachment = getRtOpsAttachment(rtOpsInfo.getOpsId());

        RtExeMetaInfo rtExeMetaInfo = getRtExeMetaInfo(rtOpsInfo.getTaskId(),
                rtOpsInfo.getTaskVersion());
        if (rtExeMetaInfo != null && StringUtils.isNotEmpty(rtExeMetaInfo.getExeMachine())) {
            String exeMachine = rtExeMetaInfo.getExeMachine();
            RtExeMachine executeMachine = getRtExeMachineByExeInfo(exeMachine);
            executeMachine.setPassWord("******");
            exeMachineInfo = JSON.toJSONString(executeMachine);
        } else {
            // -1L 为默认值
            if (-1L == rtOpsAttachment.getExeId()) {
                return "该操作正在执行中，还未有执行机信息！";
            }

            RtExeInfo rtExeInfo = getRtExeInfo(rtOpsAttachment.getExeId());


            // todo 待升级，暂时恢复测试数据
            // 升级中，通过ssh去读取文件，将获取到数据暂时写入到redis中
            if (rtExeInfo == null || StringUtils.isEmpty(rtExeInfo.getExeMachine())) {
                // throw new ExistException("获取执行机内容错误, 获取该操作执行机内容失败！");
                return "获取执行机内容错误, 获取该操作执行机内容失败！";
            }

            //获取执行机信息
            DetailTaskInfoBo detailTaskInfoBo =
                    getDetailTaskInfoBo(rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());

            RtExeMachine executeMachine = getRtExeMachineByExeInfo(rtExeInfo.getExeMachine());
            // 将密码设置为加密符号
            executeMachine.setPassWord("******");
            exeMachineInfo = JSON.toJSONString(executeMachine);
        }


        return exeMachineInfo;
    }

    protected String defaultGetNullExeParam(RtOpsInfo rtOpsInfo) {
        return "该操作无执行参数信息！";
    }

    protected String defaultGetExeParam(RtOpsInfo rtOpsInfo) throws ExistException {
        RtOpsAttachment rtOpsAttachment = getRtOpsAttachment(rtOpsInfo.getOpsId());
        String exeParam = "";
        if (rtOpsAttachment != null && StringUtils.isNotEmpty(rtOpsAttachment.getOpsParam())) {
            exeParam = rtOpsAttachment.getOpsParam();

        } else {

            // -1L 为默认值
            if (-1L == rtOpsAttachment.getExeId()) {
                return "该操作正在执行中，还未有执行参数信息！";
            }

            RtExeInfo rtExeInfo = getRtExeInfo(rtOpsAttachment.getExeId());
            // todo 待升级，暂时恢复测试数据
            // 升级中，通过ssh去读取文件，将获取到数据暂时写入到redis中
            if (rtExeInfo == null || StringUtils.isEmpty(rtExeInfo.getExeParam())) {
                // throw new ExistException("获取执行参数内容错误, 获取该操作执行参数内容失败！");
                exeParam = "获取执行参数内容错误, 获取该操作执行参数内容失败！";
            } else {
                exeParam = rtExeInfo.getExeParam();
            }
        }
        return exeParam;
    }

    public String securityGetExeParam(RtOpsInfo rtOpsInfo) throws ExistException {
        RtOpsAttachment rtOpsAttachment = getRtOpsAttachment(rtOpsInfo.getOpsId());
        String exeParam = "";
        if (ObjectUtils.isNull(rtOpsAttachment)) {
            return "该任务的操作信息为空";
        }
        if (StringUtils.isNotEmpty(rtOpsAttachment.getOpsParam())) {
            exeParam = rtOpsAttachment.getOpsParam();
        } else {
            // -1L 为默认值
            if (-1L == rtOpsAttachment.getExeId()) {
                return "该操作正在执行中，还未有执行参数信息！";
            }
            RtExeInfo rtExeInfo = getRtExeInfo(rtOpsAttachment.getExeId());
            // todo 待升级，暂时恢复测试数据
            // 升级中，通过ssh去读取文件，将获取到数据暂时写入到redis中
            if (rtExeInfo == null || StringUtils.isEmpty(rtExeInfo.getExeParam())) {
                // throw new ExistException("获取执行参数内容错误, 获取该操作执行参数内容失败！");
                exeParam = "获取执行参数内容错误, 获取该操作执行参数内容失败！";
            } else {
                exeParam = rtExeInfo.getExeParam();
            }
        }
        // TODO 陈张圣-实现密码加密处理
        FlinkParam flinkParam = JSON.parseObject(exeParam, FlinkParam.class);
        if (ObjectUtils.isNotEmpty(flinkParam.getPluginContentList())) {
            String passwordString = ".*'.*password[\\s]*'[\\s]*=[\\s]*'(.*)'";
            List<String> stringList = flinkParam.getPluginContentList().stream().map(item -> {
                if (StringUtils.isEmpty(item)) {
                    return "";
                }
                // System.out.println("-----");
                // System.out.println(item);
                Pattern passwordPattern = Pattern.compile(passwordString);
                Matcher passwordMatcher = passwordPattern.matcher(item);
                if (passwordMatcher.find()) {
                    passwordMatcher.reset();
                    while (passwordMatcher.find()) {
                        // System.out.println("0:" + m.group(0));
                        // System.out.println("1:" + m.group(1));
                        String newReplace = passwordMatcher.group(0).replace(passwordMatcher.group(1), "******");
                        return item.replace(passwordMatcher.group(0), newReplace);
                    }
                }
                // System.out.println("-----");
                return item;
            }).collect(Collectors.toList());
            flinkParam.setPluginContentList(stringList);
            return JSONObject.toJSONString(flinkParam);
        }
        return exeParam;
    }


    protected RtOpsInfo getLatestEffectiveOps(RtOpsInfo rtOpsInfo) {
        List<String> haveOpsLog =
                Arrays.asList(
                        OpsNameEnum.Starting.getValue(),
                        OpsNameEnum.Restarting.getValue());

        if (haveOpsLog.contains(rtOpsInfo.getOpsName())) {
            // 这两个直接使用opsId就可以查询到，不需要向前查询
            return null;
        }

        RtOpsInfo preRtOpsInfo = null;
        preRtOpsInfo =
                opsInfoService.getOne(
                        Wrappers.<RtOpsInfo>lambdaQuery()
                                .eq(RtOpsInfo::getTaskId, rtOpsInfo.getTaskId())
                                .eq(RtOpsInfo::getTaskVersion, rtOpsInfo.getTaskVersion())
                                .eq(RtOpsInfo::getState, RecordStateEnum.StateUse.getValue())
                                .lt(RtOpsInfo::getOpsId, rtOpsInfo.getOpsId())
                                .in(RtOpsInfo::getOpsName, haveOpsLog)
                                .orderByDesc(RtOpsInfo::getOpsId),
                        false);
        return preRtOpsInfo;
    }


    protected RtOpsAttachment getRtOpsAttachment(long opsId) {
        RtOpsAttachment rtOpsAttachment =
                opsAttachmentService.getOne(
                        Wrappers.<RtOpsAttachment>lambdaQuery()
                                .eq(RtOpsAttachment::getOpsId, opsId)
                                .eq(RtOpsAttachment::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        return rtOpsAttachment;
    }

    protected RtExeInfo getRtExeInfo(long exeInfo) {
        return exeInfoService.getById(exeInfo);
    }

    protected DetailTaskInfoBo getDetailTaskInfoBo(long taskId, String taskVersion) {
        return detailTaskInfoBoService.getTaskDetailInfoBo(taskId, taskVersion);
    }

    protected RtExeMachine getRtExeMachineByExeInfo(String exeMachineJson) {
        if (exeMachineJson.startsWith("[")) {
            List<RtExeMachine> rtExeMachineList = JSON.parseArray(exeMachineJson, RtExeMachine.class);
            Random random = new Random();
            return rtExeMachineList.get(random.nextInt(rtExeMachineList.size()));
        } else /*if (exeMachineJson.startsWith("{"))*/ {
            return JSON.parseObject(exeMachineJson, RtExeMachine.class);
        }
    }

    protected ExeMachineBo getExeMachineBo(RtExeMachine executeMachine, DetailTaskInfoBo detailTaskInfoBo) {
        List<ExeMachineBo> exeMachineBoList = detailTaskInfoBo.getExeMachineBoList();
        for (ExeMachineBo exeMachineBo : exeMachineBoList) {
            if (contrastExeMachine(executeMachine, exeMachineBo)) {
                return exeMachineBo;
            }
        }
        return null;
    }

    protected boolean contrastExeMachine(RtExeMachine executeMachine, ExeMachineBo exeMachineBo) {
        log.debug("----------------------------");
        log.debug(executeMachine.toString());
        log.debug("----------------------------");
        log.debug(exeMachineBo.toString());
        log.debug("----------------------------");

        if (executeMachine.getMachineId().equals(exeMachineBo.getRtExeMachine().getMachineId())
                && executeMachine.getIpAddress().trim().equals(exeMachineBo.getRtExeMachine().getIpAddress().trim())) {
            return true;
        }
        return false;
    }

   /* @Autowired
    private SshServerFeign sshServerFeign;*/


    private final int EXE_COUNT_DEFAULT = 3;

    protected String ssh(ExeMachineBo exeMachineBo, RtExeInfo rtExeInfo) {
        SshParamRequest sshParamRequest = createSshParamRequest(exeMachineBo, rtExeInfo);
        log.debug("--------------");
        log.debug(sshParamRequest.toString());
        log.debug("--------------");
        String resultcontent = "读取日志失败，请重试或联系管理员。";
        try {
            //调整直接通过物理方式获取
            JSONObject jsonObject = RealTimeSsh.executeParam(sshParamRequest, EXE_COUNT_DEFAULT);
            String stdMsg = (String) jsonObject.get("stdMsg");
            String errMsg = (String) jsonObject.get("errMsg");
            int resCode = (int) jsonObject.get("resCode");
            if (resCode == 0 || resCode == 1) {
                resultcontent = stdMsg;
            } else {
                resultcontent = resultcontent + errMsg;
            }
            log.debug(String.format("[日志信息]：%s", resultcontent));
        } catch (Exception e) {
            e.printStackTrace();
        }
       /* RetDataMsg retDataMsg = sshServerFeign.ssh(sshParamRequest);
        if (retDataMsg.getSuccess()) {
            resultcontent = retDataMsg.getData().toString();
        }*/
        return resultcontent;
    }


    @Autowired
    private UserPrincipal userPrincipal;

    @Value("${spring.application.name}")
    private String AppServerName;

    private SshParamRequest createSshParamRequest(ExeMachineBo exeMachineBo,
                                                  RtExeInfo rtExeInfo) {
        SshParamRequest sshParamRequest = new SshParamRequest();
        sshParamRequest.setExeServer(AppServerName);
        sshParamRequest.setExeUser(userPrincipal.getUserId());
        sshParamRequest.setExeInfoList(getExeInfoList(exeMachineBo, rtExeInfo));
        // 因为不需要参数
        sshParamRequest.setExeParams("");
        return sshParamRequest;
    }

    private List<ExeInfo> getExeInfoList(ExeMachineBo exeMachineBo,
                                         RtExeInfo rtExeInfo) {
        ExeInfo exeInfo = new ExeInfo();
        exeInfo.setExecuteMachine(getExecuteMachine(exeMachineBo.getRtExeMachine()));
        // 修改这个执行信息中的相关信息
        // 主要是用使用这个里面的执行机信息
        //exeInfo.setExecuteMachine();
        exeInfo.setExeType(SshExeType.ExeCatFile.getExeType());
        exeInfo.setExeCommand(getCommand(rtExeInfo));
        return Arrays.asList(exeInfo);
    }

    private ExecuteMachine getExecuteMachine(RtExeMachine rtExeMachine) {
        ExecuteMachine executeMachine = new ExecuteMachine();
        executeMachine.setIp(rtExeMachine.getIpAddress());
        executeMachine.setPort(rtExeMachine.getConnectPort());
        executeMachine.setUser(rtExeMachine.getUserName());
        executeMachine.setPassword(rtExeMachine.getPassWord());
        return executeMachine;
    }

    private String getCommand(RtExeInfo rtExeInfo) {
        //每次读取行数
        String maxline = businessBeanConfig.getmaxline();
        //默认200
        long line = 200;
        if (null != maxline && !"".equals(maxline)) {
            //项目有配置则取项目配置
            line = Long.parseLong(maxline);
        }
        if (null != rtExeInfo.getOpsAttr() && !"".equals(rtExeInfo.getOpsAttr())) {
            //起始行数
            long starline = Long.parseLong(rtExeInfo.getOpsAttr());
            //结束行数
            long endline = starline + line;
            String command = " sed -n '" + starline + "," + endline + "p' " + rtExeInfo.getExeLogFile();
            return command;
        } else {
            String command = "tail -n " + line + rtExeInfo.getExeLogFile();
            return command;
        }

    }

    public Map<String, String> defaultGetContentByLogFile(RtOpsInfo rtOpsInfo) throws ExistException {


        RtOpsAttachment rtOpsAttachment = getRtOpsAttachment(rtOpsInfo.getOpsId());
        Map<String, String> resultMap = new HashMap<>();

        // 新增异常重启中状态，打印的日志同最新的有效操作一样
        if (rtOpsInfo.getOpsName().equals(OpsNameEnum.ExceptionRestarting.getValue())) {
            RtOpsInfo preRtOpsInfo = getLatestEffectiveOps(rtOpsInfo);
            rtOpsAttachment = getRtOpsAttachment(preRtOpsInfo.getOpsId());
        }

        if (rtOpsAttachment == null || StringUtils.isEmpty(rtOpsAttachment.getExeLogFile())) {
            resultMap.put("msg", "该操作无日志输出！");
            resultMap.put("success", "false");
            return resultMap;
        }

       /* // -1L 为默认值
        if (-1L == rtOpsAttachment.getExeId()) {
            resultMap.put("msg","该操作正在执行中，还未有日志内容！");
            resultMap.put("success","false");
            return resultMap;
        }*/

        //改成从rtOpsAttachment 获取file
        RtExeInfo rtExeInfo = new RtExeInfo();
        rtExeInfo.setExeMachine(rtOpsAttachment.getExeMachine());
        if (StringUtils.isNotEmpty(rtOpsAttachment.getExeLogFile())) {
            rtExeInfo.setExeLogFile(rtOpsAttachment.getExeLogFile());
        }
        rtExeInfo.setExeParam(rtOpsAttachment.getOpsParam());

        // todo 待升级，暂时恢复测试数据
        if (StringUtils.isEmpty(rtExeInfo.getExeLogFile())) {
            // throw new ExistException("获取日志内容错误, 获取该操作日志文件失败！");
            resultMap.put("msg", "获取日志内容错误, 获取该操作日志文件失败！");
            resultMap.put("success", "false");
            return resultMap;
        }

        //获取执行机信息
        DetailTaskInfoBo detailTaskInfoBo =
                getDetailTaskInfoBo(rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());

        RtExeMachine executeMachine = getRtExeMachineByExeInfo(rtExeInfo.getExeMachine());


        ExeMachineBo exeMachineBo = getExeMachineBo(executeMachine, detailTaskInfoBo);

        //设置起始行数属性
        rtExeInfo.setOpsAttr(rtOpsInfo.getOpsAttr());
        String logContent = ssh(exeMachineBo, rtExeInfo);
        resultMap.put("msg", logContent);
        resultMap.put("success", "true");

        return resultMap;
    }

    public Map<String, String> defaultGetContent2(RtOpsInfo rtOpsInfo) throws ExistException {
        RtOpsAttachment rtOpsAttachment = getRtOpsAttachment(rtOpsInfo.getOpsId());
        Map<String, String> resultMap = new HashMap<>();

        if (rtOpsAttachment == null) {
            resultMap.put("msg", "该操作无日志输出！");
            resultMap.put("success", "false");
            return resultMap;
        }

        // -1L 为默认值
        if (-1L == rtOpsAttachment.getExeId()) {
            resultMap.put("msg", "该操作正在执行中，还未有日志内容！");
            resultMap.put("success", "false");
            return resultMap;
        }

        //改成从rtOpsAttachment 获取file
        RtExeInfo rtExeInfo = new RtExeInfo();
        rtExeInfo.setExeMachine(rtOpsAttachment.getExeMachine());
        rtExeInfo.setExeLogFile(rtOpsAttachment.getExeLogFile());
        rtExeInfo.setExeParam(rtOpsAttachment.getOpsParam());

        // todo 待升级，暂时恢复测试数据
        // 升级中，通过ssh去读取文件，将获取到数据暂时写入到redis中
        if (StringUtils.isEmpty(rtExeInfo.getExeLogFile())) {
            // throw new ExistException("获取日志内容错误, 获取该操作日志文件失败！");
            resultMap.put("msg", "获取日志内容错误, 获取该操作日志文件失败！");
            resultMap.put("success", "false");
            return resultMap;
        }

        //获取执行机信息
        DetailTaskInfoBo detailTaskInfoBo =
                getDetailTaskInfoBo(rtOpsInfo.getTaskId(), rtOpsInfo.getTaskVersion());

        RtExeMachine executeMachine = getRtExeMachineByExeInfo(rtExeInfo.getExeMachine());
        ExeMachineBo exeMachineBo = getExeMachineBo(executeMachine, detailTaskInfoBo);

        //设置起始行数属性
        rtExeInfo.setOpsAttr(rtOpsInfo.getOpsAttr());
        String logContent = ssh(exeMachineBo, rtExeInfo);
        resultMap.put("msg", logContent);
        resultMap.put("success", "true");

        return resultMap;
    }

}
