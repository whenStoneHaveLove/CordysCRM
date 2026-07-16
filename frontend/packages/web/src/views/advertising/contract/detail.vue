<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ detail.contract.contractName || detail.contract.contractNo }}</span>
            <n-tag v-if="detail.sealStatusLabel" :type="sealStatusTagType(detail.contract.sealStatus)">
              {{ detail.sealStatusLabel }}
            </n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-divider title-placement="left">{{ t('advertising.order.detail.tab.base') }}</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="合同编号">{{ detail.contract.contractNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="合同名称">{{ detail.contract.contractName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="业务主体">{{
            detail.businessEntityName || detail.contract.businessEntityId || '-'
          }}</n-descriptions-item>
          <n-descriptions-item label="合同方向">{{ detail.directionLabel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="合同类型">{{ detail.typeLabel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="关联方">{{
            detail.relatedPartyName || detail.contract.relatedPartyId || '-'
          }}</n-descriptions-item>
          <n-descriptions-item label="关联订单">{{
            detail.orderNo || detail.contract.orderId || '-'
          }}</n-descriptions-item>
          <n-descriptions-item label="变更单ID">{{ detail.contract.changeOrderId || '-' }}</n-descriptions-item>
          <n-descriptions-item label="签约主体">{{ detail.contract.signingEntity || '-' }}</n-descriptions-item>
          <n-descriptions-item label="有效期起">{{ fmtDate(detail.contract.validFrom) }}</n-descriptions-item>
          <n-descriptions-item label="有效期止">{{ fmtDate(detail.contract.validTo) }}</n-descriptions-item>
          <n-descriptions-item label="合同金额">{{ fmtAmount(detail.contract.amount) }}</n-descriptions-item>
          <n-descriptions-item label="合同文件">
            <a v-if="detail.contract.fileUrl" :href="detail.contract.fileUrl" target="_blank" rel="noopener">查看</a>
            <span v-else>-</span>
          </n-descriptions-item>
          <n-descriptions-item label="状态">{{ detail.statusLabel || '-' }}</n-descriptions-item>
        </n-descriptions>

        <n-divider v-if="detail.contract.rebateTerms" title-placement="left">返点条款</n-divider>
        <n-descriptions v-if="detail.contract.rebateTerms" label-placement="left" :column="1" bordered size="small">
          <n-descriptions-item label="返点条款">{{ detail.contract.rebateTerms }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">{{ t('advertising.contract.detail.tab.seal') }}</n-divider>
        <n-empty v-if="!detail.sealRecords || detail.sealRecords.length === 0" description="暂无用印记录" />
        <n-data-table
          v-else
          :columns="sealColumns"
          :data="detail.sealRecords"
          :row-key="(row: any) => row.id"
          :pagination="false"
          size="small"
        />
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
  import { h, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDataTable,
    NDescriptions,
    NDescriptionsItem,
    NDivider,
    NEmpty,
    NSpace,
    NSpin,
    NTag,
    useMessage,
  } from 'naive-ui';

  import { getAdSealRecordStatusLabel, getAdSealTypeLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdContractDetailResponse, AdSealRecordInfo } from '@lib/shared/models/advertising';

  import { getAdContractDetail } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount, fmtDate } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const contractId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<AdContractDetailResponse | null>(null);

  function sealStatusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    switch (status) {
      case 0:
        return 'default';
      case 10:
        return 'warning';
      case 20:
        return 'success';
      case 30:
        return 'error';
      default:
        return 'default';
    }
  }

  function sealRecordStatusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    if (status === 10) return 'success';
    if (status === 20) return 'error';
    return 'warning';
  }

  const sealColumns: DataTableColumn<AdSealRecordInfo>[] = [
    { key: 'sealType', title: '用印类型', width: 100, render: (row) => h('span', getAdSealTypeLabel(row.sealType)) },
    {
      key: 'status',
      title: '状态',
      width: 90,
      render: (row) =>
        h(
          NTag,
          { type: sealRecordStatusTagType(row.status) },
          { default: () => getAdSealRecordStatusLabel(row.status) }
        ),
    },
    { key: 'appliedCopies', title: '申请份数', width: 90 },
    { key: 'actualCopies', title: '实际份数', width: 90 },
    { key: 'applicantId', title: '申请人', width: 120 },
    { key: 'applyRemark', title: '申请备注', width: 150, ellipsis: { tooltip: true } },
    { key: 'approverId', title: '审批人', width: 120 },
    { key: 'approveRemark', title: '审批备注', width: 150, ellipsis: { tooltip: true } },
    { key: 'approvedAt', title: '审批时间', width: 120, render: (row) => h('span', fmtDate(row.approvedAt)) },
  ];

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdContractDetail(contractId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT });
  }

  onMounted(fetchDetail);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
</style>
