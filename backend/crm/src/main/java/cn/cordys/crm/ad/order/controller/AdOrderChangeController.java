package cn.cordys.crm.ad.order.controller;

import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.order.domain.AdOrderChange;
import cn.cordys.crm.ad.order.dto.request.AdOrderChangeApproveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderChangePageRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderChangeSaveRequest;
import cn.cordys.crm.ad.order.dto.response.AdOrderChangeDetailResponse;
import cn.cordys.crm.ad.order.dto.response.AdOrderChangeListResponse;
import cn.cordys.crm.ad.order.service.AdOrderChangeService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告改单控制器（M3 T-20/T-21，V3.1 §13.2）。
 *
 * <p>所有写操作均在 {@link AdOrderChangeService} 中标注 {@code @OperationLog}（写入 ad_operation_log）。
 * 响应由框架 {@code ResultResponseBodyAdvice} 统一包裹。端点统一位于 {@code /api/ad/order-change} 之下。</p>
 */
@Tag(name = "广告改单")
@RestController
@RequestMapping("/api/ad/order-change")
public class AdOrderChangeController {

    @Resource
    private AdOrderChangeService adOrderChangeService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @Operation(summary = "新建改单申请（草稿，禁改 order_type L-24）")
    public AdOrderChange create(@RequestBody AdOrderChangeSaveRequest request) {
        return adOrderChangeService.create(request, userId(), orgId());
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交改单：锁定父单→变更审核中(60)；审批关时直达审批通过(L-14)")
    public AdOrderChange submit(@PathVariable("id") String id) {
        return adOrderChangeService.submit(id, userId(), orgId());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "老板审批通过（改单→已审批，父单仍锁定）")
    public AdOrderChange approve(@PathVariable("id") String id,
                                 @RequestBody(required = false) AdOrderChangeApproveRequest request) {
        return adOrderChangeService.approve(id, request, userId(), orgId());
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "老板驳回（改单→已驳回，父单恢复执行中50，保留数据 L-21）")
    public AdOrderChange reject(@PathVariable("id") String id,
                                @RequestBody(required = false) AdOrderChangeApproveRequest request) {
        return adOrderChangeService.reject(id, request, userId(), orgId());
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "执行改单：应用快照+金额重算+L-04资金侧，父单恢复执行中(50)")
    public AdOrderChange execute(@PathVariable("id") String id) {
        return adOrderChangeService.execute(id, userId(), orgId());
    }

    @PostMapping("/page")
    @Operation(summary = "改单分页（筛选+关键字+主体隔离+排序）")
    public PagerWithOption<List<AdOrderChangeListResponse>> page(@RequestBody AdOrderChangePageRequest request) {
        return adOrderChangeService.page(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "改单详情")
    public AdOrderChangeDetailResponse detail(@PathVariable("id") String id) {
        return adOrderChangeService.detail(id, userId(), orgId());
    }
}
