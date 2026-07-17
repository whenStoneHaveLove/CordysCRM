import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdDictCreateUrl,
  AdDictUpdateUrl,
  AdDictDetailUrl,
  AdDictPageUrl,
  AdDictDeleteUrl,
} from '@lib/shared/api/requrls/adDict';
import type {
  AdDictSaveParams,
  AdDictPageParams,
  AdDictPageResult,
  AdDictInfo,
} from '@lib/shared/models/advertising';

export default function useAdDictApi(CDR: CordysAxios) {
  // 新增字典项
  function createAdDict(data: AdDictSaveParams) {
    return CDR.post<AdDictInfo>({ url: AdDictCreateUrl, data });
  }

  // 编辑字典项
  function updateAdDict(data: AdDictSaveParams) {
    return CDR.put<AdDictInfo>({ url: AdDictUpdateUrl, data });
  }

  // 字典项详情
  function getAdDictDetail(id: string) {
    return CDR.get<AdDictInfo>({ url: `${AdDictDetailUrl}/${id}` });
  }

  // 字典项分页
  function getAdDictPage(data: AdDictPageParams) {
    return CDR.post<AdDictPageResult>({ url: AdDictPageUrl, data }, { ignoreCancelToken: true });
  }

  // 删除字典项
  function deleteAdDict(id: string) {
    return CDR.delete<AdDictInfo>({ url: `${AdDictDeleteUrl}/${id}` });
  }

  return { createAdDict, updateAdDict, getAdDictDetail, getAdDictPage, deleteAdDict };
}
