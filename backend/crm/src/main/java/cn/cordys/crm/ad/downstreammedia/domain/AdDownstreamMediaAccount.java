package cn.cordys.crm.ad.downstreammedia.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 下游客户银行账户。
 * 一个下游客户可挂多个账户，逻辑删除、可停用。无独立权限，沿用下游客户权限。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "ad_downstream_media_account")
public class AdDownstreamMediaAccount extends BaseModel {

    @Schema(description = "下游客户id(ad_downstream_media.id)")
    private String downstreamMediaId;

    @Schema(description = "收款人全称")
    private String payeeName;

    @Schema(description = "开户行")
    private String bankName;

    @Schema(description = "银行账号")
    private String bankAccount;

    @Schema(description = "是否停用:0-启用/1-停用")
    private Integer disabled = 0;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
