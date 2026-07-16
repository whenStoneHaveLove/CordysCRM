package cn.cordys.crm.ad.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 财务待办项（M4 T-33，GET /api/ad/payment/todo/page）。
 */
@Data
public class AdPaymentTodoResponse {

    @Schema(description = "待办类型:PREPAY_CONFIRM/MEDIA_PREPAY/MEDIA_POSTPAY/RED_INVOICE/INVOICE_RECEIVE")
    private String todoType;

    @Schema(description = "待办标签")
    private String todoLabel;

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "客户id")
    private String customerId;

    @Schema(description = "订单主状态")
    private Integer status;

    @Schema(description = "待处理金额（预收/预付/尾款/应收余额等）")
    private BigDecimal amount;

    @Schema(description = "到期日/触发日（媒体尾款）")
    private Date dueDate;

    @Schema(description = "订单创建时间")
    private Long createTime;
}
