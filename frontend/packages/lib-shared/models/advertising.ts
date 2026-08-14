/**
 * 广告模块前端类型定义（镜像后端 ad 包 DTO / 实体字段）。
 * 金额字段统一为 number（BigDecimal 序列化后为 JSON number）；
 * 日期字段为 number(epoch ms) | string(ISO) | null，由前端统一格式化。
 */
import type { CommonList } from './common';

/* ----------------------------- 订单 ----------------------------- */

export interface AdOrderPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  status?: number | null;
  businessEntityId?: string | null;
  customerId?: string | null;
  orderType?: number | null;
  receiptMethod?: number | null;
  paymentMethod?: number | null;
  deliveryStartFrom?: number | null;
  deliveryStartTo?: number | null;
  [key: string]: any;
}

export interface AdOrderSaveParams {
  id?: string;
  orderName?: string;
  businessEntityId?: string;
  customerId?: string;
  industryCode?: string;
  signingEntity?: string;
  orderType?: number;
  /** 关联合同id（框架订单必选框架合同；单笔订单可后补） */
  contractId?: string;
  /** 下游媒体id列表 */
  downstreamMediaIds?: string[];
  upstreamAgentId?: string;
  agentOrderNo?: string;
  totalAmount?: number;
  rebateMode?: number;
  rebateValue?: number;
  noRebateAmount?: number;
  mediaPayableAmount?: number;
  deliveryStartDate?: number | null;
  deliveryEndDate?: number | null;
  deliveryVolume?: string;
  remark?: string;
  receiptMethod?: number;
  receiptPrepayMode?: number;
  receiptPrepayRatio?: number;
  receiptPrepayAmount?: number;
  receiptPrepayDeadline?: number | null;
  receiptAccountPeriodDays?: number;
  paymentMethod?: number;
  paymentPrepayMode?: number;
  paymentPrepayRatio?: number;
  paymentPrepayAmount?: number;
  paymentPrepayDeadline?: number | null;
  paymentPostpayTrigger?: number;
  paymentPostpayDays?: number;
  currency?: string;
}

export interface AdOrderApproveParams {
  action?: string;
  remark?: string;
}
export interface AdOrderVoidParams {
  reason?: string;
}
export interface AdOrderForceArchiveParams {
  badDebtAmount?: number;
}

export interface AdOrderListItem {
  id: string;
  orderNo?: string;
  orderName?: string;
  businessEntityId?: string;
  businessEntityName?: string;
  customerId?: string;
  customerName?: string;
  orderType?: number;
  status?: number;
  totalAmount?: number;
  receivableAmount?: number;
  mediaPayableAmount?: number;
  rebateAmount?: number;
  receiptMethod?: number;
  paymentMethod?: number;
  deliveryStartDate?: number | string | null;
  deliveryEndDate?: number | string | null;
  creatorId?: string;
  createTime?: number;
  missingContract?: number;
  receiptDone?: number;
  paymentDone?: number;
}

export interface AdOrderAttachment {
  id: string;
  orderId?: string;
  type?: number;
  fileUrl?: string;
  fileName?: string;
}

export interface AdOrderChange {
  id: string;
  orderId?: string;
  changeFields?: string;
  reason?: string;
  snapshotBefore?: string;
  snapshotAfter?: string;
  status?: number;
  approverId?: string;
  approvedAt?: number | string | null;
  approveRemark?: string;
}

export interface AdOrderLog {
  id: string;
  orderId?: string;
  action?: string;
  operatorId?: string;
  beforeValue?: string;
  afterValue?: string;
  ip?: string;
  createTime?: number;
}

export interface AdOrderAllowedAction {
  fromStatus?: number;
  toStatus?: number;
  trigger?: string;
  requiredRole?: string;
  allowed?: boolean;
  label?: string;
}

