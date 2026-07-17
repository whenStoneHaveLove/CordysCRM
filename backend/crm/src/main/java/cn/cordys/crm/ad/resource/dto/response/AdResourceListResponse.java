package cn.cordys.crm.ad.resource.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 广告资源列表项（M6 pageList 映射）。
 */
@Data
public class AdResourceListResponse {

    @Schema(description = "资源id")
    private String id;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "资源类型:10上游代理/20下游媒体")
    private Integer resourceType;

    @Schema(description = "资源类型标签")
    private String resourceTypeLabel;

    @Schema(description = "媒体渠道")
    private String mediaChannel;

    @Schema(description = "广告位")
    private String position;

    @Schema(description = "日均曝光量")
    private Long dailyImpressions;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "状态:10正常/20停用")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "归属业务主体id")
    private String businessEntityId;

    @Schema(description = "归属业务主体名称")
    private String businessEntityName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;
}
