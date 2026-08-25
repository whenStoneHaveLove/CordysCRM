<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>改单详情</span>
            <n-tag>{{ detail.statusLabel || getAdOrderChangeStatusLabel(detail.change.status) }}</n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button
              v-if="detail.change.status === 0"
              v-permission="['AD_ORDER_CHANGE:SUBMIT']"
              type="primary"
              @click="handleSubmit"
              >提交</n-button
            >
            <n-button
              v-if="detail.change.status === 10"
              v-permission="['AD_ORDER_CHANGE:APPROVE']"
              type="primary"
              @click="handleApprove"
              >审批通过</n-button
            >
            <n-button v-if="detail.change.status === 10" v-permission="['AD_ORDER_CHANGE:REJECT']" @click="handleReject"
              >驳回</n-button
            >
            <n-button
              v-if="detail.change.status === 20"
              v-permission="['AD_ORDER_CHANGE:SUBMIT']"
              type="warning"
              @click="handleExecute"
              >执行</n-button
            >
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-descriptions label-placement="left" :column="2" bordered size="small">
          <n-descriptions-item label="关联订单">{{
            detail.orderNo || detail.change.orderId || '-'
          }}</n-descriptions-item>
          <n-descriptions-item label="改单ID">{{ detail.change.id }}</n-descriptions-item>
          <n-descriptions-item label="创建人">{{ getUserName(detail.change.createUser) }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ fmtDateTime(detail.change.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="修改人">{{ getUserName(detail.change.updateUser) }}</n-descriptions-item>
          <n-descriptions-item label="修改时间">{{ fmtDateTime(detail.change.updateTime) }}</n-descriptions-item>
          <n-descriptions-item label="审批人">{{ getUserName(detail.change.approverId) }}</n-descriptions-item>
          <n-descriptions-item label="审批时间">{{ fmtDateTime(detail.change.approvedAt) }}</n-descriptions-item>
          <n-descriptions-item label="变更原因" :span="2">{{ detail.change.reason || '-' }}</n-descriptions-item>
          <n-descriptions-item label="审批备注" :span="2">{{ detail.change.approveRemark || '-' }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">字段变更对比</n-divider>
        <n-table :bordered="true" size="small" :single-line="false">
          <thead>
            <tr>
              <th style="width: 160px">变更字段</th>
              <th>原值（改前）</th>
              <th>新值（改后）</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in compareRows" :key="idx">
              <td>{{ row.label }}</td>
              <td class="before-value">{{ row.before }}</td>
              <td class="after-value">{{ row.after }}</td>
            </tr>
          </tbody>
        </n-table>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDescriptions,
    NDescriptionsItem,
    NDivider,
    NSpace,
    NSpin,
    NTable,
    NTag,
    useMessage,
  } from 'naive-ui';

  import {
    AD_ORDER_CHANGE_FIELD_META,
    AdModeOptions,
    AdOrderTypeOptions,
    AdPaymentMethodOptions,
    AdPostpayTriggerOptions,
    AdReceiptMethodOptions,
    getAdOrderChangeStatusLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderChangeDetail } from '@lib/shared/models/advertising';

  import {
    approveAdOrderChange,
    executeAdOrderChange,
    getAdBusinessEntityPage,
    getAdCustomerPage,
    getAdDictPage,
    getAdOrderChangeDetail,
    getAdUpstreamAgentPage,
    rejectAdOrderChange,
    submitAdOrderChange,
  } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();
  const { loadUserMap, getUserName } = useUserMap();

  const changeId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<AdOrderChangeDetail | null>(null);

  interface CompareRow {
    field: string;
    label: string;
    before: string;
    after: string;
  }

  function parseSnapshot(value?: string | null): Record<string, any> {
    if (!value) return {};
    try {
      return JSON.parse(value);
    } catch {
      return {};
    }
  }

  function parseFields(value?: string | null): string[] {
    if (!value) return [];
    // changeFields 可能是逗号分隔字符串，也可能是 JSON 数组
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

  type SelectItem = { label: string; value: string | number };
  const customerOptions = ref<SelectItem[]>([]);
  const upstreamAgentOptions = ref<SelectItem[]>([]);
  const industryOptions = ref<SelectItem[]>([]);
  const businessEntityOptions = ref<SelectItem[]>([]);

  const enumOptionsMap: Record<string, SelectItem[]> = {
    'enum-orderType': AdOrderTypeOptions as SelectItem[],
    'enum-rebateMode': AdModeOptions as SelectItem[],
    'enum-receiptMethod': AdReceiptMethodOptions as SelectItem[],
    'enum-paymentMethod': AdPaymentMethodOptions as SelectItem[],
    'enum-prepayMode': AdModeOptions as SelectItem[],
    'enum-postpayTrigger': AdPostpayTriggerOptions as SelectItem[],
  };

  function getOptions(control?: string): SelectItem[] {
    if (!control) return [];
    if (control === 'select-customer') return customerOptions.value;
    if (control === 'select-upstream') return upstreamAgentOptions.value;
    if (control === 'select-industry') return industryOptions.value;
    if (control === 'select-businessEntity') return businessEntityOptions.value;
    return enumOptionsMap[control] || [];
  }

  function labelOf(control: string | undefined, value: any): string {
    if (value === null || value === undefined || value === '') return '-';
    const opts = getOptions(control);
    const found = opts.find((o) => String(o.value) === String(value));
    return found ? String(found.label) : String(value);
  }

  function fmtVal(v: any, type?: string, control?: string): string {
    if (v === null || v === undefined || v === '') return '-';
    if (type === 'date') {
      const ts = Number(v);
      // 值为时间戳（秒级或毫秒级）时格式化为日期，避免直接展示数字
      if (!Number.isNaN(ts) && String(v).length >= 10) {
        const d = new Date(ts < 1e12 ? ts * 1000 : ts);
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
      }
      return String(v).slice(0, 10);
    }
    if (control) return labelOf(control, v);
    return String(v);
  }

  const compareRows = computed<CompareRow[]>(() => {
    const change = detail.value?.change;
    if (!change) return [];
    const before = parseSnapshot(change.snapshotBefore);
    const after = parseSnapshot(change.snapshotAfter);
    const fields = parseFields(change.changeFields);

    return fields.map((field) => {
      const meta = AD_ORDER_CHANGE_FIELD_META.find((m) => m.field === field);
      return {
        field,
        label: meta?.label || field,
        before: fmtVal(before[field], meta?.type, meta?.control),
        after: fmtVal(after[field], meta?.type, meta?.control),
      };
    });
  });

  async function loadSelectOptions() {
    try {
      const [cuRes, uaRes, dictRes, beRes] = await Promise.all([
        getAdCustomerPage({ current: 1, pageSize: 200 }),
        getAdUpstreamAgentPage({ current: 1, pageSize: 200, status: 10 }),
        getAdDictPage({ current: 1, pageSize: 200, dictCode: 'industry' }),
        getAdBusinessEntityPage({ current: 1, pageSize: 200 }),
      ]);
      customerOptions.value = (cuRes.list || []).map((it: any) => ({
        label: it.customerName || it.name || it.id,
        value: it.id,
      }));
      upstreamAgentOptions.value = (uaRes.list || []).map((it: any) => ({
        label: it.resourceName || it.name || it.id,
        value: it.id,
      }));
      industryOptions.value = (dictRes.list || []).map((it: any) => ({
        label: it.dictLabel || it.dictValue || it.id,
        value: it.dictValue || it.id,
      }));
      businessEntityOptions.value = (beRes.list || []).map((it: any) => ({
        label: it.name || it.id,
        value: it.id,
      }));
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdOrderChangeDetail(changeId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CHANGE });
  }

  async function handleSubmit() {
    try {
      await submitAdOrderChange(changeId);
      message.success(t('advertising.common.submitSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '提交失败');
    }
  }
  async function handleApprove() {
    try {
      await approveAdOrderChange(changeId);
      message.success(t('advertising.common.operateSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '审批失败');
    }
  }
  async function handleReject() {
    try {
      await rejectAdOrderChange(changeId);
      message.success(t('advertising.common.operateSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '驳回失败');
    }
  }
  async function handleExecute() {
    try {
      await executeAdOrderChange(changeId);
      message.success(t('advertising.common.operateSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '执行失败');
    }
  }

  onMounted(() => {
    loadUserMap();
    loadSelectOptions();
    fetchDetail();
  });
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
