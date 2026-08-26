package cn.cordys.crm.ad.payout.mapper;

import cn.cordys.crm.ad.payout.domain.AdPaymentMedia;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 付款单-下游客户付款返点明细 mapper（BaseMapper 提供 insert/batchInsert/delete 等基础 CRUD）。
 */
@Mapper
public interface AdPaymentMediaMapper extends BaseMapper<AdPaymentMedia> {
}