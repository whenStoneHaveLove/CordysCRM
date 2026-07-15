package cn.cordys.crm.ad.businessentity.mapper;

import cn.cordys.crm.ad.businessentity.domain.AdUserBusinessEntity;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户-业务主体关联 Mapper（L-06）。
 */
@Mapper
public interface ExtAdUserBusinessEntityMapper extends BaseMapper<AdUserBusinessEntity> {

    @Select("SELECT * FROM ad_user_business_entity WHERE user_id = #{userId} AND deleted = 0")
    List<AdUserBusinessEntity> selectByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM ad_user_business_entity WHERE business_entity_id = #{businessEntityId} AND deleted = 0")
    List<AdUserBusinessEntity> selectByBusinessEntityId(@Param("businessEntityId") String businessEntityId);
}