export interface AdOrderInfo {
  id: string;
  orderNo?: string;
  orderName?: string;
  businessEntityId?: string;
  customerId?: string;
  industryCode?: string;
  signingEntity?: string;
  orderType?: number;
  upstreamAgentId?: string;
  agentOrderNo?: string;
  creatorId?: string;
  status?: number;
  totalAmount?: number;
  rebateMode?: number;
  rebateValue?: number;
  noRebateAmount?: number;
  rebateAmount?: number;
  receivableAmount?: number;
  mediaPayableAmount?: number;
  deliveryStartDate?: number | string | null;
  deliveryEndDate?: number | string | null;
  deliveryVolume?: string;
  remark?: string;
  receiptMethod?: number;
  receiptPrepayMode?: number;
  receiptPrepayRatio?: number;
  receiptPrepayAmount?: number;
  receiptPrepayDeadline?: number | string | null;
  receiptAccountPeriodDays?: number;
  paymentMethod?: number;
  paymentPrepayMode?: number;
  paymentPrepayRatio?: number;
  paymentPrepayAmount?: number;
  paymentPrepayDeadline?: number | string | null;
  paymentPostpayTrigger?: number;
  paymentPostpayDays?: number;
  invoiceStatus?: number;
  receiptStatus?: number;
  mediaPaymentStatus?: number;
  invoicedAmount?: number;
  receivedAmount?: number;
  mediaPaidAmount?: number;
  badDebtAmount?: number;
  needsRedInvoice?: number;
  accountPeriodStartDate?: number | string | null;
  accountPeriodEndDate?: number | string | null;
  executionCompletedAt?: number | string | null;
  archivedAt?: number | string | null;
  voidedAt?: number | string | null;
  voidReason?: string;
  currency?: string;
  createTime?: number;
  updateTime?: number;
}

export interface AdOrderDetail {
  order: AdOrderInfo;
  contractId?: string;
  downstreamMediaIds?: string[];
  attachments: AdOrderAttachment[];
  changes: AdOrderChange[];
  logs: AdOrderLog[];
  allowedActions: AdOrderAllowedAction[];
}

export type AdOrderPageResult = CommonList<AdOrderListItem>;

/* ----------------------------- 改单 ----------------------------- */

export interface AdOrderChangePageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  status?: number | null;
  orderId?: string | null;
  [key: string]: any;
}

export interface AdOrderChangeSaveParams {
  orderId?: string;
  reason?: string;
  changeFields?: string[];
  after?: Record<string, any>;
}
export interface AdOrderChangeApproveParams {
  remark?: string;
}

export interface AdOrderChangeListItem {
  id: string;
  orderId?: string;
  orderNo?: string;
  orderName?: string;
  changeFields?: string;
  reason?: string;
  status?: number;
  statusLabel?: string;
  approverId?: string;
  approvedAt?: number | string | null;
  creatorId?: string;
  createTime?: number;
}

export interface AdOrderChangeDetail {
  change: AdOrderChange;
  orderNo?: string;
  statusLabel?: string;
}

export type AdOrderChangePageResult = CommonList<AdOrderChangeListItem>;

/* ----------------------------- 合同 ----------------------------- */

export interface AdContractPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  contractDirection?: number | null;
  contractType?: number | null;
  relatedPartyType?: number | null;
  status?: number | null;
  sealStatus?: number | null;
  businessEntityId?: string | null;
  orderId?: string | null;
  changeOrderId?: string | null;
  validFromFrom?: number | null;
  validFromTo?: number | null;
  [key: string]: any;
}

export interface AdContractSaveParams {
  id?: string;
  contractNo?: string;
  contractName?: string;
  businessEntityId?: string;
  contractDirection?: number;
  contractType?: number;
  relatedPartyId?: string;
  relatedPartyType?: number;
  orderId?: string;
  changeOrderId?: string;
  signingEntity?: string;
  validFrom?: number | null;
  validTo?: number | null;
  amount?: number;
  rebateTerms?: string;
  fileUrl?: string;
}

export interface AdContractInfo {
  id: string;
  contractNo?: string;
  contractName?: string;
  businessEntityId?: string;
  contractDirection?: number;
  contractType?: number;
  relatedPartyId?: string;
  relatedPartyType?: number;
  orderId?: string;
  changeOrderId?: string;
  signingEntity?: string;
  validFrom?: number | string | null;
  validTo?: number | string | null;
  amount?: number;
  rebateTerms?: string;
  fileUrl?: string;
  doubleSealFileUrl?: string;
  archiveApproveRemark?: string;
  archiveApproveUser?: string;
  archiveApproveTime?: number;
  sealStatus?: number;
  status?: number;
  organizationId?: string;
  deleted?: number;
  createTime?: number;
  updateTime?: number;
  createUser?: string;
  updateUser?: string;
}

