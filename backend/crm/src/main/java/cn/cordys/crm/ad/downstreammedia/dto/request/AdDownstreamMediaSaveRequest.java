package cn.cordys.crm.ad.downstreammedia.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdDownstreamMediaSaveRequest {

    @Schema(description = "id(编辑时必填)")
    private String id;

    @Schema(description = "媒体名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "媒体类型")
    private String mediaType;

    @Schema(description = "覆盖渠道")
    private String channel;

    @Schema(description = "刊例价")
    private String rateCard;

    @Schema(description = "折扣政策")
    private String discountPolicy;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "电话")
    private String contactPhone;

    @Schema(description = "合作状态:10正常/20停用")
    private Integer cooperationStatus;

    @Schema(description = "状态:10正常/20停用")
    private Integer status;

    @Schema(description = "业务主体")
    private String businessEntityId;
}
