<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ detail.customer?.name || '-' }}</span>
            <n-tag :type="statusTagType(detail.customer?.status)">{{
              getAdCustomerStatusLabel(detail.customer?.status)
            }}</n-tag>
            <n-tag>{{ getAdCustomerLevelLabel(detail.customer?.customerLevel) }}</n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button type="primary" @click="goEdit">编辑</n-button>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-divider title-placement="left">{{ t('advertising.customer.column.name') }}</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="客户名称">{{ detail.customer?.name || '-' }}</n-descriptions-item>
          <n-descriptions-item label="品牌">{{ detail.customer?.brand || '-' }}</n-descriptions-item>
          <n-descriptions-item label="行业类别">{{ detail.customer?.industryCode || '-' }}</n-descriptions-item>
          <n-descriptions-item label="行业">{{ detail.customer?.industry || '-' }}</n-descriptions-item>
          <n-descriptions-item label="签约主体">{{ detail.customer?.signingEntity || '-' }}</n-descriptions-item>
          <n-descriptions-item label="联系人">{{ detail.customer?.contactPerson || '-' }}</n-descriptions-item>
          <n-descriptions-item label="联系电话">{{ detail.customer?.contactPhone || '-' }}</n-descriptions-item>
          <n-descriptions-item label="邮箱">{{ detail.customer?.email || '-' }}</n-descriptions-item>
          <n-descriptions-item label="地址">{{ detail.customer?.address || '-' }}</n-descriptions-item>
          <n-descriptions-item label="关联订单数">{{ detail.orderCount ?? 0 }}</n-descriptions-item>
          <n-descriptions-item label="备注">{{ detail.customer?.remark || '-' }}</n-descriptions-item>
        </n-descriptions>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, NCard, NDescriptions, NDescriptionsItem, NDivider, NSpin, NTag, useMessage } from 'naive-ui';

  import { getAdCustomerLevelLabel, getAdCustomerStatusLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdCustomerInfo } from '@lib/shared/models/advertising';

  import { getAdCustomerDetail } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const customerId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<AdCustomerInfo | null>(null);

  function statusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    switch (status) {
      case 0:
        return 'success';
      case 10:
        return 'warning';
      case 20:
        return 'error';
      default:
        return 'default';
    }
  }

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdCustomerDetail(customerId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER });
  }
  function goEdit() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER_EDIT, params: { id: customerId } });
  }

  onMounted(fetchDetail);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
</style>
