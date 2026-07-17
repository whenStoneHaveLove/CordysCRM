package cn.cordys.crm.ad.businessentity.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 业务主体分页请求（V3.1 §4.1，POST /api/ad/business-entity/page）。
 */
@Data
public class AdBusinessEntityPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "关键字（主体名称/代码）")
    private String keyword;

    @Schema(description = "状态:10启用/20停用")
    private Integer status;
}
