package cn.ffcs.mtcs.realtime.server.core.ops;

import cn.ffcs.mtcs.realtime.common.entity.*;
import cn.ffcs.mtcs.realtime.server.constants.*;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineTool;
import cn.ffcs.mtcs.realtime.server.core.engine.impl.EngineFlink;
import cn.ffcs.mtcs.realtime.server.service.data.*;
import cn.ffcs.mtcs.realtime.server.core.engine.EngineFactory;
import cn.ffcs.mtcs.realtime.server.core.engine.IEngine;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.ffcs.mtcs.ssh.common.request.ExeInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

import static cn.ffcs.mtcs.realtime.server.constants.CommonConstants.*;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/19/019 20:00
 * @Version 1.0
 */
@Component
@Slf4j
public class OpsRegister {

    @Autowired
    private IRtOpsInfoService opsInfoService;

    @Autowired
    private IRtTaskLastStateService taskLastStateInfoService;

    @Autowired
    private IRtOpsAttachmentService opsAttachmentService;

    @Autowired
    private IService<RtExeMetaInfo> exeMetaInfoService;

    @Autowired
    private IRtRetryService retryService;

    @Autowired
    IRtExeInfoService rtExeInfoService;


    @Autowired
    IRtExeMachineEnvService rtExeMachineEnvService;

    @Autowired
    IRtParamInfoService rtParamInfoService;

    Map<String, String> commandPseudCodeMap = new HashMap<>();


    public RtOpsInfo registerOpsInfo(UserPrincipal principal,
                                     Long taskId, String taskVersion,
                                     String opsType, String opsName) {
        RtOpsInfo rtOpsInfo =
                createRtOpsInfo(principal, taskId, taskVersion, opsType, opsName);
        opsInfoService.save(rtOpsInfo);
        return rtOpsInfo;
    }

    /**
     * 1 注册操作动作
     * 2 注册操作表
     * 3 更新任务最新状态
     */

    public RtOpsInfo registerOps(UserPrincipal principal,
                                 Long taskId, String taskVersion,
                                 String opsType, String opsName) {

        RtOpsInfo rtOpsInfo =
                createRtOpsInfo(principal, taskId, taskVersion, opsType, opsName);
        opsInfoService.save(rtOpsInfo);

        // 如果是修改、更新操作，就不把状态写到最新中，保持原来的状态
        if (OpsNameEnum.Modify.getValue().equals(opsName)
                || OpsNameEnum.Update.getValue().equals(opsName)) {
            return rtOpsInfo;
        }

        // 更新最新的任务状态
        RtTaskLastState rtTaskLastStateInfo = getRtTaskLastState(taskId, taskVersion);
        if (null == rtTaskLastStateInfo) {
            rtTaskLastStateInfo = createRtTaskLastState(taskId, taskVersion, rtOpsInfo.getOpsId(), opsName);
            taskLastStateInfoService.save(rtTaskLastStateInfo);
        } else {
            /*if (StringUtils.isEmpty(rtTaskLastStateInfo.getRunId())) {
                String uuid = java.util.UUID.randomUUID().toString();
                rtTaskLastStateInfo.setRunId(String.format("%s-%s", taskId, uuid ));
            }*/
            rtTaskLastStateInfo.setOpsId(rtOpsInfo.getOpsId());
            rtTaskLastStateInfo.setTaskState(opsName);
            taskLastStateInfoService.updateById(rtTaskLastStateInfo);
        }

        return rtOpsInfo;
    }


