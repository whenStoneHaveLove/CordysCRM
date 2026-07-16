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
}