export interface AdContractListItem {
  id: string;
  contractNo?: string;
  contractName?: string;
  businessEntityId?: string;
  businessEntityName?: string;
  contractDirection?: number;
  contractDirectionLabel?: string;
  contractType?: number;
  contractTypeLabel?: string;
  relatedPartyId?: string;
  relatedPartyType?: number;
  relatedPartyTypeLabel?: string;
  orderId?: string;
  signingEntity?: string;
  validFrom?: number | string | null;
  validTo?: number | string | null;
  amount?: number;
  fileUrl?: string;
  sealStatus?: number;
  sealStatusLabel?: string;
  status?: number;
  statusLabel?: string;
  organizationId?: string;
  createTime?: number;
}

export interface AdContractDetailResponse {
  contract: AdContractInfo;
  businessEntityName?: string;
  relatedPartyName?: string;
  orderNo?: string;
  statusLabel?: string;
  sealStatusLabel?: string;
  directionLabel?: string;
  typeLabel?: string;
  sealRecords: AdSealRecordInfo[];
}

export type AdContractPageResult = CommonList<AdContractListItem>;

/* ----------------------------- 用印记录 ----------------------------- */

export interface AdSealRecordPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  contractId?: string | null;
  sealType?: number | null;
  status?: number | null;
  businessEntityId?: string | null;
  applyDateFrom?: number | null;
  applyDateTo?: number | null;
  [key: string]: any;
}

export interface AdSealApplyParams {
  contractId?: string;
  sealType?: number;
  appliedCopies?: number;
  applyRemark?: string;
}

export interface AdSealApproveParams {
  actualCopies?: number;
  approveRemark?: string;
}

export interface AdSealUploadParams {
  fileUrl?: string;
}

export interface AdSealRecordInfo {
  id: string;
  contractId?: string;
  sealType?: number;
  appliedCopies?: number;
  actualCopies?: number;
  applicantId?: string;
  applyRemark?: string;
  status?: number;
  approverId?: string;
  approvedAt?: number | string | null;
  approveRemark?: string;
  organizationId?: string;
  deleted?: number;
  createUser?: string;
  updateUser?: string;
  createTime?: number;
  updateTime?: number;
}

export interface AdSealRecordListItem {
  id: string;
  contractId?: string;
  contractNo?: string;
  contractName?: string;
  businessEntityId?: string;
  businessEntityName?: string;
  sealType?: number;
  sealTypeLabel?: string;
  appliedCopies?: number;
  actualCopies?: number;
  applicantId?: string;
  applyRemark?: string;
  status?: number;
  statusLabel?: string;
  approverId?: string;
  approvedAt?: number | string | null;
  approveRemark?: string;
  organizationId?: string;
  createTime?: number;
}

export interface AdSealRecordDetailResponse {
  record: AdSealRecordInfo;
  contractNo?: string;
  businessEntityName?: string;
  orderNo?: string;
  statusLabel?: string;
}

export type AdSealRecordPageResult = CommonList<AdSealRecordListItem>;

/* ----------------------------- 业务主体 ----------------------------- */

export interface AdBusinessEntityPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  status?: number | null;
  [key: string]: any;
}

export interface AdBusinessEntitySaveParams {
  id?: string;
  name?: string;
  code?: string;
  status?: number;
  /** 跨主体隔离标记：0 否 / 1 是（is_cross_entity，PRD §3.2/§3.4）。 */
  isCrossEntity?: number;
  remark?: string;
}

/** 业务主体列表项（扁平，对应后端分页列表返回；列表接口未返回 userCount/orderCount 时为空）。 */
export interface AdBusinessEntityListItem {
  id: string;
  name?: string;
  code?: string;
  status?: number;
  statusLabel?: string;
  /** 跨主体隔离标记：0 否 / 1 是（is_cross_entity，PRD §3.2/§3.4）。 */
  isCrossEntity?: number;
  remark?: string;
  /** 关联用户数（读 AD_USER_BUSINESS_ENTITY:READ，后端待补）。列表接口未返回时为空。 */
  userCount?: number;
  /** 关联订单数。列表接口未返回时为空。 */
  orderCount?: number;
  createTime?: number;
  updateTime?: number;
}

