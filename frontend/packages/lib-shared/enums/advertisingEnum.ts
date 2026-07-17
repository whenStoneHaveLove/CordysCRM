/**
 * 广告模块前端枚举（镜像后端 cn.cordys.crm.ad.common.constants / payment.constants）。
 * 状态码与后端严格一致，前端用于下拉选项与标签渲染。
 */

/** 订单主状态（10 倍数编码，V3.1 §6.1）。 */
export enum AdOrderStatusEnum {
  DRAFT = 0,
  PENDING_BOSS_APPROVAL = 10,
  APPROVED = 20,
  PENDING_PREPAY_CONFIRM = 30,
  PENDING_MEDIA_PREPAY = 40,
  EXECUTING = 50,
  CHANGE_APPROVING = 60,
  EXECUTION_COMPLETED = 70,
  SETTLEMENT = 80,
  ARCHIVED = 90,
  VOIDED = 100,
}

export const AdOrderStatusLabel: Record<number, string> = {
  [AdOrderStatusEnum.DRAFT]: '草稿',
  [AdOrderStatusEnum.PENDING_BOSS_APPROVAL]: '待老板审核',
  [AdOrderStatusEnum.APPROVED]: '审核通过',
  [AdOrderStatusEnum.PENDING_PREPAY_CONFIRM]: '待确认预收款',
  [AdOrderStatusEnum.PENDING_MEDIA_PREPAY]: '待付媒体预付款',
  [AdOrderStatusEnum.EXECUTING]: '执行中',
  [AdOrderStatusEnum.CHANGE_APPROVING]: '变更审核中',
  [AdOrderStatusEnum.EXECUTION_COMPLETED]: '执行完成',
  [AdOrderStatusEnum.SETTLEMENT]: '结算中',
  [AdOrderStatusEnum.ARCHIVED]: '已归档',
  [AdOrderStatusEnum.VOIDED]: '已作废',
};

export const AdOrderStatusOptions = Object.keys(AdOrderStatusLabel).map((key) => ({
  label: AdOrderStatusLabel[Number(key)],
  value: Number(key),
}));

/** 改单状态。 */
export enum AdOrderChangeStatusEnum {
  DRAFT = 0,
  SUBMITTED = 10,
  APPROVED = 20,
  REJECTED = 30,
  EXECUTED = 40,
}

export const AdOrderChangeStatusLabel: Record<number, string> = {
  [AdOrderChangeStatusEnum.DRAFT]: '草稿',
  [AdOrderChangeStatusEnum.SUBMITTED]: '已提交',
  [AdOrderChangeStatusEnum.APPROVED]: '审批通过',
  [AdOrderChangeStatusEnum.REJECTED]: '已驳回',
  [AdOrderChangeStatusEnum.EXECUTED]: '已执行',
};

export const AdOrderChangeStatusOptions = Object.keys(AdOrderChangeStatusLabel).map((key) => ({
  label: AdOrderChangeStatusLabel[Number(key)],
  value: Number(key),
}));

/** 订单类型。 */
export enum AdOrderTypeEnum {
  FRAMEWORK = 10,
  SINGLE = 20,
}
export const AdOrderTypeLabel: Record<number, string> = {
  [AdOrderTypeEnum.FRAMEWORK]: '框架合同',
  [AdOrderTypeEnum.SINGLE]: '单笔合同',
};
export const AdOrderTypeOptions = Object.keys(AdOrderTypeLabel).map((k) => ({
  label: AdOrderTypeLabel[Number(k)],
  value: Number(k),
}));

/** 收款方式。 */
export enum AdReceiptMethodEnum {
  PREPAY = 10,
  ACCOUNT_PERIOD = 20,
}
export const AdReceiptMethodLabel: Record<number, string> = {
  [AdReceiptMethodEnum.PREPAY]: '预付款',
  [AdReceiptMethodEnum.ACCOUNT_PERIOD]: '账期',
};
export const AdReceiptMethodOptions = Object.keys(AdReceiptMethodLabel).map((k) => ({
  label: AdReceiptMethodLabel[Number(k)],
  value: Number(k),
}));

