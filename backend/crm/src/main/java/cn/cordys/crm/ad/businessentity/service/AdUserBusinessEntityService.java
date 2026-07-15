package cn.cordys.crm.ad.businessentity.service;

import cn.cordys.crm.ad.businessentity.domain.AdUserBusinessEntity;
import cn.cordys.crm.ad.businessentity.mapper.ExtAdUserBusinessEntityMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户-业务主体关联服务（L-06）。提供基础 CRUD 及按用户查询所属主体集合。
 */
@Service
public class AdUserBusinessEntityService {

    @Resource
    private ExtAdUserBusinessEntityMapper userBusinessEntityMapper;

    public String save(AdUserBusinessEntity entity) {
        userBusinessEntityMapper.insert(entity);
        return entity.getId();
    }

    public void update(AdUserBusinessEntity entity) {
        userBusinessEntityMapper.updateById(entity);
    }

    public void remove(String id) {
        userBusinessEntityMapper.deleteByPrimaryKey(id);
    }

    public AdUserBusinessEntity get(String id) {
        return userBusinessEntityMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询某用户关联的全部业务主体 id（不含已删除）。
     */
    public List<AdUserBusinessEntity> selectByUserId(String userId) {
        return userBusinessEntityMapper.selectByUserId(userId);
    }

    /**
     * 返回某用户可见的业务主体 id 集合（用于数据隔离，V3.1 §3.4）。
     */
    public List<String> selectBusinessEntityIds(String userId) {
        return userBusinessEntityMapper.selectByUserId(userId).stream()
                .map(AdUserBusinessEntity::getBusinessEntityId)
                .collect(Collectors.toList());
    }

    /**
     * 查询某业务主体下的全部用户关联。
     */
    public List<AdUserBusinessEntity> selectByBusinessEntityId(String businessEntityId) {
        return userBusinessEntityMapper.selectByBusinessEntityId(businessEntityId);
    }
}
