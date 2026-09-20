<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar :title="t('advertising.orderDetail')" left-arrow @click-left="back" />

    <div class="flex-1 overflow-auto">
      <template v-if="detail">
        <!-- 头部 -->
        <div class="bg-white p-[12px]">
          <div class="flex items-center justify-between gap-[8px]">
            <span class="one-line-text text-[16px] font-semibold text-[var(--text-n1)]">
              {{ detail.order.orderName || detail.order.orderNo }}
            </span>
            <CrmTag
              :tag="getAdOrderStatusLabel(detail.order.status)"
              v-bind="getAdOrderStatusTagStyle(detail.order.status)"
            />
          </div>
          <div class="mt-[4px] text-[12px] text-[var(--text-n3)]">{{ detail.order.orderNo || '-' }}</div>
          <div v-if="detail.order.needsRedInvoice === 1" class="mt-[8px]">
            <CrmTag
              :tag="t('advertising.order.detail.redInvoice')"
              bg-color="rgba(226, 46, 35, 0.12)"
              text-color="#E22E23"
            />
          </div>
        </div>

        <!-- 基本信息 / 投放 / 金额 / 收款 / 付款 / 其他 -->
        <div
          v-for="section of sections"
          :key="section.title"
          class="mx-[12px] mt-[8px] rounded-lg bg-white px-[12px] py-[6px]"
        >
          <div class="py-[6px] text-[14px] font-semibold text-[var(--text-n1)]">{{ section.title }}</div>
          <div
            v-for="item of section.items"
            :key="item.label"
            class="flex gap-[12px] border-t border-[var(--text-n8)] py-[7px] text-[13px]"
            @click="item.to && goSectionLink(item)"
          >
            <span class="w-[88px] flex-shrink-0 text-[var(--text-n3)]">{{ item.label }}</span>
            <template v-if="item.to">
              <span class="flex-1 break-all text-[13px] text-[#1989fa]">{{ item.value }}</span>
              <span class="flex-shrink-0 text-[16px] text-[var(--text-n4)]">›</span>
            </template>
            <span v-else class="flex-1 break-all text-[var(--text-n1)]">{{ item.value }}</span>
          </div>
        </div>

        <!-- 下游客户付款明细 -->
        <div v-if="payables.length" class="mx-[12px] mt-[8px] rounded-lg bg-white p-[12px]">
          <div class="mb-[8px] text-[14px] font-semibold text-[var(--text-n1)]">
            {{ t('advertising.order.detail.downstream') }}
          </div>
          <div
            v-for="(p, idx) of payables"
            :key="p.downstreamMediaId || idx"
            class="border-b border-[var(--text-n8)] py-[8px] last:border-b-0"
          >
            <div class="one-line-text text-[13px] font-medium text-[var(--text-n1)]">
              {{ p.downstreamMediaName || p.downstreamMediaId || '-' }}
            </div>
            <div class="mt-[4px] flex flex-wrap gap-x-[16px] gap-y-[2px] text-[12px] text-[var(--text-n3)]">
              <span>{{ t('advertising.order.payableAmount') }} {{ fmtAmount(p.payableAmount) }}</span>
              <span>{{ t('advertising.order.actualPayable') }} {{ fmtAmount(p.actualPayable) }}</span>
              <span>{{ t('advertising.order.paymentMethod') }} {{ getAdPaymentMethodLabel(p.paymentMethod) }}</span>
            </div>
          </div>
        </div>

        <!-- 附件 -->
        <div class="mx-[12px] mt-[8px] rounded-lg bg-white p-[12px]">
          <div class="mb-[4px] text-[14px] font-semibold text-[var(--text-n1)]">
            {{ t('advertising.order.detail.attachments') }}
          </div>
          <div v-if="!detail.attachments?.length" class="py-[8px] text-[13px] text-[var(--text-n3)]">
            {{ t('advertising.order.detail.empty') }}
          </div>
          <div v-else class="flex flex-col">
            <div
              v-for="att of detail.attachments"
              :key="att.id"
              class="flex items-center justify-between gap-[8px] border-b border-[var(--text-n8)] py-[8px] last:border-b-0"
            >
              <span
                class="one-line-text flex-1 text-[13px] text-[#1989fa]"
                @click="previewAttachment(att.fileUrl, att.fileName)"
              >
                {{ att.fileName || att.fileUrl || '-' }}
              </span>
              <span
                class="flex-shrink-0 text-[12px] text-[var(--text-n3)]"
                @click="downloadAttachment(att.fileUrl)"
              >
                {{ t('advertising.download') }}
              </span>
            </div>
          </div>
        </div>

        <!-- 改单记录 -->
        <div v-if="detail.changes?.length" class="mx-[12px] mt-[8px] rounded-lg bg-white p-[12px]">
          <div class="mb-[4px] text-[14px] font-semibold text-[var(--text-n1)]">
            {{ t('advertising.order.detail.changes') }}
          </div>
          <div
            v-for="c of detail.changes"
            :key="c.id"
            class="border-b border-[var(--text-n8)] py-[8px] last:border-b-0"
          >
            <div class="flex items-center justify-between gap-[8px]">
              <span class="text-[13px] text-[var(--text-n2)]">{{ fmtDateTime(c.createTime) }}</span>
              <CrmTag :tag="getAdOrderChangeStatusLabel(c.status)" />
            </div>
            <div v-if="c.reason" class="mt-[4px] text-[12px] text-[var(--text-n3)]">{{ c.reason }}</div>
          </div>
        </div>

        <!-- 操作记录 -->
        <div v-if="detail.logs?.length" class="mx-[12px] mt-[8px] mb-[16px] rounded-lg bg-white p-[12px]">
          <div class="mb-[4px] text-[14px] font-semibold text-[var(--text-n1)]">
            {{ t('advertising.order.detail.logs') }}
          </div>
          <div
            v-for="log of detail.logs"
            :key="log.id"
            class="flex items-center justify-between gap-[8px] border-b border-[var(--text-n8)] py-[8px] last:border-b-0"
          >
            <span class="one-line-text text-[13px] text-[var(--text-n2)]">{{ logActionLabel(log.action) }}</span>
            <span class="flex-shrink-0 text-[12px] text-[var(--text-n3)]">{{ fmtDateTime(log.createTime) }}</span>
          </div>
        </div>
      </template>
      <van-empty v-else-if="!loading" :description="t('common.noData')" />
    </div>

    <!-- 底部动作 -->
    <div
      v-if="actions.length"
      class="flex flex-wrap items-center gap-[8px] border-t border-[var(--text-n8)] bg-white p-[12px]"
    >
      <van-button
        v-for="action of actions"
        :key="action.key"
        size="small"
        :type="action.type"
        @click="action.handler"
      >
        {{ action.label }}
      </van-button>
    </div>

    <!-- 动作弹层（备注 / 坏账金额） -->
    <van-popup v-model:show="modalVisible" position="bottom" round>
      <div class="p-[16px]">
        <div class="mb-[12px] text-center text-[16px] font-semibold text-[var(--text-n1)]">
          {{ modalTitle }}
        </div>
        <van-field
          v-if="modalType === 'forceArchive'"
          v-model="badDebtAmount"
          type="number"
          :label="t('advertising.order.badDebtAmount')"
          :placeholder="t('advertising.order.detail.badDebtPlaceholder')"
        />
        <van-field
          v-else
          v-model="remark"
          type="textarea"
          rows="3"
          autosize
          :label="t('advertising.order.detail.reason')"
          :placeholder="t('advertising.order.detail.reasonPlaceholder')"
        />
        <div class="mt-[20px] flex gap-[12px]">
          <van-button block plain @click="modalVisible = false">{{ t('advertising.order.cancel') }}</van-button>
          <van-button block type="primary" @click="confirmModal">
            {{ t('advertising.order.confirm') }}
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 附件 iframe 预览（非图片类） -->
    <van-popup v-model:show="previewIframeVisible" position="bottom" round :style="{ height: '80%' }">
      <div class="flex h-full flex-col bg-white">
        <div class="flex items-center justify-between border-b border-[var(--text-n8)] p-[12px]">
          <span class="one-line-text flex-1 text-[14px] font-semibold text-[var(--text-n1)]">
            {{ previewIframeName || t('advertising.preview') }}
          </span>
          <span
            class="ml-[12px] flex-shrink-0 text-[13px] text-[#1989fa]"
            @click="downloadAttachment(previewIframeFileUrl)"
          >
            {{ t('advertising.download') }}
          </span>
        </div>
        <iframe :src="previewIframeUrl" class="flex-1 w-full border-0 bg-white" />
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { showSuccessToast, showToast } from 'vant';
  import {
    getAdOrderChangeStatusLabel,
    getAdOrderStatusLabel,
    getAdOrderTypeLabel,
    getAdPaymentMethodLabel,
    getAdReceiptMethodLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderDetail } from '@lib/shared/models/advertising';

  import CrmTag from '@/components/pure/crm-tag/index.vue';
  import {
    approveAdOrder,
    confirmExecuteAdOrder,
    forceArchiveAdOrder,
    getAdDictPage,
    getAdOrderDetail,
    rejectAdOrder,
    submitAdOrder,
    voidAdOrder,
  } from '@/api/modules';
  import { hasPermission } from '@/utils/permission';
  import { AdvertisingRouteEnum } from '@/enums/routeEnum';
  import {
    downloadAttachment,
    previewAttachment,
    useAttachmentPreview,
  } from '@/views/advertising/attachment';
  import { fmtAmount, fmtDate, fmtDateTime, getAdOperationLogActionKey, getAdOrderStatusTagStyle } from '@/views/advertising/utils';

  type ModalType = 'approve' | 'reject' | 'void' | 'forceArchive' | '';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  const loading = ref(false);
  const detail = ref<AdOrderDetail | null>(null);

  const modalVisible = ref(false);
  const modalType = ref<ModalType>('');
  const remark = ref('');
  const badDebtAmount = ref('');

  const orderId = computed(() => (route.query.id as string) || '');

  const { previewIframeVisible, previewIframeUrl, previewIframeName, previewIframeFileUrl } =
    useAttachmentPreview();

  /** 列表跳转时携带的名称（详情接口不返回名称字段） */
  function queryName(key: string): string {
    return (route.query[key] as string) || '-';
  }

  const payables = computed(() => detail.value?.downstreamMediaPayables || []);

  const downstreamNames = computed(() => {
    const names = payables.value.map((p) => p.downstreamMediaName).filter(Boolean);
    return names.length ? names.join('、') : '-';
  });

  const contractText = computed(() => {
    const list = [detail.value?.contractNo, detail.value?.contractName].filter(Boolean);
    return list.length ? list.join(' ') : '-';
  });

  /** 操作记录行为：映射为中文；未命中 i18n 时回退原始动作串 */
  function logActionLabel(action?: string | null): string {
    const key = getAdOperationLogActionKey(action);
    if (!key) return '-';
    const label = t(key);
    return label !== key ? label : (action as string);
  }

  // 行业类别字典缓存：dictValue → dictLabel（对齐 web 订单详情）
  const industryLabelMap = ref<Record<string, string>>({});
  async function loadIndustryDict() {
    try {
      const res: any = await getAdDictPage({
        keyword: '',
        pageSize: 200,
        current: 1,
        dictCode: 'industry',
      });
      (res.list || []).forEach((it: any) => {
        const value = it.dictValue ?? it.id;
        if (value != null) industryLabelMap.value[value] = it.dictLabel || value;
      });
    } catch {
      // ignore
    }
  }
  function industryLabel(code?: string | number | null): string {
    if (code === null || code === undefined || code === '') return '-';
    const key = String(code);
    return industryLabelMap.value[key] || key;
  }

  const sections = computed(() => {
    const order = detail.value?.order;
    if (!order)
      return [] as {
        title: string;
        items: { label: string; value: string | number; to?: { name: string; query: Record<string, string> } }[];
      }[];
    type Row = [string, string | number | undefined, { name: string; query: Record<string, string> }?];
    const build = (title: string, pairs: Row[]) => ({
      title,
      items: pairs.map(([label, value, to]) => ({ label, value: value ?? '-', to })),
    });
    // 收款信息：仅展示所选收款方式对应的字段（避免预收/账期字段混排）
    const receiptRows: Row[] = [
      [t('advertising.order.receiptMethod'), getAdReceiptMethodLabel(order.receiptMethod)],
    ];
    if (order.receiptMethod === 10) {
      receiptRows.push(
        [
          t('advertising.order.receiptPrepayRatio'),
          order.receiptPrepayRatio != null ? `${order.receiptPrepayRatio}%` : '-',
        ],
        [t('advertising.order.receiptPrepayAmount'), fmtAmount(order.receiptPrepayAmount)],
        [t('advertising.order.receiptPrepayDeadline'), fmtDate(order.receiptPrepayDeadline)]
      );
    } else if (order.receiptMethod === 20) {
      receiptRows.push([t('advertising.order.receiptAccountPeriodDays'), order.receiptAccountPeriodDays]);
    }
    receiptRows.push([t('advertising.order.receivedAmount'), fmtAmount(order.receivedAmount)]);

    return [
      build(t('advertising.order.detail.base'), [
        [t('advertising.order.no'), order.orderNo],
        [t('advertising.orderName'), order.orderName],
        [t('advertising.order.orderType'), getAdOrderTypeLabel(order.orderType)],
        [t('advertising.customer'), queryName('customerName')],
        [t('advertising.order.businessEntity'), queryName('businessEntityName')],
        [t('advertising.order.upstreamAgent'), queryName('upstreamAgentName')],
        [t('advertising.order.agentOrderNo'), order.agentOrderNo],
        [t('advertising.order.downstream'), downstreamNames.value],
        [
          t('advertising.order.contract'),
          contractText.value,
          detail.value?.contractId
            ? { name: AdvertisingRouteEnum.AD_CONTRACT_DETAIL, query: { id: detail.value.contractId } }
            : undefined,
        ],
        [t('advertising.order.signingEntity'), order.signingEntity],
        [t('advertising.order.industry'), industryLabel(order.industryCode)],
        [t('advertising.order.currency'), order.currency],
      ]),
      build(t('advertising.order.detail.delivery'), [
        [t('advertising.order.deliveryStart'), fmtDate(order.deliveryStartDate)],
        [t('advertising.order.deliveryEnd'), fmtDate(order.deliveryEndDate)],
        [t('advertising.order.deliveryVolume'), order.deliveryVolume],
      ]),
      build(t('advertising.order.detail.amount'), [
        [t('advertising.order.totalAmount'), fmtAmount(order.totalAmount)],
        [t('advertising.order.noRebateAmount'), fmtAmount(order.noRebateAmount)],
        [t('advertising.order.rebateAmount'), fmtAmount(order.rebateAmount)],
        [t('advertising.order.receivableAmount'), fmtAmount(order.receivableAmount)],
        [t('advertising.order.mediaPayable'), fmtAmount(order.mediaPayableAmount)],
        [t('advertising.order.actualMediaPayable'), fmtAmount(order.actualMediaPayableAmount)],
        [t('advertising.order.mediaRebate'), fmtAmount(order.mediaRebateAmount)],
        [t('advertising.order.orderIncome'), fmtAmount(order.orderIncomeAmount)],
      ]),
      build(t('advertising.order.detail.receipt'), receiptRows),
      build(t('advertising.order.detail.other'), [
        [t('advertising.order.creator'), queryName('creatorName')],
        [t('advertising.order.createTime'), fmtDateTime(order.createTime)],
        [t('advertising.order.remark'), order.remark],
      ]),
    ];
  });

  const modalTitle = computed(() => {
    switch (modalType.value) {
      case 'approve':
        return t('advertising.order.detail.approve');
      case 'reject':
        return t('advertising.order.detail.reject');
      case 'void':
        return t('advertising.order.detail.void');
      case 'forceArchive':
        return t('advertising.order.detail.forceArchive');
      default:
        return '';
    }
  });

  const actions = computed(() => {
    const status = detail.value?.order?.status;
    const list: { key: string; label: string; type: 'primary' | 'danger' | 'warning' | 'default'; handler: () => void }[] =
      [];
    if (status === 0 && hasPermission('AD_ORDER:SUBMIT')) {
      list.push({ key: 'submit', label: t('advertising.order.detail.submit'), type: 'primary', handler: handleSubmit });
    }
    if (status === 0 && hasPermission('AD_ORDER:CREATE')) {
      list.push({ key: 'edit', label: t('advertising.order.edit'), type: 'default', handler: goEdit });
    }
    if (status === 10 && hasPermission('AD_ORDER:APPROVE')) {
      list.push({
        key: 'approve',
        label: t('advertising.order.detail.approve'),
        type: 'primary',
        handler: () => openModal('approve'),
      });
    }
    if (status === 10 && hasPermission('AD_ORDER:REJECT')) {
      list.push({
        key: 'reject',
        label: t('advertising.order.detail.reject'),
        type: 'default',
        handler: () => openModal('reject'),
      });
    }
    if (status === 45 && hasPermission('AD_ORDER:CONFIRM_EXECUTE')) {
      list.push({
        key: 'confirmExecute',
        label: t('advertising.order.detail.confirmExecute'),
        type: 'primary',
        handler: handleConfirmExecute,
      });
    }
    if (status === 80 && hasPermission('AD_ORDER:FORCE_ARCHIVE')) {
      list.push({
        key: 'forceArchive',
        label: t('advertising.order.detail.forceArchive'),
        type: 'danger',
        handler: () => openModal('forceArchive'),
      });
    }
    if (status !== 90 && status !== 100 && hasPermission('AD_ORDER:VOID')) {
      list.push({
        key: 'void',
        label: t('advertising.order.detail.void'),
        type: 'warning',
        handler: () => openModal('void'),
      });
    }
    return list;
  });

  async function load() {
    if (!orderId.value) return;
    loading.value = true;
    try {
      const [detailRes] = await Promise.all([getAdOrderDetail(orderId.value), loadIndustryDict()]);
      detail.value = detailRes;
    } finally {
      loading.value = false;
    }
  }

  function back() {
    router.back();
  }

  function goEdit() {
    router.push({ name: AdvertisingRouteEnum.AD_ORDER_FORM, query: { id: orderId.value } });
  }

  /** 基本信息区块中带 to 的字段（如关联合同）点击跳详情，对齐 web 超链接 */
  function goSectionLink(item: { to?: { name: string; query: Record<string, string> } }) {
    if (item.to) router.push({ name: item.to.name, query: item.to.query });
  }

  function openModal(type: ModalType) {
    modalType.value = type;
    remark.value = '';
    badDebtAmount.value = '';
    modalVisible.value = true;
  }

  async function handleSubmit() {
    await submitAdOrder(orderId.value);
    showSuccessToast(t('advertising.order.detail.submitSuccess'));
    load();
  }

  async function handleConfirmExecute() {
    await confirmExecuteAdOrder(orderId.value);
    showSuccessToast(t('advertising.order.detail.confirmSuccess'));
    load();
  }

  async function confirmModal() {
    const type = modalType.value;
    if (type === 'reject' && !remark.value.trim()) {
      showToast(t('advertising.order.detail.rejectReasonRequired'));
      return;
    }
    if (type === 'forceArchive' && !badDebtAmount.value.trim()) {
      showToast(t('advertising.order.detail.badDebtRequired'));
      return;
    }
    if (type === 'approve') {
      await approveAdOrder(orderId.value, { action: 'approve', remark: remark.value });
      showSuccessToast(t('advertising.order.detail.approveSuccess'));
    } else if (type === 'reject') {
      await rejectAdOrder(orderId.value, { action: 'reject', remark: remark.value });
      showSuccessToast(t('advertising.order.detail.rejectSuccess'));
    } else if (type === 'void') {
      await voidAdOrder(orderId.value, { reason: remark.value });
      showSuccessToast(t('advertising.order.detail.voidSuccess'));
    } else if (type === 'forceArchive') {
      await forceArchiveAdOrder(orderId.value, { badDebtAmount: Number(badDebtAmount.value) || 0 });
      showSuccessToast(t('advertising.order.detail.archiveSuccess'));
    }
    modalVisible.value = false;
    load();
  }

  onMounted(load);
</script>
