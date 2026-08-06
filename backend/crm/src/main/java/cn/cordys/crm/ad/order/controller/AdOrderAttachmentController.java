package cn.cordys.crm.ad.order.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.order.domain.AdOrderAttachment;
import cn.cordys.crm.ad.order.service.AdOrderAttachmentService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "广告订单附件")
@RestController
@RequestMapping("/api/ad/order/{orderId}/attachment")
public class AdOrderAttachmentController {

    @Resource
    private AdOrderAttachmentService adOrderAttachmentService;

    @PostMapping
    @CsPermission(PermissionConstants.AD_ORDER_CREATE)
    @Operation(summary = "上传附件（type:10盖章排期/20邮件截图/30合同/40过程/50改单）")
    public AdOrderAttachment upload(@PathVariable String orderId,
                                     @RequestParam int type,
                                     @RequestParam MultipartFile file) {
        return adOrderAttachmentService.upload(orderId, type, file, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping
    @CsPermission(PermissionConstants.AD_ORDER_READ)
    @Operation(summary = "获取订单附件列表")
    public List<AdOrderAttachment> list(@PathVariable String orderId) {
        return adOrderAttachmentService.listByOrderId(orderId);
    }

    @DeleteMapping("/{id}")
    @CsPermission(PermissionConstants.AD_ORDER_CREATE)
    @Operation(summary = "删除附件")
    public void delete(@PathVariable String orderId, @PathVariable String id) {
        adOrderAttachmentService.delete(id);
    }
}