    private RtOpsInfo createRtOpsInfo(UserPrincipal principal,
                                      Long taskId, String taskVersion,
                                      String opsType, String opsName) {
        RtOpsInfo rtOpsInfo = new RtOpsInfo();

        rtOpsInfo.setTaskId(taskId);
        rtOpsInfo.setTaskVersion(taskVersion);
        rtOpsInfo.setOpsType(opsType);
        rtOpsInfo.setOpsName(opsName);
        rtOpsInfo.setOpsDesc("");
        rtOpsInfo.setOpsUser(principal.getUserId());
        rtOpsInfo.setOpsTeam(principal.getDefaultTeamId());
        rtOpsInfo.setCrtTime(LocalDateTime.now());
        rtOpsInfo.setState(RecordStateEnum.StateUse.getValue());
        return rtOpsInfo;
    }

    private RtTaskLastState getRtTaskLastState(Long taskId, String taskVersion) {
        RtTaskLastState rtTaskLastState =
                taskLastStateInfoService.getOne(
                        Wrappers.<RtTaskLastState>lambdaQuery()
                                .eq(RtTaskLastState::getTaskId, taskId)
                                .eq(RtTaskLastState::getTaskVersion, taskVersion)
                                .eq(RtTaskLastState::getState, RecordStateEnum.StateUse.getValue()),
                        false);
        return rtTaskLastState;
    }

    private String getRtTaskLastSavePoint(Long taskId, String taskVersion) {
        String savePoint = "";
        RtTaskLastState rtTaskLastState =
                taskLastStateInfoService.getOne(
                        Wrappers.<RtTaskLastState>lambdaQuery()
                                .eq(RtTaskLastState::getTaskId, taskId)
                                .isNotNull(RtTaskLastState::getSavepoint)
                                 .eq(RtTaskLastState::getState, RecordStateEnum.StateUse.getValue())
                                .orderByDesc(RtTaskLastState::getId),

                        false);
        if (rtTaskLastState != null) {
            savePoint = rtTaskLastState.getSavepoint();
        }
        return savePoint;
    }

    private RtTaskLastState createRtTaskLastState(Long taskId, String taskVersion,
                                                  Long opsId, String opsName) {
        RtTaskLastState rtTaskLastStateInfo = new RtTaskLastState();
        rtTaskLastStateInfo.setTaskId(taskId);
        rtTaskLastStateInfo.setTaskVersion(taskVersion);
        rtTaskLastStateInfo.setOpsId(opsId);
        rtTaskLastStateInfo.setTaskState(opsName);
        //增加应用执行的runid
        //String uuid = java.util.UUID.randomUUID().toString();
        //rtTaskLastStateInfo.setRunId(String.format("%s-%s", taskId, uuid ));
        rtTaskLastStateInfo.setState(RecordStateEnum.StateUse.getValue());
        return rtTaskLastStateInfo;
    }


