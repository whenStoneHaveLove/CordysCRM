import { AdvertisingRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const advertising: AppRouteRecordRaw = {
  path: '/advertising',
  name: AdvertisingRouteEnum.AD_WORKBENCH,
  redirect: '/advertising/workbench',
  component: DEFAULT_LAYOUT,
  meta: {
    permissions: ['AD_WORKBENCH:READ'],
    // 广告模块内 depth=1 页面（工作台 + 各业务列表页）统一显示底部 tabbar
    showTabbar: true,
  },
  children: [
    {
      path: 'workbench',
      name: AdvertisingRouteEnum.AD_WORKBENCH_INDEX,
      component: () => import('@/views/advertising/workbench/index.vue'),
      meta: {
        // 底栏/标题文案：广告工作台即本端首页，用「首页」
        locale: 'menu.workbench',
        depth: 1,
        isCache: true,
        permissions: ['AD_WORKBENCH:READ'],
      },
    },
    {
      path: 'order',
      name: AdvertisingRouteEnum.AD_ORDER_INDEX,
      component: () => import('@/views/advertising/order/index.vue'),
      meta: {
        locale: 'advertising.order',
        depth: 1,
        isCache: true,
        permissions: ['AD_ORDER:READ'],
      },
    },
    {
      path: 'order/detail',
      name: AdvertisingRouteEnum.AD_ORDER_DETAIL,
      component: () => import('@/views/advertising/order/detail.vue'),
      meta: {
        depth: 2,
        isCache: true,
        // 详情页允许「有审批/驳回权限」的用户从审批中心进入（对齐后端可操作口径）
        permissions: ['AD_ORDER:READ', 'AD_ORDER:APPROVE', 'AD_ORDER:REJECT'],
      },
    },
    {
      path: 'order/form',
      name: AdvertisingRouteEnum.AD_ORDER_FORM,
      component: () => import('@/views/advertising/order/form.vue'),
      meta: {
        depth: 2,
        isCache: false,
        permissions: ['AD_ORDER:READ'],
      },
    },
    {
      path: 'contract/form',
      name: AdvertisingRouteEnum.AD_CONTRACT_FORM,
      component: () => import('@/views/advertising/contract/form.vue'),
      meta: { depth: 2, isCache: false, permissions: ['AD_CONTRACT:READ'] },
    },
    {
      path: 'customer/form',
      name: AdvertisingRouteEnum.AD_CUSTOMER_FORM,
      component: () => import('@/views/advertising/customer/form.vue'),
      meta: { depth: 2, isCache: false, permissions: ['AD_CUSTOMER:READ'] },
    },
    {
      path: 'business-entity/form',
      name: AdvertisingRouteEnum.AD_BUSINESS_ENTITY_FORM,
      component: () => import('@/views/advertising/businessEntity/form.vue'),
      meta: { depth: 2, isCache: false, permissions: ['AD_BUSINESS_ENTITY:READ'] },
    },
    {
      path: 'receipt/form',
      name: AdvertisingRouteEnum.AD_RECEIPT_FORM,
      component: () => import('@/views/advertising/receipt/form.vue'),
      meta: { depth: 2, isCache: false, permissions: ['AD_RECEIPT:READ'] },
    },
    {
      path: 'payout/form',
      name: AdvertisingRouteEnum.AD_PAYOUT_FORM,
      component: () => import('@/views/advertising/payout/form.vue'),
      meta: { depth: 2, isCache: false, permissions: ['AD_PAYOUT:READ'] },
    },
    {
      path: 'downstream-media/form',
      name: AdvertisingRouteEnum.AD_DOWNSTREAM_MEDIA_FORM,
      component: () => import('@/views/advertising/downstreamMedia/form.vue'),
      meta: { depth: 2, isCache: false, permissions: ['AD_DOWNSTREAM_MEDIA:READ'] },
    },
    {
      path: 'upstream-agent/form',
      name: AdvertisingRouteEnum.AD_UPSTREAM_AGENT_FORM,
      component: () => import('@/views/advertising/upstreamAgent/form.vue'),
      meta: { depth: 2, isCache: false, permissions: ['AD_UPSTREAM_AGENT:READ'] },
    },
    {
      path: 'seal/apply',
      name: AdvertisingRouteEnum.AD_SEAL_APPLY,
      component: () => import('@/views/advertising/seal/apply.vue'),
      meta: { depth: 2, isCache: false, permissions: ['AD_SEAL:READ', 'AD_SEAL:APPLY'] },
    },
    {
      path: 'change/create',
      name: AdvertisingRouteEnum.AD_CHANGE_FORM,
      component: () => import('@/views/advertising/change/create.vue'),
      meta: { depth: 2, isCache: false, permissions: ['AD_ORDER_CHANGE:READ', 'AD_ORDER_CHANGE:CREATE'] },
    },
    {
      path: 'approval',
      name: AdvertisingRouteEnum.AD_APPROVAL_INDEX,
      component: () => import('@/views/advertising/approval/index.vue'),
      meta: {
        locale: 'advertising.approval',
        depth: 1,
        isCache: true,
        permissions: ['AD_APPROVAL:READ'],
      },
    },
    {
      path: 'contract',
      name: AdvertisingRouteEnum.AD_CONTRACT_INDEX,
      component: () => import('@/views/advertising/contract/index.vue'),
      meta: {
        locale: 'advertising.contract',
        depth: 1,
        isCache: true,
        permissions: ['AD_CONTRACT:READ'],
      },
    },
    {
      path: 'customer',
      name: AdvertisingRouteEnum.AD_CUSTOMER_INDEX,
      component: () => import('@/views/advertising/customer/index.vue'),
      meta: {
        locale: 'advertising.customer',
        depth: 1,
        isCache: true,
        permissions: ['AD_CUSTOMER:READ'],
      },
    },
    {
      path: 'receipt',
      name: AdvertisingRouteEnum.AD_RECEIPT_INDEX,
      component: () => import('@/views/advertising/receipt/index.vue'),
      meta: {
        locale: 'advertising.receipt',
        depth: 1,
        isCache: true,
        permissions: ['AD_RECEIPT:READ'],
      },
    },
    {
      path: 'payout',
      name: AdvertisingRouteEnum.AD_PAYOUT_INDEX,
      component: () => import('@/views/advertising/payout/index.vue'),
      meta: {
        locale: 'advertising.payout',
        depth: 1,
        isCache: true,
        permissions: ['AD_PAYOUT:READ'],
      },
    },
    {
      path: 'downstream-media',
      name: AdvertisingRouteEnum.AD_DOWNSTREAM_MEDIA_INDEX,
      component: () => import('@/views/advertising/downstreamMedia/index.vue'),
      meta: {
        locale: 'advertising.downstreamMedia',
        depth: 1,
        isCache: true,
        permissions: ['AD_DOWNSTREAM_MEDIA:READ'],
      },
    },
    {
      path: 'upstream-agent',
      name: AdvertisingRouteEnum.AD_UPSTREAM_AGENT_INDEX,
      component: () => import('@/views/advertising/upstreamAgent/index.vue'),
      meta: {
        locale: 'advertising.upstreamAgent',
        depth: 1,
        isCache: true,
        permissions: ['AD_UPSTREAM_AGENT:READ'],
      },
    },
    {
      path: 'business-entity',
      name: AdvertisingRouteEnum.AD_BUSINESS_ENTITY_INDEX,
      component: () => import('@/views/advertising/businessEntity/index.vue'),
      meta: {
        locale: 'advertising.businessEntity',
        depth: 1,
        isCache: true,
        permissions: ['AD_BUSINESS_ENTITY:READ'],
      },
    },
    {
      path: 'seal',
      name: AdvertisingRouteEnum.AD_SEAL_INDEX,
      component: () => import('@/views/advertising/seal/index.vue'),
      meta: {
        locale: 'advertising.seal',
        depth: 1,
        isCache: true,
        permissions: ['AD_SEAL:READ'],
      },
    },
    {
      path: 'change',
      name: AdvertisingRouteEnum.AD_CHANGE_INDEX,
      component: () => import('@/views/advertising/change/index.vue'),
      meta: {
        locale: 'advertising.change',
        depth: 1,
        isCache: true,
        permissions: ['AD_ORDER_CHANGE:READ'],
      },
    },
    {
      path: 'contract/detail',
      name: AdvertisingRouteEnum.AD_CONTRACT_DETAIL,
      component: () => import('@/views/advertising/detail/index.vue'),
      meta: {
        locale: 'advertising.contractDetail',
        depth: 2,
        isCache: false,
        // 除 READ 外，放开该模块各流程动作权限，保证审批人/经办人可从审批中心进入详情
        permissions: [
          'AD_CONTRACT:READ',
          'AD_CONTRACT:UPDATE',
          'AD_CONTRACT:ARCHIVE_APPROVE',
          'AD_CONTRACT:VOID_SUBMIT',
          'AD_CONTRACT:VOID_APPROVE',
        ],
      },
    },
    {
      path: 'customer/detail',
      name: AdvertisingRouteEnum.AD_CUSTOMER_DETAIL,
      component: () => import('@/views/advertising/detail/index.vue'),
      meta: {
        locale: 'advertising.customerDetail',
        depth: 2,
        isCache: false,
        permissions: ['AD_CUSTOMER:READ', 'AD_CUSTOMER:UPDATE'],
      },
    },
    {
      path: 'receipt/detail',
      name: AdvertisingRouteEnum.AD_RECEIPT_DETAIL,
      component: () => import('@/views/advertising/detail/index.vue'),
      meta: {
        locale: 'advertising.receiptDetail',
        depth: 2,
        isCache: false,
        permissions: ['AD_RECEIPT:READ', 'AD_RECEIPT:UPDATE', 'AD_RECEIPT:APPROVE'],
      },
    },
    {
      path: 'payout/detail',
      name: AdvertisingRouteEnum.AD_PAYOUT_DETAIL,
      component: () => import('@/views/advertising/detail/index.vue'),
      meta: {
        locale: 'advertising.payoutDetail',
        depth: 2,
        isCache: false,
        permissions: ['AD_PAYOUT:READ', 'AD_PAYOUT:UPDATE', 'AD_PAYOUT:APPROVE', 'AD_PAYOUT:PAY'],
      },
    },
    {
      path: 'downstream-media/detail',
      name: AdvertisingRouteEnum.AD_DOWNSTREAM_MEDIA_DETAIL,
      component: () => import('@/views/advertising/detail/index.vue'),
      meta: {
        locale: 'advertising.downstreamMediaDetail',
        depth: 2,
        isCache: false,
        permissions: ['AD_DOWNSTREAM_MEDIA:READ', 'AD_DOWNSTREAM_MEDIA:UPDATE'],
      },
    },
    {
      path: 'upstream-agent/detail',
      name: AdvertisingRouteEnum.AD_UPSTREAM_AGENT_DETAIL,
      component: () => import('@/views/advertising/detail/index.vue'),
      meta: {
        locale: 'advertising.upstreamAgentDetail',
        depth: 2,
        isCache: false,
        permissions: ['AD_UPSTREAM_AGENT:READ', 'AD_UPSTREAM_AGENT:UPDATE'],
      },
    },
    {
      path: 'business-entity/detail',
      name: AdvertisingRouteEnum.AD_BUSINESS_ENTITY_DETAIL,
      component: () => import('@/views/advertising/detail/index.vue'),
      meta: {
        locale: 'advertising.businessEntityDetail',
        depth: 2,
        isCache: false,
        permissions: ['AD_BUSINESS_ENTITY:READ', 'AD_BUSINESS_ENTITY:UPDATE', 'AD_BUSINESS_ENTITY:DELETE'],
      },
    },
    {
      path: 'seal/detail',
      name: AdvertisingRouteEnum.AD_SEAL_DETAIL,
      component: () => import('@/views/advertising/detail/index.vue'),
      meta: {
        locale: 'advertising.sealDetail',
        depth: 2,
        isCache: false,
        permissions: ['AD_SEAL:READ', 'AD_SEAL:APPROVE', 'AD_SEAL:REJECT'],
      },
    },
    {
      path: 'change/detail',
      name: AdvertisingRouteEnum.AD_CHANGE_DETAIL,
      component: () => import('@/views/advertising/detail/index.vue'),
      meta: {
        locale: 'advertising.changeDetail',
        depth: 2,
        isCache: false,
        permissions: [
          'AD_ORDER_CHANGE:READ',
          'AD_ORDER_CHANGE:SUBMIT',
          'AD_ORDER_CHANGE:APPROVE',
          'AD_ORDER_CHANGE:REJECT',
        ],
      },
    },
  ],
};

export default advertising;
