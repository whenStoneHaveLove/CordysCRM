package cn.cordys.crm.ad.resource.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.resource.domain.AdResource;
import cn.cordys.crm.ad.resource.dto.request.AdResourcePageRequest;
import cn.cordys.crm.ad.resource.dto.request.AdResourceSaveRequest;
import cn.cordys.crm.ad.resource.dto.response.AdResourceDetailResponse;
import cn.cordys.crm.ad.resource.dto.response.AdResourceListResponse;
import cn.cordys.crm.ad.resource.service.AdResourceService;
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

import java.util.List;

/**
 * 广告资源控制器（M6，V3.1 广告资源管理）。
 *
 * <p>接口级权限由 {@link CsPermission} 依据 {@link PermissionConstants} 的 AD_RESOURCE_* 码校验。</p>
 */
@Tag(name = "广告资源")
@RestController
@RequestMapping("/api/ad/resource")
public class AdResourceController {

    @Resource
    private AdResourceService adResourceService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @CsPermission(PermissionConstants.AD_RESOURCE_CREATE)
    @Operation(summary = "新建广告资源")
    public AdResource create(@RequestBody AdResourceSaveRequest request) {
        return adResourceService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_RESOURCE_UPDATE)
    @Operation(summary = "编辑广告资源")
    public AdResource update(@RequestBody AdResourceSaveRequest request) {
        return adResourceService.update(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_RESOURCE_READ)
    @Operation(summary = "广告资源详情")
    public AdResourceDetailResponse detail(@PathVariable("id") String id) {
        return adResourceService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_RESOURCE_READ)
    @Operation(summary = "广告资源分页（多筛选+关键字+主体隔离+排序）")
    public PagerWithOption<List<AdResourceListResponse>> page(@RequestBody AdResourcePageRequest request) {
        return adResourceService.page(request, userId(), orgId());
    }

    @DeleteMapping("/{id}")
    @CsPermission(PermissionConstants.AD_RESOURCE_DELETE)
    @Operation(summary = "删除广告资源")
    public void delete(@PathVariable("id") String id) {
        adResourceService.delete(id, userId(), orgId());
    }
}
