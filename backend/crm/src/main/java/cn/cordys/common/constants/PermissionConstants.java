package cn.cordys.common.constants;

/**
 * @author jianxing
 * @date 2025-01-03 11:31:40
 */
public class PermissionConstants {

    /*------ start: SYSTEM_ROLE ------*/
    public static final String SYSTEM_ROLE_READ = "SYSTEM_ROLE:READ";
    public static final String SYSTEM_ROLE_ADD = "SYSTEM_ROLE:ADD";
    public static final String SYSTEM_ROLE_UPDATE = "SYSTEM_ROLE:UPDATE";
    public static final String SYSTEM_ROLE_DELETE = "SYSTEM_ROLE:DELETE";
    public static final String SYSTEM_ROLE_ADD_USER = "SYSTEM_ROLE:ADD_USER";
    public static final String SYSTEM_ROLE_REMOVE_USER = "SYSTEM_ROLE:REMOVE_USER";
    /*------ end: SYSTEM_ROLE------*/


    /*------ start: OPERATION_LOG ------*/
    public static final String OPERATION_LOG_READ = "OPERATION_LOG:READ";
    /*------ end: OPERATION_LOG ------*/

    /*------ start: SYSTEM_NOTICE ------*/
    public static final String SYSTEM_NOTICE_READ = "SYSTEM_NOTICE:READ";
    public static final String SYSTEM_NOTICE_ADD = "SYSTEM_NOTICE:ADD";
    public static final String SYSTEM_NOTICE_UPDATE = "SYSTEM_NOTICE:UPDATE";
    public static final String SYSTEM_NOTICE_DELETE = "SYSTEM_NOTICE:DELETE";
    /*------ end: SYSTEM_NOTICE ------*/

    /*------ start: SYS_DEPARTMENT ------*/
    public static final String SYS_ORGANIZATION_READ = "SYS_ORGANIZATION:READ";
    public static final String SYS_ORGANIZATION_ADD = "SYS_ORGANIZATION:ADD";
    public static final String SYS_ORGANIZATION_UPDATE = "SYS_ORGANIZATION:UPDATE";
    public static final String SYS_ORGANIZATION_DELETE = "SYS_ORGANIZATION:DELETE";
    public static final String SYS_ORGANIZATION_IMPORT = "SYS_ORGANIZATION:IMPORT";
    public static final String SYS_ORGANIZATION_SYNC = "SYS_ORGANIZATION:SYNC";
    public static final String SYS_ORGANIZATION_USER_RESET_PASSWORD = "SYS_ORGANIZATION_USER:RESET_PASSWORD";

    /*------ end: SYS_DEPARTMENT ------*/

    /*------ start: SYSTEM_SETTING ------*/
    public static final String SYSTEM_SETTING_READ = "SYSTEM_SETTING:READ";
    public static final String SYSTEM_SETTING_UPDATE = "SYSTEM_SETTING:UPDATE";
    public static final String SYSTEM_SETTING_ADD = "SYSTEM_SETTING:ADD";
    public static final String SYSTEM_SETTING_DELETE = "SYSTEM_SETTING:DELETE";
    /*------ end: SYSTEM_SETTING ------*/

    /**
     * module setting permission
     */
    public static final String MODULE_SETTING_READ = "MODULE_SETTING:READ";
    public static final String MODULE_SETTING_UPDATE = "MODULE_SETTING:UPDATE";


    /*------ start: CUSTOMER_MANAGEMENT------*/
    public static final String CUSTOMER_MANAGEMENT_READ = "CUSTOMER_MANAGEMENT:READ";
    public static final String CUSTOMER_MANAGEMENT_ADD = "CUSTOMER_MANAGEMENT:ADD";
    public static final String CUSTOMER_MANAGEMENT_UPDATE = "CUSTOMER_MANAGEMENT:UPDATE";
    public static final String CUSTOMER_MANAGEMENT_TRANSFER = "CUSTOMER_MANAGEMENT:TRANSFER";
    public static final String CUSTOMER_MANAGEMENT_RECYCLE = "CUSTOMER_MANAGEMENT:RECYCLE";
    public static final String CUSTOMER_MANAGEMENT_DELETE = "CUSTOMER_MANAGEMENT:DELETE";
    public static final String CUSTOMER_MANAGEMENT_EXPORT = "CUSTOMER_MANAGEMENT:EXPORT";
    public static final String CUSTOMER_MANAGEMENT_IMPORT = "CUSTOMER_MANAGEMENT:IMPORT";
    public static final String CUSTOMER_MANAGEMENT_MERGE = "CUSTOMER_MANAGEMENT:MERGE";
    /*------ end: CUSTOMER_MANAGEMENT ------*/


