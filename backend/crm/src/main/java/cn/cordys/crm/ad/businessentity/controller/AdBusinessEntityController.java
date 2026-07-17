package cn.cordys.crm.ad.businessentity.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.businessentity.dto.request.AdBusinessEntityPageRequest;
import cn.cordys.crm.ad.businessentity.dto.request.AdBusinessEntitySaveRequest;
import cn.cordys.crm.ad.businessentity.dto.response.AdBusinessEntityDetailResponse;
import cn.cordys.crm.ad.businessentity.dto.response.AdBusinessEntityListResponse;
import cn.cordys.crm.ad.businessentity.service.AdBusinessEntityService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告业务主体控制器（V3.1 §4.1，B-3 后端补齐）。
 *
 * <p>所有写操作在 {@link AdBusinessEntityService} 中标注 {@code @OperationLog}（写入 ad_operation_log）。
 * 接口级权限由 {@link CsPermission} 依据 {@link PermissionConstants} 的 AD_BUSINESS_ENTITY_* 码校验。</p>
 */
@Tag(name = "广告业务主体")
@RestController
@RequestMapping("/api/ad/business-entity")
public class AdBusinessEntityController {

    @Resource
    private AdBusinessEntityService adBusinessEntityService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @CsPermission(PermissionConstants.AD_BUSINESS_ENTITY_CREATE)
    @Operation(summary = "新建业务主体")
    public AdBusinessEntity create(@RequestBody AdBusinessEntitySaveRequest request) {
        return adBusinessEntityService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_BUSINESS_ENTITY_UPDATE)
    @Operation(summary = "编辑业务主体")
    public AdBusinessEntity update(@RequestBody AdBusinessEntitySaveRequest request) {
        return adBusinessEntityService.update(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_BUSINESS_ENTITY_READ)
    @Operation(summary = "业务主体详情")
    public AdBusinessEntityDetailResponse detail(@PathVariable("id") String id) {
        return adBusinessEntityService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_BUSINESS_ENTITY_READ)
    @Operation(summary = "业务主体分页（关键字+状态筛选+排序）")
    public PagerWithOption<List<AdBusinessEntityListResponse>> page(@RequestBody AdBusinessEntityPageRequest request) {
        return adBusinessEntityService.page(request, userId(), orgId());
    }
}
