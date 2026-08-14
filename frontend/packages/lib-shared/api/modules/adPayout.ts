import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdPayoutCreateUrl,
  AdPayoutUpdateUrl,
  AdPayoutDetailUrl,
  AdPayoutPageUrl,
  AdPayoutRemainingUrl,
  AdPayoutMediaUrl,
} from '@lib/shared/api/requrls/adPayout';
import type {
  AdPayoutSaveParams,
  AdPayoutApproveParams,
  AdPayoutPageParams,
  AdPayoutPageResult,
  AdPayoutInfo,
} from '@lib/shared/models/advertising';

export default function useAdPayoutApi(CDR: CordysAxios) {
  // 新建付款单
  function createAdPayout(data: AdPayoutSaveParams) {
    return CDR.post<AdPayoutInfo>({ url: AdPayoutCreateUrl, data });
  }

  // 编辑付款单
  function updateAdPayout(data: AdPayoutSaveParams) {
    return CDR.put<AdPayoutInfo>({ url: AdPayoutUpdateUrl, data });
  }

  // 提交付款单
  function submitAdPayout(id: string) {
    return CDR.post<AdPayoutInfo>({ url: `${AdPayoutUpdateUrl}/${id}/submit` });
  }

  // 审核付款单
  function approveAdPayout(id: string, data: AdPayoutApproveParams) {
    return CDR.put<AdPayoutInfo>({ url: `${AdPayoutUpdateUrl}/${id}/approve`, data });
  }

  // 付款单详情
  function getAdPayoutDetail(id: string) {
    return CDR.get<AdPayoutInfo>({ url: `${AdPayoutDetailUrl}/${id}` });
  }

  // 订单剩余应付金额
  function getAdPayoutRemaining(orderId: string) {
    return CDR.get<number>({ url: `${AdPayoutRemainingUrl}/${orderId}` });
  }

  // 订单的下游媒体列表（付款勾选用）
  function getAdPayoutMedia(orderId: string) {
    return CDR.get<Array<{ id: string; mediaName?: string; downstreamMediaId?: string }>>({
      url: `${AdPayoutMediaUrl}/${orderId}`,
    });
  }

  // 付款单分页
  function getAdPayoutPage(data: AdPayoutPageParams) {
    return CDR.post<AdPayoutPageResult>({ url: AdPayoutPageUrl, data }, { ignoreCancelToken: true });
  }

  // 删除付款单
  function deleteAdPayout(id: string) {
    return CDR.delete<void>({ url: `${AdPayoutUpdateUrl}/${id}` });
  }

  return {
    createAdPayout,
    updateAdPayout,
    submitAdPayout,
    approveAdPayout,
    getAdPayoutDetail,
    getAdPayoutRemaining,
    getAdPayoutMedia,
    getAdPayoutPage,
    deleteAdPayout,
  };
}
