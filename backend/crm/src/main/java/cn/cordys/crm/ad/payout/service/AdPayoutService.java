package cn.cordys.crm.ad.payout.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.contract.domain.AdContract;
import cn.cordys.crm.ad.contract.dto.response.AdContractBriefResponse;
import cn.cordys.crm.ad.contract.mapper.ExtAdContractMapper;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.domain.AdOrderContract;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderContractMapper;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderMapper;
import cn.cordys.crm.ad.payout.constants.PayoutBillType;
import cn.cordys.crm.ad.payout.constants.PayoutStatus;
import cn.cordys.crm.ad.payout.constants.PayoutType;
import cn.cordys.crm.ad.payout.domain.AdPayout;
import cn.cordys.crm.ad.payout.domain.AdPaymentMedia;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutMediaDetail;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutApproveRequest;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutPayRequest;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutPageRequest;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutSaveRequest;
import cn.cordys.crm.ad.payout.dto.response.AdPayoutDetailResponse;
import cn.cordys.crm.ad.payout.dto.response.AdPayoutListResponse;
import cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaDetailItem;
import cn.cordys.crm.ad.payout.mapper.AdPaymentMediaMapper;
import cn.cordys.crm.ad.payout.mapper.ExtAdPayoutMapper;
import cn.cordys.crm.ad.payout.mapper.ExtAdPayoutMediaOptionMapper;
import cn.cordys.mybatis.BaseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 广告付款单服务。
 *
 * <p>一个订单对应一个付款单，可勾选多个（默认全部）。流程：
 * 新建(草稿)→保存→编辑→提交(待审核)→审核通过(待付款)/驳回(草稿)→付款(已付款)。
 * 审核通过只更新付款单状态；付款动作才会回写订单 media_paid_amount 并置 payment_done=1。</p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdPayoutService {

    @Resource
    private BaseMapper<AdPayout> payoutMapper;
    @Resource
    private ExtAdPayoutMapper extAdPayoutMapper;
    @Resource
    private ExtAdOrderMapper extAdOrderMapper;
    @Resource
    private ExtAdPayoutMediaOptionMapper extAdPayoutMediaOptionMapper;
    @Resource
    private ExtAdOrderContractMapper orderContractMapper;
    @Resource
    private ExtAdContractMapper extAdContractMapper;
    @Resource
    private AdPaymentMediaMapper adPaymentMediaMapper;
    @Resource
    private cn.cordys.crm.ad.payout.mapper.ExtAdPaymentMediaMapper extAdPaymentMediaMapper;
    @Resource
    private cn.cordys.crm.ad.downstreammedia.mapper.ExtAdDownstreamMediaAccountMapper extAccountMapper;

    private static final ObjectMapper JSON = new ObjectMapper();

    /** 新建（草稿状态）。 */
    @OperationLog(module = "AD_PAYOUT", action = "CREATE", targetId = "")
    public AdPayout create(AdPayoutSaveRequest request, String userId, String orgId) {
        validate(request);
        assertNoExistingPayout(request);

        AdPayout p = new AdPayout();
        p.setId(IDGenerator.nextStr());
        p.setPaymentNo(genNo());
        p.setBillType(PayoutBillType.ofOrDefault(request.getBillType()).getCode());
        p.setOrderId(request.getOrderId());
        p.setAmount(request.getAmount());
        p.setPaymentTime(request.getPaymentTime());
        p.setType(request.getType() != null ? request.getType() : PayoutType.NORMAL.getCode());
        p.setMediaIds(toJson(request.getMediaIds()));
        p.setVoucherUrl(request.getVoucherUrl());
        p.setRemark(request.getRemark());
        p.setStatus(PayoutStatus.DRAFT.getCode());
        p.setOrganizationId(orgId);
        p.setCreateUser(userId);
        p.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        p.setCreateTime(now);
        p.setUpdateTime(now);
        payoutMapper.insert(p);
        saveMediaDetails(p, request.getMediaDetails(), userId, orgId, now);
        return p;
    }

    /** 编辑（仅草稿状态可编辑）。 */
    @OperationLog(module = "AD_PAYOUT", action = "UPDATE", targetId = "#request.id")
    public AdPayout update(AdPayoutSaveRequest request, String userId, String orgId) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("付款单id不能为空");
        }
        validate(request);
        AdPayout p = requirePayout(request.getId());
        if (p.getStatus() != PayoutStatus.DRAFT.getCode()) {
            throw new GenericException("仅草稿状态的付款单可编辑");
        }
        p.setBillType(PayoutBillType.ofOrDefault(request.getBillType()).getCode());
        p.setOrderId(request.getOrderId());
        p.setAmount(request.getAmount());
        p.setPaymentTime(request.getPaymentTime());
        if (request.getType() != null) {
            p.setType(request.getType());
        }
        p.setMediaIds(toJson(request.getMediaIds()));
        p.setVoucherUrl(request.getVoucherUrl());
        p.setRemark(request.getRemark());
        p.setUpdateUser(userId);
        p.setUpdateTime(System.currentTimeMillis());
        payoutMapper.update(p);
        // 先清后写明细
        extAdPaymentMediaMapper.deleteByPaymentId(p.getId());
        saveMediaDetails(p, request.getMediaDetails(), userId, orgId, p.getUpdateTime());
        return p;
    }

    /** 提交（草稿 → 待审核）。 */
    @OperationLog(module = "AD_PAYOUT", action = "SUBMIT", targetId = "#id")
    public AdPayout submit(String id, String userId, String orgId) {
        AdPayout p = requirePayout(id);
        if (p.getStatus() != PayoutStatus.DRAFT.getCode()) {
            throw new GenericException("仅草稿状态的付款单可提交");
        }
        p.setStatus(PayoutStatus.PENDING_APPROVAL.getCode());
        p.setUpdateUser(userId);
        p.setUpdateTime(System.currentTimeMillis());
        payoutMapper.update(p);
        return p;
    }

    /**
     * 审核通过/驳回。
     * 通过：仅更新付款单状态为「待付款」，不回写订单金额——订单金额由「付款」动作回写。
     * 驳回：直接回到草稿状态（去掉中间态），可重新编辑/提交。
     */
    @OperationLog(module = "AD_PAYOUT", action = "APPROVE", targetId = "#id")
    public AdPayout approve(String id, AdPayoutApproveRequest request, String userId, String orgId) {
        AdPayout p = requirePayout(id);
        if (p.getStatus() != PayoutStatus.PENDING_APPROVAL.getCode()) {
            throw new GenericException("仅待审核状态的付款单可审批");
        }
        boolean reject = "REJECT".equalsIgnoreCase(request.getAction());
        p.setApproveUser(userId);
        p.setApproveTime(System.currentTimeMillis());
        p.setApproveRemark(request.getRemark());
        p.setUpdateUser(userId);
        p.setUpdateTime(System.currentTimeMillis());

        if (reject) {
            // 驳回直接回草稿
            p.setStatus(PayoutStatus.DRAFT.getCode());
        } else {
            p.setStatus(PayoutStatus.PENDING_PAYMENT.getCode());
        }
        payoutMapper.update(p);
        return p;
    }

    /**
     * 付款（待付款 → 已付款），仅订单类型回写订单金额。
     */
    @OperationLog(module = "AD_PAYOUT", action = "PAY", targetId = "#id")
    public AdPayout pay(String id, AdPayoutPayRequest request, String userId, String orgId) {
        AdPayout p = requirePayout(id);
        if (p.getStatus() != PayoutStatus.PENDING_PAYMENT.getCode()) {
            throw new GenericException("仅待付款状态的付款单可付款");
        }
        long now = System.currentTimeMillis();
        p.setStatus(PayoutStatus.PAID.getCode());
        p.setPayUser(userId);
        p.setPayTime(now);
        p.setPayRemark(request == null ? null : request.getPayRemark());
        p.setUpdateUser(userId);
        p.setUpdateTime(now);
        payoutMapper.update(p);

        // 仅订单类型回写订单金额
        if (PayoutBillType.ofOrDefault(p.getBillType()).isOrder()
                && p.getOrderId() != null && !p.getOrderId().isBlank()) {
            AdOrder order = extAdOrderMapper.selectByPrimaryKey(p.getOrderId());
            if (order != null) {
                BigDecimal added = p.getAmount() == null ? BigDecimal.ZERO : p.getAmount();
                BigDecimal current = order.getMediaPaidAmount() == null ? BigDecimal.ZERO : order.getMediaPaidAmount();
                order.setMediaPaidAmount(current.add(added));
                order.setPaymentDone(1);
                order.setUpdateUser(userId);
                order.setUpdateTime(now);
                extAdOrderMapper.update(order);
            }
        }
        return p;
    }

    /** 详情（聚合付款单 + 审计 + 订单 + 合同信息）。 */
    public AdPayoutDetailResponse detail(String id, String userId, String orgId) {
        AdPayout p = requirePayout(id);
        AdPayoutDetailResponse resp = new AdPayoutDetailResponse();
        resp.setId(p.getId());
        resp.setPaymentNo(p.getPaymentNo());
        Integer billType = PayoutBillType.ofOrDefault(p.getBillType()).getCode();
        resp.setBillType(billType);
        resp.setBillTypeLabel(PayoutBillType.labelOf(billType));
        resp.setOrderId(p.getOrderId());
        resp.setAmount(p.getAmount());
        resp.setPaymentTime(p.getPaymentTime());
        resp.setType(p.getType());
        resp.setTypeLabel(PayoutType.labelOf(p.getType()));
        resp.setStatus(p.getStatus());
        resp.setStatusLabel(PayoutStatus.labelOf(p.getStatus()));
        resp.setMediaIds(p.getMediaIds());
        resp.setVoucherUrl(p.getVoucherUrl());
        resp.setRemark(p.getRemark());
        resp.setCreateUser(p.getCreateUser());
        resp.setCreateTime(p.getCreateTime());
        resp.setUpdateUser(p.getUpdateUser());
        resp.setUpdateTime(p.getUpdateTime());
        resp.setApproveUser(p.getApproveUser());
        resp.setApproveTime(p.getApproveTime());
        resp.setApproveRemark(p.getApproveRemark());
        resp.setPayUser(p.getPayUser());
        resp.setPayTime(p.getPayTime());
        resp.setPayRemark(p.getPayRemark());

        // 订单信息 + 关联合同（非订单类型无订单，跳过）
        if (PayoutBillType.ofOrDefault(p.getBillType()).isOrder()
                && p.getOrderId() != null && !p.getOrderId().isBlank()) {
            AdOrder order = extAdOrderMapper.selectByPrimaryKey(p.getOrderId());
            if (order != null) {
                resp.setOrderNo(order.getOrderNo());
                resp.setOrderName(order.getOrderName());
            }
            resp.setContracts(loadContracts(p.getOrderId()));
        }
        // 各下游客户付款返点明细
        resp.setMediaDetails(extAdPaymentMediaMapper.selectByPaymentId(p.getId()));
        return resp;
    }

    /** 加载订单关联的合同（统一通过 ad_order_contract 中间表，支持一对多）。 */
    private List<AdContractBriefResponse> loadContracts(String orderId) {
        List<AdContractBriefResponse> result = new java.util.ArrayList<>();
        List<AdOrderContract> orderContracts = orderContractMapper.selectByOrderId(orderId);
        if (orderContracts != null) {
            for (AdOrderContract oc : orderContracts) {
                AdContract c = extAdContractMapper.selectByPrimaryKey(oc.getContractId());
                if (c != null && (c.getDeleted() == null || c.getDeleted() == 0)) {
                    result.add(toBrief(c));
                }
            }
        }
        return result;
    }

    private AdContractBriefResponse toBrief(AdContract c) {
        AdContractBriefResponse b = new AdContractBriefResponse();
        b.setId(c.getId());
        b.setContractNo(c.getContractNo());
        b.setContractName(c.getContractName());
        b.setContractType(c.getContractType());
        b.setContractDirection(c.getContractDirection());
        b.setAmount(c.getAmount());
        b.setSealStatus(c.getSealStatus());
        return b;
    }

    /** 订单剩余应付金额（应付-已付），用于新建时带出默认金额。 */
    public BigDecimal remainingPayable(String orderId, String userId, String orgId) {
        AdOrder order = extAdOrderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new GenericException("订单不存在");
        }
        BigDecimal payable = order.getMediaPayableAmount() == null ? BigDecimal.ZERO : order.getMediaPayableAmount();
        BigDecimal paid = order.getMediaPaidAmount() == null ? BigDecimal.ZERO : order.getMediaPaidAmount();
        return payable.subtract(paid);
    }

    /** 订单的下游客户列表（付款单选择订单时带出供勾选，JOIN 出名称 + 账户）。 */
    public List<cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaOptionResponse> listMedia(String orderId, String userId, String orgId) {
        List<cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaOptionResponse> options = extAdPayoutMediaOptionMapper.selectMediaOptions(orderId);
        List<String> mediaIds = options.stream()
                .map(cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaOptionResponse::getMediaId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (!mediaIds.isEmpty()) {
            List<cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMediaAccount> all = extAccountMapper.selectByDownstreamMediaIds(mediaIds);
            java.util.Map<String, java.util.List<cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMediaAccount>> byMedia = all.stream()
                    .collect(java.util.stream.Collectors.groupingBy(cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMediaAccount::getDownstreamMediaId));
            for (cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaOptionResponse opt : options) {
                if (opt.getMediaId() == null) {
                    continue;
                }
                List<cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMediaAccount> accs = byMedia.get(opt.getMediaId());
                if (accs == null) {
                    accs = java.util.Collections.emptyList();
                }
                opt.setAccountList(accs.stream().map(a -> {
                    cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaAccountItem item = new cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaAccountItem();
                    item.setId(a.getId());
                    item.setDownstreamMediaId(a.getDownstreamMediaId());
                    item.setPayeeName(a.getPayeeName());
                    item.setBankName(a.getBankName());
                    item.setBankAccount(a.getBankAccount());
                    item.setDisabled(a.getDisabled());
                    item.setCreateTime(a.getCreateTime());
                    return item;
                }).toList());
            }
        }
        return options;
    }

    /** 分页列表。 */
    public PagerWithOption<List<AdPayoutListResponse>> page(AdPayoutPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        Page<AdPayoutListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdPayoutListResponse> list = extAdPayoutMapper.pageList(request);
        for (AdPayoutListResponse r : list) {
            Integer billType = PayoutBillType.ofOrDefault(r.getBillType()).getCode();
            r.setBillType(billType);
            r.setBillTypeLabel(PayoutBillType.labelOf(billType));
            r.setTypeLabel(PayoutType.labelOf(r.getType()));
            r.setStatusLabel(PayoutStatus.labelOf(r.getStatus()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    /** 逻辑删除（仅草稿可删）。 */
    @OperationLog(module = "AD_PAYOUT", action = "DELETE", targetId = "#id")
    public void delete(String id, String userId, String orgId) {
        AdPayout p = requirePayout(id);
        if (p.getStatus() != PayoutStatus.DRAFT.getCode()) {
            throw new GenericException("仅草稿状态的付款单可删除");
        }
        p.setDeleted(1);
        p.setUpdateUser(userId);
        p.setUpdateTime(System.currentTimeMillis());
        payoutMapper.update(p);
    }

    // ===================== 私有辅助 =====================

    private void validate(AdPayoutSaveRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new GenericException("付款金额必须大于0");
        }
        if (PayoutType.of(request.getType()) == null) {
            throw new GenericException("付款类型不合法(10普通付款/20坏账)");
        }
        PayoutBillType billType = PayoutBillType.ofOrDefault(request.getBillType());

        if (billType.isOrder()) {
            // 订单类型：必须关联订单
            if (request.getOrderId() == null || request.getOrderId().isBlank()) {
                throw new GenericException("订单类型的付款单必须选择关联订单");
            }
            return;
        }

        // 非订单类型：无关联订单，下游客户手动选择，仅支持一条明细
        if (request.getOrderId() != null && !request.getOrderId().isBlank()) {
            throw new GenericException("非订单类型的付款单不能关联订单");
        }
        List<AdPayoutMediaDetail> details = request.getMediaDetails();
        if (details == null || details.size() != 1) {
            throw new GenericException("非订单类型的付款单必须且只能有一条客户明细");
        }
        AdPayoutMediaDetail d = details.get(0);
        if (d.getMediaId() == null || d.getMediaId().isBlank()) {
            throw new GenericException("非订单类型的付款单必须选择下游客户");
        }
        if (d.getAccountId() == null || d.getAccountId().isBlank()) {
            throw new GenericException("非订单类型的付款单必须选择收款账户");
        }
    }

    /** 仅订单类型校验「一个订单只允许一个付款单」。 */
    private void assertNoExistingPayout(AdPayoutSaveRequest request) {
        if (!PayoutBillType.ofOrDefault(request.getBillType()).isOrder()) {
            return;
        }
        AdPayout existing = extAdPayoutMapper.selectByOrderId(request.getOrderId());
        if (existing != null) {
            throw new GenericException("该订单已存在付款单，一个订单仅允许一个付款单");
        }
    }

    private AdPayout requirePayout(String id) {
        AdPayout p = payoutMapper.selectByPrimaryKey(id);
        if (p == null || (p.getDeleted() != null && p.getDeleted() == 1)) {
            throw new GenericException("付款单不存在");
        }
        return p;
    }

    private String toJson(List<String> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) {
            return null;
        }
        try {
            return JSON.writeValueAsString(mediaIds);
        } catch (Exception e) {
            throw new GenericException("列表序列化失败");
        }
    }

    private String genNo() {
        return "P" + System.currentTimeMillis();
    }

    /** 写入付款单-各下游客户付款返点明细（每客户一行）。 */
    /** 空值兜底：null → 空串（用于 NOT NULL 的字符串列）。 */
    private static String nvl(String v) {
        return v == null ? "" : v;
    }

    /** 空值兜底：null → 0（用于 NOT NULL 的金额列）。 */
    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private void saveMediaDetails(AdPayout p, List<AdPayoutMediaDetail> details, String userId, String orgId, long now) {
        if (details == null || details.isEmpty()) {
            return;
        }
        List<AdPaymentMedia> rows = new java.util.ArrayList<>();
        for (AdPayoutMediaDetail d : details) {
            if (d.getMediaId() == null || d.getMediaId().isBlank()) {
                continue;
            }
            AdPaymentMedia row = new AdPaymentMedia();
            row.setId(IDGenerator.nextStr());
            row.setPaymentId(p.getId());
            row.setOrderId(p.getOrderId());
            // 非订单类型无「订单-下游客户中间表」记录，该列 NOT NULL，兜底空串
            row.setOrderDownstreamMediaId(nvl(d.getOrderDownstreamMediaId()));
            row.setMediaId(d.getMediaId());
            row.setMediaName(d.getMediaName());
            // 下列金额列均为 NOT NULL，非订单类型无返点计算，统一兜底 0
            row.setPayableAmount(nvl(d.getPayableAmount()));
            row.setNoRebateAmount(nvl(d.getNoRebateAmount()));
            // ad_payment_media.rebate_mode 为 NOT NULL，订单下游客户可能未填，兜底默认 10(比例)
            row.setRebateMode(d.getRebateMode() == null ? 10 : d.getRebateMode());
            row.setRebateValue(nvl(d.getRebateValue()));
            row.setRebateAmount(nvl(d.getRebateAmount()));
            row.setActualPayable(nvl(d.getActualPayable()));
            row.setPaidAmount(d.getPaidAmount() == null ? BigDecimal.ZERO : d.getPaidAmount());
            row.setAccountId(d.getAccountId());
            row.setOrganizationId(orgId);
            row.setCreateUser(userId);
            row.setUpdateUser(userId);
            row.setCreateTime(now);
            row.setUpdateTime(now);
            rows.add(row);
        }
        for (final AdPaymentMedia row : rows) {
            adPaymentMediaMapper.insert(row);
        }
    }
}
