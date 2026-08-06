package cn.cordys.crm.ad.upstreamagent.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.common.AdEntityPermissionProvider;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.upstreamagent.domain.AdUpstreamAgent;
import cn.cordys.crm.ad.upstreamagent.dto.request.AdUpstreamAgentPageRequest;
import cn.cordys.crm.ad.upstreamagent.dto.request.AdUpstreamAgentSaveRequest;
import cn.cordys.crm.ad.upstreamagent.dto.response.AdUpstreamAgentDetailResponse;
import cn.cordys.crm.ad.upstreamagent.dto.response.AdUpstreamAgentListResponse;
import cn.cordys.crm.ad.upstreamagent.mapper.ExtAdUpstreamAgentMapper;
import cn.cordys.mybatis.BaseMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdUpstreamAgentService {

    @Resource
    private BaseMapper<AdUpstreamAgent> agentMapper;
    @Resource
    private BaseMapper<AdBusinessEntity> businessEntityMapper;
    @Resource
    private ExtAdUpstreamAgentMapper extUpstreamAgentMapper;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;

    private static final int STATUS_NORMAL = 10;
    private static final int STATUS_DISABLED = 20;

    @OperationLog(module = "UPSTREAM_AGENT", action = "CREATE")
    public AdUpstreamAgent create(AdUpstreamAgentSaveRequest request, String userId, String orgId) {
        validate(request);
        AdUpstreamAgent agent = new AdUpstreamAgent();
        agent.setId(IDGenerator.nextStr());
        fillEntity(agent, request, userId, orgId);
        agent.setCreateUser(userId);
        agent.setCreateTime(System.currentTimeMillis());
        agentMapper.insert(agent);
        return agent;
    }

    @OperationLog(module = "UPSTREAM_AGENT", action = "UPDATE", targetId = "#request.id")
    public AdUpstreamAgent update(AdUpstreamAgentSaveRequest request, String userId, String orgId) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("id不能为空");
        }
        AdUpstreamAgent existing = requireAgent(request.getId());
        fillEntity(existing, request, userId, orgId);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        agentMapper.update(existing);
        return existing;
    }

    public AdUpstreamAgentDetailResponse detail(String id, String userId, String orgId) {
        AdUpstreamAgent agent = requireAgent(id);
        AdUpstreamAgentDetailResponse resp = new AdUpstreamAgentDetailResponse();
        resp.setAgent(agent);
        resp.setStatusLabel(agent.getStatus() != null && agent.getStatus() == STATUS_DISABLED ? "停用" : "正常");
        resp.setCooperationStatusLabel(agent.getCooperationStatus() != null && agent.getCooperationStatus() == STATUS_DISABLED ? "停用" : "正常");
        if (agent.getBusinessEntityId() != null && !agent.getBusinessEntityId().isBlank()) {
            AdBusinessEntity be = businessEntityMapper.selectByPrimaryKey(agent.getBusinessEntityId());
            resp.setBusinessEntityName(be == null ? null : be.getName());
        }
        return resp;
    }

    public PagerWithOption<List<AdUpstreamAgentListResponse>> page(AdUpstreamAgentPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        Page<AdUpstreamAgentListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdUpstreamAgentListResponse> list = extUpstreamAgentMapper.pageList(request);
        for (AdUpstreamAgentListResponse r : list) {
            r.setStatusLabel(r.getStatus() != null && r.getStatus() == STATUS_DISABLED ? "停用" : "正常");
            r.setCooperationStatusLabel(r.getCooperationStatus() != null && r.getCooperationStatus() == STATUS_DISABLED ? "停用" : "正常");
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    @OperationLog(module = "UPSTREAM_AGENT", action = "DELETE", targetId = "#id")
    public void delete(String id, String userId, String orgId) {
        AdUpstreamAgent existing = requireAgent(id);
        existing.setDeleted(1);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        agentMapper.update(existing);
    }

    // ========== helpers ==========

    private void fillEntity(AdUpstreamAgent agent, AdUpstreamAgentSaveRequest request, String userId, String orgId) {
        agent.setName(request.getName());
        agent.setCreditCode(request.getCreditCode());
        agent.setSigningEntity(request.getSigningEntity());
        agent.setContactPerson(request.getContactPerson());
        agent.setContactPhone(request.getContactPhone());
        agent.setCooperationStatus(request.getCooperationStatus() != null ? request.getCooperationStatus() : STATUS_NORMAL);
        agent.setStatus(request.getStatus() != null ? request.getStatus() : STATUS_NORMAL);
        agent.setRemark(request.getRemark());
        agent.setBusinessEntityId(request.getBusinessEntityId());
        agent.setOrganizationId(orgId);
    }

    private AdUpstreamAgent requireAgent(String id) {
        AdUpstreamAgent agent = agentMapper.selectByPrimaryKey(id);
        if (agent == null || (agent.getDeleted() != null && agent.getDeleted() == 1)) {
            throw new GenericException("上游代理不存在");
        }
        return agent;
    }

    private void validate(AdUpstreamAgentSaveRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new GenericException("代理名称不能为空");
        }
    }
}
