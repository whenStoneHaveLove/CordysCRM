package cn.cordys.crm.ad.resource.dto.response;

import cn.cordys.crm.ad.resource.domain.AdResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告资源详情响应（M6 GET /api/ad/resource/{id}）：资源主信息 + 状态标签。
 */
@Data
public class AdResourceDetailResponse {

    @Schema(description = "资源主信息")
    private AdResource resource;

    @Schema(description = "资源类型标签")
    private String resourceTypeLabel;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "归属业务主体名称")
    private String businessEntityName;
}
