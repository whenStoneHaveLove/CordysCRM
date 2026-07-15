package cn.cordys.crm.ad.businessentity.service;

import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.businessentity.mapper.ExtAdBusinessEntityMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务主体服务（V3.1 §4.1）。M1 提供基础 CRUD，列表隔离在 M2 接入。
 */
@Service
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

    public AdBusinessEntity get(String id) {
        return businessEntityMapper.selectByPrimaryKey(id);
    }

    public List<AdBusinessEntity> listByOrganizationId(String organizationId) {
        return businessEntityMapper.listByOrganizationId(organizationId);
    }
}
