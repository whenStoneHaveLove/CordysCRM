<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          :placeholder="t('advertising.resource.search')"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.resourceType"
          :placeholder="t('advertising.resource.filter.type')"
          :options="typeOptions"
          clearable
          style="width: 150px"
        />
        <n-select
          v-model:value="searchForm.status"
          :placeholder="t('advertising.resource.filter.status')"
          :options="statusOptions"
          clearable
          style="width: 150px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">{{ t('advertising.order.reset') }}</n-button>
        <n-button type="primary" @click="goCreate">{{ t('advertising.resource.new') }}</n-button>
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
    AdResourceStatusOptions,
    AdResourceTypeOptions,
    getAdResourceStatusLabel,
    getAdResourceTypeLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdResourceListItem, AdResourcePageParams } from '@lib/shared/models/advertising';

  import { getAdResourcePage } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const typeOptions = AdResourceTypeOptions;
  const statusOptions = AdResourceStatusOptions;

  const loading = ref(false);
  const list = ref<AdResourceListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
    resourceType: null as number | null,
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
      const params: AdResourcePageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        resourceType: searchForm.resourceType,
        status: searchForm.status,
      };
      const res = await getAdResourcePage(params);
      list.value = res.list || [];
      pagination.itemCount = res.total || 0;
    } catch (e) {
      message.error((e as Error).message || '查询失败');
    } finally {
      loading.value = false;
    }
  }

  function statusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    return status === 10 ? 'success' : 'error';
  }

  function openDetail(row: AdResourceListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_RESOURCE_DETAIL, params: { id: row.id } });
  }
  function openEdit(row: AdResourceListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_RESOURCE_EDIT, params: { id: row.id } });
  }

  const columns: DataTableColumn<AdResourceListItem>[] = [
    { key: 'resourceName', title: t('advertising.resource.column.name'), minWidth: 160, ellipsis: { tooltip: true } },
    {
      key: 'resourceType',
      title: t('advertising.resource.column.type'),
      width: 110,
      render: (row) => h('span', getAdResourceTypeLabel(row.resourceType)),
    },
    {
      key: 'mediaType',
      title: t('advertising.resource.column.mediaType'),
      width: 120,
      ellipsis: { tooltip: true },
    },
    {
      key: 'channel',
      title: t('advertising.resource.column.channel'),
      width: 120,
      ellipsis: { tooltip: true },
    },
    {
      key: 'businessEntityName',
      title: t('advertising.resource.column.businessEntity'),
      width: 140,
      ellipsis: { tooltip: true },
    },
    {
      key: 'rateCard',
      title: t('advertising.resource.column.rateCard'),
      width: 130,
      align: 'right',
      render: (row) => h('span', fmtAmount(row.rateCard)),
    },
    {
      key: 'status',
      title: t('advertising.resource.column.status'),
      width: 90,
      render: (row) =>
        h(NTag, { type: statusTagType(row.status) }, { default: () => getAdResourceStatusLabel(row.status) }),
    },
    {
      key: 'action',
      title: t('advertising.resource.detail'),
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
                { default: () => t('advertising.resource.detail') }
              ),
              h(
                NButton,
                { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                { default: () => t('advertising.resource.edit') }
              ),
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
    searchForm.resourceType = null;
    searchForm.status = null;
    handleSearch();
  }
  function goCreate() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_RESOURCE_CREATE });
  }

  onMounted(fetchData);
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
