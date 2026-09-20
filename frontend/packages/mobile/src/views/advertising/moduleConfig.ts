/**
 * 广告模块「权限 + 状态 + 流程」中央配置（对齐 web 端）。
 *
 * 作为移动端逐模块对齐 web 的唯一真值源：
 * - updateCode：编辑权限码（订单编辑按 web 约定复用 CREATE 码）
 * - canEdit：状态 → 是否可编辑
 * - actions：详情页底部流程动作栏（提交/审批/付款等），按权限 + 状态展示
 *
 * 详情页（detail/index.vue）消费本配置，统一渲染「权限门控的编辑」与「流程动作栏」，
 * 审批中心与业务详情页因此共用同一套权限与流程口径。
 */
import { AdOrderChangeStatusEnum, AdSealRecordStatusEnum } from '@lib/shared/enums/advertisingEnum';

import {
  approveAdOrderChange,
  approveAdPayout,
  approveAdReceipt,
  approveAdSeal,
  approveArchive,
  approveVoid,
  executeAdOrderChange,
  payAdPayout,
  rejectAdOrderChange,
  rejectAdSeal,
  rejectArchive,
  rejectVoid,
  submitAdOrderChange,
  submitAdPayout,
  submitAdReceipt,
  submitArchive,
  submitVoid,
  updateAdBusinessEntity,
  updateAdDownstreamMedia,
  updateAdUpstreamAgent,
} from '@/api/modules';

import { AdvertisingRouteEnum } from '@/enums/routeEnum';

export type AdActionInput = 'remark' | 'reason';

export interface AdActionDef {
  key: string;
  /** 按钮文案 i18n key */
  labelKey: string;
  /** 所需权限码 */
  permission: string;
  type?: 'primary' | 'danger' | 'default' | 'warning';
  /** 是否满足状态条件（展示与否） */
  show: (d: any) => boolean;
  /** 执行动作；需要备注/原因时由详情页弹层收集后传入 value */
  run: (id: string, value?: string) => Promise<any>;
  /** 是否需要弹层收集备注/原因 */
  input?: AdActionInput;
  /** 前置校验：返回 i18n key 表示拦截（如「请先上传双盖附件」），返回 null 放行 */
  guard?: (d: any) => string | null;
}

export interface AdModuleConfig {
  /** 编辑权限码（'' 表示无编辑能力）；订单编辑复用 CREATE 码 */
  updateCode: string;
  /** 状态 → 是否可编辑 */
  canEdit: (d: any) => boolean;
  /** 详情页底部流程动作（按权限 + 状态展示） */
  actions: AdActionDef[];
}

