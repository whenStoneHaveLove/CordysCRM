package cn.cordys.crm.ad.support.controller;

import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.support.domain.AdSupportTicket;
import cn.cordys.crm.ad.support.dto.request.AdSupportTicketPageRequest;
import cn.cordys.crm.ad.support.dto.request.AdSupportTicketSaveRequest;
import cn.cordys.crm.ad.support.dto.response.AdSupportTicketDetailResponse;
import cn.cordys.crm.ad.support.dto.response.AdSupportTicketListResponse;
import cn.cordys.crm.ad.support.service.AdSupportTicketService;
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
 * 广告支持工单控制器（M6，V3.1 广告支持工单管理）。
 */
@Tag(name = "广告支持工单")
@RestController
@RequestMapping("/api/ad/support")
public class AdSupportTicketController {

    @Resource
    private AdSupportTicketService adSupportTicketService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @Operation(summary = "新建支持工单（自动生成工单编号）")
    public AdSupportTicket create(@RequestBody AdSupportTicketSaveRequest request) {
        return adSupportTicketService.create(request, userId(), orgId());
    }

    @PutMapping
    @Operation(summary = "编辑工单（指派/解决/状态变更）")
    public AdSupportTicket update(@RequestBody AdSupportTicketSaveRequest request) {
        return adSupportTicketService.update(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "工单详情")
    public AdSupportTicketDetailResponse detail(@PathVariable("id") String id) {
        return adSupportTicketService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @Operation(summary = "工单分页（多筛选+关键字+排序）")
    public PagerWithOption<List<AdSupportTicketListResponse>> page(@RequestBody AdSupportTicketPageRequest request) {
        return adSupportTicketService.page(request, userId(), orgId());
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "关闭工单")
    public AdSupportTicket close(@PathVariable("id") String id) {
        return adSupportTicketService.close(id, userId(), orgId());
    }
}
