package cn.ffcs.mtcs.realtime.server.service.business;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.common.request.RtFlowPluginRelativeRequest;
import cn.ffcs.mtcs.realtime.common.request.TaskSaveRequest;
import cn.ffcs.mtcs.realtime.common.vo.DetailTaskVo;
import cn.ffcs.mtcs.realtime.common.vo.TaskInfoOfflineVo;
import cn.ffcs.mtcs.realtime.common.vo.TaskInfoOnlineVo;
import cn.ffcs.mtcs.realtime.common.vo.TaskInfoOtherVo;
import cn.ffcs.mtcs.realtime.server.constants.CommonConstants;
import cn.ffcs.mtcs.realtime.server.constants.OpsNameEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.constants.TaskLogicStateEnum;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsData;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsLogic;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.exception.FeignException;
import cn.ffcs.mtcs.realtime.server.feign.UserServerFeign;
import cn.ffcs.mtcs.realtime.server.feign.VersionServiceFeign;
import cn.ffcs.mtcs.realtime.server.mapper.RtPluginsInfoMapper;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import cn.ffcs.mtcs.realtime.server.util.DsfCycle;
import cn.ffcs.mtcs.realtime.server.util.GenerSecurityPWD;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import cn.ffcs.mtcs.version.common.constants.VersionConstants;
import cn.ffcs.mtcs.version.common.entity.MdVersionManage;
import cn.ffcs.mtcs.version.common.vo.MdVersionManageVo;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;


/**
 * @Description .
 * @Author Nemo
 * @Date 2019/12/24/024 15:14
 * @Version 1.0
 */
@Component
public class RealtimeTaskBusiness {

    /**
     * 1 ????????????
     * <p>
     * 2.1 ??????-????????????
     * 2.2 ?????????????????????????????????
     * <p>
     * 3.1 ??????-????????????
     * 3.2 ?????????????????????????????????
     * <p>
     * 4.1 ??????-????????????
     * 4.2 ?????????????????????????????????
     */


    @Autowired
    private UserServerFeign userServer;

    @Autowired
    private VersionServiceFeign versionServiceFeign;

    @Autowired
    private IRtTaskLogicStateService taskLogicStateService;

    @Autowired
    private IDetailTaskInfoBoService detailTaskInfoBoService;

    @Autowired
    private IRtTaskInfoService taskInfoService;

    @Autowired
    private IRtTaskFlowRelativeService taskFlowRelativeService;

    @Autowired
    private IRtFlowInfoService flowInfoService;

    @Autowired
    private IRtTaskLastStateService taskLastStateService;

    @Autowired
    private IRtTaskUpdateService taskUpdateService;

    @Autowired
    private IRtOpsInfoService opsInfoService;

    @Autowired
    private IOpsData opsData;

    @Autowired
    private IOpsLogic opsLogic;

    @Autowired
    private UserPrincipal userPrincipal;

    @Autowired
    private IRtPluginsInfoService pluginsInfoService;


    /**
     * ???????????????????????????????????????
     */
    @Value("${version.mapping.module}")
    private String VersionMappingModule;

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
     * ??????????????????????????????????????????rt_flow
     */
    @Value("${version.mapping.type.flow}")
    private String VersionMappingTypeFlow;

    /**
     * ??????????????????????????????????????????rt_flow
     */
    @Value("${version.mapping.name.flow}")
    private String VersionMappingNameFlow;


    /**
     * ??????????????????
     */
    @Value("${page.null.flag}")
    private String PageNullFlag;

    public Boolean saveTaskAll(TaskSaveRequest taskSaveRequest) throws FeignException, ExistException, DataOpsException {
        /**
         * 0 ?????????????????????
         * 1 ???????????????????????????????????????????????????
         * 2 ???????????????????????????????????????
         * 3 ??????????????????????????????
         * 4 ?????????????????????
         *      ???????????????????????????
         * 5 ??????????????????????????????????????????????????????????????????????????????????????????
         *
         * 6 ????????????
         *
         */
        UserPrincipal principal = userServer.getPrincipal();

        Boolean checkSameName = checkSameName(taskSaveRequest.getTaskInfo().getTaskCode());
        checkSameName = checkSameName && checkSameName(taskSaveRequest.getTaskInfo().getTaskName());
        if (!checkSameName) {
            throw new ExistException("????????????????????????????????????");
        }

        DetailTaskVo detailTaskVo = opsData.saveDetailTaskVo(principal, taskSaveRequest);
        if (ObjectUtils.isNull(detailTaskVo)) {
            return false;
        }
        return true;
    }

    public Boolean auditTask(TaskSaveRequest taskSaveRequest) throws FeignException, DataOpsException, ExistException {
        UserPrincipal principal = userServer.getPrincipal();
        //??????SQL?????????????????????????????????????????????????????? ????????????????????????????????????
        checkSqlScript(taskSaveRequest);

        // ?????????????????????????????????????????????
        Boolean checkSameName = checkSameName(taskSaveRequest.getTaskInfo().getTaskCode());
        checkSameName = checkSameName && checkSameName(taskSaveRequest.getTaskInfo().getTaskName());
        if (!checkSameName) {
            throw new ExistException("????????????????????????????????????");
        }

        DetailTaskVo detailTaskVo = opsData.auditTask(principal, taskSaveRequest);

        if (ObjectUtils.isNull(detailTaskVo)) {
            return false;
        }
        return true;

    }

    public Boolean auditTaskCallback(Long taskId, String taskVersion, String auditProcessId) throws ExistException, FeignException, DataOpsException {
        UserPrincipal principal = userServer.getPrincipal();

        return opsData.auditTaskCallback(principal, taskId, taskVersion, auditProcessId);
    }


    public Boolean releaseTask(Long taskId, String taskVersion) throws ExistException, FeignException {
        UserPrincipal principal = userServer.getPrincipal();

        boolean flag = opsData.releaseTask(principal, taskId, taskVersion);


        flag = flag && opsLogic.online(principal,
                taskId,
                taskVersion);


        // ????????????????????????????????????
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????true
        flag = flag && modifyOfflineHistory(taskId, taskVersion);

        return flag;
    }


