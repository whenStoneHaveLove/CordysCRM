package cn.cordys.crm.ad.seal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 用印记录列表项（M5 pageList 映射）。字段别名与 mapper XML 中 SELECT 的 AS 保持一致（驼峰属性名）。
 */
@Data
public class AdSealRecordListResponse {

    @Schema(description = "用印记录id")
    private String id;

    @Schema(description = "合同id")
    private String contractId;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合同名称")
    private String contractName;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "业务主体名称")
    private String businessEntityName;

    @Schema(description = "用印类型:10公章/20合同章")
    private Integer sealType;

    @Schema(description = "用印类型标签")
    private String sealTypeLabel;

    @Schema(description = "申请份数")
    private Integer appliedCopies;

    @Schema(description = "实际盖章份数")
    private Integer actualCopies;

    @Schema(description = "申请人")
    private String applicantId;

    @Schema(description = "申请备注")
    private String applyRemark;

    @Schema(description = "状态:0审批中/10通过/20驳回")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "审批人")
    private String approverId;

    @Schema(description = "审批时间")
    private Date approvedAt;

    @Schema(description = "审批备注")
    private String approveRemark;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;
}
