package cn.cordys.crm.ad.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 工作台待办聚合响应（V3.1 §8.5，B-5）。
 * <p>仅返回当前用户所具备的广告角色（MEDIA/BOSS/FINANCE）对应的待办分组，避免越权泄露其他角色数据。</p>
 */
@Data
public class AdWorkbenchTodoResponse {

    @Schema(description = "当前用户具备的广告角色（MEDIA/BOSS/FINANCE）")
    private List<String> roles;

    @Schema(description = "分角色待办卡片：key=角色，value=该角色待办列表")
    private Map<String, List<AdWorkbenchTodoItem>> todos;
}
