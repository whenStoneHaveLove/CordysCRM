package cn.cordys.crm.ad.receipt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收款单审批请求。
 */
@Data
public class AdReceiptApproveRequest {

    @Schema(description = "动作:APPROVE通过/REJECT驳回")
    private String action;

    @Schema(description = "审批备注")
    private String remark;
}
