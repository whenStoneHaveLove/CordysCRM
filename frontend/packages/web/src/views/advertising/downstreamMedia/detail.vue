<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ detail.media?.name || '-' }}</span>
            <n-tag :type="statusTagType(detail.media?.status)">{{ statusLabelText }}</n-tag>
            <n-tag>{{ cooperationLabelText }}</n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-divider title-placement="left">基本信息</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="名称">{{ detail.media?.name || '-' }}</n-descriptions-item>
          <n-descriptions-item label="类型">{{ detail.mediaTypeLabel || detail.media?.mediaType || '-' }}</n-descriptions-item>
          <n-descriptions-item label="覆盖渠道">{{ detail.media?.channel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="刊例价">{{ detail.media?.rateCard || '-' }}</n-descriptions-item>
          <n-descriptions-item label="折扣政策">{{ detail.media?.discountPolicy || '-' }}</n-descriptions-item>
          <n-descriptions-item label="联系人">{{ detail.media?.contactPerson || '-' }}</n-descriptions-item>
          <n-descriptions-item label="电话">{{ detail.media?.contactPhone || '-' }}</n-descriptions-item>
          <n-descriptions-item label="合作状态">{{ cooperationLabelText }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="statusTagType(detail.media?.status)">{{ statusLabelText }}</n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="业务主体">{{ detail.businessEntityName || '-' }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">审计信息</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="创建人">{{ getUserName(detail.media?.createUser) }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ fmtDateTime(detail.media?.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="修改人">{{ getUserName(detail.media?.updateUser) }}</n-descriptions-item>
          <n-descriptions-item label="修改时间">{{ fmtDateTime(detail.media?.updateTime) }}</n-descriptions-item>
        </n-descriptions>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, NCard, NDescriptions, NDescriptionsItem, NDivider, NSpin, NTag, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import { getAdDownstreamMediaDetail } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const mediaId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<any | null>(null);

  const statusLabelText = computed(() => {
    const s = detail.value?.media?.status;
    if (s === 20) return '停用';
    if (s === 10) return '正常';
    return detail.value?.statusLabel || '-';
  });

  const cooperationLabelText = computed(() => {
    const s = detail.value?.media?.cooperationStatus;
    if (s === 20) return '停用';
    if (s === 10) return '正常';
    return detail.value?.cooperationStatusLabel || '-';
  });

  function statusTagType(status?: number): 'success' | 'error' | 'default' {
    if (status === 20) return 'error';
    if (status === 10) return 'success';
    return 'default';
  }

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdDownstreamMediaDetail(mediaId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_DOWNSTREAM_MEDIA });
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
