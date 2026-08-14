package cn.cordys.crm.ad.receipt.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderMapper;
import cn.cordys.crm.ad.receipt.constants.ReceiptStatus;
import cn.cordys.crm.ad.receipt.constants.ReceiptType;
import cn.cordys.crm.ad.receipt.domain.AdReceipt;
import cn.cordys.crm.ad.receipt.dto.request.AdReceiptApproveRequest;
import cn.cordys.crm.ad.receipt.dto.request.AdReceiptPageRequest;
import cn.cordys.crm.ad.receipt.dto.request.AdReceiptSaveRequest;
import cn.cordys.crm.ad.receipt.dto.response.AdReceiptListResponse;
import cn.cordys.crm.ad.receipt.mapper.ExtAdReceiptMapper;
import cn.cordys.mybatis.BaseMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 广告收款单服务。
 *
 * <p>一个订单对应一个收款单。流程：新建(草稿)→保存→编辑→提交(待审核)→审核通过/驳回。
 * 审核通过后：订单 received_amount += amount，并置 receipt_done=1。</p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdReceiptService {

    @Resource
    private BaseMapper<AdReceipt> receiptMapper;
    @Resource
    private ExtAdReceiptMapper extAdReceiptMapper;
    @Resource
    private ExtAdOrderMapper extAdOrderMapper;

    /** 新建（草稿状态）。 */
    @OperationLog(module = "AD_RECEIPT", action = "CREATE", targetId = "")
    public AdReceipt create(AdReceiptSaveRequest request, String userId, String orgId) {
        validate(request);
        assertNoExistingReceipt(request.getOrderId());

        AdReceipt r = new AdReceipt();
        r.setId(IDGenerator.nextStr());
        r.setReceiptNo(genNo());
        r.setOrderId(request.getOrderId());
        r.setAmount(request.getAmount());
        r.setReceiptTime(request.getReceiptTime());
        r.setType(request.getType() != null ? request.getType() : ReceiptType.NORMAL.getCode());
        r.setVoucherUrl(request.getVoucherUrl());
        r.setRemark(request.getRemark());
        r.setStatus(ReceiptStatus.DRAFT.getCode());
        r.setOrganizationId(orgId);
        r.setCreateUser(userId);
        r.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        r.setCreateTime(now);
        r.setUpdateTime(now);
        receiptMapper.insert(r);
        return r;
    }

    /** 编辑（仅草稿/驳回状态可编辑）。 */
    @OperationLog(module = "AD_RECEIPT", action = "UPDATE", targetId = "#request.id")
    public AdReceipt update(AdReceiptSaveRequest request, String userId, String orgId) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("收款单id不能为空");
        }
        AdReceipt r = requireReceipt(request.getId());
        if (r.getStatus() != ReceiptStatus.DRAFT.getCode() && r.getStatus() != ReceiptStatus.REJECTED.getCode()) {
            throw new GenericException("仅草稿或驳回状态的收款单可编辑");
        }
        r.setOrderId(request.getOrderId());
        r.setAmount(request.getAmount());
        r.setReceiptTime(request.getReceiptTime());
        if (request.getType() != null) {
            r.setType(request.getType());
        }
        r.setVoucherUrl(request.getVoucherUrl());
        r.setRemark(request.getRemark());
        r.setUpdateUser(userId);
        r.setUpdateTime(System.currentTimeMillis());
        receiptMapper.update(r);
        return r;
    }

    /** 提交（草稿/驳回 → 待审核）。 */
    @OperationLog(module = "AD_RECEIPT", action = "SUBMIT", targetId = "#id")
    public AdReceipt submit(String id, String userId, String orgId) {
        AdReceipt r = requireReceipt(id);
        if (r.getStatus() != ReceiptStatus.DRAFT.getCode() && r.getStatus() != ReceiptStatus.REJECTED.getCode()) {
            throw new GenericException("仅草稿或驳回状态的收款单可提交");
        }
        r.setStatus(ReceiptStatus.PENDING_APPROVAL.getCode());
        r.setUpdateUser(userId);
        r.setUpdateTime(System.currentTimeMillis());
        receiptMapper.update(r);
        return r;
    }

    /** 审核通过/驳回。通过后回写订单金额并置 receipt_done=1。 */
    @OperationLog(module = "AD_RECEIPT", action = "APPROVE", targetId = "#id")
    public AdReceipt approve(String id, AdReceiptApproveRequest request, String userId, String orgId) {
        AdReceipt r = requireReceipt(id);
        if (r.getStatus() != ReceiptStatus.PENDING_APPROVAL.getCode()) {
            throw new GenericException("仅待审核状态的收款单可审批");
        }
        boolean reject = "REJECT".equalsIgnoreCase(request.getAction());
        r.setApproveUser(userId);
        r.setApproveTime(System.currentTimeMillis());
        r.setApproveRemark(request.getRemark());
        r.setUpdateUser(userId);
        r.setUpdateTime(System.currentTimeMillis());

        if (reject) {
            r.setStatus(ReceiptStatus.REJECTED.getCode());
            receiptMapper.update(r);
            return r;
        }

        r.setStatus(ReceiptStatus.APPROVED.getCode());
        receiptMapper.update(r);

        // 回写订单：累计已收 + 置已收
        AdOrder order = extAdOrderMapper.selectByPrimaryKey(r.getOrderId());
        if (order != null) {
            BigDecimal added = r.getAmount() == null ? BigDecimal.ZERO : r.getAmount();
            BigDecimal current = order.getReceivedAmount() == null ? BigDecimal.ZERO : order.getReceivedAmount();
            order.setReceivedAmount(current.add(added));
            order.setReceiptDone(1);
            order.setUpdateUser(userId);
            order.setUpdateTime(System.currentTimeMillis());
            extAdOrderMapper.update(order);
        }
        return r;
    }

    /** 详情。 */
    public AdReceipt detail(String id, String userId, String orgId) {
        return requireReceipt(id);
    }

    /** 按订单查询剩余应收金额（应收-已收），用于新建时带出默认金额。 */
    public BigDecimal remainingReceivable(String orderId, String userId, String orgId) {
        AdOrder order = extAdOrderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new GenericException("订单不存在");
        }
        BigDecimal receivable = order.getReceivableAmount() == null ? BigDecimal.ZERO : order.getReceivableAmount();
        BigDecimal received = order.getReceivedAmount() == null ? BigDecimal.ZERO : order.getReceivedAmount();
        return receivable.subtract(received);
    }

    /** 分页列表。 */
    public PagerWithOption<List<AdReceiptListResponse>> page(AdReceiptPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        Page<AdReceiptListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdReceiptListResponse> list = extAdReceiptMapper.pageList(request);
        for (AdReceiptListResponse r : list) {
            r.setTypeLabel(ReceiptType.labelOf(r.getType()));
            r.setStatusLabel(ReceiptStatus.labelOf(r.getStatus()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    /** 逻辑删除（仅草稿/驳回可删）。 */
    @OperationLog(module = "AD_RECEIPT", action = "DELETE", targetId = "#id")
    public void delete(String id, String userId, String orgId) {
        AdReceipt r = requireReceipt(id);
        if (r.getStatus() != ReceiptStatus.DRAFT.getCode() && r.getStatus() != ReceiptStatus.REJECTED.getCode()) {
            throw new GenericException("仅草稿或驳回状态的收款单可删除");
        }
        r.setDeleted(1);
        r.setUpdateUser(userId);
        r.setUpdateTime(System.currentTimeMillis());
        receiptMapper.update(r);
    }

    // ===================== 私有辅助 =====================

    private void validate(AdReceiptSaveRequest request) {
        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new GenericException("关联订单不能为空");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new GenericException("收款金额必须大于0");
        }
        if (ReceiptType.of(request.getType()) == null) {
            throw new GenericException("收款类型不合法(10普通收款/20退款)");
        }
    }

    private void assertNoExistingReceipt(String orderId) {
        AdReceipt existing = extAdReceiptMapper.selectByOrderId(orderId);
        if (existing != null) {
            throw new GenericException("该订单已存在收款单，一个订单仅允许一个收款单");
        }
    }

    private AdReceipt requireReceipt(String id) {
        AdReceipt r = receiptMapper.selectByPrimaryKey(id);
        if (r == null || (r.getDeleted() != null && r.getDeleted() == 1)) {
            throw new GenericException("收款单不存在");
        }
        return r;
    }

    private String genNo() {
        return "R" + System.currentTimeMillis();
    }
}
