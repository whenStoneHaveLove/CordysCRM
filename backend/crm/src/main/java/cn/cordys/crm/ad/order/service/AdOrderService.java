package cn.cordys.crm.ad.order.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.PermissionUtils;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.common.AdEntityPermissionProvider;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.common.constants.OrderStateMachine;
import cn.cordys.crm.ad.common.constants.OrderStatus;
import cn.cordys.crm.ad.common.constants.OrderType;
import cn.cordys.crm.ad.common.constants.PaymentMethod;
import cn.cordys.crm.ad.common.constants.ReceiptMethod;
import cn.cordys.crm.ad.contract.constants.ContractType;
import cn.cordys.crm.ad.contract.domain.AdContract;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.domain.AdOrderAttachment;
import cn.cordys.crm.ad.order.domain.AdOrderChange;
import cn.cordys.crm.ad.order.domain.AdOrderContract;
import cn.cordys.crm.ad.order.domain.AdOrderDownstreamMedia;
import cn.cordys.crm.ad.order.domain.AdOrderLog;
import cn.cordys.crm.ad.order.dto.request.AdOrderApproveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderForceArchiveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderPageRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderSaveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderVoidRequest;
import cn.cordys.crm.ad.order.dto.response.AdOrderAllowedAction;
import cn.cordys.crm.ad.order.dto.response.AdOrderDetailResponse;
import cn.cordys.crm.ad.order.dto.response.AdOrderListResponse;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderChangeMapper;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderContractMapper;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderDownstreamMediaMapper;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderMapper;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderAttachmentMapper;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderLogMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 广告订单服务（M2 订单核心闭环 T-10~T-18，V3.1 §6）。
 *
 * <p>订单状态流转由 {@link OrderStateMachine} 驱动，<b>不依赖</b> {@code cn.cordys.crm.approval} 模块。
 * 每个状态变更方法均标注 {@link OperationLog}（写入 ad_operation_log）。</p>
 *
 * <p>关键规则：
 * <ul>
 *   <li>L-14 审批开关：{@code ad.order.approval.enabled}（默认 true）；关闭时提交 0→20 直达。</li>
 *   <li>L-20 订单号：{业务主体代码}-{YYYYMMDD}-{3 位当日流水}。</li>
 *   <li>§6.2 提交守卫：需 盖章排期(附件10) + 邮件截图(附件20) + 框架合同关联 齐备。</li>
 *   <li>L-05 财务前置矩阵：依据 收款方式 + 付款方式 推导 30/40/50 与所需财务步骤。</li>
 *   <li>L-21 驳回/作废 保留附件（不删除）。</li>
 *   <li>L-26 执行完成 需 ≥1 份关联合同。</li>
 *   <li>L-08 逾期自动流转：执行中(50) 且 投放结束日 已过 → 结算中(80)（由 guarded job 触发）。</li>
 *   <li>L-13 强制归档：老板执行，带坏账金额。</li>
 *   <li>L-27 作废红冲标记：已开票则置 needs_red_invoice=1。</li>
 *   <li>L-02/L-11/L-28 金额规则见 {@link AdAmountCalculator}。</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdOrderService {

    @Resource
    private BaseMapper<AdOrder> adOrderMapper;
    @Resource
    private ExtAdOrderAttachmentMapper attachmentMapper;
    @Resource
    private ExtAdOrderContractMapper orderContractMapper;
    @Resource
    private ExtAdOrderDownstreamMediaMapper orderDownstreamMediaMapper;
    @Resource
    private cn.cordys.mybatis.BaseMapper<AdContract> contractMapper;
    @Resource
    private ExtAdOrderChangeMapper orderChangeMapper;
    @Resource
    private ExtAdOrderLogMapper orderLogMapper;
    @Resource
    private BaseMapper<AdBusinessEntity> businessEntityMapper;
    @Resource
    private ExtAdOrderMapper extAdOrderMapper;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;
    @Resource
    private AdAmountCalculator amountCalculator;

    /** L-14 审批开关（默认开启）。 */
    @Value("${ad.order.approval.enabled:true}")
    private boolean approvalEnabled;

    /** 订单号逾期自动流转动作（ad_order_log.action）。 */
    private static final String ACTION_OVERDUE = "OVERDUE_AUTO_50_TO_80";

    // ===================== 创建 / 编辑草稿 =====================

    /**
     * 新建订单（草稿 0）。生成订单号、计算金额。
     */
    @OperationLog(module = "AD_ORDER", action = "CREATE", targetId = "")
    public AdOrder create(AdOrderSaveRequest request, String userId, String orgId) {
        AdOrder order = new AdOrder();
        BeanUtils.copyProperties(request, order);
        order.setId(IDGenerator.nextStr());
        order.setOrganizationId(orgId);
        order.setCreatorId(userId);
        order.setStatus(OrderStateMachine.DRAFT);
        order.setCreateUser(userId);
        order.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setOrderNo(generateOrderNo(order.getBusinessEntityId(), orgId, now));
        amountCalculator.computeAmounts(order);
        adOrderMapper.insert(order);
        // 关联合同：框架订单必选框架合同；单笔订单可后补
        syncOrderContract(order.getId(), request.getContractId(), request.getOrderType(), userId, orgId);
        // 关联下游媒体
        syncOrderDownstreamMedia(order.getId(), request.getDownstreamMediaIds(), userId, orgId);
        return order;
    }

    /**
     * 编辑草稿（仅 0 状态）。重新计算金额。
     */
    @OperationLog(module = "AD_ORDER", action = "UPDATE", targetId = "#request.id")
    public AdOrder update(AdOrderSaveRequest request, String userId, String orgId) {
        AdOrder order = requireOrder(request.getId());
        if (order.getStatus() != OrderStateMachine.DRAFT) {
            throw new GenericException("仅草稿状态可编辑");
        }
        BeanUtils.copyProperties(request, order);
        // 受控字段不允许经编辑变更
        order.setId(request.getId());
        order.setStatus(OrderStateMachine.DRAFT);
        order.setOrganizationId(orgId);
        order.setCreatorId(order.getCreatorId());
        order.setOrderNo(order.getOrderNo());
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        amountCalculator.computeAmounts(order);
        adOrderMapper.update(order);
        // 同步合同关联
        syncOrderContract(order.getId(), request.getContractId(), request.getOrderType(), userId, orgId);
        // 同步下游媒体
        syncOrderDownstreamMedia(order.getId(), request.getDownstreamMediaIds(), userId, orgId);
        return order;
    }

    // ===================== 详情 / 分页 =====================

    /**
     * 订单详情：主信息 + 附件 + 改单历史 + 操作记录 + 当前角色允许动作。
     */
    public AdOrderDetailResponse detail(String id, String userId, String orgId) {
        AdOrder order = requireOrder(id);
        List<AdOrderAttachment> attachments = attachmentMapper.selectByOrderId(id);
        List<AdOrderChange> changes = orderChangeMapper.selectByOrderId(id);
        List<AdOrderLog> logs = orderLogMapper.selectByOrderId(id);
        logs.sort(Comparator.comparing(AdOrderLog::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        // 关联合同 ID（取第一条即可，单笔/框架订单都按 1:1 简化）
        List<AdOrderContract> orderContracts = orderContractMapper.selectByOrderId(id);
        String contractId = orderContracts.isEmpty() ? null : orderContracts.get(0).getContractId();

        // 下游媒体 id 列表
        List<AdOrderDownstreamMedia> downstreamMedias = orderDownstreamMediaMapper.selectByOrderId(id);
        List<String> downstreamMediaIds = downstreamMedias.stream()
                .map(AdOrderDownstreamMedia::getDownstreamMediaId)
                .collect(Collectors.toList());

        AdOrderDetailResponse response = new AdOrderDetailResponse();
        response.setOrder(order);
        response.setContractId(contractId);
        if (contractId != null) {
            AdContract contract = contractMapper.selectByPrimaryKey(contractId);
            if (contract != null) {
                response.setContractNo(contract.getContractNo());
                response.setContractName(contract.getContractName());
            }
        }
        response.setDownstreamMediaIds(downstreamMediaIds);
        response.setAttachments(attachments);
        response.setChanges(changes);
        response.setLogs(logs);
        response.setAllowedActions(computeAllowedActions(order, userId));
        return response;
    }

    /**
     * 分页列表（多筛选 + 关键字 + 主体隔离 + 缺合同标记 + 排序）。
     */
    public PagerWithOption<List<AdOrderListResponse>> page(AdOrderPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        Page<AdOrderListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdOrderListResponse> list = extAdOrderMapper.pageList(request);
        for (AdOrderListResponse r : list) {
            r.setOrderTypeLabel(OrderType.labelOf(r.getOrderType()));
            r.setStatusLabel(OrderStatus.labelOf(r.getStatus()));
            r.setReceiptMethodLabel(ReceiptMethod.labelOf(r.getReceiptMethod()));
            r.setPaymentMethodLabel(PaymentMethod.labelOf(r.getPaymentMethod()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    // ===================== 状态流转 =====================

    /**
     * 提交：0→10（或 L-14 开关关闭时 0→45 待执行）。守卫 §6.2 附件/合同齐备。
     */
    @OperationLog(module = "AD_ORDER", action = "SUBMIT", targetId = "#id")
    public AdOrder submit(String id, String userId, String orgId) {
        AdOrder order = requireOrder(id);
        int to = approvalEnabled ? OrderStateMachine.PENDING_BOSS_APPROVAL : OrderStateMachine.PENDING_EXECUTE;
        assertTransition(order.getStatus(), to);
        // §6.2 提交守卫
        requireAttachment(order.getId(), 10, "提交前需上传【盖章排期】附件(类型10)");
        requireAttachment(order.getId(), 20, "提交前需上传【邮件截图】附件(类型20)");
        // 框架订单(10) 必传合同；单笔订单(20) 允许后补合同（执行完成前需上传附件）
        if (order.getOrderType() != null && order.getOrderType() == OrderType.FRAMEWORK.getCode()) {
            requireContract(order.getId(), "提交前需关联【框架合同】(ad_order_contract)");
        }
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        int from = order.getStatus();
        order.setStatus(to);
        adOrderMapper.update(order);
        recordLog(order, OrderStateMachine.TRIGGER_SUBMIT, from, to, userId, orgId);
        return order;
    }

    /**
     * 老板审核：通过 10→45（待执行）；驳回 10→0（保留附件 L-21）。
     */
    @OperationLog(module = "AD_ORDER", action = "APPROVE", targetId = "#id")
    public AdOrder approve(String id, AdOrderApproveRequest request, String userId, String orgId) {
        AdOrder order = requireOrder(id);
        boolean reject = "REJECT".equalsIgnoreCase(request.getAction());
        int to = reject ? OrderStateMachine.DRAFT : OrderStateMachine.PENDING_EXECUTE;
        assertTransition(OrderStateMachine.PENDING_BOSS_APPROVAL, to);
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        int from = order.getStatus();
        order.setStatus(to);
        adOrderMapper.update(order);
        String action = reject ? OrderStateMachine.TRIGGER_REJECT : OrderStateMachine.TRIGGER_APPROVE;
        AdOrderLog log = new AdOrderLog();
        log.setId(IDGenerator.nextStr());
        log.setOrderId(order.getId());
        log.setAction(action);
        log.setOperatorId(userId);
        log.setOrganizationId(orgId);
        log.setBeforeValue(String.format("{\"status\":%d}", from));
        String afterValue = String.format("{\"status\":%d}", to);
        if (reject && request.getRemark() != null && !request.getRemark().isBlank()) {
            afterValue = String.format("{\"status\":%d,\"remark\":\"%s\"}", to, request.getRemark());
        }
        log.setAfterValue(afterValue);
        log.setCreateTime(System.currentTimeMillis());
        orderLogMapper.insert(log);
        return order;
    }

    /**
     * 确认执行：45（待执行）→ 50（执行中）。
     */
    @OperationLog(module = "AD_ORDER", action = "CONFIRM_EXECUTE", targetId = "#id")
    public AdOrder confirmExecute(String id, String userId, String orgId) {
        AdOrder order = requireOrder(id);
        int from = order.getStatus();
        int to = OrderStateMachine.EXECUTING;
        assertTransition(from, to);
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        order.setStatus(to);
        adOrderMapper.update(order);
        recordLog(order, OrderStateMachine.TRIGGER_CONFIRM_EXECUTE, from, to, userId, orgId);
        return order;
    }

    /**
     * 作废：→100（允许自除已归档外的任意状态）。保留附件(L-21)；已开票置红冲标记(L-27)。
     */
    @OperationLog(module = "AD_ORDER", action = "VOID", targetId = "#id")
    public AdOrder voidOrder(String id, AdOrderVoidRequest request, String userId, String orgId) {
        AdOrder order = requireOrder(id);
        int from = order.getStatus();
        int to = OrderStateMachine.VOIDED;
        assertTransition(from, to);
        order.setVoidedAt(new Date());
        order.setVoidReason(request.getReason());
        // L-27 红冲标记：已开票则需红冲
        boolean invoiced = (order.getInvoiceStatus() != null && order.getInvoiceStatus() > 0)
                || (order.getInvoicedAmount() != null && order.getInvoicedAmount().compareTo(BigDecimal.ZERO) > 0);
        if (invoiced) {
            order.setNeedsRedInvoice(1);
        }
        // L-21 保留附件（默认不删除）
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        order.setStatus(to);
        adOrderMapper.update(order);
        recordLog(order, OrderStateMachine.TRIGGER_VOID, from, to, userId, orgId);
        return order;
    }

    /**
     * 强制归档：80→90（老板执行，L-13 带坏账金额）。
     */
    @OperationLog(module = "AD_ORDER", action = "FORCE_ARCHIVE", targetId = "#id")
    public AdOrder forceArchive(String id, AdOrderForceArchiveRequest request, String userId, String orgId) {
        AdOrder order = requireOrder(id);
        int from = order.getStatus();
        int to = OrderStateMachine.ARCHIVED;
        assertTransition(from, to);
        assertRole(OrderStateMachine.requiredRoleFor(from, to));
        order.setArchivedAt(new Date());
        order.setBadDebtAmount(request.getBadDebtAmount());
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        order.setStatus(to);
        adOrderMapper.update(order);
        recordLog(order, OrderStateMachine.TRIGGER_FORCE_ARCHIVE, from, to, userId, orgId);
        return order;
    }

    // ===================== 逾期自动流转（L-08，由 guarded job 触发） =====================

    /**
     * 扫描并自动将逾期的执行中订单(50) 流转至结算中(80)。
     * 仅当 投放结束日 &lt; 当前日期 时触发。由 {@code AdOrderOverdueJob} 调用。
     */
    public int checkOverdue() {
        Date now = new Date();
        List<AdOrder> overdue = extAdOrderMapper.selectOverdue(new java.sql.Date(now.getTime()));
        if (overdue == null || overdue.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (AdOrder order : overdue) {
            if (!OrderStateMachine.canTransit(order.getStatus(), OrderStateMachine.SETTLEMENT, approvalEnabled)) {
                continue;
            }
            int from = order.getStatus();
            order.setStatus(OrderStateMachine.SETTLEMENT);
            order.setUpdateTime(System.currentTimeMillis());
            adOrderMapper.update(order);
            recordLog(order, ACTION_OVERDUE, from, OrderStateMachine.SETTLEMENT,
                    SessionUtils.getUserId(), order.getOrganizationId());
            count++;
        }
        return count;
    }

    // ===================== 自动归档（由 guarded job 触发） =====================

    /**
     * 扫描结算中(80)订单，满足「有关联合同 + 收款已收 + 付款已付」后自动归档到已归档(90)。
     * 由 {@code AdOrderOverdueJob} 每分钟调用。
     */
    public int checkArchive() {
        List<AdOrder> settlement = extAdOrderMapper.selectSettlement();
        if (settlement == null || settlement.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (AdOrder order : settlement) {
            // 1) 必须关联合同
            List<AdOrderContract> contracts = orderContractMapper.selectByOrderId(order.getId());
            if (contracts == null || contracts.isEmpty()) {
                continue;
            }
            // 2) 收款已收 + 付款已付
            boolean receiptDone = order.getReceiptDone() != null && order.getReceiptDone() == 1;
            boolean paymentDone = order.getPaymentDone() != null && order.getPaymentDone() == 1;
            if (!receiptDone || !paymentDone) {
                continue;
            }
            if (!OrderStateMachine.canTransit(order.getStatus(), OrderStateMachine.ARCHIVED, approvalEnabled)) {
                continue;
            }
            int from = order.getStatus();
            order.setArchivedAt(new Date());
            order.setStatus(OrderStateMachine.ARCHIVED);
            order.setUpdateTime(System.currentTimeMillis());
            adOrderMapper.update(order);
            recordLog(order, OrderStateMachine.TRIGGER_AUTO_ARCHIVE, from, OrderStateMachine.ARCHIVED,
                    SessionUtils.getUserId(), order.getOrganizationId());
            count++;
        }
        return count;
    }

    // ===================== 私有辅助 =====================

    private AdOrder requireOrder(String id) {
        AdOrder order = adOrderMapper.selectByPrimaryKey(id);
        if (order == null || (order.getDeleted() != null && order.getDeleted() == 1)) {
            throw new GenericException("订单不存在");
        }
        return order;
    }

    /**
     * 校验状态流转合法性（状态机），并按所需角色做权限校验。
     */
    private void assertTransition(int from, int to) {
        if (!OrderStateMachine.canTransit(from, to, approvalEnabled)) {
            throw new GenericException("订单状态流转不合法: " + from + " -> " + to);
        }
        assertRole(OrderStateMachine.requiredRoleFor(from, to));
    }

    /**
     * 按所需权限码做权限校验（走分配的权限码体系，见 PermissionConstants）。
     * SYSTEM 始终放行；无权限抛异常。
     */
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

    private void requireAttachment(String orderId, int type, String message) {
        long cnt = attachmentMapper.selectByOrderIdAndType(orderId, type).size();
        if (cnt == 0) {
            throw new GenericException(message);
        }
    }

    private void requireContract(String orderId, String message) {
        long cnt = orderContractMapper.selectByOrderId(orderId).size();
        if (cnt == 0) {
            throw new GenericException(message);
        }
    }

    /**
     * 同步订单与合同的关联到 ad_order_contract 中间表。
     *
     * <ul>
     *   <li>contractId 为空 → 跳过（单笔订单可后补合同）</li>
     *   <li>订单类型为 FRAMEWORK(10) → 合同必须是 FRAMEWORK(10)，否则报错</li>
     *   <li>订单类型为 SINGLE(20) → 合同允许后补，contractId 非空时校验合同存在即可</li>
     *   <li>已存在该订单-合同关联 → 跳过</li>
     * </ul>
     */
    private void syncOrderContract(String orderId, String contractId, Integer orderType, String userId, String orgId) {
        // 唯一键 uk_ad_oc(order_id, contract_id) 不区分 deleted，因此不能先删再插，
        // 必须复用已存在记录（含已删除），否则会触发 Duplicate entry。
        List<AdOrderContract> all = orderContractMapper.selectAllByOrderId(orderId);
        if (contractId == null || contractId.isBlank()) {
            // 清空：将全部关联逻辑删除
            for (AdOrderContract oc : all) {
                if (oc.getDeleted() != 1) {
                    oc.setDeleted(1);
                    orderContractMapper.update(oc);
                }
            }
            return;
        }
        AdContract contract = contractMapper.selectByPrimaryKey(contractId);
        if (contract == null || (contract.getDeleted() != null && contract.getDeleted() == 1)) {
            throw new GenericException("关联合同不存在");
        }
        // 框架订单必须绑定框架合同
        if (orderType != null && orderType == OrderType.FRAMEWORK.getCode()
                && contract.getContractType() != ContractType.FRAMEWORK.getCode()) {
            throw new GenericException("框架订单必须关联【框架合同】");
        }
        // 复用已存在的 (order_id, contract_id) 记录（含已删除），否则新增
        AdOrderContract existing = all.stream()
                .filter(oc -> contractId.equals(oc.getContractId()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.setDeleted(0);
            existing.setOrganizationId(orgId);
            orderContractMapper.update(existing);
        } else {
            AdOrderContract oc = new AdOrderContract();
            oc.setId(IDGenerator.nextStr());
            oc.setOrderId(orderId);
            oc.setContractId(contractId);
            oc.setOrganizationId(orgId);
            oc.setDeleted(0);
            oc.setCreateTime(System.currentTimeMillis());
            orderContractMapper.insert(oc);
        }
        // 其余合同关联逻辑删除（订单端当前仅支持单选）
        for (AdOrderContract oc : all) {
            if (!contractId.equals(oc.getContractId()) && oc.getDeleted() != 1) {
                oc.setDeleted(1);
                orderContractMapper.update(oc);
            }
        }
    }

    /**
     * 同步订单与下游媒体的关联到 ad_order_downstream_media 中间表。
     * 全量替换：先逻辑删除旧的关联，再插入新的关联。
     * 注意：由于唯一键 (order_id, downstream_media_id) 不区分 deleted，
     * 插入前先检查已存在记录（含已删除），若存在则复用（设 deleted=0）。
     */
    private void syncOrderDownstreamMedia(String orderId, List<String> downstreamMediaIds, String userId, String orgId) {
        // 先逻辑删除该订单的所有现有下游媒体关联
        List<AdOrderDownstreamMedia> existing = orderDownstreamMediaMapper.selectByOrderId(orderId);
        for (AdOrderDownstreamMedia odm : existing) {
            odm.setDeleted(1);
            orderDownstreamMediaMapper.update(odm);
        }
        // 插入新的关联
        if (downstreamMediaIds == null || downstreamMediaIds.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (String mediaId : downstreamMediaIds) {
            if (mediaId == null || mediaId.isBlank()) {
                continue;
            }
            // 检查是否已有同组合记录（含已删除的），有则复用避免唯一键冲突
            AdOrderDownstreamMedia reused = orderDownstreamMediaMapper.selectByOrderIdAndMediaId(orderId, mediaId);
            if (reused != null) {
                reused.setDeleted(0);
                reused.setOrganizationId(orgId);
                orderDownstreamMediaMapper.update(reused);
            } else {
                AdOrderDownstreamMedia odm = new AdOrderDownstreamMedia();
                odm.setId(IDGenerator.nextStr());
                odm.setOrderId(orderId);
                odm.setDownstreamMediaId(mediaId);
                odm.setOrganizationId(orgId);
                odm.setDeleted(0);
                odm.setCreateTime(now);
                orderDownstreamMediaMapper.insert(odm);
            }
        }
    }

    private void recordLog(AdOrder order, String action, int from, int to, String userId, String orgId) {
        AdOrderLog log = new AdOrderLog();
        log.setId(IDGenerator.nextStr());
        log.setOrderId(order.getId());
        log.setAction(action);
        // 定时任务（系统自动流转）无登录用户，operator_id 用 SYSTEM 占位，避免 NOT NULL 约束报错
        log.setOperatorId(userId == null || userId.isBlank() ? "SYSTEM" : userId);
        log.setOrganizationId(orgId);
        log.setBeforeValue(String.format("{\"status\":%d}", from));
        log.setAfterValue(String.format("{\"status\":%d}", to));
        log.setCreateTime(System.currentTimeMillis());
        orderLogMapper.insert(log);
    }

    private List<AdOrderAllowedAction> computeAllowedActions(AdOrder order, String userId) {
        int from = order.getStatus();
        List<Integer> nextStates = OrderStateMachine.nextStates(from);
        List<AdOrderAllowedAction> actions = new ArrayList<>();
        for (Integer to : nextStates) {
            AdOrderAllowedAction action = new AdOrderAllowedAction();
            action.setFromStatus(from);
            action.setToStatus(to);
            action.setTrigger(OrderStateMachine.triggerFor(from, to));
            String requiredRole = OrderStateMachine.requiredRoleFor(from, to);
            action.setRequiredRole(requiredRole);
            action.setAllowed(hasRole(requiredRole));
            OrderStatus statusEnum = OrderStatus.of(to);
            action.setLabel(statusEnum == null ? String.valueOf(to) : statusEnum.getLabel());
            actions.add(action);
        }
        return actions;
    }

    /**
     * L-20 订单号：{业务主体代码}-{YYYYMMDD}-{3 位当日流水}。
     */
    private String generateOrderNo(String businessEntityId, String orgId, long now) {
        String code = "XX";
        if (businessEntityId != null) {
            AdBusinessEntity be = businessEntityMapper.selectByPrimaryKey(businessEntityId);
            if (be != null && be.getCode() != null && !be.getCode().isBlank()) {
                code = be.getCode();
            }
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        String ymd = String.format("%04d%02d%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        long seq = extAdOrderMapper.countTodayOrders(businessEntityId, startOfDay(now), endOfDay(now), orgId) + 1;
        return code + "-" + ymd + "-" + String.format("%03d", seq);
    }

    private long startOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long endOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }
}
