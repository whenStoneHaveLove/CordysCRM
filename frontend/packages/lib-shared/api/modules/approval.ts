import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdApprovalPendingPageUrl,
  AdApprovalProcessedPageUrl,
  AdApprovalInitiatedPageUrl,
  AdApprovalCcPageUrl,
  AdApprovalPendingCountUrl,
} from '@lib/shared/api/requrls/approval';
import type {
  AdApprovalPageParams,
  AdApprovalPageResult,
  AdApprovalPendingCountResult,
} from '@lib/shared/models/advertising';

export default function useAdApprovalApi(CDR: CordysAxios) {
  // 待我审批分页（body 含 type: order/change/seal，对应三 Tab）
  function getAdApprovalPendingPage(data: AdApprovalPageParams) {
    return CDR.post<AdApprovalPageResult>({ url: AdApprovalPendingPageUrl, data }, { ignoreCancelToken: true });
  }

  // 已处理
  function getAdApprovalProcessedPage(data: AdApprovalPageParams) {
    return CDR.post<AdApprovalPageResult>({ url: AdApprovalProcessedPageUrl, data }, { ignoreCancelToken: true });
  }

  // 我发起
  function getAdApprovalInitiatedPage(data: AdApprovalPageParams) {
    return CDR.post<AdApprovalPageResult>({ url: AdApprovalInitiatedPageUrl, data }, { ignoreCancelToken: true });
  }

  // 抄送我
  function getAdApprovalCcPage(data: AdApprovalPageParams) {
    return CDR.post<AdApprovalPageResult>({ url: AdApprovalCcPageUrl, data }, { ignoreCancelToken: true });
  }

  // 待审计数（按 type 聚合）
  function getAdApprovalPendingCount(params?: { type?: string }) {
    return CDR.get<AdApprovalPendingCountResult>({ url: AdApprovalPendingCountUrl, params });
  }

  return {
    getAdApprovalPendingPage,
    getAdApprovalProcessedPage,
    getAdApprovalInitiatedPage,
    getAdApprovalCcPage,
    getAdApprovalPendingCount,
  };
}
