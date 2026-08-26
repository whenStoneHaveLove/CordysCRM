package cn.cordys.crm.ad.payout.mapper;

import cn.cordys.crm.ad.payout.dto.response.AdPayoutMediaOptionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 付款单选下拉查询（JOIN 字典填充名称 + 返点字段 + 累计已付）。
 */
@Mapper
public interface ExtAdPayoutMediaOptionMapper {

    @Select("""
            SELECT
                odm.id                    AS id,
                odm.downstream_media_id   AS mediaId,
                d.name                    AS mediaName,
                odm.payable_amount        AS payableAmount,
                odm.no_rebate_amount      AS noRebateAmount,
                odm.rebate_mode           AS rebateMode,
                odm.rebate_value          AS rebateValue,
                odm.rebate_amount         AS rebateAmount,
                odm.actual_payable        AS actualPayable,
                IFNULL(paid.total_paid, 0) AS paidAmount
            FROM ad_order_downstream_media odm
            INNER JOIN ad_downstream_media d
                ON d.id = odm.downstream_media_id
               AND d.deleted = 0
            LEFT JOIN (
                SELECT pm.order_downstream_media_id,
                       SUM(pm.paid_amount) AS total_paid
                FROM ad_payment_media pm
                INNER JOIN ad_payment p
                    ON p.id = pm.payment_id
                   AND p.deleted = 0
                WHERE pm.deleted = 0
                  AND p.status IN (0, 10, 20)
                GROUP BY pm.order_downstream_media_id
            ) paid ON paid.order_downstream_media_id = odm.id
            WHERE odm.order_id = #{orderId}
              AND odm.deleted = 0
            ORDER BY odm.create_time ASC
            """)
    List<AdPayoutMediaOptionResponse> selectMediaOptions(@Param("orderId") String orderId);
}