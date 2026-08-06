package cn.cordys.crm.ad.approval.service;

import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.service.BaseService;
import cn.cordys.crm.ad.common.constants.OrderStateMachine;
import com.github.pagehelper.Page;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 广告审批中心服务 - 直接查 ad_* 表聚合待审批项。
 */
@Service
public class AdApprovalService {

    @Resource
    private SqlSession sqlSession;
    @Resource
    private BaseService baseService;

    private static final String NS = "cn.cordys.crm.ad.approval.mapper.ExtAdApprovalMapper";

    /**
     * 待审批分页（按 type 过滤：order / change / seal）。
     */
    public PagerWithOption<List<Map<String, Object>>> pending(int current, int pageSize, String type, String orgId) {
        List<Map<String, Object>> items = new ArrayList<>();

        // 订单待审批：status = 10 (待老板审核)
        if (type == null || "order".equals(type)) {
            List<Map<String, Object>> orders = sqlSession.selectList(NS + ".pendingOrders",
                    Map.of("orgId", orgId, "status", OrderStateMachine.PENDING_BOSS_APPROVAL));
            for (Map<String, Object> o : orders) {
                o.put("type", "order");
                o.put("typeLabel", "订单审批");
                o.put("businessId", o.get("id"));
                items.add(o);
            }
        }

        // 改单待审批：status = 0
        if (type == null || "change".equals(type)) {
            List<Map<String, Object>> changes = sqlSession.selectList(NS + ".pendingChanges",
                    Map.of("orgId", orgId));
            for (Map<String, Object> c : changes) {
                c.put("type", "change");
                c.put("typeLabel", "改单审批");
                c.put("businessId", c.get("id"));
                items.add(c);
            }
        }

        // 用印待审批：status = 0
        if (type == null || "seal".equals(type)) {
            List<Map<String, Object>> seals = sqlSession.selectList(NS + ".pendingSeals",
                    Map.of("orgId", orgId));
            for (Map<String, Object> s : seals) {
                s.put("type", "seal");
                s.put("typeLabel", "用印审批");
                s.put("businessId", s.get("id"));
                items.add(s);
            }
        }

        // 按 createTime 降序
        items.sort((a, b) -> {
            Long ta = toLong(a.get("createTime"));
            Long tb = toLong(b.get("createTime"));
            return tb.compareTo(ta);
        });

        long total = items.size();

        // 内存分页
        int from = (current - 1) * pageSize;
        if (from >= items.size()) {
            Page<Map<String, Object>> emptyPage = new Page<>(current, pageSize);
            emptyPage.setTotal(total);
            return PageUtils.setPageInfoWithOption(emptyPage, Collections.emptyList(), null);
        }
        int to = Math.min(from + pageSize, items.size());
        List<Map<String, Object>> pageItems = items.subList(from, to);

        // 申请人姓名翻译
        Set<String> userIds = new HashSet<>();
        for (Map<String, Object> item : pageItems) {
            Object aid = item.get("applicantId");
            if (aid instanceof String s && !s.isEmpty()) userIds.add(s);
        }
        Map<String, String> nameMap = userIds.isEmpty() ? new HashMap<>() : baseService.getUserNameMap(userIds);
        for (Map<String, Object> item : pageItems) {
            String aid = (String) item.get("applicantId");
            if (aid != null) item.put("applicantName", nameMap.getOrDefault(aid, aid));
        }

        Page<Map<String, Object>> page = new Page<>(current, pageSize);
        page.setTotal(total);
        return PageUtils.setPageInfoWithOption(page, pageItems, null);
    }

    private long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        return 0L;
    }
}
