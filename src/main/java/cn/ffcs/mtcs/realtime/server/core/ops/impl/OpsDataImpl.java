package cn.ffcs.mtcs.realtime.server.core.ops.impl;

import cn.ffcs.common.basic.security.SecurityFactory;
import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.common.request.RtFlowPluginRelativeRequest;
import cn.ffcs.mtcs.realtime.common.request.TaskSaveRequest;
import cn.ffcs.mtcs.realtime.common.vo.DetailTaskInfoVo;
import cn.ffcs.mtcs.realtime.common.vo.DetailTaskVo;
import cn.ffcs.mtcs.realtime.common.vo.RtPluginInfoVo;
import cn.ffcs.mtcs.realtime.common.vo.base.*;
import cn.ffcs.mtcs.realtime.server.constants.*;
import cn.ffcs.mtcs.realtime.server.constants.monitor.MonitorContents;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsData;
import cn.ffcs.mtcs.realtime.server.core.ops.OpsRegister;
import cn.ffcs.mtcs.realtime.server.entity.AuditProcess;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.exception.FeignException;
import cn.ffcs.mtcs.realtime.server.feign.VersionServiceFeign;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import cn.ffcs.mtcs.realtime.server.util.GenerSecurityPWD;
import cn.ffcs.mtcs.realtime.server.util.PojoTrans;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import cn.ffcs.mtcs.version.common.constants.VersionConstants;
import cn.ffcs.mtcs.version.common.entity.MdVersionManage;
import cn.ffcs.mtcs.version.common.request.VersionSavingParamRequest;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 19:23
 * @Version 1.0
 */
@Component
public class OpsDataImpl implements IOpsData {

    @Autowired
    private IRtTaskInfoService taskInfoService;

    @Autowired
    private IRtFlowInfoService flowInfoService;

    @Autowired
    private IRtTaskFlowRelativeService taskFlowRelativeService;

    @Autowired
    private IRtExeMachineService exeMachineService;

    @Autowired
    private IRtTaskMachineRelativeService taskMachineRelativeService;

    @Autowired
    private IRtParamInfoService paramInfoService;

    @Autowired
    private OpsRegister opsRegister;

    @Autowired
    private IOpsData opsData;

    @Autowired
    private VersionServiceFeign versionServiceFeign;

    /**
     * 元数据类型映射表规格，类型，rt_task
     */
    @Value("${version.mapping.type.task}")
    private String VersionMappingTypeTask;

    /**
     * 元数据类型映射表规格，名称，rt_task
     */
    @Value("${version.mapping.name.task}")
    private String VersionMappingNameTask;

    /**
     * 版本序列号名称
     */
    @Value("${version.sequence.name}")
    private String VersionSequenceName;

    @Autowired
    private IRtFlowPluginRelativeService flowPluginRelativeService;

    @Autowired
    private IRtPluginsInfoService pluginsInfoService;

    @Autowired
    private IAuditProcessService auditProcessService;

    @Autowired
    private IRtTaskLastStateService taskLastStateInfoService;

    /**
     * 1 保存任务信息
     *
     * @param taskSaveRequest
     * @return
     */
    @Override
    public DetailTaskVo saveDetailTaskVo(UserPrincipal principal, TaskSaveRequest taskSaveRequest) throws FeignException, DataOpsException {
        /**
         * 1 保存任务基本信息
         * 2 保存流程信息
         * 3 保存任务流程关系信息
         * 4 保存任务执行机关系信息
         * 5 保存应用信息、执行信息、执行配置信息
         */

        /**
         * 1 申请任务的id和version
         * 2 获取任务基本信息，保存任务基本信息
         * 3 获取流程信息，保存流程信息
         * 4 保存任务流程关系信息
         * 5 获取主机信息，
         *      保存任务主机关系表
         * 6 获取应用参数信息，获取运行参数信息，获取执行配置信息，并保存
         */
        checkTaskSaveRequest(taskSaveRequest);
        return saveTask(principal, taskSaveRequest, OpsNameEnum.Save.getValue());
    }

    /**
     * 2 修改任务信息
     *
     * @param taskSaveRequest
     * @return
     */
    @Override
    public DetailTaskVo modifyDetailTaskVo(UserPrincipal principal,
                                           TaskSaveRequest taskSaveRequest) throws FeignException, DataOpsException, ExistException {
        /**
         * 1 保存任务基本信息
         * 2 保存流程信息
         * 3 保存任务流程关系信息
         * 4 保存任务执行机关系信息
         * 5 保存应用信息、执行信息、执行配置信息
         */

        /**
         * 1 申请任务的下一个版本
         * 2 获取任务基本信息，保存任务基本信息
         * 3 获取流程信息，保存流程信息
         * 4 保存任务流程关系信息
         * 5 获取主机信息，
         *      保存任务主机关系表
         * 6 注册更新表
         * 7 获取应用参数信息，获取运行参数信息，获取执行配置信息，并保存
         */

        checkTaskSaveRequest(taskSaveRequest);


        // 检测taskId是否存在
        RtTaskInfo rtTaskInfo =
                taskInfoService.getOne(
                        Wrappers.<RtTaskInfo>lambdaQuery()
                                .eq(RtTaskInfo::getTaskId, taskSaveRequest.getTaskInfo().getTaskId())
                                .eq(RtTaskInfo::getTaskVersion, taskSaveRequest.getTaskInfo().getTaskVersion()),
                        false);
        if (null == rtTaskInfo) {
            throw new ExistException("该任务无历史版本，无法进行修改！");
        }

        /*
            判断前台传入的ExecArg是*的话进行替换保存，不是*的话进行直接保存
         */
        List<RtFlowPluginRelativeRequest> relativeRequests = transSecurityExeArg(taskSaveRequest);
        taskSaveRequest.setPluginInfoList(relativeRequests);


        RetDataMsg<MdVersionManage> mdVersionManageRetDataMsg =
                versionServiceFeign.getMaxVersionManageInfo(
                        taskSaveRequest.getTaskInfo().getTaskId(),
                        null,
                        VersionMappingTypeTask,
                        VersionMappingNameTask);
        if (null == mdVersionManageRetDataMsg || !mdVersionManageRetDataMsg.getSuccess()) {
            throw new FeignException("修改数据时获取该任务最大版本状态失败！");
        }

        if (!taskSaveRequest.getTaskInfo().getTaskId().equals(mdVersionManageRetDataMsg.getData().getMdId())) {
            throw new ExistException("请基于最新的版本进行修改！");
        }

        if (VersionConstants.VER_STATE_TREL.equals(mdVersionManageRetDataMsg.getData().getMdVersionState())) {
            throw new ExistException("该任务最新版还在审核中，无法进行修改！");
        }

        return saveTask(principal, taskSaveRequest, OpsNameEnum.Modify.getValue());
    }


