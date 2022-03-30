package cn.ffcs.mtcs.realtime.server.core.job;

import cn.ffcs.mtcs.realtime.common.entity.RtMonitor;
import cn.ffcs.mtcs.realtime.server.feign.XxlJobAdminFeign;
import com.alibaba.fastjson.JSON;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/2/20/020 21:21
 * @Version 1.0
 */
@Component
public class SchedulerXxlJob {

    @Autowired
    private XxlJobAdminFeign xxlJobAdminFeign;

    @Autowired
    private XxlJobInfo xxlJobInfoConfig;

    /**
     * 1 新增
     * 2 启动
     * 3 停止
     */

    public int addScheduler(XxlJobInfo xxlJobInfo) {
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println(JSON.toJSONString(xxlJobInfo));
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println("---------------");
        String idString = xxlJobAdminFeign.add(xxlJobInfo).getContent();
        int id = Integer.parseInt(idString);
        return id;
    }


    public boolean updateScheduler(XxlJobInfo xxlJobInfo) {
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println(JSON.toJSONString(xxlJobInfo));
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println("---------------");
        ReturnT<String> returnt= xxlJobAdminFeign.update1(xxlJobInfo);
        if (ReturnT.SUCCESS_CODE == returnt.getCode()) {
            return true;
        }
        return false;
    }

    public boolean startScheduler(int id) {
        int code = xxlJobAdminFeign.start(id).getCode();
        if (xxlJobInfoConfig.getFailCore() == code) {
            return false;
        }
        return true;
    }

    public boolean stopScheduler(int id) {
        int code = xxlJobAdminFeign.pause(id).getCode();
        if (xxlJobInfoConfig.getFailCore() == code) {
            return false;
        }
        return true;
    }




    public XxlJobInfo createXxlJobInfo(RtMonitor rtMonitor,
                                       String executorHandler) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();

        // 使用默认值
        xxlJobInfo.setId(xxlJobInfoConfig.getId());

        // 由配置而定
        xxlJobInfo.setJobGroup(xxlJobInfoConfig.getJobGroup());
        // 根据时间间隔生成cron表达式
        xxlJobInfo.setJobCron(
                getJonCron(rtMonitor.getMonitorInterval()));
        // 操作附属信息表id + 监控id
        xxlJobInfo.setJobDesc(String.valueOf(rtMonitor.getOpsAttachmentId())
                + "-" + String.valueOf(rtMonitor.getMonitorId()));

        xxlJobInfo.setAddTime(new Date());
        xxlJobInfo.setUpdateTime(new Date());

        // 由配置而定
        xxlJobInfo.setAuthor(xxlJobInfoConfig.getAuthor());
        // 报警邮件，设置为空，使用自己的告警组件
        xxlJobInfo.setAlarmEmail(xxlJobInfoConfig.getAlarmEmail());

        // 由配置而定
        // 执行器路由策略
        xxlJobInfo.setExecutorRouteStrategy(xxlJobInfoConfig.getExecutorRouteStrategy());
        // 使用开发的特定执行器
        // 执行器，任务Handler名称，
        xxlJobInfo.setExecutorHandler(executorHandler);
        // 由配置而定
        // 执行器，任务参数
        xxlJobInfo.setExecutorParam(String.valueOf(rtMonitor.getMonitorId()));
        // 由配置而定
        // 阻塞处理策略
        xxlJobInfo.setExecutorBlockStrategy(xxlJobInfoConfig.getExecutorBlockStrategy());
        // 使用默认值
        // 任务执行超时时间，单位秒
        xxlJobInfo.setExecutorTimeout(xxlJobInfoConfig.getExecutorTimeout());
        // 使用默认值
        // 失败重试次数
        xxlJobInfo.setExecutorFailRetryCount(xxlJobInfoConfig.getExecutorFailRetryCount());

        // 由配置而定
        // 任务运行模式
        xxlJobInfo.setGlueType(xxlJobInfoConfig.getGlueType());
        // 使用默认值
        // GLUE源代码
        xxlJobInfo.setGlueSource(xxlJobInfoConfig.getGlueSource());
        // 使用默认值
        // GLUE备注
        xxlJobInfo.setGlueRemark(xxlJobInfoConfig.getGlueRemark());

        // 没有子任务
        xxlJobInfo.setChildJobId(xxlJobInfoConfig.getChildJobId());

        // 使用默认值
        // 调度状态：0-停止，1-运行
        xxlJobInfo.setTriggerStatus(xxlJobInfoConfig.getTriggerStatus());
        // 上次调度时间
        xxlJobInfo.setTriggerLastTime(xxlJobInfoConfig.getTriggerLastTime());
        // 下次调度时间
        xxlJobInfo.setTriggerNextTime(xxlJobInfoConfig.getTriggerNextTime());

        return xxlJobInfo;
    }

    /**
     * 通过时间间隔生产cron表达式
     *
     * @param monitorInterval 监控的时间间隔
     * @return
     */
    private String getJonCron(Integer monitorInterval) {
        // todo 待升级 现阶段采用规定的方式，默认是每分钟监控一次
        Random random = new Random();
        int time = random.nextInt(60);
        return String.valueOf(time) + " * * * * ? *";
    }


}
