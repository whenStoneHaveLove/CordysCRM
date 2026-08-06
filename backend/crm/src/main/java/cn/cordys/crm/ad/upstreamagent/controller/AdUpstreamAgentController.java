package cn.cordys.crm.ad.upstreamagent.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.upstreamagent.domain.AdUpstreamAgent;
import cn.cordys.crm.ad.upstreamagent.dto.request.AdUpstreamAgentPageRequest;
import cn.cordys.crm.ad.upstreamagent.dto.request.AdUpstreamAgentSaveRequest;
import cn.cordys.crm.ad.upstreamagent.dto.response.AdUpstreamAgentDetailResponse;
import cn.cordys.crm.ad.upstreamagent.dto.response.AdUpstreamAgentListResponse;
import cn.cordys.crm.ad.upstreamagent.service.AdUpstreamAgentService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "上游代理")
@RestController
@RequestMapping("/api/ad/upstream-agent")
public class AdUpstreamAgentController {

    @Resource
    private AdUpstreamAgentService agentService;

    private String userId() { return SessionUtils.getUserId(); }
    private String orgId() { return OrganizationContext.getOrganizationId(); }

    @PostMapping
    @CsPermission(PermissionConstants.AD_UPSTREAM_AGENT_CREATE)
    @Operation(summary = "新建上游代理")
    public AdUpstreamAgent create(@RequestBody AdUpstreamAgentSaveRequest request) {
        return agentService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_UPSTREAM_AGENT_UPDATE)
    @Operation(summary = "编辑上游代理")
    public AdUpstreamAgent update(@RequestBody AdUpstreamAgentSaveRequest request) {
        return agentService.update(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_UPSTREAM_AGENT_READ)
    @Operation(summary = "上游代理详情")
    public AdUpstreamAgentDetailResponse detail(@PathVariable("id") String id) {
        return agentService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_UPSTREAM_AGENT_READ)
    @Operation(summary = "上游代理分页")
    public PagerWithOption<List<AdUpstreamAgentListResponse>> page(@RequestBody AdUpstreamAgentPageRequest request) {
        return agentService.page(request, userId(), orgId());
    }

    @DeleteMapping("/{id}")
    @CsPermission(PermissionConstants.AD_UPSTREAM_AGENT_DELETE)
    @Operation(summary = "删除上游代理")
    public void delete(@PathVariable("id") String id) {
        agentService.delete(id, userId(), orgId());
    }
}
