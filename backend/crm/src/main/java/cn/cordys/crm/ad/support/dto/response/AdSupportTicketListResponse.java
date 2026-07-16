package cn.cordys.crm.ad.support.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告支持工单列表项（M6 pageList 映射）。
 */
@Data
public class AdSupportTicketListResponse {

    @Schema(description = "工单id")
    private String id;

    @Schema(description = "工单编号")
    private String ticketNo;

    @Schema(description = "工单标题")
    private String title;

    @Schema(description = "工单类型:10订单/20付款/30合同/40素材/50其他")
    private Integer ticketType;

    @Schema(description = "工单类型标签")
    private String ticketTypeLabel;

    @Schema(description = "优先级:10低/20中/30高/40紧急")
    private Integer priority;

    @Schema(description = "优先级标签")
    private String priorityLabel;

    @Schema(description = "状态:0待处理/10处理中/20已解决/30已关闭")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "关联订单id")
    private String relatedOrderId;

    @Schema(description = "指派处理人")
    private String assignedTo;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "关闭时间")
    private Long closeTime;
}
