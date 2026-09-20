import { type Ref, ref } from 'vue';

import {
  AD_ORDER_CHANGE_FIELD_META,
  AdModeOptions,
  AdOrderTypeOptions,
  AdPaymentMethodOptions,
  AdPostpayTriggerOptions,
  AdReceiptMethodOptions,
  getAdSealRecordStatusLabel,
  getAdSealTypeLabel,
} from '@lib/shared/enums/advertisingEnum';

import {
  deleteAdContractAttachment,
  getAdBusinessEntityDetail,
  getAdBusinessEntityPage,
  getAdContractDeletedDetail,
  getAdContractDetail,
  getAdCustomerDetail,
  getAdCustomerPage,
  getAdDictPage,
  getAdDownstreamMediaDetail,
  getAdDownstreamMediaPage,
  getAdOrderChangeDetail,
  getAdPayoutDetail,
  getAdReceiptDetail,
  getAdSealDetail,
  getAdUpstreamAgentDetail,
  getAdUpstreamAgentPage,
  uploadAdContractAttachment,
  uploadTempAttachment,
} from '@/api/modules';
import { hasPermission } from '@/utils/permission';

import { AdvertisingRouteEnum } from '@/enums/routeEnum';

import { getUserName } from '@/views/advertising/useUserMap';
import {
  fmtAmount,
  fmtCount,
  fmtDate,
  fmtDateTime,
  getAdChangeFieldLabel,
  getAdSealRecordStatusTagStyle,
} from '@/views/advertising/utils';

type TranslateFn = (key: string) => string;

type CellValue = string | number | null | undefined;

/** 详情行可跳转目标（渲染为超链接 + ›） */
export interface AdDetailLink {
  name: string;
  query: Record<string, string>;
}

export interface AdDetailRow {
  label: string;
  value: CellValue;
  /** 存在时该行渲染为可点击跳转（对齐 web 的关联订单/合同超链接） */
  to?: AdDetailLink;
}

export interface AdDetailCompareRow {
  label: string;
  before: string;
  after: string;
}

export interface AdDetailSection {
  title?: string;
  rows?: AdDetailRow[];
  /** 字段变更对比行（改前 → 改后），对齐 web 改单详情「字段变更对比」 */
  compareRows?: AdDetailCompareRow[];
}

export interface AdDetailConfig {
  /** 标题 i18n key */
  title: string;
  /** 详情接口；options.deleted 为真时取已作废（逻辑删除）数据 */
  fetch: (id: string, options?: { deleted?: boolean }) => Promise<any>;
  /** 由详情数据构造展示区块 */
  build: (data: any, t: TranslateFn) => AdDetailSection[];
  /** 可选的子表区块（附件 / 记录列表），渲染于标准区块之后 */
  extra?: (data: any, t: TranslateFn) => AdDetailExtraBlock[];
  /** 可选：可编辑时跳转的表单路由（详情页右上角「编辑」入口） */
  editRoute?: string;
  /** 可选：build 执行前的异步预加载（如改单对比所需的名称映射），完成后 build 会重新计算 */
  preload?: (data: any) => Promise<void>;
}

/** 详情子表区块类型 */
export type AdDetailExtraAttachmentItem = {
  id?: string;
  fileName: string;
  fileUrl: string;
  sub?: string;
  /** 是否展示删除入口（需所在区块提供 onDelete） */
  deletable?: boolean;
};
export type AdDetailExtraRecordItem = {
  primary: string;
  secondary?: string;
  /** 第三行（如用印记录的申请人/审批时间） */
  tertiary?: string;
  tag?: string;
  /** 标签配色，直接 v-bind 到 CrmTag */
  tagStyle?: { bgColor: string; textColor: string };
  to?: { name: string; query: Record<string, string> };
};
export interface AdDetailExtraAttachmentBlock {
  kind: 'attachment';
  title: string;
  items: AdDetailExtraAttachmentItem[];
  /** 可选：附件管理入口（如合同双盖附件）。存在时详情页动作栏出现「上传」按钮并弹出管理层 */
  uploader?: { title: string; label: string; accept: string; onUpload: (file: File) => Promise<void> };
  /** 可选：删除附件 */
  onDelete?: (item: AdDetailExtraAttachmentItem) => Promise<void>;
}
export interface AdDetailExtraRecordsBlock {
  kind: 'records';
  title: string;
  items: AdDetailExtraRecordItem[];
}
export type AdDetailExtraBlock = AdDetailExtraAttachmentBlock | AdDetailExtraRecordsBlock;

