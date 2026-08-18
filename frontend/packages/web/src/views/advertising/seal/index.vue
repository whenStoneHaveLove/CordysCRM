<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          :placeholder="t('advertising.seal.search')"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.sealType"
          :placeholder="t('advertising.seal.filter.sealType')"
          :options="sealTypeOptions"
          clearable
          style="width: 140px"
        />
        <n-select
          v-model:value="searchForm.status"
          :placeholder="t('advertising.seal.filter.status')"
          :options="statusOptions"
          clearable
          style="width: 130px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">{{ t('advertising.order.reset') }}</n-button>
        <n-button type="primary" @click="goApply">{{ t('advertising.seal.apply') }}</n-button>
      </n-space>
    </n-card>

    <n-card :bordered="false" class="mt-4">
      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: any) => row.id"
        remote
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
  import { h, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { NButton, NCard, NDataTable, NInput, NSelect, NSpace, NTag, useMessage } from 'naive-ui';

  import {
    AdSealRecordStatusOptions,
    AdSealTypeOptions,
    getAdSealRecordStatusLabel,
    getAdSealTypeLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdSealRecordListItem, AdSealRecordPageParams } from '@lib/shared/models/advertising';

  import { getAdSealPage } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtDate, fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const sealTypeOptions = AdSealTypeOptions;
  const statusOptions = AdSealRecordStatusOptions;

  const loading = ref(false);
  const list = ref<AdSealRecordListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
    sealType: null as number | null,
    status: null as number | null,
  });

  /* eslint-disable no-use-before-define */
  const pagination = reactive({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    showSizePicker: true,
    pageSizes: [10, 20, 50],
    onUpdatePage: (page: number) => {
      pagination.page = page;

      fetchData();
    },
    onUpdatePageSize: (size: number) => {
      pagination.pageSize = size;
      pagination.page = 1;

      fetchData();
    },
  });
  /* eslint-enable no-use-before-define */

  async function fetchData() {
    loading.value = true;
    try {
      const params: AdSealRecordPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        sealType: searchForm.sealType,
        status: searchForm.status,
      };
      const res = await getAdSealPage(params);
      list.value = res.list || [];
      pagination.itemCount = res.total || 0;
    } catch (e) {
      message.error((e as Error).message || '查询失败');
    } finally {
      loading.value = false;
    }
  }

  function sealRecordStatusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    if (status === 10) return 'success';
    if (status === 20) return 'error';
    return 'warning';
  }

  const { loadUserMap, getUserName } = useUserMap();

  function openDetail(row: AdSealRecordListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_SEAL_DETAIL, params: { id: row.id } });
  }
  function goApprove(row: AdSealRecordListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_SEAL_APPROVE, params: { id: row.id } });
  }

  const columns: DataTableColumn<AdSealRecordListItem>[] = [
    { key: 'contractNo', title: t('advertising.seal.column.contractNo'), width: 160, ellipsis: { tooltip: true } },
    {
      key: 'contractName',
      title: t('advertising.seal.column.contractName'),
      minWidth: 160,
      ellipsis: { tooltip: true },
    },
    {
      key: 'businessEntityName',
      title: t('advertising.seal.column.businessEntity'),
      width: 140,
      ellipsis: { tooltip: true },
    },
    {
      key: 'sealType',
      title: t('advertising.seal.column.sealType'),
      width: 100,
      render: (row) => h('span', getAdSealTypeLabel(row.sealType)),
    },
    {
      key: 'status',
      title: t('advertising.seal.column.status'),
      width: 90,
      render: (row) =>
        h(
          NTag,
          { type: sealRecordStatusTagType(row.status) },
          { default: () => getAdSealRecordStatusLabel(row.status) }
        ),
    },
    { key: 'appliedCopies', title: t('advertising.seal.column.appliedCopies'), width: 90 },
    { key: 'actualCopies', title: t('advertising.seal.column.actualCopies'), width: 90 },
    {
      key: 'applicantId',
      title: t('advertising.seal.column.applicant'),
      width: 120,
      render: (row) => h('span', getUserName(row.applicantId)),
    },
    {
      key: 'approverId',
      title: t('advertising.seal.column.approver'),
      width: 120,
      render: (row) => h('span', getUserName(row.approverId)),
    },
    {
      key: 'approvedAt',
      title: t('advertising.seal.column.approvedAt'),
      width: 160,
      render: (row) => h('span', fmtDateTime(row.approvedAt)),
    },
    {
      key: 'createTime',
      title: t('advertising.order.column.createTime'),
      width: 160,
      render: (row) => h('span', fmtDateTime(row.createTime)),
    },
    {
      key: 'action',
      title: t('advertising.order.detail'),
      width: 180,
      fixed: 'right' as const,
      render: (row) =>
        h(
          NSpace,
          {},
          {
            default: () => [
              h(
                NButton,
                { size: 'small', onClick: () => openDetail(row) },
                { default: () => t('advertising.order.detail') }
              ),
              row.status === 0
                ? h(
                    NButton,
                    { size: 'small', type: 'primary', onClick: () => goApprove(row) },
                    { default: () => t('advertising.seal.approve') }
                  )
                : null,
            ],
          }
        ),
    },
  ];

  function handleSearch() {
    pagination.page = 1;
    fetchData();
  }
  function handleReset() {
    searchForm.keyword = '';
    searchForm.sealType = null;
    searchForm.status = null;
    handleSearch();
  }
  function goApply() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_SEAL_APPLY });
  }

  function applyQuery() {
    const q = router.currentRoute.value.query;
    if (q.sealType != null && q.sealType !== '') searchForm.sealType = Number(q.sealType);
    if (q.status != null && q.status !== '') searchForm.status = Number(q.status);
  }

  onMounted(async () => {
    await loadUserMap();
    applyQuery();
    fetchData();
  });
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .search-card {
    margin-bottom: 0;
  }
  .mt-4 {
    margin-top: 16px;
  }
</style>
