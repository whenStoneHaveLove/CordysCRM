<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>收付款详情</span>
            <n-tag :type="detail.record.direction === 10 ? 'success' : 'warning'">{{
              detail.directionLabel || getAdPaymentDirectionLabel(detail.record.direction)
            }}</n-tag>
            <n-tag>{{ detail.typeLabel || getAdPaymentTypeLabel(detail.record.type) }}</n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button type="error" @click="openCancel">撤销/冲销</n-button>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-descriptions label-placement="left" :column="2" bordered size="small">
          <n-descriptions-item label="关联订单">{{
            detail.orderNo || detail.record.orderId || '-'
          }}</n-descriptions-item>
          <n-descriptions-item label="记录ID">{{ detail.record.id }}</n-descriptions-item>
          <n-descriptions-item label="方向">{{
            getAdPaymentDirectionLabel(detail.record.direction)
          }}</n-descriptions-item>
          <n-descriptions-item label="类型">{{ getAdPaymentTypeLabel(detail.record.type) }}</n-descriptions-item>
          <n-descriptions-item label="金额">{{ fmtAmount(detail.record.amount) }}</n-descriptions-item>
          <n-descriptions-item label="发生日期">{{ fmtDate(detail.record.occurDate) }}</n-descriptions-item>
          <n-descriptions-item label="发票号">{{ detail.record.invoiceNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="媒体/代理">{{ detail.record.resourceId || '-' }}</n-descriptions-item>
          <n-descriptions-item label="操作人">{{ detail.record.operatorId || '-' }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ fmtDateTime(detail.record.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="备注" :span="2">{{ detail.record.remark || '-' }}</n-descriptions-item>
        </n-descriptions>
      </n-card>
    </n-spin>

    <n-modal
      v-model:show="cancelModal.show"
      title="撤销/冲销"
      preset="card"
      positive-text="确定"
      negative-text="取消"
      style="width: 460px"
      @positive-click="handleCancelConfirm"
    >
      <n-input
        v-model:value="cancelModal.reason"
        type="textarea"
        :rows="3"
        :placeholder="t('advertising.common.reason')"
      />
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDescriptions,
    NDescriptionsItem,
    NInput,
    NModal,
    NSpace,
    NSpin,
    NTag,
    useMessage,
  } from 'naive-ui';

  import { getAdPaymentDirectionLabel, getAdPaymentTypeLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdPaymentRecordDetail } from '@lib/shared/models/advertising';

  import { cancelAdPayment, getAdPaymentDetail } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount, fmtDate, fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const recordId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<AdPaymentRecordDetail | null>(null);
  const cancelModal = reactive<{ show: boolean; reason: string }>({ show: false, reason: '' });

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdPaymentDetail(recordId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_PAYMENT });
  }

  function openCancel() {
    cancelModal.reason = '';
    cancelModal.show = true;
  }
  async function handleCancelConfirm() {
    try {
      await cancelAdPayment({ id: recordId, reason: cancelModal.reason });
      message.success(t('advertising.common.operateSuccess'));
      cancelModal.show = false;
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '撤销失败');
    }
  }

  onMounted(fetchDetail);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
</style>
