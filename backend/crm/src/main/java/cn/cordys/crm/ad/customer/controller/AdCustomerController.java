package cn.cordys.crm.ad.customer.controller;

import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.customer.domain.AdCustomer;
import cn.cordys.crm.ad.customer.dto.request.AdCustomerPageRequest;
import cn.cordys.crm.ad.customer.dto.request.AdCustomerSaveRequest;
import cn.cordys.crm.ad.customer.dto.response.AdCustomerDetailResponse;
import cn.cordys.crm.ad.customer.dto.response.AdCustomerListResponse;
import cn.cordys.crm.ad.customer.service.AdCustomerService;
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
 * 广告客户控制器（M6，V3.1 广告客户管理）。
 */
@Tag(name = "广告客户")
@RestController
@RequestMapping("/api/ad/customer")
public class AdCustomerController {

    @Resource
    private AdCustomerService adCustomerService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @Operation(summary = "新建广告客户")
    public AdCustomer create(@RequestBody AdCustomerSaveRequest request) {
        return adCustomerService.create(request, userId(), orgId());
    }

    @PutMapping
    @Operation(summary = "编辑广告客户")
    public AdCustomer update(@RequestBody AdCustomerSaveRequest request) {
        return adCustomerService.update(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "广告客户详情")
    public AdCustomerDetailResponse detail(@PathVariable("id") String id) {
        return adCustomerService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @Operation(summary = "广告客户分页（多筛选+关键字+排序）")
    public PagerWithOption<List<AdCustomerListResponse>> page(@RequestBody AdCustomerPageRequest request) {
        return adCustomerService.page(request, userId(), orgId());
    }
}
