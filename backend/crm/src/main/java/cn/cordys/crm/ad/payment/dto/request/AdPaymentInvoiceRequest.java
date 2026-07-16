package cn.cordys.crm.ad.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 开票请求（M4 T-32，POST /api/ad/payment/invoice，L-02 开票金额=应收）。
 * 仅更新订单 invoiced_amount / invoice_status，不触碰已收款。
 */
@Data
public class AdPaymentInvoiceRequest {

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "开票金额（=应收余额，不扣减已预收）")
    private BigDecimal amount;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "发生日期")
    private Date occurDate;

    @Schema(description = "备注")
    private String remark;
}
