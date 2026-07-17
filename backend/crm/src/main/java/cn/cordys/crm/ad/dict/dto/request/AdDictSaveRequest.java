package cn.cordys.crm.ad.dict.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告字典保存请求（V3.1 §5.3 / §4.3，创建/编辑共用）。
 * <p>创建时 {@code id} 留空，由服务生成；编辑时必须携带 {@code id}。</p>
 */
@Data
public class AdDictSaveRequest {

    @Schema(description = "字典id（编辑时必填，创建时留空）")
    private String id;

    @Schema(description = "字典编码(industry/media_type/seal_type/receipt_method/payment_method)")
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
}
