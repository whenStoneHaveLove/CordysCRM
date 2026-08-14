package cn.cordys.crm.ad.receipt.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 广告收款单（一个订单对应一个收款单）。
 */
@Data
@Table(name = "ad_receipt")
public class AdReceipt extends BaseModel {

    @Schema(description = "收款单号")
    private String receiptNo;

    @Schema(description = "关联订单")
    private String orderId;

    @Schema(description = "收款金额")
    private BigDecimal amount;

    @Schema(description = "收款时间")
    private Date receiptTime;

    @Schema(description = "类型:10普通收款/20退款")
    private Integer type = 10;

    @Schema(description = "状态:0草稿/10待审核/20审核通过/30驳回")
    private Integer status = 0;

    @Schema(description = "凭证")
    private String voucherUrl;

    @Schema(description = "审批人")
    private String approveUser;

    @Schema(description = "审批时间")
    private Long approveTime;

    @Schema(description = "审批备注")
    private String approveRemark;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
