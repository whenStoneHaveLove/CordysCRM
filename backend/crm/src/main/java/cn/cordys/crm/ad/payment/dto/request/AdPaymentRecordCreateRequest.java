package cn.cordys.crm.ad.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收付款登记请求（M4 通用登记入口，POST /api/ad/payment/create）。
 *
 * <p>通过 {@code direction} + {@code type} 表达任意一笔资金动作（预收/预付/开票收款/媒体尾款/退款/坏账），
 * 服务据此回写订单的 invoice/receipt/media 进度与累计金额（L-01/L-02/L-11/L-28）。</p>
 */
@Data
public class AdPaymentRecordCreateRequest {

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "方向:10上游收款/20下游付款")
    private Integer direction;

    @Schema(description = "类型:10预收/20预付/30开票收款/40媒体尾款/50退款/60坏账")
    private Integer type;

    @Schema(description = "金额（必须>0）")
    private BigDecimal amount;

    @Schema(description = "发生日期")
    private Date occurDate;

    @Schema(description = "发票号（type=30 开票收款时填写）")
    private String invoiceNo;

    @Schema(description = "关联媒体/代理(L-23)")
    private String resourceId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "结算口径:仅开票=INVOICE / 仅收款=RECEIPT / 同时=BOTH(默认)。仅对上游 type=30 生效")
    private String settleType;
}
