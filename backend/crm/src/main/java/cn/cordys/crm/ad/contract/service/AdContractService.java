package cn.cordys.crm.ad.contract.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.common.AdEntityPermissionProvider;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.contract.constants.ContractDirection;
import cn.cordys.crm.ad.contract.constants.ContractType;
import cn.cordys.crm.ad.contract.constants.RelatedPartyType;
import cn.cordys.crm.ad.contract.constants.SealStatus;
import cn.cordys.crm.ad.contract.domain.AdContract;
import cn.cordys.crm.ad.contract.domain.AdSealRecord;
import cn.cordys.crm.ad.contract.dto.request.AdContractPageRequest;
import cn.cordys.crm.ad.contract.dto.request.AdContractSaveRequest;
import cn.cordys.crm.ad.contract.dto.response.AdContractDetailResponse;
import cn.cordys.crm.ad.contract.dto.response.AdContractListResponse;
import cn.cordys.crm.ad.contract.mapper.ExtAdContractMapper;
import cn.cordys.crm.ad.customer.domain.AdCustomer;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.domain.AdOrderContract;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderContractMapper;
import cn.cordys.crm.ad.upstreamagent.domain.AdUpstreamAgent;
import cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMedia;
import cn.cordys.crm.ad.seal.mapper.ExtAdSealRecordMapper;
import cn.cordys.crm.system.dto.request.UploadTransferRequest;
import cn.cordys.crm.system.service.AttachmentService;
import cn.cordys.mybatis.BaseMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 广告合同服务（M5 T-40/T-41，V3.1 §5.2.3/§7.3/§8.2）。
 *
 * <p>合同 CRUD + 分页（主体隔离）+ 详情。每个状态变更方法均标注 {@link OperationLog}（写入 ad_operation_log）。
 * 列表/详情复用 {@link AdEntityPermissionProvider} 做业务主体隔离（L-06/L-12）。</p>
 *
 * <p>关联：单笔合同经 {@code order_id} 关联订单（L-10）；变更单经 {@code change_order_id} 关联（可选）。
 * 框架合同多订单关联走 ad_order_contract 中间表（T-41，本期仅保留 order_id 直接关联能力）。</p>
 *
 * <p>用印状态（seal_status）由本服务在新建/编辑时维持，申请/审批/执行由 {@code AdSealRecordService} 驱动。</p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdContractService {

    @Resource
    private BaseMapper<AdContract> contractMapper;
    @Resource
    private ExtAdSealRecordMapper sealRecordMapper;
    @Resource
    private BaseMapper<AdBusinessEntity> businessEntityMapper;
    @Resource
    private BaseMapper<AdCustomer> customerMapper;
    @Resource
    private BaseMapper<AdUpstreamAgent> upstreamAgentMapper;
    @Resource
    private BaseMapper<AdDownstreamMedia> downstreamMediaMapper;
    @Resource
    private BaseMapper<AdOrder> adOrderMapper;
    @Resource
    private ExtAdContractMapper extAdContractMapper;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;
    @Resource
    private ExtAdOrderContractMapper orderContractMapper;
    @Resource
    private AttachmentService attachmentService;

    // ===================== 新建 / 编辑 =====================

    /**
     * 新建合同（默认生效 status=10，未申请 seal_status=0）。生成合同编号、校验关键字段。
     */
    @OperationLog(module = "AD_CONTRACT", action = "CREATE", targetId = "")
    public AdContract create(AdContractSaveRequest request, String userId, String orgId) {
        validate(request);

        AdContract c = new AdContract();
        BeanUtils.copyProperties(request, c);
        c.setId(IDGenerator.nextStr());
        c.setOrganizationId(orgId);
        if (request.getContractNo() == null || request.getContractNo().isBlank()) {
            c.setContractNo(generateContractNo());
        }
        c.setSealStatus(SealStatus.NOT_APPLIED.getCode());
        c.setStatus(10);
        c.setCreateUser(userId);
        c.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        c.setCreateTime(now);
        c.setUpdateTime(now);
        contractMapper.insert(c);
        // 用印附件：把前端上传的临时文件转存为正式附件
        transferAttachment(c.getId(), orgId, userId, request.getFileUrl());
        // 如果关联了订单，自动写入 ad_order_contract 中间表（一对多）
        syncOrderContract(c.getId(), request.getOrderIds(), userId, orgId);
        return c;
    }

    /**
     * 编辑合同（维持受控字段：编号/状态/用印状态/组织/创建人/时间）。
     */
    @OperationLog(module = "AD_CONTRACT", action = "UPDATE", targetId = "#request.id")
    public AdContract update(AdContractSaveRequest request, String userId, String orgId) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("合同id不能为空");
        }
        AdContract existing = requireContract(request.getId());
        String org0 = existing.getOrganizationId();
        String no0 = existing.getContractNo();
        String cu0 = existing.getCreateUser();
        Long ct0 = existing.getCreateTime();
        Integer ss0 = existing.getSealStatus();
        Integer st0 = existing.getStatus();
        Integer del0 = existing.getDeleted();

        BeanUtils.copyProperties(request, existing);
        existing.setId(request.getId());
        existing.setOrganizationId(org0);
        existing.setContractNo(no0);
        existing.setCreateUser(cu0);
        existing.setCreateTime(ct0);
        existing.setSealStatus(ss0);
        existing.setStatus(st0);
        existing.setDeleted(del0);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(existing);
        // 同步 ad_order_contract 中间表（一对多，全量替换）
        syncOrderContract(request.getId(), request.getOrderIds(), userId, orgId);
        return existing;
    }

    /**
     * 逻辑删除合同。
     */
    @OperationLog(module = "AD_CONTRACT", action = "DELETE", targetId = "#id")
    public void delete(String id, String userId, String orgId) {
        AdContract c = requireContract(id);
        c.setDeleted(1);
        c.setUpdateUser(userId);
        c.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(c);
    }

    // ===================== 归档审批 =====================

    /**
     * 上传双盖附件（仅保存，不改状态）。
     */
    public AdContract uploadDoubleSeal(String id, String fileUrl, String userId, String orgId) {
        AdContract c = requireContract(id);
        transferAttachment(id, orgId, userId, fileUrl);
        c.setDoubleSealFileUrl(fileUrl);
        c.setUpdateUser(userId);
        c.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(c);
        return c;
    }

    /**
     * 提交归档审批：用印状态 → 归档审批中(40)。
     * 前提：sealStatus == 20(已用印) 或 50(归档审批驳回)。
     */
    @OperationLog(module = "AD_CONTRACT", action = "SUBMIT_ARCHIVE", targetId = "#id")
    public AdContract submitArchive(String id, String fileUrl, String userId, String orgId) {
        AdContract c = requireContract(id);
        if (c.getSealStatus() != SealStatus.SEALED.getCode()
                && c.getSealStatus() != SealStatus.ARCHIVE_REJECTED.getCode()) {
            throw new GenericException("仅已用印或归档审批驳回的合同可提交归档审批");
        }
        if (fileUrl != null && !fileUrl.isBlank()) {
            transferAttachment(id, orgId, userId, fileUrl);
            c.setDoubleSealFileUrl(fileUrl);
        }
        c.setSealStatus(SealStatus.ARCHIVE_APPROVING.getCode());
        c.setUpdateUser(userId);
        c.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(c);
        return c;
    }

    /**
     * 归档审批通过：用印状态 → 已归档(60)。
     * 前提：sealStatus == 40(归档审批中)。
     */
    @OperationLog(module = "AD_CONTRACT", action = "APPROVE_ARCHIVE", targetId = "#id")
    public AdContract approveArchive(String id, String remark, String userId, String orgId) {
        AdContract c = requireContract(id);
        if (c.getSealStatus() != SealStatus.ARCHIVE_APPROVING.getCode()) {
            throw new GenericException("仅归档审批中的合同可审批");
        }
        c.setSealStatus(SealStatus.ARCHIVED.getCode());
        c.setArchiveApproveRemark(remark);
        c.setArchiveApproveUser(userId);
        c.setArchiveApproveTime(System.currentTimeMillis());
        c.setUpdateUser(userId);
        c.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(c);
        return c;
    }

    /**
     * 归档审批驳回：用印状态 → 归档审批驳回(50)。
     * 前提：sealStatus == 40(归档审批中)。
     */
    @OperationLog(module = "AD_CONTRACT", action = "REJECT_ARCHIVE", targetId = "#id")
    public AdContract rejectArchive(String id, String remark, String userId, String orgId) {
        AdContract c = requireContract(id);
        if (c.getSealStatus() != SealStatus.ARCHIVE_APPROVING.getCode()) {
            throw new GenericException("仅归档审批中的合同可驳回");
        }
        c.setSealStatus(SealStatus.ARCHIVE_REJECTED.getCode());
        c.setArchiveApproveRemark(remark);
        c.setArchiveApproveUser(userId);
        c.setArchiveApproveTime(System.currentTimeMillis());
        c.setUpdateUser(userId);
        c.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(c);
        return c;
    }

    // ===================== 详情 / 分页 =====================

    /**
     * 合同详情：主信息 + 关联名称 + 状态标签 + 用印记录历史。
     */
    public AdContractDetailResponse detail(String id, String userId, String orgId) {
        AdContract c = requireContract(id);
        AdContractDetailResponse resp = new AdContractDetailResponse();
        resp.setContract(c);
        resp.setBusinessEntityName(resolveBusinessEntityName(c.getBusinessEntityId()));
        resp.setRelatedPartyName(resolveRelatedPartyName(c.getRelatedPartyType(), c.getRelatedPartyId()));
        resp.setOrderList(resolveOrderList(c.getId()));
        resp.setDirectionLabel(ContractDirection.labelOf(c.getContractDirection()));
        resp.setTypeLabel(ContractType.labelOf(c.getContractType()));
        resp.setSealStatusLabel(SealStatus.labelOf(c.getSealStatus()));
        resp.setStatusLabel(contractStatusLabel(c.getStatus()));
        List<AdSealRecord> seals = sealRecordMapper.selectByContractId(id);
        resp.setSealRecords(seals);
        return resp;
    }

    /**
     * 合同分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     */
    public PagerWithOption<List<AdContractListResponse>> page(AdContractPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        Page<AdContractListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdContractListResponse> list = extAdContractMapper.pageList(request);
        for (AdContractListResponse r : list) {
            r.setContractDirectionLabel(ContractDirection.labelOf(r.getContractDirection()));
            r.setContractTypeLabel(ContractType.labelOf(r.getContractType()));
            r.setRelatedPartyTypeLabel(RelatedPartyType.labelOf(r.getRelatedPartyType()));
            r.setSealStatusLabel(SealStatus.labelOf(r.getSealStatus()));
            r.setStatusLabel(contractStatusLabel(r.getStatus()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    // ===================== 私有辅助 =====================

    private AdContract requireContract(String id) {
        AdContract c = contractMapper.selectByPrimaryKey(id);
        if (c == null || (c.getDeleted() != null && c.getDeleted() == 1)) {
            throw new GenericException("合同不存在");
        }
        return c;
    }

    /** 把前端上传的临时附件转存为正式附件（写入 attachment 表并从 tmp 目录搬移到 transfer 目录）。
     *  tempFileUrl 为单个临时文件 ID，为空则跳过。 */
    private void transferAttachment(String contractId, String orgId, String userId, String tempFileUrl) {
        if (tempFileUrl == null || tempFileUrl.isBlank()) {
            return;
        }
        UploadTransferRequest transferRequest =
            new UploadTransferRequest(orgId, contractId, userId, List.of(tempFileUrl));
        attachmentService.processTemp(transferRequest);
    }

    private void validate(AdContractSaveRequest request) {
        if (request.getContractName() == null || request.getContractName().isBlank()) {
            throw new GenericException("合同名称不能为空");
        }
        if (request.getBusinessEntityId() == null || request.getBusinessEntityId().isBlank()) {
            throw new GenericException("业务主体不能为空");
        }
        if (ContractDirection.of(request.getContractDirection()) == null) {
            throw new GenericException("合同方向不合法(10上游/20下游)");
        }
        if (ContractType.of(request.getContractType()) == null) {
            throw new GenericException("合同类型不合法(10框架/20单笔)");
        }
        if (request.getRelatedPartyId() == null || request.getRelatedPartyId().isBlank()) {
            throw new GenericException("关联方不能为空");
        }
        if (request.getRelatedPartyType() == null
                || (request.getRelatedPartyType() != 10 && request.getRelatedPartyType() != 20
                && request.getRelatedPartyType() != 30)) {
            throw new GenericException("关联方类型不合法(10客户/20上游代理/30下游媒体)");
        }
    }

    private String contractStatusLabel(Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 10: return "生效";
            case 20: return "失效";
            case 30: return "已作废";
            default: return String.valueOf(status);
        }
    }

    /**
     * 同步 ad_order_contract 中间表：将合同与订单关联写入（一对多）。
     * 全量替换：先逻辑删除该合同已有的关联，再插入本次传入的订单列表。
     * orderIds 为 null/空则仅清除旧关联（不报错）。
     */
    private void syncOrderContract(String contractId, List<String> orderIds, String userId, String orgId) {
        // 先逻辑删除该合同已有的全部关联
        List<AdOrderContract> existing = orderContractMapper.selectByContractId(contractId);
        for (AdOrderContract oc : existing) {
            oc.setDeleted(1);
            orderContractMapper.update(oc);
        }
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (String orderId : orderIds) {
            if (orderId == null || orderId.isBlank()) {
                continue;
            }
            AdOrderContract oc = new AdOrderContract();
            oc.setId(IDGenerator.nextStr());
            oc.setOrderId(orderId);
            oc.setContractId(contractId);
            oc.setOrganizationId(orgId);
            oc.setDeleted(0);
            oc.setCreateTime(now);
            orderContractMapper.insert(oc);
        }
    }

    private String resolveBusinessEntityName(String businessEntityId) {
        if (businessEntityId == null || businessEntityId.isBlank()) {
            return null;
        }
        AdBusinessEntity be = businessEntityMapper.selectByPrimaryKey(businessEntityId);
        return be == null ? null : be.getName();
    }

    private String resolveRelatedPartyName(Integer type, String partyId) {
        if (partyId == null || partyId.isBlank()) {
            return null;
        }
        if (type != null && type == 10) {
            AdCustomer cu = customerMapper.selectByPrimaryKey(partyId);
            return cu == null ? null : cu.getName();
        }
        if (type != null && type == 20) {
            AdUpstreamAgent agent = upstreamAgentMapper.selectByPrimaryKey(partyId);
            return agent == null ? null : agent.getName();
        }
        if (type != null && type == 30) {
            AdDownstreamMedia media = downstreamMediaMapper.selectByPrimaryKey(partyId);
            return media == null ? null : media.getName();
        }
        return null;
    }

    private List<AdContractDetailResponse.AdContractOrderVO> resolveOrderList(String contractId) {
        List<AdOrderContract> links = orderContractMapper.selectByContractId(contractId);
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }
        List<AdContractDetailResponse.AdContractOrderVO> result = new ArrayList<>();
        for (AdOrderContract link : links) {
            AdOrder o = adOrderMapper.selectByPrimaryKey(link.getOrderId());
            if (o == null) {
                continue;
            }
            AdContractDetailResponse.AdContractOrderVO vo = new AdContractDetailResponse.AdContractOrderVO();
            vo.setOrderId(o.getId());
            vo.setOrderNo(o.getOrderNo());
            vo.setOrderName(o.getOrderName());
            result.add(vo);
        }
        return result;
    }

    /**
     * 合同编号：CN-yyyyMMdd-NNNN（保证开发期唯一；非流水号强约束）。
     */
    private String generateContractNo() {
        Calendar cal = Calendar.getInstance();
        String ymd = String.format("%04d%02d%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        int r = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "CN" + ymd + "-" + r;
    }
}
