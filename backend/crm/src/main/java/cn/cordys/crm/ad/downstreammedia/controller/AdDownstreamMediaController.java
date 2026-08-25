package cn.cordys.crm.ad.downstreammedia.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMedia;
import cn.cordys.crm.ad.downstreammedia.dto.request.AdDownstreamMediaPageRequest;
import cn.cordys.crm.ad.downstreammedia.dto.request.AdDownstreamMediaSaveRequest;
import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaDetailResponse;
import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaListResponse;
import cn.cordys.crm.ad.downstreammedia.service.AdDownstreamMediaService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "下游客户")
@RestController
@RequestMapping("/api/ad/downstream-media")
public class AdDownstreamMediaController {

    @Resource
    private AdDownstreamMediaService mediaService;

    private String userId() { return SessionUtils.getUserId(); }
    private String orgId() { return OrganizationContext.getOrganizationId(); }

    @PostMapping
    @CsPermission(PermissionConstants.AD_DOWNSTREAM_MEDIA_CREATE)
    @Operation(summary = "新建下游客户")
    public AdDownstreamMedia create(@RequestBody AdDownstreamMediaSaveRequest request) {
        return mediaService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_DOWNSTREAM_MEDIA_UPDATE)
    @Operation(summary = "编辑下游客户")
    public AdDownstreamMedia update(@RequestBody AdDownstreamMediaSaveRequest request) {
        return mediaService.update(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_DOWNSTREAM_MEDIA_READ)
    @Operation(summary = "下游客户详情")
    public AdDownstreamMediaDetailResponse detail(@PathVariable("id") String id) {
        return mediaService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_DOWNSTREAM_MEDIA_READ)
    @Operation(summary = "下游客户分页")
    public PagerWithOption<List<AdDownstreamMediaListResponse>> page(@RequestBody AdDownstreamMediaPageRequest request) {
        return mediaService.page(request, userId(), orgId());
    }

    @DeleteMapping("/{id}")
    @CsPermission(PermissionConstants.AD_DOWNSTREAM_MEDIA_DELETE)
    @Operation(summary = "删除下游客户")
    public void delete(@PathVariable("id") String id) {
        mediaService.delete(id, userId(), orgId());
    }
}
