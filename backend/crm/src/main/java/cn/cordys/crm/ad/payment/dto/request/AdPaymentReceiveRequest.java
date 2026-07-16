package cn.cordys.crm.ad.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收款登记请求（M4 T-32，POST /api/ad/payment/receive）。
 * 仅更新订单 received_amount / receipt_status，不触碰已开票。
 */
@Data
public class AdPaymentReceiveRequest {

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "收款金额（含已预收累计）")
    private BigDecimal amount;

    @Schema(description = "发生日期")
    private Date occurDate;

    @Schema(description = "备注")
    private String remark;
}
