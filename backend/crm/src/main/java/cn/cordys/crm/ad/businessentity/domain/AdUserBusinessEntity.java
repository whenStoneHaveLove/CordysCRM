package cn.cordys.crm.ad.businessentity.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 用户-业务主体关联（N:N，V3.1 §5.2.8，L-06）。
 * 注：本表按设计仅含 create_time/update_user（无 update_time），仍继承 BaseModel 以获得主键与组织隔离字段。
 */
@Data
@Table(name = "ad_user_business_entity")
public class AdUserBusinessEntity extends BaseModel {

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "是否主要主体:0-否/1-是")
    private Integer isPrimary = 0;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
