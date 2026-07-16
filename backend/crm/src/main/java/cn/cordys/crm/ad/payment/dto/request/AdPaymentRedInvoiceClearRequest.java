package cn.cordys.crm.ad.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 清除红冲标记请求（M4 T-33，POST /api/ad/payment/red-invoice/clear，L-27）。
 * 财务线下红冲处理完毕后清除订单 needs_red_invoice 标记。
 */
@Data
public class AdPaymentRedInvoiceClearRequest {

    @Schema(description = "关联订单id")
    private String orderId;
}
