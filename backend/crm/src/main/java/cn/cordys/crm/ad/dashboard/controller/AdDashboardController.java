package cn.cordys.crm.ad.dashboard.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.dashboard.service.AdDashboardService;
import cn.cordys.crm.ad.report.dto.response.AdWorkbenchTodoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 广告工作台控制器（V3.1 §8.5，B-5）。
 *
 * <p>分角色待办聚合：GET /api/ad/dashboard/todo。接口级权限由
 * {@link cn.cordys.common.permission.CsPermission} 依据 {@link PermissionConstants#AD_WORKBENCH_READ} 校验。</p>
 */
@Tag(name = "广告工作台")
@RestController
@RequestMapping("/api/ad/dashboard")
public class AdDashboardController {

    @Resource
    private AdDashboardService adDashboardService;

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @GetMapping("/todo")
    @CsPermission(PermissionConstants.AD_WORKBENCH_READ)
    @Operation(summary = "分角色工作台待办聚合（媒介/老板/财务）")
    public AdWorkbenchTodoResponse todo() {
        return adDashboardService.workbenchTodo(orgId());
    }
}
