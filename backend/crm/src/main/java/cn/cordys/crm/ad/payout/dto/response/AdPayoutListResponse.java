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

    @Schema(description = "关联订单")
    private String orderId;

    @Schema(description = "订单名称")
    private String orderName;

    @Schema(description = "付款金额")
    private BigDecimal amount;

    @Schema(description = "付款时间")
    private Date paymentTime;

    @Schema(description = "类型:10普通付款/20坏账")
    private Integer type;

    @Schema(description = "类型标签")
    private String typeLabel;

    @Schema(description = "状态:0草稿/10待审核/20审核通过/30驳回")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Long createTime;
}
