package cn.cordys.crm.ad.common.mapper;

import cn.cordys.crm.ad.common.domain.AdOperationLog;
import cn.cordys.crm.ad.common.dto.request.AdOperationLogPageRequest;
import cn.cordys.crm.ad.common.dto.response.AdOperationLogResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广告操作日志 Mapper。
 */
@Mapper
public interface ExtAdOperationLogMapper extends BaseMapper<AdOperationLog> {

    /**
     * 分页查询广告操作日志。
     */
    List<AdOperationLogResponse> list(@Param("request") AdOperationLogPageRequest request,
                                      @Param("orgId") String orgId);
}
