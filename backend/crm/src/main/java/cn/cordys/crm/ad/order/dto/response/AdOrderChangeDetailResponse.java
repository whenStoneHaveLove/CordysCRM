package cn.cordys.crm.ad.order.dto.response;

import cn.cordys.crm.ad.order.domain.AdOrderChange;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 改单详情（M3 GET /api/ad/order-change/{id}）。
 */
@Data
public class AdOrderChangeDetailResponse {

    @Schema(description = "改单记录")
    private AdOrderChange change;

    @Schema(description = "关联订单编号")
    private String orderNo;

    @Schema(description = "状态中文标签")
    private String statusLabel;
}