    /*------ start: CUSTOMER_MANAGEMENT_POOL ------*/
    public static final String CUSTOMER_MANAGEMENT_POOL_READ = "CUSTOMER_MANAGEMENT_POOL:READ";
    public static final String CUSTOMER_MANAGEMENT_POOL_UPDATE = "CUSTOMER_MANAGEMENT_POOL:UPDATE";
    public static final String CUSTOMER_MANAGEMENT_POOL_DELETE = "CUSTOMER_MANAGEMENT_POOL:DELETE";
    public static final String CUSTOMER_MANAGEMENT_POOL_PICK = "CUSTOMER_MANAGEMENT_POOL:PICK";
    public static final String CUSTOMER_MANAGEMENT_POOL_ASSIGN = "CUSTOMER_MANAGEMENT_POOL:ASSIGN";
    public static final String CUSTOMER_MANAGEMENT_POOL_EXPORT = "CUSTOMER_MANAGEMENT_POOL:EXPORT";
    public static final String CUSTOMER_MANAGEMENT_POOL_IMPORT = "CUSTOMER_MANAGEMENT_POOL:IMPORT";
    /*------ end: CUSTOMER_MANAGEMENT_POOL ------*/


    /*------ start: CUSTOMER_MANAGEMENT_CONTACT ------*/
    public static final String CUSTOMER_MANAGEMENT_CONTACT_READ = "CUSTOMER_MANAGEMENT_CONTACT:READ";
    public static final String CUSTOMER_MANAGEMENT_CONTACT_ADD = "CUSTOMER_MANAGEMENT_CONTACT:ADD";
    public static final String CUSTOMER_MANAGEMENT_CONTACT_UPDATE = "CUSTOMER_MANAGEMENT_CONTACT:UPDATE";
    public static final String CUSTOMER_MANAGEMENT_CONTACT_DELETE = "CUSTOMER_MANAGEMENT_CONTACT:DELETE";
    public static final String CUSTOMER_MANAGEMENT_CONTACT_EXPORT = "CUSTOMER_MANAGEMENT_CONTACT:EXPORT";
    public static final String CUSTOMER_MANAGEMENT_CONTACT_IMPORT = "CUSTOMER_MANAGEMENT_CONTACT:IMPORT";
    /*------ end: CUSTOMER_MANAGEMENT_CONTACT ------*/

    /*------ start: PRODUCT_MANAGEMENT ------*/
    public static final String PRODUCT_MANAGEMENT_READ = "PRODUCT_MANAGEMENT:READ";
    public static final String PRODUCT_MANAGEMENT_ADD = "PRODUCT_MANAGEMENT:ADD";
    public static final String PRODUCT_MANAGEMENT_UPDATE = "PRODUCT_MANAGEMENT:UPDATE";
    public static final String PRODUCT_MANAGEMENT_DELETE = "PRODUCT_MANAGEMENT:DELETE";
    public static final String PRODUCT_MANAGEMENT_IMPORT = "PRODUCT_MANAGEMENT:IMPORT";
    public static final String PRODUCT_MANAGEMENT_EXPORT = "PRODUCT_MANAGEMENT:EXPORT";
    /*------ end: PRODUCT_MANAGEMENT ------*/

