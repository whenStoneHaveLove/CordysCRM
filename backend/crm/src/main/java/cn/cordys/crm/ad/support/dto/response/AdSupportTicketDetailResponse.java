package cn.cordys.crm.ad.support.dto.response;

import cn.cordys.crm.ad.support.domain.AdSupportTicket;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告支持工单详情响应（M6 GET /api/ad/support/{id}）：工单主信息 + 状态标签。
 */
@Data
public class AdSupportTicketDetailResponse {

    @Schema(description = "工单主信息")
    private AdSupportTicket ticket;

    @Schema(description = "工单类型标签")
    private String ticketTypeLabel;

    @Schema(description = "优先级标签")
    private String priorityLabel;

    @Schema(description = "状态标签")
    private String statusLabel;
}
