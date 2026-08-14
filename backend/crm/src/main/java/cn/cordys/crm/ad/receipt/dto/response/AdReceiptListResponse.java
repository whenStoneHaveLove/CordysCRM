package cn.cordys.crm.ad.receipt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收款单列表项。
 */
@Data
public class AdReceiptListResponse {

    @Schema(description = "收款单id")
    private String id;

    @Schema(description = "收款单号")
    private String receiptNo;

    @Schema(description = "关联订单")
    private String orderId;

    @Schema(description = "订单名称")
    private String orderName;

    @Schema(description = "收款金额")
    private BigDecimal amount;

    @Schema(description = "收款时间")
    private Date receiptTime;

    @Schema(description = "类型:10普通收款/20退款")
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
