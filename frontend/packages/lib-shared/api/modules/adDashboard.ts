import type { CordysAxios } from '@lib/shared/api/http/Axios';
import { AdDashboardSummaryUrl, AdWorkbenchTodoUrl } from '@lib/shared/api/requrls/adDashboard';
import type { AdDashboardSummary, AdWorkbenchTodoResult } from '@lib/shared/models/advertising';

export default function useAdDashboardApi(CDR: CordysAxios) {
  // 分角色待办（GET /api/ad/dashboard/todo，后端待补 B-5）
  function getAdWorkbenchTodo(params?: Record<string, any>) {
    return CDR.get<AdWorkbenchTodoResult>({ url: AdWorkbenchTodoUrl, params });
  }

  // 工作台首页数据概览（GET /api/ad/dashboard/summary）
  function getAdDashboardSummary(params?: Record<string, any>) {
    return CDR.get<AdDashboardSummary>({ url: AdDashboardSummaryUrl, params });
  }

  return {
    getAdWorkbenchTodo,
    getAdDashboardSummary,
  };
}
