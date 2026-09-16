package cn.cordys.crm.ad.order.mapper;

import cn.cordys.crm.ad.order.domain.AdOrderDownstreamMedia;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ExtAdOrderDownstreamMediaMapper extends BaseMapper<AdOrderDownstreamMedia> {

    @Select("SELECT * FROM ad_order_downstream_media WHERE order_id = #{orderId} AND deleted = 0")
    List<AdOrderDownstreamMedia> selectByOrderId(@Param("orderId") String orderId);

    /** 查询同订单+同的记录（含已删除），用于复用避免唯一键冲突 */
    @Select("SELECT * FROM ad_order_downstream_media WHERE order_id = #{orderId} AND downstream_media_id = #{mediaId} LIMIT 1")
    AdOrderDownstreamMedia selectByOrderIdAndMediaId(@Param("orderId") String orderId, @Param("mediaId") String mediaId);

    /**
     * 全字段覆盖更新（**含 null**，即允许把列显式清空）。
     *
     * <p>必须走自定义 SQL：{@link BaseMapper#update} 与 {@link BaseMapper#updateById}
     * 生成的语句对每个非主键列都包了 {@code <if test="col != null">}，
     * null 值不会出现在 SET 里 → 「清空某字段」会静默变成「保留旧值」。</p>
     *
     * <p>明细是整表覆盖语义（改单把预付改成后付时要把预付三列清空），故复用已存在行时必须用本方法。</p>
     */
    @Update({
            "UPDATE ad_order_downstream_media SET",
            "order_id = #{orderId}, downstream_media_id = #{downstreamMediaId}, organization_id = #{organizationId},",
            "create_time = #{createTime}, deleted = #{deleted},",
            "payable_amount = #{payableAmount}, no_rebate_amount = #{noRebateAmount},",
            "rebate_mode = #{rebateMode}, rebate_value = #{rebateValue}, rebate_amount = #{rebateAmount},",
            "actual_payable = #{actualPayable},",
            "payment_method = #{paymentMethod}, payment_prepay_mode = #{paymentPrepayMode},",
            "payment_prepay_ratio = #{paymentPrepayRatio}, payment_prepay_amount = #{paymentPrepayAmount},",
            "payment_prepay_deadline = #{paymentPrepayDeadline},",
            "payment_postpay_trigger = #{paymentPostpayTrigger}, payment_postpay_days = #{paymentPostpayDays}",
            "WHERE id = #{id}"
    })
    int updateFull(AdOrderDownstreamMedia entity);
}
