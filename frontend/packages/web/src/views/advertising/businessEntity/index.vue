<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          :placeholder="t('advertising.businessEntity.search')"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.status"
          :placeholder="t('advertising.businessEntity.filter.status')"
          :options="statusOptions"
          clearable
          style="width: 150px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">{{ t('advertising.businessEntity.form.cancel') }}</n-button>
        <n-button type="primary" @click="goCreate">{{ t('advertising.businessEntity.new') }}</n-button>
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

  import { AdBusinessEntityStatusOptions, getAdBusinessEntityStatusLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdBusinessEntityInfo, AdBusinessEntityPageParams } from '@lib/shared/models/advertising';

  import { deleteAdBusinessEntity, getAdBusinessEntityPage } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const statusOptions = AdBusinessEntityStatusOptions;

  const loading = ref(false);
  const list = ref<AdBusinessEntityInfo[]>([]);
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
      const params: AdBusinessEntityPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        status: searchForm.status,
      };
      const res = await getAdBusinessEntityPage(params);
      list.value = res.list || [];
      pagination.itemCount = res.total || 0;
    } catch (e) {
      message.error((e as Error).message || '查询失败');
    } finally {
      loading.value = false;
    }
  }

  function statusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    return status === 10 ? 'success' : 'default';
  }

  function openDetail(row: AdBusinessEntityInfo) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY_DETAIL, params: { id: row.id } });
  }
  function openEdit(row: AdBusinessEntityInfo) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY_EDIT, params: { id: row.id } });
  }
  async function handleDisable(row: AdBusinessEntityInfo) {
    try {
      await deleteAdBusinessEntity(row.id);
      message.success(t('advertising.common.operateSuccess'));
      fetchData();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  const columns: DataTableColumn<AdBusinessEntityInfo>[] = [
    { key: 'name', title: t('advertising.businessEntity.column.name'), minWidth: 140, ellipsis: { tooltip: true } },
    { key: 'code', title: t('advertising.businessEntity.column.code'), width: 120, ellipsis: { tooltip: true } },
    {
      key: 'status',
      title: t('advertising.businessEntity.column.status'),
      width: 100,
      render: (row) =>
        h(NTag, { type: statusTagType(row.status) }, { default: () => getAdBusinessEntityStatusLabel(row.status) }),
    },
    {
      key: 'isCrossEntity',
      title: t('advertising.businessEntity.column.isCrossEntity'),
      width: 110,
      render: (row) =>
        row.isCrossEntity === 1
          ? h(NTag, { type: 'warning' }, { default: () => t('advertising.common.yes') })
          : h('span', t('advertising.common.no')),
    },
    { key: 'remark', title: t('advertising.businessEntity.column.remark'), minWidth: 160, ellipsis: { tooltip: true } },
    {
      key: 'userCount',
      title: t('advertising.businessEntity.column.userCount'),
      width: 100,
      render: (row) => h('span', row.userCount ?? '-'),
    },
    {
      key: 'orderCount',
      title: t('advertising.businessEntity.column.orderCount'),
      width: 100,
      render: (row) => h('span', row.orderCount ?? '-'),
    },
    {
      key: 'createTime',
      title: t('advertising.businessEntity.column.createTime'),
      width: 160,
      render: (row) => h('span', fmtDateTime(row.createTime)),
    },
    {
      key: 'action',
      title: t('advertising.businessEntity.detail'),
      width: 170,
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
                { default: () => t('advertising.businessEntity.detail') }
              ),
              h(
                NButton,
                { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                { default: () => t('advertising.businessEntity.edit') }
              ),
              h(
                NButton,
                { size: 'small', type: 'error', onClick: () => handleDisable(row) },
                { default: () => t('advertising.businessEntity.disable') }
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
    searchForm.status = null;
    handleSearch();
  }
  function goCreate() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY_CREATE });
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
