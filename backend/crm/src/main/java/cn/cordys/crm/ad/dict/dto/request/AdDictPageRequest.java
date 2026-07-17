package cn.cordys.crm.ad.dict.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 广告字典分页请求（V3.1 §4.3，POST /api/ad/dict/page）。
 */
@Data
public class AdDictPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "字典编码（精确筛选）")
    private String dictCode;

    @Schema(description = "关键字（显示名/字典值/编码）")
    private String keyword;

    @Schema(description = "状态:10启用/20停用")
    private Integer status;
}
