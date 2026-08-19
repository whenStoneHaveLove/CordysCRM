package cn.cordys.crm.ad.approval.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 广告审批中心 XML Mapper（聚合查询六类待审批项）。
 * SQL 在同目录 ExtAdApprovalMapper.xml。
 *
 * <p>六类待审单据及其状态条件：</p>
 * <ul>
 *   <li>订单：ad_order.status = 10（待管理组审核）</li>
 *   <li>改单：ad_order_change.status = 10（已提交）</li>
 *   <li>用印：ad_seal_record.status = 0（审批中）</li>
 *   <li>归档：ad_contract.seal_status = 40（归档审批中）</li>
 *   <li>收款单：ad_receipt.status = 10（待审核）</li>
 *   <li>付款单：ad_payout.status = 10（待审核）</li>
 * </ul>
 */
@Mapper
public interface ExtAdApprovalMapper {

    List<Map<String, Object>> pendingOrders(@Param("orgId") String orgId, @Param("status") int status);

    List<Map<String, Object>> pendingChanges(@Param("orgId") String orgId, @Param("status") int status);

    List<Map<String, Object>> pendingSeals(@Param("orgId") String orgId, @Param("status") int status);

    List<Map<String, Object>> pendingArchives(@Param("orgId") String orgId, @Param("sealStatus") int sealStatus);

    List<Map<String, Object>> pendingReceipts(@Param("orgId") String orgId, @Param("status") int status);

    List<Map<String, Object>> pendingPayouts(@Param("orgId") String orgId, @Param("status") int status);
}
