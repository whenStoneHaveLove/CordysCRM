package cn.cordys.crm.ad.dashboard.service;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.permission.PermissionUtils;
import cn.cordys.crm.ad.report.dto.response.AdDashboardSummaryResponse;
import cn.cordys.crm.ad.report.dto.response.AdWorkbenchTodoItem;
import cn.cordys.crm.ad.report.dto.response.AdWorkbenchTodoResponse;
import cn.cordys.crm.ad.report.mapper.ExtAdReportMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 广告工作台服务（V3.1 §8.5，B-5）。
 *
 * <p>分角色聚合待办：
 * 媒介（待提交订单/待执行/改单执行/待提交付款单/待用印/待提交归档/缺合同）、
 * 老板（待审订单/改单/用印/归档/收款单/付款单）、
 * 财务（待提交收款单）。计数来自 ad 表聚合，非 100% 精确，
 * 命中主状态/子状态即可（§A.5 B-5 允许合理近似）。</p>
 */
@Service
public class AdDashboardService {

    @Resource
    private ExtAdReportMapper extAdReportMapper;

    /** 工作台首页数据概览（5 个核心指标 + 近 12 个月趋势）。 */
    public AdDashboardSummaryResponse dashboardSummary(String orgId) {
        Map<String, BigDecimal> summary = extAdReportMapper.dashboardSummary(orgId);

        AdDashboardSummaryResponse resp = new AdDashboardSummaryResponse();
        resp.setActiveOrderCount(toLong(summary.get("activeOrderCount")));
        resp.setTotalReceivable(summary.getOrDefault("totalReceivable", BigDecimal.ZERO));
        resp.setTotalMediaPayable(summary.getOrDefault("totalMediaPayable", BigDecimal.ZERO));
        resp.setPendingReceivable(summary.getOrDefault("pendingReceivable", BigDecimal.ZERO));
        resp.setPendingPayable(summary.getOrDefault("pendingPayable", BigDecimal.ZERO));
        resp.setMonthlyTrend(recentMonthlyTrend(orgId));
        return resp;
    }

    /**
     * 近 12 个月订单趋势（含当月，缺数据的月份补 0）。
     * 复用 {@link ExtAdReportMapper#monthlyTrend} 逐年查询后按月份填充。
     */
    private List<Map<String, Object>> recentMonthlyTrend(String orgId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        // 生成近 12 个月（含当月）的 key
        Map<String, Map<String, Object>> monthMap = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            String key = now.minusMonths(i).format(fmt);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", key);
            m.put("orderCount", 0L);
            m.put("amount", BigDecimal.ZERO);
            monthMap.put(key, m);
        }

        // 涉及两个年份（跨年时）：只查当前年和上一年
        int thisYear = now.getYear();
        Set<Integer> years = new LinkedHashSet<>();
        years.add(thisYear);
        years.add(now.minusMonths(11).getYear());
        for (Integer y : years) {
            List<Map<String, Object>> rows = extAdReportMapper.monthlyTrend(orgId, y);
            if (rows == null) {
                continue;
            }
            for (Map<String, Object> row : rows) {
                String month = row.get("month") == null ? null : row.get("month").toString();
                if (month == null || !monthMap.containsKey(month)) {
                    continue;
                }
                Map<String, Object> target = monthMap.get(month);
                target.put("orderCount", toLong(row.get("orderCount")));
                target.put("amount", row.get("amount") == null ? BigDecimal.ZERO : row.get("amount"));
            }
        }
        return new ArrayList<>(monthMap.values());
    }

    private long toLong(Object v) {
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(v.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    /** 分角色工作台待办聚合。 */
    public AdWorkbenchTodoResponse workbenchTodo(String orgId) {
        Map<String, Object> counts = extAdReportMapper.workbenchCounts(orgId);
        Set<String> roles = resolveAdRoles();

        Map<String, List<AdWorkbenchTodoItem>> todos = new LinkedHashMap<>();
        if (roles.contains("MEDIA")) {
            todos.put("MEDIA", List.of(
                    item("pendingSubmit", "待提交订单", num(counts, "draftCount"), "/advertising/order-management/order?status=0"),
                    item("pendingExecute", "待执行订单", num(counts, "pendingExecute"), "/advertising/order-management/order?status=45"),
                    item("changeExecute", "改单待执行", num(counts, "changeExecute"), "/advertising/order-management/change?status=20"),
                    item("payoutDraft", "待提交付款单", num(counts, "payoutDraft"), "/advertising/order-management/payout?status=0"),
                    item("sealApply", "合同待用印", num(counts, "sealApply"), "/advertising/contract-management/contract?sealStatus=0"),
                    item("archiveSubmit", "合同待归档", num(counts, "archiveSubmit"), "/advertising/contract-management/contract?sealStatus=20"),
                    item("missingContract", "待补单笔合同", num(counts, "missingContract"), "/advertising/order-management/order?missingContract=1")
            ));
        }
        if (roles.contains("BOSS")) {
            todos.put("BOSS", List.of(
                    item("pendingApprove", "待审订单", num(counts, "pendingApproveOrder"), "/advertising/order-management/order?status=10"),
                    item("changeApprove", "待审改单", num(counts, "changePending"), "/advertising/order-management/change?status=10"),
                    item("sealApprove", "待审用印", num(counts, "sealApprove"), "/advertising/contract-management/seal?status=0"),
                    item("archiveApprove", "待审归档", num(counts, "archiveApprove"), "/advertising/contract-management/contract?sealStatus=40"),
                    item("receiptApprove", "待审收款单", num(counts, "receiptApprove"), "/advertising/order-management/receipt?status=10"),
                    item("payoutApprove", "待审付款单", num(counts, "payoutApprove"), "/advertising/order-management/payout?status=10")
            ));
        }
        if (roles.contains("FINANCE")) {
            todos.put("FINANCE", List.of(
                    item("receiptDraft", "待提交收款单", num(counts, "receiptDraft"), "/advertising/order-management/receipt?status=0")
            ));
        }

        AdWorkbenchTodoResponse resp = new AdWorkbenchTodoResponse();
        resp.setRoles(new ArrayList<>(roles));
        resp.setTodos(todos);
        return resp;
    }

    private AdWorkbenchTodoItem item(String key, String label, long count, String link) {
        AdWorkbenchTodoItem it = new AdWorkbenchTodoItem();
        it.setKey(key);
        it.setLabel(label);
        it.setCount(count);
        it.setLink(link);
        return it;
    }

    private long num(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(v.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 解析当前用户可见的广告工作台板块（MEDIA/BOSS/FINANCE）。
     * 通过权限点判断：AD_WORKBENCH:MEDIA / AD_WORKBENCH:BOSS / AD_WORKBENCH:FINANCE。
     * admin 用户拥有所有权限（PermissionUtils 内部对 admin 恒返回 true）。
     */
    private Set<String> resolveAdRoles() {
        Set<String> roles = new LinkedHashSet<>();
        if (PermissionUtils.hasPermission(PermissionConstants.AD_WORKBENCH_MEDIA)) {
            roles.add("MEDIA");
        }
        if (PermissionUtils.hasPermission(PermissionConstants.AD_WORKBENCH_BOSS)) {
            roles.add("BOSS");
        }
        if (PermissionUtils.hasPermission(PermissionConstants.AD_WORKBENCH_FINANCE)) {
            roles.add("FINANCE");
        }
        return roles;
    }
}
