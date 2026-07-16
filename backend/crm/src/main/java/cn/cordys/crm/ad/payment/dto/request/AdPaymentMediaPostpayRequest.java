package cn.cordys.crm.ad.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 付媒体尾款请求（M4 T-33，POST /api/ad/payment/pay-media-postpay[+/force]，L-05/L-28）。
 * force=true 为手动强制触发入口（应对部分收款/坏账场景）。
 */
@Data
public class AdPaymentMediaPostpayRequest {

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "媒体尾款金额（应付尾款=media_payable-已付）")
    private BigDecimal amount;

    @Schema(description = "发生日期")
    private Date occurDate;

    @Schema(description = "关联媒体(L-23)")
    private String resourceId;

    @Schema(description = "是否强制触发（手动入口，默认 false）")
    private Boolean force;

    @Schema(description = "备注")
    private String remark;
}
