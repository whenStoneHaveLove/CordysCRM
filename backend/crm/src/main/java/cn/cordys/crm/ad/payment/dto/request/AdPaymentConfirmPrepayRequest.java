package cn.cordys.crm.ad.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 确认预收款请求（M4 T-31，POST /api/ad/payment/confirm-prepay，L-11 基数=应收）。
 */
@Data
public class AdPaymentConfirmPrepayRequest {

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "预收金额")
    private BigDecimal amount;

    @Schema(description = "发生日期")
    private Date occurDate;

    @Schema(description = "备注")
    private String remark;
}
