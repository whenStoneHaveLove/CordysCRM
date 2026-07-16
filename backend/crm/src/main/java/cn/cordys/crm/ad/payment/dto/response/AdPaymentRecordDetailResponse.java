package cn.cordys.crm.ad.payment.dto.response;

import cn.cordys.crm.ad.payment.domain.AdPaymentRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收付款记录详情（M4，GET /api/ad/payment/detail/{id}）。
 */
@Data
public class AdPaymentRecordDetailResponse {

    @Schema(description = "收付款记录")
    private AdPaymentRecord record;

    @Schema(description = "关联订单编号")
    private String orderNo;

    @Schema(description = "方向标签")
    private String directionLabel;

    @Schema(description = "类型标签")
    private String typeLabel;
}
