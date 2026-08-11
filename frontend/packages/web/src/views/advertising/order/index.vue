<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          :placeholder="t('advertising.order.search')"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.status"
          :placeholder="t('advertising.order.filter.status')"
          :options="statusOptions"
          clearable
          style="width: 150px"
        />
        <n-select
          v-model:value="searchForm.orderType"
          :placeholder="t('advertising.order.filter.orderType')"
          :options="orderTypeOptions"
          clearable
          style="width: 150px"
        />
        <n-select
          v-model:value="searchForm.receiptMethod"
          :placeholder="t('advertising.order.filter.receiptMethod')"
          :options="receiptMethodOptions"
          clearable
          style="width: 150px"
        />
        <n-select
          v-model:value="searchForm.paymentMethod"
          :placeholder="t('advertising.order.filter.paymentMethod')"
          :options="paymentMethodOptions"
          clearable
          style="width: 150px"
        />
        <n-date-picker
          v-model:value="deliveryRange"
          type="daterange"
          clearable
          :placeholder="t('advertising.order.column.deliveryStart')"
          style="width: 240px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">{{ t('advertising.order.reset') }}</n-button>
        <n-button type="primary" @click="goCreate">{{ t('advertising.order.new') }}</n-button>
      </n-space>
    </n-card>

    <n-card :bordered="false" class="mt-4">
      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: any) => row.id"
        :scroll-x="2200"
        remote
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
  import { h, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { NButton, NCard, NDataTable, NDatePicker, NInput, NSelect, NSpace, NTag, useMessage } from 'naive-ui';

  import {
    AdOrderStatusOptions,
    AdOrderTypeOptions,
    AdPaymentMethodOptions,
    AdReceiptMethodOptions,
    getAdOrderStatusLabel,
    getAdOrderTypeLabel,
    getAdPaymentMethodLabel,
    getAdReceiptMethodLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderListItem, AdOrderPageParams } from '@lib/shared/models/advertising';

  import { getAdOrderPage } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount, fmtDate, fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const statusOptions = AdOrderStatusOptions;
  const orderTypeOptions = AdOrderTypeOptions;
  const receiptMethodOptions = AdReceiptMethodOptions;
  const paymentMethodOptions = AdPaymentMethodOptions;

  const loading = ref(false);
  const list = ref<AdOrderListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
    status: null as number | null,
    orderType: null as number | null,
    receiptMethod: null as number | null,
    paymentMethod: null as number | null,
  });
  const deliveryRange = ref<[number, number] | null>(null);

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
      const params: AdOrderPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        status: searchForm.status,
        orderType: searchForm.orderType,
        receiptMethod: searchForm.receiptMethod,
        paymentMethod: searchForm.paymentMethod,
        deliveryStartFrom: deliveryRange.value?.[0] ?? null,
        deliveryStartTo: deliveryRange.value?.[1] ?? null,
      };
      const res = await getAdOrderPage(params);
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
        return 'default';
      case 10:
        return 'warning';
      case 20:
      case 50:
        return 'info';
      case 70:
      case 90:
        return 'success';
      case 100:
        return 'error';
      default:
        return 'default';
    }
  }

  function openDetail(row: AdOrderListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_ORDER_DETAIL, params: { id: row.id } });
  }
  function openEdit(row: AdOrderListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_ORDER_EDIT, params: { id: row.id } });
  }

  const columns: DataTableColumn<AdOrderListItem>[] = [
    { key: 'orderNo', title: t('advertising.order.column.orderNo'), width: 150 },
    { key: 'orderName', title: t('advertising.order.column.orderName'), minWidth: 160, ellipsis: { tooltip: true } },
    {
      key: 'businessEntityName',
      title: t('advertising.order.column.businessEntity'),
      width: 140,
      ellipsis: { tooltip: true },
    },
    { key: 'customerName', title: t('advertising.order.column.customer'), width: 120, ellipsis: { tooltip: true } },
    {
      key: 'orderType',
      title: t('advertising.order.column.orderType'),
      width: 100,
      render: (row) => h('span', getAdOrderTypeLabel(row.orderType)),
    },
    {
      key: 'status',
      title: t('advertising.order.column.status'),
      width: 110,
      render: (row) =>
        h(NTag, { type: statusTagType(row.status) }, { default: () => getAdOrderStatusLabel(row.status) }),
    },
    {
      key: 'totalAmount',
      title: t('advertising.order.column.totalAmount'),
      width: 130,
      align: 'right',
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.totalAmount)),
    },
    {
      key: 'receivableAmount',
      title: t('advertising.order.column.receivable'),
      width: 130,
      align: 'right',
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.receivableAmount)),
    },
    {
      key: 'mediaPayableAmount',
      title: t('advertising.order.column.mediaPayable'),
      width: 130,
      align: 'right',
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.mediaPayableAmount)),
    },
    {
      key: 'rebateAmount',
      title: t('advertising.order.column.rebate'),
      width: 110,
      align: 'right',
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.rebateAmount)),
    },
    {
      key: 'receiptMethod',
      title: t('advertising.order.column.receiptMethod'),
      width: 100,
      render: (row) => h('span', getAdReceiptMethodLabel(row.receiptMethod)),
    },
    {
      key: 'paymentMethod',
      title: t('advertising.order.column.paymentMethod'),
      width: 100,
      render: (row) => h('span', getAdPaymentMethodLabel(row.paymentMethod)),
    },
    {
      key: 'deliveryStartDate',
      title: t('advertising.order.column.deliveryStart'),
      width: 120,
      render: (row) => h('span', fmtDate(row.deliveryStartDate)),
    },
    {
      key: 'createTime',
      title: t('advertising.order.column.createTime'),
      width: 160,
      render: (row) => h('span', fmtDateTime(row.createTime)),
    },
    {
      key: 'missingContract',
      title: t('advertising.order.column.missingContract'),
      width: 90,
      fixed: 'right' as const,
      render: (row) =>
        row.missingContract === 1
          ? h(NTag, { type: 'warning' }, { default: () => t('advertising.common.yes') })
          : h('span', t('advertising.common.no')),
    },
    {
      key: 'action',
      title: '详情',
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
              row.status === 0
                ? h(
                    NButton,
                    { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                    { default: () => t('advertising.order.edit') }
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
    searchForm.orderType = null;
    searchForm.receiptMethod = null;
    searchForm.paymentMethod = null;
    deliveryRange.value = null;
    handleSearch();
  }
  function goCreate() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_ORDER_CREATE });
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
