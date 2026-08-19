package cn.cordys.crm.ad.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理组审核请求（M2 POST /api/ad/order/{id}/approve）。
 * action=APPROVE 表示通过(10→20)；action=REJECT 表示驳回(10→0，保留附件 L-21)。
 */
@Data
public class AdOrderApproveRequest {

    @Schema(description = "审核动作:APPROVE 通过 / REJECT 驳回")
    private String action = "APPROVE";

    @Schema(description = "审核备注")
    private String remark;
}
