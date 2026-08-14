package cn.cordys.crm.ad.receipt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收款单保存请求（新建/编辑共用，新建时 id 留空）。
 */
@Data
public class AdReceiptSaveRequest {

    @Schema(description = "收款单id（编辑时必填，新建留空）")
    private String id;

    @Schema(description = "关联订单")
    private String orderId;

    @Schema(description = "收款金额（默认=应收-已收）")
    private BigDecimal amount;

    @Schema(description = "收款时间")
    private Date receiptTime;

    @Schema(description = "类型:10普通收款/20退款")
    private Integer type;

    @Schema(description = "凭证")
    private String voucherUrl;

    @Schema(description = "备注")
    private String remark;
}