/** 业务主体详情（嵌套 entity，对应后端 AdBusinessEntityDetailResponse）。 */
export interface AdBusinessEntityInfo {
  /** 主体主信息（后端 entity 为嵌套对象）。 */
  entity: {
    id?: string;
    name?: string;
    code?: string;
    status?: number;
    isCrossEntity?: number;
    remark?: string;
    createUser?: string;
    updateUser?: string;
    createTime?: number;
    updateTime?: number;
  };
  /** 状态标签（顶层）。 */
  statusLabel?: string;
  /** 关联用户数。 */
  userCount?: number;
  /** 关联订单数。 */
  orderCount?: number;
  createTime?: number;
  updateTime?: number;
}

export interface AdBusinessEntityDetail extends AdBusinessEntityInfo {}

export type AdBusinessEntityPageResult = CommonList<AdBusinessEntityListItem>;

/* ----------------------------- 字典 ----------------------------- */

export interface AdDictPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  dictCode?: string | null;
  status?: number | null;
  [key: string]: any;
}

export interface AdDictSaveParams {
  id?: string;
  /** 字典分组编码，如 industry / media_type / seal_type / receipt_method / payment_method（PRD §5.3）。 */
  dictCode?: string;
  /** 显示名称。 */
  dictLabel?: string;
  /** 字典值。 */
  dictValue?: string;
  sort?: number;
  status?: number;
  remark?: string;
}

export interface AdDictInfo {
  id: string;
  dictCode?: string;
  dictLabel?: string;
  dictValue?: string;
  sort?: number;
  status?: number;
  statusLabel?: string;
  remark?: string;
  createTime?: number;
}

export type AdDictPageResult = CommonList<AdDictInfo>;

/* ----------------------------- 系统开关 / 账期规则 ----------------------------- */

export interface AdSettingInfo {
  /** 审批开关：0 关 / 1 开（L-14，关闭时订单 0→20 直接通过）。 */
  approvalSwitch?: number;
  /** 默认账期天数。 */
  defaultAccountPeriodDays?: number;
  [key: string]: any;
}

/* ----------------------------- 审批中心 ----------------------------- */

/** 审批类型（对应三 Tab：订单/改单/用印）。 */
export type AdApprovalType = 'order' | 'change' | 'seal';

export interface AdApprovalPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  /** 审批类型：order/change/seal（三 Tab 过滤）。 */
  type?: string | null;
  [key: string]: any;
}

export interface AdApprovalTodoItem {
  id?: string;
  /** 业务单据 ID（订单/改单/用印记录），用于审批动作与跳转详情。 */
  businessId?: string;
  /** 审批类型：order/change/seal。 */
  type?: string;
  typeLabel?: string;
  applicantId?: string;
  applicantName?: string;
  /** 关联单号（订单号/改单号/合同号）。 */
  refNo?: string;
  businessEntityName?: string;
  customerName?: string;
  /** 金额。 */
  amount?: number;
  /** 摘要。 */
  summary?: string;
  /** 提交时间。 */
  submitTime?: number | string | null;
  createTime?: number | string | null;
  status?: number;
  [key: string]: any;
}

export type AdApprovalPageResult = CommonList<AdApprovalTodoItem>;

/** 待审计数（按 type 聚合）。 */
export interface AdApprovalPendingCountItem {
  type?: string;
  typeLabel?: string;
  count?: number;
}
export type AdApprovalPendingCountResult = AdApprovalPendingCountItem[];

/* ----------------------------- 工作台 ----------------------------- */

export interface AdWorkbenchTodoItem {
  /** 待办类型码（如 pendingSubmit / pendingApprove / financePrepay ...）。 */
  todoType?: string;
  todoLabel?: string;
  count?: number;
  refNo?: string;
  amount?: number;
  dueDate?: number | string | null;
  /** 点击跳转的路由 name（可选）。 */
  routeName?: string;
  routeParams?: Record<string, any>;
  [key: string]: any;
}

export interface AdWorkbenchTodoResult {
  media?: AdWorkbenchTodoItem[];
  boss?: AdWorkbenchTodoItem[];
  finance?: AdWorkbenchTodoItem[];
  [key: string]: any;
}

/* ----------------------------- 报表中心 ----------------------------- */

/** 订单执行汇总（按状态统计数量/金额）。 */
export interface AdReportOrderSummaryItem {
  status?: number;
  statusLabel?: string;
  count?: number;
  amount?: number;
}
export interface AdReportOrderSummary {
  total?: number;
  totalAmount?: number;
  items?: AdReportOrderSummaryItem[];
  [key: string]: any;
}

