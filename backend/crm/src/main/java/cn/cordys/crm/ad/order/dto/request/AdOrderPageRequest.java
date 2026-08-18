package cn.cordys.crm.ad.order.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 广告订单分页查询请求（M2 GET /api/ad/order/page）。
 * 继承 {@link BasePageRequest} 以获得 current/pageSize/sort；其余为本模块筛选字段。
 */
@Data
public class AdOrderPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤：可见业务主体集合；null 表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "主状态")
    private Integer status;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "客户id")
    private String customerId;

    @Schema(description = "订单类型:10框架合同/20单笔合同")
    private Integer orderType;

    @Schema(description = "收款方式:10预付款/20账期")
    private Integer receiptMethod;

    @Schema(description = "付款方式:10预付媒体/20后付媒体")
    private Integer paymentMethod;

    @Schema(description = "关键字（订单号/客户名）")
    private String keyword;

    @Schema(description = "缺合同筛选：1=仅缺合同，0=仅不缺合同，null=不限")
    private Integer missingContract;

    @Schema(description = "投放起始日-起（含）")
    private Date deliveryStartFrom;

    @Schema(description = "投放起始日-止（含）")
    private Date deliveryStartTo;
}
