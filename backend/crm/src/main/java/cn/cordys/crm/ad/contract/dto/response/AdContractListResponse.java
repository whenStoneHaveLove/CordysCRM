package cn.cordys.crm.ad.contract.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同列表项（M5 pageList 映射）。字段别名与 mapper XML 中 SELECT 的 AS 保持一致（驼峰属性名）。
 */
@Data
public class AdContractListResponse {

    @Schema(description = "合同id")
    private String id;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合同名称")
    private String contractName;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "业务主体名称")
    private String businessEntityName;

    @Schema(description = "合同方向:10上游/20下游")
    private Integer contractDirection;

    @Schema(description = "合同方向标签")
    private String contractDirectionLabel;

    @Schema(description = "合同类型:10框架/20单笔/30服务/40其他")
    private Integer contractType;

    @Schema(description = "合同类型标签")
    private String contractTypeLabel;

    @Schema(description = "关联方id")
    private String relatedPartyId;

    @Schema(description = "关联方类型:10客户/20上游代理/30下游媒体")
    private Integer relatedPartyType;

    @Schema(description = "关联方类型标签")
    private String relatedPartyTypeLabel;

    @Schema(description = "签约主体")
    private String signingEntity;

    @Schema(description = "有效期起")
    private Date validFrom;

    @Schema(description = "有效期止")
    private Date validTo;

    @Schema(description = "合同金额")
    private BigDecimal amount;

    @Schema(description = "用印附件URL")
    private String fileUrl;

    @Schema(description = "双盖附件URL")
    private String doubleSealFileUrl;

    @Schema(description = "用印状态:0未申请/10审批中/20已用印/30已驳回/40归档审批中/50归档审批驳回/60已归档")
    private Integer sealStatus;

    @Schema(description = "用印状态标签")
    private String sealStatusLabel;

    @Schema(description = "状态:10生效/20失效/30已作废")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;
}