    /*
        判断前台传入的ExecArg是*的话进行替换保存，不是*的话进行直接保存
    */
    private List<RtFlowPluginRelativeRequest> transSecurityExeArg(TaskSaveRequest taskSaveRequest) throws FeignException, ExistException {
        DetailTaskVo detailTaskVo = opsData.getDetailTaskVo(taskSaveRequest.getTaskInfo().getTaskId(), taskSaveRequest.getTaskInfo().getTaskVersion());
        // 获取用户保存在数据库中的密码
        Map<String, String> transParamMap = detailTaskVo.getPluginInfoList().stream().filter(item -> {
            // 过滤是否含有密码的插件
            return GenerSecurityPWD.judgeSecurityPassword(item.getExecArg());
        }).collect(Collectors.toMap(item -> {
            // 定位关键字作为key
            String key = GenerSecurityPWD.generSecurityKey(item.getExecArg());
            return key;
        }, item -> {
            // 定位密码当作value
            List<String> password = GenerSecurityPWD.generSecurityPassword(item.getExecArg());
            return password.get(1);
        }));

        // 判断修改任务信息是否有进行修改敏感信息，如果有敏感信息，则加密后添加，如果没有敏感信息，则将敏感信息的值用查出的敏感信息替换
        ArrayList<RtFlowPluginRelativeRequest> relativeRequests = new ArrayList<>();
        for (RtFlowPluginRelativeRequest item : taskSaveRequest.getPluginInfoList()) {
            if (GenerSecurityPWD.judgeSecurityPassword(item.getExecArg())) {
                List<String> password = GenerSecurityPWD.generSecurityPassword(item.getExecArg());
                if (password.get(0).contains("******")) {
                    String key = GenerSecurityPWD.generSecurityKey(item.getExecArg());
                    if (StringUtils.isEmpty(transParamMap.get(key))) {
                        throw new ExistException("需要对插件中涉及密码进行明文设置");
                    }
                    String newReplace = password.get(0).replace("******", transParamMap.get(key));
                    item.setExecArg(item.getExecArg().replace(password.get(0), newReplace));
                    relativeRequests.add(item);
                } else {
                    String encode = GenerSecurityPWD.encode(password.get(1));
                    String newReplace = password.get(0).replace(password.get(1), encode);
                    item.setExecArg(item.getExecArg().replace(password.get(0), newReplace));
                    relativeRequests.add(item);
                }
            } else {
                relativeRequests.add(item);
            }
        }
        return relativeRequests;
    }

    /**
     * 对插件中涉及到的密码进行加密处理
     * @param taskSaveRequest 任务请求信息，插件内容中的的密码关键字为 'password' = '具体密码值'
     * @return 替换后的插件列表信息
     * @throws FeignException Feign异常
     * @throws ExistException Exist异常
     */
    private List<RtFlowPluginRelativeRequest> encodePasswordExeArg(TaskSaveRequest taskSaveRequest) throws FeignException, ExistException {
        ArrayList<RtFlowPluginRelativeRequest> relativeRequests = new ArrayList<>();
        for (RtFlowPluginRelativeRequest item : taskSaveRequest.getPluginInfoList()) {
            if (GenerSecurityPWD.judgeSecurityPassword(item.getExecArg())) {
                List<String> password = GenerSecurityPWD.generSecurityPassword(item.getExecArg());
                if (!password.get(0).contains("******")) {
                    String encode = GenerSecurityPWD.encode(password.get(1));
                    String newReplace = password.get(0).replace(password.get(1), encode);
                    item.setExecArg(item.getExecArg().replace(password.get(0), newReplace));
                    relativeRequests.add(item);
                }
            } else {
                relativeRequests.add(item);
            }
        }
        return relativeRequests;
    }

    /**
     * 审核任务
     *
     * @param principal
     * @param taskSaveRequest
     * @return
     * @throws FeignException
     * @throws ExistException
     */
    @Override
    public DetailTaskVo auditTask(UserPrincipal principal,
                                  TaskSaveRequest taskSaveRequest) throws FeignException, ExistException, DataOpsException {
        // 1 判断是否已经有该版本在审核
        /*Long defaultCurrent = 1L;
        int defaultSize = 5;
        RetDataMsg<Page<MdVersionManageVo>> pageRetDataMsg =
                versionServiceFeign.selectAllVersionByMdIdAndMappingTypeName(
                        taskSaveRequest.getTaskInfo().getTaskId(),
                        VersionMappingTypeTask,
                        VersionMappingNameTask,
                        VersionConstants.VER_STATE_TREL,
                        defaultCurrent,
                        defaultSize);

        if (!pageRetDataMsg.getSuccess()) {
            throw new FeignException("审核时调用版本服务失败！");
        }

        List<MdVersionManageVo> mdVersionManageVoList = pageRetDataMsg.getData().getRecords();
        if (mdVersionManageVoList != null && mdVersionManageVoList.size() > 0) {
            throw new ExistException("版本审核时已经存在该任务的审核信息");
        }*/
        checkTaskSaveRequest(taskSaveRequest);
         /*
            对插件中的密码信息进行处理，不是*的话进行加密处理
         */
        List<RtFlowPluginRelativeRequest> relativeRequests = encodePasswordExeArg(taskSaveRequest);
        taskSaveRequest.setPluginInfoList(relativeRequests);
        return saveTask(principal, taskSaveRequest, OpsNameEnum.Audit.getValue());
    }

