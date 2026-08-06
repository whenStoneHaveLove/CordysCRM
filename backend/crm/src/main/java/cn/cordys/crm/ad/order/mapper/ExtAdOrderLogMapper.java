package cn.cordys.crm.ad.order.mapper;

import cn.cordys.crm.ad.order.domain.AdOrderLog;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtAdOrderLogMapper extends BaseMapper<AdOrderLog> {

    @Select("SELECT * FROM ad_order_log WHERE order_id = #{orderId}")
    List<AdOrderLog> selectByOrderId(@Param("orderId") String orderId);
}