    /*------ start: OPPORTUNITY_MANAGEMENT ------*/
    public static final String OPPORTUNITY_MANAGEMENT_READ = "OPPORTUNITY_MANAGEMENT:READ";
    public static final String OPPORTUNITY_MANAGEMENT_ADD = "OPPORTUNITY_MANAGEMENT:ADD";
    public static final String OPPORTUNITY_MANAGEMENT_UPDATE = "OPPORTUNITY_MANAGEMENT:UPDATE";
    public static final String OPPORTUNITY_MANAGEMENT_TRANSFER = "OPPORTUNITY_MANAGEMENT:TRANSFER";
    public static final String OPPORTUNITY_MANAGEMENT_DELETE = "OPPORTUNITY_MANAGEMENT:DELETE";
    public static final String OPPORTUNITY_MANAGEMENT_EXPORT = "OPPORTUNITY_MANAGEMENT:EXPORT";
    public static final String OPPORTUNITY_MANAGEMENT_RESIGN = "OPPORTUNITY_MANAGEMENT:RESIGN";
    public static final String OPPORTUNITY_MANAGEMENT_IMPORT = "OPPORTUNITY_MANAGEMENT:IMPORT";
    /*------ end: OPPORTUNITY_MANAGEMENT ------*/


    /**
     * clue permission
     */
    /*------ start: CLUE_MANAGEMENT ------*/
    public static final String CLUE_MANAGEMENT_READ = "CLUE_MANAGEMENT:READ";
    public static final String CLUE_MANAGEMENT_ADD = "CLUE_MANAGEMENT:ADD";
    public static final String CLUE_MANAGEMENT_UPDATE = "CLUE_MANAGEMENT:UPDATE";
    public static final String CLUE_MANAGEMENT_TRANSFER = "CLUE_MANAGEMENT:TRANSFER";
    public static final String CLUE_MANAGEMENT_RECYCLE = "CLUE_MANAGEMENT:RECYCLE";
    public static final String CLUE_MANAGEMENT_DELETE = "CLUE_MANAGEMENT:DELETE";
    public static final String CLUE_MANAGEMENT_EXPORT = "CLUE_MANAGEMENT:EXPORT";
    public static final String CLUE_MANAGEMENT_IMPORT = "CLUE_MANAGEMENT:IMPORT";
    /*------ end: CLUE_MANAGEMENT ------*/


    /*------ start: CLUE_MANAGEMENT_POOL ------*/
    public static final String CLUE_MANAGEMENT_POOL_READ = "CLUE_MANAGEMENT_POOL:READ";
    public static final String CLUE_MANAGEMENT_POOL_DELETE = "CLUE_MANAGEMENT_POOL:DELETE";
    public static final String CLUE_MANAGEMENT_POOL_PICK = "CLUE_MANAGEMENT_POOL:PICK";
    public static final String CLUE_MANAGEMENT_POOL_ASSIGN = "CLUE_MANAGEMENT_POOL:ASSIGN";
    public static final String CLUE_MANAGEMENT_POOL_UPDATE = "CLUE_MANAGEMENT_POOL:UPDATE";
    public static final String CLUE_MANAGEMENT_POOL_IMPORT = "CLUE_MANAGEMENT_POOL:IMPORT";
    public static final String CLUE_MANAGEMENT_POOL_EXPORT = "CLUE_MANAGEMENT_POOL:EXPORT";
    /*------ end: CLUE_MANAGEMENT_POOL ------*/

    /**
     * dashboard permission
     */
    public static final String DASHBOARD_READ = "DASHBOARD:READ";
    public static final String DASHBOARD_ADD = "DASHBOARD:ADD";
    public static final String DASHBOARD_EDIT = "DASHBOARD:UPDATE";
    public static final String DASHBOARD_DELETE = "DASHBOARD:DELETE";

    /*------ start: LICENSE ------*/
    public static final String LICENSE_READ = "LICENSE:READ";
    public static final String LICENSE_EDIT = "LICENSE:EDIT";
    /*------ end: LICENSE ------*/


    /*------ start: PERSON INFO ------*/
    public static final String PERSONAL_API_KEY_READ = "PERSONAL_API_KEY:READ";
    public static final String PERSONAL_API_KEY_ADD = "PERSONAL_API_KEY:ADD";
    public static final String PERSONAL_API_KEY_UPDATE = "PERSONAL_API_KEY:UPDATE";
    public static final String PERSONAL_API_KEY_DELETE = "PERSONAL_API_KEY:DELETE";
    /*------ end: PERSON INFO ------*/


    /*------ start: AGENT ------*/
    public static final String AGENT_READ = "AGENT:READ";
    public static final String AGENT_ADD = "AGENT:ADD";
    public static final String AGENT_UPDATE = "AGENT:UPDATE";
    public static final String AGENT_DELETE = "AGENT:DELETE";
    /*------ end: AGENT ------*/