    /**
     * 审核回退任务
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     * @throws FeignException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditTaskCallback(UserPrincipal principal, Long taskId, String taskVersion, String auditProcessId) throws FeignException, DataOpsException {
        // 在版本服务将状态修改为了待发布，现在将这条记录失效
        // 使用之前的接口去操作，audit_process_mapping表中去配置callbackId
        // 审核不通过 版本服务状态为DEV 需要将版本号进行修改

        AuditProcess auditProcess = auditProcessService.getById(auditProcessId);
        System.out.println(JSON.toJSONString(auditProcess));

        // 陈张圣-抽取AuditProcess检查为接口
        checkAuditProcess(auditProcess);

        taskFlowRelativeService.update(new UpdateWrapper<RtTaskFlowRelative>().lambda()
                .set(RtTaskFlowRelative::getTaskVersion, auditProcess.getPreInsProperty())
                .set(RtTaskFlowRelative::getFlowVersion, auditProcess.getPreInsProperty())
                .eq(RtTaskFlowRelative::getTaskId, auditProcess.getInsId())
                .eq(RtTaskFlowRelative::getTaskVersion, auditProcess.getInsVersion()));

        taskInfoService.update(new UpdateWrapper<RtTaskInfo>().lambda()
                .set(RtTaskInfo::getTaskVersion, auditProcess.getPreInsProperty())
                .eq(RtTaskInfo::getTaskId, auditProcess.getInsId())
                .eq(RtTaskInfo::getTaskVersion, auditProcess.getInsVersion()));


        flowInfoService.update(new UpdateWrapper<RtFlowInfo>().lambda()
                .set(RtFlowInfo::getFlowVersion, auditProcess.getPreInsProperty())
                .eq(RtFlowInfo::getFlowId, auditProcess.getInsId())
                .eq(RtFlowInfo::getFlowVersion, auditProcess.getInsVersion()));
        taskMachineRelativeService.update(new UpdateWrapper<RtTaskMachineRelative>().lambda()
                .set(RtTaskMachineRelative::getTaskVersion, auditProcess.getPreInsProperty())
                .eq(RtTaskMachineRelative::getTaskId, auditProcess.getInsId())
                .eq(RtTaskMachineRelative::getTaskVersion, auditProcess.getInsVersion()));
        paramInfoService.update(new UpdateWrapper<RtParamInfo>().lambda()
                .set(RtParamInfo::getTaskVersion, auditProcess.getPreInsProperty())
                .eq(RtParamInfo::getTaskId, auditProcess.getInsId())
                .eq(RtParamInfo::getTaskVersion, auditProcess.getInsVersion()));
        flowPluginRelativeService.update(new UpdateWrapper<RtFlowPluginRelative>().lambda()
                .set(RtFlowPluginRelative::getFlowVersion, auditProcess.getPreInsProperty())
                .eq(RtFlowPluginRelative::getFlowId, auditProcess.getInsId())
                .eq(RtFlowPluginRelative::getFlowVersion, auditProcess.getInsVersion()));
        taskLastStateInfoService.update(new UpdateWrapper<RtTaskLastState>().lambda()
                .set(RtTaskLastState::getTaskVersion, auditProcess.getPreInsProperty())
                .set(RtTaskLastState::getTaskState, OpsNameEnum.NoApproved.getValue())
                .eq(RtTaskLastState::getTaskId, auditProcess.getInsId())
                .eq(RtTaskLastState::getTaskVersion, auditProcess.getInsVersion()));
        return true;
    }

    private void checkAuditProcess(AuditProcess auditProcess) throws DataOpsException {
        if (ObjectUtils.isNull(auditProcess)) {
            throw new DataOpsException("不存在该审核记录");
        }
        if (StringUtils.isEmpty(auditProcess.getAuditId())) {
            throw new DataOpsException("审核单ID不能为空");
        }
        if (StringUtils.isEmpty(auditProcess.getAuditType())) {
            throw new DataOpsException("代办类型不能为空");
        }
        if (StringUtils.isEmpty(auditProcess.getPreInsProperty())) {
            throw new DataOpsException("之前版本数据不能为空");
        }
        if (StringUtils.isEmpty(auditProcess.getInsId())) {
            throw new DataOpsException("实例ID不能为空");
        }
        if (StringUtils.isEmpty(auditProcess.getInsName())) {
            throw new DataOpsException("实例名称不能为空");
        }
        if (StringUtils.isEmpty(auditProcess.getInsVersion())) {
            throw new DataOpsException("实例版本号不能为空");
        }
    }

    private void checkTaskSaveRequest(TaskSaveRequest taskSaveRequest) throws DataOpsException {
        if (ObjectUtils.isEmpty(taskSaveRequest.getFlowInfo())
                || StringUtils.isEmpty(taskSaveRequest.getFlowInfo().getEngine())) {
            throw new DataOpsException("流程信息中引擎不能为空！");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getFlowInfo())
                || StringUtils.isEmpty(taskSaveRequest.getFlowInfo().getFlowValue())) {
            throw new DataOpsException("流程信息中流程参数不能为空！");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExeMachineList())) {
            throw new DataOpsException("执行信息中执行机不能为空！");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExceptionHandle())
                || StringUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExceptionHandle().getHandleType())) {
            throw new DataOpsException("执行信息中异常处理类型不能为空！");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExeMachineList())) {
            throw new DataOpsException("执行信息中应用参数不能为空！");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExeMachineList())) {
            throw new DataOpsException("执行信息中执行参数不能为空！");
        }
    }

    /**
     * 发布任务
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     * @throws FeignException
     */
    @Override
    public boolean releaseTask(UserPrincipal principal, Long taskId, String taskVersion) {
        // 将版本服务的状态从待发布修改为发布
        // 使用之前的接口去操作，audit_process_mapping表中去配置callbackId

        boolean flag = true;
        // 注册操作
        flag = null != opsRegister.registerOps(principal,
                taskId, taskVersion,
                OpsTypeEnum.Data.getValue(), OpsNameEnum.Release.getValue());

        return flag;
    }


