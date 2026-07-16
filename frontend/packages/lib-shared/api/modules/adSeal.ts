import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdSealApplyUrl,
  AdSealApproveUrl,
  AdSealRejectUrl,
  AdSealUploadUrl,
  AdSealDetailUrl,
  AdSealPageUrl,
} from '@lib/shared/api/requrls/adSeal';
import type {
  AdSealApplyParams,
  AdSealApproveParams,
  AdSealUploadParams,
  AdSealRecordPageParams,
  AdSealRecordPageResult,
  AdSealRecordDetailResponse,
  AdSealRecordInfo,
} from '@lib/shared/models/advertising';

export default function useAdSealApi(CDR: CordysAxios) {
  // 申请用印
  function applyAdSeal(data: AdSealApplyParams) {
    return CDR.post<AdSealRecordInfo>({ url: AdSealApplyUrl, data });
  }

  // 用印审批通过
  function approveAdSeal(id: string, data?: AdSealApproveParams) {
    return CDR.post<AdSealRecordInfo>({ url: `${AdSealApproveUrl}/${id}/approve`, data });
  }

  // 用印驳回
  function rejectAdSeal(id: string, data?: AdSealApproveParams) {
    return CDR.post<AdSealRecordInfo>({ url: `${AdSealRejectUrl}/${id}/reject`, data });
  }

  // 上传盖章版
  function uploadAdSeal(id: string, data: AdSealUploadParams) {
    return CDR.post<AdSealRecordInfo>({ url: `${AdSealUploadUrl}/${id}/upload`, data });
  }

  // 用印记录详情
  function getAdSealDetail(id: string) {
    return CDR.get<AdSealRecordDetailResponse>({ url: `${AdSealDetailUrl}/${id}` });
  }

  // 用印记录分页
  function getAdSealPage(data: AdSealRecordPageParams) {
    return CDR.post<AdSealRecordPageResult>({ url: AdSealPageUrl, data }, { ignoreCancelToken: true });
  }

  return {
    applyAdSeal,
    approveAdSeal,
    rejectAdSeal,
    uploadAdSeal,
    getAdSealDetail,
    getAdSealPage,
  };
}
