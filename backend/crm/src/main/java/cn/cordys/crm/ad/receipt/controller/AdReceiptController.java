package cn.cordys.crm.ad.receipt.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.receipt.domain.AdReceipt;
import cn.cordys.crm.ad.receipt.dto.request.AdReceiptApproveRequest;
import cn.cordys.crm.ad.receipt.dto.request.AdReceiptPageRequest;
import cn.cordys.crm.ad.receipt.dto.request.AdReceiptSaveRequest;
import cn.cordys.crm.ad.receipt.dto.response.AdReceiptDetailResponse;
import cn.cordys.crm.ad.receipt.dto.response.AdReceiptListResponse;
import cn.cordys.crm.ad.receipt.service.AdReceiptService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 广告收款单控制器。
 */
@Tag(name = "广告收款单")
@RestController
@RequestMapping("/api/ad/receipt")
public class AdReceiptController {

    @Resource
    private AdReceiptService adReceiptService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @CsPermission(PermissionConstants.AD_RECEIPT_CREATE)
    @Operation(summary = "新建收款单")
    public AdReceipt create(@RequestBody AdReceiptSaveRequest request) {
        return adReceiptService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_RECEIPT_UPDATE)
    @Operation(summary = "编辑收款单")
    public AdReceipt update(@RequestBody AdReceiptSaveRequest request) {
        return adReceiptService.update(request, userId(), orgId());
    }

    @PostMapping("/{id}/submit")
    @CsPermission(PermissionConstants.AD_RECEIPT_UPDATE)
    @Operation(summary = "提交收款单（草稿→待审核）")
    public AdReceipt submit(@PathVariable("id") String id) {
        return adReceiptService.submit(id, userId(), orgId());
    }

    @PutMapping("/{id}/approve")
    @CsPermission(PermissionConstants.AD_RECEIPT_APPROVE)
    @Operation(summary = "审核收款单（通过/驳回）")
    public AdReceipt approve(@PathVariable("id") String id, @RequestBody AdReceiptApproveRequest request) {
        return adReceiptService.approve(id, request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_RECEIPT_READ)
    @Operation(summary = "收款单详情（含审计/订单/合同信息）")
    public AdReceiptDetailResponse detail(@PathVariable("id") String id) {
        return adReceiptService.detail(id, userId(), orgId());
    }

    @GetMapping("/remaining/{orderId}")
    @CsPermission(PermissionConstants.AD_RECEIPT_READ)
    @Operation(summary = "订单剩余应收金额（应收-已收）")
    public BigDecimal remainingReceivable(@PathVariable("orderId") String orderId) {
        return adReceiptService.remainingReceivable(orderId, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_RECEIPT_READ)
    @Operation(summary = "收款单分页")
    public PagerWithOption<List<AdReceiptListResponse>> page(@RequestBody AdReceiptPageRequest request) {
        return adReceiptService.page(request, userId(), orgId());
    }

    @DeleteMapping("/{id}")
    @CsPermission(PermissionConstants.AD_RECEIPT_DELETE)
    @Operation(summary = "删除收款单")
    public void delete(@PathVariable("id") String id) {
        adReceiptService.delete(id, userId(), orgId());
    }
}
