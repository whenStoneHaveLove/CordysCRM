<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          :placeholder="t('advertising.change.search')"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.status"
          :placeholder="t('advertising.change.filter.status')"
          :options="statusOptions"
          clearable
          style="width: 150px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">{{ t('advertising.order.reset') }}</n-button>
        <n-button type="primary" @click="goCreate">{{ t('advertising.change.new') }}</n-button>
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

  import { AdOrderChangeStatusOptions, getAdOrderChangeStatusLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderChangeListItem, AdOrderChangePageParams } from '@lib/shared/models/advertising';

  import { getAdOrderChangePage, submitAdOrderChange } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();
  const { loadUserMap, getUserName } = useUserMap();

  const statusOptions = AdOrderChangeStatusOptions;

  const loading = ref(false);
  const list = ref<AdOrderChangeListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
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
      const params: AdOrderChangePageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        status: searchForm.status,
      };
      const res = await getAdOrderChangePage(params);
      list.value = res.list || [];
      pagination.itemCount = res.total || 0;
    } catch (e) {
      message.error((e as Error).message || '查询失败');
    } finally {
      loading.value = false;
    }
  }

  function openDetail(row: AdOrderChangeListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CHANGE_DETAIL, params: { id: row.id } });
  }
  async function handleSubmit(row: AdOrderChangeListItem) {
    try {
      await submitAdOrderChange(row.id);
      message.success(t('advertising.common.operateSuccess'));
      await fetchData();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  const columns: DataTableColumn<AdOrderChangeListItem>[] = [
    { key: 'id', title: '改单ID', width: 180, ellipsis: { tooltip: true } },
    { key: 'orderNo', title: t('advertising.change.column.orderNo'), width: 150, ellipsis: { tooltip: true } },
    { key: 'orderName', title: t('advertising.change.column.orderName'), width: 160, ellipsis: { tooltip: true } },
    {
      key: 'reason',
      title: t('advertising.change.column.reason'),
      minWidth: 200,
      ellipsis: { tooltip: true },
      render: (row) => h('span', { title: row.reason }, row.reason || '-'),
    },
    {
      key: 'status',
      title: t('advertising.change.column.status'),
      width: 100,
      render: (row) => h(NTag, {}, { default: () => getAdOrderChangeStatusLabel(row.status) }),
    },
    {
      key: 'approverId',
      title: t('advertising.change.column.approver'),
      width: 120,
      render: (row) => h('span', getUserName(row.approverId)),
    },
    {
      key: 'approvedAt',
      title: '审批时间',
      width: 160,
      render: (row) => h('span', fmtDateTime(row.approvedAt)),
    },
    {
      key: 'creatorId',
      title: '创建人',
      width: 120,
      render: (row) => h('span', getUserName(row.creatorId)),
    },
    {
      key: 'createTime',
      title: t('advertising.change.column.createTime'),
      width: 160,
      render: (row) => h('span', fmtDateTime(row.createTime)),
    },
    {
      key: 'action',
      title: t('advertising.change.detail'),
      width: 140,
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
                { default: () => t('advertising.change.detail') }
              ),
              row.status === 0
                ? h(
                    NButton,
                    { size: 'small', type: 'primary', onClick: () => handleSubmit(row) },
                    { default: () => t('advertising.change.submit') }
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
    searchForm.status = null;
    handleSearch();
  }
  function goCreate() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CHANGE_CREATE });
  }

  function applyQuery() {
    const q = router.currentRoute.value.query;
    if (q.status != null && q.status !== '') searchForm.status = Number(q.status);
  }

  onMounted(() => {
    loadUserMap();
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
