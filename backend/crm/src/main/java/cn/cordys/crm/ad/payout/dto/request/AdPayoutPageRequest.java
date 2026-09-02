package cn.cordys.crm.ad.payout.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 付款单分页请求。
 */
@Data
public class AdPayoutPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "关键字（单号/订单号）")
    private String keyword;

    @Schema(description = "关联订单")
    private String orderId;

    @Schema(description = "状态:0草稿/10待审核/20待付款/30已付款")
    private Integer status;

    @Schema(description = "类型:10普通付款/20坏账")
    private Integer type;

    @Schema(description = "付款单类型:10订单一类/20非订单一类")
    private Integer billType;

    @Schema(description = "下游客户id(筛选：通过下游客户反查付款单)")
    private String downstreamMediaId;
}
