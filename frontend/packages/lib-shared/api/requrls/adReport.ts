// 报表中心后端 REST 端点（AdReportController: /api/ad/report）。
// 已实现：order-summary / payment-summary / monthly-trend。
// 后端待补（B-6）：PRD §9.5 的 6 类 GET /api/ad/report/{code}（order-execution / receivable-payable /
// collection / media / contract-missing / seal）。
export const AdReportOrderSummaryUrl = '/api/ad/report/order-summary';
export const AdReportPaymentSummaryUrl = '/api/ad/report/payment-summary';
export const AdReportMonthlyTrendUrl = '/api/ad/report/monthly-trend';
export const AdReportByCodeUrl = '/api/ad/report';
