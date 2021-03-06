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
     * ??????????????????????????????????????????rt_task
     */
    @Value("${version.mapping.type.task}")
    private String VersionMappingTypeTask;

    /**
     * ??????????????????????????????????????????rt_task
     */
    @Value("${version.mapping.name.task}")
    private String VersionMappingNameTask;

    /**
     * ?????????????????????
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
     * 1 ??????????????????
     *
     * @param taskSaveRequest
     * @return
     */
    @Override
    public DetailTaskVo saveDetailTaskVo(UserPrincipal principal, TaskSaveRequest taskSaveRequest) throws FeignException, DataOpsException {
        /**
         * 1 ????????????????????????
         * 2 ??????????????????
         * 3 ??????????????????????????????
         * 4 ?????????????????????????????????
         * 5 ??????????????????????????????????????????????????????
         */

        /**
         * 1 ???????????????id???version
         * 2 ???????????????????????????????????????????????????
         * 3 ???????????????????????????????????????
         * 4 ??????????????????????????????
         * 5 ?????????????????????
         *      ???????????????????????????
         * 6 ??????????????????????????????????????????????????????????????????????????????????????????
         */
        checkTaskSaveRequest(taskSaveRequest);
        return saveTask(principal, taskSaveRequest, OpsNameEnum.Save.getValue());
    }

    /**
     * 2 ??????????????????
     *
     * @param taskSaveRequest
     * @return
     */
    @Override
    public DetailTaskVo modifyDetailTaskVo(UserPrincipal principal,
                                           TaskSaveRequest taskSaveRequest) throws FeignException, DataOpsException, ExistException {
        /**
         * 1 ????????????????????????
         * 2 ??????????????????
         * 3 ??????????????????????????????
         * 4 ?????????????????????????????????
         * 5 ??????????????????????????????????????????????????????
         */

        /**
         * 1 ??????????????????????????????
         * 2 ???????????????????????????????????????????????????
         * 3 ???????????????????????????????????????
         * 4 ??????????????????????????????
         * 5 ?????????????????????
         *      ???????????????????????????
         * 6 ???????????????
         * 7 ??????????????????????????????????????????????????????????????????????????????????????????
         */

        checkTaskSaveRequest(taskSaveRequest);


        // ??????taskId????????????
        RtTaskInfo rtTaskInfo =
                taskInfoService.getOne(
                        Wrappers.<RtTaskInfo>lambdaQuery()
                                .eq(RtTaskInfo::getTaskId, taskSaveRequest.getTaskInfo().getTaskId())
                                .eq(RtTaskInfo::getTaskVersion, taskSaveRequest.getTaskInfo().getTaskVersion()),
                        false);
        if (null == rtTaskInfo) {
            throw new ExistException("????????????????????????????????????????????????");
        }

        /*
            ?????????????????????ExecArg???*?????????????????????????????????*????????????????????????
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
            throw new FeignException("?????????????????????????????????????????????????????????");
        }

        if (!taskSaveRequest.getTaskInfo().getTaskId().equals(mdVersionManageRetDataMsg.getData().getMdId())) {
            throw new ExistException("???????????????????????????????????????");
        }

        if (VersionConstants.VER_STATE_TREL.equals(mdVersionManageRetDataMsg.getData().getMdVersionState())) {
            throw new ExistException("?????????????????????????????????????????????????????????");
        }

        return saveTask(principal, taskSaveRequest, OpsNameEnum.Modify.getValue());
    }


    /*
        ?????????????????????ExecArg???*?????????????????????????????????*????????????????????????
    */
    private List<RtFlowPluginRelativeRequest> transSecurityExeArg(TaskSaveRequest taskSaveRequest) throws FeignException, ExistException {
        DetailTaskVo detailTaskVo = opsData.getDetailTaskVo(taskSaveRequest.getTaskInfo().getTaskId(), taskSaveRequest.getTaskInfo().getTaskVersion());
        // ??????????????????????????????????????????
        Map<String, String> transParamMap = detailTaskVo.getPluginInfoList().stream().filter(item -> {
            // ?????????????????????????????????
            return GenerSecurityPWD.judgeSecurityPassword(item.getExecArg());
        }).collect(Collectors.toMap(item -> {
            // ?????????????????????key
            String key = GenerSecurityPWD.generSecurityKey(item.getExecArg());
            return key;
        }, item -> {
            // ??????????????????value
            List<String> password = GenerSecurityPWD.generSecurityPassword(item.getExecArg());
            return password.get(1);
        }));

        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        ArrayList<RtFlowPluginRelativeRequest> relativeRequests = new ArrayList<>();
        for (RtFlowPluginRelativeRequest item : taskSaveRequest.getPluginInfoList()) {
            if (GenerSecurityPWD.judgeSecurityPassword(item.getExecArg())) {
                List<String> password = GenerSecurityPWD.generSecurityPassword(item.getExecArg());
                if (password.get(0).contains("******")) {
                    String key = GenerSecurityPWD.generSecurityKey(item.getExecArg());
                    if (StringUtils.isEmpty(transParamMap.get(key))) {
                        throw new ExistException("????????????????????????????????????????????????");
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
     * ????????????????????????????????????????????????
     * @param taskSaveRequest ???????????????????????????????????????????????????????????? 'password' = '???????????????'
     * @return ??????????????????????????????
     * @throws FeignException Feign??????
     * @throws ExistException Exist??????
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
     * ????????????
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
        // 1 ???????????????????????????????????????
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
            throw new FeignException("????????????????????????????????????");
        }

        List<MdVersionManageVo> mdVersionManageVoList = pageRetDataMsg.getData().getRecords();
        if (mdVersionManageVoList != null && mdVersionManageVoList.size() > 0) {
            throw new ExistException("???????????????????????????????????????????????????");
        }*/
        checkTaskSaveRequest(taskSaveRequest);
         /*
            ????????????????????????????????????????????????*????????????????????????
         */
        List<RtFlowPluginRelativeRequest> relativeRequests = encodePasswordExeArg(taskSaveRequest);
        taskSaveRequest.setPluginInfoList(relativeRequests);
        return saveTask(principal, taskSaveRequest, OpsNameEnum.Audit.getValue());
    }

    /**
     * ??????????????????
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
        // ???????????????????????????????????????????????????????????????????????????
        // ?????????????????????????????????audit_process_mapping???????????????callbackId
        // ??????????????? ?????????????????????DEV ??????????????????????????????

        AuditProcess auditProcess = auditProcessService.getById(auditProcessId);
        System.out.println(JSON.toJSONString(auditProcess));

        // ?????????-??????AuditProcess???????????????
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
            throw new DataOpsException("????????????????????????");
        }
        if (StringUtils.isEmpty(auditProcess.getAuditId())) {
            throw new DataOpsException("?????????ID????????????");
        }
        if (StringUtils.isEmpty(auditProcess.getAuditType())) {
            throw new DataOpsException("????????????????????????");
        }
        if (StringUtils.isEmpty(auditProcess.getPreInsProperty())) {
            throw new DataOpsException("??????????????????????????????");
        }
        if (StringUtils.isEmpty(auditProcess.getInsId())) {
            throw new DataOpsException("??????ID????????????");
        }
        if (StringUtils.isEmpty(auditProcess.getInsName())) {
            throw new DataOpsException("????????????????????????");
        }
        if (StringUtils.isEmpty(auditProcess.getInsVersion())) {
            throw new DataOpsException("???????????????????????????");
        }
    }

    private void checkTaskSaveRequest(TaskSaveRequest taskSaveRequest) throws DataOpsException {
        if (ObjectUtils.isEmpty(taskSaveRequest.getFlowInfo())
                || StringUtils.isEmpty(taskSaveRequest.getFlowInfo().getEngine())) {
            throw new DataOpsException("????????????????????????????????????");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getFlowInfo())
                || StringUtils.isEmpty(taskSaveRequest.getFlowInfo().getFlowValue())) {
            throw new DataOpsException("??????????????????????????????????????????");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExeMachineList())) {
            throw new DataOpsException("???????????????????????????????????????");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExceptionHandle())
                || StringUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExceptionHandle().getHandleType())) {
            throw new DataOpsException("????????????????????????????????????????????????");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExeMachineList())) {
            throw new DataOpsException("??????????????????????????????????????????");
        }
        if (ObjectUtils.isEmpty(taskSaveRequest.getExecuteInfo().getExeMachineList())) {
            throw new DataOpsException("??????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     *
     * @param principal
     * @param taskId
     * @param taskVersion
     * @return
     * @throws FeignException
     */
    @Override
    public boolean releaseTask(UserPrincipal principal, Long taskId, String taskVersion) {
        // ???????????????????????????????????????????????????
        // ?????????????????????????????????audit_process_mapping???????????????callbackId

        boolean flag = true;
        // ????????????
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
         * 1 ??????????????????????????????
         * 2 ???????????????????????????????????????????????????
         * 3 ???????????????????????????????????????
         * 4 ??????????????????????????????
         * 5 ?????????????????????
         *      ???????????????????????????
         * 6 ??????????????????????????????????????????????????????????????????????????????????????????
         * 7 ???????????????????????????????????????
         * 8 ????????????
         */

        // 1 ???????????????id???version????????????
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
                    taskId, taskSaveRequest.getTaskInfo().getTaskName() + "-??????",
                    // ??????????????????
                    VersionConstants.VER_STATE_TREL);
        } else if (OpsNameEnum.Audit.getValue().equals(opsName)) {
            //taskId = taskSaveRequest.getTaskInfo().getTaskId();
            taskId = getTaskId();
            taskVersion = getTaskVersion(
                    taskId, taskSaveRequest.getTaskInfo().getTaskName(),
                    VersionConstants.VER_STATE_TREL);
        }

        try {
            // 2 ???????????????????????????????????????????????????
            RtTaskInfo rtTaskInfo = getRtTaskInfo(taskSaveRequest);
            rtTaskInfo.setTaskId(taskId);
            rtTaskInfo.setTaskVersion(taskVersion);
            taskInfoService.save(rtTaskInfo);

            // 3 ???????????????????????????????????????
            RtFlowInfo rtFlowInfo = getRtFlowInfo(taskSaveRequest);
            // todo ????????? ???????????????????????????
            rtFlowInfo.setFlowId(taskId);
            rtFlowInfo.setFlowVersion(taskVersion);
            flowInfoService.save(rtFlowInfo);

            // 4 ??????????????????????????????
            RtTaskFlowRelative rtTaskFlowRelative = new RtTaskFlowRelative();
            rtTaskFlowRelative.setTaskId(rtTaskInfo.getTaskId());
            rtTaskFlowRelative.setTaskVersion(rtTaskInfo.getTaskVersion());
            rtTaskFlowRelative.setFlowId(rtFlowInfo.getFlowId());
            rtTaskFlowRelative.setFlowVersion(rtFlowInfo.getFlowVersion());
            taskFlowRelativeService.save(rtTaskFlowRelative);

            ExecuteInfoVo executeInfoVO = taskSaveRequest.getExecuteInfo();
            // 5.1 ??????????????????
            List<ExeMachineVo> exeMachineVOList = executeInfoVO.getExeMachineList();
            // 5.2 ???????????????????????????
            List<RtTaskMachineRelative> rtTaskMachineRelativeList = new ArrayList<>();
            for (ExeMachineVo exeMachineVO : exeMachineVOList) {
                RtTaskMachineRelative rtTaskMachineRelative = new RtTaskMachineRelative();
                rtTaskMachineRelative.setMachineId(exeMachineVO.getMachineId());
                rtTaskMachineRelative.setTaskId(rtTaskInfo.getTaskId());
                rtTaskMachineRelative.setTaskVersion(rtTaskInfo.getTaskVersion());
                rtTaskMachineRelativeList.add(rtTaskMachineRelative);
            }
            taskMachineRelativeService.saveBatch(rtTaskMachineRelativeList);

            // 6.1 ??????????????????
            List<AppParamVo> applicationParamVOList = executeInfoVO.getApplicationParamList();
            List<RtParamInfo> applicationParam = new ArrayList<>();
            for (AppParamVo applicationParamVO : applicationParamVOList) {
                RtParamInfo rtParamInfo = PojoTrans.pojoTrans(applicationParamVO, RtParamInfo.class);
                rtParamInfo.setTaskId(taskId);
                rtParamInfo.setTaskVersion(taskVersion);
                applicationParam.add(rtParamInfo);
            }
            // 6.2 ???????????????????????????????????????????????????
            List<ExecuteParamVo> executeParamVOList = executeInfoVO.getExecuteParamList();
            List<RtParamInfo> runParam = new ArrayList<>();
            for (ExecuteParamVo executeParamVO : executeParamVOList) {
                RtParamInfo rtParamInfo = PojoTrans.pojoTrans(executeParamVO, RtParamInfo.class);
                rtParamInfo.setTaskId(taskId);
                rtParamInfo.setTaskVersion(taskVersion);
                runParam.add(rtParamInfo);
            }
            // 6.3 ??????????????????
            List<RtParamInfo> rtParamInfo = new ArrayList<>();
            rtParamInfo.addAll(applicationParam);
            rtParamInfo.addAll(runParam);
            paramInfoService.saveBatch(rtParamInfo);


            //???????????????????????????
            List<RtFlowPluginRelativeRequest> pluginInfoList = taskSaveRequest.getPluginInfoList();
            List<RtFlowPluginRelative> flowPluginRelativeList = new ArrayList<>();
            String engine = rtFlowInfo.getEngine();
            for (RtFlowPluginRelativeRequest pluginInfo : pluginInfoList) {
                RtFlowPluginRelative rtFlowPluginRelative = new RtFlowPluginRelative();
                rtFlowPluginRelative.setPluginId(pluginInfo.getPluginId());

                /*//TODO ?????????--???????????????????????????
                if (GenerSecurityPWD.judgeSecurityPassword(pluginInfo.getExecArg())) {
                    List<String> password = GenerSecurityPWD.generSecurityPassword(pluginInfo.getExecArg());
                    // ????????????
                    String encode = GenerSecurityPWD.encode(password.get(1));
                    // ??????????????????????????????
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
                //??????????????? flinksql??? ??????last_plugin_id next_plugin_id??????
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


            // 7 ???????????????
            /*if (OpsNameEnum.Modify.getValue().equals(opsName)) {
                RtTaskUpdate rtTaskUpdate = getRtTaskUpdate(
                        rtTaskInfo.getTaskId(),
                        taskSaveRequest.getTaskInfo().getTaskVersion(),
                        taskVersion,
                        principal);
                taskUpdateService.save(rtTaskUpdate);
            }*/

            // 8 ????????????
            opsRegister.registerOps(principal,
                    taskId, taskVersion,
                    OpsTypeEnum.Data.getValue(), opsName);

            DetailTaskVo detailTaskVo = getDetailTaskVo(taskId, taskVersion);
            return detailTaskVo;

        } catch (Exception e) {
            //????????????
            if (OpsNameEnum.Save.getValue().equals(opsName)) {
                versionManagerRollback(taskId, taskVersion, VersionConstants.VER_STATE_DEV);
            } else if (OpsNameEnum.Modify.getValue().equals(opsName)) {
                versionManagerRollback(taskId, taskVersion, VersionConstants.VER_STATE_DEV);
            } else if (OpsNameEnum.Audit.getValue().equals(opsName)) {
                versionManagerRollback(taskId, taskVersion, VersionConstants.VER_STATE_TREL);
            }
            e.printStackTrace();
            throw new DataOpsException("???????????????????????????");
        }
    }

    /**
     * ????????????????????????taskId
     *
     * @return
     */
    private Long getTaskId() throws FeignException {
        RetDataMsg<Long> sequenceIdData = versionServiceFeign.getSequenceId(VersionSequenceName);
        if (sequenceIdData.getSuccess()) {
            return sequenceIdData.getData();
        } else {
            throw new FeignException("????????????ID?????????");
        }
    }


    /**
     * ????????????????????????taskVersion
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

        //??????version_manage
        RetDataMsg<String> retDataMsg =
                versionServiceFeign.saveAndAutoIncrementVersion(versionSavingParamRequest);

        if (retDataMsg.getSuccess()) {
            return retDataMsg.getData();
        } else {
            System.out.println("??????????????????????????? cause:" + retDataMsg.getData());
            versionServiceFeign.saveAndAutoIncrementVersionRollback(versionSavingParamRequest);
            throw new FeignException("???????????????????????????");
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
     * 3 ??????????????????(??????
     */

    /**
     * 4 ????????????????????????
     *
     * @param taskId
     * @param taskVersion
     * @return
     */
    @Override
    public DetailTaskVo getDetailTaskVo(Long taskId, String taskVersion) throws FeignException {
        /**
         * 1 ????????????????????????
         * 2 ????????????????????????
         * 3 ??????????????????????????????
         */

        DetailTaskInfoVo taskInfo = getDetailTaskInfoVo(taskId, taskVersion);
        FlowInfoVo flowInfo = getDetailFlowInfoVo(taskId, taskVersion);
        ExecuteInfoVo executeInfo = getExecuteInfoVo(taskId, taskVersion);

        //??????????????????
        List<RtPluginInfoVo> pluginInfoVoList = pluginsInfoService.listFlowPluginInfoVo(taskId, taskVersion);

        DetailTaskVo detailTask = new DetailTaskVo();
        detailTask.setTaskInfo(taskInfo);
        detailTask.setFlowInfo(flowInfo);
        detailTask.setExecuteInfo(executeInfo);
        detailTask.setPluginInfoList(pluginInfoVoList);

        return detailTask;
    }

    /**
     * ?????????-?????????????????????????????????????????????
     *
     * @param taskId
     * @param taskVersion
     * @return
     * @throws FeignException
     */
    @Override
    public DetailTaskVo getShowDetailTaskVo(Long taskId, String taskVersion) throws FeignException {
        /**
         * 1 ????????????????????????
         * 2 ????????????????????????
         * 3 ??????????????????????????????
         */
        DetailTaskInfoVo taskInfo = getDetailTaskInfoVo(taskId, taskVersion);
        FlowInfoVo flowInfo = getDetailFlowInfoVo(taskId, taskVersion);
        ExecuteInfoVo executeInfo = getExecuteInfoVo(taskId, taskVersion);
        //??????????????????
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
        // 1 ????????????????????????
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
            throw new FeignException("?????????????????????????????????");
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
            throw new FeignException("?????????????????????????????????");
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
        //??????????????????????????????
        detailTaskInfoVO.setMaxVersionState(maxVersionManage.getMdVersionState());
        //????????????????????????
        detailTaskInfoVO.setTaskStateCode(versionManage.getMdVersionState());

        return detailTaskInfoVO;
    }

    private String versionStateTrans(String versionState) {
        if (VersionConstants.VER_STATE_DEV.equals(versionState)) {
            return "???????????????";
        } else if (VersionConstants.VER_STATE_TREL.equals(versionState)) {
            return "?????????";
        } else if (VersionConstants.VER_STATE_REL.equals(versionState)) {
            return "??????";
        } else {
            return "??????";
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

        // 1 ????????????
        ExceptionHandleVo exceptionHandleVo = new ExceptionHandleVo();
        exceptionHandleVo.setHandleType(taskInfo.getHandleType());
        if (taskInfo.getHandleType().equals(RestartTypeEnum.Manual.getValue())) {
            exceptionHandleVo.setRestartInterval(null);
            exceptionHandleVo.setRestartNum(null);
        } else {
            exceptionHandleVo.setRestartInterval(taskInfo.getRestartInterval());
            exceptionHandleVo.setRestartNum(taskInfo.getRestartNum());
        }

        // 2 ???????????????
        List<ExeMachineVo> exeMachineVoList = getExeMachineVoList(taskId, taskVersion);

        // 3 ??????????????? 4 ????????????
        List<RtParamInfo> paramInfoListApplication =
                paramInfoService.<RtParamInfo>lambdaQuery()
                        .eq(RtParamInfo::getTaskId, taskId)
                        .eq(RtParamInfo::getTaskVersion, taskVersion)
                        .in(RtParamInfo::getParamType,
                                Arrays.asList(ExeParamTypeEnum.Application.getValue(),
                                        ExeParamTypeEnum.Run.getValue(),
                                        ExeParamTypeEnum.Config.getValue()))
                        .list();

        // 3 ????????????
        List<AppParamVo> applicationParamVoList = new ArrayList<>();
        // 4 ????????????
        // 4.1 ????????????
        List<ExecuteParamVo> runParamVoList = new ArrayList<>();
        // 4.2 ??????????????????
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
        // 4 ????????????
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
