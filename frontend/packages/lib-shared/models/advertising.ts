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
  receiptPrepayDeadline?: number | null;
  receiptAccountPeriodDays?: number;
  paymentMethod?: number;
  paymentPrepayMode?: number;
  paymentPrepayRatio?: number;
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
  attachments: AdOrderAttachment[];
  changes: AdOrderChange[];
  logs: AdOrderLog[];
  allowedActions: AdOrderAllowedAction[];
}

export interface AdFinancialStep {
  action?: string;
  amount?: number;
  description?: string;
}
export interface AdOrderFinancialPlan {
  toStatus?: number;
  receiptPrepayAmount?: number;
  paymentPrepayAmount?: number;
  steps?: AdFinancialStep[];
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

/* ----------------------------- 收付款 ----------------------------- */

export interface AdPaymentRecordPageParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  orderId?: string | null;
  businessEntityId?: string | null;
  direction?: number | null;
  type?: number | null;
  occurDateFrom?: number | null;
  occurDateTo?: number | null;
  [key: string]: any;
}

export interface AdPaymentRecordCreateParams {
  orderId?: string;
  direction?: number;
  type?: number;
  amount?: number;
  occurDate?: number | null;
  invoiceNo?: string;
  resourceId?: string;
  remark?: string;
  settleType?: string;
}
export interface AdPaymentCancelParams {
  id?: string;
  reason?: string;
}
export interface AdPaymentConfirmPrepayParams {
  orderId?: string;
  amount?: number;
  occurDate?: number | null;
  remark?: string;
}
export interface AdPaymentInvoiceParams {
  orderId?: string;
  amount?: number;
  invoiceNo?: string;
  occurDate?: number | null;
  remark?: string;
}
export interface AdPaymentReceiveParams {
  orderId?: string;
  amount?: number;
  occurDate?: number | null;
  remark?: string;
}
export interface AdPaymentMediaPrepayParams {
  orderId?: string;
  amount?: number;
  occurDate?: number | null;
  resourceId?: string;
  remark?: string;
  force?: boolean;
}
export interface AdPaymentRedInvoiceClearParams {
  orderId?: string;
}

export interface AdPaymentRecordInfo {
  id: string;
  orderId?: string;
  businessEntityId?: string;
  resourceId?: string;
  direction?: number;
  type?: number;
  amount?: number;
  occurDate?: number | string | null;
  voucherUrl?: string;
  invoiceNo?: string;
  remark?: string;
  operatorId?: string;
  createTime?: number;
}

export interface AdPaymentRecordListItem {
  id: string;
  orderId?: string;
  orderNo?: string;
  businessEntityId?: string;
  businessEntityName?: string;
  customerName?: string;
  resourceName?: string;
  direction?: number;
  directionLabel?: string;
  type?: number;
  typeLabel?: string;
  amount?: number;
  occurDate?: number | string | null;
  invoiceNo?: string;
  operatorId?: string;
  remark?: string;
  createTime?: number;
}

export interface AdPaymentRecordDetail {
  record: AdPaymentRecordInfo;
  orderNo?: string;
  directionLabel?: string;
  typeLabel?: string;
}

export interface AdPaymentTodoParams {
  current?: number;
  pageSize?: number;
  sort?: { name?: string; type?: string };
  keyword?: string;
  businessEntityId?: string | null;
  todoType?: string | null;
  [key: string]: any;
}

export interface AdPaymentTodoItem {
  todoType?: string;
  todoLabel?: string;
  orderId?: string;
  orderNo?: string;
  businessEntityId?: string;
  customerId?: string;
  status?: number;
  amount?: number;
  dueDate?: number | string | null;
  createTime?: number;
}

export type AdPaymentRecordPageResult = CommonList<AdPaymentRecordListItem>;
export type AdPaymentTodoResult = CommonList<AdPaymentTodoItem>;

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
  sealStatus?: number;
  status?: number;
  organizationId?: string;
  deleted?: number;
  createTime?: number;
  updateTime?: number;
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