/** 付款方式。 */
export enum AdPaymentMethodEnum {
  PREPAY_MEDIA = 10,
  POSTPAY_MEDIA = 20,
}
export const AdPaymentMethodLabel: Record<number, string> = {
  [AdPaymentMethodEnum.PREPAY_MEDIA]: '预付媒体',
  [AdPaymentMethodEnum.POSTPAY_MEDIA]: '后付媒体',
};
export const AdPaymentMethodOptions = Object.keys(AdPaymentMethodLabel).map((k) => ({
  label: AdPaymentMethodLabel[Number(k)],
  value: Number(k),
}));

/** 媒体后付触发。 */
export enum AdPostpayTriggerEnum {
  ON_UPSTREAM_FULL_PAID = 10,
  ON_EXECUTION_COMPLETED_DAYS = 20,
}
export const AdPostpayTriggerLabel: Record<number, string> = {
  [AdPostpayTriggerEnum.ON_UPSTREAM_FULL_PAID]: '收到上游全款后',
  [AdPostpayTriggerEnum.ON_EXECUTION_COMPLETED_DAYS]: '执行完成X天后',
};
export const AdPostpayTriggerOptions = Object.keys(AdPostpayTriggerLabel).map((k) => ({
  label: AdPostpayTriggerLabel[Number(k)],
  value: Number(k),
}));

/** 比例 / 固定金额模式（返点、预收、媒体预付共用）。 */
export enum AdModeEnum {
  RATIO = 10,
  FIXED = 20,
}
export const AdModeLabel: Record<number, string> = {
  [AdModeEnum.RATIO]: '比例',
  [AdModeEnum.FIXED]: '固定金额',
};
export const AdModeOptions = Object.keys(AdModeLabel).map((k) => ({
  label: AdModeLabel[Number(k)],
  value: Number(k),
}));

/** 收付款方向。 */
export enum AdPaymentDirectionEnum {
  UPSTREAM = 10,
  DOWNSTREAM = 20,
}
export const AdPaymentDirectionLabel: Record<number, string> = {
  [AdPaymentDirectionEnum.UPSTREAM]: '上游收款',
  [AdPaymentDirectionEnum.DOWNSTREAM]: '下游付款',
};
export const AdPaymentDirectionOptions = Object.keys(AdPaymentDirectionLabel).map((k) => ({
  label: AdPaymentDirectionLabel[Number(k)],
  value: Number(k),
}));

/** 收付款类型。 */
export enum AdPaymentTypeEnum {
  PRE_RECEIPT = 10,
  PRE_PAY = 20,
  INVOICE_RECEIPT = 30,
  MEDIA_POSTPAY = 40,
  REFUND = 50,
  BAD_DEBT = 60,
}
export const AdPaymentTypeLabel: Record<number, string> = {
  [AdPaymentTypeEnum.PRE_RECEIPT]: '预收',
  [AdPaymentTypeEnum.PRE_PAY]: '预付',
  [AdPaymentTypeEnum.INVOICE_RECEIPT]: '开票收款',
  [AdPaymentTypeEnum.MEDIA_POSTPAY]: '媒体尾款',
  [AdPaymentTypeEnum.REFUND]: '退款',
  [AdPaymentTypeEnum.BAD_DEBT]: '坏账',
};
export const AdPaymentTypeOptions = Object.keys(AdPaymentTypeLabel).map((k) => ({
  label: AdPaymentTypeLabel[Number(k)],
  value: Number(k),
}));

