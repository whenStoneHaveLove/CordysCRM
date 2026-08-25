package cn.cordys.crm.ad.dict.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 广告字典表（V3.1 §5.3/§4.3）。可运营维护的枚举项（行业/类型/用印类型/收付款方式等）。
 */
@Data
@Table(name = "ad_dict")
public class AdDict extends BaseModel {

    @Schema(description = "字典编码(industry/media_type/seal_type/receipt_method/payment_method)")
    private String dictCode;

    @Schema(description = "字典值(枚举码)")
    private String dictValue;

    @Schema(description = "显示名")
    private String dictLabel;

    @Schema(description = "父级值")
    private String parentValue;

    @Schema(description = "排序")
    private Integer sort = 0;

    @Schema(description = "状态:10启用/20停用")
    private Integer status = 10;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
