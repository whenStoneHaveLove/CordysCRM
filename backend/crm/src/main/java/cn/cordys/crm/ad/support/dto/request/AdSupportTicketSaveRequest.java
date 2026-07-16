package cn.cordys.crm.ad.support.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告支持工单保存请求（M6，创建/编辑共用）。
 * <p>创建时 {@code id} 留空，由服务生成；编辑时携带 {@code id}。</p>
 */
@Data
public class AdSupportTicketSaveRequest {

    @Schema(description = "工单id（编辑时必填，创建时留空）")
    private String id;

    @Schema(description = "工单标题")
    private String title;

    @Schema(description = "工单描述")
    private String description;

    @Schema(description = "工单类型:10订单/20付款/30合同/40素材/50其他")
    private Integer ticketType;

    @Schema(description = "优先级:10低/20中/30高/40紧急")
    private Integer priority;

    @Schema(description = "关联订单id(可空)")
    private String relatedOrderId;

    @Schema(description = "指派处理人（编辑时可用）")
    private String assignedTo;

    @Schema(description = "解决方案（编辑时可用）")
    private String resolution;

    @Schema(description = "状态变更:0待处理/10处理中/20已解决（编辑时可用）")
    private Integer status;
}
