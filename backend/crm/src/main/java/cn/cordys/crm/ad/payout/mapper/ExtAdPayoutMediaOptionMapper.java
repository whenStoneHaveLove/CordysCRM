package cn.cordys.crm.ad.payout.mapper;

import cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaOptionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 付款单选媒体下拉查询（JOIN 媒体字典填充名称）。
 * XML 与本接口同目录。
 */
@Mapper
public interface ExtAdPayoutMediaOptionMapper {

    @Select("""
            SELECT
                odm.id          AS id,
                odm.downstream_media_id AS mediaId,
                d.name          AS mediaName
            FROM ad_order_downstream_media odm
            INNER JOIN ad_downstream_media d
                ON d.id = odm.downstream_media_id
               AND d.deleted = 0
            WHERE odm.order_id = #{orderId}
              AND odm.deleted = 0
            ORDER BY odm.create_time ASC
            """)
    List<AdPayoutMediaOptionResponse> selectMediaOptions(@Param("orderId") String orderId);
}
