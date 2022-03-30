package cn.ffcs.mtcs.realtime.server.service.data;

import cn.ffcs.mtcs.realtime.server.pojo.bo.DetailTaskInfoBo;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/11/011 11:48
 * @Version 1.0
 */
public interface IDetailTaskInfoBoService {

    /**
     * 获取任务包含所有的配置信息
     *
     * @param taskId
     * @param taskVersion
     * @return
     */
    DetailTaskInfoBo getTaskDetailInfoBo(Long taskId, String taskVersion);
}
