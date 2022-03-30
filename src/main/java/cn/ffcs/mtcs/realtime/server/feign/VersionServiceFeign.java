package cn.ffcs.mtcs.realtime.server.feign;

import cn.ffcs.mtcs.common.response.RetDataMsg;
import cn.ffcs.mtcs.common.response.RetMsg;
import cn.ffcs.mtcs.realtime.server.feign.fallback.VersionServiceFeignFallback;
import cn.ffcs.mtcs.version.common.domain.VersionManageSql;
import cn.ffcs.mtcs.version.common.entity.MdVersionManage;
import cn.ffcs.mtcs.version.common.entity.MdVersionMapping;
import cn.ffcs.mtcs.version.common.request.StateChangingParamRequest;
import cn.ffcs.mtcs.version.common.request.VersionSavingParamRequest;
import cn.ffcs.mtcs.version.common.vo.MdVersionManageVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Description .
 * @Author Nemo
 * @Date 2019/12/3/003 17:10
 * @Version 1.0
 */
@FeignClient(value = "MTCS-VERSION-SERVER", fallback = VersionServiceFeignFallback.class)
public interface VersionServiceFeign {
    @RequestMapping(
            method = {RequestMethod.POST},
            value = {"/updateStateToValid"}
    )
    RetMsg updateStateToValid(@RequestBody StateChangingParamRequest var1);

    @RequestMapping(
            method = {RequestMethod.POST},
            value = {"/updateStateToInvalid"}
    )
    RetMsg updateStateToInvalid(@RequestBody StateChangingParamRequest var1);

    @RequestMapping(
            method = {RequestMethod.POST},
            value = {"/deleteVersionManage"}
    )
    RetMsg deleteVersionManage(@RequestBody StateChangingParamRequest var1);

    @RequestMapping(
            method = {RequestMethod.GET},
            value = {"/getNextVersion"}
    )
    RetDataMsg<String> getNextVersion(@RequestParam("mdId") Long var1, @RequestParam("versionType") Integer var2, @RequestParam("mappingType") String var3, @RequestParam("mappingName") String var4);

    @RequestMapping(
            method = {RequestMethod.GET},
            value = {"/getSequenceId"}
    )
    RetDataMsg<Long> getSequenceId(@RequestParam("sequenceName") String var1);

    @RequestMapping(
            value = {"/saveAndAutoIncrementVersion"},
            method = {RequestMethod.POST}
    )
    RetDataMsg<String> saveAndAutoIncrementVersion(@RequestBody VersionSavingParamRequest var1);

    @RequestMapping(
            value = {"/saveAndAutoIncrementVersionRollback"},
            method = {RequestMethod.POST}
    )
    RetDataMsg<String> saveAndAutoIncrementVersionRollback(@RequestBody VersionSavingParamRequest var1);

    @RequestMapping(
            value = {"/getRelVersionManageInfo"},
            method = {RequestMethod.GET}
    )
    RetDataMsg<MdVersionManage> getRelVersionManageInfo(@RequestParam("mdId") Long var1, @RequestParam("mappingType") String var2, @RequestParam(value = "mappingName", required = false) String var3);

    @RequestMapping(
            value = {"/getVersionManageInfo"},
            method = {RequestMethod.GET}
    )
    RetDataMsg<MdVersionManage> getVersionManageInfo(@RequestParam("mdId") Long var1, @RequestParam("mdVersion") String var2, @RequestParam("mappingType") String var3, @RequestParam(value = "mappingName", required = false) String var4);

    @RequestMapping(
            value = {"/getMaxVersionManageInfo"},
            method = {RequestMethod.GET}
    )
    RetDataMsg<MdVersionManage> getMaxVersionManageInfo(@RequestParam("mdId") Long var1, @RequestParam(value = "versionState", required = false) String var2, @RequestParam("mappingType") String var3, @RequestParam("mappingName") String var4);

    @RequestMapping(
            value = {"/getVersionManageInfoByMappingId"},
            method = {RequestMethod.GET}
    )
    RetDataMsg<MdVersionManage> getVersionManageInfoByMappingId(@RequestParam("mdId") Long var1, @RequestParam(value = "versionState", required = false) String var2, @RequestParam("mappingId") Long var3);

    /**
     * @deprecated
     */
    @Deprecated
    @RequestMapping(
            value = {"/getVersionManageInfoSql"},
            method = {RequestMethod.GET}
    )
    RetDataMsg<VersionManageSql> getVersionManageInfoSql(@RequestParam("mappingType") String var1, @RequestParam("mappingName") String var2, @RequestParam(value = "versionState", required = false) String var3);

    @RequestMapping(
            value = {"/getMappingId"},
            method = {RequestMethod.GET}
    )
    RetDataMsg<Long> getMappingId(@RequestParam("mappingType") String var1, @RequestParam("mappingName") String var2);

    @RequestMapping(
            value = {"/getMapping"},
            method = {RequestMethod.GET}
    )
    RetDataMsg<MdVersionMapping> getMapping(@RequestParam("mdType") Long var1);

    @RequestMapping(
            value = {"/selectAllVersionByMdIdAndMappingTypeName"},
            method = {RequestMethod.GET}
    )
    RetDataMsg<Page<MdVersionManageVo>> selectAllVersionByMdIdAndMappingTypeName(@RequestParam("mdId") Long var1, @RequestParam("mappingType") String var2, @RequestParam("mappingName") String var3, @RequestParam(value = "verState", required = false) String var4, @RequestParam("current") Long var5, @RequestParam("size") Integer var6);

    @RequestMapping(
            value = {"/selectAllVersionByMdIdAndMappingTypeNameSubtypeAndState"},
            method = {RequestMethod.GET}
    )
    RetDataMsg<Page<MdVersionManageVo>> selectAllVersionByMdIdAndMappingTypeNameSubtypeAndState(@RequestParam("mdId") Long var1, @RequestParam("mappingType") String var2, @RequestParam("mappingName") String var3, @RequestParam(value = "mappingSubtype", required = false) String var4, @RequestParam(value = "verState", required = false) String var5, @RequestParam(value = "state", required = false) String var6, @RequestParam("current") Long var7, @RequestParam("size") Integer var8);
}
