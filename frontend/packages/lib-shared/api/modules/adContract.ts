import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdContractCreateUrl,
  AdContractUpdateUrl,
  AdContractDeleteUrl,
  AdContractDetailUrl,
  AdContractPageUrl,
} from '@lib/shared/api/requrls/adContract';
import type {
  AdContractSaveParams,
  AdContractPageParams,
  AdContractPageResult,
  AdContractDetailResponse,
  AdContractInfo,
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

  // 上传双盖附件（仅保存）
  function uploadDoubleSeal(id: string, fileUrl: string) {
    return CDR.put<AdContractInfo>({ url: `${AdContractUpdateUrl}/${id}/double-seal`, data: { fileUrl } });
  }

  // 提交归档审批
  function submitArchive(id: string, fileUrl?: string) {
    return CDR.put<AdContractInfo>({ url: `${AdContractUpdateUrl}/${id}/submit-archive`, data: { fileUrl } });
  }

  // 归档审批通过（老板）
  function approveArchive(id: string, remark?: string) {
    return CDR.put<AdContractInfo>({
      url: `${AdContractUpdateUrl}/${id}/approve-archive`,
      data: { remark },
    });
  }

  // 归档审批驳回（老板）
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
    uploadDoubleSeal,
    submitArchive,
    approveArchive,
    rejectArchive,
  };
}
