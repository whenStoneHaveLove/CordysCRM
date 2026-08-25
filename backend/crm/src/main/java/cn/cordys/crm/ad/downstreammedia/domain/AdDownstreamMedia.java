package cn.cordys.crm.ad.downstreammedia.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "ad_downstream_media")
public class AdDownstreamMedia extends BaseModel {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "类型")
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

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
