package cn.cordys.crm.ad.downstreammedia.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdDownstreamMediaListResponse {

    @Schema(description = "id")
    private String id;

    @Schema(description = "媒体名称")
    private String name;

    @Schema(description = "媒体类型")
    private String mediaType;

    @Schema(description = "媒体类型标签")
    private String mediaTypeLabel;

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

    @Schema(description = "合作状态")
    private Integer cooperationStatus;

    @Schema(description = "合作状态标签")
    private String cooperationStatusLabel;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "业务主体")
    private String businessEntityId;

    @Schema(description = "业务主体名称")
    private String businessEntityName;

    @Schema(description = "创建时间")
    private Long createTime;
}
