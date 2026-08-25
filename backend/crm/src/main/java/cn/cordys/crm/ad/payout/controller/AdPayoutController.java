package cn.cordys.crm.ad.payout.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.payout.domain.AdPayout;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutApproveRequest;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutPageRequest;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutSaveRequest;
import cn.cordys.crm.ad.payout.dto.response.AdPayoutDetailResponse;
import cn.cordys.crm.ad.payout.dto.response.AdPayoutListResponse;
import cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaOptionResponse;
import cn.cordys.crm.ad.payout.service.AdPayoutService;
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
 * 广告付款单控制器。
 */
@Tag(name = "广告付款单")
@RestController
@RequestMapping("/api/ad/payout")
public class AdPayoutController {

    @Resource
    private AdPayoutService adPayoutService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @CsPermission(PermissionConstants.AD_PAYOUT_CREATE)
    @Operation(summary = "新建付款单")
    public AdPayout create(@RequestBody AdPayoutSaveRequest request) {
        return adPayoutService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_PAYOUT_UPDATE)
    @Operation(summary = "编辑付款单")
    public AdPayout update(@RequestBody AdPayoutSaveRequest request) {
        return adPayoutService.update(request, userId(), orgId());
    }

    @PostMapping("/{id}/submit")
    @CsPermission(PermissionConstants.AD_PAYOUT_UPDATE)
    @Operation(summary = "提交付款单（草稿→待审核）")
    public AdPayout submit(@PathVariable("id") String id) {
        return adPayoutService.submit(id, userId(), orgId());
    }

    @PutMapping("/{id}/approve")
    @CsPermission(PermissionConstants.AD_PAYOUT_APPROVE)
    @Operation(summary = "审核付款单（通过/驳回）")
    public AdPayout approve(@PathVariable("id") String id, @RequestBody AdPayoutApproveRequest request) {
        return adPayoutService.approve(id, request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_PAYOUT_READ)
    @Operation(summary = "付款单详情（含审计/订单/合同信息）")
    public AdPayoutDetailResponse detail(@PathVariable("id") String id) {
        return adPayoutService.detail(id, userId(), orgId());
    }

    @GetMapping("/remaining/{orderId}")
    @CsPermission(PermissionConstants.AD_PAYOUT_READ)
    @Operation(summary = "订单剩余应付金额（应付-已付）")
    public BigDecimal remainingPayable(@PathVariable("orderId") String orderId) {
        return adPayoutService.remainingPayable(orderId, userId(), orgId());
    }

    @GetMapping("/media/{orderId}")
    @CsPermission(PermissionConstants.AD_PAYOUT_READ)
    @Operation(summary = "订单的下游客户列表（付款勾选用，含名称）")
    public List<AdPayoutMediaOptionResponse> listMedia(@PathVariable("orderId") String orderId) {
        return adPayoutService.listMedia(orderId, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_PAYOUT_READ)
    @Operation(summary = "付款单分页")
    public PagerWithOption<List<AdPayoutListResponse>> page(@RequestBody AdPayoutPageRequest request) {
        return adPayoutService.page(request, userId(), orgId());
    }

    @DeleteMapping("/{id}")
    @CsPermission(PermissionConstants.AD_PAYOUT_DELETE)
    @Operation(summary = "删除付款单")
    public void delete(@PathVariable("id") String id) {
        adPayoutService.delete(id, userId(), orgId());
    }
}
