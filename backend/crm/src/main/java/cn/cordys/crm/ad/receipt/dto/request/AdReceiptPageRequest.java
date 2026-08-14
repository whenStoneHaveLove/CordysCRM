package cn.cordys.crm.ad.receipt.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 收款单分页请求。
 */
@Data
public class AdReceiptPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "关键字（单号/订单号）")
    private String keyword;

    @Schema(description = "关联订单")
    private String orderId;

    @Schema(description = "状态:0草稿/10待审核/20审核通过/30驳回")
    private Integer status;

    @Schema(description = "类型:10普通收款/20退款")
    private Integer type;
}