/** 构造单区块，空值统一降级为 '-' */
function buildSection(t: TranslateFn, pairs: [string, CellValue, AdDetailLink?][]): AdDetailSection {
  return {
    rows: pairs.map(([labelKey, value, to]) => ({
      label: t(labelKey),
      value: value === '' || value === null || value === undefined ? '-' : value,
      to,
    })),
  };
}

/** 有效期文案：两端皆空时返回 '-' */
function periodText(from?: number | string | null, to?: number | string | null): string {
  const fromText = fmtDate(from);
  const toText = fmtDate(to);
  return fromText === '-' && toText === '-' ? '-' : `${fromText} ~ ${toText}`;
}

/* ----------------------------- 改单字段变更对比（对齐 web change/detail.vue） ----------------------------- */

/** 下游客户付款返点明细为整表覆盖的特殊变更字段（不对应 ad_order 标量字段） */
const DOWNSTREAM_SPECIAL_FIELDS = ['downstreamMediaIds', 'downstreamMediaPayables'];

/** 下游客户付款返点明细对比字段定义（顺序 = 展示顺序，镜像 web DownstreamMediaCompareTable） */
const DOWNSTREAM_FIELD_DEFS: Array<{
  key: string;
  label: string;
  type?: 'amount' | 'enum' | 'date';
  options?: any[];
}> = [
  { key: 'payableAmount', label: '应付金额', type: 'amount' },
  { key: 'noRebateAmount', label: '不记返金额', type: 'amount' },
  { key: 'rebateMode', label: '返点方式', type: 'enum', options: AdModeOptions },
  { key: 'rebateValue', label: '返点值', type: 'amount' },
  { key: 'actualPayable', label: '实际应付', type: 'amount' },
  { key: 'paymentMethod', label: '付款方式', type: 'enum', options: AdPaymentMethodOptions },
  { key: 'paymentPrepayMode', label: '预付模式', type: 'enum', options: AdModeOptions },
  { key: 'paymentPrepayRatio', label: '预付比例%', type: 'amount' },
  { key: 'paymentPrepayDeadline', label: '预付截止日', type: 'date' },
  { key: 'paymentPostpayTrigger', label: '后付触发', type: 'enum', options: AdPostpayTriggerOptions },
  { key: 'paymentPostpayDays', label: '后付天数' },
];

/** 枚举选项映射（镜像 web change/detail.vue enumOptionsMap） */
const CHANGE_ENUM_OPTIONS: Record<string, any[]> = {
  'enum-orderType': AdOrderTypeOptions,
  'enum-rebateMode': AdModeOptions,
  'enum-receiptMethod': AdReceiptMethodOptions,
  'enum-paymentMethod': AdPaymentMethodOptions,
  'enum-prepayMode': AdModeOptions,
  'enum-postpayTrigger': AdPostpayTriggerOptions,
};

/** 名称映射缓存（id → 名称），preload 阶段按变更字段按需填充 */
const customerNameMap = ref<Record<string, string>>({});
const upstreamNameMap = ref<Record<string, string>>({});
const industryNameMap = ref<Record<string, string>>({});
const businessEntityNameMap = ref<Record<string, string>>({});
const downstreamNameMap = ref<Record<string, string>>({});

async function loadNameMap(
  api: (p: any) => Promise<any>,
  store: Ref<Record<string, string>>,
  nameKeys: string[]
): Promise<void> {
  try {
    const res: any = await api({ current: 1, pageSize: 500 });
    const map: Record<string, string> = {};
    (res?.list || []).forEach((it: any) => {
      map[String(it.id)] = nameKeys.map((k) => it[k]).find((v) => v != null && v !== '') || String(it.id);
    });
    store.value = map;
  } catch {
    // 静默失败，名称缺失时回退为 id
  }
}

async function loadIndustryMap(): Promise<void> {
  try {
    const res: any = await getAdDictPage({ current: 1, pageSize: 500, dictCode: 'industry' });
    const map: Record<string, string> = {};
    (res?.list || []).forEach((it: any) => {
      map[String(it.dictValue ?? it.id)] = it.dictLabel || it.dictValue || String(it.id);
    });
    industryNameMap.value = map;
  } catch {
    // 静默失败
  }
}

