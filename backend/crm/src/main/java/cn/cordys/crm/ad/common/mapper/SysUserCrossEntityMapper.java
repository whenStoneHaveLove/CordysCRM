package cn.cordys.crm.ad.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * sys_user 跨主体标记查询（L-06）。仅读取开源已有表的新增列 is_cross_entity。
 */
@Mapper
public interface SysUserCrossEntityMapper {

    @Select("SELECT is_cross_entity FROM sys_user WHERE id = #{userId}")
    Integer selectIsCrossEntity(@Param("userId") String userId);
}
