package cn.cordys.crm.ad.resource.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 广告资源分页请求（M6，POST /api/ad/resource/page）。
 */
@Data
public class AdResourcePageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "关键字（资源名称/渠道）")
    private String keyword;

    @Schema(description = "资源类型:10线上媒体/20线下广告牌/30电视/40广播/50印刷")
    private Integer resourceType;

    @Schema(description = "状态:0可用/10已占用/20维护中")
    private Integer status;
}
