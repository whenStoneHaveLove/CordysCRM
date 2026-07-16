package cn.cordys.crm.ad.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告客户保存请求（M6，创建/编辑共用）。
 * <p>创建时 {@code id} 留空，由服务生成；编辑时必须携带 {@code id}。</p>
 */
@Data
public class AdCustomerSaveRequest {

    @Schema(description = "客户id（编辑时必填，创建时留空）")
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

    @Schema(description = "状态:0活跃/10非活跃/20黑名单")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
