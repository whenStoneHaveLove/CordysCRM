<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ detail.resource?.resourceName || '-' }}</span>
            <n-tag :type="statusTagType(detail.resource?.status)">{{
              getAdResourceStatusLabel(detail.resource?.status)
            }}</n-tag>
            <n-tag>{{ getAdResourceTypeLabel(detail.resource?.resourceType) }}</n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button v-if="detail.resource?.status === 10" type="primary" @click="goEdit">编辑</n-button>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-divider title-placement="left">{{ t('advertising.resource.column.name') }}</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="资源名称">{{ detail.resource?.resourceName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="资源类型">{{
            getAdResourceTypeLabel(detail.resource?.resourceType)
          }}</n-descriptions-item>
          <n-descriptions-item label="媒体类型">{{ detail.resource?.mediaType || '-' }}</n-descriptions-item>
          <n-descriptions-item label="渠道">{{ detail.resource?.channel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="归属业务主体">{{ detail.resource?.businessEntityId || '-' }}</n-descriptions-item>
          <n-descriptions-item label="归属主体名称">{{ detail.businessEntityName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="签约主体">{{ detail.resource?.signingEntity || '-' }}</n-descriptions-item>
          <n-descriptions-item label="信用代码">{{ detail.resource?.creditCode || '-' }}</n-descriptions-item>
          <n-descriptions-item label="刊例价">{{ fmtAmount(detail.resource?.rateCard) }}</n-descriptions-item>
          <n-descriptions-item label="折扣政策">{{ detail.resource?.discountPolicy || '-' }}</n-descriptions-item>
          <n-descriptions-item label="广告位">{{ detail.resource?.position || '-' }}</n-descriptions-item>
          <n-descriptions-item label="日均曝光量">{{ detail.resource?.dailyImpressions ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="单价">{{ fmtAmount(detail.resource?.unitPrice) }}</n-descriptions-item>
          <n-descriptions-item label="备注">{{ detail.resource?.remark || '-' }}</n-descriptions-item>
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
