package cn.ffcs.mtcs.realtime.server.service.business;

import cn.ffcs.mtcs.realtime.common.entity.SysConfig;
import cn.ffcs.mtcs.realtime.common.entity.RtExeMachine;
import cn.ffcs.mtcs.realtime.common.vo.ParamDictionaryVo;
import cn.ffcs.mtcs.realtime.common.vo.base.ExeMachineVo;
import cn.ffcs.mtcs.realtime.server.constants.ExeParamTypeEnum;
import cn.ffcs.mtcs.realtime.server.constants.RecordStateEnum;
import cn.ffcs.mtcs.realtime.server.service.data.IRtExeMachineService;
import cn.ffcs.mtcs.realtime.server.service.data.ISysConfigService;
import cn.ffcs.mtcs.realtime.server.vo.EngineTypeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/8/008 15:46
 * @Version 1.0
 */
@Component
public class RealtimeOtherBusiness {

    /**
     * 1 引擎下拉框
     * 2 执行器复选下拉框
     * 3 处理方式下拉框
     * 4 获取应用参数字典表
     * 5 获取执行参数字典表
     */

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private IRtExeMachineService exeMachineService;


    /**
     * 系统配置表中的模块字段
     */
    @Value("${sys.config.module.realtime}")
    private String SysConfigModuleRealtime;

    /**
     * 系统配置表中的资源字段，引擎
     */
    @Value("${sys.config.source.engine}")
    private String SysConfigSourceEngine;

    /**
     * 系统配置表中的资源字段，异常处理方式
     */
    @Value("${sys.config.source.exceptionHandle}")
    private String SysConfigSourceExceptionHandle;

    /**
     * 系统配置表中的资源字段，引擎参数
     */
    @Value("${sys.config.source.param.prefix}")
    private String SysConfigSourceParamPrefix;


    /**
     * 1 引擎下拉框
     *
     * @return
     */
    public Map<String, String> listEngine() {
        /**
         * 在配置系统表时，一定要与constants.EngineTypeEnum中的一致
         */
        List<SysConfig> sysConfigList =
                sysConfigService.<SysConfig>lambdaQuery()
                        .eq(SysConfig::getCfgModule, SysConfigModuleRealtime)
                        .eq(SysConfig::getCfgSource, SysConfigSourceEngine)
                        .list();
        Map<String, String> data = new HashMap<>();
        for (SysConfig sysConfig : sysConfigList) {
            data.put(sysConfig.getCfgKey(), sysConfig.getCfgValueShow());
        }

        return data;
    }


    /**
     * 2 执行器复选下拉框
     *
     * @param nowTeam
     * @param engine
     * @return
     */
    public List<ExeMachineVo> listExeMachine(Long nowTeam, String engine) {
        /**
         * 1 获取用户的团队
         * 2 根据团队和引擎查询
         */
        List<RtExeMachine> exeMachineList =
                exeMachineService.<RtExeMachine>lambdaQuery()
                        .eq(RtExeMachine::getUseTeam, nowTeam)
                        .eq(RtExeMachine::getUseEngine, engine)
                        .eq(RtExeMachine::getState, RecordStateEnum.StateUse.getValue())
                        .list();

        List<ExeMachineVo> exeMachineVOList = new ArrayList<>();
        for (RtExeMachine rtExeMachine : exeMachineList) {
            ExeMachineVo exeMachineVO = new ExeMachineVo();
            exeMachineVO.setMachineId(rtExeMachine.getMachineId());
            exeMachineVO.setHostName(rtExeMachine.getHostName());
            exeMachineVO.setIpAddress(rtExeMachine.getIpAddress());
            exeMachineVOList.add(exeMachineVO);
        }
        return exeMachineVOList;
    }

