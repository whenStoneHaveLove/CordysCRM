package cn.cordys.crm.ad.seal.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.contract.domain.AdSealRecord;
import cn.cordys.crm.ad.seal.dto.request.AdSealApplyRequest;
import cn.cordys.crm.ad.seal.dto.request.AdSealApproveRequest;
import cn.cordys.crm.ad.seal.dto.request.AdSealRecordPageRequest;
import cn.cordys.crm.ad.seal.dto.request.AdSealUploadRequest;
import cn.cordys.crm.ad.seal.dto.response.AdSealRecordDetailResponse;
import cn.cordys.crm.ad.seal.dto.response.AdSealRecordListResponse;
import cn.cordys.crm.ad.seal.service.AdSealRecordService;
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
 * 广告用印控制器（M5 T-42，V3.1 §13.2）。
 *
 * <p>所有写操作均在 {@link AdSealRecordService} 中标注 {@code @OperationLog}（写入 ad_operation_log）。
 * 响应由框架 {@code ResultResponseBodyAdvice} 统一包裹。
 * 接口级权限由 {@link CsPermission} 依据 {@link PermissionConstants} 的 AD_SEAL_* 码校验（B-2）。</p>
 */
@Tag(name = "广告用印")
@RestController
@RequestMapping("/api/ad/seal")
public class AdSealRecordController {

    @Resource
    private AdSealRecordService adSealRecordService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @CsPermission(PermissionConstants.AD_SEAL_APPLY)
    @Operation(summary = "申请用印（先申请后盖章 L-07；合同置审批中）")
    public AdSealRecord apply(@RequestBody AdSealApplyRequest request) {
        return adSealRecordService.apply(request, userId(), orgId());
    }

    @PostMapping("/{id}/approve")
    @CsPermission(PermissionConstants.AD_SEAL_APPROVE)
    @Operation(summary = "用印审批通过（填实际份数；合同有文件则直接已用印）")
    public AdSealRecord approve(@PathVariable("id") String id,
                                @RequestBody(required = false) AdSealApproveRequest request) {
        return adSealRecordService.approve(id, request, userId(), orgId());
    }

    @PostMapping("/{id}/reject")
    @CsPermission(PermissionConstants.AD_SEAL_REJECT)
    @Operation(summary = "用印驳回（合同置已驳回）")
    public AdSealRecord reject(@PathVariable("id") String id,
                               @RequestBody(required = false) AdSealApproveRequest request) {
        return adSealRecordService.reject(id, request, userId(), orgId());
    }

    @PostMapping("/{id}/upload")
    @CsPermission(PermissionConstants.AD_SEAL_UPLOAD)
    @Operation(summary = "执行：上传盖章版（单笔先申请后盖章，回写合同文件并置已用印）")
    public AdSealRecord upload(@PathVariable("id") String id, @RequestBody AdSealUploadRequest request) {
        return adSealRecordService.upload(id, request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_SEAL_READ)
    @Operation(summary = "用印记录详情")
    public AdSealRecordDetailResponse detail(@PathVariable("id") String id) {
        return adSealRecordService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_SEAL_READ)
    @Operation(summary = "用印记录分页（多筛选+关键字+主体隔离+排序）")
    public PagerWithOption<List<AdSealRecordListResponse>> page(@RequestBody AdSealRecordPageRequest request) {
        return adSealRecordService.page(request, userId(), orgId());
    }
}
