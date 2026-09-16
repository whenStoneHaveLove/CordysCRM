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

    @Schema(description = "付款单类型:10订单类型/20非订单类型，默认10")
    private Integer billType;

    @Schema(description = "关联订单(非订单类型时不传)")
    private String orderId;

    @Schema(description = "付款金额（默认=应付-已付）")
    private BigDecimal amount;

    @Schema(description = "付款时间")
    private Date paymentTime;

    @Schema(description = "类型:10普通付款/20坏账")
    private Integer type;

    @Schema(description = "勾选的id列表")
    private List<String> mediaIds;

    @Schema(description = "凭证")
    private String voucherUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "各下游客户付款返点明细(每客户一行)")
    private List<AdPayoutMediaDetail> mediaDetails;

    @Schema(description = "发票临时文件ID（/attachment/upload/temp 返回，单文件）")
    private String invoiceTempFileId;

    @Schema(description = "发票文件名")
    private String invoiceFileName;

    @Schema(description = "发票号码")
    private String invoiceNo;
}
