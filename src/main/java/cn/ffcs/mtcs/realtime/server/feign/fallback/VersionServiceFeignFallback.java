package cn.ffcs.mtcs.realtime.server.feign.fallback;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.common.response.RetMsg;
import cn.ffcs.mtcs.realtime.server.feign.VersionServiceFeign;
import cn.ffcs.mtcs.version.common.domain.VersionManageSql;
import cn.ffcs.mtcs.version.common.entity.MdVersionManage;
import cn.ffcs.mtcs.version.common.entity.MdVersionMapping;
import cn.ffcs.mtcs.version.common.request.StateChangingParamRequest;
import cn.ffcs.mtcs.version.common.request.VersionSavingParamRequest;
import cn.ffcs.mtcs.version.common.vo.MdVersionManageVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Description .
 * @Author Nemo
 * @Date 2019/12/3/003 17:10
 * @Version 1.0
 */
@Component
@Slf4j
public class VersionServiceFeignFallback implements VersionServiceFeign {
    @Override
    public RetMsg updateStateToValid(StateChangingParamRequest var1) {
        return null;
    }

    @Override
    public RetMsg updateStateToInvalid(StateChangingParamRequest var1) {
        return null;
    }

    @Override
    public RetMsg deleteVersionManage(StateChangingParamRequest var1) {
        return null;
    }

    @Override
    public RetDataMsg<String> getNextVersion(Long var1, Integer var2, String var3, String var4) {
        return null;
    }

    @Override
    public RetDataMsg<Long> getSequenceId(String var1) {
        return null;
    }

    @Override
    public RetDataMsg<String> saveAndAutoIncrementVersion(VersionSavingParamRequest var1) {
        return null;
    }

    @Override
    public RetDataMsg<String> saveAndAutoIncrementVersionRollback(VersionSavingParamRequest var1) {
        return null;
    }

    @Override
    public RetDataMsg<MdVersionManage> getRelVersionManageInfo(Long var1, String var2, String var3) {
        return null;
    }

    @Override
    public RetDataMsg<MdVersionManage> getVersionManageInfo(Long var1, String var2, String var3, String var4) {
        return null;
    }

    @Override
    public RetDataMsg<MdVersionManage> getMaxVersionManageInfo(Long var1, String var2, String var3, String var4) {
        return null;
    }

    @Override
    public RetDataMsg<MdVersionManage> getVersionManageInfoByMappingId(Long var1, String var2, Long var3) {
        return null;
    }

    /**
     * @param var1
     * @param var2
     * @param var3
     * @deprecated
     */
    @Override
    public RetDataMsg<VersionManageSql> getVersionManageInfoSql(String var1, String var2, String var3) {
        return null;
    }

    @Override
    public RetDataMsg<Long> getMappingId(String var1, String var2) {
        return null;
    }

    @Override
    public RetDataMsg<MdVersionMapping> getMapping(Long var1) {
        return null;
    }

    @Override
    public RetDataMsg<Page<MdVersionManageVo>> selectAllVersionByMdIdAndMappingTypeName(Long var1, String var2, String var3, String var4, Long var5, Integer var6) {
        return null;
    }

    @Override
    public RetDataMsg<Page<MdVersionManageVo>> selectAllVersionByMdIdAndMappingTypeNameSubtypeAndState(Long var1, String var2, String var3, String var4, String var5, String var6, Long var7, Integer var8){
        return null;
    };
}