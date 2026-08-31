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
        <n-select
          v-model:value="searchForm.missingContract"
          :placeholder="t('advertising.order.filter.missingContract')"
          :options="missingContractOptions"
          clearable
          style="width: 130px"
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
        <n-button v-permission="['AD_ORDER:EXPORT']" :loading="exporting" @click="handleExport">导出</n-button>
        <ColumnSetting
          :table-key="TableKeyEnum.AD_ORDER"
          :disabled="false"
          no-pagination
          @change-columns-setting="refreshColumns"
        />
        <n-button v-permission="['AD_ORDER:CREATE']" type="primary" @click="goCreate">{{
          t('advertising.order.new')
        }}</n-button>
      </n-space>
    </n-card>

    <n-card :bordered="false" class="mt-4">
      <n-data-table
        :columns="visibleColumns"
        :data="list"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: any) => row.id"
        :scroll-x="scrollX"
        :size="tableSize"
        remote
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref, resolveDirective, withDirectives } from 'vue';
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
  import { TableKeyEnum } from '@lib/shared/enums/tableEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderListItem, AdOrderPageParams } from '@lib/shared/models/advertising';

  import ColumnSetting from '@/components/pure/crm-table/components/columnSetting.vue';

  import { exportAdOrder, getAdOrderPage } from '@/api/modules';
  import useTableStore from '@/hooks/useTableStore';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount, fmtDate, fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  // 列设置所需字段：不直接引用 crm-table 的 CrmDataTableColumn，
  // 因该模块经 @/ 别名在 vue-tsc 下解析失败会导致 render 参数退化为 any。
  // DataTableColumn 是联合类型（选择列无 key/fixed 等），故此处显式补齐列设置要用的字段。
  type AdOrderTableColumn = DataTableColumn<AdOrderListItem> & {
    key?: string | number;
    showInTable?: boolean;
    columnSelectorDisabled?: boolean;
    fixed?: 'left' | 'right' | false;
    width?: number | string;
    minWidth?: number | string;
  };

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();
  const permissionDirective = resolveDirective('permission');

  const statusOptions = AdOrderStatusOptions;
  const orderTypeOptions = AdOrderTypeOptions;
  const receiptMethodOptions = AdReceiptMethodOptions;
  const paymentMethodOptions = AdPaymentMethodOptions;
  const missingContractOptions = [
    { label: t('advertising.common.unsubmitted'), value: 1 },
    { label: t('advertising.common.submitted'), value: 0 },
  ];

  const loading = ref(false);
  const list = ref<AdOrderListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
    status: null as number | null,
    orderType: null as number | null,
    receiptMethod: null as number | null,
    paymentMethod: null as number | null,
    missingContract: null as number | null,
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
        missingContract: searchForm.missingContract,
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
      case 45:
      case 50:
      case 60:
        return 'info';
      case 80:
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

  // 全量列定义：showInTable=false 表示默认隐藏，columnSelectorDisabled=true 表示锁定不可隐藏
  const allColumns: AdOrderTableColumn[] = [
    {
      key: 'orderNo',
      title: t('advertising.order.column.orderNo'),
      width: 150,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      key: 'orderName',
      title: t('advertising.order.column.orderName'),
      minWidth: 160,
      ellipsis: { tooltip: true },
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      key: 'upstreamAgentName',
      title: t('advertising.order.column.upstreamAgent'),
      width: 140,
      ellipsis: { tooltip: true },
      showInTable: false,
    },
    {
      key: 'customerName',
      title: t('advertising.order.column.customer'),
      width: 120,
      ellipsis: { tooltip: true },
      showInTable: true,
    },
    {
      key: 'orderType',
      title: t('advertising.order.column.orderType'),
      width: 100,
      showInTable: false,
      render: (row) => h('span', getAdOrderTypeLabel(row.orderType)),
    },
    {
      key: 'status',
      title: t('advertising.order.column.status'),
      width: 110,
      showInTable: true,
      render: (row) =>
        h(NTag, { type: statusTagType(row.status) }, { default: () => getAdOrderStatusLabel(row.status) }),
    },
    {
      key: 'totalAmount',
      title: t('advertising.order.column.totalAmount'),
      width: 130,
      align: 'right',
      showInTable: true,
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.totalAmount)),
    },
    {
      key: 'receivableAmount',
      title: t('advertising.order.column.receivable'),
      width: 130,
      align: 'right',
      showInTable: true,
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.receivableAmount)),
    },
    {
      key: 'mediaPayableAmount',
      title: t('advertising.order.column.mediaPayable'),
      width: 130,
      align: 'right',
      showInTable: true,
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.mediaPayableAmount)),
    },
    {
      key: 'actualMediaPayableAmount',
      title: t('advertising.order.column.actualMediaPayable'),
      width: 130,
      align: 'right',
      showInTable: true,
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.actualMediaPayableAmount)),
    },
    {
      key: 'rebateAmount',
      title: t('advertising.order.column.rebate'),
      width: 110,
      align: 'right',
      showInTable: false,
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.rebateAmount)),
    },
    {
      key: 'mediaRebateAmount',
      title: t('advertising.order.column.mediaRebate'),
      width: 110,
      align: 'right',
      showInTable: false,
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.mediaRebateAmount)),
    },
    {
      key: 'orderIncomeAmount',
      title: t('advertising.order.column.orderIncome'),
      width: 130,
      align: 'right',
      showInTable: false,
      render: (row) => h('span', { style: 'display: block' }, fmtAmount(row.orderIncomeAmount)),
    },
    {
      key: 'receiptMethod',
      title: t('advertising.order.column.receiptMethod'),
      width: 100,
      showInTable: false,
      render: (row) => h('span', getAdReceiptMethodLabel(row.receiptMethod)),
    },
    {
      key: 'paymentMethod',
      title: t('advertising.order.column.paymentMethod'),
      width: 100,
      showInTable: false,
      render: (row) => h('span', getAdPaymentMethodLabel(row.paymentMethod)),
    },
    {
      key: 'deliveryStartDate',
      title: t('advertising.order.column.deliveryStart'),
      width: 120,
      showInTable: true,
      render: (row) => h('span', fmtDate(row.deliveryStartDate)),
    },
    {
      key: 'deliveryEndDate',
      title: '投放结束',
      width: 110,
      showInTable: false,
      render: (row) => h('span', fmtDate(row.deliveryEndDate)),
    },
    {
      key: 'createTime',
      title: t('advertising.order.column.createTime'),
      width: 160,
      showInTable: false,
      render: (row) => h('span', fmtDateTime(row.createTime)),
    },
    {
      key: 'receiptDone',
      title: t('advertising.order.column.receiptDone'),
      width: 90,
      fixed: 'right' as const,
      showInTable: true,
      columnSelectorDisabled: true,
      render: (row) =>
        h(
          NTag,
          { type: row.receiptDone === 1 ? 'success' : 'warning' },
          {
            default: () =>
              row.receiptDone === 1 ? t('advertising.common.received') : t('advertising.common.unreceived'),
          }
        ),
    },
    {
      key: 'paymentDone',
      title: t('advertising.order.column.paymentDone'),
      width: 90,
      fixed: 'right' as const,
      showInTable: true,
      columnSelectorDisabled: true,
      render: (row) =>
        h(
          NTag,
          { type: row.paymentDone === 1 ? 'success' : 'warning' },
          {
            default: () => (row.paymentDone === 1 ? t('advertising.common.paid') : t('advertising.common.unpaid')),
          }
        ),
    },
    {
      key: 'missingContract',
      title: t('advertising.order.column.contractStatus'),
      width: 90,
      fixed: 'right' as const,
      showInTable: true,
      columnSelectorDisabled: true,
      render: (row) =>
        row.missingContract === 1
          ? h(NTag, { type: 'warning' }, { default: () => t('advertising.common.unsubmitted') })
          : h(NTag, { type: 'success' }, { default: () => t('advertising.common.submitted') }),
    },
    {
      key: 'action',
      title: '详情',
      width: 150,
      fixed: 'right' as const,
      showInTable: true,
      columnSelectorDisabled: true,
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
                ? withDirectives(
                    h(
                      NButton,
                      { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                      { default: () => t('advertising.order.edit') }
                    ),
                    [[permissionDirective, ['AD_ORDER:CREATE']]]
                  )
                : null,
            ],
          }
        ),
    },
  ];

  const tableStore = useTableStore();
  // 实际渲染到表格的列（由列设置面板持久化配置决定）
  const visibleColumns = ref<AdOrderTableColumn[]>(allColumns.filter((c) => c.showInTable !== false));
  // 行高：与列设置面板的「列高」tab 联动（compact -> small / loose -> medium）
  const tableSize = ref<'small' | 'medium'>('small');

  // 列宽之和 + 余量，避免列隐藏后仍横向滚动
  const scrollX = computed(() => {
    const total = visibleColumns.value.reduce((sum: number, c: AdOrderTableColumn) => {
      const col = c as { width?: number | string; minWidth?: number | string };
      const w = col.width ?? col.minWidth ?? 120;
      return sum + (Number(w) || 120);
    }, 0);
    return total + 100;
  });

  async function refreshColumns() {
    const stored = await tableStore.getShowInTableColumns(TableKeyEnum.AD_ORDER);
    // 只用持久化结果判断「显示哪些列」与「可拖拽列的顺序」，列对象本身始终取本地 allColumns：
    // 1) 持久化的 render 经 toString 存 IndexedDB 后无法还原闭包，直接渲染会报错；
    // 2) setColumns 与 sortByOldOrder 都会把 columnSelectorDisabled 的锁定列前置，
    //    直接沿用持久化顺序会导致「收款/付款/合同状态、详情」跑到表格最前面。
    const fallback = allColumns.filter((c) => c.showInTable !== false);
    if (!stored.length) {
      visibleColumns.value = fallback;
    } else {
      const localMap = new Map(allColumns.map((c) => [String(c.key), c]));
      const lockedKeys = new Set(
        allColumns
          .filter((c: AdOrderTableColumn) => c.columnSelectorDisabled)
          .map((c: AdOrderTableColumn) => String(c.key))
      );
      type StoredColumn = {
        key?: string | number;
        fixed?: AdOrderTableColumn['fixed'];
        width?: number | string;
      };
      // 持久化的 fixed（图钉）/ width 需要还原到非锁定列上，否则用户在面板里的设置不生效
      // stored 来自 @/hooks/useTableStore（vue-tsc 下别名解析失败为 any），需显式标注 Map 泛型
      const storedMap = new Map<string, StoredColumn>(
        stored.map((s: StoredColumn) => [String(s.key), s] as [string, StoredColumn])
      );
      const shownKeys = new Set(stored.map((s: StoredColumn) => String(s.key)));
      // 可拖拽（非锁定）列：按用户在面板中调整后的顺序
      const draggableQueue = stored
        .map((s: StoredColumn) => String(s.key))
        .filter((k: string) => !lockedKeys.has(k) && localMap.has(k))
        .map((k: string) => localMap.get(k) as AdOrderTableColumn)
        .filter((c: AdOrderTableColumn) => !!c);
      // 以 allColumns 为骨架：锁定列留在原位，可拖拽位置依次填入用户排序
      const result: AdOrderTableColumn[] = [];
      allColumns.forEach((c: AdOrderTableColumn) => {
        const key = String(c.key);
        if (!shownKeys.has(key)) {
          return;
        }
        if (lockedKeys.has(key)) {
          // 锁定列同样还原图钉设置：面板已允许锁定列切换固定（未固定 / 原始方向）
          const lockedSaved = storedMap.get(key);
          let lockedFixed = lockedSaved ? lockedSaved.fixed : c.fixed;
          // 代码定义为 right 的锁定列（收款/付款/合同状态、详情）禁止被改成 left，
          // 否则会破坏「后几列固定在末尾」的布局
          if (c.fixed === 'right' && lockedFixed === 'left') {
            lockedFixed = 'right';
          }
          result.push({
            ...c,
            fixed: lockedFixed,
            width: lockedSaved?.width ?? c.width,
          });
        } else {
          const next = draggableQueue.shift();
          if (next) {
            const saved = storedMap.get(String(next.key));
            result.push({
              ...next,
              fixed: saved?.fixed ?? next.fixed,
              width: saved?.width ?? next.width,
            });
          }
        }
      });
      visibleColumns.value = result.length ? result : fallback;
    }
    const layout = await tableStore.getTableLineHeight(TableKeyEnum.AD_ORDER);
    tableSize.value = layout === 'loose' ? 'medium' : 'small';
  }

  async function initColumns() {
    await tableStore.initColumn(TableKeyEnum.AD_ORDER, allColumns);
    await refreshColumns();
  }

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
    searchForm.missingContract = null;
    deliveryRange.value = null;
    handleSearch();
  }
  function goCreate() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_ORDER_CREATE });
  }

  const exporting = ref(false);

  // 导出列 = 全量列（排除「详情」操作列），与页面可见列无关：导出用于对账需完整字段
  function buildExportHeadList() {
    return (allColumns as any[])
      .filter((c) => c.key !== 'action')
      .map((c) => ({ key: String(c.key), title: String(c.title) }));
  }

  /**
   * 触发浏览器下载：从 CDR 原生响应中取出 blob -> URL.createObjectURL -> 临时 a.click() -> revoke
   */
  async function downloadExcel(res: any) {
    const blob: Blob = res.data;
    if (!blob || blob.size === 0) {
      throw new Error('导出文件为空');
    }
    const dispo: string = res.headers?.['content-disposition'] || res.headers?.get?.('content-disposition') || '';
    const filenameMatch = /filename\*?=(?:UTF-8'')?["']?([^;"']+)/i.exec(dispo);
    const filename = filenameMatch ? decodeURIComponent(filenameMatch[1]) : '广告订单.xlsx';

    const blobUrl = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = blobUrl;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(blobUrl);
  }

  async function handleExport() {
    exporting.value = true;
    try {
      const params: AdOrderPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        status: searchForm.status,
        orderType: searchForm.orderType,
        receiptMethod: searchForm.receiptMethod,
        paymentMethod: searchForm.paymentMethod,
        missingContract: searchForm.missingContract,
        deliveryStartFrom: deliveryRange.value?.[0] ?? null,
        deliveryStartTo: deliveryRange.value?.[1] ?? null,
        headList: buildExportHeadList(),
      };
      const res = await exportAdOrder(params);
      await downloadExcel(res);
      message.success('导出成功，浏览器已开始下载');
    } catch (e) {
      message.error((e as Error).message || '导出失败');
    } finally {
      exporting.value = false;
    }
  }

  function applyQuery() {
    const q = router.currentRoute.value.query;
    if (q.status != null && q.status !== '') searchForm.status = Number(q.status);
    if (q.orderType != null && q.orderType !== '') searchForm.orderType = Number(q.orderType);
    if (q.receiptMethod != null && q.receiptMethod !== '') searchForm.receiptMethod = Number(q.receiptMethod);
    if (q.paymentMethod != null && q.paymentMethod !== '') searchForm.paymentMethod = Number(q.paymentMethod);
    if (q.missingContract != null && q.missingContract !== '') searchForm.missingContract = Number(q.missingContract);
  }

  onMounted(() => {
    applyQuery();
    fetchData();
    initColumns();
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