    /**
     * @param principal
     * @param taskSaveRequest
     * @param opsName
     * @return
     * @throws FeignException
     * @throws DataOpsException
     */

    @Transactional
    public DetailTaskVo saveTask(UserPrincipal principal,
                                 TaskSaveRequest taskSaveRequest, String opsName) throws FeignException, DataOpsException {

        /**
         * 1 申请任务的下一个版本
         * 2 获取任务基本信息，保存任务基本信息
         * 3 获取流程信息，保存流程信息
         * 4 保存任务流程关系信息
         * 5 获取主机信息，
         *      保存任务主机关系表
         * 6 获取应用参数信息，获取运行参数信息，获取执行配置信息，并保存
         * 7 如果是修改则需要注册更新表
         * 8 注册操作
         */

        // 1 申请任务的id和version，并保存
        Long taskId = -1L;
        String taskVersion = "-1";
        if (OpsNameEnum.Save.getValue().equals(opsName)) {
            taskId = getTaskId();
            taskVersion = getTaskVersion(
                    taskId, taskSaveRequest.getTaskInfo().getTaskName(),
                    VersionConstants.VER_STATE_DEV);
        } else if (OpsNameEnum.Modify.getValue().equals(opsName)) {
            taskId = taskSaveRequest.getTaskInfo().getTaskId();
            taskVersion = getTaskVersion(
                    taskId, taskSaveRequest.getTaskInfo().getTaskName() + "-修改",
                    // 直接进行审核
                    VersionConstants.VER_STATE_TREL);
        } else if (OpsNameEnum.Audit.getValue().equals(opsName)) {
            //taskId = taskSaveRequest.getTaskInfo().getTaskId();
            taskId = getTaskId();
            taskVersion = getTaskVersion(
                    taskId, taskSaveRequest.getTaskInfo().getTaskName(),
                    VersionConstants.VER_STATE_TREL);
        }

        try {
            // 2 获取任务基本信息，保存任务基本信息
            RtTaskInfo rtTaskInfo = getRtTaskInfo(taskSaveRequest);
            rtTaskInfo.setTaskId(taskId);
            rtTaskInfo.setTaskVersion(taskVersion);
            taskInfoService.save(rtTaskInfo);

            // 3 获取流程信息，保存流程信息
            RtFlowInfo rtFlowInfo = getRtFlowInfo(taskSaveRequest);
            // todo 待升级 暂时使用同一个版本
            rtFlowInfo.setFlowId(taskId);
            rtFlowInfo.setFlowVersion(taskVersion);
            flowInfoService.save(rtFlowInfo);

            // 4 保存任务流程关系信息
            RtTaskFlowRelative rtTaskFlowRelative = new RtTaskFlowRelative();
            rtTaskFlowRelative.setTaskId(rtTaskInfo.getTaskId());
            rtTaskFlowRelative.setTaskVersion(rtTaskInfo.getTaskVersion());
            rtTaskFlowRelative.setFlowId(rtFlowInfo.getFlowId());
            rtTaskFlowRelative.setFlowVersion(rtFlowInfo.getFlowVersion());
            taskFlowRelativeService.save(rtTaskFlowRelative);

            ExecuteInfoVo executeInfoVO = taskSaveRequest.getExecuteInfo();
            // 5.1 获取主机信息
            List<ExeMachineVo> exeMachineVOList = executeInfoVO.getExeMachineList();
            // 5.2 保存任务主机关系表
            List<RtTaskMachineRelative> rtTaskMachineRelativeList = new ArrayList<>();
            for (ExeMachineVo exeMachineVO : exeMachineVOList) {
                RtTaskMachineRelative rtTaskMachineRelative = new RtTaskMachineRelative();
                rtTaskMachineRelative.setMachineId(exeMachineVO.getMachineId());
                rtTaskMachineRelative.setTaskId(rtTaskInfo.getTaskId());
                rtTaskMachineRelative.setTaskVersion(rtTaskInfo.getTaskVersion());
                rtTaskMachineRelativeList.add(rtTaskMachineRelative);
            }
            taskMachineRelativeService.saveBatch(rtTaskMachineRelativeList);

            // 6.1 获取应用参数
            List<AppParamVo> applicationParamVOList = executeInfoVO.getApplicationParamList();
            List<RtParamInfo> applicationParam = new ArrayList<>();
            for (AppParamVo applicationParamVO : applicationParamVOList) {
                RtParamInfo rtParamInfo = PojoTrans.pojoTrans(applicationParamVO, RtParamInfo.class);
                rtParamInfo.setTaskId(taskId);
                rtParamInfo.setTaskVersion(taskVersion);
                applicationParam.add(rtParamInfo);
            }
            // 6.2 获取运行参数信息，获取执行配置信息
            List<ExecuteParamVo> executeParamVOList = executeInfoVO.getExecuteParamList();
            List<RtParamInfo> runParam = new ArrayList<>();
            for (ExecuteParamVo executeParamVO : executeParamVOList) {
                RtParamInfo rtParamInfo = PojoTrans.pojoTrans(executeParamVO, RtParamInfo.class);
                rtParamInfo.setTaskId(taskId);
                rtParamInfo.setTaskVersion(taskVersion);
                runParam.add(rtParamInfo);
            }
            // 6.3 保存配置信息
            List<RtParamInfo> rtParamInfo = new ArrayList<>();
            rtParamInfo.addAll(applicationParam);
            rtParamInfo.addAll(runParam);
            paramInfoService.saveBatch(rtParamInfo);


            //保存流程插件关联表
            List<RtFlowPluginRelativeRequest> pluginInfoList = taskSaveRequest.getPluginInfoList();
            List<RtFlowPluginRelative> flowPluginRelativeList = new ArrayList<>();
            String engine = rtFlowInfo.getEngine();
            for (RtFlowPluginRelativeRequest pluginInfo : pluginInfoList) {
                RtFlowPluginRelative rtFlowPluginRelative = new RtFlowPluginRelative();
                rtFlowPluginRelative.setPluginId(pluginInfo.getPluginId());

                /*//TODO 陈张圣--有带密码的进行加密
                if (GenerSecurityPWD.judgeSecurityPassword(pluginInfo.getExecArg())) {
                    List<String> password = GenerSecurityPWD.generSecurityPassword(pluginInfo.getExecArg());
                    // 密码加密
                    String encode = GenerSecurityPWD.encode(password.get(1));
                    // 加密密码替换明文密码
                    String replace = password.get(0).replace(password.get(1), encode);
                    String securityExecArg = pluginInfo.getExecArg().replace(password.get(0), replace);
                    rtFlowPluginRelative.setPluginContent(securityExecArg);
                } else {
                    rtFlowPluginRelative.setPluginContent(pluginInfo.getExecArg());
                }*/
                rtFlowPluginRelative.setPluginContent(pluginInfo.getExecArg());
                rtFlowPluginRelative.setSort(pluginInfo.getSort());
                rtFlowPluginRelative.setFlowId(taskId);
                rtFlowPluginRelative.setFlowVersion(taskVersion);
                //当引擎不为 flinksql时 记录last_plugin_id next_plugin_id字段
                if (!EngineTypeEnum.FlinkSql.getValue().equals(engine) && !EngineTypeEnum.Flume.getValue().equals(engine)) {
                    String execArg = pluginInfo.getExecArg();
                    Map<String, Map> execMap = JSON.parseObject(execArg, Map.class);
                    Map<String, String> instBaseInfoMap = execMap.get("instBaseInfo");
                    rtFlowPluginRelative.setLastPluginId(instBaseInfoMap.get("lastInstIds"));
                    rtFlowPluginRelative.setNextPluginId(instBaseInfoMap.get("nextInstIds"));
                }
                flowPluginRelativeList.add(rtFlowPluginRelative);
            }

            flowPluginRelativeService.saveBatch(flowPluginRelativeList);


            // 7 保存更新表
            /*if (OpsNameEnum.Modify.getValue().equals(opsName)) {
                RtTaskUpdate rtTaskUpdate = getRtTaskUpdate(
                        rtTaskInfo.getTaskId(),
                        taskSaveRequest.getTaskInfo().getTaskVersion(),
                        taskVersion,
                        principal);
                taskUpdateService.save(rtTaskUpdate);
            }*/

            // 8 注册操作
            opsRegister.registerOps(principal,
                    taskId, taskVersion,
                    OpsTypeEnum.Data.getValue(), opsName);

            DetailTaskVo detailTaskVo = getDetailTaskVo(taskId, taskVersion);
            return detailTaskVo;

        } catch (Exception e) {
            //失败回滚
            if (OpsNameEnum.Save.getValue().equals(opsName)) {
                versionManagerRollback(taskId, taskVersion, VersionConstants.VER_STATE_DEV);
            } else if (OpsNameEnum.Modify.getValue().equals(opsName)) {
                versionManagerRollback(taskId, taskVersion, VersionConstants.VER_STATE_DEV);
            } else if (OpsNameEnum.Audit.getValue().equals(opsName)) {
                versionManagerRollback(taskId, taskVersion, VersionConstants.VER_STATE_TREL);
            }
            e.printStackTrace();
            throw new DataOpsException("任务信息保存失败！");
        }
    }

