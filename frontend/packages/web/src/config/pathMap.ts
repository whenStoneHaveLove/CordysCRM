import { AppRouteEnum } from '@/enums/routeEnum';

export type PathMapKey = keyof typeof AppRouteEnum;

export type PathMapRoute = (typeof AppRouteEnum)[PathMapKey];

export interface PathMapItem {
  key: PathMapKey | string; // 系统设置
  locale: string;
  route: PathMapRoute | string;
  children?: PathMapItem[];
  routeQuery?: Record<string, any>;
}

/**
 * 路由与菜单、tab、权限、国际化信息的映射关系，用于通过路由直接跳转到各页面及携带 tab 参数
 * key 是与后台商定的映射 key
 * locale 是国际化的 key
 * route 是路由的 name
 * routeQuery 是路由的固定参数集合，与routeParamKeys互斥，用于跳转同一个路由但不同 tab 时或其他需要固定参数的情况
 * children 是子路由/tab集合
 *
 * 当前只保留：系统/组织架构/角色权限/模块配置 + 全部广告模块
 */
export const pathMap: PathMapItem[] = [
  // 系统设置（仅保留组织架构/角色权限/模块配置）
  {
    key: 'SYSTEM',
    route: AppRouteEnum.SYSTEM,
    locale: 'menu.settings',
    children: [
      {
        key: 'SYSTEM_ORGANIZATION',
        route: AppRouteEnum.SYSTEM_ORG,
        locale: 'menu.settings.org',
      },
      {
        key: 'SYSTEM_ROLE',
        route: AppRouteEnum.SYSTEM_ROLE,
        locale: 'menu.settings.permission',
      },
      {
        key: 'SYSTEM_MODULE',
        route: AppRouteEnum.SYSTEM_MODULE,
        locale: 'menu.settings.moduleSetting',
      },
    ],
  },
  // 广告模块
  {
    key: 'ORDER',
    route: AppRouteEnum.ADVERTISING_ORDER,
    locale: 'menu.advertisingOrder',
    children: [
      {
        key: 'ORDER_INDEX',
        route: AppRouteEnum.ADVERTISING_ORDER,
        locale: 'menu.advertisingOrder',
      },
      {
        key: 'ORDER_CHANGE',
        route: AppRouteEnum.ADVERTISING_ORDER,
        locale: 'menu.advertisingOrder',
      },
    ],
  },
  {
    key: 'CONTRACT',
    route: AppRouteEnum.ADVERTISING_CONTRACT,
    locale: 'menu.advertisingContract',
  },
  {
    key: 'CUSTOMER',
    route: AppRouteEnum.ADVERTISING_CUSTOMER,
    locale: 'menu.advertisingCustomer',
  },
  {
    key: 'BUSINESS_ENTITY',
    route: AppRouteEnum.ADVERTISING_BUSINESS_ENTITY,
    locale: 'menu.advertisingBusinessEntity',
  },
  {
    key: 'DOWNSTREAM_MEDIA',
    route: AppRouteEnum.ADVERTISING_DOWNSTREAM_MEDIA,
    locale: 'menu.advertisingDownstreamMedia',
  },
  {
    key: 'UPSTREAM_AGENT',
    route: AppRouteEnum.ADVERTISING_UPSTREAM_AGENT,
    locale: 'menu.advertisingUpstreamAgent',
  },
  {
    key: 'PAYMENT',
    route: AppRouteEnum.ADVERTISING_PAYMENT,
    locale: 'menu.advertisingPayment',
  },
  {
    key: 'SEAL',
    route: AppRouteEnum.ADVERTISING_SEAL,
    locale: 'menu.advertisingSeal',
  },
  {
    key: 'SUPPORT',
    route: AppRouteEnum.ADVERTISING_WORKBENCH,
    locale: 'menu.advertisingWorkbench',
  },
  {
    key: 'DICT',
    route: AppRouteEnum.ADVERTISING_SYSTEM_DICT,
    locale: 'menu.advertisingSystemDict',
  },
];