package cn.cordys.crm.ad.payout.mapper;

import cn.cordys.crm.ad.payout.domain.AdPayout;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutPageRequest;
import cn.cordys.crm.ad.payout.dto.response.AdPayoutListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 付款单扩展 Mapper（列表查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录。
 */
@Mapper
public interface ExtAdPayoutMapper extends BaseMapper<AdPayout> {

    /** 分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。 */
    List<AdPayoutListResponse> pageList(@Param("request") AdPayoutPageRequest request);

    /** 按订单查询付款单（一个订单一个付款单）。 */
    AdPayout selectByOrderId(@Param("orderId") String orderId);
}
