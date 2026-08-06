package cn.cordys.crm.ad.upstreamagent.dto.response;

import cn.cordys.crm.ad.upstreamagent.domain.AdUpstreamAgent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdUpstreamAgentDetailResponse {

    @Schema(description = "代理信息")
    private AdUpstreamAgent agent;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "合作状态标签")
    private String cooperationStatusLabel;

    @Schema(description = "业务主体名称")
    private String businessEntityName;
}
