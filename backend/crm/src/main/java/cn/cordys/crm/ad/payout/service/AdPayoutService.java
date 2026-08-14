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
import cn.cordys.crm.ad.payout.constants.PayoutStatus;
import cn.cordys.crm.ad.payout.constants.PayoutType;
import cn.cordys.crm.ad.payout.domain.AdPayout;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutApproveRequest;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutPageRequest;
import cn.cordys.crm.ad.payout.dto.request.AdPayoutSaveRequest;
import cn.cordys.crm.ad.payout.dto.response.AdPayoutDetailResponse;
import cn.cordys.crm.ad.payout.dto.response.AdPayoutListResponse;
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
 * <p>一个订单对应一个付款单，可勾选多个媒体（默认全部）。流程：新建(草稿)→保存→编辑→提交(待审核)→审核通过/驳回。
 * 审核通过后：订单 media_paid_amount += amount，并置 payment_done=1。</p>
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

    private static final ObjectMapper JSON = new ObjectMapper();

    /** 新建（草稿状态）。 */
    @OperationLog(module = "AD_PAYOUT", action = "CREATE", targetId = "")
    public AdPayout create(AdPayoutSaveRequest request, String userId, String orgId) {
        validate(request);
        assertNoExistingPayout(request.getOrderId());

        AdPayout p = new AdPayout();
        p.setId(IDGenerator.nextStr());
        p.setPaymentNo(genNo());
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
        return p;
    }

    /** 编辑（仅草稿/驳回状态可编辑）。 */
    @OperationLog(module = "AD_PAYOUT", action = "UPDATE", targetId = "#request.id")
    public AdPayout update(AdPayoutSaveRequest request, String userId, String orgId) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("付款单id不能为空");
        }
        AdPayout p = requirePayout(request.getId());
        if (p.getStatus() != PayoutStatus.DRAFT.getCode() && p.getStatus() != PayoutStatus.REJECTED.getCode()) {
            throw new GenericException("仅草稿或驳回状态的付款单可编辑");
        }
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
        return p;
    }

    /** 提交（草稿/驳回 → 待审核）。 */
    @OperationLog(module = "AD_PAYOUT", action = "SUBMIT", targetId = "#id")
    public AdPayout submit(String id, String userId, String orgId) {
        AdPayout p = requirePayout(id);
        if (p.getStatus() != PayoutStatus.DRAFT.getCode() && p.getStatus() != PayoutStatus.REJECTED.getCode()) {
            throw new GenericException("仅草稿或驳回状态的付款单可提交");
        }
        p.setStatus(PayoutStatus.PENDING_APPROVAL.getCode());
        p.setUpdateUser(userId);
        p.setUpdateTime(System.currentTimeMillis());
        payoutMapper.update(p);
        return p;
    }

    /** 审核通过/驳回。通过后回写订单金额并置 payment_done=1。 */
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
            p.setStatus(PayoutStatus.REJECTED.getCode());
            payoutMapper.update(p);
            return p;
        }

        p.setStatus(PayoutStatus.APPROVED.getCode());
        payoutMapper.update(p);

        // 回写订单：累计已付 + 置已付
        AdOrder order = extAdOrderMapper.selectByPrimaryKey(p.getOrderId());
        if (order != null) {
            BigDecimal added = p.getAmount() == null ? BigDecimal.ZERO : p.getAmount();
            BigDecimal current = order.getMediaPaidAmount() == null ? BigDecimal.ZERO : order.getMediaPaidAmount();
            order.setMediaPaidAmount(current.add(added));
            order.setPaymentDone(1);
            order.setUpdateUser(userId);
            order.setUpdateTime(System.currentTimeMillis());
            extAdOrderMapper.update(order);
        }
        return p;
    }

    /** 详情（聚合付款单 + 审计 + 订单 + 合同信息）。 */
    public AdPayoutDetailResponse detail(String id, String userId, String orgId) {
        AdPayout p = requirePayout(id);
        AdPayoutDetailResponse resp = new AdPayoutDetailResponse();
        resp.setId(p.getId());
        resp.setPaymentNo(p.getPaymentNo());
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

        // 订单信息
        AdOrder order = extAdOrderMapper.selectByPrimaryKey(p.getOrderId());
        if (order != null) {
            resp.setOrderNo(order.getOrderNo());
            resp.setOrderName(order.getOrderName());
        }

        // 关联合同（框架合同 + 单笔合同）
        resp.setContracts(loadContracts(p.getOrderId()));
        return resp;
    }

    /** 加载订单关联的合同（框架合同通过 ad_order_contract，单笔合同通过 ad_contract.order_id）。 */
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
        List<AdContract> singleContracts = extAdContractMapper.selectByOrderId(orderId);
        if (singleContracts != null) {
            for (AdContract c : singleContracts) {
                result.add(toBrief(c));
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

    /** 订单剩余应付金额（媒体应付-已付），用于新建时带出默认金额。 */
    public BigDecimal remainingPayable(String orderId, String userId, String orgId) {
        AdOrder order = extAdOrderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new GenericException("订单不存在");
        }
        BigDecimal payable = order.getMediaPayableAmount() == null ? BigDecimal.ZERO : order.getMediaPayableAmount();
        BigDecimal paid = order.getMediaPaidAmount() == null ? BigDecimal.ZERO : order.getMediaPaidAmount();
        return payable.subtract(paid);
    }

    /** 订单的下游媒体列表（付款单选择订单时带出供勾选，JOIN 出媒体名称）。 */
    public List<cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaOptionResponse> listMedia(String orderId, String userId, String orgId) {
        return extAdPayoutMediaOptionMapper.selectMediaOptions(orderId);
    }

    /** 分页列表。 */
    public PagerWithOption<List<AdPayoutListResponse>> page(AdPayoutPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        Page<AdPayoutListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdPayoutListResponse> list = extAdPayoutMapper.pageList(request);
        for (AdPayoutListResponse r : list) {
            r.setTypeLabel(PayoutType.labelOf(r.getType()));
            r.setStatusLabel(PayoutStatus.labelOf(r.getStatus()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    /** 逻辑删除（仅草稿/驳回可删）。 */
    @OperationLog(module = "AD_PAYOUT", action = "DELETE", targetId = "#id")
    public void delete(String id, String userId, String orgId) {
        AdPayout p = requirePayout(id);
        if (p.getStatus() != PayoutStatus.DRAFT.getCode() && p.getStatus() != PayoutStatus.REJECTED.getCode()) {
            throw new GenericException("仅草稿或驳回状态的付款单可删除");
        }
        p.setDeleted(1);
        p.setUpdateUser(userId);
        p.setUpdateTime(System.currentTimeMillis());
        payoutMapper.update(p);
    }

    // ===================== 私有辅助 =====================

    private void validate(AdPayoutSaveRequest request) {
        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new GenericException("关联订单不能为空");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new GenericException("付款金额必须大于0");
        }
        if (PayoutType.of(request.getType()) == null) {
            throw new GenericException("付款类型不合法(10普通付款/20坏账)");
        }
    }

    private void assertNoExistingPayout(String orderId) {
        AdPayout existing = extAdPayoutMapper.selectByOrderId(orderId);
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
            throw new GenericException("媒体列表序列化失败");
        }
    }

    private String genNo() {
        return "P" + System.currentTimeMillis();
    }
}