    /**
     * product price permission
     */
    public static final String PRICE_READ = "PRICE:READ";
    public static final String PRICE_ADD = "PRICE:ADD";
    public static final String PRICE_UPDATE = "PRICE:UPDATE";
    public static final String PRICE_DELETE = "PRICE:DELETE";
    public static final String PRICE_IMPORT = "PRICE:IMPORT";
    public static final String PRICE_EXPORT = "PRICE:EXPORT";


    /*------ start: OPPORTUNITY_QUOTATION ------*/

    public static final String OPPORTUNITY_QUOTATION_READ = "OPPORTUNITY_QUOTATION:READ";
    public static final String OPPORTUNITY_QUOTATION_ADD = "OPPORTUNITY_QUOTATION:ADD";
    public static final String OPPORTUNITY_QUOTATION_UPDATE = "OPPORTUNITY_QUOTATION:UPDATE";
    public static final String OPPORTUNITY_QUOTATION_DELETE = "OPPORTUNITY_QUOTATION:DELETE";
    public static final String OPPORTUNITY_QUOTATION_DOWNLOAD = "OPPORTUNITY_QUOTATION:DOWNLOAD";
    public static final String OPPORTUNITY_QUOTATION_VOIDED = "OPPORTUNITY_QUOTATION:VOIDED";
    public static final String OPPORTUNITY_QUOTATION_APPROVAL = "OPPORTUNITY_QUOTATION:APPROVAL";

    /*------ end: OPPORTUNITY_QUOTATION ------*/

    /*------ start: CONTRACT ------*/
    public static final String CONTRACT_READ = "CONTRACT:READ";
    public static final String CONTRACT_ADD = "CONTRACT:ADD";
    public static final String CONTRACT_UPDATE = "CONTRACT:UPDATE";
    public static final String CONTRACT_DELETE = "CONTRACT:DELETE";
    public static final String CONTRACT_EXPORT = "CONTRACT:EXPORT";
    public static final String CONTRACT_APPROVAL = "CONTRACT:APPROVAL";
    public static final String CONTRACT_STAGE = "CONTRACT:STAGE";
    public static final String CONTRACT_PAYMENT = "CONTRACT:PAYMENT";

    /*------ end: CONTRACT ------*/

    /*------ start: CONTRACT_CONTRACT_PAYMENT_PLAN_ROLE ------*/
    public static final String CONTRACT_PAYMENT_PLAN_READ = "CONTRACT_PAYMENT_PLAN:READ";
    public static final String CONTRACT_PAYMENT_PLAN_ADD = "CONTRACT_PAYMENT_PLAN:ADD";
    public static final String CONTRACT_PAYMENT_PLAN_UPDATE = "CONTRACT_PAYMENT_PLAN:UPDATE";
    public static final String CONTRACT_PAYMENT_PLAN_DELETE = "CONTRACT_PAYMENT_PLAN:DELETE";
    public static final String CONTRACT_PAYMENT_PLAN_IMPORT = "CONTRACT_PAYMENT_PLAN:IMPORT";
    public static final String CONTRACT_PAYMENT_PLAN_EXPORT = "CONTRACT_PAYMENT_PLAN:EXPORT";
    /*------ end: CONTRACT_CONTRACT_PAYMENT_PLAN_ROLE ------*/


    /*------ start: TENDER ------*/
    public static final String TENDER_READ = "TENDER:READ";
    /*------ end: TENDER ------*/


    /*------ start: CONTRACT_INVOICE_ROLE ------*/
    public static final String CONTRACT_INVOICE_READ = "CONTRACT_INVOICE:READ";
    public static final String CONTRACT_INVOICE_ADD = "CONTRACT_INVOICE:ADD";
    public static final String CONTRACT_INVOICE_UPDATE = "CONTRACT_INVOICE:UPDATE";
    public static final String CONTRACT_INVOICE_IMPORT = "CONTRACT_INVOICE:IMPORT";
    public static final String CONTRACT_INVOICE_EXPORT = "CONTRACT_INVOICE:EXPORT";
    public static final String CONTRACT_INVOICE_APPROVAL = "CONTRACT_INVOICE:APPROVAL";
    public static final String CONTRACT_INVOICE_DELETE = "CONTRACT_INVOICE:DELETE";
    /*------ end: CONTRACT_INVOICE_ROLE ------*/

