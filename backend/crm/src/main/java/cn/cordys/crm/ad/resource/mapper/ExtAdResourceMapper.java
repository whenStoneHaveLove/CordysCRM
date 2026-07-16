package cn.cordys.crm.ad.resource.mapper;

import cn.cordys.common.mapper.CommonMapper;
import cn.cordys.crm.ad.resource.domain.AdResource;
import cn.cordys.crm.ad.resource.dto.request.AdResourcePageRequest;
import cn.cordys.crm.ad.resource.dto.response.AdResourceListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广告资源扩展 Mapper（M6，列表/复杂查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录（src/main/java/.../resource/mapper/ExtAdResourceMapper.xml）。
 */
@Mapper
public interface ExtAdResourceMapper extends BaseMapper<AdResource> {

    /**
     * 分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     */
    List<AdResourceListResponse> pageList(@Param("request") AdResourcePageRequest request);
}
