<template>
  <div class="advertising-page">
    <n-card :bordered="false">
      <n-tabs v-model:value="currentType" type="line" @update:value="onTypeChange">
        <n-tab-pane name="order" :tab="t('advertising.approval.tab.order')" />
        <n-tab-pane name="change" :tab="t('advertising.approval.tab.change')" />
        <n-tab-pane name="seal" :tab="t('advertising.approval.tab.seal')" />
        <n-tab-pane name="archive" :tab="t('advertising.approval.tab.archive')" />
        <n-tab-pane name="receipt" :tab="t('advertising.approval.tab.receipt')" />
        <n-tab-pane name="payout" :tab="t('advertising.approval.tab.payout')" />
      </n-tabs>

      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :row-key="(row: any) => row.id"
        :pagination="pagination"
        remote
      />
    </n-card>

    <!-- 驳回弹窗 -->
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

    <!-- 详情抽屉 -->
    <n-drawer
      v-model:show="detailVisible"
      :width="760"
      placement="right"
      :title="t('advertising.approval.detail.title')"
    >
      <n-drawer-content>
        <n-spin :show="detailLoading">
          <template v-if="detailBaseFields.length">
            <n-descriptions :column="1" label-placement="left" bordered>
              <n-descriptions-item v-for="(f, idx) in detailBaseFields" :key="idx" :label="f.label">
                <n-button v-if="f.link" text type="primary" size="small" @click="openLink(f)">
                  {{ f.value }}
                </n-button>
                <span v-else>{{ f.value }}</span>
              </n-descriptions-item>
            </n-descriptions>

            <!-- 改单字段变更对比 -->
            <template v-if="detailCompareRows.length">
              <n-divider title-placement="left">字段变更对比</n-divider>
              <n-table :bordered="true" size="small" :single-line="false">
                <thead>
                  <tr>
                    <th style="width: 150px">变更字段</th>
                    <th>原值（改前）</th>
                    <th>新值（改后）</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, idx) in detailCompareRows" :key="idx">
                    <td>{{ row.label }}</td>
                    <td class="before-value">{{ row.before }}</td>
                    <td class="after-value">{{ row.after }}</td>
                  </tr>
                </tbody>
              </n-table>
            </template>

            <!-- 付款单：各下游客户付款返点明细 -->
            <template v-if="detailType === 'payout' && detailMediaList.length">
              <n-divider title-placement="left">各下游客户付款返点明细</n-divider>
              <n-table :bordered="true" size="small" :single-line="false">
                <thead>
                  <tr>
                    <th style="width: 50px">序号</th>
                    <th>下游客户</th>
                    <th style="width: 90px">应付</th>
                    <th style="width: 90px">不记返</th>
                    <th style="width: 90px">返点方式</th>
                    <th style="width: 80px">返点值</th>
                    <th style="width: 90px">返点金额</th>
                    <th style="width: 90px">实际应付</th>
                    <th style="width: 100px">本次付款</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(m, idx) in detailMediaList" :key="idx">
                    <td>{{ idx + 1 }}</td>
                    <td>{{ m.mediaName || m.mediaId || '-' }}</td>
                    <td>¥{{ fmtPayoutMoney(m.payableAmount) }}</td>
                    <td>¥{{ fmtPayoutMoney(m.noRebateAmount) }}</td>
                    <td>{{ rebateModeLabel(m.rebateMode) }}</td>
                    <td>{{ fmtRebateValue(m) }}</td>
                    <td>¥{{ fmtPayoutMoney(m.rebateAmount) }}</td>
                    <td>¥{{ fmtPayoutMoney(m.actualPayable) }}</td>
                    <td>¥{{ fmtPayoutMoney(m.paidAmount) }}</td>
                  </tr>
                </tbody>
              </n-table>
            </template>
          </template>
          <n-empty v-else-if="!detailLoading" :description="t('advertising.approval.empty')" />
        </n-spin>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDataTable,
    NDescriptions,
    NDescriptionsItem,
    NDivider,
    NDrawer,
    NDrawerContent,
    NEmpty,
    NInput,
    NModal,
    NSpace,
    NSpin,
    NTable,
    NTabPane,
    NTabs,
    useMessage,
  } from 'naive-ui';

  import { AD_ORDER_CHANGE_FIELD_META } from '@lib/shared/enums/advertisingEnum';
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
    approveAdPayout,
    approveAdReceipt,
    approveAdSeal,
    approveArchive,
    getAdApprovalPendingPage,
    getAdContractDetail,
    getAdOrderChangeDetail,
    getAdOrderDetail,
    getAdPayoutDetail,
    getAdReceiptDetail,
    getAdSealDetail,
    rejectAdOrder,
    rejectAdOrderChange,
    rejectAdSeal,
    rejectArchive,
  } from '@/api/modules';
  import { hasPermission } from '@/utils/permission';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount, fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const message = useMessage();
  const router = useRouter();

  const loading = ref(false);
  const list = ref<AdApprovalTodoItem[]>([]);
  const currentType = ref<AdApprovalType>('order');

  const rejectModalVisible = ref(false);
  const rejectRemark = ref('');
  const rejectTarget = ref<AdApprovalTodoItem | null>(null);

  const detailVisible = ref(false);
  const detailLoading = ref(false);
  const detailType = ref<string>('');
  // 基础字段：value 为显示文本，link 为跳转路由名（点击新页面查看）
  const detailBaseFields = ref<Array<{ label: string; value: string; link?: string; linkId?: string }>>([]);
  // 改单字段变更对比行
  const detailCompareRows = ref<Array<{ label: string; before: string; after: string }>>([]);
  // 付款单各下游客户付款明细
  const detailMediaList = ref<
    Array<{
      mediaName?: string;
      mediaId?: string;
      payableAmount?: number;
      noRebateAmount?: number;
      rebateMode?: number;
      rebateValue?: number;
      rebateAmount?: number;
      actualPayable?: number;
      paidAmount?: number;
    }>
  >([]);

  function fmtPayoutMoney(v?: number | null): string {
    if (v === null || v === undefined || v === '') return '0.00';
    const n = Number(v);
    if (Number.isNaN(n)) return '0.00';
    return n.toFixed(2);
  }
  function rebateModeLabel(v?: number): string {
    if (v === 10) return '比例';
    if (v === 20) return '固定金额';
    return '-';
  }
  function fmtRebateValue(m: { rebateMode?: number; rebateValue?: number | null }): string {
    if (m.rebateMode === 10) return `${fmtPayoutMoney(m.rebateValue)}%`;
    if (m.rebateMode === 20) return `¥${fmtPayoutMoney(m.rebateValue)}`;
    return '-';
  }

  /* eslint-disable no-use-before-define */
  const pagination = reactive({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    showSizePicker: true,
    pageSizes: [10, 20, 50],
    onChange: (page: number) => {
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
      const res = (await getAdApprovalPendingPage(params)) as AdApprovalPageResult;
      list.value = (res?.list as AdApprovalTodoItem[]) || [];
      pagination.itemCount = res?.total || 0;
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function onTypeChange() {
    pagination.page = 1;
    fetchData();
  }

  // 通过：按 type 调用各业务模块审批接口
  async function approveOne(row: AdApprovalTodoItem, remark?: string): Promise<void> {
    const id = row.businessId || row.id;
    if (!id) return;
    const { type } = row;
    if (type === 'order') {
      await approveAdOrder(id, { action: 'approve', remark });
    } else if (type === 'change') {
      await approveAdOrderChange(id, { remark });
    } else if (type === 'seal') {
      await approveAdSeal(id, { approveRemark: remark });
    } else if (type === 'archive') {
      await approveArchive(id, remark);
    } else if (type === 'receipt') {
      await approveAdReceipt(id, { action: 'approve', remark });
    } else if (type === 'payout') {
      await approveAdPayout(id, { action: 'approve', remark });
    }
  }

  // 驳回：同上
  async function rejectOne(row: AdApprovalTodoItem, remark?: string): Promise<void> {
    const id = row.businessId || row.id;
    if (!id) return;
    const { type } = row;
    if (type === 'order') {
      await rejectAdOrder(id, { action: 'reject', remark });
    } else if (type === 'change') {
      await rejectAdOrderChange(id, { remark });
    } else if (type === 'seal') {
      await rejectAdSeal(id, { approveRemark: remark });
    } else if (type === 'archive') {
      await rejectArchive(id, remark);
    } else if (type === 'receipt') {
      await approveAdReceipt(id, { action: 'reject', remark });
    } else if (type === 'payout') {
      await approveAdPayout(id, { action: 'reject', remark });
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

  // 各类型「审批通过」对应后端权限码
  function approvePermissionOf(type?: string): string {
    switch (type) {
      case 'order':
        return 'AD_ORDER:APPROVE';
      case 'change':
        return 'AD_ORDER_CHANGE:APPROVE';
      case 'seal':
        return 'AD_SEAL:APPROVE';
      case 'archive':
        return 'AD_CONTRACT:ARCHIVE_APPROVE';
      case 'receipt':
        return 'AD_RECEIPT:APPROVE';
      case 'payout':
        return 'AD_PAYOUT:APPROVE';
      default:
        return '';
    }
  }

  // 各类型「驳回」对应后端权限码（archive/receipt/payout 通过/驳回共用同一权限码）
  function rejectPermissionOf(type?: string): string {
    switch (type) {
      case 'order':
        return 'AD_ORDER:REJECT';
      case 'change':
        return 'AD_ORDER_CHANGE:REJECT';
      case 'seal':
        return 'AD_SEAL:REJECT';
      case 'archive':
        return 'AD_CONTRACT:ARCHIVE_APPROVE';
      case 'receipt':
        return 'AD_RECEIPT:APPROVE';
      case 'payout':
        return 'AD_PAYOUT:APPROVE';
      default:
        return '';
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

  type BaseField = { label: string; value: string; link?: string; linkId?: string };
  type CompareRow = { label: string; before: string; after: string };

  // 打开关联详情（新页面）
  function openLink(field: BaseField) {
    if (!field.link || !field.linkId) return;
    const { href } = router.resolve({ name: field.link, params: { id: field.linkId } });
    window.open(href, '_blank');
  }

  function parseSnapshot(value?: string | null): Record<string, any> {
    if (!value) return {};
    try {
      return JSON.parse(value);
    } catch {
      return {};
    }
  }

  function parseChangeFields(value?: string | null): string[] {
    if (!value) return [];
    if (value.trim().startsWith('[')) {
      try {
        const arr = JSON.parse(value);
        return Array.isArray(arr) ? arr : [];
      } catch {
        // ignore
      }
    }
    return value
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
  }

  function fmtFieldVal(v: any, meta?: { type?: string }): string {
    if (v === null || v === undefined || v === '') return '-';
    if (meta?.type === 'date') return String(v).slice(0, 10);
    return String(v);
  }

  // 解析改单快照/字段清单，生成「字段→改前→改后」对比行
  function buildChangeCompare(res: any): CompareRow[] {
    const change = res?.change || {};
    const before = parseSnapshot(change.snapshotBefore);
    const after = parseSnapshot(change.snapshotAfter);
    const fields = parseChangeFields(change.changeFields);
    return fields.map((field) => {
      const meta = AD_ORDER_CHANGE_FIELD_META.find((m) => m.field === field);
      return {
        label: meta?.label || field,
        before: fmtFieldVal(before[field], meta),
        after: fmtFieldVal(after[field], meta),
      };
    });
  }

  async function loadDetail(
    type: string,
    id: string
  ): Promise<{ base: BaseField[]; compare: CompareRow[]; mediaDetails?: any[] }> {
    if (type === 'order') {
      const res: any = await getAdOrderDetail(id);
      const o = res?.order || {};
      const contractLabel = res?.contractName || res?.contractNo || res?.contractId || '-';
      return {
        base: [
          {
            label: '订单编号',
            value: o.orderNo || '-',
            link: o.id ? AdvertisingRouteEnum.ADVERTISING_ORDER_DETAIL : undefined,
            linkId: o.id || id,
          },
          { label: '订单名称', value: o.orderName || '-' },
          {
            label: t('advertising.approval.column.amount'),
            value: o.totalAmount != null ? fmtAmount(o.totalAmount) : '-',
          },
          { label: '应收金额', value: o.receivableAmount != null ? fmtAmount(o.receivableAmount) : '-' },
          { label: '应付', value: o.mediaPayableAmount != null ? fmtAmount(o.mediaPayableAmount) : '-' },
          {
            label: '投放周期',
            value: o.deliveryStartDate && o.deliveryEndDate ? `${o.deliveryStartDate} ~ ${o.deliveryEndDate}` : '-',
          },
          {
            label: '关联合同',
            value: contractLabel,
            link: res?.contractId ? AdvertisingRouteEnum.ADVERTISING_CONTRACT_DETAIL : undefined,
            linkId: res?.contractId,
          },
          { label: '备注', value: o.remark || '-' },
        ],
        compare: [],
      };
    }
    if (type === 'change') {
      const res: any = await getAdOrderChangeDetail(id);
      const c = res?.change || {};
      return {
        base: [
          {
            label: '关联订单',
            value: res?.orderNo || res?.orderId || '-',
            link: res?.orderId ? AdvertisingRouteEnum.ADVERTISING_ORDER_DETAIL : undefined,
            linkId: res?.orderId,
          },
          { label: '订单名称', value: res?.orderName || '-' },
          { label: '变更原因', value: c.reason || '-' },
          { label: '状态', value: res?.statusLabel || '-' },
          { label: '审批备注', value: c.approveRemark || '-' },
        ],
        compare: buildChangeCompare(res),
      };
    }
    if (type === 'seal') {
      const res: any = await getAdSealDetail(id);
      const r = res?.record || {};
      return {
        base: [
          {
            label: '合同编号',
            value: res?.contractNo || r.contractId || '-',
            link: r.contractId ? AdvertisingRouteEnum.ADVERTISING_CONTRACT_DETAIL : undefined,
            linkId: r.contractId,
          },
          { label: '业务主体', value: res?.businessEntityName || '-' },
          { label: '关联订单', value: res?.orderNo || '-' },
          { label: '订单名称', value: res?.orderName || '-' },
          { label: '申请份数', value: r.appliedCopies ?? '-' },
          { label: '申请备注', value: r.applyRemark || '-' },
        ],
        compare: [],
      };
    }
    if (type === 'archive') {
      const res: any = await getAdContractDetail(id);
      const c = res?.contract || {};
      return {
        base: [
          { label: '合同编号', value: c.contractNo || '-' },
          { label: '合同名称', value: c.contractName || '-' },
          { label: t('advertising.approval.column.amount'), value: c.amount != null ? fmtAmount(c.amount) : '-' },
          {
            label: '关联订单',
            value: res?.orderNo || c.orderId || '-',
            link: c.orderId ? AdvertisingRouteEnum.ADVERTISING_ORDER_DETAIL : undefined,
            linkId: c.orderId,
          },
          { label: '订单名称', value: res?.orderName || '-' },
          { label: '用印状态', value: res?.sealStatusLabel || '-' },
          { label: '业务主体', value: res?.businessEntityName || '-' },
        ],
        compare: [],
      };
    }
    if (type === 'receipt') {
      const res: any = await getAdReceiptDetail(id);
      return {
        base: [
          { label: '收款单号', value: res?.receiptNo || '-' },
          {
            label: '关联订单',
            value: res?.orderNo || res?.orderId || '-',
            link: res?.orderId ? AdvertisingRouteEnum.ADVERTISING_ORDER_DETAIL : undefined,
            linkId: res?.orderId,
          },
          { label: '订单名称', value: res?.orderName || '-' },
          { label: t('advertising.approval.column.amount'), value: res?.amount != null ? fmtAmount(res.amount) : '-' },
          { label: '收款时间', value: fmtDateTime(res?.receiptTime) },
          { label: '备注', value: res?.remark || '-' },
        ],
        compare: [],
      };
    }
    if (type === 'payout') {
      const res: any = await getAdPayoutDetail(id);
      return {
        base: [
          { label: '付款单号', value: res?.paymentNo || '-' },
          {
            label: '关联订单',
            value: res?.orderNo || res?.orderId || '-',
            link: res?.orderId ? AdvertisingRouteEnum.ADVERTISING_ORDER_DETAIL : undefined,
            linkId: res?.orderId,
          },
          { label: '订单名称', value: res?.orderName || '-' },
          { label: t('advertising.approval.column.amount'), value: res?.amount != null ? fmtAmount(res.amount) : '-' },
          { label: '付款时间', value: fmtDateTime(res?.paymentTime) },
          { label: '备注', value: res?.remark || '-' },
        ],
        compare: [],
        mediaDetails: res?.mediaDetails || [],
      };
    }
    return { base: [], compare: [] };
  }

  // 详情抽屉：按类型加载详情
  async function openDetail(row: AdApprovalTodoItem) {
    const id = row.businessId || row.id;
    const { type } = row;
    if (!id || !type) return;
    detailVisible.value = true;
    detailLoading.value = true;
    detailBaseFields.value = [];
    detailCompareRows.value = [];
    detailMediaList.value = [];
    detailType.value = type;
    try {
      const { base, compare, mediaDetails } = await loadDetail(type, id);
      detailBaseFields.value = base;
      detailCompareRows.value = compare;
      if (type === 'payout' && mediaDetails) {
        detailMediaList.value = mediaDetails;
      }
    } catch (e) {
      message.error((e as Error).message || '加载详情失败');
    } finally {
      detailLoading.value = false;
    }
  }

  const columns: DataTableColumn<AdApprovalTodoItem>[] = [
    { key: 'typeLabel', title: '类型', width: 100, render: (row) => h('span', row.typeLabel || '-') },
    { key: 'applicantName', title: t('advertising.approval.column.applicant'), width: 110 },
    { key: 'refNo', title: t('advertising.approval.column.refNo'), width: 180, ellipsis: { tooltip: true } },
    {
      key: 'amount',
      title: t('advertising.approval.column.amount'),
      width: 140,
      align: 'right',
      render: (row) => h('span', row.amount != null ? fmtAmount(row.amount) : '-'),
    },
    {
      key: 'createTime',
      title: t('advertising.approval.column.submitTime'),
      width: 170,
      render: (row) => h('span', fmtDateTime(row.createTime)),
    },
    {
      key: 'summary',
      title: t('advertising.approval.column.summary'),
      minWidth: 180,
      ellipsis: { tooltip: true },
      render: (row) => h('span', row.summary || '-'),
    },
    {
      key: 'action',
      title: '操作',
      width: 200,
      fixed: 'right' as const,
      render: (row) =>
        h(
          NSpace,
          { size: 4 },
          {
            default: () => [
              h(
                NButton,
                { size: 'small', onClick: () => openDetail(row) },
                { default: () => t('advertising.approval.detail') }
              ),
              hasPermission(approvePermissionOf(row.type))
                ? h(
                    NButton,
                    { size: 'small', type: 'primary', onClick: () => handleApprove(row) },
                    { default: () => t('advertising.approval.approve') }
                  )
                : null,
              hasPermission(rejectPermissionOf(row.type))
                ? h(
                    NButton,
                    { size: 'small', type: 'error', onClick: () => openReject(row) },
                    { default: () => t('advertising.approval.reject') }
                  )
                : null,
            ],
          }
        ),
    },
  ];

  onMounted(fetchData);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .before-value {
    color: #999;
    word-break: break-all;
  }
  .after-value {
    color: #18a058;
    word-break: break-all;
  }
</style>