    /**
     * 通过版本服务获取taskId
     *
     * @return
     */
    private Long getTaskId() throws FeignException {
        RetDataMsg<Long> sequenceIdData = versionServiceFeign.getSequenceId(VersionSequenceName);
        if (sequenceIdData.getSuccess()) {
            return sequenceIdData.getData();
        } else {
            throw new FeignException("获取任务ID失败！");
        }
    }


    /**
     * 通过版本服务获取taskVersion
     *
     * @param taskId
     * @return
     */
    private String getTaskVersion(Long taskId, String taskName, String versionState) throws FeignException {
        VersionSavingParamRequest versionSavingParamRequest = new VersionSavingParamRequest();
        versionSavingParamRequest.setMdId(taskId);
        versionSavingParamRequest.setInsName(taskName);
        versionSavingParamRequest.setMappingType(VersionMappingTypeTask);
        versionSavingParamRequest.setMappingName(VersionMappingNameTask);
        versionSavingParamRequest.setVersionState(versionState);

        //更新version_manage
        RetDataMsg<String> retDataMsg =
                versionServiceFeign.saveAndAutoIncrementVersion(versionSavingParamRequest);

        if (retDataMsg.getSuccess()) {
            return retDataMsg.getData();
        } else {
            System.out.println("获取任务版本失败！ cause:" + retDataMsg.getData());
            versionServiceFeign.saveAndAutoIncrementVersionRollback(versionSavingParamRequest);
            throw new FeignException("获取任务版本失败！");
        }
    }

