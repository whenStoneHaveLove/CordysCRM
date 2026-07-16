package cn.cordys.crm.ad.order.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 改单分页查询请求（M3 POST /api/ad/order-change/page）。
 * 继承 {@link BasePageRequest} 获得 current/pageSize/sort；其余为本模块筛选字段。
 */
@Data
public class AdOrderChangePageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤：可见业务主体集合；null 表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "改单状态(AdOrderChangeStatus.code)")
    private Integer status;

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "关键字（变更原因/订单号）")
    private String keyword;
}
