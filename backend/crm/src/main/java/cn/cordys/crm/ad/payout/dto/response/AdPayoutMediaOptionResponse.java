package cn.cordys.crm.ad.payout.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 付款单选下拉项（订单的下游客户，JOIN ad_downstream_media 填充名称）。
 */
@Data
public class AdPayoutMediaOptionResponse {

    @Schema(description = "订单-下游客户关联id（保存到 ad_payment.media_ids）")
    private String id;

    @Schema(description = "字典id")
    private String mediaId;

    @Schema(description = "名称")
    private String mediaName;
}
