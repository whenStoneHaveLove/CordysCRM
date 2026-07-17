package cn.cordys.crm.ad.report.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.report.service.AdReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 广告报表控制器（M6，V3.1 广告报表）。
 *
 * <p>纯只读聚合查询端点。无写操作，不标注 {@code @OperationLog}。
 * 接口级权限由 {@link CsPermission} 依据 {@link PermissionConstants#AD_REPORT_READ} 校验。</p>
 */
@Tag(name = "广告报表")
@RestController
@RequestMapping("/api/ad/report")
public class AdReportController {

    @Resource
    private AdReportService adReportService;

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @GetMapping("/order-summary")
    @CsPermission(PermissionConstants.AD_REPORT_READ)
    @Operation(summary = "订单汇总：按状态统计数量和金额")
    public List<Map<String, Object>> orderSummary() {
        return adReportService.orderSummary(orgId());
    }

    @GetMapping("/payment-summary")
    @CsPermission(PermissionConstants.AD_REPORT_READ)
    @Operation(summary = "付款汇总：已付/未付/逾期金额")
    public Map<String, BigDecimal> paymentSummary() {
        return adReportService.paymentSummary(orgId());
    }

    @GetMapping("/monthly-trend")
    @CsPermission(PermissionConstants.AD_REPORT_READ)
    @Operation(summary = "月度趋势：指定年份每月订单数+金额")
    public List<Map<String, Object>> monthlyTrend(@RequestParam("year") int year) {
        return adReportService.monthlyTrend(orgId(), year);
    }

    @GetMapping("/{code}")
    @CsPermission(PermissionConstants.AD_REPORT_READ)
    @Operation(summary = "配置化报表（order-execution/receivable-payable/collection/media/contract-missing/seal）")
    public Object report(@PathVariable("code") String code) {
        return adReportService.reportByCode(code, orgId());
    }
}
