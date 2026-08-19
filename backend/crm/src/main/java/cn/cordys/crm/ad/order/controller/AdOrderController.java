package cn.cordys.crm.ad.order.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.order.dto.request.AdOrderApproveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderForceArchiveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderPageRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderSaveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderVoidRequest;
import cn.cordys.crm.ad.order.dto.response.AdOrderDetailResponse;
import cn.cordys.crm.ad.order.dto.response.AdOrderListResponse;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.service.AdOrderService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import cn.cordys.common.pager.PagerWithOption;

/**
 * 广告订单控制器（M2 订单核心闭环，V3.1 §13.2）。
 *
 * <p>所有写操作均在 {@link AdOrderService} 中标注 {@code @OperationLog}（写入 ad_operation_log）。
 * 响应由框架 {@code ResultResponseBodyAdvice} 统一包裹（包路径 cn.cordys）。
 * 接口级权限由 {@link CsPermission} 依据 {@link PermissionConstants} 的 AD_ORDER_* 码校验（B-2）。</p>
 */
@Tag(name = "广告订单")
@RestController
@RequestMapping("/api/ad/order")
public class AdOrderController {

    @Resource
    private AdOrderService adOrderService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @CsPermission(PermissionConstants.AD_ORDER_CREATE)
    @Operation(summary = "新建订单（草稿）")
    public AdOrder create(@RequestBody AdOrderSaveRequest request) {
        return adOrderService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_ORDER_CREATE)
    @Operation(summary = "编辑草稿订单")
    public AdOrder update(@RequestBody AdOrderSaveRequest request) {
        return adOrderService.update(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_ORDER_READ)
    @Operation(summary = "订单详情（订单+附件+改单历史+操作记录+可见动作）")
    public AdOrderDetailResponse detail(@PathVariable("id") String id) {
        return adOrderService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_ORDER_READ)
    @Operation(summary = "订单分页（多筛选+关键字+主体隔离+缺合同标记）")
    public PagerWithOption<List<AdOrderListResponse>> page(@RequestBody AdOrderPageRequest request) {
        return adOrderService.page(request, userId(), orgId());
    }

    @PostMapping("/{id}/submit")
    @CsPermission(PermissionConstants.AD_ORDER_SUBMIT)
    @Operation(summary = "提交（0→10；L-14 关闭时 0→20）")
    public AdOrder submit(@PathVariable("id") String id) {
        return adOrderService.submit(id, userId(), orgId());
    }

    @PostMapping("/{id}/approve")
    @CsPermission(PermissionConstants.AD_ORDER_APPROVE)
    @Operation(summary = "管理组审核（通过 10→20 / 驳回 10→0）")
    public AdOrder approve(@PathVariable("id") String id, @RequestBody AdOrderApproveRequest request) {
        return adOrderService.approve(id, request, userId(), orgId());
    }

    @PostMapping("/{id}/confirm-execute")
    @CsPermission(PermissionConstants.AD_ORDER_CONFIRM_EXECUTE)
    @Operation(summary = "确认执行（45→50）")
    public AdOrder confirmExecute(@PathVariable("id") String id) {
        return adOrderService.confirmExecute(id, userId(), orgId());
    }

    @PostMapping("/{id}/void")
    @CsPermission(PermissionConstants.AD_ORDER_VOID)
    @Operation(summary = "作废（→100；保留附件 L-21，红冲标记 L-27）")
    public AdOrder voidOrder(@PathVariable("id") String id, @RequestBody AdOrderVoidRequest request) {
        return adOrderService.voidOrder(id, request, userId(), orgId());
    }

    @PostMapping("/{id}/force-archive")
    @CsPermission(PermissionConstants.AD_ORDER_FORCE_ARCHIVE)
    @Operation(summary = "强制归档（80→90，管理组，带坏账金额 L-13）")
    public AdOrder forceArchive(@PathVariable("id") String id, @RequestBody AdOrderForceArchiveRequest request) {
        return adOrderService.forceArchive(id, request, userId(), orgId());
    }
}
