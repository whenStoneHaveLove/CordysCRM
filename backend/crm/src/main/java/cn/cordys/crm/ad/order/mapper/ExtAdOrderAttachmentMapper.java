package cn.cordys.crm.ad.order.mapper;

import cn.cordys.crm.ad.order.domain.AdOrderAttachment;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtAdOrderAttachmentMapper extends BaseMapper<AdOrderAttachment> {

    @Select("SELECT * FROM ad_order_attachment WHERE order_id = #{orderId} AND deleted = 0")
    List<AdOrderAttachment> selectByOrderId(@Param("orderId") String orderId);

    @Select("SELECT * FROM ad_order_attachment WHERE order_id = #{orderId} AND type = #{type} AND deleted = 0")
    List<AdOrderAttachment> selectByOrderIdAndType(@Param("orderId") String orderId, @Param("type") int type);
}
