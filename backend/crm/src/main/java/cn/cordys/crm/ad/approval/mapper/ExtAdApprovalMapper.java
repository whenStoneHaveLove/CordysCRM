package cn.cordys.crm.ad.approval.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 广告审批中心 XML Mapper（聚合查询 ad_order / ad_order_change / ad_seal_record 待审项）。
 * SQL 在同目录 ExtAdApprovalMapper.xml。
 */
@Mapper
public interface ExtAdApprovalMapper {

    List<Map<String, Object>> pendingOrders(@Param("orgId") String orgId, @Param("status") int status);

    List<Map<String, Object>> pendingChanges(@Param("orgId") String orgId);

    List<Map<String, Object>> pendingSeals(@Param("orgId") String orgId);
}
