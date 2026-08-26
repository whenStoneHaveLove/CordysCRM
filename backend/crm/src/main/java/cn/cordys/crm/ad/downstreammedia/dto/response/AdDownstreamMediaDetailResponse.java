package cn.cordys.crm.ad.downstreammedia.dto.response;

import cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMedia;
import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaAccountItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AdDownstreamMediaDetailResponse {

    @Schema(description = "信息")
    private AdDownstreamMedia media;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "合作状态标签")
    private String cooperationStatusLabel;

    @Schema(description = "类型标签")
    private String mediaTypeLabel;

    @Schema(description = "业务主体名称")
    private String businessEntityName;

    @Schema(description = "银行账户列表")
    private List<AdDownstreamMediaAccountItem> accountList;
}