    /**
     * 3 处理方式下拉框
     *
     * @return
     */
    public Map<String, String> listExceptionHandleType() {
        /**
         * 在配置系统表时，一定要与 constants.RestartTypeEnum 中的一致
         */
        List<SysConfig> sysConfigList =
                sysConfigService.<SysConfig>lambdaQuery()
                        .eq(SysConfig::getCfgModule, SysConfigModuleRealtime)
                        .eq(SysConfig::getCfgSource, SysConfigSourceExceptionHandle)
                        .list();
        Map<String, String> data = new HashMap<>();
        for (SysConfig sysConfig : sysConfigList) {
            data.put(sysConfig.getCfgKey(), sysConfig.getCfgValueShow());
        }

        return data;
    }

    /**
     * 4 获取应用参数字典表
     *
     * @param engine
     * @return
     */
    public List<ParamDictionaryVo> getAppParamDic(String engine) {
        return getExecuteParamDic(engine, ExeParamTypeEnum.Application.getValue());
    }

    /**
     * 5 获取执行参数字典表
     *
     * @param engine
     * @return
     */
    public List<ParamDictionaryVo> getExecuteParamDic(String engine) {
        List<ParamDictionaryVo> runParamDictionaryVO =
                getExecuteParamDic(engine, ExeParamTypeEnum.Run.getValue());
        List<ParamDictionaryVo> configParamDictionaryVO =
                getExecuteParamDic(engine, ExeParamTypeEnum.Config.getValue());
        List<ParamDictionaryVo> paramDictionaryVOList = new ArrayList<>();
        paramDictionaryVOList.addAll(runParamDictionaryVO);
        paramDictionaryVOList.addAll(configParamDictionaryVO);
        return paramDictionaryVOList;
    }


    private List<ParamDictionaryVo> getExecuteParamDic(String engine, String paramType) {
        /**
         * 在配置系统表时，一定要注意 前缀和格式
         */
        String cfgSource = SysConfigSourceParamPrefix + "_" + engine.toUpperCase() + "_" + paramType.toUpperCase();
        List<SysConfig> sysConfigList =
                sysConfigService.<SysConfig>lambdaQuery()
                        .eq(SysConfig::getCfgModule, SysConfigModuleRealtime)
                        .eq(SysConfig::getCfgSource, cfgSource)
                        .list();
        List<ParamDictionaryVo> paramDictionaryVOList = new ArrayList<>();
        for (SysConfig sysConfig : sysConfigList) {
            ParamDictionaryVo paramDictionaryVO = new ParamDictionaryVo();
            paramDictionaryVO.setParamType(paramType);
            paramDictionaryVO.setParamShow(sysConfig.getCfgValueShow());
            paramDictionaryVO.setParamKey(sysConfig.getCfgKey());
            paramDictionaryVO.setParamValue(sysConfig.getCfgValue());
            paramDictionaryVO.setParamDesc(sysConfig.getCfgKeyDesc());
            paramDictionaryVOList.add(paramDictionaryVO);
        }
        return paramDictionaryVOList;
    }

    /**
     * 获取引擎列表
     * @return 引擎列表
     */
    public List<EngineTypeVo> listEngineInfo() {
        /**
         * 在配置系统表时，一定要与constants.EngineTypeEnum中的一致
         */
        List<SysConfig> sysConfigList =
                sysConfigService.<SysConfig>lambdaQuery()
                        .eq(SysConfig::getCfgModule, SysConfigModuleRealtime)
                        .eq(SysConfig::getCfgSource, SysConfigSourceEngine)
                        .eq(SysConfig::getState, RecordStateEnum.StateUse.getValue())
                        .list();
        List<EngineTypeVo> data = new ArrayList<>();
        for (SysConfig sysConfig : sysConfigList) {
            EngineTypeVo engineTypeVo = new EngineTypeVo();
            engineTypeVo.setEngineCode(sysConfig.getCfgKey());
            engineTypeVo.setEngineName(sysConfig.getCfgValueShow());
            if ("flink".equalsIgnoreCase(sysConfig.getCfgKey())
               || "spark".equalsIgnoreCase(sysConfig.getCfgKey())) {
                engineTypeVo.setCheckJson(true);
            } else {
                engineTypeVo.setCheckJson(false);
            }
            data.add(engineTypeVo);
        }

        return data;
    }

}
