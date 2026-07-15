package cn.cordys.crm.ad.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单作废请求（M2 POST /api/ad/order/{id}/void）。
 */
@Data
public class AdOrderVoidRequest {

    @Schema(description = "作废原因")
    private String reason;
}
