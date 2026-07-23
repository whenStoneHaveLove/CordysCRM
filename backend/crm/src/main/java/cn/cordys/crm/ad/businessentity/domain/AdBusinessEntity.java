package cn.cordys.crm.ad.businessentity.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 业务主体（骥笙/泰和，V3.1 §4.1）。
 */
@Data
@Table(name = "ad_business_entity")
public class AdBusinessEntity extends BaseModel {

    @Schema(description = "主体名称")
    private String name;

    @Schema(description = "主体代码(JS/TH)")
    private String code;

    @Schema(description = "状态:10启用/20停用")
    private Integer status = 10;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;

    @Schema(description = "是否跨主体:0-否/1-是")
    @Column(name = "is_cross_entity")
    private Integer isCrossEntity = 0;

    @Schema(description = "备注")
    @Column(name = "remark")
    private String remark;
}
