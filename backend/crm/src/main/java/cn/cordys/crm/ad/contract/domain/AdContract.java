package cn.cordys.crm.ad.contract.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 广告合同统一表（V3.1 §5.2.3，L-10）。
 */
@Data
@Table(name = "ad_contract")
public class AdContract extends BaseModel {

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合同名称")
    private String contractName;

    @Schema(description = "业务主体")
    private String businessEntityId;

    @Schema(description = "合同方向:10上游/20下游")
    private Integer contractDirection;

    @Schema(description = "合同类型:10框架/20单笔")
    private Integer contractType;

    @Schema(description = "关联方id")
    private String relatedPartyId;

    @Schema(description = "关联方类型:10客户/20上游代理/30下游媒体")
    private Integer relatedPartyType;

    @Schema(description = "关联订单(单笔合同)")
    private String orderId;

    @Schema(description = "签约主体")
    private String signingEntity;

    @Schema(description = "有效期起")
    private Date validFrom;

    @Schema(description = "有效期止")
    private Date validTo;

    @Schema(description = "合同金额")
    private BigDecimal amount;

    @Schema(description = "返点条款")
    private String rebateTerms;

    @Schema(description = "合同文件(单笔用印前可空,L-07)")
    private String fileUrl;

    @Schema(description = "用印状态:0未申请/10审批中/20已用印/30已驳回")
    private Integer sealStatus = 0;

    @Schema(description = "状态:10生效/20失效/30已作废")
    private Integer status = 10;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
