<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ t('advertising.seal.detail') }} - {{ detail.contractNo || detail.record.contractId }}</span>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <!-- 基本信息 -->
        <n-divider title-placement="left">基本信息</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="合同编号">{{ detail.contractNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="业务主体">{{ detail.businessEntityName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="关联订单">{{ detail.orderNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="用印类型">{{ getAdSealTypeLabel(detail.record.sealType) }}</n-descriptions-item>
          <n-descriptions-item label="申请份数">{{ detail.record.appliedCopies || '-' }}</n-descriptions-item>
          <n-descriptions-item label="实际盖章份数">{{ detail.record.actualCopies ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="状态">{{ detail.statusLabel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="申请人">{{ getUserName(detail.record.applicantId) }}</n-descriptions-item>
          <n-descriptions-item label="申请备注">{{ detail.record.applyRemark || '-' }}</n-descriptions-item>
          <n-descriptions-item label="审批人">{{ getUserName(detail.record.approverId) }}</n-descriptions-item>
          <n-descriptions-item label="审批时间">{{ fmtDateTime(detail.record.approvedAt) }}</n-descriptions-item>
          <n-descriptions-item label="审批备注">{{ detail.record.approveRemark || '-' }}</n-descriptions-item>
        </n-descriptions>

        <!-- 审计信息 -->
        <n-divider title-placement="left">审计信息</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="创建人">{{ getUserName(detail.record.createUser) }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ fmtDateTime(detail.record.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="修改人">{{ getUserName(detail.record.updateUser) }}</n-descriptions-item>
          <n-descriptions-item label="修改时间">{{ fmtDateTime(detail.record.updateTime) }}</n-descriptions-item>
        </n-descriptions>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, NCard, NDescriptions, NDescriptionsItem, NDivider, NSpace, NSpin, useMessage } from 'naive-ui';

  import { getAdSealTypeLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdSealRecordDetailResponse } from '@lib/shared/models/advertising';

  import { getAdSealDetail } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const sealId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<AdSealRecordDetailResponse | null>(null);

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdSealDetail(sealId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_SEAL });
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
