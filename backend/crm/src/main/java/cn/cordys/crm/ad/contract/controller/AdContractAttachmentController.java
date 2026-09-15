package cn.cordys.crm.ad.contract.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.contract.domain.AdContractAttachment;
import cn.cordys.crm.ad.contract.service.AdContractAttachmentService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合同附件控制器（用印附件 / 双盖附件，支持多文件）。
 */
@Tag(name = "广告合同附件")
@RestController
@RequestMapping("/api/ad/contract/{contractId}/attachment")
public class AdContractAttachmentController {

    @Resource
    private AdContractAttachmentService contractAttachmentService;

    @PostMapping
    @CsPermission(PermissionConstants.AD_CONTRACT_UPDATE)
    @Operation(summary = "上传合同附件(type:10用印附件/20双盖附件)")
    public AdContractAttachment upload(@PathVariable String contractId,
                                       @RequestParam int type,
                                       @RequestParam String tempFileId,
                                       @RequestParam(required = false) String fileName) {
        return contractAttachmentService.upload(contractId, type, tempFileId, fileName,
                SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping
    @CsPermission(PermissionConstants.AD_CONTRACT_READ)
    @Operation(summary = "获取合同附件列表")
    public List<AdContractAttachment> list(@PathVariable String contractId) {
        return contractAttachmentService.listByContractId(contractId);
    }

    @DeleteMapping("/{id}")
    @CsPermission(PermissionConstants.AD_CONTRACT_UPDATE)
    @Operation(summary = "删除合同附件")
    public void delete(@PathVariable String contractId, @PathVariable String id) {
        contractAttachmentService.delete(id);
    }
}