    /*------ start: BUSINESS_TITLE ------*/
    public static final String CONTRACT_BUSINESS_TITLE_READ = "CONTRACT_BUSINESS_TITLE:READ";
    public static final String CONTRACT_BUSINESS_TITLE_ADD = "CONTRACT_BUSINESS_TITLE:ADD";
    public static final String CONTRACT_BUSINESS_TITLE_UPDATE = "CONTRACT_BUSINESS_TITLE:UPDATE";
    public static final String CONTRACT_BUSINESS_TITLE_DELETE = "CONTRACT_BUSINESS_TITLE:DELETE";
    public static final String CONTRACT_BUSINESS_TITLE_EXPORT = "CONTRACT_BUSINESS_TITLE:EXPORT";
    public static final String CONTRACT_BUSINESS_TITLE_APPROVAL = "CONTRACT_BUSINESS_TITLE:APPROVAL";
    public static final String CONTRACT_BUSINESS_TITLE_IMPORT = "CONTRACT_BUSINESS_TITLE:IMPORT";

    /*------ end: BUSINESS_TITLE ------*/

	/**
	 * Contract payment record permission
	 */
	public static final String CONTRACT_PAYMENT_RECORD_READ = "CONTRACT_PAYMENT_RECORD:READ";
	public static final String CONTRACT_PAYMENT_RECORD_ADD = "CONTRACT_PAYMENT_RECORD:ADD";
	public static final String CONTRACT_PAYMENT_RECORD_UPDATE = "CONTRACT_PAYMENT_RECORD:UPDATE";
	public static final String CONTRACT_PAYMENT_RECORD_DELETE = "CONTRACT_PAYMENT_RECORD:DELETE";
	public static final String CONTRACT_PAYMENT_RECORD_IMPORT = "CONTRACT_PAYMENT_RECORD:IMPORT";
	public static final String CONTRACT_PAYMENT_RECORD_EXPORT = "CONTRACT_PAYMENT_RECORD:EXPORT";

    /*------ start: ORDER_ROLE ------*/
    public static final String ORDER_READ = "ORDER:READ";
    public static final String ORDER_ADD = "ORDER:ADD";
    public static final String ORDER_UPDATE = "ORDER:UPDATE";
    public static final String ORDER_DELETE = "ORDER:DELETE";
    public static final String ORDER_DOWNLOAD = "ORDER:DOWNLOAD";
    /*------ end: ORDER_ROLE ------*/

    /*------ start: PROCESS_SETTING ------*/
    public static final String PROCESS_SETTING_READ = "PROCESS_SETTING:READ";
    public static final String PROCESS_SETTING_ADD = "PROCESS_SETTING:ADD";
    public static final String PROCESS_SETTING_UPDATE = "PROCESS_SETTING:UPDATE";
    public static final String PROCESS_SETTING_DELETE = "PROCESS_SETTING:DELETE";
    /*------ end: PROCESS_SETTING ------*/

    /*------ start: CUSTOM_FORM ------*/
    public static final String CUSTOM_FORM_READ = "CUSTOM_FORM:READ";
    public static final String CUSTOM_FORM_ADD = "CUSTOM_FORM:ADD";
    /*------ end: CUSTOM_FORM ------*/


    /*====================================================================
     * 广告下单系统 V3.1 权限码（AD_*）
     * 命名规范：MODULE:ACTION；值须与播种 SQL（V3.0.0_2__ad_permissions.sql）
     * 及前端 advertising.ts 的 meta.permissions 完全一致（§A.1 / §C.5 红线）。
     * 权威码：AD_ORDER_CHANGE:* 与 AD_SEAL:*；
     * 旧码 AD_ORDER:CHANGE / AD_CONTRACT:SEAL_* 已废弃（仅种子历史残留，不在此定义）。
     *====================================================================*/

