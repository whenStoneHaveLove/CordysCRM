package cn.cordys.crm.ad.payout.mapper;

import cn.cordys.crm.ad.payout.domain.AdPayoutInvoice;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 付款单发票 Mapper。
 */
@Mapper
public interface ExtAdPayoutInvoiceMapper extends BaseMapper<AdPayoutInvoice> {

    @Select("SELECT * FROM ad_payout_invoice WHERE payout_id = #{payoutId} LIMIT 1")
    AdPayoutInvoice selectByPayoutId(@Param("payoutId") String payoutId);
}
