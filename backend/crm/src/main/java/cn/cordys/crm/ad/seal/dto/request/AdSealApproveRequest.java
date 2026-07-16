package cn.cordys.crm.ad.seal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用印审批请求（M5，POST /api/ad/seal/{contractId}/approve|reject 或合同 approve-seal/reject-seal 共用）。
 *
 * <p>老板(ROLE_BOSS)审批：通过时填实际盖章份数；驳回时填驳回备注。</p>
 */
@Data
public class AdSealApproveRequest {

    @Schema(description = "实际盖章份数（审批通过时必填）")
    private Integer actualCopies;

    @Schema(description = "审批备注（驳回时建议填写）")
    private String approveRemark;
}
