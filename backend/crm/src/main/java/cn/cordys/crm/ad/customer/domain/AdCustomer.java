package cn.cordys.crm.ad.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 广告客户（V3.1 §5.2.7，M6扩展）。
 *
 * <p>M1~M5 字段：id, name, brand, industryCode, signingEntity, organizationId, deleted。
 * M6 新增：contactPerson, contactPhone, email, address, industry, customerLevel,
 * status(客户状态), remark（广告客户管理场景）。</p>
 */
@Data
@Table(name = "ad_customer")
public class AdCustomer extends BaseModel {

    /** M1~M5: 客户名称（M6中视为customerName） */
    @Schema(description = "客户名称(全局唯一)")
    private String name;

    /** M1~M5: 品牌 */
    @Schema(description = "品牌")
    private String brand;

    /** M1~M5: 行业类别(字典) */
    @Schema(description = "行业类别(字典)")
    private String industryCode;

    /** M1~M5: 签约主体 */
    @Schema(description = "签约主体")
    private String signingEntity;

    /** M6: 联系人 */
    @Schema(description = "联系人(M6)")
    private String contactPerson;

    /** M6: 联系电话 */
    @Schema(description = "联系电话(M6)")
    private String contactPhone;

    /** M6: 邮箱 */
    @Schema(description = "邮箱(M6)")
    private String email;

    /** M6: 地址 */
    @Schema(description = "地址(M6)")
    private String address;

    /** M6: 行业 */
    @Schema(description = "行业(M6)")
    private String industry;

    /** M6: 客户等级:10VIP/20普通/30潜力 */
    @Schema(description = "客户等级:10VIP/20普通/30潜力(M6)")
    private Integer customerLevel;

    /** M6: 客户状态:0活跃/10非活跃/20黑名单 */
    @Schema(description = "客户状态:0活跃/10非活跃/20黑名单(M6)")
    private Integer status = 0;

    /** M6: 备注 */
    @Schema(description = "备注(M6)")
    private String remark;

    /** M1~M5: 组织(租户)id */
    @Schema(description = "组织(租户)id")
    private String organizationId;

    /** 是否删除:0-否/1-是 */
    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
