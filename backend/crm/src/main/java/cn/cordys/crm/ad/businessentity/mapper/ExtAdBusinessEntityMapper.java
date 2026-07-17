package cn.cordys.crm.ad.businessentity.mapper;

import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.businessentity.dto.request.AdBusinessEntityPageRequest;
import cn.cordys.crm.ad.businessentity.dto.response.AdBusinessEntityListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 业务主体 Mapper（基础 CRUD 由 {@link BaseMapper} 提供，列表查询在 M2 接入 CommonMapper）。
 */
@Mapper
public interface ExtAdBusinessEntityMapper extends BaseMapper<AdBusinessEntity> {

    @Select("SELECT * FROM ad_business_entity WHERE organization_id = #{orgId} AND deleted = 0 ORDER BY create_time DESC")
    List<AdBusinessEntity> listByOrganizationId(@Param("orgId") String orgId);

    /**
     * 业务主体分页（V3.1 §4.1，B-3）。SQL 见同目录 ExtAdBusinessEntityMapper.xml。
     */
    List<AdBusinessEntityListResponse> pageList(AdBusinessEntityPageRequest request);
}
