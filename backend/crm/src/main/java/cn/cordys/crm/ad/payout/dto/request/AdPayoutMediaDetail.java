package cn.cordys.crm.ad.payout.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 付款单保存请求中：每下游客户的付款返点明细。
 */
@Data
public class AdPayoutMediaDetail {

    @Schema(description = "订单-下游客户中间表id")
    private String orderDownstreamMediaId;

    @Schema(description = "下游客户id")
    private String mediaId;

    @Schema(description = "下游客户名称(冗余)")
    private String mediaName;

    @Schema(description = "应付金额")
    private BigDecimal payableAmount;

    @Schema(description = "不记返金额")
    private BigDecimal noRebateAmount;

    @Schema(description = "返点方式:10-比例/20-固定金额")
    private Integer rebateMode;

    @Schema(description = "返点值")
    private BigDecimal rebateValue;

    @Schema(description = "返点金额")
    private BigDecimal rebateAmount;

    @Schema(description = "实际应付")
    private BigDecimal actualPayable;

    @Schema(description = "本次付款金额")
    private BigDecimal paidAmount;

    @Schema(description = "下游客户银行账户id(ad_downstream_media_account.id)")
    private String accountId;
}