package cn.cordys.crm.ad.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 广告订单列表项（M2 pageList 映射）。
 * 字段别名与 mapper XML 中 SELECT 的 AS 保持一致（驼峰属性名）。
 */
@Data
public class AdOrderListResponse {

    @Schema(description = "订单id")
    private String id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "订单名称")
    private String orderName;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "业务主体名称")
    private String businessEntityName;

    @Schema(description = "客户id")
    private String customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "订单类型:10框架合同/20单笔合同")
    private Integer orderType;

    @Schema(description = "主状态")
    private Integer status;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "应收金额")
    private BigDecimal receivableAmount;

    @Schema(description = "媒体应付总额")
    private BigDecimal mediaPayableAmount;

    @Schema(description = "返点金额")
    private BigDecimal rebateAmount;

    @Schema(description = "收款方式:10预付款/20账期")
    private Integer receiptMethod;

    @Schema(description = "付款方式:10预付媒体/20后付媒体")
    private Integer paymentMethod;

    @Schema(description = "投放起始日")
    private Date deliveryStartDate;

    @Schema(description = "投放结束日")
    private Date deliveryEndDate;

    @Schema(description = "下单人")
    private String creatorId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "缺合同标记:0否/1是（LEFT JOIN ad_order_contract 计数）")
    private Integer missingContract;
}
