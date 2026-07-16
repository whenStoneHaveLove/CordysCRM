package cn.cordys.crm.ad.customer.dto.response;

import cn.cordys.crm.ad.customer.domain.AdCustomer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告客户详情响应（M6 GET /api/ad/customer/{id}）：客户主信息 + 状态标签。
 */
@Data
public class AdCustomerDetailResponse {

    @Schema(description = "客户主信息")
    private AdCustomer customer;

    @Schema(description = "客户等级标签")
    private String customerLevelLabel;

    @Schema(description = "状态标签")
    private String statusLabel;
}