function parseChangeFields(value?: string | null): string[] {
  if (!value) return [];
  if (value.trim().startsWith('[')) {
    try {
      const arr = JSON.parse(value);
      return Array.isArray(arr) ? arr.map((f) => String(f)) : [];
    } catch {
      return [];
    }
  }
  return value
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean);
}

function parseSnapshot(value?: string | null): Record<string, any> {
  if (!value) return {};
  try {
    return JSON.parse(value);
  } catch {
    return {};
  }
}

function nameOf(map: Record<string, string>, value: any): string {
  if (value === null || value === undefined || value === '') return '-';
  return map[String(value)] || String(value);
}

function enumLabel(options: any[] | undefined, value: any): string {
  if (value === null || value === undefined || value === '') return '-';
  const found = (options || []).find((o) => String(o.value) === String(value));
  return found ? String(found.label) : String(value);
}

/** 标量变更字段值格式化（对齐 web changeValLabel：日期 / 枚举 / 关联名称） */
function formatChangeFieldValue(field: string, value: any): string {
  if (value === null || value === undefined || value === '') return '-';
  const meta = AD_ORDER_CHANGE_FIELD_META.find((m) => m.field === field);
  if (!meta) return String(value);
  if (meta.type === 'date') {
    const ts = Number(value);
    if (!Number.isNaN(ts) && String(value).length >= 10) return fmtDate(ts < 1e12 ? ts * 1000 : ts);
    return String(value).slice(0, 10);
  }
  const { control } = meta;
  if (control === 'select-customer') return nameOf(customerNameMap.value, value);
  if (control === 'select-upstream') return nameOf(upstreamNameMap.value, value);
  if (control === 'select-industry') return nameOf(industryNameMap.value, value);
  if (control === 'select-businessEntity') return nameOf(businessEntityNameMap.value, value);
  if (control) return enumLabel(CHANGE_ENUM_OPTIONS[control], value);
  return String(value);
}

/** 下游明细单元格格式化（对齐 web fmtCell，含前端计算的 actualPayable） */
function fmtDownstreamCell(def: (typeof DOWNSTREAM_FIELD_DEFS)[number], row: any): string {
  if (!row) return '-';
  if (def.key === 'actualPayable') {
    const base = Number(row.payableAmount || 0) - Number(row.noRebateAmount || 0);
    let actual = base;
    if (row.rebateMode === 10) actual = base - (base * Number(row.rebateValue || 0)) / 100;
    else if (row.rebateMode === 20) actual = base - Number(row.rebateValue || 0);
    return fmtAmount(actual);
  }
  const raw = row[def.key];
  if (raw === null || raw === undefined || raw === '') return '-';
  if (def.type === 'enum') return enumLabel(def.options, raw);
  if (def.type === 'amount') return fmtAmount(Number(raw));
  if (def.type === 'date') {
    const ts = Number(raw);
    if (!Number.isNaN(ts) && String(raw).length >= 10) return fmtDate(ts < 1e12 ? ts * 1000 : ts);
    return String(raw).slice(0, 10);
  }
  return String(raw);
}

/** 下游客户付款返点明细对比：按媒体分组，每个媒体一个区块（对齐 web DownstreamMediaCompareTable） */
function buildDownstreamCompareSections(beforeRaw: any, afterRaw: any, t: TranslateFn): AdDetailSection[] {
  const beforeRows: any[] = Array.isArray(beforeRaw) ? beforeRaw : [];
  const afterRows: any[] = Array.isArray(afterRaw) ? afterRaw : [];
  const beforeMap = new Map<string, any>();
  const afterMap = new Map<string, any>();
  beforeRows.forEach((r) => beforeMap.set(String(r.downstreamMediaId), r));
  afterRows.forEach((r) => afterMap.set(String(r.downstreamMediaId), r));

  const orderedIds: string[] = [];
  afterRows.forEach((r) => {
    const id = String(r.downstreamMediaId);
    if (!orderedIds.includes(id)) orderedIds.push(id);
  });
  beforeRows.forEach((r) => {
    const id = String(r.downstreamMediaId);
    if (!orderedIds.includes(id)) orderedIds.push(id);
  });

  const sections: AdDetailSection[] = [];
  orderedIds.forEach((id) => {
    const b = beforeMap.get(id);
    const a = afterMap.get(id);
    const rawName = downstreamNameMap.value[id] || id;
    let mediaName = rawName;
    if (!b) mediaName = `${rawName}${t('advertising.change.detail.added')}`;
    else if (!a) mediaName = `${rawName}${t('advertising.change.detail.removed')}`;

    const compareRows: AdDetailCompareRow[] = DOWNSTREAM_FIELD_DEFS.map((def) => ({
      label: def.label,
      before: !b ? '-' : fmtDownstreamCell(def, b),
      after: !a ? '-' : fmtDownstreamCell(def, a),
    }));
    sections.push({ title: mediaName, compareRows });
  });
  return sections;
}

