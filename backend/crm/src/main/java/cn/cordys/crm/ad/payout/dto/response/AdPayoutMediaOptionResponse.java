package cn.cordys.crm.ad.payout.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 付款单选媒体下拉项（订单的下游媒体，JOIN ad_downstream_media 填充媒体名称）。
 */
@Data
public class AdPayoutMediaOptionResponse {

    @Schema(description = "订单-下游媒体关联id（保存到 ad_payment.media_ids）")
    private String id;

    @Schema(description = "媒体字典id")
    private String mediaId;

    @Schema(description = "媒体名称")
    private String mediaName;
}