/** 状态中文标签（未知码回退为字符串）。 */
export function getAdOrderStatusLabel(status?: number | null): string {
  if (status == null) return '-';
  return AdOrderStatusLabel[status] ?? String(status);
}
export function getAdOrderChangeStatusLabel(status?: number | null): string {
  if (status == null) return '-';
  return AdOrderChangeStatusLabel[status] ?? String(status);
}
export function getAdOrderTypeLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdOrderTypeLabel[value] ?? String(value);
}
export function getAdReceiptMethodLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdReceiptMethodLabel[value] ?? String(value);
}
export function getAdPaymentMethodLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdPaymentMethodLabel[value] ?? String(value);
}
export function getAdPaymentDirectionLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdPaymentDirectionLabel[value] ?? String(value);
}
export function getAdPaymentTypeLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdPaymentTypeLabel[value] ?? String(value);
}

/* ----------------------------- 合同 ----------------------------- */

/** 合同方向（镜像 ContractDirection）。 */
export enum AdContractDirectionEnum {
  UPSTREAM = 10,
  DOWNSTREAM = 20,
}
export const AdContractDirectionLabel: Record<number, string> = {
  [AdContractDirectionEnum.UPSTREAM]: '上游',
  [AdContractDirectionEnum.DOWNSTREAM]: '下游',
};
export const AdContractDirectionOptions = Object.keys(AdContractDirectionLabel).map((k) => ({
  label: AdContractDirectionLabel[Number(k)],
  value: Number(k),
}));

/** 合同类型（镜像 ContractType）。 */
export enum AdContractTypeEnum {
  FRAMEWORK = 10,
  SINGLE = 20,
}
export const AdContractTypeLabel: Record<number, string> = {
  [AdContractTypeEnum.FRAMEWORK]: '框架',
  [AdContractTypeEnum.SINGLE]: '单笔',
};
export const AdContractTypeOptions = Object.keys(AdContractTypeLabel).map((k) => ({
  label: AdContractTypeLabel[Number(k)],
  value: Number(k),
}));

/** 合同用印状态（镜像 SealStatus）。 */
export enum AdSealStatusEnum {
  NOT_APPLIED = 0,
  APPROVING = 10,
  SEALED = 20,
  REJECTED = 30,
}
export const AdSealStatusLabel: Record<number, string> = {
  [AdSealStatusEnum.NOT_APPLIED]: '未申请',
  [AdSealStatusEnum.APPROVING]: '审批中',
  [AdSealStatusEnum.SEALED]: '已用印',
  [AdSealStatusEnum.REJECTED]: '已驳回',
};
export const AdSealStatusOptions = Object.keys(AdSealStatusLabel).map((k) => ({
  label: AdSealStatusLabel[Number(k)],
  value: Number(k),
}));

/* ----------------------------- 用印记录 ----------------------------- */

/** 用印记录状态（镜像 AdSealRecordStatus）。 */
export enum AdSealRecordStatusEnum {
  APPROVING = 0,
  APPROVED = 10,
  REJECTED = 20,
}
export const AdSealRecordStatusLabel: Record<number, string> = {
  [AdSealRecordStatusEnum.APPROVING]: '审批中',
  [AdSealRecordStatusEnum.APPROVED]: '通过',
  [AdSealRecordStatusEnum.REJECTED]: '驳回',
};
export const AdSealRecordStatusOptions = Object.keys(AdSealRecordStatusLabel).map((k) => ({
  label: AdSealRecordStatusLabel[Number(k)],
  value: Number(k),
}));

/** 用印类型。 */
export enum AdSealTypeEnum {
  COMPANY = 10,
  CONTRACT = 20,
}
export const AdSealTypeLabel: Record<number, string> = {
  [AdSealTypeEnum.COMPANY]: '公章',
  [AdSealTypeEnum.CONTRACT]: '合同章',
};
export const AdSealTypeOptions = Object.keys(AdSealTypeLabel).map((k) => ({
  label: AdSealTypeLabel[Number(k)],
  value: Number(k),
}));

