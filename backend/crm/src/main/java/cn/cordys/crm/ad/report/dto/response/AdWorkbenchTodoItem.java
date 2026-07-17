package cn.cordys.crm.ad.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工作台待办项（V3.1 §8.5，B-5）。
 */
@Data
public class AdWorkbenchTodoItem {

    @Schema(description = "待办key（前端用于图标/跳转）")
    private String key;

    @Schema(description = "待办标签")
    private String label;

    @Schema(description = "待办数量")
    private long count;

    @Schema(description = "点击跳转路由（前端使用）")
    private String link;
}
