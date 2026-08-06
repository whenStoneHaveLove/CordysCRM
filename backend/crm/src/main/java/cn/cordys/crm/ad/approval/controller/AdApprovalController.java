package cn.cordys.crm.ad.approval.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.approval.service.AdApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 广告审批中心（查询 ad_* 表聚合待审批项）。
 * 订单 approval 动作仍走 AdOrderController / AdSealRecordController / AdOrderChangeController。
 */
@Tag(name = "广告审批中心")
@RestController
@RequestMapping("/api/ad/approval")
public class AdApprovalController {

    @Resource
    private AdApprovalService adApprovalService;

    @PostMapping("/pending")
    @CsPermission(PermissionConstants.AD_APPROVAL_READ)
    @Operation(summary = "待审批分页（body 含 type:order/change/seal）")
    public PagerWithOption<List<Map<String, Object>>> pending(@RequestBody Map<String, Object> params) {
        int current = params.get("current") instanceof Number n ? n.intValue() : 1;
        int pageSize = params.get("pageSize") instanceof Number n ? n.intValue() : 10;
        String type = params.get("type") instanceof String s ? s : null;
        return adApprovalService.pending(current, pageSize, type, OrganizationContext.getOrganizationId());
    }
}
