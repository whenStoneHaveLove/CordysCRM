package cn.cordys.crm.ad.upstreamagent.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdUpstreamAgentPageRequest extends BasePageRequest {

    @Schema(description = "关键字(代理名称)")
    private String keyword;

    @Schema(description = "合作状态")
    private Integer cooperationStatus;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "组织id")
    private String organizationId;

    @Schema(description = "主体过滤集合")
    private List<String> entityIds;
}
