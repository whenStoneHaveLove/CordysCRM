import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdContractCreateUrl,
  AdContractDeleteUrl,
  AdContractDetailUrl,
  AdContractPageUrl,
  AdContractUpdateUrl,
} from '@lib/shared/api/requrls/adContract';
import type {
  AdContractDetailResponse,
  AdContractInfo,
  AdContractPageParams,
  AdContractPageResult,
  AdContractSaveParams,
} from '@lib/shared/models/advertising';

export default function useAdContractApi(CDR: CordysAxios) {
  // 新建合同
  function createAdContract(data: AdContractSaveParams) {
    return CDR.post<AdContractInfo>({ url: AdContractCreateUrl, data });
  }

  // 编辑合同
  function updateAdContract(data: AdContractSaveParams) {
    return CDR.put<AdContractInfo>({ url: AdContractUpdateUrl, data });
  }

  // 逻辑删除合同
  function deleteAdContract(id: string) {
    return CDR.delete<void>({ url: `${AdContractDeleteUrl}/${id}` });
  }

  // 合同详情
  function getAdContractDetail(id: string) {
    return CDR.get<AdContractDetailResponse>({ url: `${AdContractDetailUrl}/${id}` });
  }

  // 合同分页
  function getAdContractPage(data: AdContractPageParams) {
    return CDR.post<AdContractPageResult>({ url: AdContractPageUrl, data }, { ignoreCancelToken: true });
  }

  // 提交归档审批（双盖附件已由前端经附件接口写入）
  function submitArchive(id: string) {
    return CDR.put<AdContractInfo>({ url: `${AdContractUpdateUrl}/${id}/submit-archive`, data: {} });
  }

  // 归档审批通过（管理组）
  function approveArchive(id: string, remark?: string) {
    return CDR.put<AdContractInfo>({
      url: `${AdContractUpdateUrl}/${id}/approve-archive`,
      data: { remark },
    });
  }

  // 归档审批驳回（管理组）
  function rejectArchive(id: string, remark?: string) {
    return CDR.put<AdContractInfo>({
      url: `${AdContractUpdateUrl}/${id}/reject-archive`,
      data: { remark },
    });
  }

  return {
    createAdContract,
    updateAdContract,
    deleteAdContract,
    getAdContractDetail,
    getAdContractPage,
    submitArchive,
    approveArchive,
    rejectArchive,
  };
}
