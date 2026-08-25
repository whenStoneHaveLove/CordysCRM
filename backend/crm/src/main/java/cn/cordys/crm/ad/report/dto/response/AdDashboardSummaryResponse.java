package cn.cordys.crm.ad.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 工作台首页数据概览响应。
 *
 * <p>聚合「进行中订单数 + 应收/应付总额 + 待收/待付款」5 个核心指标，以及近 12 个月订单趋势。
 * 金额口径遵循「收款/付款单审核通过后该订单的钱即结束」规则：</p>
 * <ul>
 *   <li>待收款 = SUM(receivable_amount - received_amount) WHERE receipt_done=0（收款单未审核通过）</li>
 *   <li>待付款 = SUM(media_payable_amount - media_paid_amount) WHERE payment_done=0（付款单未审核通过）</li>
 * </ul>
 */
@Data
public class AdDashboardSummaryResponse {

    @Schema(description = "进行中订单数（非归档/非作废）")
    private long activeOrderCount;

    @Schema(description = "应收总额（未作废订单）")
    private BigDecimal totalReceivable;

    @Schema(description = "应付总额（未作废订单）")
    private BigDecimal totalMediaPayable;

    @Schema(description = "待收款金额（收款单未审核通过的订单）")
    private BigDecimal pendingReceivable;

    @Schema(description = "待付款金额（付款单未审核通过的订单）")
    private BigDecimal pendingPayable;

    @Schema(description = "近12个月趋势：value=月份(yyyy-MM)，orderCount=订单数，amount=订单金额")
    private List<Map<String, Object>> monthlyTrend;
}
