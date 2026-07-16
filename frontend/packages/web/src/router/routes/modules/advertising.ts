import { AdvertisingRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const advertising: AppRouteRecordRaw = {
  path: '/advertising',
  name: AdvertisingRouteEnum.ADVERTISING,
  redirect: '/advertising/order',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'module.advertising',
    permissions: ['AD_ORDER:READ', 'AD_ORDER_CHANGE:READ', 'AD_PAYMENT:READ', 'AD_CONTRACT:READ', 'AD_SEAL:READ'],
    icon: 'iconicon_order_form',
    hideChildrenInMenu: true,
    collapsedLocale: 'module.advertising',
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
      path: 'payment',
      name: AdvertisingRouteEnum.ADVERTISING_PAYMENT,
      component: () => import('@/views/advertising/payment/index.vue'),
      meta: {
        locale: 'module.advertising.payment',
        isTopMenu: true,
        permissions: ['AD_PAYMENT:READ'],
      },
    },
    {
      path: 'payment/create',
      name: AdvertisingRouteEnum.ADVERTISING_PAYMENT_CREATE,
      component: () => import('@/views/advertising/payment/create.vue'),
      meta: {
        locale: 'module.advertising.payment',
        permissions: ['AD_PAYMENT:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_PAYMENT,
      },
    },
    {
      path: 'payment/detail/:id',
      name: AdvertisingRouteEnum.ADVERTISING_PAYMENT_DETAIL,
      component: () => import('@/views/advertising/payment/detail.vue'),
      meta: {
        locale: 'module.advertising.payment',
        permissions: ['AD_PAYMENT:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_PAYMENT,
      },
    },
    {
      path: 'payment/media',
      name: AdvertisingRouteEnum.ADVERTISING_PAYMENT_MEDIA,
      component: () => import('@/views/advertising/payment/media.vue'),
      meta: {
        locale: 'module.advertising.payment.media',
        permissions: ['AD_PAYMENT:READ'],
        hideInMenu: true,
        activeMenu: AdvertisingRouteEnum.ADVERTISING_PAYMENT,
      },
    },
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

export default advertising;
