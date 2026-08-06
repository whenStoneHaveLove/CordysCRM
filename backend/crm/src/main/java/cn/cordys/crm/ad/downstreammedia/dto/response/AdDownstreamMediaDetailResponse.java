package cn.cordys.crm.ad.downstreammedia.dto.response;

import cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMedia;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdDownstreamMediaDetailResponse {

    @Schema(description = "媒体信息")
    private AdDownstreamMedia media;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "合作状态标签")
    private String cooperationStatusLabel;

    @Schema(description = "媒体类型标签")
    private String mediaTypeLabel;

    @Schema(description = "业务主体名称")
    private String businessEntityName;
}
