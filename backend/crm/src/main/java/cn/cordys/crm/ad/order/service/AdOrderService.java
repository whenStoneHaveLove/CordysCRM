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
import cn.cordys.crm.ad.order.dto.request.AdOrderCopyRequest;
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
import cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMedia;
import cn.cordys.crm.ad.downstreammedia.mapper.ExtAdDownstreamMediaMapper;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
 *   <li>L-13 强制归档：管理组执行，带坏账金额。</li>
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
    private ExtAdDownstreamMediaMapper downstreamMediaMapper;
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
        // 派生列(实际应付/应付返点/订单收入)在 syncOrderDownstreamMedia 中按下游明细累加后由 update 写回，
        // 此处 insert 前先置 0，避免 NOT NULL 列传入 null 报错（DDL 为 NOT NULL DEFAULT 0）。
        if (order.getActualMediaPayableAmount() == null) {
            order.setActualMediaPayableAmount(BigDecimal.ZERO);
        }
        if (order.getMediaRebateAmount() == null) {
            order.setMediaRebateAmount(BigDecimal.ZERO);
        }
        if (order.getOrderIncomeAmount() == null) {
            order.setOrderIncomeAmount(BigDecimal.ZERO);
        }
        adOrderMapper.insert(order);
        // 关联合同：框架订单必选框架合同；单笔订单可后补
        syncOrderContract(order.getId(), request.getContractId(), request.getOrderType(), userId, orgId);
        // 关联下游客户
        syncOrderDownstreamMedia(order, request.getDownstreamMediaIds(),
                request.getDownstreamMediaPayables(), userId, orgId);
        return order;
    }

    /**
     * 复制字段 key（与前端勾选项一一对应）。
     *
     * <p>默认勾选 {@link #DEFAULT_FIELDS}：业务主体、订单类型、客户、上游代理、
     * 下游客户、返点方式、收款方式、账期天数。</p>
     */
    public static final class CopyField {
        /** 业务主体 */
        public static final String BUSINESS_ENTITY_ID = "businessEntityId";
        /** 订单类型 */
        public static final String ORDER_TYPE = "orderType";
        /** 客户 */
        public static final String CUSTOMER_ID = "customerId";
        /** 上游代理 */
        public static final String UPSTREAM_AGENT_ID = "upstreamAgentId";
        /** 下游客户（含付款/返点明细） */
        public static final String DOWNSTREAM_MEDIA = "downstreamMedia";
        /** 返点方式 */
        public static final String REBATE_MODE = "rebateMode";
        /** 返点值 */
        public static final String REBATE_VALUE = "rebateValue";
        /** 收款方式 */
        public static final String RECEIPT_METHOD = "receiptMethod";
        /** 账期天数 */
        public static final String RECEIPT_ACCOUNT_PERIOD_DAYS = "receiptAccountPeriodDays";
        /** 行业类别 */
        public static final String INDUSTRY_CODE = "industryCode";
        /** 签约主体 */
        public static final String SIGNING_ENTITY = "signingEntity";
        /** 代理订单号 */
        public static final String AGENT_ORDER_NO = "agentOrderNo";
        /** 订单金额 */
        public static final String TOTAL_AMOUNT = "totalAmount";
        /** 不记返金额 */
        public static final String NO_REBATE_AMOUNT = "noRebateAmount";
        /** 投放起始日 */
        public static final String DELIVERY_START_DATE = "deliveryStartDate";
        /** 投放结束日 */
        public static final String DELIVERY_END_DATE = "deliveryEndDate";
        /** 投放量 */
        public static final String DELIVERY_VOLUME = "deliveryVolume";
        /** 备注 */
        public static final String REMARK = "remark";
        /** 预收模式 */
        public static final String RECEIPT_PREPAY_MODE = "receiptPrepayMode";
        /** 预收比例 */
        public static final String RECEIPT_PREPAY_RATIO = "receiptPrepayRatio";
        /** 预收金额 */
        public static final String RECEIPT_PREPAY_AMOUNT = "receiptPrepayAmount";
        /** 预收截止日 */
        public static final String RECEIPT_PREPAY_DEADLINE = "receiptPrepayDeadline";
        /** 付款方式 */
        public static final String PAYMENT_METHOD = "paymentMethod";
        /** 预付模式 */
        public static final String PAYMENT_PREPAY_MODE = "paymentPrepayMode";
        /** 预付比例 */
        public static final String PAYMENT_PREPAY_RATIO = "paymentPrepayRatio";
        /** 预付金额 */
        public static final String PAYMENT_PREPAY_AMOUNT = "paymentPrepayAmount";
        /** 预付截止日 */
        public static final String PAYMENT_PREPAY_DEADLINE = "paymentPrepayDeadline";
        /** 后付触发条件 */
        public static final String PAYMENT_POSTPAY_TRIGGER = "paymentPostpayTrigger";
        /** 后付天数 */
        public static final String PAYMENT_POSTPAY_DAYS = "paymentPostpayDays";
        /** 关联合同 */
        public static final String CONTRACT_ID = "contractId";
        /** 扩展字段 */
        public static final String EXT_JSON = "extJson";

        private CopyField() {
        }
    }

    /** 复制弹窗默认勾选的字段。 */
    public static final List<String> DEFAULT_COPY_FIELDS = List.of(
            CopyField.BUSINESS_ENTITY_ID,
            CopyField.ORDER_TYPE,
            CopyField.CUSTOMER_ID,
            CopyField.UPSTREAM_AGENT_ID,
            CopyField.DOWNSTREAM_MEDIA,
            CopyField.REBATE_MODE,
            CopyField.RECEIPT_METHOD,
            CopyField.RECEIPT_ACCOUNT_PERIOD_DAYS);

    /**
     * 复制订单：按勾选字段生成一张新的<b>草稿</b>订单。
     *
     * <p>规则：</p>
     * <ul>
     *   <li>订单名称为「源订单名称-复制」（可用 {@code request.orderName} 覆盖）；</li>
     *   <li>仅复制勾选字段，其余业务列一律为空，金额派生列按 0 写入；</li>
     *   <li>下游客户勾选时同时复制付款/返点明细，并由明细重算应付相关派生列；</li>
     *   <li>关联合同勾选时复制源订单的合同；</li>
     *   <li>新订单状态固定为草稿(0)，编号按业务主体 + 当天流水重新生成。</li>
     * </ul>
     *
     * @param request 复制请求（源订单 id + 勾选字段）
     * @param userId  操作人
     * @param orgId   组织 id
     * @return 新建的草稿订单
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(module = "AD_ORDER", action = "COPY", targetId = "")
    public AdOrder copy(AdOrderCopyRequest request, String userId, String orgId) {
        if (request == null || request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("请选择要复制的订单");
        }
        AdOrder source = requireOrder(request.getId());
        Set<String> fields = request.getFields() == null
                ? Collections.emptySet()
                : request.getFields().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(field -> !field.isEmpty())
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        long now = System.currentTimeMillis();
        AdOrder order = new AdOrder();
        order.setId(IDGenerator.nextStr());
        order.setOrganizationId(orgId);
        order.setCreatorId(userId);
        order.setStatus(OrderStateMachine.DRAFT);
        order.setCreateUser(userId);
        order.setUpdateUser(userId);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setDeleted(0);
        order.setOrderName(resolveCopyOrderName(source, request.getOrderName()));

        // ---- 按勾选复制业务列，未勾选一律保持 null ----
        if (fields.contains(CopyField.BUSINESS_ENTITY_ID)) {
            order.setBusinessEntityId(source.getBusinessEntityId());
        }
        if (fields.contains(CopyField.ORDER_TYPE)) {
            order.setOrderType(source.getOrderType());
        }
        if (fields.contains(CopyField.CUSTOMER_ID)) {
            order.setCustomerId(source.getCustomerId());
        }
        if (fields.contains(CopyField.UPSTREAM_AGENT_ID)) {
            order.setUpstreamAgentId(source.getUpstreamAgentId());
        }
        if (fields.contains(CopyField.INDUSTRY_CODE)) {
            order.setIndustryCode(source.getIndustryCode());
        }
        if (fields.contains(CopyField.SIGNING_ENTITY)) {
            order.setSigningEntity(source.getSigningEntity());
        }
        if (fields.contains(CopyField.AGENT_ORDER_NO)) {
            order.setAgentOrderNo(source.getAgentOrderNo());
        }
        if (fields.contains(CopyField.TOTAL_AMOUNT)) {
            order.setTotalAmount(source.getTotalAmount());
        }
        if (fields.contains(CopyField.REBATE_MODE)) {
            order.setRebateMode(source.getRebateMode());
        }
        if (fields.contains(CopyField.REBATE_VALUE)) {
            order.setRebateValue(source.getRebateValue());
        }
        if (fields.contains(CopyField.NO_REBATE_AMOUNT)) {
            order.setNoRebateAmount(source.getNoRebateAmount());
        }
        if (fields.contains(CopyField.DELIVERY_START_DATE)) {
            order.setDeliveryStartDate(source.getDeliveryStartDate());
        }
        if (fields.contains(CopyField.DELIVERY_END_DATE)) {
            order.setDeliveryEndDate(source.getDeliveryEndDate());
        }
        if (fields.contains(CopyField.DELIVERY_VOLUME)) {
            order.setDeliveryVolume(source.getDeliveryVolume());
        }
        if (fields.contains(CopyField.REMARK)) {
            order.setRemark(source.getRemark());
        }
        if (fields.contains(CopyField.RECEIPT_METHOD)) {
            order.setReceiptMethod(source.getReceiptMethod());
        }
        if (fields.contains(CopyField.RECEIPT_PREPAY_MODE)) {
            order.setReceiptPrepayMode(source.getReceiptPrepayMode());
        }
        if (fields.contains(CopyField.RECEIPT_PREPAY_RATIO)) {
            order.setReceiptPrepayRatio(source.getReceiptPrepayRatio());
        }
        if (fields.contains(CopyField.RECEIPT_PREPAY_AMOUNT)) {
            order.setReceiptPrepayAmount(source.getReceiptPrepayAmount());
        }
        if (fields.contains(CopyField.RECEIPT_PREPAY_DEADLINE)) {
            order.setReceiptPrepayDeadline(source.getReceiptPrepayDeadline());
        }
        if (fields.contains(CopyField.RECEIPT_ACCOUNT_PERIOD_DAYS)) {
            order.setReceiptAccountPeriodDays(source.getReceiptAccountPeriodDays());
        }
        if (fields.contains(CopyField.PAYMENT_METHOD)) {
            order.setPaymentMethod(source.getPaymentMethod());
        }
        if (fields.contains(CopyField.PAYMENT_PREPAY_MODE)) {
            order.setPaymentPrepayMode(source.getPaymentPrepayMode());
        }
        if (fields.contains(CopyField.PAYMENT_PREPAY_RATIO)) {
            order.setPaymentPrepayRatio(source.getPaymentPrepayRatio());
        }
        if (fields.contains(CopyField.PAYMENT_PREPAY_AMOUNT)) {
            order.setPaymentPrepayAmount(source.getPaymentPrepayAmount());
        }
        if (fields.contains(CopyField.PAYMENT_PREPAY_DEADLINE)) {
            order.setPaymentPrepayDeadline(source.getPaymentPrepayDeadline());
        }
        if (fields.contains(CopyField.PAYMENT_POSTPAY_TRIGGER)) {
            order.setPaymentPostpayTrigger(source.getPaymentPostpayTrigger());
        }
        if (fields.contains(CopyField.PAYMENT_POSTPAY_DAYS)) {
            order.setPaymentPostpayDays(source.getPaymentPostpayDays());
        }
        if (fields.contains(CopyField.EXT_JSON)) {
            order.setExtJson(source.getExtJson());
        }

        order.setOrderNo(generateOrderNo(order.getBusinessEntityId(), orgId, now));
        amountCalculator.computeAmounts(order);
        // 未勾选下游客户时不存在下游明细，应付口径派生列必须为 0（computeAmounts 会用总额兜底）
        if (!fields.contains(CopyField.DOWNSTREAM_MEDIA)) {
            order.setMediaPayableAmount(BigDecimal.ZERO);
            order.setActualMediaPayableAmount(BigDecimal.ZERO);
            order.setMediaRebateAmount(BigDecimal.ZERO);
        }
        if (order.getActualMediaPayableAmount() == null) {
            order.setActualMediaPayableAmount(BigDecimal.ZERO);
        }
        if (order.getMediaRebateAmount() == null) {
            order.setMediaRebateAmount(BigDecimal.ZERO);
        }
        if (order.getOrderIncomeAmount() == null) {
            order.setOrderIncomeAmount(BigDecimal.ZERO);
        }
        adOrderMapper.insert(order);

        // 关联合同：勾选「关联合同」才复制源订单的合同
        String contractId = null;
        if (fields.contains(CopyField.CONTRACT_ID)) {
            List<AdOrderContract> sourceContracts = orderContractMapper.selectByOrderId(source.getId());
            contractId = sourceContracts.isEmpty() ? null : sourceContracts.get(0).getContractId();
        }
        syncOrderContract(order.getId(), contractId, order.getOrderType(), userId, orgId);

        // 下游客户：勾选「下游客户」才复制关联关系与付款/返点明细
        if (fields.contains(CopyField.DOWNSTREAM_MEDIA)) {
            List<AdOrderDownstreamMedia> sourceMedias =
                    orderDownstreamMediaMapper.selectByOrderId(source.getId());
            List<String> mediaIds = new ArrayList<>();
            List<AdOrderSaveRequest.DownstreamMediaPayableDTO> payables = new ArrayList<>();
            for (AdOrderDownstreamMedia odm : sourceMedias) {
                if (odm.getDownstreamMediaId() == null || odm.getDownstreamMediaId().isBlank()) {
                    continue;
                }
                mediaIds.add(odm.getDownstreamMediaId());
                AdOrderSaveRequest.DownstreamMediaPayableDTO dto =
                        new AdOrderSaveRequest.DownstreamMediaPayableDTO();
                dto.setDownstreamMediaId(odm.getDownstreamMediaId());
                dto.setPayableAmount(odm.getPayableAmount());
                dto.setNoRebateAmount(odm.getNoRebateAmount());
                dto.setRebateMode(odm.getRebateMode());
                dto.setRebateValue(odm.getRebateValue());
                dto.setPaymentMethod(odm.getPaymentMethod());
                dto.setPaymentPrepayMode(odm.getPaymentPrepayMode());
                dto.setPaymentPrepayRatio(odm.getPaymentPrepayRatio());
                dto.setPaymentPrepayDeadline(odm.getPaymentPrepayDeadline());
                dto.setPaymentPostpayTrigger(odm.getPaymentPostpayTrigger());
                dto.setPaymentPostpayDays(odm.getPaymentPostpayDays());
                payables.add(dto);
            }
            syncOrderDownstreamMedia(order, mediaIds, payables, userId, orgId);
        }
        return order;
    }

    /**
     * 复制订单名称：默认「源订单名称-复制」，源名称为空时退化为「复制订单」。
     */
    private String resolveCopyOrderName(AdOrder source, String requestName) {
        if (requestName != null && !requestName.isBlank()) {
            return requestName.trim();
        }
        String baseName = source.getOrderName() == null ? "" : source.getOrderName().trim();
        return baseName.isEmpty() ? "复制订单" : baseName + "-复制";
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
        // 同 create：派生列随后由 syncOrderDownstreamMedia 重算写回，此处先置 0 避免 NOT NULL 列收到 null
        if (order.getActualMediaPayableAmount() == null) {
            order.setActualMediaPayableAmount(BigDecimal.ZERO);
        }
        if (order.getMediaRebateAmount() == null) {
            order.setMediaRebateAmount(BigDecimal.ZERO);
        }
        if (order.getOrderIncomeAmount() == null) {
            order.setOrderIncomeAmount(BigDecimal.ZERO);
        }
        adOrderMapper.update(order);
        // 同步合同关联
        syncOrderContract(order.getId(), request.getContractId(), request.getOrderType(), userId, orgId);
        // 同步下游客户
        syncOrderDownstreamMedia(order, request.getDownstreamMediaIds(),
                request.getDownstreamMediaPayables(), userId, orgId);
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

        // 下游客户 id 列表
        List<AdOrderDownstreamMedia> downstreamMedias = orderDownstreamMediaMapper.selectByOrderId(id);
        List<String> downstreamMediaIds = downstreamMedias.stream()
                .map(AdOrderDownstreamMedia::getDownstreamMediaId)
                .collect(Collectors.toList());

        // 下游客户付款返点明细
        List<AdOrderDetailResponse.DownstreamMediaPayableVO> payableVos = downstreamMedias.stream()
                .map(odm -> {
                    AdOrderDetailResponse.DownstreamMediaPayableVO vo =
                            new AdOrderDetailResponse.DownstreamMediaPayableVO();
                    vo.setDownstreamMediaId(odm.getDownstreamMediaId());
                    AdDownstreamMedia dm = downstreamMediaMapper.selectByPrimaryKey(odm.getDownstreamMediaId());
                    vo.setDownstreamMediaName(dm == null ? null : dm.getName());
                    vo.setPayableAmount(odm.getPayableAmount());
                    vo.setNoRebateAmount(odm.getNoRebateAmount());
                    vo.setRebateMode(odm.getRebateMode());
                    vo.setRebateValue(odm.getRebateValue());
                    vo.setRebateAmount(odm.getRebateAmount());
                    vo.setActualPayable(odm.getActualPayable());
                    vo.setPaymentMethod(odm.getPaymentMethod());
                    vo.setPaymentPrepayMode(odm.getPaymentPrepayMode());
                    vo.setPaymentPrepayRatio(odm.getPaymentPrepayRatio());
                    vo.setPaymentPrepayAmount(odm.getPaymentPrepayAmount());
                    vo.setPaymentPrepayDeadline(odm.getPaymentPrepayDeadline());
                    vo.setPaymentPostpayTrigger(odm.getPaymentPostpayTrigger());
                    vo.setPaymentPostpayDays(odm.getPaymentPostpayDays());
                    return vo;
                })
                .collect(Collectors.toList());

        AdOrderDetailResponse response = new AdOrderDetailResponse();
        response.setOrder(order);
        response.setContractId(contractId);
        if (contractId != null) {
            AdContract contract = contractMapper.selectByPrimaryKey(contractId);
            if (contract != null) {
                response.setContractId(contract.getId());
                response.setContractNo(contract.getContractNo());
                response.setContractName(contract.getContractName());
            }
        }
        response.setDownstreamMediaIds(downstreamMediaIds);
        response.setDownstreamMediaPayables(payableVos);
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

    /**
     * 导出全量列表（与 {@link #page} 共用同一套筛选/主体隔离逻辑，仅绕过分页）。
     */
    public List<AdOrderListResponse> exportList(AdOrderPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        request.setCurrent(1);
        request.setPageSize(100000);
        List<AdOrderListResponse> list = extAdOrderMapper.pageList(request);
        for (AdOrderListResponse r : list) {
            r.setOrderTypeLabel(OrderType.labelOf(r.getOrderType()));
            r.setStatusLabel(OrderStatus.labelOf(r.getStatus()));
            r.setReceiptMethodLabel(ReceiptMethod.labelOf(r.getReceiptMethod()));
            r.setPaymentMethodLabel(PaymentMethod.labelOf(r.getPaymentMethod()));
        }
        return list;
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
     * 管理组审核：通过 10→45（待执行）；驳回 10→0（保留附件 L-21）。
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
     * 强制归档：80→90（管理组执行，L-13 带坏账金额）。
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
     * 同步订单与下游客户的关联到 ad_order_downstream_media 中间表。
     * 全量替换：先逻辑删除旧的关联，再插入新的关联。
     * 注意：由于唯一键 (order_id, downstream_media_id) 不区分 deleted，
     * 插入前先检查已存在记录（含已删除），若存在则复用（设 deleted=0）。
     */
    public void syncOrderDownstreamMedia(AdOrder order, List<String> downstreamMediaIds,
                                           List<AdOrderSaveRequest.DownstreamMediaPayableDTO> payables,
                                           String userId, String orgId) {
        String orderId = order.getId();
        // 先逻辑删除该订单的所有现有下游客户关联
        List<AdOrderDownstreamMedia> existing = orderDownstreamMediaMapper.selectByOrderId(orderId);
        for (AdOrderDownstreamMedia odm : existing) {
            odm.setDeleted(1);
            orderDownstreamMediaMapper.update(odm);
        }
        // 插入新的关联
        if (downstreamMediaIds == null || downstreamMediaIds.isEmpty()) {
            return;
        }
        // 明细按下游客户id建立索引，便于回填付款返点字段
        java.util.Map<String, AdOrderSaveRequest.DownstreamMediaPayableDTO> payableMap = new java.util.HashMap<>();
        if (payables != null) {
            for (AdOrderSaveRequest.DownstreamMediaPayableDTO dto : payables) {
                if (dto != null && dto.getDownstreamMediaId() != null) {
                    payableMap.put(dto.getDownstreamMediaId(), dto);
                }
            }
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
                fillPayableFields(reused, payableMap.get(mediaId));
                orderDownstreamMediaMapper.update(reused);
            } else {
                AdOrderDownstreamMedia odm = new AdOrderDownstreamMedia();
                odm.setId(IDGenerator.nextStr());
                odm.setOrderId(orderId);
                odm.setDownstreamMediaId(mediaId);
                odm.setOrganizationId(orgId);
                odm.setDeleted(0);
                odm.setCreateTime(now);
                fillPayableFields(odm, payableMap.get(mediaId));
                orderDownstreamMediaMapper.insert(odm);
            }
        }
        // 订单应付总额/实际应付/应付返点/订单收入/付款方式 由明细推导，复用统一方法
        applyIncomeFromPayables(order, payables);
        adOrderMapper.update(order);
    }

    /**
     * 根据下游客户付款明细列表，重算并回填订单主表：
     * 应付总额(mediaPayableAmount)、实际应付总额(actualMediaPayableAmount)、
     * 应付返点(mediaRebateAmount)、订单收入(orderIncomeAmount)、付款方式(paymentMethod)。
     * 仅做字段赋值，不执行数据库更新，由调用方负责 update。
     * 该方法为唯一计算来源，create/update 与历史数据批量补数均复用，保证逻辑一致。
     */
    private void applyIncomeFromPayables(AdOrder order, List<AdOrderSaveRequest.DownstreamMediaPayableDTO> payables) {
        // 订单应付总额 = 各下游客户应付金额之和（后端累加并写回主表）
        BigDecimal payableSum = BigDecimal.ZERO;
        // 订单实际应付总额(返点后) = 各下游客户 (应付金额 - 返点金额) 之和
        BigDecimal actualPayableSum = BigDecimal.ZERO;
        for (AdOrderSaveRequest.DownstreamMediaPayableDTO dto : (payables == null ? java.util.Collections.<AdOrderSaveRequest.DownstreamMediaPayableDTO>emptyList() : payables)) {
            if (dto == null) {
                continue;
            }
            if (dto.getPayableAmount() != null) {
                payableSum = payableSum.add(dto.getPayableAmount());
                // 与 fillPayableFields 同样的返点计算逻辑,保证主表与明细一致
                BigDecimal payableAmt = dto.getPayableAmount();
                BigDecimal noRebate = dto.getNoRebateAmount() == null ? BigDecimal.ZERO : dto.getNoRebateAmount();
                Integer rebateMode = dto.getRebateMode() == null ? 10 : dto.getRebateMode();
                BigDecimal rebateValue = dto.getRebateValue() == null ? BigDecimal.ZERO : dto.getRebateValue();
                BigDecimal rebateAmount = BigDecimal.ZERO;
                if (payableAmt != null) {
                    if (rebateMode == 10) {
                        BigDecimal base = payableAmt.subtract(noRebate);
                        if (base.compareTo(BigDecimal.ZERO) < 0) {
                            base = BigDecimal.ZERO;
                        }
                        rebateAmount = base.multiply(rebateValue)
                                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    } else if (rebateMode == 20) {
                        rebateAmount = rebateValue;
                    }
                }
                actualPayableSum = actualPayableSum.add(payableAmt.subtract(rebateAmount));
            }
        }
        order.setMediaPayableAmount(payableSum);
        order.setActualMediaPayableAmount(actualPayableSum);
        // 应付返点 = 应付金额 - 实际应付
        order.setMediaRebateAmount(payableSum.subtract(actualPayableSum));
        // 订单收入 = 实际应收 - 实际应付
        BigDecimal actualReceivable = order.getReceivableAmount() == null ? BigDecimal.ZERO : order.getReceivableAmount();
        order.setOrderIncomeAmount(actualReceivable.subtract(actualPayableSum));
        // 订单付款方式由下游客户明细推导：存在任一客户为预付(10)则订单为预付；
        // 仅当所有客户均为后付(20)时订单才为后付；无客户明细则为 null。
        Integer derivedPaymentMethod = null;
        java.util.List<AdOrderSaveRequest.DownstreamMediaPayableDTO> payableList =
                payables == null ? java.util.Collections.<AdOrderSaveRequest.DownstreamMediaPayableDTO>emptyList() : payables;
        if (!payableList.isEmpty()) {
            boolean allPostpay = true;
            boolean anyPrepay = false;
            for (AdOrderSaveRequest.DownstreamMediaPayableDTO dto : payableList) {
                if (dto == null) {
                    continue;
                }
                Integer pm = dto.getPaymentMethod();
                if (Integer.valueOf(10).equals(pm)) {
                    anyPrepay = true;
                    allPostpay = false;
                    break;
                } else if (Integer.valueOf(20).equals(pm)) {
                    allPostpay = true;
                } else {
                    allPostpay = false;
                }
            }
            if (anyPrepay) {
                derivedPaymentMethod = 10;
            } else if (allPostpay) {
                derivedPaymentMethod = 20;
            }
        }
        order.setPaymentMethod(derivedPaymentMethod);
    }

    /**
     * 仅根据订单主表已有金额字段，计算并回填「应付返点」「订单收入」两个派生列。
     * 不重算 mediaPayableAmount/actualMediaPayableAmount（保留主表原值，避免无下游明细时把主表金额错误地置 0）。
     * 用于历史数据批量补数场景：主表原有两个应付字段已正确，仅需补出新增的派生字段。
     * 公式：
     *   应付返点 = mediaPayableAmount - actualMediaPayableAmount
     *   订单收入 = receivableAmount    - actualMediaPayableAmount
     * 任意一侧为 null 时按 0 计，结果仍为 null 时不写（保留 null 便于排查）。
     */
    /**
     * 补出三列（不动主表其他金额字段，与需求口径一致）：
     *   实际应付 = 各下游明细实际应付之和（实际应付以明细 actual_payable 列为优先；该列为 null 的旧数据
     *             用「应付金额 - 返点金额」即时兜底，与 fillPayableFields 写库逻辑一致，与前端明细行/底部累加展示一致）
     *   应付返点 = 主表应付金额(mediaPayableAmount) - 实际应付
     *   订单收入 = 实际应收(receivableAmount) - 实际应付
     */
    private void applyDerivedOnlyFromOrder(AdOrder order, List<AdOrderDownstreamMedia> details) {
        BigDecimal actualPayableSum = BigDecimal.ZERO;
        if (details != null) {
            for (AdOrderDownstreamMedia d : details) {
                if (d == null) {
                    continue;
                }
                BigDecimal actual = d.getActualPayable();
                if (actual == null) {
                    // 历史数据该列为 null 时，按「应付金额 - 返点金额」兜底（与 fillPayableFields 写库公式一致）
                    BigDecimal payable = d.getPayableAmount();
                    BigDecimal rebate = d.getRebateAmount() == null ? BigDecimal.ZERO : d.getRebateAmount();
                    if (payable != null) {
                        // 负值如实保留（订单收入=应收-实际应付可能为负，按用户要求直接展示负值）
                        actual = payable.subtract(rebate);
                    }
                }
                if (actual != null) {
                    actualPayableSum = actualPayableSum.add(actual);
                }
            }
        }
        order.setActualMediaPayableAmount(actualPayableSum);
        if (order.getMediaPayableAmount() != null) {
            order.setMediaRebateAmount(order.getMediaPayableAmount().subtract(actualPayableSum));
        }
        if (order.getReceivableAmount() != null) {
            order.setOrderIncomeAmount(order.getReceivableAmount().subtract(actualPayableSum));
        }
    }

    /**
     * 历史数据批量补数：重算所有订单的应付/实际应付/应付返点/订单收入/付款方式。
     * 从 ad_order_downstream_media 读取各订单已有的下游明细（不依赖前端 request），
     * 复用 applyIncomeFromPayable 的统一计算逻辑，逐单 updateById 写回主表。
     * 幂等：重复执行结果一致。仅供数据修复时手动触发一次。
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public int recomputeIncomeFieldsForAll() {
        // 走 XML 显式列的 selectAllNonDeleted：框架默认 select(E criteria) 会把 int 默认 0 当 WHERE 条件，
        // 导致只有草稿（status=0）订单被匹配，其余状态订单都被滤掉。这里只过滤 deleted=0。
        List<AdOrder> orders = extAdOrderMapper.selectAllNonDeleted();
        log.info("[recompute-income] 候选订单数: {}", orders.size());
        int count = 0;
        for (AdOrder order : orders) {
            // 补数仅写回「实际应付 / 应付返点 / 订单收入」三列：从下游明细 actual_payable 累加 = 实际应付；
            // 应付返点 = 主表应付金额 - 实际应付；订单收入 = 应收 - 实际应付
            List<AdOrderDownstreamMedia> details = orderDownstreamMediaMapper.selectByOrderId(order.getId());
            applyDerivedOnlyFromOrder(order, details);
            Integer affected =             adOrderMapper.updateById(order);
            count++;
        }
        log.info("[recompute-income] 累计处理订单数: {}", count);
        return count;
    }

    /** 把前端传来的付款返点明细写入关联行，并自动计算 rebateAmount/actualPayable */
    private void fillPayableFields(AdOrderDownstreamMedia odm, AdOrderSaveRequest.DownstreamMediaPayableDTO dto) {
        if (dto == null) {
            return;
        }
        BigDecimal payable = dto.getPayableAmount();
        BigDecimal noRebate = dto.getNoRebateAmount() == null ? BigDecimal.ZERO : dto.getNoRebateAmount();
        Integer rebateMode = dto.getRebateMode() == null ? 10 : dto.getRebateMode();
        BigDecimal rebateValue = dto.getRebateValue() == null ? BigDecimal.ZERO : dto.getRebateValue();
        odm.setPayableAmount(payable);
        odm.setNoRebateAmount(dto.getNoRebateAmount());
        odm.setRebateMode(rebateMode);
        odm.setRebateValue(rebateValue);
        // 自动计算返点金额与实际应付
        BigDecimal rebateAmount = BigDecimal.ZERO;
        if (rebateMode != null && rebateValue != null) {
            if (rebateMode == 10) {
                BigDecimal base = (payable == null ? BigDecimal.ZERO : payable)
                        .subtract(noRebate);
                if (base.compareTo(BigDecimal.ZERO) < 0) {
                    base = BigDecimal.ZERO;
                }
                rebateAmount = base.multiply(rebateValue)
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            } else if (rebateMode == 20) {
                rebateAmount = rebateValue;
            }
        }
        odm.setRebateAmount(rebateAmount);
        BigDecimal actual = (payable == null ? BigDecimal.ZERO : payable).subtract(rebateAmount);
        odm.setActualPayable(actual);
        // 付款方式（下放到每个客户），预付金额基数=该客户应付金额
        odm.setPaymentMethod(dto.getPaymentMethod());
        odm.setPaymentPrepayMode(dto.getPaymentPrepayMode());
        odm.setPaymentPrepayRatio(dto.getPaymentPrepayRatio());
        odm.setPaymentPrepayDeadline(dto.getPaymentPrepayDeadline());
        odm.setPaymentPostpayTrigger(dto.getPaymentPostpayTrigger());
        odm.setPaymentPostpayDays(dto.getPaymentPostpayDays());
        BigDecimal prepayAmount = null;
        if (dto.getPaymentMethod() != null && dto.getPaymentMethod() == 10
                && dto.getPaymentPrepayMode() != null && payable != null) {
            if (dto.getPaymentPrepayMode() == 10 && dto.getPaymentPrepayRatio() != null) {
                prepayAmount = payable.multiply(dto.getPaymentPrepayRatio())
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            } else if (dto.getPaymentPrepayMode() == 20 && dto.getPaymentPrepayDeadline() != null
                    && dto.getPaymentPrepayRatio() != null) {
                // 固定金额模式用 paymentPrepayRatio 列承载固定金额值
                prepayAmount = dto.getPaymentPrepayRatio();
            }
        }
        odm.setPaymentPrepayAmount(prepayAmount);
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
        // 业务主体为空时（复制订单未勾选业务主体）必须走 IS NULL 统计，
        // 否则 business_entity_id = NULL 恒不成立，流水号永远从 001 开始。
        long todayCount = (businessEntityId == null || businessEntityId.isBlank())
                ? extAdOrderMapper.countTodayOrdersWithoutEntity(startOfDay(now), endOfDay(now), orgId)
                : extAdOrderMapper.countTodayOrders(businessEntityId, startOfDay(now), endOfDay(now), orgId);
        long seq = todayCount + 1;
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
