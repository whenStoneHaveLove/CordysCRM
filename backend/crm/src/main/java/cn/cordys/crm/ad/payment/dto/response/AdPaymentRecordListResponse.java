package cn.cordys.crm.ad.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收付款记录列表项（M4 pageList 映射）。
 * 字段别名与 mapper XML 中 SELECT 的 AS 保持一致（驼峰属性名）。
 */
@Data
public class AdPaymentRecordListResponse {

    @Schema(description = "记录id")
    private String id;

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "业务主体名称")
    private String businessEntityName;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "媒体/代理名称")
    private String resourceName;

    @Schema(description = "方向:10上游收款/20下游付款")
    private Integer direction;

    @Schema(description = "方向标签")
    private String directionLabel;

    @Schema(description = "类型:10预收/20预付/30开票收款/40媒体尾款/50退款/60坏账")
    private Integer type;

    @Schema(description = "类型标签")
    private String typeLabel;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "发生日期")
    private Date occurDate;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "操作人")
    private String operatorId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Long createTime;
}
