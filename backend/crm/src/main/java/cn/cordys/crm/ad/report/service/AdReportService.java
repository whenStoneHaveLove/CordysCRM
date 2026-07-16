package cn.cordys.crm.ad.report.service;

import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.report.mapper.ExtAdReportMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 广告报表服务（M6，V3.1 广告报表）。
 *
 * <p>纯只读聚合查询，查询已有 M1~M4 表。不标注 {@code @OperationLog}（无写操作）。</p>
 */
@Slf4j
@Service
public class AdReportService {

    @Resource
    private ExtAdReportMapper extAdReportMapper;

    /** 订单汇总：按状态统计数量和金额。 */
    public List<Map<String, Object>> orderSummary(String orgId) {
        return extAdReportMapper.orderSummary(orgId);
    }

    /** 付款汇总：已付/未付/逾期金额。 */
    public Map<String, BigDecimal> paymentSummary(String orgId) {
        return extAdReportMapper.paymentSummary(orgId);
    }

    /** 月度趋势：指定年份每月订单数+金额。 */
    public List<Map<String, Object>> monthlyTrend(String orgId, int year) {
        return extAdReportMapper.monthlyTrend(orgId, year);
    }
}
