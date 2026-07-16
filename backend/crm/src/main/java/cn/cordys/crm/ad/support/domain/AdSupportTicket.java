package cn.cordys.crm.ad.support.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 广告支持工单（M6，V3.1 广告支持工单管理）。
 */
@Data
@Table(name = "ad_support_ticket")
public class AdSupportTicket extends BaseModel {

    @Schema(description = "工单编号(自动生成)")
    private String ticketNo;

    @Schema(description = "工单标题")
    private String title;

    @Schema(description = "工单描述")
    private String description;

    @Schema(description = "工单类型:10订单/20付款/30合同/40素材/50其他")
    private Integer ticketType;

    @Schema(description = "优先级:10低/20中/30高/40紧急")
    private Integer priority = 20;

    @Schema(description = "状态:0待处理/10处理中/20已解决/30已关闭")
    private Integer status = 0;

    @Schema(description = "关联订单id(可空)")
    private String relatedOrderId;

    @Schema(description = "指派处理人")
    private String assignedTo;

    @Schema(description = "解决方案")
    private String resolution;

    @Schema(description = "关闭时间")
    private Long closeTime;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
