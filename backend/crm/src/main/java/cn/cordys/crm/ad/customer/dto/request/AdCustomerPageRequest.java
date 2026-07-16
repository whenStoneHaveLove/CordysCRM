package cn.cordys.crm.ad.customer.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 广告客户分页请求（M6，POST /api/ad/customer/page）。
 */
@Data
public class AdCustomerPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "关键字（客户名称/联系人/电话）")
    private String keyword;

    @Schema(description = "客户等级:10VIP/20普通/30潜力")
    private Integer customerLevel;

    @Schema(description = "状态:0活跃/10非活跃/20黑名单")
    private Integer status;
}
