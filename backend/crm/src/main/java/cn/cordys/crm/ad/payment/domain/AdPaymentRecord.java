package cn.cordys.crm.ad.payment.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 广告收付款记录（V3.1 §5.2.5，L-28 扩展）。
 */
@Data
@Table(name = "ad_payment_record")
public class AdPaymentRecord extends BaseModel {

    @Schema(description = "关联订单")
    private String orderId;

    @Schema(description = "业务主体")
    private String businessEntityId;

    @Schema(description = "关联媒体/代理(L-23)")
    private String resourceId;

    @Schema(description = "方向:10上游收款/20下游付款")
    private Integer direction;

    @Schema(description = "类型:10预收/20预付/30开票收款/40媒体尾款/50退款/60坏账")
    private Integer type;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "发生日期")
    private Date occurDate;

    @Schema(description = "凭证")
    private String voucherUrl;

    @Schema(description = "发票号(type=30)")
    private String invoiceNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作人")
    private String operatorId;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
