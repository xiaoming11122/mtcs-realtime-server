package cn.ffcs.mtcs.realtime.server.service.business;

import cn.ffcs.mtcs.common.constants.StateConstants;
import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.realtime.common.request.TaskSaveRequest;
import cn.ffcs.mtcs.realtime.common.vo.DetailTaskVo;
import cn.ffcs.mtcs.realtime.common.vo.TaskVersionVo;
import cn.ffcs.mtcs.realtime.server.core.ops.IOpsData;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.exception.ExistException;
import cn.ffcs.mtcs.realtime.server.exception.FeignException;
import cn.ffcs.mtcs.realtime.server.feign.UserServerFeign;
import cn.ffcs.mtcs.realtime.server.feign.VersionServiceFeign;
import cn.ffcs.mtcs.user.common.domain.UserPrincipal;
import cn.ffcs.mtcs.version.common.constants.VersionConstants;
import cn.ffcs.mtcs.version.common.feign.IVersionFeignService;
import cn.ffcs.mtcs.version.common.vo.MdVersionManageVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * @Description.
 * @Author Nemo
 * @Date 2020/1/8/008 11:22
 * @Version 1.0
 */
@Component
public class RealtimeDetailBusiness {


    /**
     * 1 获取任务版本列表
     * 2 获取任务详细包含所有信息
     * 3 修改
     */

    @Autowired
    private UserServerFeign userServerFeign;

    @Autowired
    private VersionServiceFeign versionServiceFeign;

    @Autowired
    private IOpsData opsData;


    /**
     * 元数据类型映射表规格，模块
     */
    @Value("${version.mapping.module}")
    private String VersionMappingModule;

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
     * 元数据类型映射表规格，类型，rt_flow
     */
    @Value("${version.mapping.type.flow}")
    private String VersionMappingTypeFlow;

    /**
     * 元数据类型映射表规格，名称，rt_flow
     */
    @Value("${version.mapping.name.flow}")
    private String VersionMappingNameFlow;

    @Autowired
    private IVersionFeignService versionFeignService;

    /**
     * 1 获取任务版本列表
     *
     * @param taskId 任务Id
     * @return
     */
    public TaskVersionVo getTaskVersionAll(Long taskId) throws FeignException {
        TaskVersionVo taskVersionVo = new TaskVersionVo();
        taskVersionVo.setTaskId(taskId);
        taskVersionVo.setTaskVersion(listTaskVersion(taskId));
        return taskVersionVo;
    }

    /**
     * 通过任务id，使用版本服务查询任务的所有版本
     *
     * @param taskId
     * @return
     */
    private List<String> listTaskVersion(Long taskId) throws FeignException {
        /**
         * 1 根据相关信息取出数据
         */
        StringBuffer verState = new StringBuffer();
        verState.append(VersionConstants.VER_STATE_DEV);
        verState.append(",").append(VersionConstants.VER_STATE_HIS);
        verState.append(",").append(VersionConstants.VER_STATE_TREL);
        verState.append(",").append(VersionConstants.VER_STATE_REL);


        StringBuffer state = new StringBuffer();
        state.append(StateConstants.STATE_NORMAL);
        state.append(",").append(StateConstants.STATE_MODIFYING);
        state.append(",").append(StateConstants.STATE_CREATING);

        Long defaultCurrent = 1L;
        int defaultSize = 100;

        RetDataMsg<Page<MdVersionManageVo>> retDataMsg =
                versionFeignService.selectAllVersionByMdIdAndMappingTypeNameSubtypeAndState(
                        taskId,
                        VersionMappingTypeTask,
                        VersionMappingNameTask,
                        null,
                        verState.toString(),
                        state.toString(),
                        defaultCurrent,
                        defaultSize);
        if (!retDataMsg.getSuccess()) {
            throw new FeignException("调用版本服务失败！");
        }

        List<String> taskVersionList = new ArrayList<>();
        List versionManageVoList = retDataMsg.getData().getRecords();
        for (Object mdVersionManageVo : versionManageVoList) {

            if (mdVersionManageVo instanceof MdVersionManageVo){
                taskVersionList.add(String.valueOf(((MdVersionManageVo)mdVersionManageVo).getMdVersion()));
            }else {
                taskVersionList.add(String.valueOf(((Map)mdVersionManageVo).get("mdVersion")));
            }


        }

//        Comparator byTopicNameASC = Comparator.comparing(KafkaGroupTopicInfo::getTopicName, String.CASE_INSENSITIVE_ORDER);
        taskVersionList.sort((a, b) -> new Double(b).compareTo(new Double(a)));
//        Collections.reverse(taskVersionList);
        return taskVersionList;
    }


    /**
     * 2 获取任务详细信息
     *
     * @param taskId
     * @param taskVersion
     * @return
     */
    public DetailTaskVo getDetailTaskInfo(Long taskId,
                                          String taskVersion) throws FeignException {
        return opsData.getShowDetailTaskVo(taskId, taskVersion);
    }


    /**
     * 4 修改任务元信息
     *
     * @param taskSaveRequest
     * @return
     */
    public String modifyTaskInfo(TaskSaveRequest taskSaveRequest) throws
            FeignException, DataOpsException, ExistException {

        /**
         * 0 申请一个新的版本
         * 1 获取任务基本信息，保存任务基本信息
         * 2 获取流程信息，保存流程信息
         * 3 保存任务流程关系信息
         * 4 获取主机信息，保存任务主机关系表
         * 5 获取应用参数信息，获取运行参数信息，获取执行配置信息，并保存
         * 6 注册更新表
         */

        UserPrincipal principal = userServerFeign.getPrincipal();

        DetailTaskVo detailTaskVo = opsData.modifyDetailTaskVo(principal, taskSaveRequest);

        if (null == detailTaskVo) {
            return "修改失败！";
        } else {
            return "修改成功！";
        }
    }


}