    /*------ start: AD_ORDER（§3.3 / §6 / §9.1） ------*/
    public static final String AD_ORDER_READ = "AD_ORDER:READ";
    public static final String AD_ORDER_CREATE = "AD_ORDER:CREATE";
    public static final String AD_ORDER_SUBMIT = "AD_ORDER:SUBMIT";
    public static final String AD_ORDER_APPROVE = "AD_ORDER:APPROVE";
    public static final String AD_ORDER_REJECT = "AD_ORDER:REJECT";
    public static final String AD_ORDER_CONFIRM_EXECUTE = "AD_ORDER:CONFIRM_EXECUTE";
    public static final String AD_ORDER_ARCHIVE = "AD_ORDER:ARCHIVE";
    public static final String AD_ORDER_FORCE_ARCHIVE = "AD_ORDER:FORCE_ARCHIVE";
    public static final String AD_ORDER_VOID = "AD_ORDER:VOID";
    public static final String AD_ORDER_EXPORT = "AD_ORDER:EXPORT";
    /*------ end: AD_ORDER ------*/

    /*------ start: AD_ORDER_CHANGE（§3.3 / §6.2 / §9.1，替代旧 AD_ORDER:CHANGE） ------*/
    public static final String AD_ORDER_CHANGE_READ = "AD_ORDER_CHANGE:READ";
    public static final String AD_ORDER_CHANGE_CREATE = "AD_ORDER_CHANGE:CREATE";
    public static final String AD_ORDER_CHANGE_APPROVE = "AD_ORDER_CHANGE:APPROVE";
    public static final String AD_ORDER_CHANGE_REJECT = "AD_ORDER_CHANGE:REJECT";
    /*------ end: AD_ORDER_CHANGE ------*/

    /*------ start: AD_RECEIPT（收款单） ------*/
    public static final String AD_RECEIPT_READ = "AD_RECEIPT:READ";
    public static final String AD_RECEIPT_CREATE = "AD_RECEIPT:CREATE";
    public static final String AD_RECEIPT_UPDATE = "AD_RECEIPT:UPDATE";
    public static final String AD_RECEIPT_DELETE = "AD_RECEIPT:DELETE";
    public static final String AD_RECEIPT_APPROVE = "AD_RECEIPT:APPROVE";
    /*------ end: AD_RECEIPT ------*/

    /*------ start: AD_PAYOUT（付款单） ------*/
    public static final String AD_PAYOUT_READ = "AD_PAYOUT:READ";
    public static final String AD_PAYOUT_CREATE = "AD_PAYOUT:CREATE";
    public static final String AD_PAYOUT_UPDATE = "AD_PAYOUT:UPDATE";
    public static final String AD_PAYOUT_DELETE = "AD_PAYOUT:DELETE";
    public static final String AD_PAYOUT_APPROVE = "AD_PAYOUT:APPROVE";
    /*------ end: AD_PAYOUT ------*/

    /*------ start: AD_CONTRACT（§3.3 / §9.3） ------*/
    public static final String AD_CONTRACT_READ = "AD_CONTRACT:READ";
    public static final String AD_CONTRACT_CREATE = "AD_CONTRACT:CREATE";
    public static final String AD_CONTRACT_UPDATE = "AD_CONTRACT:UPDATE";
    public static final String AD_CONTRACT_DELETE = "AD_CONTRACT:DELETE";
    public static final String AD_CONTRACT_ARCHIVE_APPROVE = "AD_CONTRACT:ARCHIVE_APPROVE";
    /*------ end: AD_CONTRACT ------*/

    /*------ start: AD_SEAL（§3.3 / §9.3，替代旧 AD_CONTRACT:SEAL_*） ------*/
    public static final String AD_SEAL_READ = "AD_SEAL:READ";
    public static final String AD_SEAL_APPLY = "AD_SEAL:APPLY";
    public static final String AD_SEAL_APPROVE = "AD_SEAL:APPROVE";
    public static final String AD_SEAL_REJECT = "AD_SEAL:REJECT";
    public static final String AD_SEAL_UPLOAD = "AD_SEAL:UPLOAD";
    /*------ end: AD_SEAL ------*/

    /*------ start: AD_UPSTREAM_AGENT（§8.4 / §9.4） ------*/
    public static final String AD_UPSTREAM_AGENT_READ = "AD_UPSTREAM_AGENT:READ";
    public static final String AD_UPSTREAM_AGENT_CREATE = "AD_UPSTREAM_AGENT:CREATE";
    public static final String AD_UPSTREAM_AGENT_UPDATE = "AD_UPSTREAM_AGENT:UPDATE";
    public static final String AD_UPSTREAM_AGENT_DELETE = "AD_UPSTREAM_AGENT:DELETE";
    /*------ end: AD_UPSTREAM_AGENT ------*/

