import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdCustomerCreateUrl,
  AdCustomerUpdateUrl,
  AdCustomerDetailUrl,
  AdCustomerPageUrl,
} from '@lib/shared/api/requrls/adCustomer';
import type {
  AdCustomerSaveParams,
  AdCustomerPageParams,
  AdCustomerPageResult,
  AdCustomerInfo,
} from '@lib/shared/models/advertising';

export default function useAdCustomerApi(CDR: CordysAxios) {
  // 新建客户（名称全局唯一，后端校验；下单自动同步）
  function createAdCustomer(data: AdCustomerSaveParams) {
    return CDR.post<AdCustomerInfo>({ url: AdCustomerCreateUrl, data });
  }

  // 编辑客户
  function updateAdCustomer(data: AdCustomerSaveParams) {
    return CDR.put<AdCustomerInfo>({ url: AdCustomerUpdateUrl, data });
  }

  // 客户详情
  function getAdCustomerDetail(id: string) {
    return CDR.get<AdCustomerInfo>({ url: `${AdCustomerDetailUrl}/${id}` });
  }

  // 客户分页
  function getAdCustomerPage(data: AdCustomerPageParams) {
    return CDR.post<AdCustomerPageResult>({ url: AdCustomerPageUrl, data }, { ignoreCancelToken: true });
  }

  // 删除客户（后端 DELETE 端点待补，B-7；前端先定义，UI 暂未挂载删除入口）
  function deleteAdCustomer(id: string) {
    return CDR.delete<void>({ url: `${AdCustomerUpdateUrl}/${id}` });
  }

  return {
    createAdCustomer,
    updateAdCustomer,
    getAdCustomerDetail,
    getAdCustomerPage,
    deleteAdCustomer,
  };
}
