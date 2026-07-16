package cn.cordys.crm.ad.contract.controller;

import cn.cordys.common.pager.PagerWithOption;
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
 * 广告合同控制器（M5 T-40/T-41，V3.1 §13.2）。
 *
 * <p>所有写操作均在 {@link AdContractService} 中标注 {@code @OperationLog}（写入 ad_operation_log）。
 * 响应由框架 {@code ResultResponseBodyAdvice} 统一包裹（包路径 cn.cordys）。</p>
 *
 * <p>鉴权：由 {@link AdContractService} 依据角色做角色级守卫（同 M2/M3 风格），未接入 ad 专用权限常量（M6 收口）。</p>
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
    @Operation(summary = "新建合同（默认生效，未申请用印）")
    public AdContract create(@RequestBody AdContractSaveRequest request) {
        return adContractService.create(request, userId(), orgId());
    }

    @PutMapping
    @Operation(summary = "编辑合同（维持编号/状态/用印状态等受控字段）")
    public AdContract update(@RequestBody AdContractSaveRequest request) {
        return adContractService.update(request, userId(), orgId());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除合同")
    public void delete(@PathVariable("id") String id) {
        adContractService.delete(id, userId(), orgId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "合同详情（主信息+关联名称+状态标签+用印历史）")
    public AdContractDetailResponse detail(@PathVariable("id") String id) {
        return adContractService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @Operation(summary = "合同分页（多筛选+关键字+主体隔离+排序）")
    public PagerWithOption<List<AdContractListResponse>> page(@RequestBody AdContractPageRequest request) {
        return adContractService.page(request, userId(), orgId());
    }
}
