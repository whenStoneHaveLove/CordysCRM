package cn.cordys.crm.ad.payout.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 付款单付款请求（待付款 → 已付款）。
 */
@Data
public class AdPayoutPayRequest {

    @Schema(description = "付款备注")
    private String payRemark;
}