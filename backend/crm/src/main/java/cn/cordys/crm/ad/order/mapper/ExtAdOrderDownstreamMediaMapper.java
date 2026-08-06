package cn.cordys.crm.ad.order.mapper;

import cn.cordys.crm.ad.order.domain.AdOrderDownstreamMedia;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtAdOrderDownstreamMediaMapper extends BaseMapper<AdOrderDownstreamMedia> {

    @Select("SELECT * FROM ad_order_downstream_media WHERE order_id = #{orderId} AND deleted = 0")
    List<AdOrderDownstreamMedia> selectByOrderId(@Param("orderId") String orderId);

    /** 查询同订单+同媒体的记录（含已删除），用于复用避免唯一键冲突 */
    @Select("SELECT * FROM ad_order_downstream_media WHERE order_id = #{orderId} AND downstream_media_id = #{mediaId} LIMIT 1")
    AdOrderDownstreamMedia selectByOrderIdAndMediaId(@Param("orderId") String orderId, @Param("mediaId") String mediaId);
}
