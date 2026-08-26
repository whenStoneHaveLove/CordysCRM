package cn.cordys.crm.ad.payout.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 付款单-各下游客户付款返点明细。
 * 一笔付款单对应一行/多行（每客户一行）。
 */
@Data
@Table(name = "ad_payment_media")
public class AdPaymentMedia {

    @Schema(description = "id")
    private String id;

    @Schema(description = "付款单id")
    private String paymentId;

    @Schema(description = "订单id")
    private String orderId;

    @Schema(description = "订单-下游客户中间表id")
    private String orderDownstreamMediaId;

    @Schema(description = "下游客户id")
    private String mediaId;

    @Schema(description = "下游客户名称")
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

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}