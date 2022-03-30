package cn.ffcs.mtcs.realtime.server.util;

import org.springframework.beans.BeanUtils;

/**
 * @Description .
 * @Author Nemo
 * @Date 2020/1/13/013 9:55
 * @Version 1.0
 */
public class PojoTrans {

    public static <TargetClass> TargetClass pojoTrans(
            Object sourceEntity, Class<TargetClass> targetClass){
        // 判断source是否为空!
        if (sourceEntity == null) {
            return null;
        }
        // 判断TargetClass是否为空
        if (targetClass == null) {
            return null;
        }
        try {
            TargetClass newInstance = targetClass.newInstance();
            BeanUtils.copyProperties(sourceEntity, newInstance);
            // sourceEntity转换target
            return newInstance;
        } catch (Exception e) {
            return null;
        }
    }

    public static <Vo> Vo poToVo(Object poEntity, Class<Vo> voClass) {
        return pojoTrans(poEntity, voClass);
    }

    public static <Po> Po voToPo(Object voEntity, Class<Po> poClass) {
        return pojoTrans(voEntity, poClass);
    }


}
