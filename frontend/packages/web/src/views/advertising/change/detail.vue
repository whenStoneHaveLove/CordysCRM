<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>改单详情</span>
            <n-tag>{{ detail.statusLabel || getAdOrderChangeStatusLabel(detail.change.status) }}</n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button v-if="detail.change.status === 0" type="primary" @click="handleSubmit">提交</n-button>
            <n-button v-if="detail.change.status === 10" type="primary" @click="handleApprove">审批通过</n-button>
            <n-button v-if="detail.change.status === 10" @click="handleReject">驳回</n-button>
            <n-button v-if="detail.change.status === 20" type="warning" @click="handleExecute">执行</n-button>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-descriptions label-placement="left" :column="2" bordered size="small">
          <n-descriptions-item label="关联订单">{{
            detail.orderNo || detail.change.orderId || '-'
          }}</n-descriptions-item>
          <n-descriptions-item label="改单ID">{{ detail.change.id }}</n-descriptions-item>
          <n-descriptions-item label="审批人">{{ detail.change.approverId || '-' }}</n-descriptions-item>
          <n-descriptions-item label="审批时间">{{ fmtDateTime(detail.change.approvedAt) }}</n-descriptions-item>
          <n-descriptions-item label="变更字段" :span="2">{{ detail.change.changeFields || '-' }}</n-descriptions-item>
          <n-descriptions-item label="变更原因" :span="2">{{ detail.change.reason || '-' }}</n-descriptions-item>
          <n-descriptions-item label="审批备注" :span="2">{{ detail.change.approveRemark || '-' }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">{{ t('advertising.change.detail.snapshotBefore') }}</n-divider>
        <pre class="snapshot">{{ pretty(detail.change.snapshotBefore) }}</pre>

        <n-divider title-placement="left">{{ t('advertising.change.detail.snapshotAfter') }}</n-divider>
        <pre class="snapshot">{{ pretty(detail.change.snapshotAfter) }}</pre>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDescriptions,
    NDescriptionsItem,
    NDivider,
    NEmpty,
    NSpace,
    NSpin,
    NTag,
    useMessage,
  } from 'naive-ui';

  import { getAdOrderChangeStatusLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderChangeDetail } from '@lib/shared/models/advertising';

  import {
    approveAdOrderChange,
    executeAdOrderChange,
    getAdOrderChangeDetail,
    rejectAdOrderChange,
    submitAdOrderChange,
  } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const changeId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<AdOrderChangeDetail | null>(null);

  function pretty(value?: string | null): string {
    if (!value) return '-';
    try {
      return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
      return value;
    }
  }

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdOrderChangeDetail(changeId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CHANGE });
  }

  async function handleSubmit() {
    try {
      await submitAdOrderChange(changeId);
      message.success(t('advertising.common.submitSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '提交失败');
    }
  }
  async function handleApprove() {
    try {
      await approveAdOrderChange(changeId);
      message.success(t('advertising.common.operateSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '审批失败');
    }
  }
  async function handleReject() {
    try {
      await rejectAdOrderChange(changeId);
      message.success(t('advertising.common.operateSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '驳回失败');
    }
  }
  async function handleExecute() {
    try {
      await executeAdOrderChange(changeId);
      message.success(t('advertising.common.operateSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '执行失败');
    }
  }

  onMounted(fetchDetail);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .snapshot {
    overflow: auto;
    padding: 12px;
    max-height: 320px;
    border-radius: 4px;
    white-space: pre-wrap;
    background: #f5f5f5;
    word-break: break-all;
  }
</style>
