<template>
  <div class="advertising-page">
    <n-card :bordered="false">
      <n-space justify="space-between" align="center" class="mb-4">
        <n-tabs v-model:value="currentType" type="line">
          <n-tab-pane name="order" :tab="t('advertising.approval.tab.order')" />
          <n-tab-pane name="change" :tab="t('advertising.approval.tab.change')" />
          <n-tab-pane name="seal" :tab="t('advertising.approval.tab.seal')" />
        </n-tabs>
        <n-button type="primary" :disabled="!checkedRowKeys.length" @click="handleBatchApprove">
          {{ t('advertising.approval.batchApprove') }} ({{ checkedRowKeys.length }})
        </n-button>
      </n-space>

      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :row-key="(row: any) => row.id"
        :checked-row-keys="checkedRowKeys"
        remote
        @update:checked-row-keys="onChecked"
      />
    </n-card>

    <n-modal
      v-model:show="rejectModalVisible"
      preset="dialog"
      :title="t('advertising.approval.confirmReject')"
      :positive-text="t('advertising.approval.reject')"
      :negative-text="t('advertising.common.cancel')"
      @positive-click="confirmReject"
    >
      <n-input v-model:value="rejectRemark" type="textarea" :placeholder="t('advertising.approval.rejectRemark')" />
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { h, onMounted, reactive, ref, watch } from 'vue';
  import { NButton, NCard, NDataTable, NInput, NModal, NSpace, NTabPane, NTabs, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type {
    AdApprovalPageParams,
    AdApprovalPageResult,
    AdApprovalTodoItem,
    AdApprovalType,
  } from '@lib/shared/models/advertising';

  import {
    approveAdOrder,
    approveAdOrderChange,
    approveAdSeal,
    getAdApprovalPendingPage,
    rejectAdOrder,
    rejectAdOrderChange,
    rejectAdSeal,
  } from '@/api/modules';

  import { fmtAmount, fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const message = useMessage();

  const loading = ref(false);
  const list = ref<AdApprovalTodoItem[]>([]);
  const currentType = ref<AdApprovalType>('order');
  const checkedRowKeys = ref<string[]>([]);

  const rejectModalVisible = ref(false);
  const rejectRemark = ref('');
  const rejectTarget = ref<AdApprovalTodoItem | null>(null);

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
      const params: AdApprovalPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        type: currentType.value,
      };
      const res = await getAdApprovalPendingPage(params);
      list.value = (res?.list as AdApprovalTodoItem[]) || [];
      pagination.itemCount = res?.total || 0;
      checkedRowKeys.value = [];
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function onChecked(keys: Array<string | number>) {
    checkedRowKeys.value = keys.map(String);
  }

  function resolveType(row: AdApprovalTodoItem): AdApprovalType {
    return (row.type as AdApprovalType) || currentType.value;
  }

  // 通过：按 type 调用各业务模块审批接口（受 AD_ORDER:APPROVE / AD_ORDER_CHANGE:APPROVE / AD_SEAL:APPROVE 保护）
  async function approveOne(row: AdApprovalTodoItem, remark?: string): Promise<void> {
    const id = row.businessId || row.id;
    if (!id) return;
    const type = resolveType(row);
    if (type === 'order') {
      await approveAdOrder(id, { action: 'approve', remark });
    } else if (type === 'change') {
      await approveAdOrderChange(id, { remark });
    } else {
      await approveAdSeal(id, { approveRemark: remark });
    }
  }

  // 驳回：同上
  async function rejectOne(row: AdApprovalTodoItem, remark?: string): Promise<void> {
    const id = row.businessId || row.id;
    if (!id) return;
    const type = resolveType(row);
    if (type === 'order') {
      await rejectAdOrder(id, { action: 'reject', remark });
    } else if (type === 'change') {
      await rejectAdOrderChange(id, { remark });
    } else {
      await rejectAdSeal(id, { approveRemark: remark });
    }
  }

  async function handleApprove(row: AdApprovalTodoItem) {
    try {
      await approveOne(row);
      message.success(t('advertising.common.operateSuccess'));
      fetchData();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  function openReject(row: AdApprovalTodoItem) {
    rejectTarget.value = row;
    rejectRemark.value = '';
    rejectModalVisible.value = true;
  }

  async function confirmReject() {
    if (!rejectTarget.value) return;
    try {
      await rejectOne(rejectTarget.value, rejectRemark.value);
      message.success(t('advertising.common.operateSuccess'));
      rejectModalVisible.value = false;
      fetchData();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  async function handleBatchApprove() {
    const rows = list.value.filter((r) => r.id && checkedRowKeys.value.includes(r.id));
    if (!rows.length) return;
    try {
      await Promise.all(rows.map((r) => approveOne(r)));
      message.success(t('advertising.common.operateSuccess'));
      checkedRowKeys.value = [];
      fetchData();
    } catch (e) {
      message.error((e as Error).message || '批量操作失败');
    }
  }

  const columns: DataTableColumn<AdApprovalTodoItem>[] = [
    { type: 'selection' },
    { key: 'applicantName', title: t('advertising.approval.column.applicant'), width: 120 },
    { key: 'refNo', title: t('advertising.approval.column.refNo'), width: 160, ellipsis: { tooltip: true } },
    {
      key: 'amount',
      title: t('advertising.approval.column.amount'),
      width: 140,
      align: 'right',
      render: (row) => h('span', row.amount != null ? fmtAmount(row.amount) : '-'),
    },
    {
      key: 'submitTime',
      title: t('advertising.approval.column.submitTime'),
      width: 170,
      render: (row) => h('span', fmtDateTime(row.submitTime)),
    },
    {
      key: 'summary',
      title: t('advertising.approval.column.summary'),
      minWidth: 160,
      ellipsis: { tooltip: true },
      render: (row) => h('span', row.summary || '-'),
    },
    {
      key: 'action',
      title: t('advertising.order.detail'),
      width: 160,
      fixed: 'right' as const,
      render: (row) =>
        h(
          NSpace,
          {},
          {
            default: () => [
              h(
                NButton,
                { size: 'small', type: 'primary', onClick: () => handleApprove(row) },
                { default: () => t('advertising.approval.approve') }
              ),
              h(
                NButton,
                { size: 'small', type: 'error', onClick: () => openReject(row) },
                { default: () => t('advertising.approval.reject') }
              ),
            ],
          }
        ),
    },
  ];

  watch(currentType, () => {
    pagination.page = 1;
    fetchData();
  });

  onMounted(fetchData);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .mb-4 {
    margin-bottom: 16px;
  }
</style>
