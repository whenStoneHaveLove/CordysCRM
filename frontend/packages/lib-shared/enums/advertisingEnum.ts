/**
 * 广告模块前端枚举（镜像后端 cn.cordys.crm.ad.common.constants / payment.constants）。
 * 状态码与后端严格一致，前端用于下拉选项与标签渲染。
 */

/** 订单主状态（10 倍数编码，V3.1 §6.1 简化版）。 */
export enum AdOrderStatusEnum {
  DRAFT = 0,
  PENDING_BOSS_APPROVAL = 10,
  PENDING_EXECUTE = 45,
  EXECUTING = 50,
  CHANGE_APPROVING = 60,
  SETTLEMENT = 80,
  ARCHIVED = 90,
  VOIDED = 100,
}

export const AdOrderStatusLabel: Record<number, string> = {
  [AdOrderStatusEnum.DRAFT]: '草稿',
  [AdOrderStatusEnum.PENDING_BOSS_APPROVAL]: '审批中',
  [AdOrderStatusEnum.PENDING_EXECUTE]: '待执行',
  [AdOrderStatusEnum.EXECUTING]: '执行中',
  [AdOrderStatusEnum.CHANGE_APPROVING]: '改单审核中',
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
  [AdOrderChangeStatusEnum.SUBMITTED]: '审批中',
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

/** 收款单状态。 */
export enum AdReceiptStatusEnum {
  DRAFT = 0,
  PENDING_APPROVAL = 10,
  APPROVED = 20,
  REJECTED = 30,
}
export const AdReceiptStatusLabel: Record<number, string> = {
  [AdReceiptStatusEnum.DRAFT]: '草稿',
  [AdReceiptStatusEnum.PENDING_APPROVAL]: '待审核',
  [AdReceiptStatusEnum.APPROVED]: '审核通过',
  [AdReceiptStatusEnum.REJECTED]: '驳回',
};
export const AdReceiptStatusOptions = Object.keys(AdReceiptStatusLabel).map((k) => ({
  label: AdReceiptStatusLabel[Number(k)],
  value: Number(k),
}));

/** 收款单类型。 */
export enum AdReceiptTypeEnum {
  NORMAL = 10,
  REFUND = 20,
}
export const AdReceiptTypeLabel: Record<number, string> = {
  [AdReceiptTypeEnum.NORMAL]: '普通收款',
  [AdReceiptTypeEnum.REFUND]: '退款',
};
export const AdReceiptTypeOptions = Object.keys(AdReceiptTypeLabel).map((k) => ({
  label: AdReceiptTypeLabel[Number(k)],
  value: Number(k),
}));

/** 付款单状态。 */
export enum AdPayoutStatusEnum {
  DRAFT = 0,
  PENDING_APPROVAL = 10,
  APPROVED = 20,
  REJECTED = 30,
}
export const AdPayoutStatusLabel: Record<number, string> = {
  [AdPayoutStatusEnum.DRAFT]: '草稿',
  [AdPayoutStatusEnum.PENDING_APPROVAL]: '待审核',
  [AdPayoutStatusEnum.APPROVED]: '审核通过',
  [AdPayoutStatusEnum.REJECTED]: '驳回',
};
export const AdPayoutStatusOptions = Object.keys(AdPayoutStatusLabel).map((k) => ({
  label: AdPayoutStatusLabel[Number(k)],
  value: Number(k),
}));

/** 付款单类型。 */
export enum AdPayoutTypeEnum {
  NORMAL = 10,
  BAD_DEBT = 20,
}
export const AdPayoutTypeLabel: Record<number, string> = {
  [AdPayoutTypeEnum.NORMAL]: '普通付款',
  [AdPayoutTypeEnum.BAD_DEBT]: '坏账',
};
export const AdPayoutTypeOptions = Object.keys(AdPayoutTypeLabel).map((k) => ({
  label: AdPayoutTypeLabel[Number(k)],
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
export function getAdReceiptStatusLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdReceiptStatusLabel[value] ?? String(value);
}
export function getAdReceiptTypeLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdReceiptTypeLabel[value] ?? String(value);
}
export function getAdPayoutStatusLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdPayoutStatusLabel[value] ?? String(value);
}
export function getAdPayoutTypeLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdPayoutTypeLabel[value] ?? String(value);
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
  SERVICE = 30,
  OTHER = 40,
}
export const AdContractTypeLabel: Record<number, string> = {
  [AdContractTypeEnum.FRAMEWORK]: '框架',
  [AdContractTypeEnum.SINGLE]: '单笔',
  [AdContractTypeEnum.SERVICE]: '服务',
  [AdContractTypeEnum.OTHER]: '其他',
};
export const AdContractTypeOptions = Object.keys(AdContractTypeLabel).map((k) => ({
  label: AdContractTypeLabel[Number(k)],
  value: Number(k),
}));

