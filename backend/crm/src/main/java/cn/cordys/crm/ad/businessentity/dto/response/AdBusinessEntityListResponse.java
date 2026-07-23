package cn.cordys.crm.ad.businessentity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 业务主体列表项（V3.1 §4.1 pageList 映射）。
 */
@Data
public class AdBusinessEntityListResponse {

    @Schema(description = "主体id")
    private String id;

    @Schema(description = "主体名称")
    private String name;

    @Schema(description = "主体代码")
    private String code;

    @Schema(description = "状态:10启用/20停用")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "是否跨主体:0-否/1-是")
    private Integer isCrossEntity;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "关联用户数")
    private Integer userCount;

    @Schema(description = "关联订单数")
    private Integer orderCount;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;
}