export const AD_MODULE_CONFIG: Record<string, AdModuleConfig> = {
  [AdvertisingRouteEnum.AD_CONTRACT_DETAIL]: {
    updateCode: 'AD_CONTRACT:UPDATE',
    // 合同详情接口主体包在 contract 内（AdContractDetailResponse），状态一律从嵌套取，对齐 web detail.contract
    canEdit: (d) => d?.contract?.sealStatus === 0 || d?.contract?.sealStatus === 30,
    actions: [
      {
        key: 'submitArchive',
        labelKey: 'advertising.action.archiveSubmit',
        permission: 'AD_CONTRACT:UPDATE',
        type: 'primary',
        show: (d) => d?.contract?.sealStatus === 20 || d?.contract?.sealStatus === 50,
        // 对齐 web handleQuickSubmitArchive：无 type=20（双盖）附件时不允许提交归档
        guard: (d) =>
          (d?.attachments || []).some((a: any) => Number(a.type) === 20)
            ? null
            : 'advertising.contract.detail.doubleSealRequired',
        run: (id) => submitArchive(id),
      },
      {
        key: 'approveArchive',
        labelKey: 'advertising.action.archiveApprove',
        permission: 'AD_CONTRACT:ARCHIVE_APPROVE',
        type: 'primary',
        show: (d) => d?.contract?.sealStatus === 40,
        run: (id, v) => approveArchive(id, v),
        input: 'remark',
      },
      {
        key: 'rejectArchive',
        labelKey: 'advertising.action.archiveReject',
        permission: 'AD_CONTRACT:ARCHIVE_APPROVE',
        type: 'danger',
        show: (d) => d?.contract?.sealStatus === 40,
        run: (id, v) => rejectArchive(id, v),
        input: 'remark',
      },
      {
        key: 'submitVoid',
        labelKey: 'advertising.action.voidSubmit',
        permission: 'AD_CONTRACT:VOID_SUBMIT',
        type: 'danger',
        show: (d) => d?.contract?.status === 10 || d?.contract?.status === 20,
        run: (id, v) => submitVoid(id, v),
        input: 'reason',
      },
      {
        key: 'approveVoid',
        labelKey: 'advertising.action.voidApprove',
        permission: 'AD_CONTRACT:VOID_APPROVE',
        type: 'primary',
        show: (d) => d?.contract?.status === 70,
        run: (id, v) => approveVoid(id, v),
        input: 'remark',
      },
      {
        key: 'rejectVoid',
        labelKey: 'advertising.action.voidReject',
        permission: 'AD_CONTRACT:VOID_APPROVE',
        type: 'danger',
        show: (d) => d?.contract?.status === 70,
        run: (id, v) => rejectVoid(id, v),
        input: 'remark',
      },
    ],
  },
  [AdvertisingRouteEnum.AD_RECEIPT_DETAIL]: {
    updateCode: 'AD_RECEIPT:UPDATE',
    canEdit: (d) => d?.status === 0 || d?.status === 30,
    actions: [
      {
        key: 'submit',
        labelKey: 'advertising.action.submit',
        permission: 'AD_RECEIPT:UPDATE',
        type: 'default',
        show: (d) => d?.status === 0 || d?.status === 30,
        run: (id) => submitAdReceipt(id),
      },
      {
        key: 'approve',
        labelKey: 'advertising.action.approve',
        permission: 'AD_RECEIPT:APPROVE',
        type: 'primary',
        show: (d) => d?.status === 10,
        run: (id, v) => approveAdReceipt(id, { action: 'approve', remark: v }),
        input: 'remark',
      },
      {
        key: 'reject',
        labelKey: 'advertising.action.reject',
        permission: 'AD_RECEIPT:APPROVE',
        type: 'danger',
        show: (d) => d?.status === 10,
        run: (id, v) => approveAdReceipt(id, { action: 'reject', remark: v }),
        input: 'remark',
      },
    ],
  },
  [AdvertisingRouteEnum.AD_PAYOUT_DETAIL]: {
    updateCode: 'AD_PAYOUT:UPDATE',
    canEdit: (d) => d?.status === 0,
    actions: [
      {
        key: 'submit',
        labelKey: 'advertising.action.submit',
        permission: 'AD_PAYOUT:UPDATE',
        type: 'default',
        show: (d) => d?.status === 0,
        run: (id) => submitAdPayout(id),
      },
      {
        key: 'approve',
        labelKey: 'advertising.action.approve',
        permission: 'AD_PAYOUT:APPROVE',
        type: 'primary',
        show: (d) => d?.status === 10,
        run: (id, v) => approveAdPayout(id, { action: 'approve', remark: v }),
        input: 'remark',
      },
      {
        key: 'reject',
        labelKey: 'advertising.action.reject',
        permission: 'AD_PAYOUT:APPROVE',
        type: 'danger',
        show: (d) => d?.status === 10,
        run: (id, v) => approveAdPayout(id, { action: 'reject', remark: v }),
        input: 'remark',
      },
      {
        key: 'pay',
        labelKey: 'advertising.action.pay',
        permission: 'AD_PAYOUT:PAY',
        type: 'primary',
        show: (d) => d?.status === 20,
        run: (id, v) => payAdPayout(id, { payRemark: v }),
        input: 'remark',
      },
    ],
  },
  [AdvertisingRouteEnum.AD_SEAL_DETAIL]: {
    updateCode: '',
    canEdit: () => false,
    // 用印记录详情接口主体包在 record 内（AdSealRecordDetailResponse），状态取 record.status
    actions: [
      {
        key: 'approve',
        labelKey: 'advertising.action.approve',
        permission: 'AD_SEAL:APPROVE',
        type: 'primary',
        show: (d) => d?.record?.status === AdSealRecordStatusEnum.APPROVING,
        run: (id, v) => approveAdSeal(id, { approveRemark: v }),
        input: 'remark',
      },
      {
        key: 'reject',
        labelKey: 'advertising.action.reject',
        permission: 'AD_SEAL:REJECT',
        type: 'danger',
        show: (d) => d?.record?.status === AdSealRecordStatusEnum.APPROVING,
        run: (id, v) => rejectAdSeal(id, { approveRemark: v }),
        input: 'remark',
      },
    ],
  },
  [AdvertisingRouteEnum.AD_CHANGE_DETAIL]: {
    updateCode: '',
    canEdit: () => false,
    // 改单详情接口数据包裹在 change 内（{ change, statusLabel, orderNo }），状态取 change.status
    actions: [
      {
        key: 'submit',
        labelKey: 'advertising.action.submit',
        permission: 'AD_ORDER_CHANGE:SUBMIT',
        type: 'default',
        show: (d) => d?.change?.status === AdOrderChangeStatusEnum.DRAFT,
        run: (id) => submitAdOrderChange(id),
      },
      {
        key: 'approve',
        labelKey: 'advertising.action.approve',
        permission: 'AD_ORDER_CHANGE:APPROVE',
        type: 'primary',
        show: (d) => d?.change?.status === AdOrderChangeStatusEnum.SUBMITTED,
        run: (id, v) => approveAdOrderChange(id, { remark: v }),
        input: 'remark',
      },
      {
        key: 'reject',
        labelKey: 'advertising.action.reject',
        permission: 'AD_ORDER_CHANGE:REJECT',
        type: 'danger',
        show: (d) => d?.change?.status === AdOrderChangeStatusEnum.SUBMITTED,
        run: (id, v) => rejectAdOrderChange(id, { remark: v }),
        input: 'remark',
      },
      {
        key: 'execute',
        labelKey: 'advertising.action.execute',
        permission: 'AD_ORDER_CHANGE:SUBMIT',
        type: 'primary',
        show: (d) => d?.change?.status === AdOrderChangeStatusEnum.APPROVED,
        run: (id) => executeAdOrderChange(id),
      },
    ],
  },
  [AdvertisingRouteEnum.AD_CUSTOMER_DETAIL]: {
    updateCode: 'AD_CUSTOMER:UPDATE',
    canEdit: () => true,
    actions: [],
  },
  [AdvertisingRouteEnum.AD_BUSINESS_ENTITY_DETAIL]: {
    updateCode: 'AD_BUSINESS_ENTITY:UPDATE',
    canEdit: () => true,
    // 业务主体详情主体包在 entity 内（AdBusinessEntityDetailResponse）
    actions: [
      {
        key: 'enable',
        labelKey: 'advertising.action.enable',
        permission: 'AD_BUSINESS_ENTITY:UPDATE',
        type: 'primary',
        show: (d) => d?.entity?.status === 20,
        run: (id) => updateAdBusinessEntity({ id, status: 10 }),
      },
      {
        key: 'disable',
        labelKey: 'advertising.action.disable',
        permission: 'AD_BUSINESS_ENTITY:DELETE',
        type: 'default',
        show: (d) => d?.entity?.status === 10,
        run: (id) => updateAdBusinessEntity({ id, status: 20 }),
      },
    ],
  },
  [AdvertisingRouteEnum.AD_DOWNSTREAM_MEDIA_DETAIL]: {
    updateCode: 'AD_DOWNSTREAM_MEDIA:UPDATE',
    canEdit: () => true,
    // 下游客户详情主体包在 media 内（AdDownstreamMediaDetailResponse）
    actions: [
      {
        key: 'enable',
        labelKey: 'advertising.action.enable',
        permission: 'AD_DOWNSTREAM_MEDIA:UPDATE',
        type: 'primary',
        show: (d) => d?.media?.status === 20,
        run: (id) => updateAdDownstreamMedia({ id, status: 10 }),
      },
      {
        key: 'disable',
        labelKey: 'advertising.action.disable',
        permission: 'AD_DOWNSTREAM_MEDIA:UPDATE',
        type: 'default',
        show: (d) => d?.media?.status === 10,
        run: (id) => updateAdDownstreamMedia({ id, status: 20 }),
      },
    ],
  },
  [AdvertisingRouteEnum.AD_UPSTREAM_AGENT_DETAIL]: {
    updateCode: 'AD_UPSTREAM_AGENT:UPDATE',
    canEdit: () => true,
    // 上游代理商详情主体包在 agent 内（对齐 web upstreamAgent/detail.vue 的 detail.agent）
    actions: [
      {
        key: 'enable',
        labelKey: 'advertising.action.enable',
        permission: 'AD_UPSTREAM_AGENT:UPDATE',
        type: 'primary',
        show: (d) => d?.agent?.status === 20,
        run: (id) => updateAdUpstreamAgent({ id, status: 10 }),
      },
      {
        key: 'disable',
        labelKey: 'advertising.action.disable',
        permission: 'AD_UPSTREAM_AGENT:UPDATE',
        type: 'default',
        show: (d) => d?.agent?.status === 10,
        run: (id) => updateAdUpstreamAgent({ id, status: 20 }),
      },
    ],
  },
};
