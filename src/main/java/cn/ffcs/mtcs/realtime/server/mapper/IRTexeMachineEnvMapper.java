package cn.ffcs.mtcs.realtime.server.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * @author chenhy
 */
public interface IRTexeMachineEnvMapper {

    /**
     * 插入
     * @param machineId
     * @param evnKey
     * @param evnShow
     * @param evnValue
     * @return int
     */
    @Insert(" INSERT INTO rt_exe_machine_env ( machine_id, env_key, env_show, env_value, state ) VALUES (#{machineId}, #{evnKey}, #{evnShow}, #{evnValue}, '10A') ")
    int insertByJson(@Param("machineId") long machineId, @Param("evnKey") String evnKey, @Param("evnShow") String evnShow, @Param("evnValue") String evnValue);
}
