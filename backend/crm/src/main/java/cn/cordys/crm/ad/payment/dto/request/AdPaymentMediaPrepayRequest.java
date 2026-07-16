package cn.cordys.crm.ad.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 付媒体预付款请求（M4 T-31，POST /api/ad/payment/pay-media-prepay，L-28 基数=media_payable）。
 */
@Data
public class AdPaymentMediaPrepayRequest {

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "媒体预付金额")
    private BigDecimal amount;

    @Schema(description = "发生日期")
    private Date occurDate;

    @Schema(description = "关联媒体(L-23)")
    private String resourceId;

    @Schema(description = "备注")
    private String remark;
}
