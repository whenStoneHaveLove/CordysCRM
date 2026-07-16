import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdPaymentCreateUrl,
  AdPaymentCancelUrl,
  AdPaymentPageUrl,
  AdPaymentDetailUrl,
  AdPaymentConfirmPrepayUrl,
  AdPaymentPayMediaPrepayUrl,
  AdPaymentInvoiceUrl,
  AdPaymentReceiveUrl,
  AdPaymentPayMediaPostpayUrl,
  AdPaymentPayMediaPostpayForceUrl,
  AdPaymentRedInvoiceClearUrl,
  AdPaymentTodoPageUrl,
} from '@lib/shared/api/requrls/adPayment';
import type {
  AdPaymentRecordCreateParams,
  AdPaymentCancelParams,
  AdPaymentRecordPageParams,
  AdPaymentRecordPageResult,
  AdPaymentRecordDetail,
  AdPaymentRecordInfo,
  AdPaymentConfirmPrepayParams,
  AdPaymentInvoiceParams,
  AdPaymentReceiveParams,
  AdPaymentMediaPrepayParams,
  AdPaymentRedInvoiceClearParams,
  AdPaymentTodoParams,
  AdPaymentTodoResult,
  AdOrderInfo,
} from '@lib/shared/models/advertising';

export default function useAdPaymentApi(CDR: CordysAxios) {
  // 收付款登记（通用：预收/预付/开票收款/媒体尾款/退款/坏账）
  function createAdPayment(data: AdPaymentRecordCreateParams) {
    return CDR.post<AdPaymentRecordInfo>({ url: AdPaymentCreateUrl, data });
  }

  // 撤销/冲销收付款记录
  function cancelAdPayment(data: AdPaymentCancelParams) {
    return CDR.post<AdPaymentRecordInfo>({ url: AdPaymentCancelUrl, data });
  }

  // 收付款记录分页
  function getAdPaymentPage(data: AdPaymentRecordPageParams) {
    return CDR.post<AdPaymentRecordPageResult>({ url: AdPaymentPageUrl, data }, { ignoreCancelToken: true });
  }

  // 收付款记录详情
  function getAdPaymentDetail(id: string) {
    return CDR.get<AdPaymentRecordDetail>({ url: `${AdPaymentDetailUrl}/${id}` });
  }

  // 确认预收款（基数=应收）
  function confirmPrepayAdPayment(data: AdPaymentConfirmPrepayParams) {
    return CDR.post<AdPaymentRecordInfo>({ url: AdPaymentConfirmPrepayUrl, data });
  }

  // 付媒体预付款（基数=media_payable）
  function payMediaPrepayAdPayment(data: AdPaymentMediaPrepayParams) {
    return CDR.post<AdPaymentRecordInfo>({ url: AdPaymentPayMediaPrepayUrl, data });
  }

  // 开票（仅回写已开票）
  function invoiceAdPayment(data: AdPaymentInvoiceParams) {
    return CDR.post<AdPaymentRecordInfo>({ url: AdPaymentInvoiceUrl, data });
  }

  // 收款登记（仅回写已收款）
  function receiveAdPayment(data: AdPaymentReceiveParams) {
    return CDR.post<AdPaymentRecordInfo>({ url: AdPaymentReceiveUrl, data });
  }

  // 付媒体尾款
  function payMediaPostpayAdPayment(data: AdPaymentMediaPrepayParams) {
    return CDR.post<AdPaymentRecordInfo>({ url: AdPaymentPayMediaPostpayUrl, data });
  }

  // 付媒体尾款-手动强制触发
  function payMediaPostpayForceAdPayment(data: AdPaymentMediaPrepayParams) {
    return CDR.post<AdPaymentRecordInfo>({ url: AdPaymentPayMediaPostpayForceUrl, data });
  }

  // 清除红冲标记
  function clearRedInvoiceAdPayment(data: AdPaymentRedInvoiceClearParams) {
    return CDR.post<AdOrderInfo>({ url: AdPaymentRedInvoiceClearUrl, data });
  }

  // 财务待办
  function getAdPaymentTodoPage(data: AdPaymentTodoParams) {
    return CDR.post<AdPaymentTodoResult>({ url: AdPaymentTodoPageUrl, data }, { ignoreCancelToken: true });
  }

  return {
    createAdPayment,
    cancelAdPayment,
    getAdPaymentPage,
    getAdPaymentDetail,
    confirmPrepayAdPayment,
    payMediaPrepayAdPayment,
    invoiceAdPayment,
    receiveAdPayment,
    payMediaPostpayAdPayment,
    payMediaPostpayForceAdPayment,
    clearRedInvoiceAdPayment,
    getAdPaymentTodoPage,
  };
}
