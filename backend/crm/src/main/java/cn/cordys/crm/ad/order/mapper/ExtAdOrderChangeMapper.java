package cn.cordys.crm.ad.order.mapper;

import cn.cordys.common.mapper.CommonMapper;
import cn.cordys.crm.ad.order.domain.AdOrderChange;
import cn.cordys.crm.ad.order.dto.request.AdOrderChangePageRequest;
import cn.cordys.crm.ad.order.dto.response.AdOrderChangeListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 改单扩展 Mapper（M3 T-20/T-21，列表/复杂查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录（src/main/java/.../order/mapper/ExtAdOrderChangeMapper.xml）。
 *
 * <p>约定：request 参数统一以 {@code @Param("request")} 暴露，复用
 * {@code CommonMapper.sort} 动态排序片段，并按 {@code business_entity_id IN (...)} 做主体隔离。</p>
 */
@Mapper
public interface ExtAdOrderChangeMapper extends BaseMapper<AdOrderChange> {

    /**
     * 分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     *
     * @param request 分页与筛选条件（含 organizationId 与 entityIds 主体隔离集合）
     * @return 列表项
     */
    List<AdOrderChangeListResponse> pageList(@Param("request") AdOrderChangePageRequest request);
}
