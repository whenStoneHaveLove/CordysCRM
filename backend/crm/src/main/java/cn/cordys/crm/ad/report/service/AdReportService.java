package cn.cordys.crm.ad.report.service;

import cn.cordys.common.exception.GenericException;
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

    /**
     * 配置化报表分发（B-6，PRD §9.5）。
     *
     * @param code 报表编码：order-execution / receivable-payable / collection / media / contract-missing / seal
     * @return 各报表的结构化统计结果
     */
    public Object reportByCode(String code, String orgId) {
        if (code == null || code.isBlank()) {
            throw new GenericException("报表编码不能为空");
        }
        switch (code) {
            case "order-execution":
                return orderSummary(orgId);
            case "receivable-payable":
                return receivablePayable(orgId);
            case "collection":
                return collection(orgId);
            case "media":
                return mediaInvestment(orgId);
            case "contract-missing":
                return contractMissing(orgId);
            case "seal":
                return sealStatusSummary(orgId);
            default:
                throw new GenericException("未知报表编码: " + code);
        }
    }

    /** 报表-应收应付：应收总额 vs 应付总额（B-6）。 */
    public Map<String, BigDecimal> receivablePayable(String orgId) {
        return extAdReportMapper.receivablePayable(orgId);
    }

    /** 报表-回款追踪：已收款 vs 已开票（B-6）。 */
    public Map<String, BigDecimal> collection(String orgId) {
        return extAdReportMapper.collection(orgId);
    }

    /** 报表-投放：已付款（B-6）。 */
    public Map<String, BigDecimal> mediaInvestment(String orgId) {
        return extAdReportMapper.mediaInvestment(orgId);
    }

    /** 报表-缺合同跟踪：缺合同的订单列表（B-6）。 */
    public List<Map<String, Object>> contractMissing(String orgId) {
        return extAdReportMapper.contractMissing(orgId);
    }

    /** 报表-用印统计：按用印状态分布（B-6）。 */
    public List<Map<String, Object>> sealStatusSummary(String orgId) {
        return extAdReportMapper.sealStatusSummary(orgId);
    }
}
