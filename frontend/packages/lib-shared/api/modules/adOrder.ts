import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AdOrderCreateUrl,
  AdOrderUpdateUrl,
  AdOrderDetailUrl,
  AdOrderPageUrl,
  AdOrderSubmitUrl,
  AdOrderApproveUrl,
  AdOrderRejectUrl,
  AdOrderConfirmExecuteUrl,
  AdOrderVoidUrl,
  AdOrderForceArchiveUrl,
  AdOrderExportUrl,
} from '@lib/shared/api/requrls/adOrder';
import type {
  AdOrderSaveParams,
  AdOrderPageParams,
  AdOrderPageResult,
  AdOrderDetail,
  AdOrderInfo,
  AdOrderApproveParams,
  AdOrderVoidParams,
  AdOrderForceArchiveParams,
} from '@lib/shared/models/advertising';

export default function useAdOrderApi(CDR: CordysAxios) {
  // 新建订单（草稿）
  function createAdOrder(data: AdOrderSaveParams) {
    return CDR.post<AdOrderInfo>({ url: AdOrderCreateUrl, data });
  }

  // 编辑草稿订单
  function updateAdOrder(data: AdOrderSaveParams) {
    return CDR.put<AdOrderInfo>({ url: AdOrderUpdateUrl, data });
  }

  // 订单详情
  function getAdOrderDetail(id: string) {
    return CDR.get<AdOrderDetail>({ url: `${AdOrderDetailUrl}/${id}` });
  }

  // 订单分页
  function getAdOrderPage(data: AdOrderPageParams) {
    return CDR.post<AdOrderPageResult>({ url: AdOrderPageUrl, data }, { ignoreCancelToken: true });
  }

  // 提交（0→10；审批关时 0→20）
  function submitAdOrder(id: string) {
    return CDR.post<AdOrderInfo>({ url: `${AdOrderSubmitUrl}/${id}/submit` });
  }

  // 管理组审核（通过 10→20 / 驳回 10→0）
  function approveAdOrder(id: string, data: AdOrderApproveParams) {
    return CDR.post<AdOrderInfo>({ url: `${AdOrderApproveUrl}/${id}/approve`, data });
  }

  // 管理组驳回（10→0）
  function rejectAdOrder(id: string, data: AdOrderApproveParams) {
    return CDR.post<AdOrderInfo>({ url: `${AdOrderRejectUrl}/${id}/reject`, data });
  }

  // 确认执行（45→50）
  function confirmExecuteAdOrder(id: string) {
    return CDR.post<AdOrderInfo>({ url: `${AdOrderConfirmExecuteUrl}/${id}/confirm-execute` });
  }

  // 作废（→100）
  function voidAdOrder(id: string, data: AdOrderVoidParams) {
    return CDR.post<AdOrderInfo>({ url: `${AdOrderVoidUrl}/${id}/void`, data });
  }

  // 强制归档（80→90，管理组，带坏账金额）
  function forceArchiveAdOrder(id: string, data: AdOrderForceArchiveParams) {
    return CDR.post<AdOrderInfo>({ url: `${AdOrderForceArchiveUrl}/${id}/force-archive`, data });
  }

  // 导出订单（按筛选条件，同步流式返回 Excel 文件；返回原生响应以便读取 blob，自动携带 X-AUTH-TOKEN/CSRF-TOKEN）
  function exportAdOrder(data: AdOrderPageParams) {
    return CDR.post<any>({
      url: AdOrderExportUrl,
      data,
      responseType: 'blob',
    }, { isReturnNativeResponse: true, isTransformResponse: false });
  }

  return {
    createAdOrder,
    updateAdOrder,
    getAdOrderDetail,
    getAdOrderPage,
    submitAdOrder,
    approveAdOrder,
    rejectAdOrder,
    confirmExecuteAdOrder,
    voidAdOrder,
    forceArchiveAdOrder,
    exportAdOrder,
  };
}
