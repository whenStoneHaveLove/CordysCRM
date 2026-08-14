package cn.cordys.crm.ad.receipt.mapper;

import cn.cordys.crm.ad.receipt.domain.AdReceipt;
import cn.cordys.crm.ad.receipt.dto.request.AdReceiptPageRequest;
import cn.cordys.crm.ad.receipt.dto.response.AdReceiptListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收款单扩展 Mapper（列表查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录。
 */
@Mapper
public interface ExtAdReceiptMapper extends BaseMapper<AdReceipt> {

    /** 分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。 */
    List<AdReceiptListResponse> pageList(@Param("request") AdReceiptPageRequest request);

    /** 按订单查询收款单（一个订单一个收款单）。 */
    AdReceipt selectByOrderId(@Param("orderId") String orderId);
}
