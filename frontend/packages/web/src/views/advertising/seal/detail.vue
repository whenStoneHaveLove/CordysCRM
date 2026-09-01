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
          <n-descriptions-item label="合同编号">
            <n-button v-if="detail.contractId" text type="primary" size="small" @click="openContractDetail">
              {{ detail.contractNo || '-' }}
            </n-button>
            <span v-else>{{ detail.contractNo || '-' }}</span>
          </n-descriptions-item>
          <n-descriptions-item label="业务主体">{{ detail.businessEntityName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="关联订单">{{ detail.orderNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="用印附件">
            <template v-if="detail.contractFileUrl">
              <n-space>
                <n-button size="tiny" type="primary" ghost @click="handlePreview(detail.contractFileUrl!)"
                  >预览</n-button
                >
                <n-button size="tiny" type="primary" ghost @click="handleDownload(detail.contractFileUrl!)"
                  >下载</n-button
                >
              </n-space>
            </template>
            <span v-else>-</span>
          </n-descriptions-item>
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
  import useUserStore from '@/store/modules/user';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();
  const userStore = useUserStore();

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

  /** 新 tab 打开关联合同详情 */
  function openContractDetail() {
    const contractId = detail.value?.contractId;
    if (!contractId) return;
    const { href } = router.resolve({
      name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_DETAIL,
      params: { id: contractId },
    });
    window.open(href, '_blank');
  }

  /** 附件预览 */
  function handlePreview(fileUrl: string) {
    const previewUrl = `/attachment/preview/${fileUrl}?userId=${userStore.userInfo?.id || ''}`;
    window.open(previewUrl, '_blank');
  }

  /** 附件下载 */
  function handleDownload(fileUrl: string) {
    const downloadUrl = `/attachment/download/${fileUrl}?userId=${userStore.userInfo?.id || ''}`;
    const a = document.createElement('a');
    a.href = downloadUrl;
    a.click();
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
