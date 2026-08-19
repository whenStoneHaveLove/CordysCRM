package cn.cordys.crm.ad.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 强制归档请求（M2 POST /api/ad/order/{id}/force-archive，管理组执行，L-13）。
 */
@Data
public class AdOrderForceArchiveRequest {

    @Schema(description = "坏账金额(L-13)")
    private BigDecimal badDebtAmount;
}
