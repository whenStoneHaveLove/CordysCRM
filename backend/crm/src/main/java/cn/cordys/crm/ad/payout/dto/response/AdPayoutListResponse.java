package cn.cordys.crm.ad.payout.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 付款单列表项。
 */
@Data
public class AdPayoutListResponse {

    @Schema(description = "付款单id")
    private String id;

    @Schema(description = "付款单号")
    private String paymentNo;

    @Schema(description = "付款单类型:10订单类型/20非订单类型")
    private Integer billType;

    @Schema(description = "付款单类型标签")
    private String billTypeLabel;

    @Schema(description = "关联订单")
    private String orderId;

    @Schema(description = "订单名称")
    private String orderName;

    @Schema(description = "下游客户名称(多个逗号分割)")
    private String mediaNames;

    @Schema(description = "付款金额")
    private BigDecimal amount;

    @Schema(description = "付款时间")
    private Date paymentTime;

    @Schema(description = "类型:10普通付款/20坏账")
    private Integer type;

    @Schema(description = "类型标签")
    private String typeLabel;

    @Schema(description = "状态:0草稿/10待审核/20待付款/30已付款")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Long createTime;
}