    private void versionManagerRollback(Long taskId, String taskVersion, String versionState) {
        VersionSavingParamRequest versionSavingParamRequest = new VersionSavingParamRequest();
        versionSavingParamRequest.setMdId(taskId);
        versionSavingParamRequest.setMdVersion(taskVersion);
        versionSavingParamRequest.setMappingType(VersionMappingTypeTask);
        versionSavingParamRequest.setMappingName(VersionMappingNameTask);
        versionSavingParamRequest.setVersionState(versionState);
        versionServiceFeign.saveAndAutoIncrementVersionRollback(versionSavingParamRequest);
    }


    private RtTaskInfo getRtTaskInfo(TaskSaveRequest taskSaveRequest) {
        RtTaskInfo rtTaskInfo = PojoTrans.pojoTrans(taskSaveRequest.getTaskInfo(), RtTaskInfo.class);
        rtTaskInfo.setHandleType(taskSaveRequest.getExecuteInfo().getExceptionHandle().getHandleType());
        if (RestartTypeEnum.Manual.getValue().equals(taskSaveRequest.getExecuteInfo().getExceptionHandle().getHandleType())) {
            rtTaskInfo.setRestartInterval(MonitorContents.TimeInterval);
            rtTaskInfo.setRestartNum(MonitorContents.MonitorMinCount);
        } else {
            rtTaskInfo.setRestartInterval(taskSaveRequest.getExecuteInfo().getExceptionHandle().getRestartInterval());
            rtTaskInfo.setRestartNum(taskSaveRequest.getExecuteInfo().getExceptionHandle().getRestartNum());
        }
        return rtTaskInfo;
    }

    private RtFlowInfo getRtFlowInfo(TaskSaveRequest taskSaveRequest) {
        return PojoTrans.pojoTrans(taskSaveRequest.getFlowInfo(), RtFlowInfo.class);
    }

    private RtTaskUpdate getRtTaskUpdate(Long taskId,
                                         String taskVersionOld, String taskVersionNew,
                                         UserPrincipal principal) {
        RtTaskUpdate rtTaskUpdate = new RtTaskUpdate();
        rtTaskUpdate.setTaskId(taskId);
        rtTaskUpdate.setTaskVersionOld(taskVersionOld);
        rtTaskUpdate.setTaskVersionNew(taskVersionNew);
        rtTaskUpdate.setUpdateState(UpdateStateEnum.Init.getValue());
        rtTaskUpdate.setUpdateDesc("");
        rtTaskUpdate.setCrtUser(principal.getUserId());
        rtTaskUpdate.setCrtTeam(principal.getDefaultTeamId());
        rtTaskUpdate.setCrtTime(LocalDateTime.now());
        rtTaskUpdate.setMdfyUser(principal.getUserId());
        rtTaskUpdate.setMdfyTime(LocalDateTime.now());
        rtTaskUpdate.setState(RecordStateEnum.StateUse.getValue());

        return rtTaskUpdate;
    }


    /**
     * 3 删除任务信息(无）
     */

    /**
     * 4 查询任务详细信息
     *
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public DetailTaskVo getDetailTaskVo(Long taskId, String taskVersion) throws FeignException {
        /**
         * 1 获取任务的全信息
         * 2 获取流程的全信息
         * 3 获取执行信息的全信息
         */

        DetailTaskInfoVo taskInfo = getDetailTaskInfoVo(taskId, taskVersion);
        FlowInfoVo flowInfo = getDetailFlowInfoVo(taskId, taskVersion);
        ExecuteInfoVo executeInfo = getExecuteInfoVo(taskId, taskVersion);

        //获取插件信息
        List<RtPluginInfoVo> pluginInfoVoList = pluginsInfoService.listFlowPluginInfoVo(taskId, taskVersion);

        DetailTaskVo detailTask = new DetailTaskVo();
        detailTask.setTaskInfo(taskInfo);
        detailTask.setFlowInfo(flowInfo);
        detailTask.setExecuteInfo(executeInfo);
        detailTask.setPluginInfoList(pluginInfoVoList);