/** 应收应付汇总（已付/未付/逾期）。 */
export interface AdReportPaymentSummary {
  receivableAmount?: number;
  mediaPayableAmount?: number;
  paidAmount?: number;
  unpaidAmount?: number;
  overdueAmount?: number;
  [key: string]: any;
}

/** 月度趋势。 */
export interface AdReportMonthlyTrendItem {
  month?: string;
  orderCount?: number;
  amount?: number;
}
export interface AdReportMonthlyTrendResult {
  year?: number;
  items?: AdReportMonthlyTrendItem[];
  [key: string]: any;
}

/** 6 类报表（PRD §9.5，后端待补 B-6）通用返回结构。 */
export interface AdReportItem {
  code?: string;
  label?: string;
  total?: number;
  totalAmount?: number;
  items?: Array<Record<string, any>>;
  [key: string]: any;
}

/* ----------------------------- 资源 ----------------------------- */

export interface AdResourcePageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  resourceType?: number | null;
  status?: number | null;
  businessEntityId?: string | null;
  [key: string]: any;
}

export interface AdResourceSaveParams {
  id?: string;
  resourceName?: string;
  resourceType?: number;
  mediaType?: string;
  channel?: string;
  rateCard?: number | null;
  discountPolicy?: string;
  creditCode?: string;
  signingEntity?: string;
  businessEntityId?: string;
  position?: string;
  dailyImpressions?: number | null;
  unitPrice?: number | null;
  remark?: string;
}

export interface AdResourceInfo {
  /** 资源主信息（后端 AdResourceDetailResponse.resource 为嵌套对象）。 */
  resource: {
    id?: string;
    resourceName?: string;
    resourceType?: number;
    mediaType?: string;
    channel?: string;
    rateCard?: number | null;
    discountPolicy?: string;
    creditCode?: string;
    signingEntity?: string;
    businessEntityId?: string;
    position?: string;
    dailyImpressions?: number | null;
    unitPrice?: number | null;
    status?: number;
    remark?: string;
  };
  /** 资源类型标签（顶层）。 */
  resourceTypeLabel?: string;
  /** 状态标签（顶层）。 */
  statusLabel?: string;
  /** 归属业务主体名称（顶层）。 */
  businessEntityName?: string;
}

export interface AdResourceListItem {
  id: string;
  resourceName?: string;
  resourceType?: number;
  resourceTypeLabel?: string;
  mediaType?: string;
  mediaTypeLabel?: string;
  channel?: string;
  businessEntityId?: string;
  businessEntityName?: string;
  rateCard?: number;
  position?: string;
  status?: number;
  statusLabel?: string;
}

export type AdResourcePageResult = CommonList<AdResourceListItem>;

/* ----------------------------- 客户库 ----------------------------- */

export interface AdCustomerPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  industryCode?: string | null;
  customerLevel?: number | null;
  status?: number | null;
  [key: string]: any;
}

export interface AdCustomerSaveParams {
  id?: string;
  /** 客户名称：全局唯一（按名称去重，无业务主体字段），PRD §8.4。 */
  customerName?: string;
  brand?: string;
  industryCode?: string;
  signingEntity?: string;
  contactPerson?: string;
  contactPhone?: string;
  email?: string;
  address?: string;
  industry?: string;
  customerLevel?: number;
  status?: number;
  remark?: string;
}

export interface AdCustomerInfo {
  /** 客户主信息（后端 AdCustomerDetailResponse.customer 为嵌套对象，实体字段名为 name） */
  customer: {
    id: string;
    /** 客户名称：后端实体字段为 name（非列表/保存用的 customerName） */
    name?: string;
    brand?: string;
    industryCode?: string;
    signingEntity?: string;
    contactPerson?: string;
    contactPhone?: string;
    email?: string;
    address?: string;
    industry?: string;
    customerLevel?: number;
    status?: number;
    remark?: string;
    createUser?: string;
    updateUser?: string;
    createTime?: number;
    updateTime?: number;
  };
  /** 客户等级标签 */
  customerLevelLabel?: string;
  /** 状态标签 */
  statusLabel?: string;
  /** 关联订单数（后端详情接口当前未返回，前端兜底为 0） */
  orderCount?: number;
}

