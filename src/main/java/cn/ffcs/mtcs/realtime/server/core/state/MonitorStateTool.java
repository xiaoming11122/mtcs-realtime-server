package cn.ffcs.mtcs.realtime.server.core.state;

import cn.ffcs.mtcs.realtime.common.entity.RtAppState;
import cn.ffcs.mtcs.realtime.server.constants.CommonConstants;
import cn.ffcs.mtcs.realtime.server.constants.EngineTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.ExeMachineKeyEnum;
import cn.ffcs.mtcs.realtime.server.constants.PseudCodeEnum;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.realtime.server.pojo.bo.ExeMachineBo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nemo
 * @version V1.0
 * @Description: .
 * @date 2020/7/7 17:41
 */
@Component
public class MonitorStateTool {

    /**
     * 创建伪代码的键值对
     *
     * @param exeMachineBo
     * @param rtAppState
     * @return
     */
    public Map<String, String> generatePseudCode(ExeMachineBo exeMachineBo,
                                                 RtAppState rtAppState) {
        Map<String, String> map = new HashMap<>();

        /*String CodeMonitorLib = "%CodeMonitorLib%";
        String CodeMonitorJar = "%CodeMonitorJar%";
        String CodeMonitorMain = "%CodeMonitorMain%";
        String CodeMonitorParam = "%CodeMonitorParam%";*/

        map.put(CommonConstants.CodeMonitorLib,
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.RealtimeMonitorJarLib.getValue()));

        map.put(CommonConstants.CodeMonitorJar,
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.RealtimeMonitorJar.getValue()));

        map.put(CommonConstants.CodeMonitorMain,
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.RealtimeMonitorMain.getValue()));

        map.put(CommonConstants.CodeMonitorParam, String.valueOf(rtAppState.getId()));

        return map;
    }

    public Map<String, String> generateDirPath(ExeMachineBo exeMachineBo,
                                               RtAppState rtAppState) {

        String shellDir =
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.RealtimeMonitorShellDir.getValue())
                        + "/" + rtAppState.getTaskId() + "-" + rtAppState.getTaskVersion()
                        + "/" + LocalDateTime.now().format(CommonConstants.dirDateTimeFormatter)
                        + "/" + LocalDateTime.now().getHour();

        String logExeDir =
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.RealtimeMonitorExeLogDir.getValue())
                        + "/" + rtAppState.getTaskId() + "-" + rtAppState.getTaskVersion()
                        + "/" + LocalDateTime.now().format(CommonConstants.dirDateTimeFormatter)
                        + "/" + LocalDateTime.now().getHour();

        String logRunDir =
                exeMachineBo.getExeMachineEnv()
                        .get(ExeMachineKeyEnum.RealtimeMonitorRunLogDir.getValue())
                        + "/" + rtAppState.getTaskId() + "-" + rtAppState.getTaskVersion()
                        + "/" + LocalDateTime.now().format(CommonConstants.dirDateTimeFormatter)
                        + "/" + LocalDateTime.now().getHour();

        Map<String, String> pathMap = new HashMap<>();
        pathMap.put(ExeMachineKeyEnum.RealtimeMonitorShellDir.getValue(), shellDir);
        pathMap.put(ExeMachineKeyEnum.RealtimeMonitorExeLogDir.getValue(), logExeDir);
        pathMap.put(ExeMachineKeyEnum.RealtimeMonitorRunLogDir.getValue(), logRunDir);

        return pathMap;
    }

    public String generateFileName(RtAppState rtAppState) {
        String fileName =
                rtAppState.getMonitorId() +
                        "_" + rtAppState.getId() +
                        "-" + rtAppState.getMonitorCount();
        return fileName;
    }
}
