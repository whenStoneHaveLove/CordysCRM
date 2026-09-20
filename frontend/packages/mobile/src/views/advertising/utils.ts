/**
 * 广告模块（移动端）通用格式化与配色工具。
 * 仅移动端使用；不依赖 web 端源码。金额/颜色口径与 web 端广告模块保持一致。
 */
import {
  AD_ORDER_CHANGE_FIELD_META,
  AdOrderStatusEnum,
  AdSealRecordStatusEnum,
  AdSealStatusEnum,
} from '@lib/shared/enums/advertisingEnum';

/** 金额格式化：千分位 + 固定两位小数；空值返回 '-' */
export function fmtAmount(value?: number | string | null): string {
  if (value === null || value === undefined || value === '') return '-';
  const num = Number(value);
  if (Number.isNaN(num)) return '-';
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

/** 数量（整数）格式化：原样输出，空值返回 0 */
export function fmtCount(value?: number | null): string {
  if (value === null || value === undefined) return '0';
  return String(value);
}

/** 日期格式化：YYYY-MM-DD；空值返回 '-' */
export function fmtDate(value?: number | string | Date | null): string {
  if (value === null || value === undefined || value === '') return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  const pad = (n: number) => `${n}`.padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** 时间格式化：YYYY-MM-DD HH:mm；空值返回 '-' */
export function fmtDateTime(value?: number | string | Date | null): string {
  if (value === null || value === undefined || value === '') return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  const pad = (n: number) => `${n}`.padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}`;
}

/** 订单状态标签配色（返回值可直接 v-bind 到 CrmTag） */
export function getAdOrderStatusTagStyle(status?: number | null): { bgColor: string; textColor: string } {
  switch (status) {
    case AdOrderStatusEnum.DRAFT:
      return { bgColor: 'var(--text-n8)', textColor: 'var(--text-n2)' };
    case AdOrderStatusEnum.PENDING_BOSS_APPROVAL:
      return { bgColor: 'rgba(255, 162, 0, 0.12)', textColor: '#FFA200' };
    case AdOrderStatusEnum.PENDING_EXECUTE:
    case AdOrderStatusEnum.EXECUTING:
      return { bgColor: 'rgba(51, 112, 255, 0.12)', textColor: '#3370FF' };
    case AdOrderStatusEnum.CHANGE_APPROVING:
      return { bgColor: 'rgba(145, 117, 255, 0.12)', textColor: '#9175FF' };
    case AdOrderStatusEnum.SETTLEMENT:
    case AdOrderStatusEnum.ARCHIVED:
      return { bgColor: 'rgba(0, 194, 97, 0.12)', textColor: '#00C261' };
    case AdOrderStatusEnum.VOIDED:
      return { bgColor: 'rgba(226, 46, 35, 0.12)', textColor: '#E22E23' };
    default:
      return { bgColor: 'var(--text-n8)', textColor: 'var(--text-n2)' };
  }
}

/**
 * 合同用印状态标签配色（对齐 web contract/index.vue 的 sealStatusTagType）。
 * 已用印/已归档=绿、审批中/归档审批中=橙、驳回类=红、未申请=灰。
 */
export function getAdSealStatusTagStyle(status?: number | null): { bgColor: string; textColor: string } {
  switch (status) {
    case AdSealStatusEnum.SEALED:
    case AdSealStatusEnum.ARCHIVED:
      return { bgColor: 'rgba(0, 194, 97, 0.12)', textColor: '#00C261' };
    case AdSealStatusEnum.APPROVING:
    case AdSealStatusEnum.ARCHIVE_APPROVING:
      return { bgColor: 'rgba(255, 162, 0, 0.12)', textColor: '#FFA200' };
    case AdSealStatusEnum.REJECTED:
    case AdSealStatusEnum.ARCHIVE_REJECTED:
      return { bgColor: 'rgba(226, 46, 35, 0.12)', textColor: '#E22E23' };
    default:
      return { bgColor: 'var(--text-n8)', textColor: 'var(--text-n2)' };
  }
}

/**
 * 用印记录状态标签配色（对齐 web contract/detail.vue 的 sealRecordStatusTagType）。
 * 注意用的是「用印记录状态」枚举（0 审批中/10 通过/20 驳回），与合同用印状态枚举不同。
 */
export function getAdSealRecordStatusTagStyle(status?: number | null): { bgColor: string; textColor: string } {
  switch (status) {
    case AdSealRecordStatusEnum.APPROVED:
      return { bgColor: 'rgba(0, 194, 97, 0.12)', textColor: '#00C261' };
    case AdSealRecordStatusEnum.REJECTED:
      return { bgColor: 'rgba(226, 46, 35, 0.12)', textColor: '#E22E23' };
    default:
      return { bgColor: 'rgba(255, 162, 0, 0.12)', textColor: '#FFA200' };
  }
}

/**
 * 「完成 / 未完成」型标签配色（收款 / 付款 / 合同提交状态）。
 * done 为 true/1 时绿色，否则橙色。
 */
export function getDoneTagStyle(done?: number | boolean): { bgColor: string; textColor: string } {
  const finished = done === 1 || done === true;
  return finished
    ? { bgColor: 'rgba(0, 194, 97, 0.12)', textColor: '#00C261' }
    : { bgColor: 'rgba(255, 162, 0, 0.12)', textColor: '#FFA200' };
}

/**
 * 广告操作日志行为类型 → i18n key（对齐 web config/adLog.ts 的 adLogActionOption）。
 * 未命中时回退为原始动作串（避免空白）。
 */
const AD_OPERATION_LOG_ACTION_KEY: Record<string, string> = {
  CREATE: 'advertising.log.action.CREATE',
  COPY: 'advertising.log.action.COPY',
  UPDATE: 'advertising.log.action.UPDATE',
  DELETE: 'advertising.log.action.DELETE',
  SUBMIT: 'advertising.log.action.SUBMIT',
  APPROVE: 'advertising.log.action.APPROVE',
  REJECT: 'advertising.log.action.REJECT',
  EXECUTE: 'advertising.log.action.EXECUTE',
  VOID: 'advertising.log.action.VOID',
  FORCE_ARCHIVE: 'advertising.log.action.FORCE_ARCHIVE',
  FINANCIAL_PRE_ACTION: 'advertising.log.action.FINANCIAL_PRE_ACTION',
  CONFIRM_EXECUTE: 'advertising.log.action.CONFIRM_EXECUTE',
  COMPLETE_EXECUTE: 'advertising.log.action.COMPLETE_EXECUTE',
  DISABLE: 'advertising.log.action.DISABLE',
  APPLY: 'advertising.log.action.APPLY',
  CLOSE: 'advertising.log.action.CLOSE',
  RECEIVE: 'advertising.log.action.RECEIVE',
  PAY_MEDIA_PREPAY: 'advertising.log.action.PAY_MEDIA_PREPAY',
  PAY_MEDIA_POSTPAY: 'advertising.log.action.PAY_MEDIA_POSTPAY',
  RED_INVOICE_CLEAR: 'advertising.log.action.RED_INVOICE_CLEAR',
  CANCEL: 'advertising.log.action.CANCEL',
  CHANGE_REFUND: 'advertising.log.action.CHANGE_REFUND',
  CHANGE_BAD_DEBT: 'advertising.log.action.CHANGE_BAD_DEBT',
  SUBMIT_ARCHIVE: 'advertising.log.action.SUBMIT_ARCHIVE',
  APPROVE_ARCHIVE: 'advertising.log.action.APPROVE_ARCHIVE',
  REJECT_ARCHIVE: 'advertising.log.action.REJECT_ARCHIVE',
  VOID_SUBMIT: 'advertising.log.action.VOID_SUBMIT',
  VOID_APPROVE: 'advertising.log.action.VOID_APPROVE',
  VOID_REJECT: 'advertising.log.action.VOID_REJECT',
  OVERDUE_AUTO_50_TO_80: 'advertising.log.action.OVERDUE_AUTO_50_TO_80',
  PAY: 'advertising.log.action.PAY',
  // 订单状态机触发动作（ad_order_log.action，对齐 web 订单详情 getLogActionLabel）
  CHANGE: 'advertising.log.action.CHANGE',
  APPLY_CHANGE: 'advertising.log.action.APPLY_CHANGE',
  CHANGE_APPROVED: 'advertising.log.action.CHANGE_APPROVED',
  CHANGE_REJECTED: 'advertising.log.action.CHANGE_REJECTED',
  CHANGE_EXECUTED: 'advertising.log.action.CHANGE_EXECUTED',
  CHANGE_MONEY_SIDE: 'advertising.log.action.CHANGE_MONEY_SIDE',
  AUTO_OVERDUE: 'advertising.log.action.AUTO_OVERDUE',
  AUTO_ARCHIVE: 'advertising.log.action.AUTO_ARCHIVE',
  AUTO_PREPAY: 'advertising.log.action.AUTO_PREPAY',
  AUTO_EXECUTE: 'advertising.log.action.AUTO_EXECUTE',
  ARCHIVE: 'advertising.log.action.ARCHIVE',
  UPLOAD_DOUBLE_SEAL: 'advertising.log.action.UPLOAD_DOUBLE_SEAL',
};

export function getAdOperationLogActionKey(action?: string | null): string {
  if (!action) return '';
  return AD_OPERATION_LOG_ACTION_KEY[action] || action;
}

/**
 * 补充字段标签：后端 AdOrderChangeService 允许变更、但 AD_ORDER_CHANGE_FIELD_META 未收录的字段。
 * - 下游客户明细以整表覆盖方式随改单提交，不对应 ad_order 标量字段（镜像后端 SPECIAL_FIELDS），
 *   两个 key 恒成对出现，统一折叠为同一标签，展示时去重；
 * - paymentPrepay* / paymentPostpay* / signingEntity 属后端 MUTABLE_FIELDS 但不在移动端改单可选项内，
 *   出现时同样需要中文标签。
 * 文案对齐 order/locale/zh-CN.ts，避免列表/详情出现英文属性名。
 */
const AD_CHANGE_EXTRA_FIELD_LABEL: Record<string, string> = {
  downstreamMediaIds: '付款返点明细（下游客户）',
  downstreamMediaPayables: '付款返点明细（下游客户）',
  paymentPrepayMode: '预付方式',
  paymentPrepayRatio: '预付比例',
  paymentPrepayDeadline: '预付截止日',
  paymentPostpayTrigger: '后付触发',
  paymentPostpayDays: '后付天数',
  signingEntity: '签约主体',
};

/** 改单变更字段英文 key → 中文标签；未知字段回退原始 key，避免出现空白。 */
export function getAdChangeFieldLabel(field: string): string {
  const meta = AD_ORDER_CHANGE_FIELD_META.find((m) => m.field === field);
  return meta?.label || AD_CHANGE_EXTRA_FIELD_LABEL[field] || field;
}

/**
 * 改单 changeFields（JSON 数组或逗号分隔的英文 key 串）→ 中文标签串（已去重）。
 * 空值 / 解析失败 / 无有效字段时返回 '-'。
 */
export function formatAdChangeFields(value?: string | null): string {
  if (!value) return '-';
  let fields: string[] = [];
  const trimmed = value.trim();
  if (trimmed.startsWith('[')) {
    try {
      const arr = JSON.parse(trimmed);
      if (Array.isArray(arr)) fields = arr.map((f) => String(f));
    } catch {
      fields = [];
    }
  } else {
    fields = trimmed
      .split(',')
      .map((f) => f.trim())
      .filter(Boolean);
  }
  const labels = Array.from(new Set(fields.map((f) => getAdChangeFieldLabel(f))));
  return labels.length ? labels.join('、') : '-';
}