/** 状态中文标签获取函数。 */
export function getAdContractDirectionLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdContractDirectionLabel[value] ?? String(value);
}
export function getAdContractTypeLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdContractTypeLabel[value] ?? String(value);
}
export function getAdSealStatusLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdSealStatusLabel[value] ?? String(value);
}
export function getAdSealRecordStatusLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdSealRecordStatusLabel[value] ?? String(value);
}
export function getAdSealTypeLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdSealTypeLabel[value] ?? String(value);
}

/* ----------------------------- 业务主体 ----------------------------- */

/** 业务主体状态（10 启用 / 20 停用，PRD §5.2.7）。 */
export enum AdBusinessEntityStatusEnum {
  ENABLED = 10,
  DISABLED = 20,
}
export const AdBusinessEntityStatusLabel: Record<number, string> = {
  [AdBusinessEntityStatusEnum.ENABLED]: '启用',
  [AdBusinessEntityStatusEnum.DISABLED]: '停用',
};
export const AdBusinessEntityStatusOptions = Object.keys(AdBusinessEntityStatusLabel).map((k) => ({
  label: AdBusinessEntityStatusLabel[Number(k)],
  value: Number(k),
}));

/* ----------------------------- 字典 ----------------------------- */

/** 字典状态（10 启用 / 20 停用）。 */
export enum AdDictStatusEnum {
  ENABLED = 10,
  DISABLED = 20,
}
export const AdDictStatusLabel: Record<number, string> = {
  [AdDictStatusEnum.ENABLED]: '启用',
  [AdDictStatusEnum.DISABLED]: '停用',
};
export const AdDictStatusOptions = Object.keys(AdDictStatusLabel).map((k) => ({
  label: AdDictStatusLabel[Number(k)],
  value: Number(k),
}));

/** 状态中文标签获取函数。 */
export function getAdBusinessEntityStatusLabel(status?: number | null): string {
  if (status == null) return '-';
  return AdBusinessEntityStatusLabel[status] ?? String(status);
}
export function getAdDictStatusLabel(status?: number | null): string {
  if (status == null) return '-';
  return AdDictStatusLabel[status] ?? String(status);
}

/** 改单可变更字段白名单（镜像 AdOrderChangeService.MUTABLE_FIELDS，camelCase）。 */
export interface AdChangeFieldMeta {
  field: string;
  label: string;
  type: 'number' | 'date' | 'text';
}

export const AD_ORDER_CHANGE_FIELD_META: AdChangeFieldMeta[] = [
  { field: 'orderName', label: '订单名称', type: 'text' },
  { field: 'customerId', label: '客户ID', type: 'text' },
  { field: 'industryCode', label: '行业类别', type: 'text' },
  { field: 'signingEntity', label: '签约主体', type: 'text' },
  { field: 'totalAmount', label: '订单总金额', type: 'number' },
  { field: 'noRebateAmount', label: '不记返金额', type: 'number' },
  { field: 'rebateValue', label: '返点值', type: 'number' },
  { field: 'rebateMode', label: '返点方式', type: 'number' },
  { field: 'receiptMethod', label: '收款方式', type: 'number' },
  { field: 'receiptPrepayMode', label: '预收模式', type: 'number' },
  { field: 'receiptPrepayRatio', label: '预收比例%', type: 'number' },
  { field: 'receiptPrepayDeadline', label: '预收截止日', type: 'date' },
  { field: 'receiptAccountPeriodDays', label: '账期天数', type: 'number' },
  { field: 'paymentMethod', label: '付款方式', type: 'number' },
  { field: 'paymentPrepayMode', label: '媒体预付模式', type: 'number' },
  { field: 'paymentPrepayRatio', label: '媒体预付比例%', type: 'number' },
  { field: 'paymentPrepayDeadline', label: '媒体预付截止日', type: 'date' },
  { field: 'paymentPostpayTrigger', label: '后付触发', type: 'number' },
  { field: 'paymentPostpayDays', label: '后付天数', type: 'number' },
  { field: 'deliveryStartDate', label: '投放起始日', type: 'date' },
  { field: 'deliveryEndDate', label: '投放结束日', type: 'date' },
  { field: 'deliveryVolume', label: '投放量+单位', type: 'text' },
  { field: 'currency', label: '币种', type: 'text' },
  { field: 'upstreamAgentId', label: '上游代理', type: 'text' },
  { field: 'agentOrderNo', label: '代理订单号', type: 'text' },
  { field: 'remark', label: '备注', type: 'text' },
  { field: 'extJson', label: '扩展字段', type: 'text' },
];

