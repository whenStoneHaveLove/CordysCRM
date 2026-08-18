package cn.cordys.crm.ad.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单允许动作（按当前用户角色计算，M2 详情返回）。
 */
@Data
public class AdOrderAllowedAction {

    @Schema(description = "源状态")
    private Integer fromStatus;

    @Schema(description = "目标状态")
    private Integer toStatus;

    @Schema(description = "触发动作(SUBMIT/APPROVE/REJECT/...)")
    private String trigger;

    @Schema(description = "所需权限码(AD_ORDER:*/AD_ORDER_CHANGE:*/SYSTEM)，走分配的权限码体系")
    private String requiredRole;

    @Schema(description = "当前用户是否有权执行")
    private Boolean allowed;

    @Schema(description = "目标状态中文标签")
    private String label;
}
