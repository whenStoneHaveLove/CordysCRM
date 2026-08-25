package cn.cordys.crm.ad.payout.dto.response;

import cn.cordys.crm.ad.contract.dto.response.AdContractBriefResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 付款单详情（聚合付款单自身 + 审计 + 订单 + 合同信息）。
 */
@Data
public class AdPayoutDetailResponse {

    /* ---- 付款单自身 ---- */
    @Schema(description = "付款单id")
    private String id;

    @Schema(description = "付款单号")
    private String paymentNo;

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "付款金额")
    private BigDecimal amount;

    @Schema(description = "付款时间")
    private Date paymentTime;

    @Schema(description = "类型:10普通付款/20坏账")
    private Integer type;

    @Schema(description = "类型标签")
    private String typeLabel;

    @Schema(description = "状态:0草稿/10待审核/20审核通过/30驳回")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "勾选的id列表(JSON)")
    private String mediaIds;

    @Schema(description = "凭证")
    private String voucherUrl;

    @Schema(description = "备注")
    private String remark;

    /* ---- 审计信息 ---- */
    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "审批人")
    private String approveUser;

    @Schema(description = "审批时间")
    private Long approveTime;

    @Schema(description = "审批备注")
    private String approveRemark;

    /* ---- 关联订单信息 ---- */
    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "订单名称")
    private String orderName;

    /* ---- 关联合同信息 ---- */
    @Schema(description = "关联合同列表")
    private List<AdContractBriefResponse> contracts;
}
