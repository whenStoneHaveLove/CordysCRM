import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdOrderChangeCreateUrl,
  AdOrderChangeSubmitUrl,
  AdOrderChangeApproveUrl,
  AdOrderChangeRejectUrl,
  AdOrderChangeExecuteUrl,
  AdOrderChangePageUrl,
  AdOrderChangeDetailUrl,
} from '@lib/shared/api/requrls/adOrderChange';
import type {
  AdOrderChangeSaveParams,
  AdOrderChangePageParams,
  AdOrderChangePageResult,
  AdOrderChange,
  AdOrderChangeDetail,
  AdOrderChangeApproveParams,
} from '@lib/shared/models/advertising';

export default function useAdOrderChangeApi(CDR: CordysAxios) {
  // 新建改单申请（草稿）
  function createAdOrderChange(data: AdOrderChangeSaveParams) {
    return CDR.post<AdOrderChange>({ url: AdOrderChangeCreateUrl, data });
  }

  // 提交改单：锁定父单→变更审核中(60)
  function submitAdOrderChange(id: string) {
    return CDR.post<AdOrderChange>({ url: `${AdOrderChangeSubmitUrl}/${id}/submit` });
  }

  // 老板审批通过
  function approveAdOrderChange(id: string, data?: AdOrderChangeApproveParams) {
    return CDR.post<AdOrderChange>({ url: `${AdOrderChangeApproveUrl}/${id}/approve`, data });
  }

  // 老板驳回
  function rejectAdOrderChange(id: string, data?: AdOrderChangeApproveParams) {
    return CDR.post<AdOrderChange>({ url: `${AdOrderChangeRejectUrl}/${id}/reject`, data });
  }

  // 执行改单：应用快照+金额重算，父单恢复执行中(50)
  function executeAdOrderChange(id: string) {
    return CDR.post<AdOrderChange>({ url: `${AdOrderChangeExecuteUrl}/${id}/execute` });
  }

  // 改单分页
  function getAdOrderChangePage(data: AdOrderChangePageParams) {
    return CDR.post<AdOrderChangePageResult>({ url: AdOrderChangePageUrl, data }, { ignoreCancelToken: true });
  }

  // 改单详情
  function getAdOrderChangeDetail(id: string) {
    return CDR.get<AdOrderChangeDetail>({ url: `${AdOrderChangeDetailUrl}/${id}` });
  }

  return {
    createAdOrderChange,
    submitAdOrderChange,
    approveAdOrderChange,
    rejectAdOrderChange,
    executeAdOrderChange,
    getAdOrderChangePage,
    getAdOrderChangeDetail,
  };
}
