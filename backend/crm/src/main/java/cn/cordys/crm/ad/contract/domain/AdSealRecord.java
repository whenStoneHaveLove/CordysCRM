package cn.cordys.crm.ad.contract.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

/**
 * 广告用印记录（V3.1 §5.2.4，L-07/L-19）。
 */
@Data
@Table(name = "ad_seal_record")
public class AdSealRecord extends BaseModel {

    @Schema(description = "合同id")
    private String contractId;

    @Schema(description = "用印类型:10公章/20合同章")
    private Integer sealType;

    @Schema(description = "申请份数")
    private Integer appliedCopies;

    @Schema(description = "实际盖章份数(审批时填)")
    private Integer actualCopies;

    @Schema(description = "申请人")
    private String applicantId;

    @Schema(description = "申请备注")
    private String applyRemark;

    @Schema(description = "状态:0审批中/10通过/20驳回")
    private Integer status = 0;

    @Schema(description = "审批人")
    private String approverId;

    @Schema(description = "审批时间")
    private Date approvedAt;

    @Schema(description = "审批备注")
    private String approveRemark;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
