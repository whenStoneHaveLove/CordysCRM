<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          :placeholder="t('advertising.contract.search')"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.contractDirection"
          :placeholder="t('advertising.contract.filter.direction')"
          :options="directionOptions"
          clearable
          style="width: 130px"
        />
        <n-select
          v-model:value="searchForm.contractType"
          :placeholder="t('advertising.contract.filter.type')"
          :options="typeOptions"
          clearable
          style="width: 130px"
        />
        <n-select
          v-model:value="searchForm.sealStatus"
          :placeholder="t('advertising.contract.filter.sealStatus')"
          :options="sealStatusOptions"
          clearable
          style="width: 140px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">{{ t('advertising.order.reset') }}</n-button>
        <n-button v-permission="['AD_CONTRACT:CREATE']" type="primary" @click="goCreate">{{ t('advertising.contract.new') }}</n-button>
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
  import { h, onMounted, reactive, ref, resolveDirective, withDirectives } from 'vue';
  import { useRouter } from 'vue-router';
  import { NButton, NCard, NDataTable, NInput, NSelect, NSpace, NTag, useMessage } from 'naive-ui';

  import {
    AdContractDirectionOptions,
    AdContractTypeOptions,
    AdSealStatusOptions,
    getAdContractDirectionLabel,
    getAdContractTypeLabel,
    getAdSealStatusLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdContractListItem, AdContractPageParams } from '@lib/shared/models/advertising';

  import { getAdContractPage } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount, fmtDate, fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const permissionDirective = resolveDirective('permission');

  const directionOptions = AdContractDirectionOptions;
  const typeOptions = AdContractTypeOptions;
  const sealStatusOptions = AdSealStatusOptions;

  const loading = ref(false);
  const list = ref<AdContractListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
    contractDirection: null as number | null,
    contractType: null as number | null,
    sealStatus: null as number | null,
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
      const params: AdContractPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        contractDirection: searchForm.contractDirection,
        contractType: searchForm.contractType,
        sealStatus: searchForm.sealStatus,
      };
      const res = await getAdContractPage(params);
      list.value = res.list || [];
      pagination.itemCount = res.total || 0;
    } catch (e) {
      message.error((e as Error).message || '查询失败');
    } finally {
      loading.value = false;
    }
  }

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
      case 40:
        return 'info';
      case 50:
        return 'error';
      case 60:
        return 'success';
      default:
        return 'default';
    }
  }

  // 编辑按钮：仅未申请(0) 和 已驳回(30) 时显示
  function canEdit(row: AdContractListItem): boolean {
    return row.sealStatus === 0 || row.sealStatus === 30;
  }

  function openDetail(row: AdContractListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_DETAIL, params: { id: row.id } });
  }
  function openEdit(row: AdContractListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_EDIT, params: { id: row.id } });
  }

  const columns: DataTableColumn<AdContractListItem>[] = [
    { key: 'contractNo', title: t('advertising.contract.column.contractNo'), width: 160, ellipsis: { tooltip: true } },
    {
      key: 'contractName',
      title: t('advertising.contract.column.contractName'),
      minWidth: 160,
      ellipsis: { tooltip: true },
    },
    {
      key: 'businessEntityName',
      title: t('advertising.contract.column.businessEntity'),
      width: 140,
      ellipsis: { tooltip: true },
    },
    {
      key: 'contractDirection',
      title: t('advertising.contract.column.direction'),
      width: 100,
      render: (row) =>
        h(
          NTag,
          { type: row.contractDirection === 10 ? 'success' : 'warning' },
          { default: () => getAdContractDirectionLabel(row.contractDirection) }
        ),
    },
    {
      key: 'contractType',
      title: t('advertising.contract.column.type'),
      width: 90,
      render: (row) => h('span', getAdContractTypeLabel(row.contractType)),
    },
    {
      key: 'amount',
      title: t('advertising.contract.column.amount'),
      width: 130,
      align: 'right',
      render: (row) => h('span', fmtAmount(row.amount)),
    },
    {
      key: 'sealStatus',
      title: t('advertising.contract.column.sealStatus'),
      width: 110,
      render: (row) =>
        h(NTag, { type: sealStatusTagType(row.sealStatus) }, { default: () => getAdSealStatusLabel(row.sealStatus) }),
    },
    {
      key: 'status',
      title: t('advertising.contract.column.status'),
      width: 100,
      render: (row) => h('span', row.statusLabel || row.status || '-'),
    },
    {
      key: 'validFrom',
      title: t('advertising.contract.column.validFrom'),
      width: 120,
      render: (row) => h('span', fmtDate(row.validFrom)),
    },
    {
      key: 'validTo',
      title: t('advertising.contract.column.validTo'),
      width: 120,
      render: (row) => h('span', fmtDate(row.validTo)),
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
      width: 150,
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
              canEdit(row)
                ? withDirectives(
                    h(
                      NButton,
                      { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                      { default: () => t('advertising.order.edit') }
                    ),
                    [[permissionDirective, ['AD_CONTRACT:UPDATE']]]
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
    searchForm.contractDirection = null;
    searchForm.contractType = null;
    searchForm.sealStatus = null;
    handleSearch();
  }
  function goCreate() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_CREATE });
  }

  function applyQuery() {
    const q = router.currentRoute.value.query;
    if (q.contractDirection != null && q.contractDirection !== '') searchForm.contractDirection = Number(q.contractDirection);
    if (q.contractType != null && q.contractType !== '') searchForm.contractType = Number(q.contractType);
    if (q.sealStatus != null && q.sealStatus !== '') searchForm.sealStatus = Number(q.sealStatus);
  }

  onMounted(() => {
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
