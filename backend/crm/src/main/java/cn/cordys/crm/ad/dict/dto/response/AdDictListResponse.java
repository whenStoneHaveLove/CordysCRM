package cn.cordys.crm.ad.dict.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告字典列表项（V3.1 §4.3 pageList 映射）。
 */
@Data
public class AdDictListResponse {

    @Schema(description = "字典id")
    private String id;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典值(枚举码)")
    private String dictValue;

    @Schema(description = "显示名")
    private String dictLabel;

    @Schema(description = "父级值")
    private String parentValue;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态:10启用/20停用")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;
}
