<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ detail.name || '-' }}</span>
            <n-tag :type="statusTagType(detail.status)">{{ getAdResourceStatusLabel(detail.status) }}</n-tag>
            <n-tag>{{ getAdResourceTypeLabel(detail.resourceType) }}</n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button v-if="detail.status === 10" type="primary" @click="goEdit">编辑</n-button>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-divider title-placement="left">{{ t('advertising.resource.column.name') }}</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="资源名称">{{ detail.name || '-' }}</n-descriptions-item>
          <n-descriptions-item label="资源类型">{{ getAdResourceTypeLabel(detail.resourceType) }}</n-descriptions-item>
          <n-descriptions-item label="媒体类型">{{ detail.mediaType || '-' }}</n-descriptions-item>
          <n-descriptions-item label="渠道">{{ detail.channel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="归属业务主体">{{ detail.businessEntityId || '-' }}</n-descriptions-item>
          <n-descriptions-item label="归属主体名称">{{ detail.businessEntityName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="签约主体">{{ detail.signingEntity || '-' }}</n-descriptions-item>
          <n-descriptions-item label="信用代码">{{ detail.creditCode || '-' }}</n-descriptions-item>
          <n-descriptions-item label="刊例价">{{ fmtAmount(detail.rateCard) }}</n-descriptions-item>
          <n-descriptions-item label="折扣政策">{{ detail.discountPolicy || '-' }}</n-descriptions-item>
          <n-descriptions-item label="广告位">{{ detail.position || '-' }}</n-descriptions-item>
          <n-descriptions-item label="日均曝光量">{{ detail.dailyImpressions ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="单价">{{ fmtAmount(detail.unitPrice) }}</n-descriptions-item>
          <n-descriptions-item label="备注">{{ detail.remark || '-' }}</n-descriptions-item>
        </n-descriptions>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, NCard, NDescriptions, NDescriptionsItem, NDivider, NSpin, NTag, useMessage } from 'naive-ui';

  import { getAdResourceStatusLabel, getAdResourceTypeLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdResourceInfo } from '@lib/shared/models/advertising';

  import { getAdResourceDetail } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const resourceId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<AdResourceInfo | null>(null);

  function statusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    return status === 10 ? 'success' : 'error';
  }

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdResourceDetail(resourceId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_RESOURCE });
  }
  function goEdit() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_RESOURCE_EDIT, params: { id: resourceId } });
  }

  onMounted(fetchDetail);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
</style>
