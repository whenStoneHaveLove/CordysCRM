import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdPayoutCreateUrl,
  AdPayoutDetailUrl,
  AdPayoutMediaUrl,
  AdPayoutPageUrl,
  AdPayoutRemainingUrl,
  AdPayoutUpdateUrl,
} from '@lib/shared/api/requrls/adPayout';
import type {
  AdPayoutApproveParams,
  AdPayoutDetail,
  AdPayoutInfo,
  AdPayoutMediaOption,
  AdPayoutPageParams,
  AdPayoutPageResult,
  AdPayoutPayParams,
  AdPayoutSaveParams,
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

  // 付款（待付款 → 已付款）
  function payAdPayout(id: string, data: AdPayoutPayParams) {
    return CDR.put<AdPayoutInfo>({ url: `${AdPayoutUpdateUrl}/${id}/pay`, data });
  }

  // 付款单详情
  function getAdPayoutDetail(id: string) {
    return CDR.get<AdPayoutDetail>({ url: `${AdPayoutDetailUrl}/${id}` });
  }

  // 订单剩余应付金额
  function getAdPayoutRemaining(orderId: string) {
    return CDR.get<number>({ url: `${AdPayoutRemainingUrl}/${orderId}` });
  }

  // 订单的下游客户列表（付款勾选用，含名称）
  function getAdPayoutMedia(orderId: string) {
    return CDR.get<AdPayoutMediaOption[]>({
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
    payAdPayout,
    getAdPayoutDetail,
    getAdPayoutRemaining,
    getAdPayoutMedia,
    getAdPayoutPage,
    deleteAdPayout,
  };
}
