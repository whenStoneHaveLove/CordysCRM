<template>
  <div class="advertising-page">
    <n-card :bordered="false" :title="t('advertising.payment.media')">
      <n-space align="end" class="mb-4">
        <n-select
          v-model:value="selectedOrderId"
          :options="orderOptions"
          :loading="orderLoading"
          filterable
          placeholder="选择关联订单"
          style="width: 360px"
        />
      </n-space>
      <n-grid :cols="3" :x-gap="16" :y-gap="16">
        <n-grid-item v-for="a in actions" :key="a.type">
          <n-card size="small" hoverable>
            <n-space vertical align="center">
              <span>{{ a.label }}</span>
              <n-button type="primary" size="small" @click="openMedia(a)">执行</n-button>
            </n-space>
          </n-card>
        </n-grid-item>
      </n-grid>
      <n-empty v-if="!selectedOrderId" class="mt-4" description="请先选择关联订单" />
    </n-card>

    <n-modal
      v-model:show="mediaAction.show"
      :title="mediaAction.title"
      preset="card"
      positive-text="确定"
      negative-text="取消"
      style="width: 480px"
      @positive-click="handleMediaConfirm"
    >
      <n-form label-placement="left" label-width="100px">
        <n-form-item v-if="mediaAction.fields.includes('amount')" label="金额">
          <n-input-number
            v-model:value="mediaAction.amount"
            :min="0"
            :precision="2"
            :show-button="false"
            style="width: 100%"
          />
        </n-form-item>
        <n-form-item v-if="mediaAction.fields.includes('occurDate')" label="发生日期">
          <n-date-picker v-model:value="mediaAction.occurDate" type="date" clearable style="width: 100%" />
        </n-form-item>
        <n-form-item v-if="mediaAction.fields.includes('invoiceNo')" label="发票号">
          <n-input v-model:value="mediaAction.invoiceNo" placeholder="" />
        </n-form-item>
        <n-form-item v-if="mediaAction.fields.includes('resourceId')" label="媒体/代理">
          <n-input v-model:value="mediaAction.resourceId" placeholder="" />
        </n-form-item>
        <n-form-item v-if="mediaAction.fields.includes('remark')" label="备注">
          <n-input v-model:value="mediaAction.remark" type="textarea" :rows="2" />
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue';
  import {
    NButton,
    NCard,
    NDatePicker,
    NEmpty,
    NForm,
    NFormItem,
    NGrid,
    NGridItem,
    NInput,
    NInputNumber,
    NModal,
    NSelect,
    NSpace,
    useMessage,
  } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderListItem } from '@lib/shared/models/advertising';

  import {
    clearRedInvoiceAdPayment,
    confirmPrepayAdPayment,
    getAdOrderPage,
    invoiceAdPayment,
    payMediaPostpayAdPayment,
    payMediaPostpayForceAdPayment,
    payMediaPrepayAdPayment,
    receiveAdPayment,
  } from '@/api/modules';

  const { t } = useI18n();
  const message = useMessage();

  interface MediaActionDef {
    type:
      | 'confirmPrepay'
      | 'payMediaPrepay'
      | 'invoice'
      | 'receive'
      | 'payMediaPostpay'
      | 'payMediaPostpayForce'
      | 'clearRedInvoice';
    label: string;
    fields: string[];
  }

  const actions: MediaActionDef[] = [
    { type: 'confirmPrepay', label: '确认预收款', fields: ['amount', 'occurDate', 'remark'] },
    { type: 'payMediaPrepay', label: '付媒体预付款', fields: ['amount', 'occurDate', 'resourceId', 'remark'] },
    { type: 'invoice', label: '开票', fields: ['amount', 'invoiceNo', 'occurDate', 'remark'] },
    { type: 'receive', label: '收款登记', fields: ['amount', 'occurDate', 'remark'] },
    { type: 'payMediaPostpay', label: '付媒体尾款', fields: ['amount', 'occurDate', 'resourceId', 'remark'] },
    {
      type: 'payMediaPostpayForce',
      label: '付媒体尾款(强制)',
      fields: ['amount', 'occurDate', 'resourceId', 'remark'],
    },
    { type: 'clearRedInvoice', label: '清除红冲', fields: [] },
  ];

  const selectedOrderId = ref<string | null>(null);
  const orderOptions = ref<{ label: string; value: string }[]>([]);
  const orderLoading = ref(false);

  const mediaAction = reactive<{
    show: boolean;
    type: MediaActionDef['type'];
    title: string;
    fields: string[];
    amount: number | null;
    occurDate: number | null;
    invoiceNo: string;
    resourceId: string;
    remark: string;
  }>({
    show: false,
    type: 'confirmPrepay',
    title: '',
    fields: [],
    amount: null,
    occurDate: null,
    invoiceNo: '',
    resourceId: '',
    remark: '',
  });

  function openMedia(a: MediaActionDef) {
    if (!selectedOrderId.value) {
      message.warning('请先选择关联订单');
      return;
    }
    mediaAction.type = a.type;
    mediaAction.title = a.label;
    mediaAction.fields = a.fields;
    mediaAction.amount = null;
    mediaAction.occurDate = null;
    mediaAction.invoiceNo = '';
    mediaAction.resourceId = '';
    mediaAction.remark = '';
    mediaAction.show = true;
  }

  async function handleMediaConfirm() {
    if (!selectedOrderId.value) return;
    const orderId = selectedOrderId.value;
    try {
      switch (mediaAction.type) {
        case 'confirmPrepay':
          await confirmPrepayAdPayment({
            orderId,
            amount: mediaAction.amount ?? undefined,
            occurDate: mediaAction.occurDate,
            remark: mediaAction.remark || undefined,
          });
          break;
        case 'payMediaPrepay':
          await payMediaPrepayAdPayment({
            orderId,
            amount: mediaAction.amount ?? undefined,
            occurDate: mediaAction.occurDate,
            resourceId: mediaAction.resourceId || undefined,
            remark: mediaAction.remark || undefined,
          });
          break;
        case 'invoice':
          await invoiceAdPayment({
            orderId,
            amount: mediaAction.amount ?? undefined,
            invoiceNo: mediaAction.invoiceNo || undefined,
            occurDate: mediaAction.occurDate,
            remark: mediaAction.remark || undefined,
          });
          break;
        case 'receive':
          await receiveAdPayment({
            orderId,
            amount: mediaAction.amount ?? undefined,
            occurDate: mediaAction.occurDate,
            remark: mediaAction.remark || undefined,
          });
          break;
        case 'payMediaPostpay':
          await payMediaPostpayAdPayment({
            orderId,
            amount: mediaAction.amount ?? undefined,
            occurDate: mediaAction.occurDate,
            resourceId: mediaAction.resourceId || undefined,
            remark: mediaAction.remark || undefined,
          });
          break;
        case 'payMediaPostpayForce':
          await payMediaPostpayForceAdPayment({
            orderId,
            amount: mediaAction.amount ?? undefined,
            occurDate: mediaAction.occurDate,
            resourceId: mediaAction.resourceId || undefined,
            remark: mediaAction.remark || undefined,
          });
          break;
        case 'clearRedInvoice':
          await clearRedInvoiceAdPayment({ orderId });
          break;
        default:
          break;
      }
      message.success(t('advertising.common.operateSuccess'));
      mediaAction.show = false;
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  async function loadOrders() {
    orderLoading.value = true;
    try {
      const res = await getAdOrderPage({ current: 1, pageSize: 200 });
      orderOptions.value = (res.list || []).map((o: AdOrderListItem) => ({
        label: `${o.orderNo || o.orderName}（${o.orderName || ''}）`,
        value: o.id,
      }));
    } catch (e) {
      message.error((e as Error).message || '加载订单失败');
    } finally {
      orderLoading.value = false;
    }
  }

  onMounted(loadOrders);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .mb-4 {
    margin-bottom: 16px;
  }
  .mt-4 {
    margin-top: 16px;
  }
</style>
