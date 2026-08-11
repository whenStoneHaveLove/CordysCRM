export enum SystemRouteEnum {
  SYSTEM = 'system',
  SYSTEM_ORG = 'systemOrg',
  SYSTEM_ROLE = 'systemRole',
  SYSTEM_MODULE = 'systemModule',
  SYSTEM_BUSINESS = 'systemBusiness',
  SYSTEM_LICENSE = 'systemLicense',
  SYSTEM_LOG = 'systemLog',
  SYSTEM_MESSAGE = 'systemMessage',
  SYSTEM_PROCESS = 'systemProcess',
  SYSTEM_PROCESS_INDEX = 'systemProcessIndex',
  SYSTEM_PROCESS_WORKFLOW = 'systemProcessWorkflow',
}

export enum OpportunityRouteEnum {
  OPPORTUNITY = 'opportunity',
  OPPORTUNITY_OPT = 'opportunityOpt',
  OPPORTUNITY_QUOTATION = 'opportunityQuotation',
}

export enum ClueRouteEnum {
  CLUE_MANAGEMENT = 'leadManagement',
  CLUE_MANAGEMENT_CLUE = 'leadManagementLead',
  CLUE_MANAGEMENT_POOL = 'leadManagementPool',
}

export enum CustomerRouteEnum {
  CUSTOMER = 'account',
  CUSTOMER_INDEX = 'accountIndex',
  CUSTOMER_CONTACT = 'accountContact',
  CUSTOMER_OPEN_SEA = 'accountOpenSea',
}

export enum ContractRouteEnum {
  CONTRACT = 'contract',
  CONTRACT_INDEX = 'contractIndex',
  CONTRACT_PAYMENT = 'contractPaymentPlan',
  CONTRACT_PAYMENT_RECORD = 'contractPaymentRecord',
  CONTRACT_BUSINESS_NAME = 'contractBusinessName',
  CONTRACT_INVOICE = 'contractInvoice',
}

export enum OrderRouteEnum {
  ORDER = 'order',
  ORDER_INDEX = 'orderIndex',
}

export enum ProductRouteEnum {
  PRODUCT = 'product',
  PRODUCT_PRO = 'productPro',
  PRODUCT_PRICE = 'productPrice',
}

export enum PersonalRouteEnum {
  PERSONAL_INFO = 'personalInfo',
  PERSONAL_PLAN = 'personalPlan',
  PERSONAL_EXPORT = 'personalExport',
  LOGOUT = 'logout',
}

export enum WorkbenchRouteEnum {
  WORKBENCH = 'workbench',
  WORKBENCH_INDEX = 'workbenchIndex',
}

export enum AgentRouteEnum {
  AGENT = 'agent',
  AGENT_INDEX = 'agentIndex',
}

export enum DashboardRouteEnum {
  DASHBOARD = 'dashboard',
  DASHBOARD_INDEX = 'dashboardIndex',
  DASHBOARD_LINK = 'dashboardLink',
  DASHBOARD_MODULE = 'dashboardModule',
}

export enum TenderRouteEnum {
  TENDER = 'tender',
  TENDER_INDEX = 'tenderIndex',
}

export enum FullPageEnum {
  FULL_PAGE = 'fullPage',
  FULL_PAGE_DASHBOARD = 'fullPageDashboard',
  FULL_PAGE_EXPORT_QUOTATION = 'fullPageExportQuotation',
  FULL_PAGE_EXPORT_ORDER = 'fullPageExportOrder',
}

export enum CustomFormRouteEnum {
  CUSTOM_FORM = 'customForm',
  CUSTOM_FORM_INDEX = 'customFormIndex',
}

