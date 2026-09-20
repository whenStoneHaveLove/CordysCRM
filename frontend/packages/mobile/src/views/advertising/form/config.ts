import {
  AdBusinessEntityStatusOptions,
  AdContractDirectionEnum,
  AdContractDirectionOptions,
  AdContractTypeOptions,
  AdCustomerLevelOptions,
  AdCustomerStatusOptions,
  AdReceiptTypeOptions,
  AdRelatedPartyTypeEnum,
  AdRelatedPartyTypeOptions,
  AdResourceStatusOptions,
  AdSealTypeOptions,
} from '@lib/shared/enums/advertisingEnum';

import {
  applyAdSeal,
  createAdBusinessEntity,
  createAdContract,
  createAdCustomer,
  createAdDownstreamMedia,
  createAdReceipt,
  createAdUpstreamAgent,
  getAdBusinessEntityDetail,
  getAdBusinessEntityPage,
  getAdContractDetail,
  getAdContractPage,
  getAdCustomerDetail,
  getAdCustomerPage,
  getAdDictPage,
  getAdDownstreamMediaDetail,
  getAdDownstreamMediaPage,
  getAdOrderPage,
  getAdReceiptDetail,
  getAdReceiptRemaining,
  getAdSealDetail,
  getAdUpstreamAgentDetail,
  getAdUpstreamAgentPage,
  updateAdBusinessEntity,
  updateAdContract,
  updateAdCustomer,
  updateAdDownstreamMedia,
  updateAdReceipt,
  updateAdUpstreamAgent,
  uploadTempAttachment,
} from '@/api/modules';

import { fmtAmount } from '@/views/advertising/utils';

export interface AdFormField {
  field: string;
  label: string; // i18n key
  type: 'text' | 'number' | 'textarea' | 'date' | 'select' | 'enum' | 'multi' | 'switch';
  required?: boolean;
  options?: { label: string; value: number }[]; // enum
  apiKey?: string; // select / multi（静态）
  /** 动态下拉：当 dependsOn 字段变化时，按其值解析数据源 */
  dependsOn?: string;
  /** 动态枚举：依据其它字段值过滤选项（如合同方向过滤关联方类型） */
  filter?: (model: Record<string, any>) => { label: string; value: number }[];
  placeholder?: string;
}

export interface AdFormGroup {
  title?: string;
  fields: AdFormField[];
}

export interface AdFormSelectApi {
  api: (p: any) => Promise<any>;
  labelKey?: string;
  valueKey?: string;
  /** 选项文案提取（优先于 labelKey），口径对齐 web */
  text?: (item: any) => string;
}

export interface AdFormAttachmentConfig {
  type: number;
  /** 临时上传，返回临时文件ID */
  uploadTemp: (file: File) => Promise<any>;
  read: (res: any) => { id: string; name: string; fileUrl?: string }[];
  buildSealFileUrls: (items: { id: string; name: string }[]) => any[];
}

export interface AdFormConfig {
  titleCreate: string;
  titleEdit: string;
  selectApis: Record<string, AdFormSelectApi>;
  fetchDetail: (id: string) => Promise<any>;
  mapDetail: (res: any) => Record<string, any>;
  buildPayload: (model: Record<string, any>, attachments?: any) => any;
  /** 返回错误提示文案（已是完整句子）或 null */
  validate: (model: Record<string, any>, t: (k: string) => string) => string | null;
  groups: AdFormGroup[];
  dynamicOptions?: { field: string; dependsOn: string; resolver: (dep: any) => AdFormSelectApi | null };
  resetOn?: { field: string; clear: string[] }[];
  /** 选择某字段后触发（如选订单带出剩余可收） */
  afterSelect?: Record<string, (value: any, model: Record<string, any>) => void | Promise<void>>;
  /** 顶部提示文案（如剩余可收金额） */
  hint?: (model: Record<string, any>, t: (k: string) => string) => string;
  create: (p: any) => Promise<any>;
  update: (p: any) => Promise<any>;
  attachments?: AdFormAttachmentConfig;
}

function toNum(v?: number | string | null): number | null {
  if (v === undefined || v === null || v === '') return null;
  if (typeof v === 'number') return v;
  const parsed = Date.parse(v);
  return Number.isNaN(parsed) ? null : parsed;
}

