package cn.cordys.crm.ad.order.mapper;

import cn.cordys.common.mapper.CommonMapper;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.dto.request.AdOrderPageRequest;
import cn.cordys.crm.ad.order.dto.response.AdOrderListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;

/**
 * 广告订单扩展 Mapper（M2 T-11，列表/复杂查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录（src/main/java/.../order/mapper/ExtAdOrderMapper.xml）。
 *
 * <p>约定：request 参数统一以 {@code @Param("request")} 暴露，便于在 XML 中通过
 * {@code request.xxx} 引用，并复用 {@code CommonMapper.sort} 动态排序片段。</p>
 */
@Mapper
public interface ExtAdOrderMapper extends BaseMapper<AdOrder> {

    /**
     * 分页列表（多筛选 + 关键字 + 主体隔离 + 缺合同标记 + 排序）。
     *
     * @param request 分页与筛选条件（含 organizationId 与 entityIds 主体隔离集合）
     * @return 列表项
     */
    List<AdOrderListResponse> pageList(@Param("request") AdOrderPageRequest request);

    /**
     * 统计某业务主体当天的订单数（用于 L-20 订单号 3 位流水号）。
     */
    long countTodayOrders(@Param("businessEntityId") String businessEntityId,
                           @Param("start") Long start,
                           @Param("end") Long end,
                           @Param("orgId") String orgId);

    /**
     * 查询已逾期的执行中订单（L-08 自动流转 50→80）。
     * 条件：status=50(EXECUTING) 且 delivary_end_date < now。
     */
    List<AdOrder> selectOverdue(@Param("now") Date now);
}
