package cn.cordys.crm.ad.resource.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.common.AdEntityPermissionProvider;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.resource.constants.ResourceStatus;
import cn.cordys.crm.ad.resource.constants.ResourceType;
import cn.cordys.crm.ad.resource.domain.AdResource;
import cn.cordys.crm.ad.resource.dto.request.AdResourcePageRequest;
import cn.cordys.crm.ad.resource.dto.request.AdResourceSaveRequest;
import cn.cordys.crm.ad.resource.dto.response.AdResourceDetailResponse;
import cn.cordys.crm.ad.resource.dto.response.AdResourceListResponse;
import cn.cordys.crm.ad.resource.mapper.ExtAdResourceMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUser;
import cn.cordys.common.dto.RoleDataScopeDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 广告资源服务（M6，V3.1 广告资源管理）。
 *
 * <p>资源 CRUD + 分页（主体隔离）+ 详情。写操作标注 {@link OperationLog}。</p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdResourceService {

    @Resource
    private BaseMapper<AdResource> resourceMapper;
    @Resource
    private BaseMapper<AdBusinessEntity> businessEntityMapper;
    @Resource
    private ExtAdResourceMapper extAdResourceMapper;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;

    private static final String ROLE_MEDIA = "ROLE_MEDIA";

    /** 新建资源。 */
    @OperationLog(module = "RESOURCE", action = "CREATE", targetId = "")
    public AdResource create(AdResourceSaveRequest request, String userId, String orgId) {
        assertRole(ROLE_MEDIA);
        validate(request);

        AdResource r = new AdResource();
        r.setId(IDGenerator.nextStr());
        r.setName(request.getResourceName());
        r.setResourceType(request.getResourceType());
        r.setChannel(request.getMediaChannel());
        r.setPosition(request.getPosition());
        r.setDailyImpressions(request.getDailyImpressions());
        r.setUnitPrice(request.getUnitPrice());
        r.setStatus(request.getStatus() != null ? request.getStatus() : ResourceStatus.AVAILABLE.getCode());
        r.setRemark(request.getRemark());
        r.setBusinessEntityId(request.getBusinessEntityId());
        r.setOrganizationId(orgId);
        r.setCreateUser(userId);
        r.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        r.setCreateTime(now);
        r.setUpdateTime(now);
        resourceMapper.insert(r);
        return r;
    }

    /** 编辑资源。 */
    @OperationLog(module = "RESOURCE", action = "UPDATE", targetId = "#request.id")
    public AdResource update(AdResourceSaveRequest request, String userId, String orgId) {
        assertRole(ROLE_MEDIA);
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("资源id不能为空");
        }
        AdResource existing = requireResource(request.getId());
        String org0 = existing.getOrganizationId();
        String cu0 = existing.getCreateUser();
        Long ct0 = existing.getCreateTime();
        Integer del0 = existing.getDeleted();

        existing.setName(request.getResourceName());
        existing.setResourceType(request.getResourceType());
        existing.setChannel(request.getMediaChannel());
        existing.setPosition(request.getPosition());
        existing.setDailyImpressions(request.getDailyImpressions());
        existing.setUnitPrice(request.getUnitPrice());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setRemark(request.getRemark());
        existing.setBusinessEntityId(request.getBusinessEntityId());
        existing.setOrganizationId(org0);
        existing.setCreateUser(cu0);
        existing.setCreateTime(ct0);
        existing.setDeleted(del0);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        resourceMapper.update(existing);
        return existing;
    }

    /** 详情。 */
    public AdResourceDetailResponse detail(String id, String userId, String orgId) {
        AdResource r = requireResource(id);
        AdResourceDetailResponse resp = new AdResourceDetailResponse();
        resp.setResource(r);
        resp.setResourceTypeLabel(ResourceType.labelOf(r.getResourceType()));
        resp.setStatusLabel(ResourceStatus.labelOf(r.getStatus()));
        if (r.getBusinessEntityId() != null && !r.getBusinessEntityId().isBlank()) {
            AdBusinessEntity be = businessEntityMapper.selectByPrimaryKey(r.getBusinessEntityId());
            resp.setBusinessEntityName(be == null ? null : be.getName());
        }
        return resp;
    }

    /** 分页列表。 */
    public PagerWithOption<List<AdResourceListResponse>> page(AdResourcePageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        Page<AdResourceListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdResourceListResponse> list = extAdResourceMapper.pageList(request);
        for (AdResourceListResponse r : list) {
            r.setResourceTypeLabel(ResourceType.labelOf(r.getResourceType()));
            r.setStatusLabel(ResourceStatus.labelOf(r.getStatus()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    // ===================== 私有辅助 =====================

    private AdResource requireResource(String id) {
        AdResource r = resourceMapper.selectByPrimaryKey(id);
        if (r == null || (r.getDeleted() != null && r.getDeleted() == 1)) {
            throw new GenericException("资源不存在");
        }
        return r;
    }

    private void validate(AdResourceSaveRequest request) {
        if (request.getResourceName() == null || request.getResourceName().isBlank()) {
            throw new GenericException("资源名称不能为空");
        }
        if (ResourceType.of(request.getResourceType()) == null) {
            throw new GenericException("资源类型不合法(10线上媒体/20线下广告牌/30电视/40广播/50印刷)");
        }
    }

    // ===================== 角色守卫 =====================

    private void assertRole(String requiredRole) {
        if (requiredRole == null) return;
        if (!hasRole(requiredRole)) {
            throw new GenericException("当前角色无权执行该操作: " + requiredRole);
        }
    }

    private boolean hasRole(String requiredRole) {
        if (requiredRole == null) return true;
        SessionUser user = cn.cordys.security.SessionUtils.getUser();
        if (user == null) return false;
        List<String> roleNames = user.getRoles() == null ? Collections.emptyList()
                : user.getRoles().stream()
                .map(RoleDataScopeDTO::getName)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        boolean isAdmin = roleNames.stream()
                .anyMatch(n -> n.contains("admin") || n.contains("超级") || n.contains("管理员"));
        switch (requiredRole) {
            case ROLE_MEDIA:
                return isAdmin || roleNames.stream()
                        .anyMatch(n -> n.contains("媒体") || n.contains("media") || n.contains("运营"));
            default:
                return false;
        }
    }
}
