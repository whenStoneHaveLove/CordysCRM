package cn.cordys.crm.ad.seal.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.common.AdEntityPermissionProvider;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.contract.constants.SealStatus;
import cn.cordys.crm.ad.contract.domain.AdContract;
import cn.cordys.crm.ad.contract.domain.AdSealRecord;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.seal.constants.AdSealRecordStatus;
import cn.cordys.crm.ad.seal.dto.request.AdSealApplyRequest;
import cn.cordys.crm.ad.seal.dto.request.AdSealApproveRequest;
import cn.cordys.crm.ad.seal.dto.request.AdSealRecordPageRequest;
import cn.cordys.crm.ad.seal.dto.request.AdSealUploadRequest;
import cn.cordys.crm.ad.seal.dto.response.AdSealRecordDetailResponse;
import cn.cordys.crm.ad.seal.dto.response.AdSealRecordListResponse;
import cn.cordys.crm.ad.seal.mapper.ExtAdSealRecordMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUser;
import cn.cordys.common.dto.RoleDataScopeDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 广告用印服务（M5 T-42，V3.1 §5.2.4/§7.3/L-07/L-19）。
 *
 * <p>用印生命周期（申请/审批/驳回/执行上传），<b>不依赖</b> {@code cn.cordys.crm.approval} 模块。
 * 每个状态变更方法均标注 {@link OperationLog}（写入 ad_operation_log）。</p>
 *
 * <p>状态联动：
 * <ul>
 *   <li>申请(apply)：seal.status=0审批中；合同 seal_status=审批中(10)。</li>
 *   <li>通过(approve)：seal.status=10通过，填实际份数；合同若已有文件(file_url)则 seal_status=已用印(20)。</li>
 *   <li>驳回(reject)：seal.status=20驳回；合同 seal_status=已驳回(30)。</li>
 *   <li>执行上传(upload)：合同 file_url 回填，seal_status=已用印(20)（单笔先申请后盖章，L-07）。</li>
 * </ul>
 * 主体隔离经合同 business_entity_id 注入（{@link AdEntityPermissionProvider}，L-06/L-12）。</p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdSealRecordService {

    @Resource
    private BaseMapper<AdSealRecord> sealRecordMapper;
    @Resource
    private BaseMapper<AdContract> contractMapper;
    @Resource
    private BaseMapper<AdOrder> adOrderMapper;
    @Resource
    private BaseMapper<AdBusinessEntity> businessEntityMapper;
    @Resource
    private ExtAdSealRecordMapper extAdSealRecordMapper;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;

    /** 用印模块角色守卫（best-effort，同 M2/M3）。 */
    private static final String ROLE_MEDIA = "ROLE_MEDIA";
    private static final String ROLE_BOSS = "ROLE_BOSS";

    // ===================== 申请 / 审批 / 执行 =====================

    /**
     * 申请用印（L-07 先申请后盖章）：创建 seal 记录(status=0)，合同 seal_status=审批中(10)。
     */
    @OperationLog(module = "SEAL", action = "APPLY", targetId = "")
    public AdSealRecord apply(AdSealApplyRequest request, String userId, String orgId) {
        assertRole(ROLE_MEDIA);
        if (request.getContractId() == null || request.getContractId().isBlank()) {
            throw new GenericException("合同id不能为空");
        }
        if (request.getSealType() == null || (request.getSealType() != 10 && request.getSealType() != 20)) {
            throw new GenericException("用印类型不合法(10公章/20合同章)");
        }
        if (request.getAppliedCopies() == null || request.getAppliedCopies() <= 0) {
            throw new GenericException("申请份数必须大于0");
        }
        AdContract contract = requireContract(request.getContractId());

        AdSealRecord s = new AdSealRecord();
        s.setId(IDGenerator.nextStr());
        s.setContractId(contract.getId());
        s.setSealType(request.getSealType());
        s.setAppliedCopies(request.getAppliedCopies());
        s.setApplicantId(userId);
        s.setApplyRemark(request.getApplyRemark());
        s.setStatus(AdSealRecordStatus.APPROVING.getCode());
        s.setOrganizationId(orgId);
        s.setCreateUser(userId);
        s.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        s.setCreateTime(now);
        s.setUpdateTime(now);
        sealRecordMapper.insert(s);

        contract.setSealStatus(SealStatus.APPROVING.getCode());
        contract.setUpdateUser(userId);
        contract.setUpdateTime(now);
        contractMapper.update(contract);
        return s;
    }

    /**
     * 用印审批通过（填实际份数）。合同若已有文件直接置已用印(20)，否则保持审批中(10)待上传。
     */
    @OperationLog(module = "SEAL", action = "APPROVE", targetId = "#id")
    public AdSealRecord approve(String id, AdSealApproveRequest request, String userId, String orgId) {
        assertRole(ROLE_BOSS);
        AdSealRecord s = requireSeal(id);
        if (s.getStatus() != AdSealRecordStatus.APPROVING.getCode()) {
            throw new GenericException("仅审批中的用印记录可被审批");
        }
        long now = System.currentTimeMillis();
        s.setStatus(AdSealRecordStatus.APPROVED.getCode());
        s.setActualCopies(request == null ? null : request.getActualCopies());
        s.setApproverId(userId);
        s.setApprovedAt(new Date());
        s.setApproveRemark(request == null ? null : request.getApproveRemark());
        s.setUpdateUser(userId);
        s.setUpdateTime(now);
        sealRecordMapper.update(s);

        AdContract contract = requireContract(s.getContractId());
        boolean hasFile = contract.getFileUrl() != null && !contract.getFileUrl().isBlank();
        if (hasFile) {
            contract.setSealStatus(SealStatus.SEALED.getCode());
            contract.setUpdateUser(userId);
            contract.setUpdateTime(now);
            contractMapper.update(contract);
        }
        return s;
    }

    /**
     * 用印驳回。合同 seal_status=已驳回(30)。
     */
    @OperationLog(module = "SEAL", action = "REJECT", targetId = "#id")
    public AdSealRecord reject(String id, AdSealApproveRequest request, String userId, String orgId) {
        assertRole(ROLE_BOSS);
        AdSealRecord s = requireSeal(id);
        if (s.getStatus() != AdSealRecordStatus.APPROVING.getCode()) {
            throw new GenericException("仅审批中的用印记录可被驳回");
        }
        long now = System.currentTimeMillis();
        s.setStatus(AdSealRecordStatus.REJECTED.getCode());
        s.setApproverId(userId);
        s.setApprovedAt(new Date());
        s.setApproveRemark(request == null ? null : request.getApproveRemark());
        s.setUpdateUser(userId);
        s.setUpdateTime(now);
        sealRecordMapper.update(s);

        AdContract contract = requireContract(s.getContractId());
        contract.setSealStatus(SealStatus.REJECTED.getCode());
        contract.setUpdateUser(userId);
        contract.setUpdateTime(now);
        contractMapper.update(contract);
        return s;
    }

    /**
     * 执行（上传盖章版，L-07）：回填合同 file_url，置 seal_status=已用印(20)。需先审批通过。
     */
    @OperationLog(module = "SEAL", action = "EXECUTE", targetId = "#id")
    public AdSealRecord upload(String id, AdSealUploadRequest request, String userId, String orgId) {
        assertRole(ROLE_MEDIA);
        AdSealRecord s = requireSeal(id);
        if (s.getStatus() != AdSealRecordStatus.APPROVED.getCode()) {
            throw new GenericException("请先审批通过用印记录再上传盖章版");
        }
        if (request == null || request.getFileUrl() == null || request.getFileUrl().isBlank()) {
            throw new GenericException("盖章版合同文件URL不能为空");
        }
        AdContract contract = requireContract(s.getContractId());
        long now = System.currentTimeMillis();
        contract.setFileUrl(request.getFileUrl());
        contract.setSealStatus(SealStatus.SEALED.getCode());
        contract.setUpdateUser(userId);
        contract.setUpdateTime(now);
        contractMapper.update(contract);
        return s;
    }

    // ===================== 详情 / 分页 =====================

    /**
     * 用印记录详情：记录 + 合同编号 + 订单编号 + 业务主体名称 + 状态标签。
     */
    public AdSealRecordDetailResponse detail(String id, String userId, String orgId) {
        AdSealRecord s = requireSeal(id);
        AdSealRecordDetailResponse resp = new AdSealRecordDetailResponse();
        resp.setRecord(s);
        resp.setStatusLabel(AdSealRecordStatus.labelOf(s.getStatus()));
        AdContract contract = contractMapper.selectByPrimaryKey(s.getContractId());
        String orderNo = null;
        String businessEntityName = null;
        if (contract != null) {
            resp.setContractNo(contract.getContractNo());
            if (contract.getOrderId() != null && !contract.getOrderId().isBlank()) {
                AdOrder o = adOrderMapper.selectByPrimaryKey(contract.getOrderId());
                orderNo = o == null ? null : o.getOrderNo();
            }
            if (contract.getBusinessEntityId() != null && !contract.getBusinessEntityId().isBlank()) {
                AdBusinessEntity be = businessEntityMapper.selectByPrimaryKey(contract.getBusinessEntityId());
                businessEntityName = be == null ? null : be.getName();
            }
        }
        resp.setOrderNo(orderNo);
        resp.setBusinessEntityName(businessEntityName);
        return resp;
    }

    /**
     * 用印记录分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     */
    public PagerWithOption<List<AdSealRecordListResponse>> page(AdSealRecordPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        Page<AdSealRecordListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdSealRecordListResponse> list = extAdSealRecordMapper.pageList(request);
        for (AdSealRecordListResponse r : list) {
            r.setStatusLabel(AdSealRecordStatus.labelOf(r.getStatus()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    // ===================== 私有辅助 =====================

    private AdSealRecord requireSeal(String id) {
        AdSealRecord s = sealRecordMapper.selectByPrimaryKey(id);
        if (s == null || (s.getDeleted() != null && s.getDeleted() == 1)) {
            throw new GenericException("用印记录不存在");
        }
        return s;
    }

    private AdContract requireContract(String id) {
        AdContract c = contractMapper.selectByPrimaryKey(id);
        if (c == null || (c.getDeleted() != null && c.getDeleted() == 1)) {
            throw new GenericException("合同不存在");
        }
        return c;
    }

    // ===================== 角色守卫（best-effort，同 M2/M3） =====================

    private void assertRole(String requiredRole) {
        if (requiredRole == null) {
            return;
        }
        if (!hasRole(requiredRole)) {
            throw new GenericException("当前角色无权执行该操作: " + requiredRole);
        }
    }

    private boolean hasRole(String requiredRole) {
        if (requiredRole == null) {
            return true;
        }
        SessionUser user = cn.cordys.security.SessionUtils.getUser();
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
            case ROLE_MEDIA:
                return isAdmin || roleNames.stream()
                        .anyMatch(n -> n.contains("媒体") || n.contains("media") || n.contains("运营"));
            case ROLE_BOSS:
                return isAdmin || roleNames.stream()
                        .anyMatch(n -> n.contains("老板") || n.contains("boss"));
            default:
                return false;
        }
    }
}
