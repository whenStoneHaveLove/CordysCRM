<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          :placeholder="t('advertising.customer.search')"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <n-input
          v-model:value="searchForm.industryCode"
          :placeholder="t('advertising.customer.filter.industry')"
          clearable
          style="width: 150px"
        />
        <n-select
          v-model:value="searchForm.customerLevel"
          :placeholder="t('advertising.customer.filter.level')"
          :options="levelOptions"
          clearable
          style="width: 150px"
        />
        <n-select
          v-model:value="searchForm.status"
          :placeholder="t('advertising.customer.filter.status')"
          :options="statusOptions"
          clearable
          style="width: 150px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">{{ t('advertising.order.reset') }}</n-button>
        <n-button type="primary" @click="goCreate">{{ t('advertising.customer.new') }}</n-button>
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
    AdCustomerLevelOptions,
    AdCustomerStatusOptions,
    getAdCustomerLevelLabel,
    getAdCustomerStatusLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdCustomerListItem, AdCustomerPageParams } from '@lib/shared/models/advertising';

  import { getAdCustomerPage } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const levelOptions = AdCustomerLevelOptions;
  const statusOptions = AdCustomerStatusOptions;

  const loading = ref(false);
  const list = ref<AdCustomerListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
    industryCode: '',
    customerLevel: null as number | null,
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
      const params: AdCustomerPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        industryCode: searchForm.industryCode || undefined,
        customerLevel: searchForm.customerLevel,
        status: searchForm.status,
      };
      const res = await getAdCustomerPage(params);
      list.value = res.list || [];
      pagination.itemCount = res.total || 0;
    } catch (e) {
      message.error((e as Error).message || '查询失败');
    } finally {
      loading.value = false;
    }
  }

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

  function openDetail(row: AdCustomerListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER_DETAIL, params: { id: row.id } });
  }
  function openEdit(row: AdCustomerListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER_EDIT, params: { id: row.id } });
  }

  const columns: DataTableColumn<AdCustomerListItem>[] = [
    { key: 'customerName', title: t('advertising.customer.column.name'), minWidth: 160, ellipsis: { tooltip: true } },
    {
      key: 'brand',
      title: t('advertising.customer.column.brand'),
      width: 140,
      ellipsis: { tooltip: true },
    },
    {
      key: 'industry',
      title: t('advertising.customer.column.industry'),
      width: 140,
      ellipsis: { tooltip: true },
    },
    {
      key: 'customerLevel',
      title: t('advertising.customer.column.level'),
      width: 100,
      render: (row) => h('span', getAdCustomerLevelLabel(row.customerLevel)),
    },
    {
      key: 'status',
      title: t('advertising.customer.column.status'),
      width: 100,
      render: (row) =>
        h(NTag, { type: statusTagType(row.status) }, { default: () => getAdCustomerStatusLabel(row.status) }),
    },
    {
      key: 'signingEntity',
      title: t('advertising.customer.form.signingEntity'),
      width: 140,
      ellipsis: { tooltip: true },
      render: (row) => h('span', row.signingEntity || '-'),
    },
    {
      key: 'action',
      title: t('advertising.customer.detail'),
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
                { default: () => t('advertising.customer.detail') }
              ),
              h(
                NButton,
                { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                { default: () => t('advertising.customer.edit') }
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
    searchForm.industryCode = '';
    searchForm.customerLevel = null;
    searchForm.status = null;
    handleSearch();
  }
  function goCreate() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER_CREATE });
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
