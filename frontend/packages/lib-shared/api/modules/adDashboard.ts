import type { CordysAxios } from '@lib/shared/api/http/Axios';
import { AdWorkbenchTodoUrl } from '@lib/shared/api/requrls/adDashboard';
import type { AdWorkbenchTodoResult } from '@lib/shared/models/advertising';

export default function useAdDashboardApi(CDR: CordysAxios) {
  // 分角色待办（GET /api/ad/dashboard/todo，后端待补 B-5）
  function getAdWorkbenchTodo(params?: Record<string, any>) {
    return CDR.get<AdWorkbenchTodoResult>({ url: AdWorkbenchTodoUrl, params });
  }

  return {
    getAdWorkbenchTodo,
  };
}
