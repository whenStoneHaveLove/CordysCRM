package cn.cordys.crm.ad.businessentity.dto.response;

import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 业务主体详情响应（V3.1 §4.1 GET /api/ad/business-entity/{id}）：主体主信息 + 状态标签。
 */
@Data
public class AdBusinessEntityDetailResponse {

    @Schema(description = "主体主信息")
    private AdBusinessEntity entity;

    @Schema(description = "状态标签")
    private String statusLabel;
}
