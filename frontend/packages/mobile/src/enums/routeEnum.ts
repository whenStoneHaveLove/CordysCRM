export enum OpportunityRouteEnum {
  OPPORTUNITY = 'opportunity',
  OPPORTUNITY_INDEX = 'opportunityIndex',
  OPPORTUNITY_DETAIL = 'opportunityDetail',
}

export enum ClueRouteEnum {
  CLUE = 'lead',
  CLUE_INDEX = 'leadIndex',
  CLUE_DETAIL = 'leadDetail',
  CLUE_POOL_DETAIL = 'leadPoolDetail',
  CONVERT = 'convert',
  MOVE_TO_POOL = 'moveToPool',
}

export enum CustomerRouteEnum {
  CUSTOMER = 'account',
  CUSTOMER_INDEX = 'accountIndex',
  CUSTOMER_DETAIL = 'accountDetail',
  CUSTOMER_TRANSFER = 'accountTransfer',
  CUSTOMER_DISTRIBUTE = 'accountDistribute',
  CUSTOMER_OPENSEA_DETAIL = 'accountOpenSeaDetail',
  CUSTOMER_RELATION = 'accountRelation',
  CUSTOMER_COLLABORATOR = 'accountCollaborator',
}

export enum CommonRouteEnum {
  COMMON = 'common',
  FORM_CREATE = 'formCreate',
  CONTACT_DETAIL = 'contactDetail',
  FOLLOW_DETAIL = 'followDetail',
  WORKFLOW_STAGE = 'workflowStage',
}

export enum ProductRouteEnum {
  PRODUCT = 'product',
  PRODUCT_PRO = 'productPro',
}

export enum MineRouteEnum {
  MINE = 'mine',
  MINE_INDEX = 'mineIndex',
  MINE_MESSAGE = 'mineMessage',
  MINE_DETAIL = 'mineDetail',
}

export enum WorkbenchRouteEnum {
  WORKBENCH = 'workbench',
  WORKBENCH_INDEX = 'workbenchIndex',
  WORKBENCH_AGENT = 'workbenchAgent',
  WORKBENCH_DUPLICATE_CHECK = 'workbenchDuplicateCheck',
  WORKBENCH_DUPLICATE_CHECK_DETAIL = 'workbenchDuplicateCheckDetail',
}

export enum AdvertisingRouteEnum {
  ADVERTISING = 'advertising',
  ADVERTISING_INDEX = 'advertisingIndex',
  AD_WORKBENCH = 'adWorkbench',
  AD_WORKBENCH_INDEX = 'adWorkbenchIndex',
  AD_ORDER = 'adOrder',
  AD_ORDER_INDEX = 'adOrderList',
  AD_ORDER_DETAIL = 'adOrderDetail',
  AD_ORDER_FORM = 'adOrderForm',
  AD_APPROVAL = 'adApproval',
  AD_APPROVAL_INDEX = 'adApprovalList',
  AD_APPROVAL_DETAIL = 'adApprovalDetail',
  AD_CONTRACT = 'adContract',
  AD_CONTRACT_INDEX = 'adContractList',
  AD_CONTRACT_DETAIL = 'adContractDetail',
  AD_CONTRACT_FORM = 'adContractForm',
  AD_CUSTOMER = 'adCustomer',
  AD_CUSTOMER_INDEX = 'adCustomerList',
  AD_CUSTOMER_DETAIL = 'adCustomerDetail',
  AD_CUSTOMER_FORM = 'adCustomerForm',
  AD_RECEIPT = 'adReceipt',
  AD_RECEIPT_INDEX = 'adReceiptList',
  AD_RECEIPT_DETAIL = 'adReceiptDetail',
  AD_RECEIPT_FORM = 'adReceiptForm',
  AD_PAYOUT = 'adPayout',
  AD_PAYOUT_INDEX = 'adPayoutList',
  AD_PAYOUT_DETAIL = 'adPayoutDetail',
  AD_PAYOUT_FORM = 'adPayoutForm',
  AD_DOWNSTREAM_MEDIA = 'adDownstreamMedia',
  AD_DOWNSTREAM_MEDIA_INDEX = 'adDownstreamMediaList',
  AD_DOWNSTREAM_MEDIA_DETAIL = 'adDownstreamMediaDetail',
  AD_DOWNSTREAM_MEDIA_FORM = 'adDownstreamMediaForm',
  AD_UPSTREAM_AGENT = 'adUpstreamAgent',
  AD_UPSTREAM_AGENT_INDEX = 'adUpstreamAgentList',
  AD_UPSTREAM_AGENT_DETAIL = 'adUpstreamAgentDetail',
  AD_UPSTREAM_AGENT_FORM = 'adUpstreamAgentForm',
  AD_BUSINESS_ENTITY = 'adBusinessEntity',
  AD_BUSINESS_ENTITY_INDEX = 'adBusinessEntityList',
  AD_BUSINESS_ENTITY_DETAIL = 'adBusinessEntityDetail',
  AD_BUSINESS_ENTITY_FORM = 'adBusinessEntityForm',
  AD_SEAL = 'adSeal',
  AD_SEAL_INDEX = 'adSealList',
  AD_SEAL_DETAIL = 'adSealDetail',
  AD_SEAL_APPLY = 'adSealApply',
  AD_CHANGE = 'adChange',
  AD_CHANGE_INDEX = 'adChangeList',
  AD_CHANGE_DETAIL = 'adChangeDetail',
  AD_CHANGE_FORM = 'adChangeForm',
  AD_REPORT = 'adReport',
  AD_REPORT_INDEX = 'adReportList',
  AD_LOG = 'adLog',
  AD_LOG_INDEX = 'adLogList',
}

export const AppRouteEnum = {
  ...OpportunityRouteEnum,
  ...ClueRouteEnum,
  ...CustomerRouteEnum,
  ...CommonRouteEnum,
  ...ProductRouteEnum,
  ...MineRouteEnum,
  ...WorkbenchRouteEnum,
  ...AdvertisingRouteEnum,
};
