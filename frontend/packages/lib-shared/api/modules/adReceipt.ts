import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdReceiptCreateUrl,
  AdReceiptUpdateUrl,
  AdReceiptDetailUrl,
  AdReceiptPageUrl,
  AdReceiptRemainingUrl,
} from '@lib/shared/api/requrls/adReceipt';
import type {
  AdReceiptSaveParams,
  AdReceiptApproveParams,
  AdReceiptPageParams,
  AdReceiptPageResult,
  AdReceiptInfo,
  AdReceiptDetail,
} from '@lib/shared/models/advertising';

export default function useAdReceiptApi(CDR: CordysAxios) {
  // 新建收款单
  function createAdReceipt(data: AdReceiptSaveParams) {
    return CDR.post<AdReceiptInfo>({ url: AdReceiptCreateUrl, data });
  }

  // 编辑收款单
  function updateAdReceipt(data: AdReceiptSaveParams) {
    return CDR.put<AdReceiptInfo>({ url: AdReceiptUpdateUrl, data });
  }

  // 提交收款单
  function submitAdReceipt(id: string) {
    return CDR.post<AdReceiptInfo>({ url: `${AdReceiptUpdateUrl}/${id}/submit` });
  }

  // 审核收款单
  function approveAdReceipt(id: string, data: AdReceiptApproveParams) {
    return CDR.put<AdReceiptInfo>({ url: `${AdReceiptUpdateUrl}/${id}/approve`, data });
  }

  // 收款单详情
  function getAdReceiptDetail(id: string) {
    return CDR.get<AdReceiptDetail>({ url: `${AdReceiptDetailUrl}/${id}` });
  }

  // 订单剩余应收金额
  function getAdReceiptRemaining(orderId: string) {
    return CDR.get<number>({ url: `${AdReceiptRemainingUrl}/${orderId}` });
  }

  // 收款单分页
  function getAdReceiptPage(data: AdReceiptPageParams) {
    return CDR.post<AdReceiptPageResult>({ url: AdReceiptPageUrl, data }, { ignoreCancelToken: true });
  }

  // 删除收款单
  function deleteAdReceipt(id: string) {
    return CDR.delete<void>({ url: `${AdReceiptUpdateUrl}/${id}` });
  }

  return {
    createAdReceipt,
    updateAdReceipt,
    submitAdReceipt,
    approveAdReceipt,
    getAdReceiptDetail,
    getAdReceiptRemaining,
    getAdReceiptPage,
    deleteAdReceipt,
  };
}
