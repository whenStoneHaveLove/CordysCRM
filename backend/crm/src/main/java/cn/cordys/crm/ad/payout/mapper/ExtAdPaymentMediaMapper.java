package cn.cordys.crm.ad.payout.mapper;

import cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaDetailItem;
import cn.cordys.crm.ad.payout.domain.AdPaymentMedia;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 付款单-下游客户付款返点明细 mapper。
 */
@Mapper
public interface ExtAdPaymentMediaMapper {

    /**
     * 按付款单id查询明细。
     */
    @Select("""
            SELECT
                id                       AS id,
                payment_id               AS paymentId,
                order_id                 AS orderId,
                order_downstream_media_id AS orderDownstreamMediaId,
                media_id                 AS mediaId,
                media_name               AS mediaName,
                payable_amount        AS payableAmount,
                no_rebate_amount      AS noRebateAmount,
                rebate_mode           AS rebateMode,
                rebate_value          AS rebateValue,
                rebate_amount         AS rebateAmount,
                actual_payable        AS actualPayable,
                paid_amount           AS paidAmount
            FROM ad_payment_media
            WHERE payment_id = #{paymentId}
              AND deleted = 0
            ORDER BY create_time ASC
            """)
    List<AdPayoutMediaDetailItem> selectByPaymentId(@Param("paymentId") String paymentId);

    /**
     * 物理删除某付款单下所有明细（保存时先清后插）。
     */
    @Delete("DELETE FROM ad_payment_media WHERE payment_id = #{paymentId}")
    int deleteByPaymentId(@Param("paymentId") String paymentId);

    /**
     * 查询某付款单下明细（内部用）。
     */
    @Select("SELECT * FROM ad_payment_media WHERE payment_id = #{paymentId} AND deleted = 0")
    List<AdPaymentMedia> selectEntitiesByPaymentId(@Param("paymentId") String paymentId);
}