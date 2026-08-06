<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ detail.agent?.name || '-' }}</span>
            <n-tag :type="statusTagType(detail.agent?.status)">{{ statusLabelText }}</n-tag>
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
          <n-descriptions-item label="代理名称">{{ detail.agent?.name || '-' }}</n-descriptions-item>
          <n-descriptions-item label="社会信用代码">{{ detail.agent?.creditCode || '-' }}</n-descriptions-item>
          <n-descriptions-item label="签约主体">{{ detail.agent?.signingEntity || '-' }}</n-descriptions-item>
          <n-descriptions-item label="联系人">{{ detail.agent?.contactPerson || '-' }}</n-descriptions-item>
          <n-descriptions-item label="电话">{{ detail.agent?.contactPhone || '-' }}</n-descriptions-item>
          <n-descriptions-item label="合作状态">{{ cooperationLabelText }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="statusTagType(detail.agent?.status)">{{ statusLabelText }}</n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="业务主体">{{ detail.businessEntityName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="备注">{{ detail.agent?.remark || '-' }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">审计信息</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="创建人">{{ getUserName(detail.agent?.createUser) }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ fmtDateTime(detail.agent?.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="修改人">{{ getUserName(detail.agent?.updateUser) }}</n-descriptions-item>
          <n-descriptions-item label="修改时间">{{ fmtDateTime(detail.agent?.updateTime) }}</n-descriptions-item>
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

  import { getAdUpstreamAgentDetail } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const agentId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<any | null>(null);

  const statusLabelText = computed(() => {
    const s = detail.value?.agent?.status;
    if (s === 20) return '停用';
    if (s === 10) return '正常';
    return detail.value?.statusLabel || '-';
  });

  const cooperationLabelText = computed(() => {
    const s = detail.value?.agent?.cooperationStatus;
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
      detail.value = await getAdUpstreamAgentDetail(agentId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_UPSTREAM_AGENT });
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
