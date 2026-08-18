package cn.cordys.crm.ad.approval.service;

import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.service.BaseService;
import cn.cordys.crm.ad.common.constants.OrderStateMachine;
import com.github.pagehelper.Page;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 广告审批中心服务 - 聚合六类待审批项。
 *
 * <p>六类：订单(order) / 改单(change) / 用印(seal) / 归档(archive) / 收款单(receipt) / 付款单(payout)。</p>
 */
@Service
public class AdApprovalService {

    @Resource
    private SqlSession sqlSession;
    @Resource
    private BaseService baseService;

    private static final String NS = "cn.cordys.crm.ad.approval.mapper.ExtAdApprovalMapper";

    /** 改单表 ad_order_change.status 的「已提交(待审批)」值。 */
    private static final int CHANGE_PENDING = 10;
    /** 用印表 ad_seal_record.status 的「审批中」值。 */
    private static final int SEAL_PENDING = 0;
    /** 合同表 ad_contract.seal_status 的「归档审批中」值。 */
    private static final int ARCHIVE_PENDING = 40;
    /** 收款/付款表 status 的「待审核」值。 */
    private static final int RECEIPT_PAYOUT_PENDING = 10;

    /**
     * 待审批分页（按 type 过滤：order / change / seal / archive / receipt / payout，null 表示全部）。
     */
    public PagerWithOption<List<Map<String, Object>>> pending(int current, int pageSize, String type, String orgId) {
        List<Map<String, Object>> items = new ArrayList<>();

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

        if (type == null || "change".equals(type)) {
            List<Map<String, Object>> changes = sqlSession.selectList(NS + ".pendingChanges",
                    Map.of("orgId", orgId, "status", CHANGE_PENDING));
            for (Map<String, Object> c : changes) {
                c.put("type", "change");
                c.put("typeLabel", "改单审批");
                c.put("businessId", c.get("id"));
                items.add(c);
            }
        }

        if (type == null || "seal".equals(type)) {
            List<Map<String, Object>> seals = sqlSession.selectList(NS + ".pendingSeals",
                    Map.of("orgId", orgId, "status", SEAL_PENDING));
            for (Map<String, Object> s : seals) {
                s.put("type", "seal");
                s.put("typeLabel", "用印审批");
                s.put("businessId", s.get("id"));
                items.add(s);
            }
        }

        if (type == null || "archive".equals(type)) {
            List<Map<String, Object>> archives = sqlSession.selectList(NS + ".pendingArchives",
                    Map.of("orgId", orgId, "sealStatus", ARCHIVE_PENDING));
            for (Map<String, Object> a : archives) {
                a.put("type", "archive");
                a.put("typeLabel", "归档审批");
                a.put("businessId", a.get("id"));
                items.add(a);
            }
        }

        if (type == null || "receipt".equals(type)) {
            List<Map<String, Object>> receipts = sqlSession.selectList(NS + ".pendingReceipts",
                    Map.of("orgId", orgId, "status", RECEIPT_PAYOUT_PENDING));
            for (Map<String, Object> r : receipts) {
                r.put("type", "receipt");
                r.put("typeLabel", "收款审批");
                r.put("businessId", r.get("id"));
                items.add(r);
            }
        }

        if (type == null || "payout".equals(type)) {
            List<Map<String, Object>> payouts = sqlSession.selectList(NS + ".pendingPayouts",
                    Map.of("orgId", orgId, "status", RECEIPT_PAYOUT_PENDING));
            for (Map<String, Object> p : payouts) {
                p.put("type", "payout");
                p.put("typeLabel", "付款审批");
                p.put("businessId", p.get("id"));
                items.add(p);
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
        List<Map<String, Object>> pageItems = new ArrayList<>(items.subList(from, to));

        // 申请人姓名翻译
        Set<String> userIds = new HashSet<>();
        for (Map<String, Object> item : pageItems) {
            Object aid = item.get("applicantId");
            if (aid instanceof String s && !s.isEmpty()) {
                userIds.add(s);
            }
        }
        Map<String, String> nameMap = userIds.isEmpty() ? new HashMap<>() : baseService.getUserNameMap(userIds);
        for (Map<String, Object> item : pageItems) {
            String aid = (String) item.get("applicantId");
            if (aid != null) {
                item.put("applicantName", nameMap.getOrDefault(aid, aid));
            }
        }

        Page<Map<String, Object>> page = new Page<>(current, pageSize);
        page.setTotal(total);
        return PageUtils.setPageInfoWithOption(page, pageItems, null);
    }

    private long toLong(Object v) {
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s && !s.isEmpty()) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }
}
