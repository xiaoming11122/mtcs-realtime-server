package cn.ffcs.mtcs.realtime.server.core.engine;

import cn.ffcs.mtcs.realtime.common.entity.RtExeMetaInfo;
import cn.ffcs.mtcs.realtime.common.entity.RtOpsAttachment;
import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;

import java.util.Map;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/7/007 15:43
 * @Version 1.0
 */
public interface IEngine {
    /**
     * 1 创建SSHParamRequest
     * 2 封装命令
     *
     */

    /**
     * 创建执行是的脚本
     * 这个脚本是ssh服务中的ExeInfo列表json格式数据
     * 用于调用ssh服务
     *
     * @param taskDetailInfoBo
     * @return
     */
    String createExeCommand(DetailTaskInfoBo taskDetailInfoBo);

    /**
     * 创建程序启动、重启时的参数，json
     *
     * @param taskDetailInfoBo
     * @return
     */
    String createExeParam(UserPrincipal principal, DetailTaskInfoBo taskDetailInfoBo);

    /**
     * 在真正使用时替换其中的伪代码
     * 这个伪代码不是shellTemplate中的伪代码
     * 而是需要在操作执行时才能确定的伪代码
     *
     * @param command
     * @param params
     * @return
     */
    String transCommandPseudCode(String command, String... params);


    /**
     *
     * @param command
     * @param pseudCodeMap
     * @return
     */
    String transCommandPseudCode(String command, Map<String,String> pseudCodeMap );

    /**
     * 在真正使用时替换其中的伪代码
     * 这个伪代码不是shellTemplate中的伪代码
     * 而是需要在操作执行时才能确定的伪代码
     *
     * @param param
     * @param params
     * @return
     */
    String transParamPseudCode(String param, String... params);


    /**
     * 获取插件的对应的Jar具体路径信息，并根据伪码进行替换
     * @param command 命令模板信息
     * @param basePluginPath 流程插件的基础路径，后续根据该路径组装成具体的插件路径
     * @param taskId 任务ID
     * @param taskVersion 任务版本号
     * @return 替换后flink可识别的插件路径伪码信息
     */
    Map<String, String> getPluginJarPseudCodeMap(String command, String basePluginPath,
                                   Long taskId, String taskVersion);


    /**
     * 创建停止时的脚本
     * 目前flink引擎获取rt_exe_machine_env 中streaming_flink_stop_shell_template 停止模板生成
     * spark引擎停止命令和启动命令一致，由mtcs-data-exchange中根据rtOpsAttachment 中OpsParam的opsName来判断是启动还是停止
     *
     * @param rtExeMetaInfo
     * @return
     */
    String createStopCommand(RtExeMetaInfo rtExeMetaInfo, RtOpsAttachment rtOpsAttachment,String... params) ;


}
