package cn.cordys.crm.ad.order.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 广告订单主表（V3.1 §5.2.1）。
 * 主键/审计字段沿用开源基线 {@link BaseModel}（VARCHAR id、BIGINT 时间、create_user/update_user、
 * organization_id、deleted）。业务列严格按 V3.1 定义。
 */
@Data
@Table(name = "ad_order")
public class AdOrder extends BaseModel {

    @Schema(description = "订单编号 主体代码-YYYYMMDD-3位流水(L-20)")
    private String orderNo;

    @Schema(description = "订单名称")
    private String orderName;

    @Schema(description = "业务主体(下单必选,L-29)")
    private String businessEntityId;

    @Schema(description = "客户id")
    private String customerId;

    @Schema(description = "行业类别(字典)")
    private String industryCode;

    @Schema(description = "签约主体")
    private String signingEntity;

    @Schema(description = "订单类型:10框架合同/20单笔合同")
    private Integer orderType;

    @Schema(description = "上游代理(可空)")
    private String upstreamAgentId;

    @Schema(description = "代理订单号")
    private String agentOrderNo;

    @Schema(description = "下单人")
    private String creatorId;

    @Schema(description = "主状态(L-01,10倍数)")
    private Integer status;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "返点方式:10比例/20固定金额")
    private Integer rebateMode;

    @Schema(description = "返点值")
    private BigDecimal rebateValue;

    @Schema(description = "不记返金额，默认 0")
    private BigDecimal noRebateAmount = BigDecimal.ZERO;

    @Schema(description = "返点金额(自动)")
    private BigDecimal rebateAmount;

    @Schema(description = "应收金额=总额-返点")
    private BigDecimal receivableAmount;

    @Schema(description = "应付总额(下游口径,L-28)")
    private BigDecimal mediaPayableAmount;

    @Schema(description = "投放起始日")
    @Column(name = "delivery_start_date")
    private Date deliveryStartDate;

    @Schema(description = "投放结束日")
    @Column(name = "delivery_end_date")
    private Date deliveryEndDate;

    @Schema(description = "投放量+单位")
    @Column(name = "delivery_volume")
    private String deliveryVolume;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "收款方式:10预收/20账期")
    private Integer receiptMethod;

    @Schema(description = "预付模式:10比例/20固定(L-25)")
    private Integer receiptPrepayMode;

    @Schema(description = "预收比例%")
    private BigDecimal receiptPrepayRatio;

    @Schema(description = "预收金额(基数=应收,L-11)")
    private BigDecimal receiptPrepayAmount;

    @Schema(description = "预收截止日")
    private Date receiptPrepayDeadline;

    @Schema(description = "账期天数(账期时必填)")
    private Integer receiptAccountPeriodDays;

    @Schema(description = "付款方式:10预付/20后付")
    private Integer paymentMethod;

    @Schema(description = "预付模式:10比例/20固定")
    private Integer paymentPrepayMode;

    @Schema(description = "预付比例%")
    private BigDecimal paymentPrepayRatio;

    @Schema(description = "预付金额(基数=media_payable,L-28)")
    private BigDecimal paymentPrepayAmount;

    @Schema(description = "预付截止日")
    private Date paymentPrepayDeadline;

    @Schema(description = "后付触发:10收到上游全款/20执行完成X天(L-05)")
    private Integer paymentPostpayTrigger;

    @Schema(description = "后付X天(trigger=20必填)")
    private Integer paymentPostpayDays;

    @Schema(description = "开票进度:0未开/10部分/20全额(L-01)")
    private Integer invoiceStatus = 0;

    @Schema(description = "收款进度:0未收/10部分/20全额")
    private Integer receiptStatus = 0;

    @Schema(description = "付款进度:0未付/10部分/20全额")
    private Integer mediaPaymentStatus = 0;

    @Schema(description = "收款状态:0待收/1已收")
    private Integer receiptDone = 0;

    @Schema(description = "付款状态:0待付/1已付")
    private Integer paymentDone = 0;

    @Schema(description = "已开票累计")
    private BigDecimal invoicedAmount = BigDecimal.ZERO;

    @Schema(description = "已收款累计(含预收)")
    private BigDecimal receivedAmount = BigDecimal.ZERO;

    @Schema(description = "已付款累计")
    private BigDecimal mediaPaidAmount = BigDecimal.ZERO;

    @Schema(description = "坏账金额(强制归档,L-13)")
    private BigDecimal badDebtAmount;

    @Schema(description = "需红冲标记:0否/1是(L-27)")
    private Integer needsRedInvoice = 0;

    @Schema(description = "账期起算日(L-09/L-26)")
    private Date accountPeriodStartDate;

    @Schema(description = "账期到期日(自动)")
    private Date accountPeriodEndDate;

    @Schema(description = "执行完成时间")
    private Date executionCompletedAt;

    @Schema(description = "归档时间")
    private Date archivedAt;

    @Schema(description = "作废时间")
    private Date voidedAt;

    @Schema(description = "作废原因")
    private String voidReason;

    @Schema(description = "扩展字段(V3.1 §4.4)")
    private String extJson;

    @Schema(description = "币种，默认 CNY")
    private String currency = "CNY";

    @Schema(description = "预留多租户")
    private String tenantId;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
