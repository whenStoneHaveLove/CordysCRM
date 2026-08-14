package cn.cordys.crm.ad.contract.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.contract.domain.AdContract;
import cn.cordys.crm.ad.contract.dto.request.AdContractPageRequest;
import cn.cordys.crm.ad.contract.dto.request.AdContractSaveRequest;
import cn.cordys.crm.ad.contract.dto.response.AdContractDetailResponse;
import cn.cordys.crm.ad.contract.dto.response.AdContractListResponse;
import cn.cordys.crm.ad.contract.service.AdContractService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 广告合同控制器（M5 T-40/T-41，V3.1 §13.2）。
 *
 * <p>所有写操作均在 {@link AdContractService} 中标注 {@code @OperationLog}（写入 ad_operation_log）。
 * 响应由框架 {@code ResultResponseBodyAdvice} 统一包裹。
 * 接口级权限由 {@link CsPermission} 依据 {@link PermissionConstants} 的 AD_CONTRACT_* 码校验（B-2）。</p>
 */
@Tag(name = "广告合同")
@RestController
@RequestMapping("/api/ad/contract")
public class AdContractController {

    @Resource
    private AdContractService adContractService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @CsPermission(PermissionConstants.AD_CONTRACT_CREATE)
    @Operation(summary = "新建合同（默认生效，未申请用印）")
    public AdContract create(@RequestBody AdContractSaveRequest request) {
        return adContractService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_CONTRACT_UPDATE)
    @Operation(summary = "编辑合同（维持编号/状态/用印状态等受控字段）")
    public AdContract update(@RequestBody AdContractSaveRequest request) {
        return adContractService.update(request, userId(), orgId());
    }

    @DeleteMapping("/{id}")
    @CsPermission(PermissionConstants.AD_CONTRACT_DELETE)
    @Operation(summary = "逻辑删除合同")
    public void delete(@PathVariable("id") String id) {
        adContractService.delete(id, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_CONTRACT_READ)
    @Operation(summary = "合同详情（主信息+关联名称+状态标签+用印历史）")
    public AdContractDetailResponse detail(@PathVariable("id") String id) {
        return adContractService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_CONTRACT_READ)
    @Operation(summary = "合同分页（多筛选+关键字+主体隔离+排序）")
    public PagerWithOption<List<AdContractListResponse>> page(@RequestBody AdContractPageRequest request) {
        return adContractService.page(request, userId(), orgId());
    }

    // ===================== 归档审批 =====================

    @PutMapping("/{id}/double-seal")
    @CsPermission(PermissionConstants.AD_CONTRACT_ARCHIVE_APPROVE)
    @Operation(summary = "上传双盖附件（仅保存，不改状态）")
    public AdContract uploadDoubleSeal(@PathVariable("id") String id, @RequestBody Map<String, String> body) {
        return adContractService.uploadDoubleSeal(id, body.get("fileUrl"), userId(), orgId());
    }

    @PutMapping("/{id}/submit-archive")
    @CsPermission(PermissionConstants.AD_CONTRACT_UPDATE)
    @Operation(summary = "提交归档审批（用印状态→归档审批中）")
    public AdContract submitArchive(@PathVariable("id") String id, @RequestBody Map<String, String> body) {
        return adContractService.submitArchive(id, body.get("fileUrl"), userId(), orgId());
    }

    @PutMapping("/{id}/approve-archive")
    @CsPermission(PermissionConstants.AD_CONTRACT_ARCHIVE_APPROVE)
    @Operation(summary = "归档审批通过（用印状态→已归档，老板操作）")
    public AdContract approveArchive(@PathVariable("id") String id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        return adContractService.approveArchive(id, remark, userId(), orgId());
    }

    @PutMapping("/{id}/reject-archive")
    @CsPermission(PermissionConstants.AD_CONTRACT_ARCHIVE_APPROVE)
    @Operation(summary = "归档审批驳回（用印状态→归档审批驳回，老板操作）")
    public AdContract rejectArchive(@PathVariable("id") String id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        return adContractService.rejectArchive(id, remark, userId(), orgId());
    }
}