    private boolean modifyOfflineHistory(Long taskId, String taskVersion) throws ExistException {
        boolean usingVersionFlag =
                opsLogic.checkUsingVersion(taskId, taskVersion, TaskLogicStateEnum.Online.getValue());

        System.out.println("-------------?????????????????????????????????" + usingVersionFlag);

        if (!usingVersionFlag) {
            return true;
        }

        boolean flag = true;

        List<RtTaskLogicState> rtTaskLogicStateList =
                taskLogicStateService.lambdaQuery()
                        .eq(RtTaskLogicState::getTaskId, taskId)
                        .eq(RtTaskLogicState::getTaskState, TaskLogicStateEnum.Online.getValue())
                        .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue())
                        .orderByDesc(RtTaskLogicState::getStateId)
                        .list();

        if (null == rtTaskLogicStateList || rtTaskLogicStateList.size() == 0) {
            throw new ExistException("????????????????????????????????????");
        }

        // ?????????????????????????????????????????????????????????????????????
        if (rtTaskLogicStateList.size() == 1) {
            return true;
        }

        for (int i = 1; i < rtTaskLogicStateList.size(); i++) {
            RtTaskLogicState rtTaskLogicState = rtTaskLogicStateList.get(i);
            System.out.println("------------????????????????????????"
                    + rtTaskLogicState.getTaskId() + ", " + rtTaskLogicState.getTaskVersion());
            flag = flag && opsLogic.offline(userPrincipal, rtTaskLogicState.getTaskId(), rtTaskLogicState.getTaskVersion());
        }
        return flag;
    }


    public boolean checkSameName(String taskName) throws ExistException {
        // 1 ??????????????????
        RtTaskInfo rtTaskInfo = taskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskCode, taskName)
                        .or()
                        .eq(RtTaskInfo::getTaskName, taskName),
                false);
        if (ObjectUtils.isNotNull(rtTaskInfo)) {
            throw new ExistException("??????????????????????????????");
        }
        return true;
    }


    /**
     * 2.1 ?????????????????????????????????
     *
     * @param current
     * @param size
     * @return
     * @throws FeignException
     */
    public IPage<TaskInfoOnlineVo> listTaskInfoOnline(
            long current,
            int size) throws FeignException {
        /**
         * 1 ????????????????????????
         * 2 ???????????????????????????????????????????????????????????????????????????????????????????????????
         * 3 ??????????????????????????????
         * 4 ??????????????????????????????????????????
         * 5 ???????????????????????????TaskInfoOnlineVO
         */

        // 1 ????????????????????????
        Long teamId = userServer.getPrincipal().getDefaultTeamId();

        IPage<RtTaskLogicState> rtTaskLogicStateInfoPage = new Page<>(current, size);
        taskLogicStateService.page(
                rtTaskLogicStateInfoPage,
                Wrappers.<RtTaskLogicState>lambdaQuery()
                        .eq(RtTaskLogicState::getCrtTeam, teamId)
                        .eq(RtTaskLogicState::getTaskState, TaskLogicStateEnum.Online.getValue())
                        .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue())
                        .groupBy(RtTaskLogicState::getTaskId)
                        .orderByDesc(RtTaskLogicState::getTaskId));

        // 2 ???????????????????????????????????????
        List<RtTaskLogicState> taskLogicStateList = rtTaskLogicStateInfoPage.getRecords();

        List<TaskInfoOnlineVo> taskInfoOnlineVoList = new ArrayList<>();
        for (RtTaskLogicState rtTaskLogicState : taskLogicStateList) {
            TaskInfoOnlineVo taskInfoOnlineVo = getTaskInfoOnlineVo(rtTaskLogicState.getTaskId());
            taskInfoOnlineVoList.add(taskInfoOnlineVo);
        }

        IPage<TaskInfoOnlineVo> taskInfoOnlineVoPage =
                new Page<>(current, size,
                        rtTaskLogicStateInfoPage.getTotal(),
                        rtTaskLogicStateInfoPage.isSearchCount());
        taskInfoOnlineVoPage.setRecords(taskInfoOnlineVoList);

        return taskInfoOnlineVoPage;
    }

    /**
     * 2.2 ??????-???????????????
     * ???????????????????????????????????????
     *
     * @param taskName ?????????
     * @param current
     * @param size
     * @return
     */
    public IPage<TaskInfoOnlineVo> listTaskInfoOnlineByName(
            String taskName,
            Long current, Integer size) throws ExistException, FeignException {

        taskName = transSpecialString(taskName);

        // ????????????????????????
        Long teamId = userServer.getPrincipal().getDefaultTeamId();

        // 1 ??????????????????
        Page<RtTaskLogicState> rtTaskLogicStateInfoPage = new Page<>(current, size);
        taskLogicStateService.pageRtTaskLogicStateByTaskName(
                rtTaskLogicStateInfoPage,
                teamId,
                taskName,
                TaskLogicStateEnum.Online.getValue());

        // 2 ???????????????????????????????????????
        List<RtTaskLogicState> taskLogicStateList = rtTaskLogicStateInfoPage.getRecords();

        List<TaskInfoOnlineVo> taskInfoOnlineVoList = new ArrayList<>();
        for (RtTaskLogicState rtTaskLogicState : taskLogicStateList) {
            TaskInfoOnlineVo taskInfoOnlineVo = getTaskInfoOnlineVo(rtTaskLogicState.getTaskId());
            taskInfoOnlineVoList.add(taskInfoOnlineVo);
        }

        IPage<TaskInfoOnlineVo> taskInfoOnlineVoPage =
                new Page<>(current, size,
                        rtTaskLogicStateInfoPage.getTotal(),
                        rtTaskLogicStateInfoPage.isSearchCount());
        taskInfoOnlineVoPage.setRecords(taskInfoOnlineVoList);

        return taskInfoOnlineVoPage;
    }

    private String transSpecialString(String taskName) {
        taskName = taskName.replaceAll("%", "\\\\%");
        taskName = taskName.replaceAll("_", "\\\\_");
        return taskName;
    }

    private TaskInfoOnlineVo getTaskInfoOnlineVo(Long taskId) {
        List<RtTaskLogicState> rtTaskLogicStateList =
                taskLogicStateService.lambdaQuery()
                        .eq(RtTaskLogicState::getTaskId, taskId)
                        .eq(RtTaskLogicState::getTaskState, TaskLogicStateEnum.Online.getValue())
                        .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue())
                        .orderByAsc(RtTaskLogicState::getStateId)
                        .list();

        RtTaskLogicState usingVersionRtTaskLogicState =
                rtTaskLogicStateList.get(rtTaskLogicStateList.size() - 1);

        for (RtTaskLogicState rtTaskLogicState : rtTaskLogicStateList) {
            // 1 ?????????????????????
            if (opsLogic.checkUsingVersion(
                    rtTaskLogicState.getTaskId(), rtTaskLogicState.getTaskVersion(),
                    TaskLogicStateEnum.Online.getValue())) {
                usingVersionRtTaskLogicState = rtTaskLogicState;
            }
        }
        return getTaskInfoOnlineVo(usingVersionRtTaskLogicState);
    }


    private TaskInfoOnlineVo getTaskInfoOnlineVo(RtTaskLogicState rtTaskLogicState) {

        // ???????????????id?????????
        Long taskId = rtTaskLogicState.getTaskId();
        String taskVersion = rtTaskLogicState.getTaskVersion();

        DetailTaskInfoBo detailTaskInfoBo = detailTaskInfoBoService.getTaskDetailInfoBo(taskId, taskVersion);

        RtTaskLastState rtTaskLastState = taskLastStateService.getOne(
                Wrappers.<RtTaskLastState>lambdaQuery()
                        .eq(RtTaskLastState::getTaskId, taskId)
                        .eq(RtTaskLastState::getTaskVersion, taskVersion)
                        .eq(RtTaskLastState::getState, RecordStateEnum.StateUse.getValue()),
                false);

        TaskInfoOnlineVo taskInfoOnlineVo = new TaskInfoOnlineVo();
        taskInfoOnlineVo.setTaskId(taskId);
        taskInfoOnlineVo.setTaskCode(detailTaskInfoBo.getRtTaskInfo().getTaskCode());
        taskInfoOnlineVo.setTaskName(detailTaskInfoBo.getRtTaskInfo().getTaskName());
        taskInfoOnlineVo.setTaskVersion(taskVersion);
        taskInfoOnlineVo.setEngine(detailTaskInfoBo.getRtFlowInfo().getEngine());
        taskInfoOnlineVo.setTaskState(rtTaskLastState.getTaskState());
        taskInfoOnlineVo.setTaskStateShow(OpsNameEnum.getShowByValue(rtTaskLastState.getTaskState()));
        taskInfoOnlineVo.setIpAddress(getIpAddress(detailTaskInfoBo.getRtExeMachineList()));
        taskInfoOnlineVo.setLastStartTime(getLastStartTime(taskId, taskVersion));
        taskInfoOnlineVo.setLastEndTime(getLastEndTime(taskId, taskVersion));
        taskInfoOnlineVo.setCrtTime(rtTaskLogicState.getCrtTime().format(CommonConstants.dateTimeFormatter));
        taskInfoOnlineVo.setOpsTime(rtTaskLastState.getOpsTime().format(CommonConstants.dateTimeFormatter));
        List<String> updateVersionList = getUpdateVersion(taskId, taskVersion);
        taskInfoOnlineVo.setUpdateVersion(updateVersionList);
        taskInfoOnlineVo.setUpdateFlag(isUpdate(updateVersionList));

        return taskInfoOnlineVo;
    }

    private String getIpAddress(List<RtExeMachine> rtExeMachineList) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(rtExeMachineList.get(0).getIpAddress());
        for (int i = 1; i < rtExeMachineList.size(); i++) {
            stringBuffer.append(",").append(rtExeMachineList.get(i).getIpAddress());
        }
        return stringBuffer.toString();
    }

    private String getLastStartTime(Long taskId, String taskVersion) {
        List<String> startTypeOps =
                Arrays.asList(OpsNameEnum.Starting.getValue(),
                        OpsNameEnum.Restarting.getValue());

        RtOpsInfo rtOpsInfo =
                opsInfoService.getOne(
                        Wrappers.<RtOpsInfo>lambdaQuery()
                                .eq(RtOpsInfo::getTaskId, taskId)
                                .eq(RtOpsInfo::getTaskVersion, taskVersion)
                                .eq(RtOpsInfo::getState, RecordStateEnum.StateUse.getValue())
                                .orderByDesc(RtOpsInfo::getOpsId)
                                .in(RtOpsInfo::getOpsName, startTypeOps),
                        false);

        if (null == rtOpsInfo) {
            return PageNullFlag;
        }

        return rtOpsInfo.getCrtTime().format(CommonConstants.dateTimeFormatter);
    }

    private String getLastEndTime(Long taskId, String taskVersion) {
        List<String> startTypeOps =
                Arrays.asList(OpsNameEnum.StartException.getValue(),
                        OpsNameEnum.RestartException.getValue(),
                        OpsNameEnum.RunException.getValue(),
                        OpsNameEnum.Stop.getValue(),
                        OpsNameEnum.Stopping.getValue());

        RtOpsInfo rtOpsInfo =
                opsInfoService.getOne(
                        Wrappers.<RtOpsInfo>lambdaQuery()
                                .eq(RtOpsInfo::getTaskId, taskId)
                                .eq(RtOpsInfo::getTaskVersion, taskVersion)
                                .eq(RtOpsInfo::getState, RecordStateEnum.StateUse.getValue())
                                .orderByDesc(RtOpsInfo::getOpsId)
                                .in(RtOpsInfo::getOpsName, startTypeOps),
                        false);

        if (null == rtOpsInfo) {
            return PageNullFlag;
        }
        return rtOpsInfo.getCrtTime().format(CommonConstants.dateTimeFormatter);
    }

    private List<String> getUpdateVersion(Long taskId, String taskVersion) {
        RtTaskLogicState rtTaskLogicState =
                taskLogicStateService.getOne(
                        Wrappers.<RtTaskLogicState>lambdaQuery()
                                .eq(RtTaskLogicState::getTaskId, taskId)
                                .eq(RtTaskLogicState::getTaskVersion, taskVersion)
                                .eq(RtTaskLogicState::getTaskState, TaskLogicStateEnum.Online.getValue())
                                .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue()),
                        false);

        List<RtTaskLogicState> rtTaskLogicStateList =
                taskLogicStateService.lambdaQuery()
                        .eq(RtTaskLogicState::getTaskId, taskId)
                        .eq(RtTaskLogicState::getTaskState, TaskLogicStateEnum.Online.getValue())
                        .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue())
                        .orderByDesc(RtTaskLogicState::getStateId)
                        .list();

        List<String> updateVersionList = new ArrayList<>();
        for (RtTaskLogicState rtTaskLogicState1 : rtTaskLogicStateList) {
            if (rtTaskLogicState1.getStateId() > rtTaskLogicState.getStateId()) {
                updateVersionList.add(rtTaskLogicState1.getTaskVersion());
            }
        }
        return updateVersionList;
    }

    private boolean isUpdate(List<String> updateVersionList) {
        return null != updateVersionList && updateVersionList.size() != 0;
    }


    /**
     * 3.1 ??????-????????????
     * ?????????????????????
     *
     * @param current ??????
     * @param size    ????????????????????????
     * @return
     */
    public IPage<TaskInfoOfflineVo> listTaskInfoOffline(
            long current,
            int size) {
        /**
         * 1 ?????????????????????
         * 2 ????????????????????????????????????
         */
        // 1 ????????????????????????
        Long teamId = userServer.getPrincipal().getDefaultTeamId();

        IPage<RtTaskLogicState> rtTaskLogicStateInfoPage = new Page<>(current, size);
        taskLogicStateService.page(
                rtTaskLogicStateInfoPage,
                Wrappers.<RtTaskLogicState>lambdaQuery()
                        .eq(RtTaskLogicState::getCrtTeam, teamId)
                        .eq(RtTaskLogicState::getTaskState, TaskLogicStateEnum.Offline.getValue())
                        .eq(RtTaskLogicState::getState, RecordStateEnum.StateUse.getValue())
                        .orderByDesc(RtTaskLogicState::getStateId));

        List<RtTaskLogicState> taskLogicStateList = rtTaskLogicStateInfoPage.getRecords();

        List<TaskInfoOfflineVo> taskInfoOfflineVoList = new ArrayList<>();
        for (RtTaskLogicState rtTaskLogicState : taskLogicStateList) {
            TaskInfoOfflineVo taskInfoOnlineVo = getTaskInfoOfflineVo(rtTaskLogicState);
            taskInfoOfflineVoList.add(taskInfoOnlineVo);
        }

        IPage<TaskInfoOfflineVo> taskInfoOfflineVoPage =
                new Page<>(current, size,
                        rtTaskLogicStateInfoPage.getTotal(),
                        rtTaskLogicStateInfoPage.isSearchCount());
        taskInfoOfflineVoPage.setRecords(taskInfoOfflineVoList);

        return taskInfoOfflineVoPage;
    }


    /**
     * 3.2 ??????-???????????????
     * ???????????????????????????????????????
     *
     * @param taskName ?????????
     * @return
     */
    public IPage<TaskInfoOfflineVo> listTaskInfoOfflineByName(
            String taskName,
            Long current, Integer size) throws ExistException {

        taskName = transSpecialString(taskName);

        // ????????????????????????
        Long teamId = userServer.getPrincipal().getDefaultTeamId();

        Page<RtTaskLogicState> rtTaskLogicStateInfoPage = new Page<>(current, size);
        taskLogicStateService.pageRtTaskLogicStateByTaskNameOffline(
                rtTaskLogicStateInfoPage,
                teamId,
                taskName,
                TaskLogicStateEnum.Offline.getValue());

        List<RtTaskLogicState> taskLogicStateList = rtTaskLogicStateInfoPage.getRecords();

        List<TaskInfoOfflineVo> taskInfoOfflineVoList = new ArrayList<>();
        for (RtTaskLogicState rtTaskLogicState : taskLogicStateList) {
            TaskInfoOfflineVo taskInfoOnlineVo = getTaskInfoOfflineVo(rtTaskLogicState);
            taskInfoOfflineVoList.add(taskInfoOnlineVo);
        }

        IPage<TaskInfoOfflineVo> taskInfoOfflineVoPage =
                new Page<>(current, size,
                        rtTaskLogicStateInfoPage.getTotal(),
                        rtTaskLogicStateInfoPage.isSearchCount());
        taskInfoOfflineVoPage.setRecords(taskInfoOfflineVoList);

        return taskInfoOfflineVoPage;
    }

    private TaskInfoOfflineVo getTaskInfoOfflineVo(RtTaskLogicState rtTaskLogicState) {
        // ???????????????id?????????
        Long taskId = rtTaskLogicState.getTaskId();
        String taskVersion = rtTaskLogicState.getTaskVersion();

        RtTaskInfo rtTaskInfo = getRtTaskInfo(taskId, taskVersion);

        RtFlowInfo rtFlowInfo = getRtFlowInfo(taskId, taskVersion);

        RtOpsInfo rtOpsInfo =
                opsInfoService.getOne(
                        Wrappers.<RtOpsInfo>lambdaQuery()
                                .eq(RtOpsInfo::getTaskId, taskId)
                                .eq(RtOpsInfo::getTaskVersion, taskVersion)
                                .eq(RtOpsInfo::getOpsName, OpsNameEnum.Offline.getValue())
                                .eq(RtOpsInfo::getState, RecordStateEnum.StateUse.getValue())
                                .orderByDesc(RtOpsInfo::getOpsId),
                        false);

        TaskInfoOfflineVo taskInfoOfflineVo = new TaskInfoOfflineVo();
        taskInfoOfflineVo.setTaskId(taskId);
        taskInfoOfflineVo.setTaskCode(rtTaskInfo.getTaskCode());
        taskInfoOfflineVo.setTaskName(rtTaskInfo.getTaskName());
        taskInfoOfflineVo.setTaskVersion(rtTaskInfo.getTaskVersion());
        taskInfoOfflineVo.setEngine(rtFlowInfo.getEngine());
        //
        if (ObjectUtils.isNotNull(rtOpsInfo) && !StringUtils.isEmpty(rtOpsInfo.getOpsUser())) {
            taskInfoOfflineVo.setOfflineUser(getUserName(rtOpsInfo.getOpsUser()));
        }
        if (ObjectUtils.isNotNull(rtOpsInfo) && !StringUtils.isEmpty(rtOpsInfo.getCrtTime())) {
            taskInfoOfflineVo.setOfflineTime(rtOpsInfo.getCrtTime().format(CommonConstants.dateTimeFormatter));
        }

        return taskInfoOfflineVo;
    }

    @Autowired
    private ISysUserService sysUserService;

    private String getUserName(Long userId) {
        SysUser sysUser = sysUserService.getById(userId);
        return sysUser.getUsername();
    }


    @Autowired
    private IMdVersionManageService mdVersionManageService;


    /**
     * 4.1 ??????-????????????
     * ?????????????????????
     *
     * @param current ??????
     * @param size    ????????????????????????
     * @return
     */
    public IPage<TaskInfoOtherVo> listTaskInfoOther(
            long current,
            int size) throws FeignException {

        // ????????????????????????
        Long teamId = userServer.getPrincipal().getDefaultTeamId();

        RetDataMsg<Long> retDataMsgMappingId = versionServiceFeign.getMappingId(VersionMappingTypeTask, VersionMappingNameTask);
        if (!retDataMsgMappingId.getSuccess()
                || null == retDataMsgMappingId.getData()) {
            throw new FeignException("??????????????????????????????????????????");
        }

//        List<String> versionStateList =
//                Arrays.asList(VersionConstants.VER_STATE_TREL, VersionConstants.VER_STATE_REL,VersionConstants.VER_STATE_DEV,VersionConstants.VER_STATE_HIS);

//        IPage<MdVersionManage> mdVersionManageIPage = new Page<>(current, size);
       /* mdVersionManageService.page(
                mdVersionManageIPage,
                Wrappers.<MdVersionManage>lambdaQuery()
                        .eq(MdVersionManage::getCrtTeam, teamId)
                        .eq(MdVersionManage::getMdType, retDataMsgMappingId.getData())
                        .in(MdVersionManage::getMdVersionState, versionStateList)
                        .orderByDesc(MdVersionManage::getMdId)
                        .orderByDesc(MdVersionManage::getMdVersion));*/
        Page<MdVersionManage> mdVersionManageIPage = new Page<>(current, size);
        mdVersionManageService.pageMdVersionManageNew(
                mdVersionManageIPage,
                teamId,
                retDataMsgMappingId.getData(),
                null);

        List<TaskInfoOtherVo> taskInfoOtherVoList = new ArrayList<>();
        for (MdVersionManage mdVersionManage : mdVersionManageIPage.getRecords()) {
            taskInfoOtherVoList.add(getTaskInfoOtherVo(mdVersionManage));
        }

        IPage<TaskInfoOtherVo> taskInfoOtherVoIPage =
                new Page<>(current, size,
                        mdVersionManageIPage.getTotal(),
                        mdVersionManageIPage.isSearchCount());
        taskInfoOtherVoIPage.setRecords(taskInfoOtherVoList);

        return taskInfoOtherVoIPage;
    }


    /**
     * 4.2 ??????-???????????????
     * ???????????????????????????????????????
     *
     * @param taskName ?????????
     * @param current
     * @param size
     * @return
     */
    public IPage<TaskInfoOtherVo> listTaskInfoOtherByName(
            String taskName,
            long current, int size) throws ExistException, FeignException {

        taskName = transSpecialString(taskName);

        // ????????????????????????
        Long teamId = userServer.getPrincipal().getDefaultTeamId();

        RetDataMsg<Long> retDataMsgMappingId = versionServiceFeign.getMappingId(VersionMappingTypeTask, VersionMappingNameTask);
        if (!retDataMsgMappingId.getSuccess()
                || null == retDataMsgMappingId.getData()) {
            throw new FeignException("??????????????????????????????????????????");
        }

        List<String> versionStateList =
                Arrays.asList(VersionConstants.VER_STATE_TREL, VersionConstants.VER_STATE_REL, VersionConstants.VER_STATE_DEV, VersionConstants.VER_STATE_HIS);

        Page<MdVersionManage> mdVersionManageIPage = new Page<>(current, size);
        /*mdVersionManageService.pageMdVersionManageByTaskName(
                mdVersionManageIPage,
                teamId,
                retDataMsgMappingId.getData(),
                versionStateList,
                taskName);*/

        mdVersionManageService.pageMdVersionManageNew(
                mdVersionManageIPage,
                teamId,
                retDataMsgMappingId.getData(),
                taskName);

        List<TaskInfoOtherVo> taskInfoOtherVoList = new ArrayList<>();
        for (MdVersionManage mdVersionManage : mdVersionManageIPage.getRecords()) {
            taskInfoOtherVoList.add(getTaskInfoOtherVo(mdVersionManage));
        }

        IPage<TaskInfoOtherVo> taskInfoOtherVoIPage =
                new Page<>(current, size,
                        mdVersionManageIPage.getTotal(),
                        mdVersionManageIPage.isSearchCount());
        taskInfoOtherVoIPage.setRecords(taskInfoOtherVoList);

        return taskInfoOtherVoIPage;
    }

    private TaskInfoOtherVo getTaskInfoOtherVo(MdVersionManageVo mdVersionManage) {
        TaskInfoOtherVo taskInfoOtherVo = new TaskInfoOtherVo();

        RtTaskInfo rtTaskInfo = getRtTaskInfo(mdVersionManage.getMdId(), mdVersionManage.getMdVersion());
        RtFlowInfo rtFlowInfo = getRtFlowInfo(mdVersionManage.getMdId(), mdVersionManage.getMdVersion());

        taskInfoOtherVo.setTaskId(rtTaskInfo.getTaskId());
        taskInfoOtherVo.setTaskCode(rtTaskInfo.getTaskCode());
        taskInfoOtherVo.setTaskName(rtTaskInfo.getTaskName());
        taskInfoOtherVo.setTaskVersion(rtTaskInfo.getTaskVersion());
        taskInfoOtherVo.setEngine(rtFlowInfo.getEngine());
        taskInfoOtherVo.setActionUser(mdVersionManage.getCrtUserName());
        taskInfoOtherVo.setActionType(versionStateTrans(mdVersionManage.getMdVersionState()));
        taskInfoOtherVo.setActionTime(mdVersionManage.getCrtTime().format(CommonConstants.dateTimeFormatter));

        return taskInfoOtherVo;
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


    private TaskInfoOtherVo getTaskInfoOtherVo(MdVersionManage mdVersionManage) {
        TaskInfoOtherVo taskInfoOtherVo = new TaskInfoOtherVo();

        RtTaskInfo rtTaskInfo = getRtTaskInfo(mdVersionManage.getMdId(), mdVersionManage.getMdVersion());
        RtFlowInfo rtFlowInfo = getRtFlowInfo(mdVersionManage.getMdId(), mdVersionManage.getMdVersion());

        taskInfoOtherVo.setTaskId(rtTaskInfo.getTaskId());
        taskInfoOtherVo.setTaskCode(rtTaskInfo.getTaskCode());
        taskInfoOtherVo.setTaskName(rtTaskInfo.getTaskName());
        taskInfoOtherVo.setTaskVersion(rtTaskInfo.getTaskVersion());
        taskInfoOtherVo.setEngine(rtFlowInfo.getEngine());
        taskInfoOtherVo.setActionUser(getUserName(mdVersionManage.getCrtUser()));
        taskInfoOtherVo.setActionType(versionStateTrans(mdVersionManage.getMdVersionState()));
        taskInfoOtherVo.setActionTime(mdVersionManage.getMdfyTime().format(CommonConstants.dateTimeFormatter));

        return taskInfoOtherVo;
    }


    private RtTaskInfo getRtTaskInfo(Long taskId, String taskVersion) {
        RtTaskInfo rtTaskInfo = taskInfoService.getOne(
                Wrappers.<RtTaskInfo>lambdaQuery()
                        .eq(RtTaskInfo::getTaskId, taskId)
                        .eq(RtTaskInfo::getTaskVersion, taskVersion),
                false);
        return rtTaskInfo;
    }

    private RtFlowInfo getRtFlowInfo(Long taskId, String taskVersion) {
        RtTaskFlowRelative taskFlowRelative = taskFlowRelativeService.getOne(
                Wrappers.<RtTaskFlowRelative>lambdaQuery()
                        .eq(RtTaskFlowRelative::getTaskId, taskId)
                        .eq(RtTaskFlowRelative::getTaskVersion, taskVersion),
                false);

        RtFlowInfo rtFlowInfo = flowInfoService.getOne(
                Wrappers.<RtFlowInfo>lambdaQuery()
                        .eq(RtFlowInfo::getFlowId, taskFlowRelative.getFlowId())
                        .eq(RtFlowInfo::getFlowVersion, taskFlowRelative.getFlowVersion()),
                false);

        return rtFlowInfo;
    }


    /**
     * ????????????ID??????????????????
     *
     * @param pluginId
     * @return
     */
    public JSONObject getPluginElements(String pluginId) throws ExistException {

        List<Map<String, Object>> elementslist = null;
        JSONObject form = new JSONObject();
        try {
            //????????????????????????
            RtPluginsInfo pluginsInfo = pluginsInfoService.getById(pluginId);
            Map<String, Object> pluginsInfomap = new HashMap<>();
            pluginsInfomap.put("pluginsVersion", pluginsInfo.getPluginsVersion());
            pluginsInfomap.put("pluginId", pluginsInfo.getPluginId());
            pluginsInfomap.put("pluginCode", pluginsInfo.getPluginCode());
            pluginsInfomap.put("pluginName", pluginsInfo.getPluginName());
            form.put("pluginsInfo", pluginsInfomap);

            //????????????????????????
            elementslist = pluginsInfoService.getPluginElements(pluginId);
            form.put("elementslist", elementslist);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return form;
    }


    /**
     * ????????????ID????????????????????????????????????
     *
     * @param flowId      ??????ID
     * @param flowVersion ?????????
     * @return
     */
    public JSONObject getPluginCfgInfo(String flowId, String flowVersion) throws ExistException {


        JSONObject form = new JSONObject();
        try {
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("flowId", flowId);
            parammap.put("flowVersion", flowVersion);
            List<Map<String, Object>> pluginInstNolist = new ArrayList<>();
            //?????????????????????
            pluginInstNolist = pluginsInfoService.getPluginInstNo(parammap);
            if (pluginInstNolist != null && pluginInstNolist.size() > 0) {
                for (Map<String, Object> InstNo : pluginInstNolist) {
                    String inst_no = InstNo.get("inst_no").toString();
                    parammap.put("inst_no", inst_no);
                    List<Map<String, Object>> resultlist = new ArrayList<>();
                    //??????????????????
                    List<Map<String, Object>> pluginOneLevelList = pluginsInfoService.getPluginOneLevel(parammap);
                    if (pluginOneLevelList != null && pluginOneLevelList.size() > 0) {
                        for (Map<String, Object> pluginOneLevel : pluginOneLevelList) {
                            String cfg_key_id = pluginOneLevel.get("cfg_key_id").toString();
                            String cfg_key = pluginOneLevel.get("cfg_key").toString();
                            String cfg_value = pluginOneLevel.get("cfg_value").toString();
                            parammap.put("up_cfg_key_id", cfg_key_id);
                            //??????????????????
                            List<Map<String, Object>> pluginOtherLevelList = pluginsInfoService.getPluginOtherLevel(parammap);
                            if (pluginOtherLevelList != null && pluginOtherLevelList.size() > 0) {
                                Map<String, Object> resultmap = new HashMap<>();
                                for (Map<String, Object> pluginOtherLevel : pluginOtherLevelList) {
                                    String cfg_key_id_3 = pluginOtherLevel.get("cfg_key_id").toString();
                                    String cfg_key_3 = pluginOtherLevel.get("cfg_key").toString();
                                    String cfg_value_3 = pluginOtherLevel.get("cfg_value").toString();
                                    parammap.put("up_cfg_key_id", cfg_key_id_3);
                                    //??????????????????
                                    List<Map<String, Object>> pluginOtherLevelList_3 = pluginsInfoService.getPluginOtherLevel(parammap);
                                    if (pluginOtherLevelList_3 != null && pluginOtherLevelList_3.size() > 0) {
                                        Map<String, Object> resultmap_3 = new HashMap<>();
                                        for (Map<String, Object> pluginOtherLevel_3 : pluginOtherLevelList_3) {
                                            String cfg_key_id_4 = pluginOtherLevel_3.get("cfg_key_id").toString();
                                            String cfg_key_4 = pluginOtherLevel_3.get("cfg_key").toString();
                                            String cfg_value_4 = pluginOtherLevel_3.get("cfg_value").toString();
                                            resultmap_3.put(cfg_key_4, cfg_value_4);
                                        }
                                        resultmap.put(cfg_key_3, resultmap_3);
                                    } else {
                                        resultmap.put(cfg_key_3, cfg_value_3);
                                    }
                                }
                                Map<String, Object> resultmap2 = new HashMap<>();
                                resultmap2.put(cfg_key, resultmap);
                                resultlist.add(resultmap2);
                            } else {
                                Map<String, Object> resultmap = new HashMap<>();
                                resultmap.put(cfg_key, cfg_value);
                                resultlist.add(resultmap);
                            }
                        }
                    }
                    form.put(inst_no, resultlist);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return form;
    }


    public List getPlugInfo(String engine) {
        class PlugInfoType {
            public String typeName;

            public final List<Map<String, Object>> listMap = new ArrayList<>();
        }

        List<PlugInfoType> plugInfoTypeList = new ArrayList<>();

        pluginsInfoService.selectPlugByEngine(engine).forEach(listMap -> {
            PlugInfoType plugInfoType = new PlugInfoType();
            plugInfoType.typeName = listMap.get("typeName").toString();
            listMap.remove("typeName");
            plugInfoType.listMap.add(listMap);
            plugInfoTypeList.add(plugInfoType);
        });

        return plugInfoTypeList;
    }

    /**
     * ??????SQL?????????????????????????????????????????????????????? ????????????????????????????????????
     *
     * @return
     */
    public void checkSqlScript(TaskSaveRequest taskSaveRequest) throws DataOpsException {

        //??????????????????
        List<RtFlowPluginRelativeRequest> pluginInfoList = taskSaveRequest.getPluginInfoList();
        List<String> checkSqlList = new ArrayList<>();
        if (null != pluginInfoList && pluginInfoList.size() > 0) {
            for (RtFlowPluginRelativeRequest pluginInfo : pluginInfoList) {
                String pluginId = String.valueOf(pluginInfo.getPluginId());
                String execArg = pluginInfo.getExecArg();
                //??????????????????
                String check_type = pluginsInfoService.selectPluginCheck(pluginId);
                if ("FLINKSQL".equals(check_type)) {
                    // ????????????????????????????????????
                    RtPluginsInfo rtPluginsInfo = pluginsInfoService.getById(pluginId);
                    String pluginName = rtPluginsInfo.getPluginName();
                    String check = GenerSecurityPWD.parseFlinkSql(execArg, pluginName);
                    if (!StringUtils.isEmpty(check)) {
                        checkSqlList.add(check);
                    }
                }
//                if ("FLINKSQL_SOURCE".equals(check_type)) {
//                    //????????????  CREATE TABLE......WITH().....??????  ???WITH(???????????????????????????)
//                    int index = execArg.indexOf("CREATE TABLE");
//                    if (index > -1) {
//                        String execArg_with = execArg.substring(index, execArg.length());
//                        int index_with = execArg_with.indexOf("WITH");
//                        if (index_with == -1) {
//                            throw new DataOpsException("??????pluginId=" + pluginId + "??????????????????????????????CREATE TABLE????????????WITH");
//                        } else {
//                            String execArg_other = execArg.substring(index_with, execArg_with.length());
//                            if (execArg_other.indexOf("(") > -1 && execArg_other.indexOf(")") > -1) {
//                                String execArg_with_value = execArg_other.substring(execArg_other.indexOf("(") + 1, execArg_other.indexOf(")"));
//                                execArg_with_value = execArg_with_value.replaceAll("'", "\"");
//                                execArg_with_value = "{" + execArg_with_value.replaceAll("=", ":") + "}";
//                                try {
//                                    Map<String, Object> withValueMap = JSON.parseObject(execArg_with_value, Map.class);
//                                    for (Map.Entry<String, Object> entry : withValueMap.entrySet()) {
//                                        //??????WITH()???????????????
//                                        if (null == entry.getValue() || "".equals(entry.getValue())) {
//                                            throw new DataOpsException("??????pluginId=" + pluginId + "??????????????????????????????WITH()???????????????????????????");
//                                        }
//                                    }
//                                } catch (Exception e) {
//                                    throw new DataOpsException("??????pluginId=" + pluginId + "???????????????????????????");
//                                }
//                            } else {
//                                throw new DataOpsException("??????pluginId=" + pluginId + "??????????????????????????????CREATE TABLE......WITH????????????()");
//                            }
//                        }
//                    }
//                } else if ("FLINKSQL_SQL".equals(check_type)) {
//                    //????????????????????????   ??????(FOR SYSTEM_TIME AS OF a.procTime)??????
//                    execArg = execArg.replaceAll("\\r\\n", "");
//                    if (execArg.indexOf("FOR") > -1) {
//                        String execArg_for = execArg.substring(execArg.indexOf("FOR") + 1, execArg.length());
//                        if (execArg.indexOf("AS OF") > -1) {
//                            execArg = execArg.replaceAll("FOR SYSTEM_TIME AS OF a.procTime", "");
//                        }
//                    }
//                    boolean result = checkSqlFormat(execArg);
//                    if (!result) {
//                        throw new DataOpsException("??????pluginId=" + pluginId + "??????????????????????????????SQL????????????");
//                    }
//
//                }
            }
            if (ObjectUtils.isNotNull(checkSqlList)) {
                throw new DataOpsException(org.apache.commons.lang.StringUtils.join(checkSqlList, ";"));
            }
        }
    }

    //SQL????????????
    public static boolean checkSqlFormat(String sql) {
        boolean result = true;
        List<SQLStatement> stateMentList = new ArrayList<>();
        SQLStatementParser parser = null;
        try {
            parser = SQLParserUtils.createSQLStatementParser(sql, "mysql");
            stateMentList = parser.parseStatementList();
        } catch (ParserException e) {
            result = false;
            System.out.println("SQL???????????????????????????" + e.getMessage());
        }
        return result;
    }


    /**
     * json???????????????????????????????????????????????????
     *
     * @return
     */
    public String checkPluginNoteSort(String jsonStr) throws DataOpsException {
        JSONArray jsonlist = JSONArray.parseArray(jsonStr);
        String resultstr = "???????????????";
        //??????????????????
        JSONArray pluginNoteList = new JSONArray();
        for (int i = 0; i < jsonlist.size(); i++) {
            JSONObject pluginNote = jsonlist.getJSONObject(i).getJSONObject("instBaseInfo");
            pluginNoteList.add(pluginNote);
            String instId = pluginNote.getString("instId");
        }

        //??????????????????????????????????????????  ????????????????????????
        Boolean startResult = false;
        Boolean endResult = false;
        Boolean nullResult = false;
        Boolean relateResult = true;
        String errorInstId = "";
        for (int i = 0; i < pluginNoteList.size(); i++) {
            JSONObject pluginNote = pluginNoteList.getJSONObject(i);
            String instId = pluginNote.getString("instId");
            String lastInstIds = pluginNote.getString("lastInstIds");
            String nextInstIds = pluginNote.getString("nextInstIds");
            //??????????????????????????????????????????
            if ("".equals(lastInstIds)) {
                startResult = true;
            }
            if ("".equals(nextInstIds)) {
                endResult = true;
            }
            //????????????????????????
            if ("".equals(lastInstIds) && "".equals(nextInstIds)) {
                nullResult = true;
                errorInstId = instId;
                break;
            }
            //??????????????????????????????
            if (!"".equals(lastInstIds)) {
                relateResult = checkRelateNote(pluginNoteList, lastInstIds, instId);
                if (!relateResult) {
                    errorInstId = instId;
                    break;
                }
            }
        }


        if (nullResult) {
            resultstr = "????????????????????????????????????" + errorInstId + ",????????????";
        } else {
            if (!startResult && !endResult) {
                resultstr = "???????????????????????????????????????????????????????????????,????????????";
            } else {
                if (!relateResult) {
                    resultstr = "????????????????????????????????????????????????,????????????:" + errorInstId + ",????????????";
                } else {
                    //????????????  dsf??????????????????
                    pluginNoteList.sort(Comparator.comparing(obj -> ((JSONObject) obj).getBigDecimal("instId")));
                    DsfCycle dsfCycle = new DsfCycle();

                    //???????????????
                    setDsfCycleLine(pluginNoteList, dsfCycle);
                    List<String> reslut = new ArrayList<>();
                    reslut = dsfCycle.find();
                    String smg = "";
                    for (String string : reslut) {
                        smg += string;
                        System.out.println("????????????" + string);
                    }
                    if (smg.indexOf("no cycle") == -1) {
                        resultstr = "???????????????????????????????????????" + smg + ",????????????";
                    }
                }
            }
        }

        return resultstr;
    }


    //?????? ??????????????????????????????????????????????????? ???????????????
    public boolean checkRelateNote(JSONArray pluginNoteList, String lastInstIds, String instId) {
        Boolean relateResult = false;
        for (int j = 0; j < pluginNoteList.size(); j++) {
            JSONObject pluginNote_j = pluginNoteList.getJSONObject(j);
            String nextInstIds_j = pluginNote_j.getString("nextInstIds");
            String instId_j = pluginNote_j.getString("instId");
            if (instId_j.equals(lastInstIds) && nextInstIds_j.indexOf(instId) > -1) {
                relateResult = true;
                break;
            }
        }
        return relateResult;
    }

    //???????????????
    public void setDsfCycleLine(JSONArray pluginNoteList, DsfCycle dsfCycle) {
        for (int i = 0; i < pluginNoteList.size(); i++) {
            JSONObject pluginNote = pluginNoteList.getJSONObject(i);
            String instIds = pluginNote.getString("instId");
            String nextInstIds = pluginNote.getString("nextInstIds");

            if (nextInstIds.indexOf(",") > -1) {
                String[] childNodeIds = nextInstIds.split(",");
                for (String childNode : childNodeIds) {
                    dsfCycle.addLine(instIds, childNode);
                    System.out.println("====" + instIds + "->" + childNode);
                }
            } else {
                dsfCycle.addLine(instIds, nextInstIds);
                System.out.println("====" + instIds + "->" + nextInstIds);
            }
        }
    }

}
