package cn.cordys.crm.ad.businessentity.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.businessentity.constants.BusinessEntityStatus;
import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.businessentity.dto.request.AdBusinessEntityPageRequest;
import cn.cordys.crm.ad.businessentity.dto.request.AdBusinessEntitySaveRequest;
import cn.cordys.crm.ad.businessentity.dto.response.AdBusinessEntityDetailResponse;
import cn.cordys.crm.ad.businessentity.dto.response.AdBusinessEntityListResponse;
import cn.cordys.crm.ad.businessentity.mapper.ExtAdBusinessEntityMapper;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业务主体服务（V3.1 §4.1）。M1 提供基础 CRUD，列表隔离在 M2 接入。
 *
 * <p>B-3 补齐：新增 {@code create/update/detail/page} 以支撑 {@code AdBusinessEntityController}
 * （POST/PUT/GET /{id}/POST page）。分页复用 {@code ExtAdBusinessEntityMapper.pageList}（PageHelper）。</p>
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AdBusinessEntityService {

    @Resource
    private ExtAdBusinessEntityMapper businessEntityMapper;

    public String save(AdBusinessEntity entity) {
        businessEntityMapper.insert(entity);
        return entity.getId();
    }

    public void update(AdBusinessEntity entity) {
        businessEntityMapper.updateById(entity);
    }

    public void remove(String id) {
        businessEntityMapper.deleteByPrimaryKey(id);
    }

    /**
     * 停用业务主体（status 10→20）。
     */
    @OperationLog(module = "AD_BUSINESS_ENTITY", action = "DISABLE", targetId = "#id")
    public void disable(String id) {
        AdBusinessEntity entity = get(id);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new GenericException("业务主体不存在");
        }
        entity.setStatus(BusinessEntityStatus.DISABLED.getCode());
        businessEntityMapper.updateById(entity);
    }

    public AdBusinessEntity get(String id) {
        return businessEntityMapper.selectByPrimaryKey(id);
    }

    public List<AdBusinessEntity> listByOrganizationId(String organizationId) {
        return businessEntityMapper.listByOrganizationId(organizationId);
    }

    /** 新建业务主体（B-3）。 */
    @OperationLog(module = "AD_BUSINESS_ENTITY", action = "CREATE", targetId = "")
    public AdBusinessEntity create(AdBusinessEntitySaveRequest request, String userId, String orgId) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new GenericException("主体名称不能为空");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new GenericException("主体代码不能为空");
        }
        AdBusinessEntity entity = new AdBusinessEntity();
        entity.setId(IDGenerator.nextStr());
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : BusinessEntityStatus.ENABLED.getCode());
        entity.setIsCrossEntity(request.getIsCrossEntity() != null ? request.getIsCrossEntity() : 0);
        entity.setRemark(request.getRemark());
        entity.setOrganizationId(orgId);
        entity.setCreateUser(userId);
        entity.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        businessEntityMapper.insert(entity);
        return entity;
    }

    /** 编辑业务主体（B-3）。 */
    @OperationLog(module = "AD_BUSINESS_ENTITY", action = "UPDATE", targetId = "#request.id")
    public AdBusinessEntity update(AdBusinessEntitySaveRequest request, String userId, String orgId) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("主体id不能为空");
        }
        AdBusinessEntity existing = requireEntity(request.getId());
        existing.setName(request.getName());
        existing.setCode(request.getCode());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getIsCrossEntity() != null) {
            existing.setIsCrossEntity(request.getIsCrossEntity());
        }
        existing.setRemark(request.getRemark());
        existing.setOrganizationId(orgId);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        businessEntityMapper.updateById(existing);
        return existing;
    }

    /** 业务主体详情（B-3）。 */
    public AdBusinessEntityDetailResponse detail(String id, String userId, String orgId) {
        AdBusinessEntity entity = requireEntity(id);
        AdBusinessEntityDetailResponse resp = new AdBusinessEntityDetailResponse();
        resp.setEntity(entity);
        resp.setStatusLabel(BusinessEntityStatus.labelOf(entity.getStatus()));
        return resp;
    }

    /** 业务主体分页（B-3）。 */
    public cn.cordys.common.pager.PagerWithOption<List<AdBusinessEntityListResponse>> page(
            AdBusinessEntityPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        Page<AdBusinessEntityListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdBusinessEntityListResponse> list = businessEntityMapper.pageList(request);
        for (AdBusinessEntityListResponse r : list) {
            r.setStatusLabel(BusinessEntityStatus.labelOf(r.getStatus()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    // ===================== 私有辅助 =====================

    private AdBusinessEntity requireEntity(String id) {
        AdBusinessEntity entity = businessEntityMapper.selectByPrimaryKey(id);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new GenericException("业务主体不存在");
        }
        return entity;
    }
}
