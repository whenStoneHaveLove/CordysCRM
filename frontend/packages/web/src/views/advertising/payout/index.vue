<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          placeholder="付款单号/订单号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.status"
          :options="statusOptions"
          placeholder="状态"
          clearable
          style="width: 140px"
        />
        <n-select
          v-model:value="searchForm.type"
          :options="typeOptions"
          placeholder="类型"
          clearable
          style="width: 140px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button v-permission="['AD_PAYOUT:CREATE']" type="primary" @click="openCreate">新建付款</n-button>
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

    <!-- 新建/编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="modalTitle" preset="card" style="width: 560px">
      <n-form ref="formRef" :model="form" label-placement="left" :label-width="100">
        <n-form-item label="关联订单" path="orderId">
          <n-select
            v-model:value="form.orderId"
            filterable
            remote
            clearable
            :loading="orderLoading"
            :options="orderOptions"
            placeholder="选择订单"
            @search="searchOrders"
            @focus="() => searchOrders('')"
            @update:value="onOrderChange"
          />
        </n-form-item>
        <n-form-item label="付款金额" path="amount">
          <n-input-number
            v-model:value="form.amount"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="剩余应付自动带出"
          />
        </n-form-item>
        <n-form-item label="付款时间">
          <n-date-picker v-model:value="form.paymentTime" type="date" style="width: 100%" />
        </n-form-item>
        <n-form-item label="类型">
          <n-select v-model:value="form.type" :options="typeOptions" />
        </n-form-item>
        <n-form-item v-if="mediaOptions.length > 0" label="付款媒体">
          <n-checkbox-group v-model:value="form.mediaIds">
            <n-space vertical>
              <n-checkbox v-for="m in mediaOptions" :key="m.value" :value="m.value" :label="m.label" />
            </n-space>
          </n-checkbox-group>
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="form.remark" type="textarea" :rows="2" placeholder="备注" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 审核弹窗 -->
    <n-modal v-model:show="showApprove" preset="card" title="审批" style="width: 480px">
      <n-space vertical>
        <div>
          <div class="action-modal-label">审批备注</div>
          <n-input v-model:value="approveRemark" type="textarea" :rows="3" placeholder="请输入审批备注" />
        </div>
      </n-space>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showApprove = false">取消</n-button>
          <n-button type="success" :loading="saving" @click="doApprove('APPROVE')">通过</n-button>
          <n-button type="error" :loading="saving" @click="doApprove('REJECT')">驳回</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 详情弹窗 -->
    <n-modal v-model:show="showDetail" preset="card" title="付款单详情" style="width: 640px">
      <n-spin :show="detailLoading">
        <n-descriptions label-placement="left" :column="2" bordered size="small">
          <n-descriptions-item label="付款单号">{{ detail.paymentNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="付款金额">¥{{ detail.amount ?? 0 }}</n-descriptions-item>
          <n-descriptions-item label="付款时间">{{ fmtDate(detail.paymentTime) }}</n-descriptions-item>
          <n-descriptions-item label="类型">{{ detail.typeLabel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="状态">{{ detail.statusLabel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="备注">{{ detail.remark || '-' }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">审计信息</n-divider>
        <n-descriptions label-placement="left" :column="2" bordered size="small">
          <n-descriptions-item label="创建人">{{ getUserName(detail.createUser) }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ fmtDateTime(detail.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="修改人">{{ getUserName(detail.updateUser) }}</n-descriptions-item>
          <n-descriptions-item label="修改时间">{{ fmtDateTime(detail.updateTime) }}</n-descriptions-item>
          <n-descriptions-item label="审批人">{{ getUserName(detail.approveUser) }}</n-descriptions-item>
          <n-descriptions-item label="审批时间">{{ fmtDateTime(detail.approveTime) }}</n-descriptions-item>
          <n-descriptions-item label="审批备注" :span="2">{{ detail.approveRemark || '-' }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">关联订单</n-divider>
        <n-descriptions label-placement="left" :column="2" bordered size="small">
          <n-descriptions-item label="订单编号">
            <n-button text type="primary" @click="goOrderDetail(detail.orderId)">{{ detail.orderNo || '-' }}</n-button>
          </n-descriptions-item>
          <n-descriptions-item label="订单名称">{{ detail.orderName || '-' }}</n-descriptions-item>
        </n-descriptions>

        <template v-if="detail.contracts && detail.contracts.length">
          <n-divider title-placement="left">关联合同</n-divider>
          <n-descriptions label-placement="left" :column="2" bordered size="small">
            <n-descriptions-item v-for="c in detail.contracts" :key="`no-${c.id}`" label="合同编号">
              <n-button text type="primary" @click="goContractDetail(c.id)">{{ c.contractNo || '-' }}</n-button>
            </n-descriptions-item>
            <n-descriptions-item v-for="c in detail.contracts" :key="`name-${c.id}`" label="合同名称">
              {{ c.contractName || '-' }}
            </n-descriptions-item>
          </n-descriptions>
        </template>
      </n-spin>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDetail = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NCheckbox,
    NCheckboxGroup,
    NDataTable,
    NDatePicker,
    NDescriptions,
    NDescriptionsItem,
    NDivider,
    NForm,
    NFormItem,
    NInput,
    NInputNumber,
    NModal,
    NSelect,
    NSpace,
    NSpin,
    NTag,
    useMessage,
  } from 'naive-ui';

  import {
    AdPayoutStatusOptions,
    AdPayoutTypeOptions,
    getAdPayoutStatusLabel,
    getAdPayoutTypeLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdPayoutDetail, AdPayoutInfo, AdPayoutPageParams } from '@lib/shared/models/advertising';

  import {
    approveAdPayout,
    createAdPayout,
    getAdOrderPage,
    getAdPayoutDetail,
    getAdPayoutMedia,
    getAdPayoutPage,
    getAdPayoutRemaining,
    submitAdPayout,
    updateAdPayout,
  } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtDate, fmtDateTime, toTimeStamp } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const message = useMessage();
  const router = useRouter();
  const { loadUserMap, getUserName } = useUserMap();

  const statusOptions = AdPayoutStatusOptions;
  const typeOptions = AdPayoutTypeOptions;

  const loading = ref(false);
  const saving = ref(false);
  const list = ref<AdPayoutInfo[]>([]);
  const searchForm = reactive({
    keyword: '',
    status: null as number | null,
    type: null as number | null,
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
      const params: AdPayoutPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        status: searchForm.status,
        type: searchForm.type,
      };
      const res = await getAdPayoutPage(params);
      list.value = res.list || [];
      pagination.itemCount = res.total || 0;
    } catch (e) {
      message.error((e as Error).message || '查询失败');
    } finally {
      loading.value = false;
    }
  }

  const mediaOptions = ref<Array<{ label: string; value: string }>>([]);

  interface PayoutForm {
    orderId?: string;
    amount?: number;
    paymentTime?: number | null;
    type?: number;
    mediaIds?: string[];
    remark?: string;
  }
  const form = reactive<PayoutForm>({
    orderId: undefined,
    amount: undefined,
    paymentTime: null,
    type: 10,
    mediaIds: [],
    remark: undefined,
  });

  // ---- 订单选择 ----
  const orderLoading = ref(false);
  const orderOptions = ref<Array<{ label: string; value: string }>>([]);
  async function searchOrders(keyword: string) {
    orderLoading.value = true;
    try {
      const res = await getAdOrderPage({ current: 1, pageSize: 20, keyword: keyword || undefined });
      orderOptions.value = (res.list || []).map((it: any) => ({
        label: `${it.orderNo || ''} ${it.orderName || ''}`,
        value: it.id,
      }));
    } catch (e) {
      // ignore
    } finally {
      orderLoading.value = false;
    }
  }
  async function onOrderChange(orderId: string) {
    if (!orderId) {
      form.amount = undefined;
      form.mediaIds = [];
      mediaOptions.value = [];
      return;
    }
    try {
      const remaining = await getAdPayoutRemaining(orderId);
      form.amount = Number(remaining ?? 0);
      const media = await getAdPayoutMedia(orderId);
      mediaOptions.value = (media || []).map((m: any) => ({
        label: m.mediaName || m.mediaId || m.id,
        value: m.id,
      }));
      // 默认全选
      form.mediaIds = mediaOptions.value.map((m) => m.value);
    } catch (e) {
      // ignore
    }
  }

  // ---- 新建/编辑 ----
  const showModal = ref(false);
  const editId = ref('');
  const modalTitle = computed(() => (editId.value ? '编辑付款单' : '新建付款单'));

  function resetForm() {
    form.orderId = undefined;
    form.amount = undefined;
    form.paymentTime = null;
    form.type = 10;
    form.mediaIds = [];
    form.remark = undefined;
    mediaOptions.value = [];
    editId.value = '';
  }

  function openCreate() {
    resetForm();
    orderOptions.value = [];
    showModal.value = true;
  }

  async function openEdit(row: AdPayoutInfo) {
    editId.value = row.id!;
    showModal.value = true;
    try {
      const res = await getAdPayoutDetail(row.id!);
      form.orderId = res.orderId;
      form.amount = res.amount;
      form.paymentTime = toTimeStamp(res.paymentTime);
      form.type = res.type ?? 10;
      form.remark = res.remark;
      if (res.orderId) {
        orderOptions.value = [{ label: res.orderName || res.orderId, value: res.orderId }];
        const media = await getAdPayoutMedia(res.orderId);
        mediaOptions.value = (media || []).map((m: any) => ({
          label: m.mediaName || m.mediaId || m.id,
          value: m.id,
        }));
      }
      if (res.mediaIds) {
        try {
          form.mediaIds = JSON.parse(res.mediaIds);
        } catch (e) {
          form.mediaIds = [];
        }
      }
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    }
  }

  async function handleSave() {
    if (!form.orderId) {
      message.warning('请选择关联订单');
      return;
    }
    if (!form.amount || form.amount <= 0) {
      message.warning('付款金额必须大于0');
      return;
    }
    saving.value = true;
    try {
      const payload: any = {
        orderId: form.orderId,
        amount: form.amount,
        paymentTime: form.paymentTime,
        type: form.type,
        mediaIds: form.mediaIds,
        remark: form.remark,
      };
      if (editId.value) {
        payload.id = editId.value;
        await updateAdPayout(payload);
      } else {
        await createAdPayout(payload);
      }
      message.success('保存成功');
      showModal.value = false;
      fetchData();
    } catch (e) {
      message.error((e as Error).message || '保存失败');
    } finally {
      saving.value = false;
    }
  }

  // ---- 提交 / 审核 ----
  async function handleSubmit(row: AdPayoutInfo) {
    try {
      await submitAdPayout(row.id!);
      message.success('已提交');
      fetchData();
    } catch (e) {
      message.error((e as Error).message || '提交失败');
    }
  }

  const showApprove = ref(false);
  const approveId = ref('');
  const approveRemark = ref('');
  function openApprove(row: AdPayoutInfo) {
    approveId.value = row.id!;
    approveRemark.value = '';
    showApprove.value = true;
  }
  async function doApprove(action: 'APPROVE' | 'REJECT') {
    saving.value = true;
    try {
      await approveAdPayout(approveId.value, { action, remark: approveRemark.value });
      message.success(action === 'APPROVE' ? '已通过' : '已驳回');
      showApprove.value = false;
      fetchData();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    } finally {
      saving.value = false;
    }
  }

  function statusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    switch (status) {
      case 0:
        return 'default';
      case 10:
        return 'warning';
      case 20:
        return 'success';
      case 30:
        return 'error';
      default:
        return 'default';
    }
  }

  // ---- 详情 ----
  const showDetail = ref(false);
  const detailLoading = ref(false);
  const detail = reactive<AdPayoutDetail>({});
  async function openDetail(row: AdPayoutInfo) {
    showDetail.value = true;
    detailLoading.value = true;
    try {
      const res = await getAdPayoutDetail(row.id!);
      Object.assign(detail, res);
    } catch (e) {
      message.error((e as Error).message || '加载详情失败');
    } finally {
      detailLoading.value = false;
    }
  }
  function goOrderDetail(orderId?: string) {
    if (!orderId) return;
    const { href } = router.resolve({ name: AdvertisingRouteEnum.ADVERTISING_ORDER_DETAIL, params: { id: orderId } });
    window.open(href, '_blank');
  }
  function goContractDetail(contractId?: string) {
    if (!contractId) return;
    const { href } = router.resolve({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_DETAIL, params: { id: contractId } });
    window.open(href, '_blank');
  }
  const columns: DataTableColumn<AdPayoutInfo>[] = [
    { key: 'paymentNo', title: '付款单号', minWidth: 140, ellipsis: { tooltip: true } },
    { key: 'orderName', title: '关联订单', minWidth: 160, ellipsis: { tooltip: true } },
    { key: 'amount', title: '付款金额', width: 120, render: (row) => h('span', `¥${row.amount ?? 0}`) },
    { key: 'paymentTime', title: '付款时间', width: 110, render: (row) => h('span', fmtDate(row.paymentTime)) },
    { key: 'type', title: '类型', width: 100, render: (row) => h('span', getAdPayoutTypeLabel(row.type)) },
    {
      key: 'status',
      title: '状态',
      width: 100,
      render: (row) =>
        h(NTag, { type: statusTagType(row.status) }, { default: () => getAdPayoutStatusLabel(row.status) }),
    },
    {
      key: 'action',
      title: '操作',
      width: 200,
      fixed: 'right' as const,
      render: (row) =>
        h(
          NSpace,
          { wrap: false },
          {
            default: () => {
              const actions: any[] = [];
              actions.push(h(NButton, { size: 'small', onClick: () => openDetail(row) }, { default: () => '详情' }));
              if (row.status === 0 || row.status === 30) {
                actions.push(
                  h(
                    NButton,
                    { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                    { default: () => '编辑' }
                  ),
                  h(
                    NButton,
                    { size: 'small', type: 'primary', onClick: () => handleSubmit(row) },
                    { default: () => '提交' }
                  )
                );
              }
              if (row.status === 10) {
                actions.push(
                  h(
                    NButton,
                    { size: 'small', type: 'success', onClick: () => openApprove(row) },
                    { default: () => '审批' }
                  )
                );
              }
              return actions;
            },
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
    searchForm.type = null;
    handleSearch();
  }

  onMounted(() => {
    loadUserMap();
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
  .action-modal-label {
    margin-bottom: 4px;
    font-size: 12px;
    color: var(--text-n2);
  }
</style>
