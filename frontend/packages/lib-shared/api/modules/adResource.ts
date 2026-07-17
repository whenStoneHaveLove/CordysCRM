import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdResourceCreateUrl,
  AdResourceUpdateUrl,
  AdResourceDetailUrl,
  AdResourcePageUrl,
} from '@lib/shared/api/requrls/adResource';
import type {
  AdResourceSaveParams,
  AdResourcePageParams,
  AdResourcePageResult,
  AdResourceInfo,
} from '@lib/shared/models/advertising';

export default function useAdResourceApi(CDR: CordysAxios) {
  // 新建资源
  function createAdResource(data: AdResourceSaveParams) {
    return CDR.post<AdResourceInfo>({ url: AdResourceCreateUrl, data });
  }

  // 编辑资源
  function updateAdResource(data: AdResourceSaveParams) {
    return CDR.put<AdResourceInfo>({ url: AdResourceUpdateUrl, data });
  }

  // 资源详情
  function getAdResourceDetail(id: string) {
    return CDR.get<AdResourceInfo>({ url: `${AdResourceDetailUrl}/${id}` });
  }

  // 资源分页
  function getAdResourcePage(data: AdResourcePageParams) {
    return CDR.post<AdResourcePageResult>({ url: AdResourcePageUrl, data }, { ignoreCancelToken: true });
  }

  // 删除资源（后端 DELETE 端点待补，B-7；前端先定义，UI 暂未挂载删除入口）
  function deleteAdResource(id: string) {
    return CDR.delete<void>({ url: `${AdResourceUpdateUrl}/${id}` });
  }

  return {
    createAdResource,
    updateAdResource,
    getAdResourceDetail,
    getAdResourcePage,
    deleteAdResource,
  };
}
