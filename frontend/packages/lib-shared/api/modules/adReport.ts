import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdReportOrderSummaryUrl,
  AdReportPaymentSummaryUrl,
  AdReportMonthlyTrendUrl,
  AdReportByCodeUrl,
} from '@lib/shared/api/requrls/adReport';
import type {
  AdReportOrderSummary,
  AdReportPaymentSummary,
  AdReportMonthlyTrendResult,
  AdReportItem,
} from '@lib/shared/models/advertising';

export default function useAdReportApi(CDR: CordysAxios) {
  // 订单执行汇总（按状态统计数量/金额）
  function getAdReportOrderSummary(params?: Record<string, any>) {
    return CDR.get<AdReportOrderSummary>({ url: AdReportOrderSummaryUrl, params });
  }

  // 应收应付汇总（已付/未付/逾期）
  function getAdReportPaymentSummary(params?: Record<string, any>) {
    return CDR.get<AdReportPaymentSummary>({ url: AdReportPaymentSummaryUrl, params });
  }

  // 月度趋势
  function getAdReportMonthlyTrend(params: { year: number }) {
    return CDR.get<AdReportMonthlyTrendResult>({ url: AdReportMonthlyTrendUrl, params });
  }

  // 6 类报表（PRD §9.5，后端待补 B-6）：GET /api/ad/report/{code}
  function getAdReportByCode(code: string, params?: Record<string, any>) {
    return CDR.get<AdReportItem>({ url: `${AdReportByCodeUrl}/${code}`, params });
  }

  return {
    getAdReportOrderSummary,
    getAdReportPaymentSummary,
    getAdReportMonthlyTrend,
    getAdReportByCode,
  };
}
