import type { CommonList } from '@lib/shared/models/common';

import CDR from '@/api/http/index';

const AdOperationLogListUrl = '/ad/operation/log/list';

export interface AdOperationLogItem {
  id: string;
  module: string;
  action: string;
  targetId: string;
  operatorId: string;
  operatorName: string;
  bizType: string;
  ip: string;
  createTime: number;
  beforeValue?: string;
  afterValue?: string;
}

export interface AdOperationLogParams {
  current: number;
  pageSize: number;
  operator?: string;
  startTime?: number;
  endTime?: number;
  action?: string;
  module?: string;
  keyword?: string;
}

// 广告操作日志列表
export function adOperationLogList(data: AdOperationLogParams) {
  return CDR.post<CommonList<AdOperationLogItem>>({ url: AdOperationLogListUrl, data });
}