/** 用印记录补充行：申请人 / 审批人 / 审批时间（对齐 web 用印记录表列） */
function sealRecordExtraText(r: any, t: TranslateFn): string {
  const parts: string[] = [];
  if (r.applicantId) parts.push(`${t('advertising.field.applicant')} ${getUserName(r.applicantId)}`);
  if (r.approverId) parts.push(`${t('advertising.field.approver')} ${getUserName(r.approverId)}`);
  if (r.approvedAt) parts.push(`${t('advertising.field.approveTime')} ${fmtDateTime(r.approvedAt)}`);
  return parts.join(' · ');
}

const CONFIG_MAP: Record<string, AdDetailConfig> = {
  [AdvertisingRouteEnum.AD_CONTRACT_DETAIL]: {
    title: 'advertising.contractDetail',
    editRoute: AdvertisingRouteEnum.AD_CONTRACT_FORM,
    // 已作废合同走「忽略 deleted 标记」的详情接口（列表「已作废」tab 带 deleted=1）
    fetch: (id, options) => (options?.deleted ? getAdContractDeletedDetail(id) : getAdContractDetail(id)),
    build: (data, t) => {
      const contract = data?.contract || {};
      const sections: AdDetailSection[] = [
        buildSection(t, [
          ['advertising.field.contractNo', contract.contractNo],
          ['advertising.field.contractName', contract.contractName],
          ['advertising.field.businessEntity', data?.businessEntityName],
          ['advertising.field.direction', data?.directionLabel],
          ['advertising.field.contractType', data?.typeLabel],
          ['advertising.field.relatedParty', data?.relatedPartyName],
          ['advertising.field.signingEntity', contract.signingEntity],
          ['advertising.field.validPeriod', periodText(contract.validFrom, contract.validTo)],
          ['advertising.field.amount', fmtAmount(contract.amount)],
          ['advertising.field.rebateTerms', contract.rebateTerms],
          ['advertising.field.sealStatus', data?.sealStatusLabel],
          ['advertising.field.status', data?.statusLabel],
        ]),
      ];
      // 归档审批信息 / 作废信息：与 web 一致，仅在存在对应流程痕迹时展示
      if (contract.archiveApproveRemark || contract.archiveApproveUser) {
        sections.push({
          ...buildSection(t, [
            ['advertising.field.archiveApproveUser', getUserName(contract.archiveApproveUser)],
            ['advertising.field.archiveApproveTime', fmtDateTime(contract.archiveApproveTime)],
            ['advertising.field.archiveApproveRemark', contract.archiveApproveRemark],
          ]),
          title: t('advertising.contract.detail.archiveInfo'),
        });
      }
      if (contract.voidReason || contract.voidApplicantId) {
        sections.push({
          ...buildSection(t, [
            ['advertising.field.voidReason', contract.voidReason],
            ['advertising.field.voidApplicant', getUserName(contract.voidApplicantId)],
            ['advertising.field.voidAppliedAt', fmtDateTime(contract.voidAppliedAt)],
            ['advertising.field.voidApproveUser', getUserName(contract.voidApproveUser)],
            ['advertising.field.voidApproveTime', fmtDateTime(contract.voidApproveTime)],
            ['advertising.field.voidApproveRemark', contract.voidApproveRemark],
          ]),
          title: t('advertising.contract.detail.voidInfo'),
        });
      }
      return sections;
    },
    extra: (data, t) => {
      const blocks: AdDetailExtraBlock[] = [];
      const contract = data?.contract || {};
      const attachSub = (type: number) => {
        if (type === 10) return t('advertising.contract.detail.attachSeal');
        if (type === 20) return t('advertising.contract.detail.attachDoubleSeal');
        return '';
      };
      const attachments = data?.attachments || [];
      // 已用印 / 归档驳回 → 可管理双盖附件（上传 type=20 + 删除），对齐 web 的「上传双盖附件」弹层
      const canManageDoubleSeal =
        (contract.sealStatus === 20 || contract.sealStatus === 50) && hasPermission('AD_CONTRACT:UPDATE');
      if (attachments.length || canManageDoubleSeal) {
        const block: AdDetailExtraAttachmentBlock = {
          kind: 'attachment',
          title: t('advertising.contract.detail.attachments'),
          items: attachments.map((a: any) => ({
            id: a.id,
            fileName: a.fileName,
            fileUrl: a.fileUrl,
            sub: attachSub(a.type),
            // 仅双盖附件可删（用印附件由申请流程产生）
            deletable: canManageDoubleSeal && Number(a.type) === 20,
          })),
        };
        if (canManageDoubleSeal) {
          block.uploader = {
            title: t('advertising.contract.detail.attachDoubleSeal'),
            label: t('advertising.contract.detail.uploadDoubleSeal'),
            accept: '.pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png',
            onUpload: async (file: File) => {
              const res: any = await uploadTempAttachment(file);
              const fileId = res?.data?.[0] || res?.data || '';
              if (!fileId) throw new Error(t('advertising.contract.detail.uploadFailed'));
              await uploadAdContractAttachment(String(contract.id), 20, fileId, file.name);
            },
          };
          block.onDelete = async (item) => {
            if (!item.id) return;
            await deleteAdContractAttachment(String(contract.id), item.id);
          };
        }
        blocks.push(block);
      }
      const sealRecords = data?.sealRecords || [];
      if (sealRecords.length) {
        blocks.push({
          kind: 'records',
          title: t('advertising.contract.detail.sealRecords'),
          items: sealRecords.map((r: any) => ({
            primary: getAdSealTypeLabel(r.sealType),
            secondary: `${t('advertising.field.appliedCopies')} ${r.appliedCopies ?? 0} / ${t(
              'advertising.field.actualCopies'
            )} ${r.actualCopies ?? 0}`,
            tertiary: sealRecordExtraText(r, t),
            // 用印记录状态用「记录」枚举（0 审批中/10 通过/20 驳回），不能与合同用印状态混用
            tag: getAdSealRecordStatusLabel(r.status),
            tagStyle: getAdSealRecordStatusTagStyle(r.status),
          })),
        });
      }
      const orderList = data?.orderList || [];
      if (orderList.length) {
        blocks.push({
          kind: 'records',
          title: t('advertising.contract.detail.relatedOrders'),
          items: orderList.map((o: any) => ({
            primary: `${o.orderName || '-'} (${o.orderNo || '-'})`,
            to: { name: AdvertisingRouteEnum.AD_ORDER_DETAIL, query: { id: o.orderId } },
          })),
        });
      }
      return blocks;
    },
  },
  [AdvertisingRouteEnum.AD_CUSTOMER_DETAIL]: {
    title: 'advertising.customerDetail',
    editRoute: AdvertisingRouteEnum.AD_CUSTOMER_FORM,
    fetch: getAdCustomerDetail,
    build: (data, t) => {
      const customer = data?.customer || {};
      return [
        buildSection(t, [
          ['advertising.field.customerName', customer.name],
          ['advertising.field.brand', customer.brand],
          ['advertising.field.industry', customer.industry],
          ['advertising.field.customerLevel', data?.customerLevelLabel],
          ['advertising.field.signingEntity', customer.signingEntity],
          ['advertising.field.contactPerson', customer.contactPerson],
          ['advertising.field.contactPhone', customer.contactPhone],
          ['advertising.field.email', customer.email],
          ['advertising.field.address', customer.address],
          ['advertising.field.orderCount', fmtCount(data?.orderCount ?? 0)],
          ['advertising.field.status', data?.statusLabel],
          ['advertising.field.remark', customer.remark],
          ['advertising.field.creator', getUserName(customer.createUser)],
          ['advertising.field.createTime', fmtDateTime(customer.createTime)],
        ]),
      ];
    },
  },
  [AdvertisingRouteEnum.AD_RECEIPT_DETAIL]: {
    title: 'advertising.receiptDetail',
    editRoute: AdvertisingRouteEnum.AD_RECEIPT_FORM,
    fetch: getAdReceiptDetail,
    build: (data, t) => [
      buildSection(t, [
        ['advertising.field.receiptNo', data?.receiptNo],
        [
          'advertising.field.orderNo',
          data?.orderNo,
          data?.orderId ? { name: AdvertisingRouteEnum.AD_ORDER_DETAIL, query: { id: data.orderId } } : undefined,
        ],
        ['advertising.field.orderName', data?.orderName],
        ['advertising.field.receiptType', data?.typeLabel],
        ['advertising.field.amount', fmtAmount(data?.amount)],
        ['advertising.field.receiptTime', fmtDate(data?.receiptTime)],
        ['advertising.field.status', data?.statusLabel],
        ['advertising.field.voucher', data?.voucherUrl],
        ['advertising.field.remark', data?.remark],
        ['advertising.field.creator', getUserName(data?.createUser)],
        ['advertising.field.createTime', fmtDateTime(data?.createTime)],
        ['advertising.field.approver', getUserName(data?.approveUser)],
        ['advertising.field.approveTime', fmtDateTime(data?.approveTime)],
        ['advertising.field.approveRemark', data?.approveRemark],
      ]),
    ],
    extra: (data, t) => {
      const contracts = data?.contracts || [];
      if (!contracts.length) return [];
      return [
        {
          kind: 'records',
          title: t('advertising.contract.detail.relatedContracts'),
          items: contracts.map((c: any) => ({
            primary: `${c.contractName || '-'} (${c.contractNo || '-'})`,
            to: { name: AdvertisingRouteEnum.AD_CONTRACT_DETAIL, query: { id: c.id } },
          })),
        },
      ];
    },
  },
  [AdvertisingRouteEnum.AD_PAYOUT_DETAIL]: {
    title: 'advertising.payoutDetail',
    editRoute: AdvertisingRouteEnum.AD_PAYOUT_FORM,
    fetch: getAdPayoutDetail,
    build: (data, t) => [
      buildSection(t, [
        ['advertising.field.paymentNo', data?.paymentNo],
        ['advertising.field.billType', data?.billTypeLabel],
        ['advertising.field.paymentType', data?.typeLabel],
        [
          'advertising.field.orderNo',
          data?.orderNo,
          data?.orderId ? { name: AdvertisingRouteEnum.AD_ORDER_DETAIL, query: { id: data.orderId } } : undefined,
        ],
        ['advertising.field.orderName', data?.orderName],
        ['advertising.field.mediaNames', data?.mediaNames],
        ['advertising.field.amount', fmtAmount(data?.amount)],
        ['advertising.field.paymentTime', fmtDate(data?.paymentTime)],
        ['advertising.field.status', data?.statusLabel],
        ['advertising.field.voucher', data?.voucherUrl],
        ['advertising.field.remark', data?.remark],
        ['advertising.field.creator', getUserName(data?.createUser)],
        ['advertising.field.createTime', fmtDateTime(data?.createTime)],
        ['advertising.field.approver', getUserName(data?.approveUser)],
        ['advertising.field.approveTime', fmtDateTime(data?.approveTime)],
        ['advertising.field.approveRemark', data?.approveRemark],
        ['advertising.field.payUser', getUserName(data?.payUser)],
        ['advertising.field.payTime', fmtDateTime(data?.payTime)],
        ['advertising.field.payRemark', data?.payRemark],
      ]),
    ],
    extra: (data, t) => {
      const blocks: AdDetailExtraBlock[] = [];
      const contracts = data?.contracts || [];
      if (contracts.length) {
        blocks.push({
          kind: 'records',
          title: t('advertising.contract.detail.relatedContracts'),
          items: contracts.map((c: any) => ({
            primary: `${c.contractName || '-'} (${c.contractNo || '-'})`,
            to: { name: AdvertisingRouteEnum.AD_CONTRACT_DETAIL, query: { id: c.id } },
          })),
        });
      }
      const mediaDetails = data?.mediaDetails || [];
      if (mediaDetails.length) {
        blocks.push({
          kind: 'records',
          title: t('advertising.contract.detail.mediaDetails'),
          items: mediaDetails.map((m: any) => ({
            primary: m.mediaName || '-',
            secondary: `${t('advertising.order.payableAmount')} ${fmtAmount(m.payableAmount)} / ${t(
              'advertising.order.actualPayable'
            )} ${fmtAmount(m.actualPayable)}`,
          })),
        });
      }
      const invoice = data?.invoice;
      if (invoice?.fileUrl) {
        blocks.push({
          kind: 'attachment',
          title: t('advertising.contract.detail.invoice'),
          items: [
            {
              fileName: invoice.fileName || invoice.invoiceNo || t('advertising.contract.detail.invoice'),
              fileUrl: invoice.fileUrl,
            },
          ],
        });
      }
      return blocks;
    },
  },
  [AdvertisingRouteEnum.AD_SEAL_DETAIL]: {
    title: 'advertising.sealDetail',
    fetch: getAdSealDetail,
    build: (data, t) => {
      const record = data?.record || {};
      return [
        buildSection(t, [
          [
            'advertising.field.contractNo',
            data?.contractNo,
            data?.contractId
              ? { name: AdvertisingRouteEnum.AD_CONTRACT_DETAIL, query: { id: data.contractId } }
              : undefined,
          ],
          ['advertising.field.businessEntity', data?.businessEntityName],
          ['advertising.field.orderNo', data?.orderNo],
          ['advertising.field.sealType', record.sealType != null ? getAdSealTypeLabel(record.sealType) : null],
          ['advertising.field.appliedCopies', fmtCount(record.appliedCopies ?? 0)],
          ['advertising.field.actualCopies', fmtCount(record.actualCopies ?? 0)],
          ['advertising.field.applyRemark', record.applyRemark],
          ['advertising.field.status', data?.statusLabel],
          ['advertising.field.approvedAt', fmtDateTime(record.approvedAt)],
          ['advertising.field.approveRemark', record.approveRemark],
          ['advertising.field.createTime', fmtDateTime(record.createTime)],
        ]),
      ];
    },
  },
  [AdvertisingRouteEnum.AD_BUSINESS_ENTITY_DETAIL]: {
    title: 'advertising.businessEntityDetail',
    editRoute: AdvertisingRouteEnum.AD_BUSINESS_ENTITY_FORM,
    fetch: getAdBusinessEntityDetail,
    build: (data, t) => {
      const entity = data?.entity || {};
      return [
        buildSection(t, [
          ['advertising.field.entityName', entity.name],
          ['advertising.field.entityCode', entity.code],
          ['advertising.field.crossEntity', entity.isCrossEntity ? t('advertising.crossEntity') : null],
          ['advertising.field.userCount', fmtCount(data?.userCount ?? 0)],
          ['advertising.field.orderCount', fmtCount(data?.orderCount ?? 0)],
          ['advertising.field.status', data?.statusLabel],
          ['advertising.field.remark', entity.remark],
          ['advertising.field.creator', getUserName(entity.createUser)],
          ['advertising.field.createTime', fmtDateTime(entity.createTime)],
        ]),
      ];
    },
  },
  [AdvertisingRouteEnum.AD_CHANGE_DETAIL]: {
    title: 'advertising.changeDetail',
    fetch: getAdOrderChangeDetail,
    // 按需预加载名称映射，供字段对比展示（客户 / 上游 / 行业 / 业务主体 / 下游客户）
    preload: async (data) => {
      const change = data?.change || {};
      const fields = parseChangeFields(change.changeFields);
      const tasks: Promise<void>[] = [];
      if (fields.includes('customerId')) {
        tasks.push(loadNameMap(getAdCustomerPage, customerNameMap, ['customerName', 'name']));
      }
      if (fields.includes('upstreamAgentId')) {
        tasks.push(loadNameMap(getAdUpstreamAgentPage, upstreamNameMap, ['resourceName', 'name']));
      }
      if (fields.includes('industryCode')) tasks.push(loadIndustryMap());
      if (fields.includes('businessEntityId') || fields.includes('signingEntity')) {
        tasks.push(loadNameMap(getAdBusinessEntityPage, businessEntityNameMap, ['name']));
      }
      if (fields.some((f) => DOWNSTREAM_SPECIAL_FIELDS.includes(f))) {
        tasks.push(loadNameMap(getAdDownstreamMediaPage, downstreamNameMap, ['resourceName', 'name']));
      }
      await Promise.all(tasks);
    },
    build: (data, t) => {
      const change = data?.change || {};
      const before = parseSnapshot(change.snapshotBefore);
      const after = parseSnapshot(change.snapshotAfter);
      const fields = parseChangeFields(change.changeFields);
      const hasDownstream = fields.some((f) => DOWNSTREAM_SPECIAL_FIELDS.includes(f));

      const sections: AdDetailSection[] = [
        buildSection(t, [
          [
            'advertising.field.orderNo',
            data?.orderNo,
            data?.change?.orderId
              ? { name: AdvertisingRouteEnum.AD_ORDER_DETAIL, query: { id: data.change.orderId } }
              : undefined,
          ],
          ['advertising.field.changeId', change.id],
          ['advertising.field.reason', change.reason],
          ['advertising.field.status', data?.statusLabel],
          ['advertising.field.creator', getUserName(change.createUser)],
          ['advertising.field.createTime', fmtDateTime(change.createTime)],
          ['advertising.field.updateUser', getUserName(change.updateUser)],
          ['advertising.field.updateTime', fmtDateTime(change.updateTime)],
          ['advertising.field.approver', getUserName(change.approverId)],
          ['advertising.field.approvedAt', fmtDateTime(change.approvedAt)],
          ['advertising.field.approveRemark', change.approveRemark],
        ]),
      ];

      // 字段变更对比（标量字段，排除下游明细特殊字段）
      const scalarFields = fields.filter((f) => !DOWNSTREAM_SPECIAL_FIELDS.includes(f));
      if (scalarFields.length) {
        sections.push({
          title: t('advertising.change.detail.compareTitle'),
          compareRows: scalarFields.map((f) => ({
            label: getAdChangeFieldLabel(f),
            before: formatChangeFieldValue(f, before[f]),
            after: formatChangeFieldValue(f, after[f]),
          })),
        });
      }

      // 下游客户付款返点明细变更
      if (hasDownstream) {
        sections.push({ title: t('advertising.change.detail.downstreamCompareTitle') });
        sections.push(
          ...buildDownstreamCompareSections(before.downstreamMediaPayables, after.downstreamMediaPayables, t)
        );
      }
      return sections;
    },
  },
  [AdvertisingRouteEnum.AD_DOWNSTREAM_MEDIA_DETAIL]: {
    title: 'advertising.downstreamMediaDetail',
    fetch: getAdDownstreamMediaDetail,
    build: (data, t) => {
      const media = data?.media || {};
      return [
        buildSection(t, [
          ['advertising.field.mediaName', media.name],
          ['advertising.field.mediaType', data?.mediaTypeLabel ?? media.mediaTypeLabel],
          ['advertising.field.channel', media.channel],
          ['advertising.field.rateCard', media.rateCard],
          ['advertising.field.discountPolicy', media.discountPolicy],
          ['advertising.field.businessEntity', data?.businessEntityName ?? media.businessEntityName],
          ['advertising.field.contactPerson', media.contactPerson],
          ['advertising.field.contactPhone', media.contactPhone],
          ['advertising.field.cooperationStatus', data?.cooperationStatusLabel],
          ['advertising.field.status', data?.statusLabel],
          ['advertising.field.createTime', fmtDateTime(media.createTime)],
        ]),
      ];
    },
  },
  [AdvertisingRouteEnum.AD_UPSTREAM_AGENT_DETAIL]: {
    title: 'advertising.upstreamAgentDetail',
    fetch: getAdUpstreamAgentDetail,
    // 上游代理商详情主体包在 agent 内（对齐 web upstreamAgent/detail.vue 的 detail.agent）
    build: (data, t) => {
      const agent = data?.agent || {};
      return [
        buildSection(t, [
          ['advertising.field.agentName', agent.name],
          ['advertising.field.signingEntity', agent.signingEntity],
          ['advertising.field.businessEntity', data?.businessEntityName],
          ['advertising.field.contactPerson', agent.contactPerson],
          ['advertising.field.contactPhone', agent.contactPhone],
          ['advertising.field.status', data?.statusLabel],
          ['advertising.field.remark', agent.remark],
          ['advertising.field.createTime', fmtDateTime(agent.createTime)],
        ]),
      ];
    },
  },
};

const FALLBACK_CONFIG: AdDetailConfig = {
  title: 'advertising.contractDetail',
  fetch: () => Promise.resolve(null),
  build: () => [],
};

/** 依据当前详情路由名取配置 */
export function getAdDetailConfig(routeName: string): AdDetailConfig {
  return CONFIG_MAP[routeName] || FALLBACK_CONFIG;
}
