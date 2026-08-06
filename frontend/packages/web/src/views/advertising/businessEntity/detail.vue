<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ detail.entity?.name || '-' }}</span>
            <n-tag :type="statusTagType(detail.entity?.status)">{{ statusLabelText }}</n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-divider title-placement="left">基本信息</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="主体名称">{{ detail.entity?.name || '-' }}</n-descriptions-item>
          <n-descriptions-item label="主体代码">{{ detail.entity?.code || '-' }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="statusTagType(detail.entity?.status)">{{ statusLabelText }}</n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="是否跨主体">
            {{ detail.entity?.isCrossEntity === 1 ? t('advertising.common.yes') : t('advertising.common.no') }}
          </n-descriptions-item>
          <n-descriptions-item label="关联用户数">{{ detail.userCount ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="关联订单数">{{ detail.orderCount ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="备注" :span="3">{{ detail.entity?.remark || '-' }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">审计信息</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="创建人">{{ getUserName(detail.entity?.createUser) }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ fmtDateTime(detail.entity?.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="修改人">{{ getUserName(detail.entity?.updateUser) }}</n-descriptions-item>
          <n-descriptions-item label="修改时间">{{ fmtDateTime(detail.entity?.updateTime) }}</n-descriptions-item>
        </n-descriptions>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, NCard, NDescriptions, NDescriptionsItem, NDivider, NSpin, NTag, useMessage } from 'naive-ui';

  import { getAdBusinessEntityStatusLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdBusinessEntityDetail } from '@lib/shared/models/advertising';

  import { getAdBusinessEntityDetail } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const entityId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<AdBusinessEntityDetail | null>(null);

  const statusLabelText = computed(() => {
    const s = detail.value?.entity?.status;
    if (s === 20) return '停用';
    if (s === 10) return '启用';
    return getAdBusinessEntityStatusLabel(s);
  });

  function statusTagType(status?: number): 'success' | 'default' {
    return status === 10 ? 'success' : 'default';
  }

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdBusinessEntityDetail(entityId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY });
  }

  const { loadUserMap, getUserName } = useUserMap();

  onMounted(async () => {
    await Promise.all([fetchDetail(), loadUserMap()]);
  });
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
</style>