/** 广告客户实体（后端新建/编辑返回 AdCustomer 实体，字段为 name 的扁平结构）。 */
export interface AdCustomerEntity {
  id?: string;
  /** 客户名称：后端实体字段为 name（非列表/保存用的 customerName） */
  name?: string;
  brand?: string;
  industryCode?: string;
  signingEntity?: string;
  contactPerson?: string;
  contactPhone?: string;
  email?: string;
  address?: string;
  industry?: string;
  customerLevel?: number;
  status?: number;
  remark?: string;
}

export interface AdCustomerListItem {
  id: string;
  customerName?: string;
  brand?: string;
  industry?: string;
  industryCode?: string;
  industryLabel?: string;
  customerLevel?: number;
  customerLevelLabel?: string;
  status?: number;
  statusLabel?: string;
  signingEntity?: string;
}

export type AdCustomerPageResult = CommonList<AdCustomerListItem>;

/* ==================== 收款单 ==================== */

export interface AdReceiptSaveParams {
  id?: string;
  orderId?: string;
  amount?: number;
  receiptTime?: number | string | null;
  type?: number;
  voucherUrl?: string;
  remark?: string;
}

export interface AdReceiptApproveParams {
  action?: string;
  remark?: string;
}

export interface AdReceiptPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  orderId?: string;
  status?: number | null;
  type?: number | null;
  [key: string]: any;
}

export interface AdReceiptInfo {
  id?: string;
  receiptNo?: string;
  orderId?: string;
  orderName?: string;
  amount?: number;
  receiptTime?: number | string | null;
  type?: number;
  typeLabel?: string;
  status?: number;
  statusLabel?: string;
  voucherUrl?: string;
  approveUser?: string;
  approveTime?: number;
  approveRemark?: string;
  remark?: string;
  createTime?: number;
}

export interface AdContractBriefInfo {
  id?: string;
  contractNo?: string;
  contractName?: string;
  contractType?: number;
  contractDirection?: number;
  amount?: number;
  sealStatus?: number;
}

export interface AdReceiptDetail {
  id?: string;
  receiptNo?: string;
  orderId?: string;
  amount?: number;
  receiptTime?: number | string | null;
  type?: number;
  typeLabel?: string;
  status?: number;
  statusLabel?: string;
  voucherUrl?: string;
  remark?: string;
  createUser?: string;
  createTime?: number;
  updateUser?: string;
  updateTime?: number;
  approveUser?: string;
  approveTime?: number;
  approveRemark?: string;
  orderNo?: string;
  orderName?: string;
  contracts?: AdContractBriefInfo[];
}

export type AdReceiptPageResult = CommonList<AdReceiptInfo>;

/* ==================== 付款单 ==================== */

export interface AdPayoutSaveParams {
  id?: string;
  orderId?: string;
  amount?: number;
  paymentTime?: number | string | null;
  type?: number;
  mediaIds?: string[];
  voucherUrl?: string;
  remark?: string;
}

export interface AdPayoutApproveParams {
  action?: string;
  remark?: string;
}

export interface AdPayoutPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  orderId?: string;
  status?: number | null;
  type?: number | null;
  [key: string]: any;
}

export interface AdPayoutInfo {
  id?: string;
  paymentNo?: string;
  orderId?: string;
  orderName?: string;
  amount?: number;
  paymentTime?: number | string | null;
  type?: number;
  typeLabel?: string;
  status?: number;
  statusLabel?: string;
  mediaIds?: string;
  voucherUrl?: string;
  approveUser?: string;
  approveTime?: number;
  approveRemark?: string;
  remark?: string;
  createTime?: number;
}

export interface AdPayoutDetail {
  id?: string;
  paymentNo?: string;
  orderId?: string;
  amount?: number;
  paymentTime?: number | string | null;
  type?: number;
  typeLabel?: string;
  status?: number;
  statusLabel?: string;
  mediaIds?: string;
  voucherUrl?: string;
  remark?: string;
  createUser?: string;
  createTime?: number;
  updateUser?: string;
  updateTime?: number;
  approveUser?: string;
  approveTime?: number;
  approveRemark?: string;
  orderNo?: string;
  orderName?: string;
  contracts?: AdContractBriefInfo[];
}

export type AdPayoutPageResult = CommonList<AdPayoutInfo>;
