<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          :placeholder="t('advertising.payment.search')"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.direction"
          :placeholder="t('advertising.payment.filter.direction')"
          :options="directionOptions"
          clearable
          style="width: 150px"
        />
        <n-select
          v-model:value="searchForm.type"
          :placeholder="t('advertising.payment.filter.type')"
          :options="typeOptions"
          clearable
          style="width: 150px"
        />
        <n-date-picker
          v-model:value="occurRange"
          type="daterange"
          clearable
          :placeholder="t('advertising.payment.column.occurDate')"
          style="width: 240px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">{{ t('advertising.order.reset') }}</n-button>
        <n-button type="primary" @click="goCreate">{{ t('advertising.payment.new') }}</n-button>
        <n-button @click="goMedia">{{ t('advertising.payment.media') }}</n-button>
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

    <n-modal
      v-model:show="cancelModal.show"
      title="撤销/冲销"
      preset="card"
      positive-text="确定"
      negative-text="取消"
      style="width: 460px"
      @positive-click="handleCancelConfirm"
    >
      <n-input
        v-model:value="cancelModal.reason"
        type="textarea"
        :rows="3"
        :placeholder="t('advertising.common.reason')"
      />
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { h, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { NButton, NCard, NDataTable, NDatePicker, NInput, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui';

  import {
    AdPaymentDirectionOptions,
    AdPaymentTypeOptions,
    getAdPaymentDirectionLabel,
    getAdPaymentTypeLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdPaymentRecordListItem, AdPaymentRecordPageParams } from '@lib/shared/models/advertising';

  import { cancelAdPayment, getAdPaymentPage } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount, fmtDate } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const directionOptions = AdPaymentDirectionOptions;
  const typeOptions = AdPaymentTypeOptions;

  const loading = ref(false);
  const list = ref<AdPaymentRecordListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
    direction: null as number | null,
    type: null as number | null,
  });
  const occurRange = ref<[number, number] | null>(null);

  const cancelModal = reactive<{ show: boolean; id: string; reason: string }>({ show: false, id: '', reason: '' });

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
      const params: AdPaymentRecordPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        direction: searchForm.direction,
        type: searchForm.type,
        occurDateFrom: occurRange.value?.[0] ?? null,
        occurDateTo: occurRange.value?.[1] ?? null,
      };
      const res = await getAdPaymentPage(params);
      list.value = res.list || [];
      pagination.itemCount = res.total || 0;
    } catch (e) {
      message.error((e as Error).message || '查询失败');
    } finally {
      loading.value = false;
    }
  }

  function openDetail(row: AdPaymentRecordListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_PAYMENT_DETAIL, params: { id: row.id } });
  }
  function openCancel(row: AdPaymentRecordListItem) {
    cancelModal.id = row.id;
    cancelModal.reason = '';
    cancelModal.show = true;
  }

  const columns: DataTableColumn<AdPaymentRecordListItem>[] = [
    { key: 'orderNo', title: t('advertising.payment.column.orderNo'), width: 150, ellipsis: { tooltip: true } },
    {
      key: 'businessEntityName',
      title: t('advertising.payment.column.businessEntity'),
      width: 130,
      ellipsis: { tooltip: true },
    },
    { key: 'customerName', title: t('advertising.payment.column.customer'), width: 120, ellipsis: { tooltip: true } },
    { key: 'resourceName', title: t('advertising.payment.column.resource'), width: 130, ellipsis: { tooltip: true } },
    {
      key: 'direction',
      title: t('advertising.payment.column.direction'),
      width: 110,
      render: (row) =>
        h(
          NTag,
          { type: row.direction === 10 ? 'success' : 'warning' },
          { default: () => getAdPaymentDirectionLabel(row.direction) }
        ),
    },
    {
      key: 'type',
      title: t('advertising.payment.column.type'),
      width: 110,
      render: (row) => h(NTag, {}, { default: () => getAdPaymentTypeLabel(row.type) }),
    },
    {
      key: 'amount',
      title: t('advertising.payment.column.amount'),
      width: 130,
      align: 'right',
      render: (row) => h('span', fmtAmount(row.amount)),
    },
    {
      key: 'occurDate',
      title: t('advertising.payment.column.occurDate'),
      width: 120,
      render: (row) => h('span', fmtDate(row.occurDate)),
    },
    { key: 'invoiceNo', title: t('advertising.payment.column.invoiceNo'), width: 140, ellipsis: { tooltip: true } },
    { key: 'operatorId', title: t('advertising.payment.column.operator'), width: 120 },
    {
      key: 'action',
      title: t('advertising.payment.detail'),
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
                { default: () => t('advertising.payment.detail') }
              ),
              h(
                NButton,
                { size: 'small', type: 'error', onClick: () => openCancel(row) },
                { default: () => t('advertising.payment.cancel') }
              ),
            ],
          }
        ),
    },
  ];

  async function handleCancelConfirm() {
    try {
      await cancelAdPayment({ id: cancelModal.id, reason: cancelModal.reason });
      message.success(t('advertising.common.operateSuccess'));
      cancelModal.show = false;
      await fetchData();
    } catch (e) {
      message.error((e as Error).message || '撤销失败');
    }
  }

  function handleSearch() {
    pagination.page = 1;
    fetchData();
  }
  function handleReset() {
    searchForm.keyword = '';
    searchForm.direction = null;
    searchForm.type = null;
    occurRange.value = null;
    handleSearch();
  }
  function goCreate() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_PAYMENT_CREATE });
  }
  function goMedia() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_PAYMENT_MEDIA });
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