/** 合同关联方类型（镜像 RelatedPartyType，ad_contract.related_party_type NOT NULL，决定 related_party_id 取数来源）。 */
export enum AdRelatedPartyTypeEnum {
  CUSTOMER = 10,
  UPSTREAM_AGENT = 20,
  DOWNSTREAM_MEDIA = 30,
}
export const AdRelatedPartyTypeLabel: Record<number, string> = {
  [AdRelatedPartyTypeEnum.CUSTOMER]: '客户',
  [AdRelatedPartyTypeEnum.UPSTREAM_AGENT]: '上游代理',
  [AdRelatedPartyTypeEnum.DOWNSTREAM_MEDIA]: '下游媒体',
};
export const AdRelatedPartyTypeOptions = Object.keys(AdRelatedPartyTypeLabel).map((k) => ({
  label: AdRelatedPartyTypeLabel[Number(k)],
  value: Number(k),
}));

/** 合同用印状态（镜像 SealStatus）。 */
export enum AdSealStatusEnum {
  NOT_APPLIED = 0,
  APPROVING = 10,
  SEALED = 20,
  REJECTED = 30,
  ARCHIVE_APPROVING = 40,
  ARCHIVE_REJECTED = 50,
  ARCHIVED = 60,
}
export const AdSealStatusLabel: Record<number, string> = {
  [AdSealStatusEnum.NOT_APPLIED]: '未申请',
  [AdSealStatusEnum.APPROVING]: '审批中',
  [AdSealStatusEnum.SEALED]: '已用印',
  [AdSealStatusEnum.REJECTED]: '已驳回',
  [AdSealStatusEnum.ARCHIVE_APPROVING]: '归档审批中',
  [AdSealStatusEnum.ARCHIVE_REJECTED]: '归档审批驳回',
  [AdSealStatusEnum.ARCHIVED]: '已归档',
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
export function getAdRelatedPartyTypeLabel(value?: number | null): string {
  if (value == null) return '-';
  return AdRelatedPartyTypeLabel[value] ?? String(value);
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
  /** 控件类型：默认按 type 渲染，特殊字段指定下拉/枚举 */
  control?:
    | 'select-customer'
    | 'select-upstream'
    | 'select-industry'
    | 'select-businessEntity'
    | 'enum-orderType'
    | 'enum-rebateMode'
    | 'enum-receiptMethod'
    | 'enum-paymentMethod'
    | 'enum-prepayMode'
    | 'enum-postpayTrigger';
}

export const AD_ORDER_CHANGE_FIELD_META: AdChangeFieldMeta[] = [
  { field: 'orderName', label: '订单名称', type: 'text' },
  { field: 'customerId', label: '客户', type: 'text', control: 'select-customer' },
  { field: 'industryCode', label: '行业类别', type: 'text', control: 'select-industry' },
  { field: 'totalAmount', label: '订单总金额', type: 'number' },
  { field: 'noRebateAmount', label: '不记返金额', type: 'number' },
  { field: 'rebateValue', label: '返点值', type: 'number' },
  { field: 'rebateMode', label: '返点方式', type: 'number', control: 'enum-rebateMode' },
  { field: 'receiptMethod', label: '收款方式', type: 'number', control: 'enum-receiptMethod' },
  { field: 'receiptPrepayMode', label: '预收模式', type: 'number', control: 'enum-prepayMode' },
  { field: 'receiptPrepayRatio', label: '预收比例%', type: 'number' },
  { field: 'receiptPrepayDeadline', label: '预收截止日', type: 'date' },
  { field: 'receiptAccountPeriodDays', label: '账期天数', type: 'number' },
  { field: 'paymentMethod', label: '付款方式', type: 'number', control: 'enum-paymentMethod' },
  { field: 'paymentPrepayMode', label: '媒体预付模式', type: 'number', control: 'enum-prepayMode' },
  { field: 'paymentPrepayRatio', label: '媒体预付比例%', type: 'number' },
  { field: 'paymentPrepayDeadline', label: '媒体预付截止日', type: 'date' },
  { field: 'paymentPostpayTrigger', label: '后付触发', type: 'number', control: 'enum-postpayTrigger' },
  { field: 'paymentPostpayDays', label: '后付天数', type: 'number' },
  { field: 'deliveryStartDate', label: '投放起始日', type: 'date' },
  { field: 'deliveryEndDate', label: '投放结束日', type: 'date' },
  { field: 'deliveryVolume', label: '投放量+单位', type: 'text' },
  { field: 'currency', label: '币种', type: 'text' },
  { field: 'upstreamAgentId', label: '上游代理', type: 'text', control: 'select-upstream' },
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
