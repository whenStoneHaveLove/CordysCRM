package cn.cordys.crm.ad.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 改单审批请求（M3 POST /api/ad/order-change/{id}/approve|reject）。
 * 审批人取当前登录用户；备注可选。
 */
@Data
public class AdOrderChangeApproveRequest {

    @Schema(description = "审批备注")
    private String remark;
}
