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
                m.id                       AS id,
                m.payment_id               AS paymentId,
                m.order_id                 AS orderId,
                m.order_downstream_media_id AS orderDownstreamMediaId,
                m.media_id                 AS mediaId,
                m.media_name               AS mediaName,
                m.payable_amount        AS payableAmount,
                m.no_rebate_amount      AS noRebateAmount,
                m.rebate_mode           AS rebateMode,
                m.rebate_value          AS rebateValue,
                m.rebate_amount         AS rebateAmount,
                m.actual_payable        AS actualPayable,
                m.paid_amount           AS paidAmount,
                m.account_id            AS accountId,
                acc.payee_name          AS payeeName,
                acc.bank_name           AS bankName,
                acc.bank_account        AS bankAccount,
                acc.disabled            AS accountDisabled
            FROM ad_payment_media m
            LEFT JOIN ad_downstream_media_account acc ON acc.id = m.account_id AND acc.deleted = 0
            WHERE m.payment_id = #{paymentId}
              AND m.deleted = 0
            ORDER BY m.create_time ASC
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