package cn.cordys.crm.ad.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 广告报表扩展 Mapper（M6，纯查询；仅读已有 M1~M4 表）。
 * XML 与本接口同目录（src/main/java/.../report/mapper/ExtAdReportMapper.xml）。
 */
@Mapper
public interface ExtAdReportMapper {

    /**
     * 订单汇总：按状态统计数量和金额。
     */
    List<Map<String, Object>> orderSummary(@Param("organizationId") String organizationId);

    /**
     * 付款汇总：已付/未付/逾期金额。
     */
    Map<String, BigDecimal> paymentSummary(@Param("organizationId") String organizationId);

    /**
     * 月度趋势：指定年份每月订单数+金额。
     */
    List<Map<String, Object>> monthlyTrend(@Param("organizationId") String organizationId,
                                           @Param("year") int year);

    /**
     * 工作台待办聚合计数（B-5）：单次查询返回所有角色所需计数。
     */
    Map<String, Object> workbenchCounts(@Param("organizationId") String organizationId);

    /**
     * 工作台首页数据概览：进行中订单数 + 应收/媒体应付总额 + 待收/待付款。
     * 待收/待付仅统计收款单/付款单尚未审核通过的订单（receipt_done=0 / payment_done=0）。
     */
    Map<String, BigDecimal> dashboardSummary(@Param("organizationId") String organizationId);

    /**
     * 报表-应收应付：应收总额 vs 媒体应付总额（B-6）。
     */
    Map<String, BigDecimal> receivablePayable(@Param("organizationId") String organizationId);

    /**
     * 报表-回款追踪：已收款 vs 已开票（B-6）。
     */
    Map<String, BigDecimal> collection(@Param("organizationId") String organizationId);

    /**
     * 报表-媒体投放：已付媒体款（B-6）。
     */
    Map<String, BigDecimal> mediaInvestment(@Param("organizationId") String organizationId);

    /**
     * 报表-缺合同跟踪：缺合同的订单列表（B-6）。
     */
    List<Map<String, Object>> contractMissing(@Param("organizationId") String organizationId);

    /**
     * 报表-用印统计：按用印状态分布（B-6）。
     */
    List<Map<String, Object>> sealStatusSummary(@Param("organizationId") String organizationId);
}