function numOrUndef(v: any): number | undefined {
  return v === '' || v === null || v === undefined ? undefined : Number(v);
}

function req(model: Record<string, any>, field: string, labelKey: string, t: (k: string) => string): string | null {
  const v = model[field];
  if (v === undefined || v === null || v === '') return `${t(labelKey)} ${t('advertising.form.required')}`;
  return null;
}

export const AD_FORM_CONFIG: Record<string, AdFormConfig> = {
  customer: {
    titleCreate: 'advertising.customer.form.title.create',
    titleEdit: 'advertising.customer.form.title.edit',
    selectApis: {
      industry: {
        api: () => getAdDictPage({ keyword: '', pageSize: 200, current: 1, dictCode: 'industry' }),
        labelKey: 'dictLabel',
        valueKey: 'dictValue',
        text: (i) => i.dictLabel || i.dictValue,
      },
    },
    fetchDetail: getAdCustomerDetail,
    mapDetail: (res) => {
      const o = res.customer || {};
      return {
        id: o.id,
        customerName: o.name ?? o.customerName,
        brand: o.brand,
        industryCode: o.industryCode,
        signingEntity: o.signingEntity,
        contactPerson: o.contactPerson,
        contactPhone: o.contactPhone,
        email: o.email,
        address: o.address,
        customerLevel: o.customerLevel ?? 20,
        status: o.status ?? 0,
        remark: o.remark,
      };
    },
    buildPayload: (model) => ({
      customerName: model.customerName,
      brand: model.brand,
      industryCode: model.industryCode,
      signingEntity: model.signingEntity,
      contactPerson: model.contactPerson,
      contactPhone: model.contactPhone,
      email: model.email,
      address: model.address,
      customerLevel: numOrUndef(model.customerLevel),
      status: numOrUndef(model.status),
      remark: model.remark,
    }),
    validate: (model, t) => req(model, 'customerName', 'advertising.customer.form.name', t),
    groups: [
      {
        fields: [
          { field: 'customerName', label: 'advertising.customer.form.name', type: 'text', required: true },
          { field: 'brand', label: 'advertising.customer.form.brand', type: 'text' },
          {
            field: 'industryCode',
            label: 'advertising.customer.form.industryCode',
            type: 'select',
            apiKey: 'industry',
          },
          { field: 'signingEntity', label: 'advertising.customer.form.signingEntity', type: 'text' },
          { field: 'contactPerson', label: 'advertising.customer.form.contactPerson', type: 'text' },
          { field: 'contactPhone', label: 'advertising.customer.form.contactPhone', type: 'text' },
          { field: 'email', label: 'advertising.customer.form.email', type: 'text' },
          { field: 'address', label: 'advertising.customer.form.address', type: 'text' },
          {
            field: 'customerLevel',
            label: 'advertising.customer.form.customerLevel',
            type: 'enum',
            options: AdCustomerLevelOptions,
          },
          {
            field: 'status',
            label: 'advertising.customer.form.status',
            type: 'enum',
            options: AdCustomerStatusOptions,
          },
          { field: 'remark', label: 'advertising.customer.form.remark', type: 'textarea' },
        ],
      },
    ],
    create: createAdCustomer,
    update: updateAdCustomer,
  },

  businessEntity: {
    titleCreate: 'advertising.businessEntity.form.title.create',
    titleEdit: 'advertising.businessEntity.form.title.edit',
    selectApis: {},
    fetchDetail: getAdBusinessEntityDetail,
    mapDetail: (res) => {
      const o = res.entity || {};
      return {
        id: o.id,
        name: o.name,
        code: o.code,
        status: o.status ?? 10,
        isCrossEntity: o.isCrossEntity ?? 0,
        remark: o.remark,
      };
    },
    buildPayload: (model) => ({
      name: model.name,
      code: model.code,
      status: numOrUndef(model.status) ?? 10,
      isCrossEntity: model.isCrossEntity ?? 0,
      remark: model.remark,
    }),
    validate: (model, t) => {
      const e1 = req(model, 'name', 'advertising.businessEntity.form.name', t);
      if (e1) return e1;
      return req(model, 'code', 'advertising.businessEntity.form.code', t);
    },
    groups: [
      {
        fields: [
          { field: 'name', label: 'advertising.businessEntity.form.name', type: 'text', required: true },
          { field: 'code', label: 'advertising.businessEntity.form.code', type: 'text', required: true },
          {
            field: 'status',
            label: 'advertising.businessEntity.form.status',
            type: 'enum',
            options: AdBusinessEntityStatusOptions,
          },
          { field: 'isCrossEntity', label: 'advertising.businessEntity.form.isCrossEntity', type: 'switch' },
          { field: 'remark', label: 'advertising.businessEntity.form.remark', type: 'textarea' },
        ],
      },
    ],
    create: createAdBusinessEntity,
    update: updateAdBusinessEntity,
  },

  contract: {
    titleCreate: 'advertising.contract.form.title.create',
    titleEdit: 'advertising.contract.form.title.edit',
    selectApis: {
      businessEntityId: { api: getAdBusinessEntityPage, text: (i) => i.name || i.id },
      orderIds: {
        api: getAdOrderPage,
        labelKey: 'orderName',
        text: (i) => [i.orderNo, i.orderName].filter(Boolean).join(' ') || i.id,
      },
    },
    fetchDetail: getAdContractDetail,
    mapDetail: (res) => {
      const o = res.contract || {};
      return {
        id: o.id,
        contractNo: o.contractNo,
        contractName: o.contractName,
        businessEntityId: o.businessEntityId,
        contractDirection: o.contractDirection ?? null,
        contractType: o.contractType ?? null,
        relatedPartyType: o.relatedPartyType ?? null,
        relatedPartyId: o.relatedPartyId,
        orderIds: (res.orderList || []).map((it: any) => it.orderId).filter(Boolean),
        signingEntity: o.signingEntity,
        validFrom: toNum(o.validFrom),
        validTo: toNum(o.validTo),
        amount: o.amount ?? null,
        rebateTerms: o.rebateTerms,
      };
    },
    buildPayload: (model, attachments) => ({
      contractNo: model.contractNo,
      contractName: model.contractName,
      businessEntityId: model.businessEntityId,
      contractDirection: numOrUndef(model.contractDirection),
      contractType: numOrUndef(model.contractType),
      relatedPartyType: numOrUndef(model.relatedPartyType),
      relatedPartyId: model.relatedPartyId,
      orderIds: model.orderIds || [],
      signingEntity: model.signingEntity,
      validFrom: model.validFrom ?? undefined,
      validTo: model.validTo ?? undefined,
      amount: numOrUndef(model.amount),
      rebateTerms: model.rebateTerms,
      sealFileUrls: attachments || [],
    }),
    validate: (model, t) => {
      const checks: [string, string][] = [
        ['contractName', 'advertising.contract.form.contractName'],
        ['businessEntityId', 'advertising.contract.form.businessEntityId'],
        ['contractDirection', 'advertising.contract.form.contractDirection'],
        ['contractType', 'advertising.contract.form.contractType'],
        ['relatedPartyType', 'advertising.contract.form.relatedPartyType'],
        ['relatedPartyId', 'advertising.contract.form.relatedPartyId'],
      ];
      const hit = checks.find(([field]) => {
        const v = model[field];
        return v === undefined || v === null || v === '';
      });
      return hit ? req(model, hit[0], hit[1], t) : null;
    },
    groups: [
      {
        fields: [
          { field: 'contractNo', label: 'advertising.contract.form.contractNo', type: 'text' },
          { field: 'contractName', label: 'advertising.contract.form.contractName', type: 'text', required: true },
          {
            field: 'businessEntityId',
            label: 'advertising.contract.form.businessEntityId',
            type: 'select',
            apiKey: 'businessEntityId',
            required: true,
          },
          {
            field: 'contractDirection',
            label: 'advertising.contract.form.contractDirection',
            type: 'enum',
            options: AdContractDirectionOptions,
            required: true,
          },
          {
            field: 'contractType',
            label: 'advertising.contract.form.contractType',
            type: 'enum',
            options: AdContractTypeOptions,
            required: true,
          },
          {
            field: 'relatedPartyType',
            label: 'advertising.contract.form.relatedPartyType',
            type: 'enum',
            options: AdRelatedPartyTypeOptions,
            required: true,
            filter: (m) => {
              if (m.contractDirection === AdContractDirectionEnum.UPSTREAM) {
                return AdRelatedPartyTypeOptions.filter(
                  (it) =>
                    it.value === AdRelatedPartyTypeEnum.CUSTOMER || it.value === AdRelatedPartyTypeEnum.UPSTREAM_AGENT
                );
              }
              if (m.contractDirection === AdContractDirectionEnum.DOWNSTREAM) {
                return AdRelatedPartyTypeOptions.filter((it) => it.value === AdRelatedPartyTypeEnum.DOWNSTREAM_MEDIA);
              }
              return AdRelatedPartyTypeOptions;
            },
          },
          {
            field: 'relatedPartyId',
            label: 'advertising.contract.form.relatedPartyId',
            type: 'select',
            apiKey: 'relatedPartyId',
            required: true,
            dependsOn: 'relatedPartyType',
          },
          { field: 'orderIds', label: 'advertising.contract.form.orderId', type: 'multi', apiKey: 'orderIds' },
        ],
      },
      {
        title: 'advertising.order.detail.amount',
        fields: [
          { field: 'amount', label: 'advertising.contract.form.amount', type: 'number' },
          { field: 'signingEntity', label: 'advertising.contract.form.signingEntity', type: 'text' },
          { field: 'validFrom', label: 'advertising.contract.form.validFrom', type: 'date' },
          { field: 'validTo', label: 'advertising.contract.form.validTo', type: 'date' },
        ],
      },
      {
        fields: [{ field: 'rebateTerms', label: 'advertising.contract.form.rebateTerms', type: 'textarea' }],
      },
    ],
    dynamicOptions: {
      field: 'relatedPartyId',
      dependsOn: 'relatedPartyType',
      resolver: (dep) => {
        if (dep === AdRelatedPartyTypeEnum.CUSTOMER) {
          return { api: getAdCustomerPage, text: (i) => i.customerName || i.name || i.id };
        }
        if (dep === AdRelatedPartyTypeEnum.UPSTREAM_AGENT) {
          return { api: getAdUpstreamAgentPage, text: (i) => i.resourceName || i.name || i.id };
        }
        if (dep === AdRelatedPartyTypeEnum.DOWNSTREAM_MEDIA) {
          return { api: getAdDownstreamMediaPage, text: (i) => i.resourceName || i.name || i.id };
        }
        return null;
      },
    },
    resetOn: [
      { field: 'contractDirection', clear: ['relatedPartyType', 'relatedPartyId'] },
      { field: 'relatedPartyType', clear: ['relatedPartyId'] },
    ],
    create: createAdContract,
    update: updateAdContract,
    attachments: {
      type: 10,
      uploadTemp: (file) => uploadTempAttachment(file),
      read: (res) =>
        (res.attachments || [])
          .filter((a: any) => a.type === 10)
          .map((a: any) => ({ id: a.fileUrl, name: a.fileName, fileUrl: a.fileUrl })),
      buildSealFileUrls: (items) => items.map((i) => ({ tempFileId: i.id, fileName: i.name })),
    },
  },

  upstreamAgent: {
    titleCreate: 'advertising.upstreamAgent.form.title.create',
    titleEdit: 'advertising.upstreamAgent.form.title.edit',
    selectApis: {},
    fetchDetail: getAdUpstreamAgentDetail,
    mapDetail: (res) => {
      const o = (res as any)?.agent || (res as any)?.entity || res || {};
      return {
        id: o.id,
        name: o.name,
        creditCode: o.creditCode,
        signingEntity: o.signingEntity,
        contactPerson: o.contactPerson,
        contactPhone: o.contactPhone,
        remark: o.remark,
      };
    },
    buildPayload: (model) => ({
      name: model.name,
      creditCode: model.creditCode,
      signingEntity: model.signingEntity,
      contactPerson: model.contactPerson,
      contactPhone: model.contactPhone,
      remark: model.remark,
    }),
    validate: (model, t) => req(model, 'name', 'advertising.upstreamAgent.form.name', t),
    groups: [
      {
        fields: [
          { field: 'name', label: 'advertising.upstreamAgent.form.name', type: 'text', required: true },
          { field: 'creditCode', label: 'advertising.upstreamAgent.form.creditCode', type: 'text' },
          { field: 'signingEntity', label: 'advertising.upstreamAgent.form.signingEntity', type: 'text' },
          { field: 'contactPerson', label: 'advertising.upstreamAgent.form.contactPerson', type: 'text' },
          { field: 'contactPhone', label: 'advertising.upstreamAgent.form.contactPhone', type: 'text' },
          { field: 'remark', label: 'advertising.upstreamAgent.form.remark', type: 'textarea' },
        ],
      },
    ],
    create: createAdUpstreamAgent,
    update: updateAdUpstreamAgent,
  },

  receipt: {
    titleCreate: 'advertising.receipt.form.title.create',
    titleEdit: 'advertising.receipt.form.title.edit',
    selectApis: {
      orderId: {
        api: () => getAdOrderPage({ keyword: '', pageSize: 200, current: 1, statusList: [45, 50, 80] }),
        text: (i) => [i.orderNo, i.orderName].filter(Boolean).join(' ') || i.id,
      },
    },
    fetchDetail: getAdReceiptDetail,
    mapDetail: (res) => {
      const o = (res as any)?.receipt || (res as any)?.entity || res || {};
      return {
        id: o.id,
        orderId: o.orderId,
        amount: o.amount ?? null,
        receiptTime: toNum(o.receiptTime),
        type: o.type ?? 10,
        remark: o.remark,
      };
    },
    buildPayload: (model) => ({
      orderId: model.orderId,
      amount: numOrUndef(model.amount),
      receiptTime: model.receiptTime ?? undefined,
      type: numOrUndef(model.type) ?? 10,
      remark: model.remark,
    }),
    validate: (model, t) => {
      if (!model.orderId) return `${t('advertising.receipt.form.orderId')} ${t('advertising.form.required')}`;
      if (!model.amount || model.amount <= 0) return t('advertising.receipt.form.amountRequired');
      return null;
    },
    groups: [
      {
        fields: [
          {
            field: 'orderId',
            label: 'advertising.receipt.form.orderId',
            type: 'select',
            apiKey: 'orderId',
            required: true,
          },
          { field: 'amount', label: 'advertising.receipt.form.amount', type: 'number', required: true },
          { field: 'receiptTime', label: 'advertising.receipt.form.receiptTime', type: 'date' },
          { field: 'type', label: 'advertising.receipt.form.type', type: 'enum', options: AdReceiptTypeOptions },
          { field: 'remark', label: 'advertising.receipt.form.remark', type: 'textarea' },
        ],
      },
    ],
    afterSelect: {
      orderId: async (val, m) => {
        try {
          const r = await getAdReceiptRemaining(val);
          if (r != null && (m.amount === '' || m.amount == null)) m.amount = r;
          m._remaining = r;
        } catch {
          /* ignore */
        }
      },
    },
    hint: (m, t) =>
      m._remaining != null ? `${t('advertising.receipt.form.remaining')}：${fmtAmount(m._remaining)}` : '',
    create: createAdReceipt,
    update: updateAdReceipt,
  },

  seal: {
    titleCreate: 'advertising.seal.apply',
    titleEdit: 'advertising.seal.apply',
    selectApis: {
      contractId: {
        api: () =>
          Promise.all([
            getAdContractPage({ keyword: '', pageSize: 200, current: 1, status: 10, sealStatus: 0 }),
            getAdContractPage({ keyword: '', pageSize: 200, current: 1, status: 10, sealStatus: 30 }),
          ]).then(([a, b]) => ({
            list: [...((a as any).list || (a as any).records || []), ...((b as any).list || (b as any).records || [])],
          })),
        text: (i) => `${i.contractNo} - ${i.contractName}`,
      },
    },
    fetchDetail: getAdSealDetail,
    mapDetail: (res) => {
      const o = (res as any)?.record || (res as any)?.entity || res || {};
      return {
        id: o.id,
        contractId: o.contractId,
        sealType: o.sealType ?? null,
        appliedCopies: o.appliedCopies ?? 1,
        applyRemark: o.applyRemark,
      };
    },
    buildPayload: (model) => ({
      contractId: model.contractId,
      sealType: numOrUndef(model.sealType),
      appliedCopies: numOrUndef(model.appliedCopies),
      applyRemark: model.applyRemark,
    }),
    validate: (model, t) => {
      if (!model.contractId) return `${t('advertising.seal.form.contractId')} ${t('advertising.form.required')}`;
      if (model.sealType === null || model.sealType === undefined)
        return `${t('advertising.seal.form.sealType')} ${t('advertising.form.required')}`;
      if (!model.appliedCopies || model.appliedCopies < 1)
        return `${t('advertising.seal.form.appliedCopies')} ${t('advertising.form.required')}`;
      return null;
    },
    groups: [
      {
        fields: [
          {
            field: 'contractId',
            label: 'advertising.seal.form.contractId',
            type: 'select',
            apiKey: 'contractId',
            required: true,
          },
          {
            field: 'sealType',
            label: 'advertising.seal.form.sealType',
            type: 'enum',
            options: AdSealTypeOptions,
            required: true,
          },
          { field: 'appliedCopies', label: 'advertising.seal.form.appliedCopies', type: 'number', required: true },
          { field: 'applyRemark', label: 'advertising.seal.form.applyRemark', type: 'textarea' },
        ],
      },
    ],
    create: applyAdSeal,
    update: applyAdSeal,
  },

  downstreamMedia: {
    titleCreate: 'advertising.downstreamMedia.form.title.create',
    titleEdit: 'advertising.downstreamMedia.form.title.edit',
    selectApis: {
      mediaType: {
        api: () => getAdDictPage({ keyword: '', pageSize: 200, current: 1, dictCode: 'media_type' }),
        labelKey: 'dictLabel',
        valueKey: 'dictValue',
        text: (i) => i.dictLabel || i.dictValue,
      },
    },
    fetchDetail: getAdDownstreamMediaDetail,
    mapDetail: (res) => {
      const o = (res as any)?.media || (res as any)?.entity || res || {};
      return {
        id: o.id,
        name: o.name,
        mediaType: o.mediaType ?? null,
        channel: o.channel,
        rateCard: o.rateCard,
        discountPolicy: o.discountPolicy,
        contactPerson: o.contactPerson,
        contactPhone: o.contactPhone,
        cooperationStatus: o.cooperationStatus ?? 10,
      };
    },
    buildPayload: (model) => ({
      name: model.name,
      mediaType: numOrUndef(model.mediaType),
      channel: model.channel,
      rateCard: model.rateCard,
      discountPolicy: model.discountPolicy,
      contactPerson: model.contactPerson,
      contactPhone: model.contactPhone,
      cooperationStatus: numOrUndef(model.cooperationStatus) ?? 10,
    }),
    validate: (model, t) => req(model, 'name', 'advertising.downstreamMedia.form.name', t),
    groups: [
      {
        fields: [
          { field: 'name', label: 'advertising.downstreamMedia.form.name', type: 'text', required: true },
          {
            field: 'mediaType',
            label: 'advertising.downstreamMedia.form.mediaType',
            type: 'select',
            apiKey: 'mediaType',
          },
          { field: 'channel', label: 'advertising.downstreamMedia.form.channel', type: 'text' },
          { field: 'rateCard', label: 'advertising.downstreamMedia.form.rateCard', type: 'text' },
          { field: 'discountPolicy', label: 'advertising.downstreamMedia.form.discountPolicy', type: 'text' },
          { field: 'contactPerson', label: 'advertising.downstreamMedia.form.contactPerson', type: 'text' },
          { field: 'contactPhone', label: 'advertising.downstreamMedia.form.contactPhone', type: 'text' },
          {
            field: 'cooperationStatus',
            label: 'advertising.downstreamMedia.form.cooperationStatus',
            type: 'enum',
            options: AdResourceStatusOptions,
          },
        ],
      },
    ],
    create: createAdDownstreamMedia,
    update: updateAdDownstreamMedia,
  },
};
