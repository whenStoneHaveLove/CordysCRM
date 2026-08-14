package cn.cordys.crm.ad.payout.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 付款单保存请求（新建/编辑共用，新建时 id 留空）。
 */
@Data
public class AdPayoutSaveRequest {

    @Schema(description = "付款单id（编辑时必填，新建留空）")
    private String id;

    @Schema(description = "关联订单")
    private String orderId;

    @Schema(description = "付款金额（默认=媒体应付-已付）")
    private BigDecimal amount;

    @Schema(description = "付款时间")
    private Date paymentTime;

    @Schema(description = "类型:10普通付款/20坏账")
    private Integer type;

    @Schema(description = "勾选的媒体id列表")
    private List<String> mediaIds;

    @Schema(description = "凭证")
    private String voucherUrl;

    @Schema(description = "备注")
    private String remark;
}
