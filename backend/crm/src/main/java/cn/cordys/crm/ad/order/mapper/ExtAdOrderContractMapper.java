package cn.cordys.crm.ad.order.mapper;

import cn.cordys.crm.ad.order.domain.AdOrderContract;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtAdOrderContractMapper extends BaseMapper<AdOrderContract> {

    @Select("SELECT * FROM ad_order_contract WHERE order_id = #{orderId} AND deleted = 0")
    List<AdOrderContract> selectByOrderId(@Param("orderId") String orderId);

    @Select("SELECT * FROM ad_order_contract WHERE order_id = #{orderId}")
    List<AdOrderContract> selectAllByOrderId(@Param("orderId") String orderId);

    @Select("SELECT * FROM ad_order_contract WHERE contract_id = #{contractId} AND deleted = 0")
    List<AdOrderContract> selectByContractId(@Param("contractId") String contractId);
}
