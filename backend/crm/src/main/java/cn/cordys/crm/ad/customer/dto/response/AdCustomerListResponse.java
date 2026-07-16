package cn.cordys.crm.ad.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告客户列表项（M6 pageList 映射）。
 */
@Data
public class AdCustomerListResponse {

    @Schema(description = "客户id")
    private String id;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "行业")
    private String industry;

    @Schema(description = "客户等级:10VIP/20普通/30潜力")
    private Integer customerLevel;

    @Schema(description = "客户等级标签")
    private String customerLevelLabel;

    @Schema(description = "状态:0活跃/10非活跃/20黑名单")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;
}