    //map转string
    public static String getMapToString(Map<String, Object> map) {
        Set<String> keySet = map.keySet();
        //将set集合转换为数组
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        //给数组排序(升序)
        Arrays.sort(keyArray);
        //因为String拼接效率会很低的，所以转用StringBuilder
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            // 参数值为空，则不参与签名 这个方法trim()是去空格
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                sb.append(keyArray[i]).append(":").append(String.valueOf(map.get(keyArray[i])).trim());
            }
            if (i != keyArray.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private Map<String, String> getParamInfoMap(Long taskId, String taskVersion) {
        Map<String, String> paramInfoMap = new HashMap<>();
        List<RtParamInfo> rtParamInfoList =
                rtParamInfoService.list(new QueryWrapper<RtParamInfo>().lambda()
                        .eq(RtParamInfo::getTaskId,taskId)
                        .eq(RtParamInfo::getTaskVersion,taskVersion));
        if (rtParamInfoList != null && rtParamInfoList.size() > 0) {
            for (RtParamInfo rtParamInfo: rtParamInfoList) {
                paramInfoMap.put("%" + rtParamInfo.getParamKey() + "%", rtParamInfo.getParamValue());
            }
        }
        return paramInfoMap;
    }

    private Map <String, String> getCommandPseudCodeMap(String engineName, String opsName, RtExeMetaInfo rtExeMetaInfo,
                                                        RtOpsAttachment rtOpsAttachment, Long exeId) {
        if (commandPseudCodeMap.isEmpty()) {
            commandPseudCodeMap.put(PseudCodeEnum.SubmitType.getValue(), SubmitTypeEnum.Yarn.getValue());
            commandPseudCodeMap.put(PseudCodeEnum.OpsAttachmentId.getValue(), String.valueOf(rtOpsAttachment.getId()));
            commandPseudCodeMap.put(PseudCodeEnum.CodeRunId.getValue(), StringUtils.isNotEmpty(rtExeMetaInfo.getRunId()) ?
                    rtExeMetaInfo.getRunId() : "");
            //增加对savepoint处理
            if (EngineTypeEnum.Flink.getValue().equalsIgnoreCase(engineName) ||
                    EngineTypeEnum.Spark.getValue().equalsIgnoreCase(engineName)) {
                String savePoint = getRtTaskLastSavePoint(rtOpsAttachment.getTaskId(), rtOpsAttachment.getTaskVersion());
                log.debug("task_id:{},task_version:{},lastSavepoint:{}", rtOpsAttachment.getTaskId(),
                        rtOpsAttachment.getTaskVersion(), savePoint);
                commandPseudCodeMap.put(PseudCodeEnum.CodeLastSavepoint.getValue(), StringUtils.isNotEmpty(savePoint) ?
                        savePoint : "");
            }
            //增加参数的配置信息
            commandPseudCodeMap.putAll(getParamInfoMap(rtOpsAttachment.getTaskId(), rtOpsAttachment.getTaskVersion()));
            // TODO czs新增 设置停止任务CodeAppId伪代码
            if (EngineTypeEnum.Flume.getValue().equals(engineName)) {
                commandPseudCodeMap.put(CommonConstants.CodeAppId, rtExeMetaInfo.getRunId());
            }
        }

        if (OpsNameEnum.Stopping.getValue().equals(opsName)) {
            //获取执行信息
            if (exeId != null && exeId > 0) {
                RtExeInfo rtExeInfo = rtExeInfoService.getById(exeId);

                Map<String, String> resultParam = JSON.parseObject(rtExeInfo.getExeResultParam(), HashMap.class);
                log.debug("resultParam:{}", resultParam);
                // TODO 陈张圣添加-处理控制帧异常
                if (ObjectUtils.isNotEmpty(resultParam)) {
                    if (StringUtils.isNotEmpty(resultParam.get(EXEC_RESULT_APP_ID_KEY))) {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkAppId.getValue(), resultParam.get(EXEC_RESULT_APP_ID_KEY));
                    } else {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkAppId.getValue(), resultParam.get(ExeMachineKeyEnum.CodeFlinkAppId.getValue()));
                    }
                    if (StringUtils.isNotEmpty(resultParam.get(EXEC_RESULT_JOB_ID_KEY))) {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkJobId.getValue(), resultParam.get(EXEC_RESULT_JOB_ID_KEY));
                    } else {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkJobId.getValue(), resultParam.get(ExeMachineKeyEnum.CodeFlinkJobId.getValue()));
                    }
                    if (StringUtils.isNotEmpty(resultParam.get(EXEC_RESULT_WEB_URL_KEY))) {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkJobManager.getValue(), resultParam.get(EXEC_RESULT_WEB_URL_KEY));
                    } else {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkJobManager.getValue(), resultParam.get(ExeMachineKeyEnum.CodeFlinkJobManager.getValue()));
                    }
                }
            } else if (StringUtils.isNotEmpty(rtOpsAttachment.getExeResult())) {
                log.debug("从OpsAttachment的ExeResult值中获取appid:{}", rtOpsAttachment.getExeResult());
                commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkAppId.getValue(), rtOpsAttachment.getExeResult());
            }
        } else {
            commandPseudCodeMap.put(CodeFlumeParam, rtOpsAttachment.getId().toString());
        }
        return  commandPseudCodeMap;
    }
    public String getOpsCommand(String engineName, String opsName, RtExeMetaInfo rtExeMetaInfo,
                                RtOpsAttachment rtOpsAttachment, Long exeId) {
        IEngine iEngine = EngineFactory.getIEngine(engineName);

        String command = "";
        Map<String, Map> commandMap = new HashMap<>();
        if (StringUtils.isNotEmpty(rtExeMetaInfo.getExeCommand())) {
            commandMap = JSON.parseObject(rtExeMetaInfo.getExeCommand(), Map.class);
        }
        Map<String, String> commandPseudCodeMap = new HashMap<String, String>();
        commandPseudCodeMap.put(PseudCodeEnum.SubmitType.getValue(), SubmitTypeEnum.Yarn.getValue());
        commandPseudCodeMap.put(PseudCodeEnum.OpsAttachmentId.getValue(), String.valueOf(rtOpsAttachment.getId()));
        commandPseudCodeMap.put(PseudCodeEnum.CodeRunId.getValue(), StringUtils.isNotEmpty(rtExeMetaInfo.getRunId())?
                rtExeMetaInfo.getRunId():"");
        //增加对savepoint处理
        String savePoint = getRtTaskLastSavePoint(rtOpsAttachment.getTaskId(), rtOpsAttachment.getTaskVersion());
            log.debug("task_id:{},task_version:{},lastSavepoint:{}", rtOpsAttachment.getTaskId(),
                    rtOpsAttachment.getTaskVersion(), savePoint);
            commandPseudCodeMap.put(PseudCodeEnum.CodeLastSavepoint.getValue(), StringUtils.isNotEmpty(savePoint) ?
                    savePoint : "");

        //增加参数的配置信息
        commandPseudCodeMap.putAll(getParamInfoMap(rtOpsAttachment.getTaskId(), rtOpsAttachment.getTaskVersion()));
        // TODO czs新增 设置停止任务CodeAppId伪代码
        if (EngineTypeEnum.Flume.getValue().equals(engineName)) {
            commandPseudCodeMap.put(CommonConstants.CodeAppId, rtExeMetaInfo.getRunId());
        }

        String execCommand;
        //当前执行机ID
        String exeMetaId;
        if (OpsNameEnum.Stopping.getValue().equals(opsName)) {

            exeMetaId = String.valueOf(rtExeMetaInfo.getRunMachineId());
            //取该机器下对应命令
            Map<Object, Map> execCommandmap = commandMap.get(exeMetaId);
            Map<String, Object> execCommandmapSub = execCommandmap.get(CommonConstants.STOP_COMMAND_KEY);
            JSONArray jArray = new JSONArray();
            jArray.add(execCommandmapSub);
            execCommand = jArray.toString();
            //获取执行信息
            if (exeId != null && exeId > 0) {
                RtExeInfo rtExeInfo = rtExeInfoService.getById(exeId);

                Map<String, String> resultParam = JSON.parseObject(rtExeInfo.getExeResultParam(), HashMap.class);
                log.debug("resultParam:{}", resultParam);
                // TODO 陈张圣添加-处理空指针异常
                if (ObjectUtils.isNotEmpty(resultParam)) {
                    if (StringUtils.isNotEmpty(resultParam.get(EXEC_RESULT_APP_ID_KEY))) {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkAppId.getValue(), resultParam.get(EXEC_RESULT_APP_ID_KEY));
                    } else {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkAppId.getValue(), resultParam.get(ExeMachineKeyEnum.CodeFlinkAppId.getValue()));
                    }
                    if (StringUtils.isNotEmpty(resultParam.get(EXEC_RESULT_JOB_ID_KEY))) {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkJobId.getValue(), resultParam.get(EXEC_RESULT_JOB_ID_KEY));
                    } else {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkJobId.getValue(), resultParam.get(ExeMachineKeyEnum.CodeFlinkJobId.getValue()));
                    }
                    if (StringUtils.isNotEmpty(resultParam.get(EXEC_RESULT_WEB_URL_KEY))) {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkJobManager.getValue(), resultParam.get(EXEC_RESULT_WEB_URL_KEY));
                    } else {
                        commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkJobManager.getValue(), resultParam.get(ExeMachineKeyEnum.CodeFlinkJobManager.getValue()));
                    }
                }
            } else if (StringUtils.isNotEmpty(rtOpsAttachment.getExeResult())) {
                log.debug("从OpsAttachment的ExeResult值中获取appid:{}", rtOpsAttachment.getExeResult());
                commandPseudCodeMap.put(PseudCodeEnum.CodeFlinkAppId.getValue(), rtOpsAttachment.getExeResult());
            }
        } else {
            //获取执行信息  多个机器随机取一个 并记录执行机器id
            Random rand = new Random();
            String exeMachine = rtExeMetaInfo.getExeMachine();
            List<Map<Object, Object>> exeMachineList = (List<Map<Object, Object>>) JSONArray.parse(exeMachine);
            int i = rand.nextInt(exeMachineList.size());
            Map<Object, Object> exeMachinemap = exeMachineList.get(i);
            //机器id
            exeMetaId = String.valueOf(exeMachinemap.get("machineId"));
            //取该机器对应命令
            Map<String, Map> execCommandmap = commandMap.get(exeMetaId);
            Map<String, Object> execCommandmapSub = execCommandmap.get(CommonConstants.EXEC_COMMAND_KEY);
            JSONArray jArray = new JSONArray();
            jArray.add(execCommandmapSub);
            execCommand = jArray.toString();
            //更新当前执行机器id
            rtExeMetaInfo.setRunMachineId(Long.parseLong(exeMetaId + ""));
            exeMetaInfoService.updateById(rtExeMetaInfo);
            commandPseudCodeMap.put(CodeFlumeParam, rtOpsAttachment.getId().toString());
        }
        //获取基础路径
        String basePath = "";
        if (StringUtils.isNotEmpty(exeMetaId)) {
            Map<String,String> exeMachineEnvMap = rtExeMachineEnvService.getRtExeMachineEnvMap(Long.valueOf(exeMetaId));
            String basePathKey = "base_path";
            if (exeMachineEnvMap != null && exeMachineEnvMap.containsKey(basePathKey)) {
                basePath = exeMachineEnvMap.get(basePathKey);
            }
        }
        commandPseudCodeMap.putAll(iEngine.getPluginJarPseudCodeMap(command, basePath,
                rtOpsAttachment.getTaskId(), rtOpsAttachment.getTaskVersion()));

        log.debug("替换的伪码commandPseudCodeMap信息:{}", commandPseudCodeMap);

        command = iEngine.transCommandPseudCode(
                execCommand, commandPseudCodeMap);

        log.debug("生成的命令为:{}", command);
        return command;
    }

    /**
     * 注册执行参数信息
     *
     * @param opsId       操作ID
     * @param taskId      任务ID
     * @param taskVersion 任务版本
     * @param opsName     操作名称
     * @param engineName  执行引擎
     * @return
     */

    public RtOpsAttachment registerOpsAttachment(String opsId,
                                                 Long taskId, String taskVersion,
                                                 String opsName, String engineName) {
        RtExeMetaInfo rtExeMetaInfo = getRtExeMetaInfo(taskId, taskVersion);
        RtOpsAttachment rtOpsAttachment = new RtOpsAttachment();
        rtOpsAttachment.setOpsId(Long.parseLong(opsId));
        rtOpsAttachment.setTaskId(taskId);
        rtOpsAttachment.setTaskVersion(taskVersion);
        rtOpsAttachment.setExeMachine(rtExeMetaInfo.getExeMachine());
        rtOpsAttachment.setOpsCommand("");
        rtOpsAttachment.setOpsParam("");
        rtOpsAttachment.setRestartType(rtExeMetaInfo.getRestartType());
        rtOpsAttachment.setRestartInterval(rtExeMetaInfo.getRestartInterval());
        rtOpsAttachment.setRestartNum(rtExeMetaInfo.getRestartNum());
        rtOpsAttachment.setFailCount(0);
        // 默认值为-1L
        Long exeId = -1L;
        if (OpsNameEnum.Running.getValue().equals(opsName)
                || OpsNameEnum.Stopping.getValue().equals(opsName)) {
            exeId = getRunningExeId(taskId, taskVersion);
        }
        rtOpsAttachment.setExeId(exeId);
        rtOpsAttachment.setState(RecordStateEnum.StateUse.getValue());
        opsAttachmentService.save(rtOpsAttachment);
        IEngine iEngine = EngineFactory.getIEngine(engineName);
        String command = getOpsCommand(engineName, opsName, rtExeMetaInfo, rtOpsAttachment, exeId);
        //生成插件的jar包路径信息

        log.debug("生成的命令为:{}", command);

        String param =
                iEngine.transParamPseudCode(
                        rtExeMetaInfo.getExeParam(),
                        opsName,
                        String.valueOf(rtOpsAttachment.getId()));


        log.debug("生成的参数为:{}", param);

        List<ExeInfo> exeInfoList = JSON.parseArray(command, ExeInfo.class);
        if (exeInfoList != null && exeInfoList.size() > 0) {
            ExeInfo exeInfo = exeInfoList.get(0);
            //设置日志文件路径
            rtOpsAttachment.setExeLogFile(exeInfo.getExeLogFile());
        }
        rtOpsAttachment.setOpsCommand(command);
        rtOpsAttachment.setOpsParam(param);

        opsAttachmentService.updateById(rtOpsAttachment);

        return rtOpsAttachment;
    }

    public Long getRunningExeId(Long taskId, String taskVersion) {
        //临时更改只取运行的信息
        List<String> preOpsName =
                Arrays.asList(OpsNameEnum.Starting.getValue(),
                        OpsNameEnum.Restarting.getValue());
        /*List<String> preOpsName =
                Arrays.asList(OpsNameEnum.Running.getValue());*/
        RtOpsInfo rtOpsInfo =
                opsInfoService.getOne(
                        Wrappers.<RtOpsInfo>lambdaQuery()
                                .eq(RtOpsInfo::getTaskId, taskId)
                                .eq(RtOpsInfo::getTaskVersion, taskVersion)
                                .eq(RtOpsInfo::getState, RecordStateEnum.StateUse.getValue())
                                .in(RtOpsInfo::getOpsName, preOpsName)
                                .orderByDesc(RtOpsInfo::getOpsId),
                        false);

        RtOpsAttachment rtOpsAttachment =
                opsAttachmentService.getOne(
                        Wrappers.<RtOpsAttachment>lambdaQuery()
                                .eq(RtOpsAttachment::getOpsId, rtOpsInfo.getOpsId())
                                .eq(RtOpsAttachment::getState, RecordStateEnum.StateUse.getValue()),
                        false);

        return rtOpsAttachment.getExeId();
    }


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

    public RtRetry getRetry(Long id) {
        return retryService.getById(id);
    }

    public RtRetry registerRtRetry(Long taskId, String taskVersion,
                                   String opsName, int retryNum) {
        RtRetry rtRetry = new RtRetry();
        rtRetry.setTaskId(taskId);
        rtRetry.setTaskVersion(taskVersion);
        rtRetry.setOpsName(opsName);
        rtRetry.setRetryNum(retryNum);
        rtRetry.setFailCount(0);
        rtRetry.setCrtTime(LocalDateTime.now());
        rtRetry.setState(RecordStateEnum.StateUse.getValue());
        retryService.save(rtRetry);
        return rtRetry;
    }

}
