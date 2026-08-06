package cn.cordys.crm.ad.downstreammedia.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdDownstreamMediaPageRequest extends BasePageRequest {

    @Schema(description = "关键字(媒体名称)")
    private String keyword;

    @Schema(description = "媒体类型")
    private String mediaType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "组织id")
    private String organizationId;

    @Schema(description = "主体过滤集合")
    private List<String> entityIds;
}
