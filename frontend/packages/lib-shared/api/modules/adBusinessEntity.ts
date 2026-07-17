import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdBusinessEntityCreateUrl,
  AdBusinessEntityUpdateUrl,
  AdBusinessEntityDetailUrl,
  AdBusinessEntityPageUrl,
  AdBusinessEntityDeleteUrl,
} from '@lib/shared/api/requrls/adBusinessEntity';
import type {
  AdBusinessEntitySaveParams,
  AdBusinessEntityPageParams,
  AdBusinessEntityPageResult,
  AdBusinessEntityDetail,
  AdBusinessEntityInfo,
} from '@lib/shared/models/advertising';

export default function useAdBusinessEntityApi(CDR: CordysAxios) {
  // 新建业务主体
  function createAdBusinessEntity(data: AdBusinessEntitySaveParams) {
    return CDR.post<AdBusinessEntityInfo>({ url: AdBusinessEntityCreateUrl, data });
  }

  // 编辑业务主体
  function updateAdBusinessEntity(data: AdBusinessEntitySaveParams) {
    return CDR.put<AdBusinessEntityInfo>({ url: AdBusinessEntityUpdateUrl, data });
  }

  // 业务主体详情
  function getAdBusinessEntityDetail(id: string) {
    return CDR.get<AdBusinessEntityDetail>({ url: `${AdBusinessEntityDetailUrl}/${id}` });
  }

  // 业务主体分页
  function getAdBusinessEntityPage(data: AdBusinessEntityPageParams) {
    return CDR.post<AdBusinessEntityPageResult>({ url: AdBusinessEntityPageUrl, data }, { ignoreCancelToken: true });
  }

  // 停用/删除业务主体
  function deleteAdBusinessEntity(id: string) {
    return CDR.delete<AdBusinessEntityInfo>({ url: `${AdBusinessEntityDeleteUrl}/${id}` });
  }

  return {
    createAdBusinessEntity,
    updateAdBusinessEntity,
    getAdBusinessEntityDetail,
    getAdBusinessEntityPage,
    deleteAdBusinessEntity,
  };
}
