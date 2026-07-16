package cn.cordys.crm.ad.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收付款撤销（冲销）请求（M4，POST /api/ad/payment/cancel）。
 * 撤销会反向回写订单的 invoice/receipt/media 累计与进度，并对原记录做逻辑删除。
 */
@Data
public class AdPaymentCancelRequest {

    @Schema(description = "收付款记录id")
    private String id;

    @Schema(description = "撤销原因")
    private String reason;
}
