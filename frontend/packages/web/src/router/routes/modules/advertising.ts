import { AdvertisingRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

/**
 * 工作台（独立父模块，无子标签）
 */
const workbench: AppRouteRecordRaw = {
  path: '/advertising/workbench',
  name: AdvertisingRouteEnum.ADVERTISING_WORKBENCH_GROUP,
  redirect: '/advertising/workbench',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'module.advertising.workbench',
    permissions: ['AD_WORKBENCH:READ'],
    icon: 'iconicon_home',
    hideChildrenInMenu: true,
    collapsedLocale: 'module.advertising.workbench',
  },
  children: [
    {
      path: '',
      name: AdvertisingRouteEnum.ADVERTISING_WORKBENCH,
      component: () => import('@/views/advertising/workbench/index.vue'),
      meta: {
        locale: 'module.advertising.workbench',
        permissions: ['AD_WORKBENCH:READ'],
      },
    },
  ],
};

/**
 * 订单管理（父模块）：下单 / 改单 / 收付款
 */
const orderManagement: AppRouteRecordRaw = {
  path: '/advertising/order-management',
  name: AdvertisingRouteEnum.ADVERTISING_ORDER_MANAGEMENT,
  redirect: '/advertising/order-management/order',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'module.advertising.orderManagement',
    permissions: ['AD_ORDER:READ', 'AD_ORDER_CHANGE:READ', 'AD_RECEIPT:READ', 'AD_PAYOUT:READ'],
    icon: 'iconicon_order_form',
    hideChildrenInMenu: true,
    collapsedLocale: 'module.advertising.orderManagement',
  },
  children: [
    {
      path: 'order',
      name: AdvertisingRouteEnum.ADVERTISING_ORDER,
      component: () => import('@/views/advertising/order/index.vue'),
      meta: {
        locale: 'module.advertising.order',
        isTopMenu: true,
        permissions: ['AD_ORDER:READ'],
      },
    },
    {
      path: 'order/create',
      name: AdvertisingRouteEnum.ADVERTISING_ORDER_CREATE,
      component: () => import('@/views/advertising/order/create.vue'),
      meta: {
        locale: 'module.advertising.order',
        permissions: ['AD_ORDER:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_ORDER,
      },
    },
    {
      path: 'order/edit/:id',
      name: AdvertisingRouteEnum.ADVERTISING_ORDER_EDIT,
      component: () => import('@/views/advertising/order/create.vue'),
      meta: {
        locale: 'module.advertising.order',
        permissions: ['AD_ORDER:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_ORDER,
      },
    },
    {
      path: 'order/detail/:id',
      name: AdvertisingRouteEnum.ADVERTISING_ORDER_DETAIL,
      component: () => import('@/views/advertising/order/detail.vue'),
      meta: {
        locale: 'module.advertising.order',
        permissions: ['AD_ORDER:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_ORDER,
      },
    },
    {
      path: 'change',
      name: AdvertisingRouteEnum.ADVERTISING_CHANGE,
      component: () => import('@/views/advertising/change/index.vue'),
      meta: {
        locale: 'module.advertising.change',
        isTopMenu: true,
        permissions: ['AD_ORDER_CHANGE:READ'],
      },
    },
    {
      path: 'change/create',
      name: AdvertisingRouteEnum.ADVERTISING_CHANGE_CREATE,
      component: () => import('@/views/advertising/change/create.vue'),
      meta: {
        locale: 'module.advertising.change',
        permissions: ['AD_ORDER_CHANGE:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_CHANGE,
      },
    },
    {
      path: 'change/detail/:id',
      name: AdvertisingRouteEnum.ADVERTISING_CHANGE_DETAIL,
      component: () => import('@/views/advertising/change/detail.vue'),
      meta: {
        locale: 'module.advertising.change',
        permissions: ['AD_ORDER_CHANGE:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_CHANGE,
      },
    },
    {
      path: 'receipt',
      name: AdvertisingRouteEnum.ADVERTISING_RECEIPT,
      component: () => import('@/views/advertising/receipt/index.vue'),
      meta: {
        locale: 'module.advertising.receipt',
        isTopMenu: true,
        permissions: ['AD_RECEIPT:READ'],
      },
    },
    {
      path: 'payout',
      name: AdvertisingRouteEnum.ADVERTISING_PAYOUT,
      component: () => import('@/views/advertising/payout/index.vue'),
      meta: {
        locale: 'module.advertising.payout',
        isTopMenu: true,
        permissions: ['AD_PAYOUT:READ'],
      },
    },
  ],
};

/**
 * 合同管理（父模块）：合同 / 用印
 */
const contractManagement: AppRouteRecordRaw = {
  path: '/advertising/contract-management',
  name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_MANAGEMENT,
  redirect: '/advertising/contract-management/contract',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'module.advertising.contractManagement',
    permissions: ['AD_CONTRACT:READ', 'AD_SEAL:READ'],
    icon: 'iconicon_contract',
    hideChildrenInMenu: true,
    collapsedLocale: 'module.advertising.contractManagement',
  },
  children: [
    {
      path: 'contract',
      name: AdvertisingRouteEnum.ADVERTISING_CONTRACT,
      component: () => import('@/views/advertising/contract/index.vue'),
      meta: {
        locale: 'module.advertising.contract',
        isTopMenu: true,
        permissions: ['AD_CONTRACT:READ'],
      },
    },
    {
      path: 'contract/create',
      name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_CREATE,
      component: () => import('@/views/advertising/contract/create.vue'),
      meta: {
        locale: 'module.advertising.contract',
        permissions: ['AD_CONTRACT:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_CONTRACT,
      },
    },
    {
      path: 'contract/edit/:id',
      name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_EDIT,
      component: () => import('@/views/advertising/contract/create.vue'),
      meta: {
        locale: 'module.advertising.contract',
        permissions: ['AD_CONTRACT:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_CONTRACT,
      },
    },
    {
      path: 'contract/detail/:id',
      name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_DETAIL,
      component: () => import('@/views/advertising/contract/detail.vue'),
      meta: {
        locale: 'module.advertising.contract',
        permissions: ['AD_CONTRACT:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_CONTRACT,
      },
    },
    {
      path: 'seal',
      name: AdvertisingRouteEnum.ADVERTISING_SEAL,
      component: () => import('@/views/advertising/seal/index.vue'),
      meta: {
        locale: 'module.advertising.seal',
        isTopMenu: true,
        permissions: ['AD_SEAL:READ'],
      },
    },
    {
      path: 'seal/apply',
      name: AdvertisingRouteEnum.ADVERTISING_SEAL_APPLY,
      component: () => import('@/views/advertising/seal/apply.vue'),
      meta: {
        locale: 'module.advertising.seal',
        permissions: ['AD_SEAL:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_SEAL,
      },
    },
    {
      path: 'seal/approve/:id',
      name: AdvertisingRouteEnum.ADVERTISING_SEAL_APPROVE,
      component: () => import('@/views/advertising/seal/approve.vue'),
      meta: {
        locale: 'module.advertising.seal',
        permissions: ['AD_SEAL:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_SEAL,
      },
    },
    {
      path: 'seal/detail/:id',
      name: AdvertisingRouteEnum.ADVERTISING_SEAL_DETAIL,
      component: () => import('@/views/advertising/seal/detail.vue'),
      meta: {
        locale: 'module.advertising.seal',
        permissions: ['AD_SEAL:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_SEAL,
      },
    },
  ],
};

/**
 * 资源管理（父模块）：上游代理 / 下游媒体 / 客户信息 / 业务主体
 */
const resourceManagement: AppRouteRecordRaw = {
  path: '/advertising/resource-management',
  name: AdvertisingRouteEnum.ADVERTISING_RESOURCE_MANAGEMENT,
  redirect: '/advertising/resource-management/upstream-agent',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'module.advertising.resourceManagement',
    permissions: ['AD_UPSTREAM_AGENT:READ', 'AD_DOWNSTREAM_MEDIA:READ', 'AD_CUSTOMER:READ', 'AD_BUSINESS_ENTITY:READ'],
    icon: 'iconicon_enterprise',
    hideChildrenInMenu: true,
    collapsedLocale: 'module.advertising.resourceManagement',
  },
  children: [
    {
      path: 'upstream-agent',
      name: AdvertisingRouteEnum.ADVERTISING_UPSTREAM_AGENT,
      component: () => import('@/views/advertising/upstreamAgent/index.vue'),
      meta: {
        locale: 'module.advertising.upstreamAgent',
        isTopMenu: true,
        permissions: ['AD_UPSTREAM_AGENT:READ'],
      },
    },
    {
      path: 'upstream-agent/detail/:id',
      name: AdvertisingRouteEnum.ADVERTISING_UPSTREAM_AGENT_DETAIL,

      component: () => import('@/views/advertising/upstreamAgent/detail.vue'),
      meta: {
        locale: 'module.advertising.upstreamAgent',
        permissions: ['AD_UPSTREAM_AGENT:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_UPSTREAM_AGENT,
      },
    },
    {
      path: 'downstream-media',
      name: AdvertisingRouteEnum.ADVERTISING_DOWNSTREAM_MEDIA,
      component: () => import('@/views/advertising/downstreamMedia/index.vue'),
      meta: {
        locale: 'module.advertising.downstreamMedia',
        isTopMenu: true,
        permissions: ['AD_DOWNSTREAM_MEDIA:READ'],
      },
    },
    {
      path: 'downstream-media/detail/:id',
      name: AdvertisingRouteEnum.ADVERTISING_DOWNSTREAM_MEDIA_DETAIL,

      component: () => import('@/views/advertising/downstreamMedia/detail.vue'),
      meta: {
        locale: 'module.advertising.downstreamMedia',
        permissions: ['AD_DOWNSTREAM_MEDIA:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_DOWNSTREAM_MEDIA,
      },
    },
    {
      path: 'customer',
      name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER,
      component: () => import('@/views/advertising/customer/index.vue'),
      meta: {
        locale: 'module.advertising.customer',
        isTopMenu: true,
        permissions: ['AD_CUSTOMER:READ'],
      },
    },
    {
      path: 'customer/create',
      name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER_CREATE,
      component: () => import('@/views/advertising/customer/create.vue'),
      meta: {
        locale: 'module.advertising.customer',
        permissions: ['AD_CUSTOMER:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_CUSTOMER,
      },
    },
    {
      path: 'customer/edit/:id',
      name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER_EDIT,
      component: () => import('@/views/advertising/customer/create.vue'),
      meta: {
        locale: 'module.advertising.customer',
        permissions: ['AD_CUSTOMER:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_CUSTOMER,
      },
    },
    {
      path: 'customer/detail/:id',
      name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER_DETAIL,
      component: () => import('@/views/advertising/customer/detail.vue'),
      meta: {
        locale: 'module.advertising.customer',
        permissions: ['AD_CUSTOMER:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_CUSTOMER,
      },
    },
    {
      path: 'business-entity',
      name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY,
      component: () => import('@/views/advertising/businessEntity/index.vue'),
      meta: {
        locale: 'module.advertising.businessEntity',
        isTopMenu: true,
        permissions: ['AD_BUSINESS_ENTITY:READ'],
      },
    },
    {
      path: 'business-entity/create',
      name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY_CREATE,
      component: () => import('@/views/advertising/businessEntity/create.vue'),
      meta: {
        locale: 'module.advertising.businessEntity',
        permissions: ['AD_BUSINESS_ENTITY:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY,
      },
    },
    {
      path: 'business-entity/edit/:id',
      name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY_EDIT,
      component: () => import('@/views/advertising/businessEntity/create.vue'),
      meta: {
        locale: 'module.advertising.businessEntity',
        permissions: ['AD_BUSINESS_ENTITY:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY,
      },
    },
    {
      path: 'business-entity/detail/:id',
      name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY_DETAIL,
      component: () => import('@/views/advertising/businessEntity/detail.vue'),
      meta: {
        locale: 'module.advertising.businessEntity',
        permissions: ['AD_BUSINESS_ENTITY:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY,
      },
    },
  ],
};

/**
 * 审批中心（独立父模块，无子标签）
 */
const approval: AppRouteRecordRaw = {
  path: '/advertising/approval',
  name: AdvertisingRouteEnum.ADVERTISING_APPROVAL_GROUP,
  redirect: '/advertising/approval',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'module.advertising.approval',
    permissions: ['AD_APPROVAL:READ'],
    icon: 'iconicon_check',
    hideChildrenInMenu: true,
    collapsedLocale: 'module.advertising.approval',
  },
  children: [
    {
      path: '',
      name: AdvertisingRouteEnum.ADVERTISING_APPROVAL,
      component: () => import('@/views/advertising/approval/index.vue'),
      meta: {
        locale: 'module.advertising.approval',
        permissions: ['AD_APPROVAL:READ'],
      },
    },
  ],
};

/**
 * 报表中心（独立父模块，无子标签）
 */
const report: AppRouteRecordRaw = {
  path: '/advertising/report',
  name: AdvertisingRouteEnum.ADVERTISING_REPORT_GROUP,
  redirect: '/advertising/report',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'module.advertising.report',
    permissions: ['AD_REPORT:READ'],
    icon: 'iconicon_data',
    hideChildrenInMenu: true,
    collapsedLocale: 'module.advertising.report',
  },
  children: [
    {
      path: '',
      name: AdvertisingRouteEnum.ADVERTISING_REPORT,
      component: () => import('@/views/advertising/report/index.vue'),
      meta: {
        locale: 'module.advertising.report',
        permissions: ['AD_REPORT:READ'],
      },
    },
  ],
};

/**
 * 系统配置（独立父模块，无子标签）
 */
const system: AppRouteRecordRaw = {
  path: '/advertising/system',
  name: AdvertisingRouteEnum.ADVERTISING_SYSTEM_GROUP,
  redirect: '/advertising/system',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'module.advertising.system',
    permissions: ['AD_SYSTEM:READ', 'AD_DICT:READ', 'AD_SYSTEM:CONFIG'],
    icon: 'iconicon_set_up',
    hideChildrenInMenu: true,
    collapsedLocale: 'module.advertising.system',
  },
  children: [
    {
      path: '',
      name: AdvertisingRouteEnum.ADVERTISING_SYSTEM,
      component: () => import('@/views/advertising/system/index.vue'),
      meta: {
        locale: 'module.advertising.system',
        permissions: ['AD_SYSTEM:READ'],
      },
    },
    {
      path: 'business-entity',
      name: AdvertisingRouteEnum.ADVERTISING_SYSTEM_BE,
      component: () => import('@/views/advertising/system/business-entity/index.vue'),
      meta: {
        locale: 'module.advertising.businessEntity',
        permissions: ['AD_BUSINESS_ENTITY:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_SYSTEM,
      },
    },
    {
      path: 'dict',
      name: AdvertisingRouteEnum.ADVERTISING_SYSTEM_DICT,
      component: () => import('@/views/advertising/system/dict/index.vue'),
      meta: {
        locale: 'module.advertising.dict',
        permissions: ['AD_DICT:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_SYSTEM,
      },
    },
    {
      path: 'settings',
      name: AdvertisingRouteEnum.ADVERTISING_SYSTEM_SETTINGS,
      component: () => import('@/views/advertising/system/settings.vue'),
      meta: {
        locale: 'module.advertising.systemSettings',
        permissions: ['AD_SYSTEM:CONFIG'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_SYSTEM,
      },
    },
  ],
};

/**
 * 操作日志（独立父模块，无子标签）
 */
const adLog: AppRouteRecordRaw = {
  path: '/advertising/log',
  name: AdvertisingRouteEnum.ADVERTISING_LOG_GROUP,
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'advertising.log.title',
    permissions: ['AD_OPERATION_LOG:READ'],
    icon: 'iconicon_history',
    hideChildrenInMenu: true,
    collapsedLocale: 'advertising.log.title',
  },
  children: [
    {
      path: '',
      name: AdvertisingRouteEnum.ADVERTISING_LOG,
      component: () => import('@/views/advertising/log/index.vue'),
      meta: {
        locale: 'advertising.log.title',
        permissions: ['AD_OPERATION_LOG:READ'],
      },
    },
  ],
};

const advertising: AppRouteRecordRaw[] = [
  workbench,
  orderManagement,
  contractManagement,
  resourceManagement,
  approval,
  report,
  system,
  adLog,
];

export default advertising;
