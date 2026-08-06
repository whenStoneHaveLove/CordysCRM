package cn.cordys.crm.ad.payment.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.common.AdEntityPermissionProvider;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.common.constants.MediaPaymentStatus;
import cn.cordys.crm.ad.common.constants.OrderStateMachine;
import cn.cordys.crm.ad.common.constants.PaymentMethod;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.domain.AdOrderLog;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderMapper;
import cn.cordys.crm.ad.payment.constants.PaymentDirection;
import cn.cordys.crm.ad.payment.constants.PaymentType;
import cn.cordys.crm.ad.payment.domain.AdPaymentRecord;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentCancelRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentConfirmPrepayRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentInvoiceRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentMediaPostpayRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentMediaPrepayRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentRecordCreateRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentRecordPageRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentReceiveRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentRedInvoiceClearRequest;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentTodoRequest;
import cn.cordys.crm.ad.payment.dto.response.AdPaymentRecordDetailResponse;
import cn.cordys.crm.ad.payment.dto.response.AdPaymentRecordListResponse;
import cn.cordys.crm.ad.payment.dto.response.AdPaymentTodoResponse;
import cn.cordys.crm.ad.payment.mapper.ExtAdPaymentRecordMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUtils;
import cn.cordys.security.SessionUser;
import cn.cordys.common.dto.RoleDataScopeDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 广告收付款（财务中心）服务（M4 T-30~T-35，V3.1 §6/§8.3/§13.2）。
 *
 * <p>本服务是 M3 改单「资金侧」明细（L-04 退款 type=50 等）真正落 {@code ad_payment_record} 的地方，
 * 并在每次登记/撤销时回写订单的 invoice_status / receipt_status / media_payment_status 与
 * invoiced_amount / received_amount / media_paid_amount（L-01/L-02/L-11/L-28），保证收付款与金额核销一致。</p>
 *
 * <p>状态机：
 * <ul>
 *   <li>媒体付款进度 {@code media_payment_status}(0未付/10部分/20全额) 由 {@link MediaPaymentStatus} 枚举驱动，
 *        随 media_paid_amount 累加在 service 内以 guard + 角色校验方式推导（不依赖审批模块）。</li>
 *   <li>确认预收款/付媒体预付款还会驱动订单主状态机 30→40/50、40→50（与 OrderStateMachine 一致）。</li>
 * </ul>
 * 所有状态变更方法均标注 {@link OperationLog}（写入 ad_operation_log），主状态流转另写 ad_order_log。</p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdPaymentRecordService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Resource
    private BaseMapper<AdPaymentRecord> paymentMapper;
    @Resource
    private ExtAdOrderMapper adOrderMapper;
    @Resource
    private BaseMapper<AdOrderLog> orderLogMapper;
    @Resource
    private cn.cordys.common.service.BaseService baseService;
    @Resource
    private ExtAdPaymentRecordMapper extPaymentMapper;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;

    // ===================== 通用登记（创建 ad_payment_record 并回写订单金额） =====================

    /**
     * 通用收付款登记（M4 核心入口）：记录一笔资金动作并回写订单发票/收款/媒体付款进度与累计。
     */
    @OperationLog(module = "PAYMENT", action = "CREATE", targetId = "#request.orderId")
    public AdPaymentRecord create(AdPaymentRecordCreateRequest request, String userId, String orgId) {
        assertFinanceRole();
        AdOrder order = requireOrder(request.getOrderId());
        boolean invoiceOnly = "INVOICE".equalsIgnoreCase(request.getSettleType());
        boolean receiptOnly = "RECEIPT".equalsIgnoreCase(request.getSettleType());
        return doCreate(order, request.getDirection(), request.getType(), request.getAmount(),
                request.getOccurDate(), request.getResourceId(), request.getInvoiceNo(),
                request.getRemark(), invoiceOnly, receiptOnly, userId, orgId);
    }

    /**
     * 确认预收款（T-31，L-11 基数=应收）。回写 received_amount，并驱动主状态 30→40/50。
     */
    @OperationLog(module = "PAYMENT", action = "CONFIRM_PREPAY", targetId = "#request.orderId")
    public AdPaymentRecord confirmPrepay(AdPaymentConfirmPrepayRequest request, String userId, String orgId) {
        assertFinanceRole();
        AdOrder order = requireOrder(request.getOrderId());
        AdPaymentRecord rec = doCreate(order, PaymentDirection.UPSTREAM.getCode(), PaymentType.PRE_RECEIPT.getCode(),
                request.getAmount(), request.getOccurDate(), null, null, request.getRemark(), false, false, userId, orgId);
        transitAfterPrepayConfirm(order, userId, orgId);
        return rec;
    }

    /**
     * 付媒体预付款（T-31，L-28 基数=media_payable）。回写 media_paid_amount，并驱动主状态 40→50。
     */
    @OperationLog(module = "PAYMENT", action = "PAY_MEDIA_PREPAY", targetId = "#request.orderId")
    public AdPaymentRecord payMediaPrepay(AdPaymentMediaPrepayRequest request, String userId, String orgId) {
        assertFinanceRole();
        AdOrder order = requireOrder(request.getOrderId());
        AdPaymentRecord rec = doCreate(order, PaymentDirection.DOWNSTREAM.getCode(), PaymentType.PRE_PAY.getCode(),
                request.getAmount(), request.getOccurDate(), request.getResourceId(), null, request.getRemark(), false, false, userId, orgId);
        transitAfterMediaPrepay(order, userId, orgId);
        return rec;
    }

    /**
     * 开票（T-32，L-02 开票金额=应收）。仅回写 invoiced_amount / invoice_status，不触碰已收款。
     */
    @OperationLog(module = "PAYMENT", action = "INVOICE", targetId = "#request.orderId")
    public AdPaymentRecord invoice(AdPaymentInvoiceRequest request, String userId, String orgId) {
        assertFinanceRole();
        AdOrder order = requireOrder(request.getOrderId());
        return doCreate(order, PaymentDirection.UPSTREAM.getCode(), PaymentType.INVOICE_RECEIPT.getCode(),
                request.getAmount(), request.getOccurDate(), null, request.getInvoiceNo(), request.getRemark(),
                true, false, userId, orgId);
    }

    /**
     * 收款登记（T-32）。仅回写 received_amount / receipt_status，不触碰已开票。
     */
    @OperationLog(module = "PAYMENT", action = "RECEIVE", targetId = "#request.orderId")
    public AdPaymentRecord receive(AdPaymentReceiveRequest request, String userId, String orgId) {
        assertFinanceRole();
        AdOrder order = requireOrder(request.getOrderId());
        return doCreate(order, PaymentDirection.UPSTREAM.getCode(), PaymentType.INVOICE_RECEIPT.getCode(),
                request.getAmount(), request.getOccurDate(), null, null, request.getRemark(),
                false, true, userId, orgId);
    }

    /**
     * 付媒体尾款（T-33，L-05/L-28）。回写 media_paid_amount；媒体已全额付清时非强制触发将被拒绝。
     */
    @OperationLog(module = "PAYMENT", action = "PAY_MEDIA_POSTPAY", targetId = "#request.orderId")
    public AdPaymentRecord payMediaPostpay(AdPaymentMediaPostpayRequest request, String userId, String orgId) {
        assertFinanceRole();
        AdOrder order = requireOrder(request.getOrderId());
        boolean force = request.getForce() != null && request.getForce();
        if (!force && order.getMediaPaymentStatus() != null && order.getMediaPaymentStatus() == 20) {
            throw new GenericException("媒体款已全额付清，如需仍可付请使用强制触发入口(force=true)");
        }
        return doCreate(order, PaymentDirection.DOWNSTREAM.getCode(), PaymentType.MEDIA_POSTPAY.getCode(),
                request.getAmount(), request.getOccurDate(), request.getResourceId(), null, request.getRemark(), false, false, userId, orgId);
    }

    /**
     * 清除红冲标记（T-33，L-27）。财务线下红冲处理完毕后清除订单 needs_red_invoice 标记。
     */
    @OperationLog(module = "PAYMENT", action = "RED_INVOICE_CLEAR", targetId = "#request.orderId")
    public AdOrder clearRedInvoice(AdPaymentRedInvoiceClearRequest request, String userId, String orgId) {
        assertFinanceRole();
        AdOrder order = requireOrder(request.getOrderId());
        order.setNeedsRedInvoice(0);
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        adOrderMapper.update(order);
        return order;
    }

    // ===================== 撤销（冲销） =====================

    /**
     * 撤销收付款记录（M4，cancel/reverse）。反向回写订单金额进度，并对原记录做逻辑删除。
     */
    @OperationLog(module = "PAYMENT", action = "CANCEL", targetId = "#request.id")
    public AdPaymentRecord cancel(AdPaymentCancelRequest request, String userId, String orgId) {
        assertFinanceRole();
        AdPaymentRecord rec = paymentMapper.selectByPrimaryKey(request.getId());
        if (rec == null || (rec.getDeleted() != null && rec.getDeleted() == 1)) {
            throw new GenericException("收付款记录不存在");
        }
        AdOrder order = requireOrder(rec.getOrderId());
        // 反向回写（type=30 默认双向冲减）
        applyFinanceEffect(order, rec.getDirection(), rec.getType(),
                rec.getAmount() == null ? ZERO : rec.getAmount().negate(), false, false);
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        adOrderMapper.update(order);
        // 逻辑删除原记录
        rec.setDeleted(1);
        rec.setUpdateUser(userId);
        rec.setUpdateTime(System.currentTimeMillis());
        paymentMapper.updateById(rec);
        return rec;
    }

    // ===================== 分页 / 详情 / 待办 =====================

    /**
     * 收付款记录分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     */
    public PagerWithOption<List<AdPaymentRecordListResponse>> page(AdPaymentRecordPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        Page<AdPaymentRecordListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdPaymentRecordListResponse> list = extPaymentMapper.pageList(request);

        // 操作人姓名翻译
        Set<String> userIds = new HashSet<>();
        for (AdPaymentRecordListResponse r : list) {
            if (r.getOperatorId() != null) userIds.add(r.getOperatorId());
        }
        Map<String, String> userNameMap = userIds.isEmpty() ? new HashMap<>() : baseService.getUserNameMap(userIds);

        for (AdPaymentRecordListResponse r : list) {
            r.setDirectionLabel(PaymentDirection.labelOf(r.getDirection()));
            r.setTypeLabel(PaymentType.labelOf(r.getType()));
            if (r.getOperatorId() != null) {
                r.setOperatorName(userNameMap.get(r.getOperatorId()));
            }
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    /**
     * 收付款记录详情。
     */
    public AdPaymentRecordDetailResponse detail(String id, String userId, String orgId) {
        AdPaymentRecord rec = paymentMapper.selectByPrimaryKey(id);
        if (rec == null || (rec.getDeleted() != null && rec.getDeleted() == 1)) {
            throw new GenericException("收付款记录不存在");
        }
        AdPaymentRecordDetailResponse resp = new AdPaymentRecordDetailResponse();
        resp.setRecord(rec);
        resp.setDirectionLabel(PaymentDirection.labelOf(rec.getDirection()));
        resp.setTypeLabel(PaymentType.labelOf(rec.getType()));
        AdOrder order = adOrderMapper.selectByPrimaryKey(rec.getOrderId());
        resp.setOrderNo(order == null ? null : order.getOrderNo());
        return resp;
    }

    /**
     * 财务待办（M4 T-33）：预收款待确认/媒体预付款待付/媒体尾款待付/红冲待办/开票收款待处理。
     */
    public PagerWithOption<List<AdPaymentTodoResponse>> todoPage(AdPaymentTodoRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        List<AdOrder> orders = queryTodoOrders(orgId, request.getEntityIds(), request.getBusinessEntityId(), request.getKeyword());
        List<AdPaymentTodoResponse> all = new ArrayList<>();
        for (AdOrder o : orders) {
            buildTodos(o, all);
        }
        all.sort(Comparator.comparing(AdPaymentTodoResponse::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        int current = request.getCurrent() <= 0 ? 1 : request.getCurrent();
        int pageSize = request.getPageSize() <= 0 ? 20 : request.getPageSize();
        int fromIdx = Math.min((current - 1) * pageSize, all.size());
        int toIdx = Math.min(fromIdx + pageSize, all.size());
        List<AdPaymentTodoResponse> slice = all.subList(fromIdx, toIdx);
        Page<AdPaymentTodoResponse> page = new Page<>(current, pageSize);
        page.setTotal(all.size());
        page.addAll(slice);
        return PageUtils.setPageInfoWithOption(page, slice, null);
    }

    // ===================== M3 改单资金侧接入点（L-04，由 M3 改单执行时调用） =====================

    /**
     * 改单资金侧：生成"应退款"明细（type=50）。由 M3 改单执行（金额变小、已收&gt;新应收）调用。
     */
    @OperationLog(module = "PAYMENT", action = "CHANGE_REFUND", targetId = "#orderId")
    public AdPaymentRecord createRefundForChange(String orderId, BigDecimal refundAmount, String userId, String orgId) {
        AdOrder order = requireOrder(orderId);
        if (refundAmount == null || refundAmount.compareTo(ZERO) <= 0) {
            return null;
        }
        return doCreate(order, PaymentDirection.UPSTREAM.getCode(), PaymentType.REFUND.getCode(),
                refundAmount, new Date(), null, null, "改单资金侧-应退款", false, false, userId, orgId);
    }

    /**
     * 改单资金侧：生成"坏账"标记明细（type=60）。由 M3 改单执行（金额变小、已收&gt;新应收）调用。
     */
    @OperationLog(module = "PAYMENT", action = "CHANGE_BAD_DEBT", targetId = "#orderId")
    public AdPaymentRecord createBadDebtForChange(String orderId, BigDecimal badDebtAmount, String userId, String orgId) {
        AdOrder order = requireOrder(orderId);
        if (badDebtAmount == null || badDebtAmount.compareTo(ZERO) <= 0) {
            return null;
        }
        return doCreate(order, PaymentDirection.DOWNSTREAM.getCode(), PaymentType.BAD_DEBT.getCode(),
                badDebtAmount, new Date(), null, null, "改单资金侧-坏账", false, false, userId, orgId);
    }

    // ===================== 私有辅助 =====================

    /**
     * 写入一笔收付款记录并回写订单金额进度（同一 order 对象，多次调用叠加）。
     */
    private AdPaymentRecord doCreate(AdOrder order, int direction, int type, BigDecimal amount,
                                     Date occurDate, String resourceId, String invoiceNo, String remark,
                                     boolean invoiceOnly, boolean receiptOnly, String userId, String orgId) {
        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new GenericException("金额必须大于0");
        }
        if (direction != PaymentDirection.UPSTREAM.getCode() && direction != PaymentDirection.DOWNSTREAM.getCode()) {
            throw new GenericException("方向不合法(10上游收款/20下游付款)");
        }
        if (type < 10 || type > 60) {
            throw new GenericException("类型不合法(10预收/20预付/30开票收款/40媒体尾款/50退款/60坏账)");
        }
        AdPaymentRecord rec = new AdPaymentRecord();
        rec.setId(IDGenerator.nextStr());
        rec.setOrderId(order.getId());
        rec.setBusinessEntityId(order.getBusinessEntityId());
        rec.setResourceId(resourceId);
        rec.setDirection(direction);
        rec.setType(type);
        rec.setAmount(amount);
        rec.setOccurDate(occurDate == null ? new Date() : occurDate);
        rec.setInvoiceNo(invoiceNo);
        rec.setRemark(remark);
        rec.setOperatorId(userId);
        rec.setOrganizationId(orgId);
        rec.setCreateUser(userId);
        rec.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        rec.setCreateTime(now);
        rec.setUpdateTime(now);
        rec.setDeleted(0);
        paymentMapper.insert(rec);

        applyFinanceEffect(order, direction, type, amount, invoiceOnly, receiptOnly);
        order.setUpdateUser(userId);
        order.setUpdateTime(now);
        adOrderMapper.update(order);
        return rec;
    }

    /**
     * 按方向/类型/金额回写订单的 invoice/receipt/media 累计与进度（L-01/L-02/L-11/L-28）。
     */
    private void applyFinanceEffect(AdOrder order, int direction, int type, BigDecimal amount,
                                    boolean invoiceOnly, boolean receiptOnly) {
        BigDecimal received = nvl(order.getReceivedAmount());
        BigDecimal invoiced = nvl(order.getInvoicedAmount());
        BigDecimal mediaPaid = nvl(order.getMediaPaidAmount());
        BigDecimal badDebt = nvl(order.getBadDebtAmount());
        BigDecimal delta = amount == null ? ZERO : amount;

        if (direction == PaymentDirection.UPSTREAM.getCode()) {
            if (type == PaymentType.PRE_RECEIPT.getCode()) {
                received = received.add(delta);
            } else if (type == PaymentType.INVOICE_RECEIPT.getCode()) {
                if (invoiceOnly) {
                    invoiced = invoiced.add(delta);
                } else if (receiptOnly) {
                    received = received.add(delta);
                } else {
                    invoiced = invoiced.add(delta);
                    received = received.add(delta);
                }
            } else if (type == PaymentType.REFUND.getCode()) {
                received = received.subtract(delta);
                if (received.compareTo(ZERO) < 0) {
                    received = ZERO;
                }
            }
        } else if (direction == PaymentDirection.DOWNSTREAM.getCode()) {
            if (type == PaymentType.PRE_PAY.getCode() || type == PaymentType.MEDIA_POSTPAY.getCode()) {
                mediaPaid = mediaPaid.add(delta);
            } else if (type == PaymentType.BAD_DEBT.getCode()) {
                badDebt = badDebt.add(delta);
            }
        }
        order.setReceivedAmount(received);
        order.setInvoicedAmount(invoiced);
        order.setMediaPaidAmount(mediaPaid);
        order.setBadDebtAmount(badDebt);

        order.setInvoiceStatus(computeProgress(invoiced, order.getReceivableAmount()));
        order.setReceiptStatus(computeProgress(received, order.getReceivableAmount()));
        order.setMediaPaymentStatus(MediaPaymentStatus.compute(mediaPaid, order.getMediaPayableAmount()));
    }

    /**
     * 进度推导（0未/10部分/20全额）：done/total 口径同 MediaPaymentStatus.compute。
     */
    private int computeProgress(BigDecimal done, BigDecimal total) {
        BigDecimal d = nvl(done);
        BigDecimal t = nvl(total);
        if (t.compareTo(ZERO) <= 0) {
            return d.compareTo(ZERO) > 0 ? 20 : 0;
        }
        if (d.compareTo(ZERO) <= 0) {
            return 0;
        }
        if (d.compareTo(t) >= 0) {
            return 20;
        }
        return 10;
    }

    /**
     * 确认预收款后驱动主状态：30 → 40(若预付媒体) / 50(否则)。
     */
    private void transitAfterPrepayConfirm(AdOrder order, String userId, String orgId) {
        int from = order.getStatus();
        if (from != OrderStateMachine.PENDING_PREPAY_CONFIRM) {
            return;
        }
        boolean needMedia = order.getPaymentMethod() != null
                && order.getPaymentMethod() == PaymentMethod.PREPAY_MEDIA.getCode();
        int to = needMedia ? OrderStateMachine.PENDING_MEDIA_PREPAY : OrderStateMachine.EXECUTING;
        if (!OrderStateMachine.canTransit(from, to)) {
            throw new GenericException("确认预收款后状态流转不合法: " + from + " -> " + to);
        }
        order.setStatus(to);
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        adOrderMapper.update(order);
        recordOrderLog(order, needMedia ? OrderStateMachine.TRIGGER_PREPAY_CONFIRMED_MEDIA
                : OrderStateMachine.TRIGGER_PREPAY_CONFIRMED_EXECUTE, from, to, userId, orgId);
    }

    /**
     * 付媒体预付款后驱动主状态：40 → 50。
     */
    private void transitAfterMediaPrepay(AdOrder order, String userId, String orgId) {
        int from = order.getStatus();
        if (from != OrderStateMachine.PENDING_MEDIA_PREPAY) {
            return;
        }
        int to = OrderStateMachine.EXECUTING;
        if (!OrderStateMachine.canTransit(from, to)) {
            throw new GenericException("付媒体预付款后状态流转不合法: " + from + " -> " + to);
        }
        order.setStatus(to);
        order.setUpdateUser(userId);
        order.setUpdateTime(System.currentTimeMillis());
        adOrderMapper.update(order);
        recordOrderLog(order, OrderStateMachine.TRIGGER_MEDIA_PREPAY_PAID, from, to, userId, orgId);
    }

    private AdOrder requireOrder(String id) {
        AdOrder order = adOrderMapper.selectByPrimaryKey(id);
        if (order == null || (order.getDeleted() != null && order.getDeleted() == 1)) {
            throw new GenericException("订单不存在");
        }
        return order;
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

    private List<AdOrder> queryTodoOrders(String orgId, List<String> entityIds, String businessEntityId, String keyword) {
        List<Integer> statusList = List.of(
                OrderStateMachine.PENDING_PREPAY_CONFIRM,
                OrderStateMachine.PENDING_MEDIA_PREPAY,
                OrderStateMachine.EXECUTION_COMPLETED,
                OrderStateMachine.SETTLEMENT);
        List<AdOrder> list1 = adOrderMapper.selectTodoOrders(orgId, statusList, entityIds, businessEntityId, keyword);
        List<AdOrder> list2 = adOrderMapper.selectRedInvoiceOrders(orgId, entityIds, businessEntityId, keyword);

        Map<String, AdOrder> merged = new LinkedHashMap<>();
        for (AdOrder o : list1) {
            merged.put(o.getId(), o);
        }
        for (AdOrder o : list2) {
            merged.put(o.getId(), o);
        }
        return new ArrayList<>(merged.values());
    }

    private void buildTodos(AdOrder o, List<AdPaymentTodoResponse> out) {
        if (o.getStatus() != null && o.getStatus() == OrderStateMachine.PENDING_PREPAY_CONFIRM) {
            out.add(todo("PREPAY_CONFIRM", "待确认预收款", o, o.getReceiptPrepayAmount(), null));
        }
        if (o.getStatus() != null && o.getStatus() == OrderStateMachine.PENDING_MEDIA_PREPAY) {
            out.add(todo("MEDIA_PREPAY", "待付媒体预付款", o, o.getPaymentPrepayAmount(), null));
        }
        if (o.getNeedsRedInvoice() != null && o.getNeedsRedInvoice() == 1) {
            out.add(todo("RED_INVOICE", "红冲待办", o, null, null));
        }
        if (o.getPaymentMethod() != null && o.getPaymentMethod() == PaymentMethod.POSTPAY_MEDIA.getCode()
                && (o.getMediaPaymentStatus() == null || o.getMediaPaymentStatus() != 20)
                && (o.getStatus() == OrderStateMachine.EXECUTION_COMPLETED
                || o.getStatus() == OrderStateMachine.SETTLEMENT)) {
            BigDecimal remain = nvl(o.getMediaPayableAmount()).subtract(nvl(o.getMediaPaidAmount()));
            out.add(todo("MEDIA_POSTPAY", "媒体尾款待付", o, remain, o.getExecutionCompletedAt()));
        }
        if (o.getStatus() != null && o.getStatus() == OrderStateMachine.SETTLEMENT
                && (o.getInvoiceStatus() == null || o.getInvoiceStatus() != 20)) {
            out.add(todo("INVOICE_RECEIVE", "开票/收款待处理", o,
                    nvl(o.getReceivableAmount()).subtract(nvl(o.getInvoicedAmount())), null));
        }
    }

    private AdPaymentTodoResponse todo(String type, String label, AdOrder o, BigDecimal amount, Date dueDate) {
        AdPaymentTodoResponse t = new AdPaymentTodoResponse();
        t.setTodoType(type);
        t.setTodoLabel(label);
        t.setOrderId(o.getId());
        t.setOrderNo(o.getOrderNo());
        t.setBusinessEntityId(o.getBusinessEntityId());
        t.setCustomerId(o.getCustomerId());
        t.setStatus(o.getStatus());
        t.setAmount(amount);
        t.setDueDate(dueDate);
        t.setCreateTime(o.getCreateTime());
        return t;
    }

    // ===================== 角色守卫（best-effort，同 M2/M3） =====================

    private void assertFinanceRole() {
        if (!hasRole(OrderStateMachine.ROLE_FINANCE)) {
            throw new GenericException("当前角色无权执行收付款操作: " + OrderStateMachine.ROLE_FINANCE);
        }
    }

    private boolean hasRole(String requiredRole) {
        if (requiredRole == null || OrderStateMachine.ROLE_SYSTEM.equals(requiredRole)) {
            return true;
        }
        SessionUser user = SessionUtils.getUser();
        if (user == null) {
            return false;
        }
        List<String> roleNames = user.getRoles() == null ? Collections.emptyList()
                : user.getRoles().stream()
                .map(RoleDataScopeDTO::getName)
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase())
                .collect(Collectors.toList());
        boolean isAdmin = roleNames.stream()
                .anyMatch(n -> n.contains("admin") || n.contains("超级") || n.contains("管理员"));
        switch (requiredRole) {
            case OrderStateMachine.ROLE_MEDIA:
                return isAdmin || roleNames.stream()
                        .anyMatch(n -> n.contains("媒体") || n.contains("media") || n.contains("运营"));
            case OrderStateMachine.ROLE_BOSS:
                return isAdmin || roleNames.stream()
                        .anyMatch(n -> n.contains("老板") || n.contains("boss"));
            case OrderStateMachine.ROLE_FINANCE:
                return isAdmin || roleNames.stream()
                        .anyMatch(n -> n.contains("财务") || n.contains("finance"));
            default:
                return false;
        }
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? ZERO : v;
    }
}
