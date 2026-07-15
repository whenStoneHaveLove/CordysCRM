package cn.cordys.crm.ad.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 广告客户库（V3.1 §5.2.7，L-16）。无 business_entity_id（全局唯一）。
 */
@Data
@Table(name = "ad_customer")
public class AdCustomer extends BaseModel {

    @Schema(description = "客户名称(全局唯一)")
    private String name;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "行业类别(字典)")
    private String industryCode;

    @Schema(description = "签约主体")
    private String signingEntity;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
