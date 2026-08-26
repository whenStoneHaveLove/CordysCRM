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
    <n-modal v-model:show="showModal" :title="modalTitle" preset="card" style="width: 880px">
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

        <!-- 各下游客户返点与本次付款明细 -->
        <template v-if="mediaOptions.length > 0">
          <n-divider title-placement="center" class="media-divider"> 各下游客户返点信息与本次付款 </n-divider>
          <div class="media-cards">
            <div v-for="(m, idx) in mediaOptions" :key="m.mediaId || m.id" class="media-card">
              <div class="media-card-header">
                <span class="media-card-index">{{ idx + 1 }}</span>
                <span class="media-card-name">{{ m.mediaName || m.mediaId }}</span>
              </div>
              <n-descriptions :column="3" size="small" bordered label-placement="left" :label-width="92">
                <n-descriptions-item label="应付">¥{{ fmtMoney(m.payableAmount) }}</n-descriptions-item>
                <n-descriptions-item label="不记返">¥{{ fmtMoney(m.noRebateAmount) }}</n-descriptions-item>
                <n-descriptions-item label="实际应付">¥{{ fmtMoney(m.actualPayable) }}</n-descriptions-item>
                <n-descriptions-item label="返点方式">{{ rebateModeLabel(m.rebateMode) }}</n-descriptions-item>
                <n-descriptions-item label="返点值">{{ fmtRebateValue(m) }}</n-descriptions-item>
                <n-descriptions-item label="返点金额">¥{{ fmtMoney(m.rebateAmount) }}</n-descriptions-item>
                <n-descriptions-item label="累计已付">¥{{ fmtMoney(m.paidAmount) }}</n-descriptions-item>
                <n-descriptions-item label="剩余应付">¥{{ fmtMoney(remainingOf(m)) }}</n-descriptions-item>
                <n-descriptions-item label="本次付款">
                  <n-input-number
                    v-model:value="mediaPaidDraft[m.mediaId || m.id || '']"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    placeholder="0.00"
                    @update:value="recalcAmount"
                  />
                </n-descriptions-item>
                <n-descriptions-item label="账户信息" :span="3">
                  <n-select
                    v-model:value="mediaAccountDraft[m.mediaId || m.id || '']"
                    filterable
                    clearable
                    :options="accountOptionsOf(m)"
                    placeholder="选择收款账户"
                  />
                </n-descriptions-item>
              </n-descriptions>
            </div>
            <div class="media-summary">
              已填本次付款合计：<span class="summary-amount">¥{{ fmtMoney(currentPaidTotal) }}</span>
            </div>
          </div>
        </template>

        <n-form-item label="备注" class="mt-12">
          <n-input v-model:value="form.remark" type="textarea" :rows="2" placeholder="备注" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button
            v-permission="[editId ? 'AD_PAYOUT:UPDATE' : 'AD_PAYOUT:CREATE']"
            type="primary"
            :loading="saving"
            @click="handleSave"
            >保存</n-button
          >
          <n-button
            v-permission="[editId ? 'AD_PAYOUT:UPDATE' : 'AD_PAYOUT:CREATE']"
            type="info"
            :loading="submitting"
            @click="handleSubmitModal"
            >提交</n-button
          >
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
          <n-button v-permission="['AD_PAYOUT:APPROVE']" type="success" :loading="saving" @click="doApprove('APPROVE')"
            >通过</n-button
          >
          <n-button v-permission="['AD_PAYOUT:APPROVE']" type="error" :loading="saving" @click="doApprove('REJECT')"
            >驳回</n-button
          >
        </n-space>
      </template>
    </n-modal>

    <!-- 详情弹窗 -->
    <n-modal v-model:show="showDetail" preset="card" title="付款单详情" style="width: 760px">
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

        <!-- 各下游客户付款返点明细 -->
        <n-divider title-placement="left">各下游客户付款返点明细</n-divider>
        <n-table
          v-if="detail.mediaDetails && detail.mediaDetails.length"
          :bordered="true"
          size="small"
          :single-line="false"
        >
          <thead>
            <tr>
              <th style="width: 60px">序号</th>
              <th>下游客户</th>
              <th style="width: 100px">应付</th>
              <th style="width: 100px">不记返</th>
              <th style="width: 100px">返点方式</th>
              <th style="width: 90px">返点值</th>
              <th style="width: 100px">返点金额</th>
              <th style="width: 100px">实际应付</th>
              <th style="width: 110px">本次付款</th>
              <th style="width: 200px">收款账户</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(m, idx) in detail.mediaDetails" :key="m.mediaId || m.orderDownstreamMediaId || idx">
              <td>{{ idx + 1 }}</td>
              <td>{{ m.mediaName || m.mediaId || '-' }}</td>
              <td>¥{{ fmtMoney(m.payableAmount) }}</td>
              <td>¥{{ fmtMoney(m.noRebateAmount) }}</td>
              <td>{{ rebateModeLabel(m.rebateMode) }}</td>
              <td>{{ fmtDetailRebateValue(m) }}</td>
              <td>¥{{ fmtMoney(m.rebateAmount) }}</td>
              <td>¥{{ fmtMoney(m.actualPayable) }}</td>
              <td>¥{{ fmtMoney(m.paidAmount) }}</td>
              <td>{{ formatAccount(m) }}</td>
            </tr>
          </tbody>
        </n-table>
        <n-empty v-else description="暂无各下游客户付款明细" />
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
  import { computed, h, onMounted, reactive, ref, resolveDirective, withDirectives } from 'vue';
  import { useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDataTable,
    NDatePicker,
    NDescriptions,
    NDescriptionsItem,
    NDivider,
    NEmpty,
    NForm,
    NFormItem,
    NInput,
    NInputNumber,
    NModal,
    NSelect,
    NSpace,
    NSpin,
    NTable,
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
  import type {
    AdPayoutDetail,
    AdPayoutInfo,
    AdPayoutMediaDetailItem,
    AdPayoutMediaOption,
    AdPayoutPageParams,
  } from '@lib/shared/models/advertising';

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

  const permissionDirective = resolveDirective('permission');

  const statusOptions = AdPayoutStatusOptions;
  const typeOptions = AdPayoutTypeOptions;

  const loading = ref(false);
  const saving = ref(false);
  const submitting = ref(false);
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

  /* ========== 工具 ========== */
  function fmtMoney(v?: number | null): string {
    if (v === null || v === undefined) return '0.00';
    const n = Number(v);
    if (Number.isNaN(n)) return '0.00';
    return n.toFixed(2);
  }
  function rebateModeLabel(v?: number): string {
    if (v === 10) return '比例';
    if (v === 20) return '固定金额';
    return '-';
  }
  function fmtRebateValue(m: AdPayoutMediaOption): string {
    if (m.rebateMode === 10) return `${fmtMoney(m.rebateValue)}%`;
    if (m.rebateMode === 20) return `¥${fmtMoney(m.rebateValue)}`;
    return '-';
  }
  function fmtDetailRebateValue(m: AdPayoutMediaDetailItem): string {
    if (m.rebateMode === 10) return `${fmtMoney(m.rebateValue)}%`;
    if (m.rebateMode === 20) return `¥${fmtMoney(m.rebateValue)}`;
    return '-';
  }
  function remainingOf(m: AdPayoutMediaOption): number {
    const actual = Number(m.actualPayable ?? 0);
    const paid = Number(m.paidAmount ?? 0);
    const diff = actual - paid;
    return diff > 0 ? diff : 0;
  }
  function formatAccount(m: AdPayoutMediaDetailItem): string {
    if (!m.payeeName && !m.bankName && !m.bankAccount) return '-';
    const disabled = m.accountDisabled === 1 ? '（停用）' : '';
    return `${m.payeeName || ''} - ${m.bankName || ''} - ${m.bankAccount || ''}${disabled}`;
  }

  /* ========== 新建/编辑表单状态 ========== */
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

  /* ========== 订单选择 + 下游客户返点信息 ========== */
  const orderLoading = ref(false);
  const orderOptions = ref<Array<{ label: string; value: string }>>([]);
  const mediaOptions = ref<AdPayoutMediaOption[]>([]);
  // 每客户本次付款草稿（key = mediaId || id）
  const mediaPaidDraft = reactive<Record<string, number>>({});
  // 每客户所选账户草稿（key = mediaId || id）
  const mediaAccountDraft = reactive<Record<string, string>>({});

  function resetMediaDraft() {
    Object.keys(mediaPaidDraft).forEach((k) => delete mediaPaidDraft[k]);
    Object.keys(mediaAccountDraft).forEach((k) => delete mediaAccountDraft[k]);
  }

  function accountOptionsOf(m: AdPayoutMediaOption) {
    const opts = (m.accountList || []).map((a) => ({
      label: `${a.payeeName || ''} - ${a.bankName || ''} - ${a.bankAccount || ''}${a.disabled === 1 ? '（停用）' : ''}`,
      value: a.id || '',
    }));
    return opts;
  }

  function firstAvailableAccountId(m: AdPayoutMediaOption): string {
    const acc = (m.accountList || []).find((a) => a.disabled !== 1);
    return acc?.id || '';
  }

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

  function recalcAmount() {
    // 本次付款合计自动汇总到 form.amount
    const total = Object.values(mediaPaidDraft).reduce((sum, n) => sum + (Number(n) || 0), 0);
    if (total > 0) {
      form.amount = Number(total.toFixed(2));
    }
  }
  const currentPaidTotal = computed(() => Object.values(mediaPaidDraft).reduce((sum, n) => sum + (Number(n) || 0), 0));

  /* ========== 新建/编辑 ========== */
  const showModal = ref(false);
  const editId = ref('');
  const createdId = ref('');
  const modalTitle = computed(() => (editId.value ? '编辑付款单' : '新建付款单'));

  async function onOrderChange(orderId: string) {
    resetMediaDraft();
    mediaOptions.value = [];
    if (!orderId) {
      form.amount = undefined;
      return;
    }
    try {
      const remaining = await getAdPayoutRemaining(orderId);
      form.amount = Number(remaining ?? 0);
      const media = (await getAdPayoutMedia(orderId)) || [];
      mediaOptions.value = media as AdPayoutMediaOption[];
      // 新建模式：本次付款默认带出各客户的剩余应付 + 默认回带一条可用账户
      if (!editId.value) {
        mediaOptions.value.forEach((m) => {
          const key = m.mediaId || m.id || '';
          if (!key) return;
          mediaPaidDraft[key] = remainingOf(m);
          mediaAccountDraft[key] = firstAvailableAccountId(m);
        });
        recalcAmount();
      }
    } catch (e) {
      // ignore
    }
  }

  function resetForm() {
    form.orderId = undefined;
    form.amount = undefined;
    form.paymentTime = null;
    form.type = 10;
    form.mediaIds = [];
    form.remark = undefined;
    mediaOptions.value = [];
    resetMediaDraft();
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
    resetMediaDraft();
    try {
      const res = await getAdPayoutDetail(row.id!);
      form.orderId = res.orderId;
      form.amount = res.amount;
      form.paymentTime = toTimeStamp(res.paymentTime);
      form.type = res.type ?? 10;
      form.remark = res.remark;
      if (res.orderId) {
        orderOptions.value = [{ label: res.orderName || res.orderId, value: res.orderId }];
        const media = (await getAdPayoutMedia(res.orderId)) || [];
        mediaOptions.value = media as AdPayoutMediaOption[];
      }
      // 回填明细草稿
      const details = res.mediaDetails || [];
      details.forEach((d) => {
        const key = d.mediaId || '';
        if (!key) return;
        mediaPaidDraft[key] = Number(d.paidAmount ?? 0);
        mediaAccountDraft[key] =
          d.accountId || firstAvailableAccountId(mediaOptions.value.find((m) => (m.mediaId || m.id) === key) || {});
      });
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
      const mediaDetails: AdPayoutMediaDetailItem[] = mediaOptions.value
        .map((m) => {
          const key = m.mediaId || m.id || '';
          const paid = Number(mediaPaidDraft[key] || 0);
          return {
            orderDownstreamMediaId: m.id,
            mediaId: m.mediaId,
            mediaName: m.mediaName,
            payableAmount: Number(m.payableAmount ?? 0),
            noRebateAmount: Number(m.noRebateAmount ?? 0),
            rebateMode: m.rebateMode,
            rebateValue: Number(m.rebateValue ?? 0),
            rebateAmount: Number(m.rebateAmount ?? 0),
            actualPayable: Number(m.actualPayable ?? 0),
            paidAmount: paid,
            accountId: mediaAccountDraft[key] || undefined,
          };
        })
        .filter((d) => d.mediaId);
      const mediaIds = mediaOptions.value.map((m) => m.mediaId || m.id).filter(Boolean) as string[];

      const payload: any = {
        orderId: form.orderId,
        amount: form.amount,
        paymentTime: form.paymentTime,
        type: form.type,
        mediaIds,
        remark: form.remark,
        mediaDetails,
      };
      if (editId.value) {
        payload.id = editId.value;
        await updateAdPayout(payload);
      } else {
        const res = await createAdPayout(payload);
        createdId.value = (res as any)?.id || '';
      }
      message.success('保存成功');
      showModal.value = false;
      fetchData();
      return editId.value ? editId.value : createdId.value;
    } catch (e) {
      message.error((e as Error).message || '保存失败');
      return '';
    } finally {
      saving.value = false;
    }
  }

  /** 模态提交：先保存草稿，校验账户后提交（草稿→待审核）。 */
  async function handleSubmitModal() {
    if (mediaOptions.value.length === 0) {
      message.warning('请先选择关联订单');
      return;
    }
    const unselected = mediaOptions.value.filter((m) => {
      const key = m.mediaId || m.id || '';
      return !mediaAccountDraft[key];
    });
    if (unselected.length > 0) {
      message.warning('每个下游客户都必须选择收款账户后才能提交');
      return;
    }
    submitting.value = true;
    try {
      const savedId = await handleSave();
      if (!savedId) return;
      await submitAdPayout(savedId);
      message.success('已提交');
      showModal.value = false;
      fetchData();
    } catch (e) {
      message.error((e as Error).message || '提交失败');
    } finally {
      submitting.value = false;
    }
  }

  /* ========== 提交 / 审核 ========== */
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

  /* ========== 详情 ========== */
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
    const { href } = router.resolve({
      name: AdvertisingRouteEnum.ADVERTISING_CONTRACT_DETAIL,
      params: { id: contractId },
    });
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
                  withDirectives(
                    h(
                      NButton,
                      { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                      { default: () => '编辑' }
                    ),
                    [[permissionDirective, ['AD_PAYOUT:UPDATE']]]
                  ),
                  withDirectives(
                    h(
                      NButton,
                      { size: 'small', type: 'primary', onClick: () => handleSubmit(row) },
                      { default: () => '提交' }
                    ),
                    [[permissionDirective, ['AD_PAYOUT:UPDATE']]]
                  )
                );
              }
              if (row.status === 10) {
                actions.push(
                  withDirectives(
                    h(
                      NButton,
                      { size: 'small', type: 'success', onClick: () => openApprove(row) },
                      { default: () => '审批' }
                    ),
                    [[permissionDirective, ['AD_PAYOUT:APPROVE']]]
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

  function applyQuery() {
    const q = router.currentRoute.value.query;
    if (q.status != null && q.status !== '') searchForm.status = Number(q.status);
    if (q.type != null && q.type !== '') searchForm.type = Number(q.type);
  }

  onMounted(() => {
    loadUserMap();
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
  .mt-12 {
    margin-top: 12px;
  }
  .action-modal-label {
    margin-bottom: 4px;
    font-size: 12px;
    color: var(--text-n2);
  }
  .media-divider {
    color: var(--text-n2);
    font-weight: 600;
  }
  .media-divider :deep(.n-divider__title) {
    font-weight: 600;
    color: var(--text-n2);
  }
  .media-cards {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .media-card {
    border: 1px solid var(--border-color, #e5e7eb);
    border-left: 3px solid var(--primary-color);
    border-radius: 4px;
    padding: 10px 12px 6px;
    background: var(--card-color, #fafbfc);
  }
  .media-card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }
  .media-card-index {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: var(--primary-color);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    line-height: 1;
  }
  .media-card-name {
    font-weight: 600;
    color: var(--text-color);
    font-size: 13px;
  }
  .media-summary {
    text-align: right;
    font-size: 13px;
    color: var(--text-n2);
  }
  .summary-amount {
    color: var(--primary-color);
    font-weight: 600;
    margin-left: 4px;
  }
</style>
