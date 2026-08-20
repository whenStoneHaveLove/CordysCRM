package cn.cordys.crm.ad.order.service;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.PermissionUtils;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.common.AdEntityPermissionProvider;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.common.constants.AdOrderChangeStatus;
import cn.cordys.crm.ad.common.constants.OrderStateMachine;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.domain.AdOrderChange;
import cn.cordys.crm.ad.order.domain.AdOrderLog;
import cn.cordys.crm.ad.order.dto.request.AdOrderChangeApproveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderChangePageRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderChangeSaveRequest;
import cn.cordys.crm.ad.order.dto.response.AdOrderChangeDetailResponse;
import cn.cordys.crm.ad.order.dto.response.AdOrderChangeListResponse;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderChangeMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 广告改单服务（M3 T-20/T-21，V3.1 §6/§7.2/§8）。
 *
 * <p>改单生命周期（提交/审批/执行/驳回）由本服务驱动，<b>不依赖</b> {@code cn.cordys.crm.approval} 模块。
 * 每个状态变更方法均标注 {@link OperationLog}（写入 ad_operation_log）。</p>
 *
 * <p>关键规则：
 * <ul>
 *   <li>§6.2/§8 状态联动：发起改单时父单切入改单审核中(60)并记录改单前状态(orderStatusBefore)，
 *       驳回/执行后父单恢复为改单前状态。</li>
 *   <li>L-24 改单禁止变更 {@code order_type}（框架↔单笔不可改），提交时校验。</li>
 *   <li>L-14 审批开关：{@code ad.order.approval.enabled}（默认 true）；关闭时提交直达审批通过(跳过管理组审批)。</li>
 *   <li>L-04 资金侧：执行时按新应收对比已收/已开票，置红冲标记(needs_red_invoice)，
 *       并记录应退款/待补收/待补开至订单操作日志（支付记录由 M4 负责）。</li>
 *   <li>L-21 驳回保留数据：驳回仅回退状态，不删除任何附件/快照。</li>
 *   <li>L-22 改单锁定期间允许上传过程附件(type=40)，由附件服务控制，本服务不阻断。</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdOrderChangeService {

    @Resource
    private BaseMapper<AdOrderChange> orderChangeMapper;
    @Resource
    private BaseMapper<AdOrder> adOrderMapper;
    @Resource
    private BaseMapper<AdOrderLog> orderLogMapper;
    @Resource
    private ExtAdOrderChangeMapper extAdOrderChangeMapper;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;
    @Resource
    private AdAmountCalculator amountCalculator;
    @Resource
    private cn.cordys.common.service.BaseService baseService;

    /** L-14 审批开关（默认开启）。 */
    @Value("${ad.order.approval.enabled:true}")
    private boolean approvalEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 改单执行动作（写入 ad_order_log.action）。 */
    private static final String ACTION_CHANGE_EXECUTED = "CHANGE_EXECUTED";

    /** 允许变更的订单业务字段白名单（camelCase）。 */
    private static final Set<String> MUTABLE_FIELDS = new HashSet<>(Arrays.asList(
            "totalAmount", "noRebateAmount", "rebateValue", "receiptPrepayRatio", "paymentPrepayRatio",
            "rebateMode", "receiptMethod", "receiptPrepayMode", "receiptAccountPeriodDays",
            "paymentMethod", "paymentPrepayMode", "paymentPostpayTrigger", "paymentPostpayDays",
            "deliveryStartDate", "deliveryEndDate", "receiptPrepayDeadline", "paymentPrepayDeadline",
            "orderName", "deliveryVolume", "remark", "extJson", "currency",
            "signingEntity", "industryCode", "upstreamAgentId", "agentOrderNo", "customerId"
    ));

    // ===================== 新建改单申请（草稿） =====================

    /**
     * 新建改单申请（草稿 0）。捕获变更前快照、校验字段白名单与 order_type 不可变(L-24)。
     */
    @OperationLog(module = "AD_ORDER_CHANGE", action = "CREATE", targetId = "")
    public AdOrderChange create(AdOrderChangeSaveRequest request, String userId, String orgId) {
        AdOrder order = requireOrder(request.getOrderId());
        List<String> fields = request.getChangeFields();
        if (fields == null || fields.isEmpty()) {
            throw new GenericException("变更字段清单不能为空");
        }
        if (request.getAfter() == null || request.getAfter().isEmpty()) {
            throw new GenericException("变更后快照不能为空");
        }
        validateChangeFields(fields);

        // 变更前快照：仅捕获白名单内字段当前值
        Map<String, Object> before = new LinkedHashMap<>();
        for (String f : fields) {
            before.put(f, readField(order, f));
        }
        String snapshotBefore = toJson(before);
        String snapshotAfter = toJson(request.getAfter());

        AdOrderChange change = new AdOrderChange();
        change.setId(IDGenerator.nextStr());
        change.setOrderId(order.getId());
        change.setOrganizationId(order.getOrganizationId());
        change.setChangeFields(toJson(fields));
        change.setReason(request.getReason() == null ? "" : request.getReason());
        change.setSnapshotBefore(snapshotBefore);
        change.setSnapshotAfter(snapshotAfter);
        change.setStatus(AdOrderChangeStatus.DRAFT.getCode());
        change.setCreateUser(userId);
        change.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        change.setCreateTime(now);
        change.setUpdateTime(now);
        orderChangeMapper.insert(change);
        return change;
    }

    // ===================== 提交流转 =====================

    /**
     * 提交改单：草稿(0) → 已提交(10) [L-14 关闭时直达审批通过(20)]。
     * 父单主状态切入改单审核中(60)，并记录改单前订单状态(orderStatusBefore)，驳回/执行后恢复。
     */
    @OperationLog(module = "AD_ORDER_CHANGE", action = "SUBMIT", targetId = "#id")
    public AdOrderChange submit(String id, String userId, String orgId) {
        AdOrderChange change = requireChange(id);
        if (change.getStatus() != AdOrderChangeStatus.DRAFT.getCode()) {
            throw new GenericException("仅草稿状态可提交");
        }
        AdOrder order = requireOrder(change.getOrderId());
        if (!OrderStateMachine.canApplyChange(order.getStatus())) {
            throw new GenericException("当前订单状态(" + order.getStatus() + ")不可发起改单，仅待执行/执行中/结算中可改单");
        }
        assertRole(PermissionConstants.AD_ORDER_CHANGE_SUBMIT);
        int fromOrder = order.getStatus();
        // 记录改单前订单状态，订单切入改单审核中(60)（停留态），改单结束后恢复
        change.setOrderStatusBefore(fromOrder);
        change.setUpdateUser(userId);
        change.setUpdateTime(System.currentTimeMillis());
        orderChangeMapper.update(change);

        // 父单主状态切入改单审核中(60)
        order.setStatus(OrderStateMachine.CHANGE_APPROVING);
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        adOrderMapper.update(order);

        int changeTo = approvalEnabled ? AdOrderChangeStatus.SUBMITTED.getCode()
                : AdOrderChangeStatus.APPROVED.getCode();
        change.setStatus(changeTo);
        change.setUpdateUser(userId);
        change.setUpdateTime(System.currentTimeMillis());
        orderChangeMapper.update(change);
        recordOrderLog(order, OrderStateMachine.TRIGGER_APPLY_CHANGE, fromOrder,
                OrderStateMachine.CHANGE_APPROVING, userId, orgId);
        return change;
    }

    /**
     * 管理组审批通过：已提交(10) → 审批通过(20)；订单主状态保持改单审核中(60)（停留态，待执行时恢复）。
     */
    @OperationLog(module = "AD_ORDER_CHANGE", action = "APPROVE", targetId = "#id")
    public AdOrderChange approve(String id, AdOrderChangeApproveRequest request, String userId, String orgId) {
        AdOrderChange change = requireChange(id);
        if (change.getStatus() != AdOrderChangeStatus.SUBMITTED.getCode()) {
            throw new GenericException("仅已提交的改单可被审批");
        }
        assertRole(PermissionConstants.AD_ORDER_CHANGE_APPROVE);
        AdOrder order = requireOrder(change.getOrderId());
        // 订单主状态保持改单审核中(60)，不改回原状态
        change.setStatus(AdOrderChangeStatus.APPROVED.getCode());
        change.setApproverId(userId);
        change.setApprovedAt(new Date());
        change.setApproveRemark(request == null ? null : request.getRemark());
        change.setUpdateUser(userId);
        change.setUpdateTime(System.currentTimeMillis());
        orderChangeMapper.update(change);
        Integer before = change.getOrderStatusBefore();
        int from = before != null ? before : order.getStatus();
        recordOrderLog(order, OrderStateMachine.TRIGGER_CHANGE_APPROVED,
                OrderStateMachine.CHANGE_APPROVING, from, userId, orgId);
        return change;
    }

    /**
     * 管理组驳回：已提交(10) → 已驳回(30)；父单恢复改单前状态（保留数据 L-21）。
     */
    @OperationLog(module = "AD_ORDER_CHANGE", action = "REJECT", targetId = "#id")
    public AdOrderChange reject(String id, AdOrderChangeApproveRequest request, String userId, String orgId) {
        AdOrderChange change = requireChange(id);
        if (change.getStatus() != AdOrderChangeStatus.SUBMITTED.getCode()) {
            throw new GenericException("仅已提交的改单可驳回");
        }
        assertRole(PermissionConstants.AD_ORDER_CHANGE_REJECT);
        AdOrder order = requireOrder(change.getOrderId());
        int fromOrder = order.getStatus();
        change.setStatus(AdOrderChangeStatus.REJECTED.getCode());
        change.setApproverId(userId);
        change.setApprovedAt(new Date());
        change.setApproveRemark(request == null ? null : request.getRemark());
        change.setUpdateUser(userId);
        change.setUpdateTime(System.currentTimeMillis());
        orderChangeMapper.update(change);
        // 改单驳回：订单从 60 恢复为改单前状态
        Integer before = change.getOrderStatusBefore();
        if (before != null) {
            order.setStatus(before);
            order.setUpdateUser(userId);
            order.setUpdateTime(System.currentTimeMillis());
            adOrderMapper.update(order);
            recordOrderLog(order, OrderStateMachine.TRIGGER_CHANGE_REJECTED, fromOrder,
                    before, userId, orgId);
        }
        return change;
    }

    /**
     * 执行改单：审批通过(20) → 已执行(40)。应用快照 + 金额重算 + L-04 资金侧；父单从改单审核中(60)恢复改单前状态。
     */
    @OperationLog(module = "AD_ORDER_CHANGE", action = "EXECUTE", targetId = "#id")
    public AdOrderChange execute(String id, String userId, String orgId) {
        AdOrderChange change = requireChange(id);
        if (change.getStatus() != AdOrderChangeStatus.APPROVED.getCode()) {
            throw new GenericException("仅审批通过的改单可执行");
        }
        assertRole(PermissionConstants.AD_ORDER_CHANGE_SUBMIT);
        AdOrder order = requireOrder(change.getOrderId());
        int fromOrder = order.getStatus();

        // 1) 应用变更后快照（仅白名单字段）
        Map<String, Object> after = parseJson(change.getSnapshotAfter());
        applyAfter(order, after);
        // 2) 金额重算（L-02/L-11/L-28）
        amountCalculator.computeAmounts(order);
        // 3) 父单从改单审核中(60)恢复为改单前状态
        Integer before = change.getOrderStatusBefore();
        int restoreTo = before != null ? before : fromOrder;
        order.setStatus(restoreTo);
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        adOrderMapper.update(order);
        // 4) 改单 → 已执行
        change.setStatus(AdOrderChangeStatus.EXECUTED.getCode());
        change.setUpdateUser(userId);
        change.setUpdateTime(System.currentTimeMillis());
        orderChangeMapper.update(change);
        recordOrderLog(order, ACTION_CHANGE_EXECUTED, fromOrder,
                restoreTo, userId, orgId);
        return change;
    }

    // ===================== 详情 / 分页 =====================

    /**
     * 改单详情：改单记录 + 关联订单号 + 状态标签。
     */
    public AdOrderChangeDetailResponse detail(String id, String userId, String orgId) {
        AdOrderChange change = requireChange(id);
        AdOrderChangeDetailResponse resp = new AdOrderChangeDetailResponse();
        resp.setChange(change);
        resp.setStatusLabel(AdOrderChangeStatus.labelOf(change.getStatus()));
        AdOrder order = adOrderMapper.selectByPrimaryKey(change.getOrderId());
        if (order != null) {
            resp.setOrderId(order.getId());
            resp.setOrderNo(order.getOrderNo());
            resp.setOrderName(order.getOrderName());
        }
        return resp;
    }

    /**
     * 改单分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     */
    public PagerWithOption<List<AdOrderChangeListResponse>> page(AdOrderChangePageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        Page<AdOrderChangeListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdOrderChangeListResponse> list = extAdOrderChangeMapper.pageList(request);

        // 审批人/创建人姓名翻译
        Set<String> userIds = new HashSet<>();
        for (AdOrderChangeListResponse r : list) {
            if (r.getApproverId() != null) userIds.add(r.getApproverId());
            if (r.getCreatorId() != null) userIds.add(r.getCreatorId());
        }
        Map<String, String> userNameMap = userIds.isEmpty() ? new HashMap<>() : baseService.getUserNameMap(userIds);

        for (AdOrderChangeListResponse r : list) {
            r.setStatusLabel(AdOrderChangeStatus.labelOf(r.getStatus()));
            if (r.getApproverId() != null) {
                r.setApproverName(userNameMap.get(r.getApproverId()));
            }
            if (r.getCreatorId() != null) {
                r.setCreatorName(userNameMap.get(r.getCreatorId()));
            }
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    // ===================== 私有辅助 =====================

    private AdOrderChange requireChange(String id) {
        AdOrderChange c = orderChangeMapper.selectByPrimaryKey(id);
        if (c == null || (c.getDeleted() != null && c.getDeleted() == 1)) {
            throw new GenericException("改单不存在");
        }
        return c;
    }

    private AdOrder requireOrder(String id) {
        AdOrder o = adOrderMapper.selectByPrimaryKey(id);
        if (o == null || (o.getDeleted() != null && o.getDeleted() == 1)) {
            throw new GenericException("订单不存在");
        }
        return o;
    }

    /**
     * 校验变更字段：禁止 orderType(L-24)，仅允许白名单字段。
     */
    private void validateChangeFields(List<String> fields) {
        for (String f : fields) {
            if ("orderType".equals(f) || "order_type".equals(f)) {
                throw new GenericException("改单禁止变更订单类型(order_type)，如需变更请走作废重建流程(L-24)");
            }
            if (!MUTABLE_FIELDS.contains(f)) {
                throw new GenericException("不支持变更字段: " + f);
            }
        }
    }

    /**
     * 读取订单指定字段当前值（用于变更前快照）。
     */
    private Object readField(AdOrder order, String field) {
        switch (field) {
            case "totalAmount": return order.getTotalAmount();
            case "noRebateAmount": return order.getNoRebateAmount();
            case "rebateValue": return order.getRebateValue();
            case "receiptPrepayRatio": return order.getReceiptPrepayRatio();
            case "paymentPrepayRatio": return order.getPaymentPrepayRatio();
            case "rebateMode": return order.getRebateMode();
            case "receiptMethod": return order.getReceiptMethod();
            case "receiptPrepayMode": return order.getReceiptPrepayMode();
            case "receiptAccountPeriodDays": return order.getReceiptAccountPeriodDays();
            case "paymentMethod": return order.getPaymentMethod();
            case "paymentPrepayMode": return order.getPaymentPrepayMode();
            case "paymentPostpayTrigger": return order.getPaymentPostpayTrigger();
            case "paymentPostpayDays": return order.getPaymentPostpayDays();
            case "deliveryStartDate": return order.getDeliveryStartDate();
            case "deliveryEndDate": return order.getDeliveryEndDate();
            case "receiptPrepayDeadline": return order.getReceiptPrepayDeadline();
            case "paymentPrepayDeadline": return order.getPaymentPrepayDeadline();
            case "orderName": return order.getOrderName();
            case "deliveryVolume": return order.getDeliveryVolume();
            case "remark": return order.getRemark();
            case "extJson": return order.getExtJson();
            case "currency": return order.getCurrency();
            case "signingEntity": return order.getSigningEntity();
            case "industryCode": return order.getIndustryCode();
            case "upstreamAgentId": return order.getUpstreamAgentId();
            case "agentOrderNo": return order.getAgentOrderNo();
            case "customerId": return order.getCustomerId();
            default: return null;
        }
    }

    /**
     * 将快照 Map 应用到订单（仅白名单字段，按类型安全转换）。
     */
    private void applyAfter(AdOrder order, Map<String, Object> after) {
        if (after == null) {
            return;
        }
        for (Map.Entry<String, Object> e : after.entrySet()) {
            String field = e.getKey();
            Object val = e.getValue();
            switch (field) {
                case "totalAmount": order.setTotalAmount(toBigDecimal(val)); break;
                case "noRebateAmount": order.setNoRebateAmount(toBigDecimal(val)); break;
                case "rebateValue": order.setRebateValue(toBigDecimal(val)); break;
                case "receiptPrepayRatio": order.setReceiptPrepayRatio(toBigDecimal(val)); break;
                case "paymentPrepayRatio": order.setPaymentPrepayRatio(toBigDecimal(val)); break;
                case "rebateMode": order.setRebateMode(toInt(val)); break;
                case "receiptMethod": order.setReceiptMethod(toInt(val)); break;
                case "receiptPrepayMode": order.setReceiptPrepayMode(toInt(val)); break;
                case "receiptAccountPeriodDays": order.setReceiptAccountPeriodDays(toInt(val)); break;
                case "paymentMethod": order.setPaymentMethod(toInt(val)); break;
                case "paymentPrepayMode": order.setPaymentPrepayMode(toInt(val)); break;
                case "paymentPostpayTrigger": order.setPaymentPostpayTrigger(toInt(val)); break;
                case "paymentPostpayDays": order.setPaymentPostpayDays(toInt(val)); break;
                case "deliveryStartDate": order.setDeliveryStartDate(toDate(val)); break;
                case "deliveryEndDate": order.setDeliveryEndDate(toDate(val)); break;
                case "receiptPrepayDeadline": order.setReceiptPrepayDeadline(toDate(val)); break;
                case "paymentPrepayDeadline": order.setPaymentPrepayDeadline(toDate(val)); break;
                case "orderName": order.setOrderName(toStr(val)); break;
                case "deliveryVolume": order.setDeliveryVolume(toStr(val)); break;
                case "remark": order.setRemark(toStr(val)); break;
                case "extJson": order.setExtJson(toStr(val)); break;
                case "currency": order.setCurrency(toStr(val)); break;
                case "signingEntity": order.setSigningEntity(toStr(val)); break;
                case "industryCode": order.setIndustryCode(toStr(val)); break;
                case "upstreamAgentId": order.setUpstreamAgentId(toStr(val)); break;
                case "agentOrderNo": order.setAgentOrderNo(toStr(val)); break;
                case "customerId": order.setCustomerId(toStr(val)); break;
                default: break;
            }
        }
    }

    private void recordOrderLog(AdOrder order, String action, int from, int to, String userId, String orgId) {
        AdOrderLog log = new AdOrderLog();
        log.setId(IDGenerator.nextStr());
        log.setOrderId(order.getId());
        log.setAction(action);
        log.setOperatorId(userId);
        log.setOrganizationId(orgId);
        log.setBeforeValue(String.format("{\"status\":%d}", from));
        log.setAfterValue(String.format("{\"status\":%d}", to));
        log.setCreateTime(System.currentTimeMillis());
        orderLogMapper.insert(log);
    }

    // ===================== 类型转换 =====================

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        if (v instanceof Number) {
            return BigDecimal.valueOf(((Number) v).doubleValue());
        }
        if (v instanceof String) {
            String s = ((String) v).trim();
            return s.isEmpty() ? null : new BigDecimal(s);
        }
        return null;
    }

    private Integer toInt(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        if (v instanceof String) {
            String s = ((String) v).trim();
            return s.isEmpty() ? null : Integer.parseInt(s);
        }
        return null;
    }

    private Date toDate(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Date) {
            return (Date) v;
        }
        if (v instanceof Number) {
            return new Date(((Number) v).longValue());
        }
        if (v instanceof String) {
            String s = ((String) v).trim();
            return s.isEmpty() ? null : new Date(Long.parseLong(s));
        }
        return null;
    }

    private String toStr(Object v) {
        return v == null ? null : v.toString();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    // ===================== JSON =====================

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new GenericException("快照序列化失败: " + e.getMessage());
        }
    }

    private Map<String, Object> parseJson(String s) {
        try {
            return objectMapper.readValue(s, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new GenericException("快照解析失败: " + e.getMessage());
        }
    }

    // ===================== 权限守卫（走分配的权限码体系，见 PermissionConstants） =====================

    private void assertRole(String requiredPermission) {
        if (requiredPermission == null || OrderStateMachine.ROLE_SYSTEM.equals(requiredPermission)) {
            return;
        }
        if (!PermissionUtils.hasPermission(requiredPermission)) {
            throw new GenericException("当前权限无权执行该操作: " + requiredPermission);
        }
    }

    private boolean hasRole(String requiredPermission) {
        if (requiredPermission == null || OrderStateMachine.ROLE_SYSTEM.equals(requiredPermission)) {
            return true;
        }
        return PermissionUtils.hasPermission(requiredPermission);
    }
}
