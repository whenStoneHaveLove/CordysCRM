package cn.cordys.crm.ad.downstreammedia.mapper;

import cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMediaAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtAdDownstreamMediaAccountMapper {

    /**
     * 查询某下游客户的账户列表（未删除）。
     */
    List<AdDownstreamMediaAccount> selectByDownstreamMediaId(@Param("downstreamMediaId") String downstreamMediaId);

    /**
     * 查询某下游客户下最早一条“可用”账户（启用且未删除），用于默认回带。
     */
    AdDownstreamMediaAccount selectFirstAvailable(@Param("downstreamMediaId") String downstreamMediaId);

    /**
     * 批量查询多个下游客户的账户列表（未删除）。key 为 downstream_media_id。
     */
    @org.apache.ibatis.annotations.Select("""
            SELECT id, downstream_media_id, payee_name, bank_name, bank_account, disabled,
                   organization_id, create_time, update_time, create_user, update_user, deleted
            FROM ad_downstream_media_account
            WHERE deleted = 0
              AND downstream_media_id IN
              <foreach item='mid' collection='downstreamMediaIds' open='(' separator=',' close=')'>
                  #{mid}
              </foreach>
            ORDER BY create_time ASC, id ASC
            """)
    java.util.List<AdDownstreamMediaAccount> selectByDownstreamMediaIds(@Param("downstreamMediaIds") java.util.List<String> downstreamMediaIds);
}
