package cn.cordys.crm.ad.payment.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentCancelRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentConfirmPrepayRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentInvoiceRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentMediaPostpayRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentMediaPrepayRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentRecordCreateRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentRecordPageRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentReceiveRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentRedInvoiceClearRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentTodoRequest;
import cn.cordys.crm.ad.payment.dto.response.AdPaymentRecordDetailResponse;
import cn.cordys.crm.ad.payment.dto.response.AdPaymentRecordListResponse;
import cn.cordys.crm.ad.payment.dto.response.AdPaymentTodoResponse;
import cn.cordys.crm.ad.payment.domain.AdPaymentRecord;
import cn.cordys.crm.ad.payment.service.AdPaymentRecordService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告收付款（财务中心）控制器（M4 T-30~T-35，V3.1 §13.2）。
 *
 * <p>所有写操作在 {@link AdPaymentRecordService} 中标注 {@code @OperationLog}（写入 ad_operation_log），
 * 主状态流转另写 ad_order_log。响应由框架 {@code ResultResponseBodyAdvice} 统一包裹。
 * 接口级权限由 {@link CsPermission} 依据 {@link PermissionConstants} 的 AD_PAYMENT_* 码校验（B-2）。</p>
 */
@Tag(name = "广告收付款（财务中心）")
@RestController
@RequestMapping("/api/ad/payment")
public class AdPaymentController {

    @Resource
    private AdPaymentRecordService adPaymentRecordService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping("/create")
    @CsPermission(PermissionConstants.AD_PAYMENT_RECEIVE)
    @Operation(summary = "收付款登记（通用：预收/预付/开票收款/媒体尾款/退款/坏账）")
    public AdPaymentRecord create(@RequestBody AdPaymentRecordCreateRequest request) {
        return adPaymentRecordService.create(request, userId(), orgId());
    }

    @PostMapping("/cancel")
    @CsPermission(PermissionConstants.AD_PAYMENT_RECEIVE)
    @Operation(summary = "撤销/冲销收付款记录（反向回写订单金额进度）")
    public AdPaymentRecord cancel(@RequestBody AdPaymentCancelRequest request) {
        return adPaymentRecordService.cancel(request, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_PAYMENT_READ)
    @Operation(summary = "收付款记录分页（多筛选+关键字+主体隔离+排序）")
    public PagerWithOption<List<AdPaymentRecordListResponse>> page(@RequestBody AdPaymentRecordPageRequest request) {
        return adPaymentRecordService.page(request, userId(), orgId());
    }

    @GetMapping("/detail/{id}")
    @CsPermission(PermissionConstants.AD_PAYMENT_READ)
    @Operation(summary = "收付款记录详情")
    public AdPaymentRecordDetailResponse detail(@PathVariable("id") String id) {
        return adPaymentRecordService.detail(id, userId(), orgId());
    }

    @PostMapping("/confirm-prepay")
    @CsPermission(PermissionConstants.AD_PAYMENT_CONFIRM_PREPAY)
    @Operation(summary = "确认预收款（T-31，L-11 基数=应收）")
    public AdPaymentRecord confirmPrepay(@RequestBody AdPaymentConfirmPrepayRequest request) {
        return adPaymentRecordService.confirmPrepay(request, userId(), orgId());
    }

    @PostMapping("/pay-media-prepay")
    @CsPermission(PermissionConstants.AD_PAYMENT_PAY_MEDIA_PREPAY)
    @Operation(summary = "付媒体预付款（T-31，L-28 基数=media_payable）")
    public AdPaymentRecord payMediaPrepay(@RequestBody AdPaymentMediaPrepayRequest request) {
        return adPaymentRecordService.payMediaPrepay(request, userId(), orgId());
    }

    @PostMapping("/invoice")
    @CsPermission(PermissionConstants.AD_PAYMENT_INVOICE)
    @Operation(summary = "开票（T-32，L-02 开票金额=应收，仅回写已开票）")
    public AdPaymentRecord invoice(@RequestBody AdPaymentInvoiceRequest request) {
        return adPaymentRecordService.invoice(request, userId(), orgId());
    }

    @PostMapping("/receive")
    @CsPermission(PermissionConstants.AD_PAYMENT_RECEIVE)
    @Operation(summary = "收款登记（T-32，仅回写已收款）")
    public AdPaymentRecord receive(@RequestBody AdPaymentReceiveRequest request) {
        return adPaymentRecordService.receive(request, userId(), orgId());
    }

    @PostMapping("/pay-media-postpay")
    @CsPermission(PermissionConstants.AD_PAYMENT_PAY_MEDIA_POSTPAY)
    @Operation(summary = "付媒体尾款（T-33，L-05/L-28）")
    public AdPaymentRecord payMediaPostpay(@RequestBody AdPaymentMediaPostpayRequest request) {
        return adPaymentRecordService.payMediaPostpay(request, userId(), orgId());
    }

    @PostMapping("/pay-media-postpay/force")
    @CsPermission(PermissionConstants.AD_PAYMENT_PAY_MEDIA_POSTPAY)
    @Operation(summary = "付媒体尾款-手动强制触发（T-33，应对部分收款/坏账）")
    public AdPaymentRecord payMediaPostpayForce(@RequestBody AdPaymentMediaPostpayRequest request) {
        request.setForce(true);
        return adPaymentRecordService.payMediaPostpay(request, userId(), orgId());
    }

    @PostMapping("/red-invoice/clear")
    @CsPermission(PermissionConstants.AD_PAYMENT_RED_INVOICE_CLEAR)
    @Operation(summary = "清除红冲标记（T-33，L-27，线下红冲处理后）")
    public AdOrder clearRedInvoice(@RequestBody AdPaymentRedInvoiceClearRequest request) {
        return adPaymentRecordService.clearRedInvoice(request, userId(), orgId());
    }

    @PostMapping("/todo/page")
    @CsPermission(PermissionConstants.AD_PAYMENT_READ)
    @Operation(summary = "财务待办（T-33：预收款/媒体预付/媒体尾款/红冲/开票收款）")
    public PagerWithOption<List<AdPaymentTodoResponse>> todoPage(@RequestBody AdPaymentTodoRequest request) {
        return adPaymentRecordService.todoPage(request, userId(), orgId());
    }
}
