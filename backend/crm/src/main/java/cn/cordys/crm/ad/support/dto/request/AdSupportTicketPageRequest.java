package cn.cordys.crm.ad.support.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 广告支持工单分页请求（M6，POST /api/ad/support/page）。
 */
@Data
public class AdSupportTicketPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "关键字（工单编号/标题）")
    private String keyword;

    @Schema(description = "工单类型:10订单/20付款/30合同/40素材/50其他")
    private Integer ticketType;

    @Schema(description = "优先级:10低/20中/30高/40紧急")
    private Integer priority;

    @Schema(description = "状态:0待处理/10处理中/20已解决/30已关闭")
    private Integer status;
}
