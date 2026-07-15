package cn.cordys.crm.ad.resource.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 上下游资源（V3.1 §5.2.7）。
 */
@Data
@Table(name = "ad_resource")
public class AdResource extends BaseModel {

    @Schema(description = "资源类型:10代理/20媒体")
    private Integer resourceType;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "媒体类型(字典)")
    private String mediaType;

    @Schema(description = "渠道")
    private String channel;

    @Schema(description = "刊例价")
    private String rateCard;

    @Schema(description = "折扣政策")
    private String discountPolicy;

    @Schema(description = "信用代码")
    private String creditCode;

    @Schema(description = "签约主体")
    private String signingEntity;

    @Schema(description = "归属业务主体")
    private String businessEntityId;

    @Schema(description = "状态:10正常/20停用")
    private Integer status = 10;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