/* ----------------------------- 资源（ResourceType / ResourceStatus） ----------------------------- */

/** 资源类型（镜像 ResourceType，PRD §5.2.7 / §13.4）。 */
export enum AdResourceTypeEnum {
  UPSTREAM_AGENT = 10,
  DOWNSTREAM_MEDIA = 20,
}
export const AdResourceTypeLabel: Record<number, string> = {
  [AdResourceTypeEnum.UPSTREAM_AGENT]: '上游代理',
  [AdResourceTypeEnum.DOWNSTREAM_MEDIA]: '下游媒体',
};
export const AdResourceTypeOptions = Object.keys(AdResourceTypeLabel).map((k) => ({
  label: AdResourceTypeLabel[Number(k)],
  value: Number(k),
}));

/** 资源状态（镜像 ResourceStatus）。 */
export enum AdResourceStatusEnum {
  NORMAL = 10,
  DISABLED = 20,
}
export const AdResourceStatusLabel: Record<number, string> = {
  [AdResourceStatusEnum.NORMAL]: '正常',
  [AdResourceStatusEnum.DISABLED]: '停用',
};
export const AdResourceStatusOptions = Object.keys(AdResourceStatusLabel).map((k) => ({
  label: AdResourceStatusLabel[Number(k)],
  value: Number(k),
}));

/* ----------------------------- 客户（CustomerStatus / CustomerLevel） ----------------------------- */

/** 客户状态（镜像 CustomerStatus，PRD §5.2.7 / §8.4）。 */
export enum AdCustomerStatusEnum {
  ACTIVE = 0,
  INACTIVE = 10,
  BLACKLIST = 20,
}
export const AdCustomerStatusLabel: Record<number, string> = {
  [AdCustomerStatusEnum.ACTIVE]: '活跃',
  [AdCustomerStatusEnum.INACTIVE]: '非活跃',
  [AdCustomerStatusEnum.BLACKLIST]: '黑名单',
};
export const AdCustomerStatusOptions = Object.keys(AdCustomerStatusLabel).map((k) => ({
  label: AdCustomerStatusLabel[Number(k)],
  value: Number(k),
}));

/** 客户等级（镜像 CustomerLevel）。 */
export enum AdCustomerLevelEnum {
  VIP = 10,
  NORMAL = 20,
  POTENTIAL = 30,
}
export const AdCustomerLevelLabel: Record<number, string> = {
  [AdCustomerLevelEnum.VIP]: 'VIP',
  [AdCustomerLevelEnum.NORMAL]: '普通',
  [AdCustomerLevelEnum.POTENTIAL]: '潜力',
};
export const AdCustomerLevelOptions = Object.keys(AdCustomerLevelLabel).map((k) => ({
  label: AdCustomerLevelLabel[Number(k)],
  value: Number(k),
}));

/** 状态中文标签获取函数（未知码回退为字符串）。 */
export function getAdResourceTypeLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdResourceTypeLabel[value] ?? String(value);
}
export function getAdResourceStatusLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdResourceStatusLabel[value] ?? String(value);
}
export function getAdCustomerStatusLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdCustomerStatusLabel[value] ?? String(value);
}
export function getAdCustomerLevelLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdCustomerLevelLabel[value] ?? String(value);
}
