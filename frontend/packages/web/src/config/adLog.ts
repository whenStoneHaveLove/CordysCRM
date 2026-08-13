/**
 * 广告操作日志筛选配置。
 * module 与后端写入 ad_operation_log.module 的值保持一致（统一 AD_ 前缀）。
 */
export const adLogModuleOption = [
  { value: 'AD_ORDER', label: 'module.advertising.order' },
  { value: 'AD_ORDER_CHANGE', label: 'module.advertising.change' },
  { value: 'AD_CONTRACT', label: 'module.advertising.contract' },
  { value: 'AD_CUSTOMER', label: 'module.advertising.customer' },
  { value: 'AD_BUSINESS_ENTITY', label: 'module.advertising.businessEntity' },
  { value: 'AD_DOWNSTREAM_MEDIA', label: 'module.advertising.downstreamMedia' },
  { value: 'AD_UPSTREAM_AGENT', label: 'module.advertising.upstreamAgent' },
  { value: 'AD_PAYMENT', label: 'module.advertising.payment' },
  { value: 'AD_SEAL', label: 'module.advertising.seal' },
  { value: 'AD_SUPPORT', label: 'module.advertising.workbench' },
  { value: 'AD_DICT', label: 'module.advertising.dict' },
];

export const adLogActionOption = [
  { value: 'CREATE', label: 'advertising.log.action.CREATE' },
  { value: 'UPDATE', label: 'advertising.log.action.UPDATE' },
  { value: 'DELETE', label: 'advertising.log.action.DELETE' },
  { value: 'SUBMIT', label: 'advertising.log.action.SUBMIT' },
  { value: 'APPROVE', label: 'advertising.log.action.APPROVE' },
  { value: 'REJECT', label: 'advertising.log.action.REJECT' },
  { value: 'EXECUTE', label: 'advertising.log.action.EXECUTE' },
  { value: 'VOID', label: 'advertising.log.action.VOID' },
  { value: 'FORCE_ARCHIVE', label: 'advertising.log.action.FORCE_ARCHIVE' },
  { value: 'FINANCIAL_PRE_ACTION', label: 'advertising.log.action.FINANCIAL_PRE_ACTION' },
  { value: 'CONFIRM_EXECUTE', label: 'advertising.log.action.CONFIRM_EXECUTE' },
  { value: 'COMPLETE_EXECUTE', label: 'advertising.log.action.COMPLETE_EXECUTE' },
  { value: 'DISABLE', label: 'advertising.log.action.DISABLE' },
  { value: 'APPLY', label: 'advertising.log.action.APPLY' },
  { value: 'CLOSE', label: 'advertising.log.action.CLOSE' },
  { value: 'RECEIVE', label: 'advertising.log.action.RECEIVE' },
  { value: 'PAY_MEDIA_PREPAY', label: 'advertising.log.action.PAY_MEDIA_PREPAY' },
  { value: 'PAY_MEDIA_POSTPAY', label: 'advertising.log.action.PAY_MEDIA_POSTPAY' },
  { value: 'RED_INVOICE_CLEAR', label: 'advertising.log.action.RED_INVOICE_CLEAR' },
  { value: 'CANCEL', label: 'advertising.log.action.CANCEL' },
  { value: 'CHANGE_REFUND', label: 'advertising.log.action.CHANGE_REFUND' },
  { value: 'CHANGE_BAD_DEBT', label: 'advertising.log.action.CHANGE_BAD_DEBT' },
  { value: 'SUBMIT_ARCHIVE', label: 'advertising.log.action.SUBMIT_ARCHIVE' },
  { value: 'APPROVE_ARCHIVE', label: 'advertising.log.action.APPROVE_ARCHIVE' },
  { value: 'REJECT_ARCHIVE', label: 'advertising.log.action.REJECT_ARCHIVE' },
];
