package cn.cordys.crm.ad.seal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用印申请请求（M5，POST /api/ad/seal/apply 或合同 submit-seal 共用）。
 *
 * <p>媒介(ROLE_MEDIA)发起：基于合同申请用印，填写用印类型与申请份数（L-07 单笔合同可先申请后盖章）。
 * 服务据此创建 {@code ad_seal_record}（status=0 审批中）并将合同 {@code seal_status} 置为 10 审批中。</p>
 */
@Data
public class AdSealApplyRequest {

    @Schema(description = "合同id")
    private String contractId;

    @Schema(description = "用印类型:10公章/20合同章")
    private Integer sealType;

    @Schema(description = "申请份数")
    private Integer appliedCopies;

    @Schema(description = "申请备注")
    private String applyRemark;
}