        return detailTask;
    }

    /**
     * 陈张圣-查询任务详细信息，密码加密处理
     *
     * @param taskId
     * @param taskVersion
     * @return
     * @throws FeignException
     */
    @Override
    public DetailTaskVo getShowDetailTaskVo(Long taskId, String taskVersion) throws FeignException {
        /**
         * 1 获取任务的全信息
         * 2 获取流程的全信息
         * 3 获取执行信息的全信息
         */
        DetailTaskInfoVo taskInfo = getDetailTaskInfoVo(taskId, taskVersion);
        FlowInfoVo flowInfo = getDetailFlowInfoVo(taskId, taskVersion);
        ExecuteInfoVo executeInfo = getExecuteInfoVo(taskId, taskVersion);
        //获取插件信息
        List<RtPluginInfoVo> pluginInfoVoList = pluginsInfoService.listFlowPluginInfoVo(taskId, taskVersion);
        String flowValue = flowInfo.getFlowValue();
        String passwordString = ".*'.*password[\\s]*'[\\s]*=[\\s]*'(.*)'";
        Pattern passwordPattern = Pattern.compile(passwordString);
        Matcher passwordMatcher = passwordPattern.matcher(flowValue);
        if (passwordMatcher.find()) {
            passwordMatcher.reset();
            while (passwordMatcher.find()) {
                // System.out.println("0:" + m.group(0));
                // System.out.println("1:" + m.group(1));
                String newFlowInfo = flowValue.replace(passwordMatcher.group(1), "******");
                flowInfo.setFlowValue(newFlowInfo);
            }
        }
        List<RtPluginInfoVo> rtPluginInfoVoList = pluginInfoVoList.stream().map(item -> {
            String execArg = item.getExecArg();
            Matcher execArgMatcher = passwordPattern.matcher(execArg);
            // System.out.println("-----");
            // System.out.println("execArg: " + execArg);
            // System.out.println("pluginsDesc " + pluginsDesc);
            if (execArgMatcher.find()) {
                execArgMatcher.reset();
                while (execArgMatcher.find()) {
                    // System.out.println("0:" + execArgMatcher.group(0));
                    // System.out.println("1:" + execArgMatcher.group(1));
                    String execArgNewStr = execArg.replace(execArgMatcher.group(1), "******");
                    item.setExecArg(execArgNewStr);
                }
            }
            // System.out.println("-----");
            return item;
        }).collect(Collectors.toList());

        DetailTaskVo detailTask = new DetailTaskVo();
        detailTask.setTaskInfo(taskInfo);
        detailTask.setFlowInfo(flowInfo);
        detailTask.setExecuteInfo(executeInfo);

        // detailTask.setPluginInfoList(pluginInfoVoList);
        detailTask.setPluginInfoList(rtPluginInfoVoList);

        return detailTask;
    }


    private DetailTaskInfoVo getDetailTaskInfoVo(Long taskId, String taskVersion) throws FeignException {
        // 1 获取任务的全信息
        RtTaskInfo taskInfo = taskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskId, taskId)
                        .eq(RtTaskInfo::getTaskVersion, taskVersion),
                false);

        RetDataMsg<MdVersionManage> versionManageRetDataMsg =
                versionServiceFeign.getVersionManageInfo(
                        taskId,
                        taskVersion,
                        VersionMappingTypeTask,
                        VersionMappingNameTask
                );
        if (!versionManageRetDataMsg.getSuccess()) {
            throw new FeignException("调用版本管理服务失败！");
        }
        MdVersionManage versionManage = versionManageRetDataMsg.getData();

        RetDataMsg<MdVersionManage> versionManageMaxRetDataMsg =
                versionServiceFeign.getMaxVersionManageInfo(
                        taskId,
                        null,
                        VersionMappingTypeTask,
                        VersionMappingNameTask
                );
        if (!versionManageRetDataMsg.getSuccess()) {
            throw new FeignException("调用版本管理服务失败！");
        }
        MdVersionManage maxVersionManage = versionManageMaxRetDataMsg.getData();

        DetailTaskInfoVo detailTaskInfoVO = new DetailTaskInfoVo();
        detailTaskInfoVO.setTaskId(taskId);
        detailTaskInfoVO.setTaskCode(taskInfo.getTaskCode());
        detailTaskInfoVO.setTaskName(taskInfo.getTaskName());
        detailTaskInfoVO.setTaskVersion(taskVersion);
        detailTaskInfoVO.setTaskDesc(taskInfo.getTaskDesc());
        detailTaskInfoVO.setTaskState(versionStateTrans(versionManage.getMdVersionState()));
        detailTaskInfoVO.setCrtUserName(getUserName(versionManage.getCrtUser()));
        detailTaskInfoVO.setCrtTime(versionManage.getCrtTime().format(CommonConstants.dateTimeFormatter));
        detailTaskInfoVO.setMdfyUserName(getUserName(versionManage.getMdfyUser()));
        detailTaskInfoVO.setMdfyTime(versionManage.getMdfyTime().format(CommonConstants.dateTimeFormatter));
        //新增最大任务版本状态
        detailTaskInfoVO.setMaxVersionState(maxVersionManage.getMdVersionState());
        //当前任务版本状态
        detailTaskInfoVO.setTaskStateCode(versionManage.getMdVersionState());

        return detailTaskInfoVO;
    }

    private String versionStateTrans(String versionState) {
        if (VersionConstants.VER_STATE_DEV.equals(versionState)) {
            return "未通过审核";
        } else if (VersionConstants.VER_STATE_TREL.equals(versionState)) {
            return "待审核";
        } else if (VersionConstants.VER_STATE_REL.equals(versionState)) {
            return "发布";
        } else {
            return "历史";
        }
    }

    @Autowired
    private ISysUserService sysUserService;

    private String getUserName(Long userId) {
        SysUser sysUser = sysUserService.getById(userId);
        return sysUser.getUsername();
    }


    private FlowInfoVo getDetailFlowInfoVo(Long flowId, String flowVersion) {
        RtTaskFlowRelative taskFlowRelative =
                taskFlowRelativeService.getOne(
                        Wrappers.<RtTaskFlowRelative>lambdaQuery()
                                .eq(RtTaskFlowRelative::getTaskId, flowId)
                                .eq(RtTaskFlowRelative::getTaskVersion, flowVersion),
                        false);

        RtFlowInfo flowInfo =
                flowInfoService.getOne(
                        Wrappers.<RtFlowInfo>lambdaQuery()
                                .eq(RtFlowInfo::getFlowId, taskFlowRelative.getFlowId())
                                .eq(RtFlowInfo::getFlowVersion, taskFlowRelative.getFlowVersion()),
                        false);

        FlowInfoVo flowInfoVo = new FlowInfoVo();
        flowInfoVo.setFlowId(flowInfo.getFlowId());
        flowInfoVo.setFlowCode(flowInfo.getFlowCode());
        flowInfoVo.setFlowName(flowInfo.getFlowName());
        flowInfoVo.setFlowVersion(flowInfo.getFlowVersion());
        flowInfoVo.setEngine(flowInfo.getEngine());
        flowInfoVo.setFlowDesc(flowInfo.getFlowDesc());
        flowInfoVo.setFlowValue(flowInfo.getFlowValue());

        return flowInfoVo;
    }


    private ExecuteInfoVo getExecuteInfoVo(Long taskId, String taskVersion) {
        RtTaskInfo taskInfo = taskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskId, taskId)
                        .eq(RtTaskInfo::getTaskVersion, taskVersion),
                false);

        // 1 异常处理
        ExceptionHandleVo exceptionHandleVo = new ExceptionHandleVo();
        exceptionHandleVo.setHandleType(taskInfo.getHandleType());
        if (taskInfo.getHandleType().equals(RestartTypeEnum.Manual.getValue())) {
            exceptionHandleVo.setRestartInterval(null);
            exceptionHandleVo.setRestartNum(null);
        } else {
            exceptionHandleVo.setRestartInterval(taskInfo.getRestartInterval());
            exceptionHandleVo.setRestartNum(taskInfo.getRestartNum());
        }

        // 2 获取执行机
        List<ExeMachineVo> exeMachineVoList = getExeMachineVoList(taskId, taskVersion);

        // 3 应用参数， 4 执行参数
        List<RtParamInfo> paramInfoListApplication =
                paramInfoService.<RtParamInfo>lambdaQuery()
                        .eq(RtParamInfo::getTaskId, taskId)
                        .eq(RtParamInfo::getTaskVersion, taskVersion)
                        .in(RtParamInfo::getParamType,
                                Arrays.asList(ExeParamTypeEnum.Application.getValue(),
                                        ExeParamTypeEnum.Run.getValue(),
                                        ExeParamTypeEnum.Config.getValue()))
                        .list();

        // 3 应用参数
        List<AppParamVo> applicationParamVoList = new ArrayList<>();
        // 4 执行参数
        // 4.1 运行参数
        List<ExecuteParamVo> runParamVoList = new ArrayList<>();
        // 4.2 运行配置参数
        List<ExecuteParamVo> configParamVoList = new ArrayList<>();

        for (RtParamInfo rtParamInfo : paramInfoListApplication) {
            if (ExeParamTypeEnum.Application.getValue().equals(rtParamInfo.getParamType())) {
                AppParamVo applicationParamVo = new AppParamVo();
                applicationParamVo.setParamType(rtParamInfo.getParamType());
                applicationParamVo.setParamKey(rtParamInfo.getParamKey());
                applicationParamVo.setParamShow(rtParamInfo.getParamShow());
                applicationParamVo.setParamValue(rtParamInfo.getParamValue());
                applicationParamVo.setParamDesc(rtParamInfo.getParamDesc());
                applicationParamVoList.add(applicationParamVo);
            } else if (ExeParamTypeEnum.Run.getValue().equals(rtParamInfo.getParamType())) {
                ExecuteParamVo executeParamVo = new ExecuteParamVo();
                executeParamVo.setParamType(rtParamInfo.getParamType());
                executeParamVo.setParamKey(rtParamInfo.getParamKey());
                executeParamVo.setParamShow(rtParamInfo.getParamShow());
                executeParamVo.setParamValue(rtParamInfo.getParamValue());
                executeParamVo.setParamDesc(rtParamInfo.getParamDesc());
                runParamVoList.add(executeParamVo);
            } else {
                ExecuteParamVo executeParamVo = new ExecuteParamVo();
                executeParamVo.setParamType(rtParamInfo.getParamType());
                executeParamVo.setParamKey(rtParamInfo.getParamKey());
                executeParamVo.setParamShow(rtParamInfo.getParamShow());
                executeParamVo.setParamValue(rtParamInfo.getParamValue());
                executeParamVo.setParamDesc(rtParamInfo.getParamDesc());
                configParamVoList.add(executeParamVo);
            }
        }
        // 4 执行参数
        List<ExecuteParamVo> executeParamVo = new ArrayList<>();
        executeParamVo.addAll(runParamVoList);
        executeParamVo.addAll(configParamVoList);

        ExecuteInfoVo executeInfoVo = new ExecuteInfoVo();
        executeInfoVo.setExeMachineList(exeMachineVoList);
        executeInfoVo.setExceptionHandle(exceptionHandleVo);
        executeInfoVo.setApplicationParamList(applicationParamVoList);
        executeInfoVo.setExecuteParamList(executeParamVo);

        return executeInfoVo;
    }

    private List<ExeMachineVo> getExeMachineVoList(Long taskId, String taskVersion) {
        List<RtTaskMachineRelative> taskMachineRelativeList =
                taskMachineRelativeService.lambdaQuery()
                        .eq(RtTaskMachineRelative::getTaskId, taskId)
                        .eq(RtTaskMachineRelative::getTaskVersion, taskVersion)
                        .list();

        List<Long> machineIdList = new ArrayList<>();
        for (RtTaskMachineRelative taskMachineRelative : taskMachineRelativeList) {
            machineIdList.add(taskMachineRelative.getMachineId());
        }

        List<RtExeMachine> exeMachineList =
                exeMachineService.lambdaQuery()
                        .in(RtExeMachine::getMachineId, machineIdList)
                        .list();

        List<ExeMachineVo> exeMachineVoList = new ArrayList<>();
        for (RtExeMachine exeMachine : exeMachineList) {
            ExeMachineVo exeMachineVO = new ExeMachineVo();
            exeMachineVO.setMachineId(exeMachine.getMachineId());
            exeMachineVO.setHostName(exeMachine.getHostName());
            exeMachineVO.setIpAddress(exeMachine.getIpAddress());
            exeMachineVoList.add(exeMachineVO);
        }
        return exeMachineVoList;
    }
}