export enum AdvertisingRouteEnum {
  // 父模块（左侧导航分组）
  ADVERTISING_ORDER_MANAGEMENT = 'advertisingOrderManagement',
  ADVERTISING_CONTRACT_MANAGEMENT = 'advertisingContractManagement',
  ADVERTISING_RESOURCE_MANAGEMENT = 'advertisingResourceManagement',
  ADVERTISING_WORKBENCH_GROUP = 'advertisingWorkbenchGroup',
  ADVERTISING_APPROVAL_GROUP = 'advertisingApprovalGroup',
  ADVERTISING_REPORT_GROUP = 'advertisingReportGroup',
  ADVERTISING_SYSTEM_GROUP = 'advertisingSystemGroup',
  ADVERTISING_ORDER = 'advertisingOrder',
  ADVERTISING_ORDER_CREATE = 'advertisingOrderCreate',
  ADVERTISING_ORDER_EDIT = 'advertisingOrderEdit',
  ADVERTISING_ORDER_DETAIL = 'advertisingOrderDetail',
  ADVERTISING_CHANGE = 'advertisingChange',
  ADVERTISING_CHANGE_CREATE = 'advertisingChangeCreate',
  ADVERTISING_CHANGE_DETAIL = 'advertisingChangeDetail',
  ADVERTISING_PAYMENT = 'advertisingPayment',
  ADVERTISING_PAYMENT_CREATE = 'advertisingPaymentCreate',
  ADVERTISING_PAYMENT_DETAIL = 'advertisingPaymentDetail',
  ADVERTISING_PAYMENT_MEDIA = 'advertisingPaymentMedia',
  ADVERTISING_CONTRACT = 'advertisingContract',
  ADVERTISING_CONTRACT_CREATE = 'advertisingContractCreate',
  ADVERTISING_CONTRACT_EDIT = 'advertisingContractEdit',
  ADVERTISING_CONTRACT_DETAIL = 'advertisingContractDetail',
  ADVERTISING_SEAL = 'advertisingSeal',
  ADVERTISING_SEAL_APPLY = 'advertisingSealApply',
  ADVERTISING_SEAL_APPROVE = 'advertisingSealApprove',
  ADVERTISING_SEAL_DETAIL = 'advertisingSealDetail',
  ADVERTISING_UPSTREAM_AGENT = 'advertisingUpstreamAgent',
  ADVERTISING_UPSTREAM_AGENT_DETAIL = 'advertisingUpstreamAgentDetail',
  ADVERTISING_DOWNSTREAM_MEDIA = 'advertisingDownstreamMedia',
  ADVERTISING_DOWNSTREAM_MEDIA_DETAIL = 'advertisingDownstreamMediaDetail',
  ADVERTISING_CUSTOMER = 'advertisingCustomer',
  ADVERTISING_CUSTOMER_CREATE = 'advertisingCustomerCreate',
  ADVERTISING_CUSTOMER_EDIT = 'advertisingCustomerEdit',
  ADVERTISING_CUSTOMER_DETAIL = 'advertisingCustomerDetail',
  ADVERTISING_WORKBENCH = 'advertisingWorkbench',
  ADVERTISING_APPROVAL = 'advertisingApproval',
  ADVERTISING_REPORT = 'advertisingReport',
  ADVERTISING_BUSINESS_ENTITY = 'advertisingBusinessEntity',
  ADVERTISING_BUSINESS_ENTITY_CREATE = 'advertisingBusinessEntityCreate',
  ADVERTISING_BUSINESS_ENTITY_EDIT = 'advertisingBusinessEntityEdit',
  ADVERTISING_BUSINESS_ENTITY_DETAIL = 'advertisingBusinessEntityDetail',
  ADVERTISING_SYSTEM = 'advertisingSystem',
  ADVERTISING_SYSTEM_BE = 'advertisingSystemBe',
  ADVERTISING_SYSTEM_DICT = 'advertisingSystemDict',
  ADVERTISING_SYSTEM_SETTINGS = 'advertisingSystemSettings',
  ADVERTISING_LOG_GROUP = 'advertisingLogGroup',
  ADVERTISING_LOG = 'advertisingLog',
}

export const AppRouteEnum = {
  ...SystemRouteEnum,
  ...OpportunityRouteEnum,
  ...ClueRouteEnum,
  ...CustomerRouteEnum,
  ...ProductRouteEnum,
  ...PersonalRouteEnum,
  ...WorkbenchRouteEnum,
  ...DashboardRouteEnum,
  ...AgentRouteEnum,
  ...ContractRouteEnum,
  ...OrderRouteEnum,
  ...TenderRouteEnum,
  ...CustomFormRouteEnum,
  ...AdvertisingRouteEnum,
};
