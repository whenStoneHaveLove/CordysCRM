package cn.cordys.crm.ad.dashboard.service;

import cn.cordys.common.dto.RoleDataScopeDTO;
import cn.cordys.crm.ad.report.dto.response.AdWorkbenchTodoItem;
import cn.cordys.crm.ad.report.dto.response.AdWorkbenchTodoResponse;
import cn.cordys.crm.ad.report.mapper.ExtAdReportMapper;
import cn.cordys.security.SessionUser;
import cn.cordys.security.SessionUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 广告工作台服务（V3.1 §8.5，B-5）。
 *
 * <p>分角色聚合待办：媒介（待提交/改单草稿/用印申请/缺合同）、老板（待审订单/改单/用印/强制归档）、
 * 财务（待确认预收/媒体预付/开票/收款/媒体尾款/红冲）。计数来自 ad 表聚合，非 100% 精确，
 * 命中主状态/子状态即可（§A.5 B-5 允许合理近似）。</p>
 */
@Service
public class AdDashboardService {

    @Resource
    private ExtAdReportMapper extAdReportMapper;

    /** 分角色工作台待办聚合。 */
    public AdWorkbenchTodoResponse workbenchTodo(String orgId) {
        Map<String, Object> counts = extAdReportMapper.workbenchCounts(orgId);
        Set<String> roles = resolveAdRoles();

        Map<String, List<AdWorkbenchTodoItem>> todos = new LinkedHashMap<>();
        if (roles.contains("MEDIA")) {
            todos.put("MEDIA", List.of(
                    item("pendingSubmit", "待提交审核", num(counts, "draftCount"), "/advertising/order"),
                    item("changeDraft", "改单待审", num(counts, "changePending"), "/advertising/order-change"),
                    item("sealApply", "用印申请", num(counts, "sealPending"), "/advertising/seal"),
                    item("missingContract", "缺合同提醒", num(counts, "missingContract"), "/advertising/order")
            ));
        }
        if (roles.contains("BOSS")) {
            todos.put("BOSS", List.of(
                    item("pendingApprove", "待我审批(订单)", num(counts, "pendingApproveOrder"), "/advertising/order"),
                    item("changeApprove", "待审改单", num(counts, "changePending"), "/advertising/order-change"),
                    item("sealApprove", "待审用印", num(counts, "sealPending"), "/advertising/seal"),
                    item("forceArchive", "强制归档", num(counts, "forceArchive"), "/advertising/order")
            ));
        }
        if (roles.contains("FINANCE")) {
            todos.put("FINANCE", List.of(
                    item("prepayConfirm", "待确认预收", num(counts, "prepayConfirm"), "/advertising/payment"),
                    item("mediaPrepay", "待付媒体预付款", num(counts, "mediaPrepay"), "/advertising/payment"),
                    item("invoice", "待开票", num(counts, "invoice"), "/advertising/payment"),
                    item("receive", "待收款", num(counts, "receive"), "/advertising/payment"),
                    item("mediaPostpay", "待付媒体尾款", num(counts, "mediaPostpay"), "/advertising/payment"),
                    item("redInvoice", "红冲待办", num(counts, "redInvoice"), "/advertising/payment")
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
     * 解析当前用户具备的广告角色分组（MEDIA/BOSS/FINANCE）。
     * 管理员视为全部角色（可见所有待办卡片）；否则按角色名模糊匹配。
     */
    private Set<String> resolveAdRoles() {
        SessionUser user = SessionUtils.getUser();
        Set<String> roles = new LinkedHashSet<>();
        if (user == null) {
            return roles;
        }
        List<String> roleNames = user.getRoles() == null ? Collections.<String>emptyList()
                : user.getRoles().stream()
                .map(RoleDataScopeDTO::getName)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        boolean isAdmin = roleNames.stream()
                .anyMatch(n -> n.contains("admin") || n.contains("超级") || n.contains("管理员"));
        if (isAdmin) {
            roles.add("MEDIA");
            roles.add("BOSS");
            roles.add("FINANCE");
            return roles;
        }
        if (roleNames.stream().anyMatch(n -> n.contains("媒体") || n.contains("media") || n.contains("运营"))) {
            roles.add("MEDIA");
        }
        if (roleNames.stream().anyMatch(n -> n.contains("财务") || n.contains("finance"))) {
            roles.add("FINANCE");
        }
        if (roleNames.stream().anyMatch(n -> n.contains("老板") || n.contains("boss"))) {
            roles.add("BOSS");
        }
        return roles;
    }
}
