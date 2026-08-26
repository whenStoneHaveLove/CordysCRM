package cn.cordys.crm.ad.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 广告订单新建/编辑（草稿）请求（V3.1 §13.2）。
 * 仅包含业务可填字段；orderNo/status/creatorId 等由后端生成或受控，不在本请求内。
 */
@Data
public class AdOrderSaveRequest {

    @Schema(description = "订单id（更新草稿时必填，新建忽略）")
    private String id;

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

    @Schema(description = "关联合同id(框架订单必选框架合同；单笔订单可后补)")
    private String contractId;

    @Schema(description = "上游代理(可空)")
    private String upstreamAgentId;

    @Schema(description = "代理订单号")
    private String agentOrderNo;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "返点方式:10比例/20固定金额")
    private Integer rebateMode;

    @Schema(description = "返点值")
    private BigDecimal rebateValue;

    @Schema(description = "不记返金额，默认 0")
    private BigDecimal noRebateAmount = BigDecimal.ZERO;

    @Schema(description = "应付总额(下游口径,L-28)")
    private BigDecimal mediaPayableAmount;

    @Schema(description = "投放起始日")
    private Date deliveryStartDate;

    @Schema(description = "投放结束日")
    private Date deliveryEndDate;

    @Schema(description = "投放量+单位")
    private String deliveryVolume;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "收款方式:10预收/20账期")
    private Integer receiptMethod;

    @Schema(description = "预收模式:10比例/20固定(L-25)")
    private Integer receiptPrepayMode;

    @Schema(description = "预收比例%(mode=10时填)")
    private BigDecimal receiptPrepayRatio;

    @Schema(description = "预收固定金额(mode=20时填)")
    private BigDecimal receiptPrepayAmount;

    @Schema(description = "预收截止日")
    private Date receiptPrepayDeadline;

    @Schema(description = "账期天数(账期时必填)")
    private Integer receiptAccountPeriodDays;

    @Schema(description = "付款方式:10预付/20后付")
    private Integer paymentMethod;

    @Schema(description = "预付模式:10比例/20固定")
    private Integer paymentPrepayMode;

    @Schema(description = "预付比例%(mode=10时填)")
    private BigDecimal paymentPrepayRatio;

    @Schema(description = "预付固定金额(mode=20时填)")
    private BigDecimal paymentPrepayAmount;

    @Schema(description = "预付截止日")
    private Date paymentPrepayDeadline;

    @Schema(description = "后付触发:10收到上游全款/20执行完成X天(L-05)")
    private Integer paymentPostpayTrigger;

    @Schema(description = "后付X天(trigger=20必填)")
    private Integer paymentPostpayDays;

    @Schema(description = "币种，默认 CNY")
    private String currency = "CNY";

    @Schema(description = "下游客户id列表(至少选一个)")
    private List<String> downstreamMediaIds;

    @Schema(description = "下游客户付款返点明细(与 downstreamMediaIds 一一对应,可选填)")
    private List<DownstreamMediaPayableDTO> downstreamMediaPayables;

    /** 单个下游客户的付款返点明细 */
    @Data
    public static class DownstreamMediaPayableDTO {
        @Schema(description = "下游客户id")
        private String downstreamMediaId;

        @Schema(description = "应付金额")
        private BigDecimal payableAmount;

        @Schema(description = "不记返金额")
        private BigDecimal noRebateAmount;

        @Schema(description = "返点方式:10-比例/20-固定金额")
        private Integer rebateMode;

        @Schema(description = "返点值:比例时存百分比数值,固定金额时存金额")
        private BigDecimal rebateValue;
    }
}