    /*------ start: AD_DOWNSTREAM_MEDIA（§8.4 / §9.4） ------*/
    public static final String AD_DOWNSTREAM_MEDIA_READ = "AD_DOWNSTREAM_MEDIA:READ";
    public static final String AD_DOWNSTREAM_MEDIA_CREATE = "AD_DOWNSTREAM_MEDIA:CREATE";
    public static final String AD_DOWNSTREAM_MEDIA_UPDATE = "AD_DOWNSTREAM_MEDIA:UPDATE";
    public static final String AD_DOWNSTREAM_MEDIA_DELETE = "AD_DOWNSTREAM_MEDIA:DELETE";
    /*------ end: AD_DOWNSTREAM_MEDIA ------*/

    /*------ start: AD_CUSTOMER（§8.4 / §9.4） ------*/
    public static final String AD_CUSTOMER_READ = "AD_CUSTOMER:READ";
    public static final String AD_CUSTOMER_CREATE = "AD_CUSTOMER:CREATE";
    public static final String AD_CUSTOMER_UPDATE = "AD_CUSTOMER:UPDATE";
    public static final String AD_CUSTOMER_DELETE = "AD_CUSTOMER:DELETE";
    /*------ end: AD_CUSTOMER ------*/

    /*------ start: AD_BUSINESS_ENTITY（§3.2 / §8.5 / §9.4） ------*/
    public static final String AD_BUSINESS_ENTITY_READ = "AD_BUSINESS_ENTITY:READ";
    public static final String AD_BUSINESS_ENTITY_CREATE = "AD_BUSINESS_ENTITY:CREATE";
    public static final String AD_BUSINESS_ENTITY_UPDATE = "AD_BUSINESS_ENTITY:UPDATE";
    public static final String AD_BUSINESS_ENTITY_DELETE = "AD_BUSINESS_ENTITY:DELETE";
    /*------ end: AD_BUSINESS_ENTITY ------*/

    /*------ start: AD_DICT（§4.3 / §8.5 / §9.4） ------*/
    public static final String AD_DICT_READ = "AD_DICT:READ";
    public static final String AD_DICT_CREATE = "AD_DICT:CREATE";
    public static final String AD_DICT_UPDATE = "AD_DICT:UPDATE";
    public static final String AD_DICT_DELETE = "AD_DICT:DELETE";
    /*------ end: AD_DICT ------*/

    /*------ start: AD_USER_BUSINESS_ENTITY（§3.2 / §9.4） ------*/
    public static final String AD_USER_BUSINESS_ENTITY_READ = "AD_USER_BUSINESS_ENTITY:READ";
    public static final String AD_USER_BUSINESS_ENTITY_CREATE = "AD_USER_BUSINESS_ENTITY:CREATE";
    public static final String AD_USER_BUSINESS_ENTITY_UPDATE = "AD_USER_BUSINESS_ENTITY:UPDATE";
    public static final String AD_USER_BUSINESS_ENTITY_DELETE = "AD_USER_BUSINESS_ENTITY:DELETE";
    /*------ end: AD_USER_BUSINESS_ENTITY ------*/

    /*------ start: AD_REPORT（§8.5 / §9.5） ------*/
    public static final String AD_REPORT_READ = "AD_REPORT:READ";
    /*------ end: AD_REPORT ------*/

    /*------ start: AD_WORKBENCH（§8.5） ------*/
    public static final String AD_WORKBENCH_READ = "AD_WORKBENCH:READ";
    /*------ end: AD_WORKBENCH ------*/

    /*------ start: AD_APPROVAL（§8.5） ------*/
    public static final String AD_APPROVAL_READ = "AD_APPROVAL:READ";
    public static final String AD_APPROVAL_APPROVE = "AD_APPROVAL:APPROVE";
    /*------ end: AD_APPROVAL ------*/

    /*------ start: AD_SYSTEM（§8.5） ------*/
    public static final String AD_SYSTEM_READ = "AD_SYSTEM:READ";
    public static final String AD_SYSTEM_CONFIG = "AD_SYSTEM:CONFIG";
    /*------ end: AD_SYSTEM ------*/
}

