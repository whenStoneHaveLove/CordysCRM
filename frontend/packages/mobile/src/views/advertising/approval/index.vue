<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar :title="t('advertising.approval')" left-arrow @click-left="$router.back()" />

    <!-- 搜索 + 类型筛选（口径对齐 web：仅「待我审批」+ 类型筛选） -->
    <div class="px-[12px] pt-[12px]">
      <van-search
        v-model="keyword"
        :placeholder="t('advertising.approval.searchPlaceholder')"
        shape="round"
        clearable
        @search="reset"
        @clear="reset"
      />
      <div class="mt-[8px] flex gap-2 overflow-x-auto pb-[4px]">
        <span
          v-for="opt of typeOptions"
          :key="opt.value"
          class="flex-shrink-0 rounded-full px-[12px] py-[4px] text-[12px]"
          :class="
            filterType === opt.value ? 'bg-[var(--primary-6)] text-white' : 'bg-white text-[var(--text-n3)]'
          "
          @click="onTypeChange(opt.value)"
        >
          {{ opt.label }}
        </span>
      </div>
    </div>

    <!-- 列表（按 key 重挂载，确保切筛选重新加载首屏） -->
    <div :key="listKey" class="flex-1 overflow-auto px-[12px]">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        :finished-text="t('advertising.approval.noMore')"
        @load="onLoad"
      >
        <div v-for="item of list" :key="item.id" class="mb-[12px] rounded-lg bg-white px-[12px] py-[10px]">
          <div @click="goDetail(item)">
            <div class="flex items-center justify-between gap-[8px]">
              <span class="one-line-text text-[14px] font-medium text-[var(--text-n1)]">
                {{ item.typeLabel || item.type || '-' }} · {{ item.refNo || '-' }}
              </span>
              <span v-if="item.amount != null" class="flex-shrink-0 text-[14px] font-semibold text-[var(--text-n1)]">
                {{ fmtAmount(item.amount) }}
              </span>
            </div>
            <div class="one-line-text mt-[4px] text-[12px] text-[var(--text-n3)]">{{ descOf(item) }}</div>
            <div class="mt-[2px] text-[12px] text-[var(--text-n4)]">
              {{ fmtDateTime(item.submitTime || item.createTime) }}
            </div>
          </div>
          <!-- 审批动作（对齐 web：通过 / 驳回，按类型鉴权） -->
          <div
            v-if="canApprove(item.type) || canReject(item.type)"
            class="mt-[8px] flex justify-end gap-[8px] border-t border-[var(--text-n8)] pt-[8px]"
          >
            <van-button
              v-if="canReject(item.type)"
              size="small"
              plain
              type="danger"
              :disabled="operatingId === item.id"
              @click.stop="openReject(item)"
            >
              {{ t('advertising.approval.reject') }}
            </van-button>
            <van-button
              v-if="canApprove(item.type)"
              size="small"
              type="primary"
              :disabled="operatingId === item.id"
              @click.stop="onApprove(item)"
            >
              {{ t('advertising.approval.approve') }}
            </van-button>
          </div>
        </div>
        <van-empty v-if="!list.length && !loading" :description="t('advertising.noTodo')" />
      </van-list>
    </div>

    <!-- 驳回原因弹层 -->
    <van-popup v-model:show="rejectVisible" position="bottom" round>
      <div class="p-[16px]">
        <div class="mb-[12px] text-center text-[15px] font-semibold text-[var(--text-n1)]">
          {{ t('advertising.approval.confirmReject') }}
        </div>
        <van-field
          v-model="rejectRemark"
          type="textarea"
          rows="3"
          autosize
          :placeholder="t('advertising.approval.rejectRemark')"
          class="rounded-lg bg-[var(--text-n9)]"
        />
        <div class="mt-[16px] flex gap-[12px]">
          <van-button block plain @click="rejectVisible = false">{{ t('advertising.common.cancel') }}</van-button>
          <van-button block type="danger" :loading="submitting" @click="confirmReject">
            {{ t('advertising.approval.reject') }}
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { showSuccessToast, showToast } from 'vant';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdApprovalTodoItem } from '@lib/shared/models/advertising';

  import {
    approveAdOrder,
    approveAdOrderChange,
    approveAdPayout,
    approveAdReceipt,
    approveAdSeal,
    approveArchive,
    approveVoid,
    getAdApprovalPendingPage,
    rejectAdOrder,
    rejectAdOrderChange,
    rejectAdSeal,
    rejectArchive,
    rejectVoid,
  } from '@/api/modules';
  import { AdvertisingRouteEnum } from '@/enums/routeEnum';
  import { hasPermission } from '@/utils/permission';
  import { fmtAmount, fmtDateTime } from '@/views/advertising/utils';

  const { t } = useI18n();
  const router = useRouter();

  const typeOptions = [
    { value: '', label: t('advertising.approval.filterAll') },
    { value: 'order', label: t('advertising.approval.filterOrder') },
    { value: 'change', label: t('advertising.approval.filterChange') },
    { value: 'seal', label: t('advertising.approval.filterSeal') },
    { value: 'archive', label: t('advertising.approval.filterArchive') },
    { value: 'receipt', label: t('advertising.approval.filterReceipt') },
    { value: 'payout', label: t('advertising.approval.filterPayout') },
    { value: 'void', label: t('advertising.approval.filterVoid') },
  ];

  const keyword = ref('');
  const filterType = ref('');
  const listKey = ref(0);
  const list = ref<AdApprovalTodoItem[]>([]);
  const loading = ref(false);
  const finished = ref(false);
  const current = ref(1);
  const total = ref(0);

  const operatingId = ref('');
  const submitting = ref(false);
  const rejectVisible = ref(false);
  const rejectRemark = ref('');
  const rejectTarget = ref<AdApprovalTodoItem | null>(null);

  function descOf(item: AdApprovalTodoItem): string {
    const parts = [item.businessEntityName, item.customerName, item.applicantName].filter(Boolean);
    return parts.join(' · ') || item.summary || '-';
  }

  // 各类型「审批通过 / 驳回」权限码（与 web 完全一致）
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
      case 'void':
        return 'AD_CONTRACT:VOID_APPROVE';
      case 'receipt':
        return 'AD_RECEIPT:APPROVE';
      case 'payout':
        return 'AD_PAYOUT:APPROVE';
      default:
        return '';
    }
  }
  function rejectPermissionOf(type?: string): string {
    switch (type) {
      case 'order':
        return 'AD_ORDER:REJECT';
      case 'change':
        return 'AD_ORDER_CHANGE:REJECT';
      case 'seal':
        return 'AD_SEAL:REJECT';
      default:
        // archive / void / receipt / payout 通过、驳回共用同一权限码
        return approvePermissionOf(type);
    }
  }
  function canApprove(type?: string): boolean {
    const code = approvePermissionOf(type);
    return !!code && hasPermission(code);
  }
  function canReject(type?: string): boolean {
    const code = rejectPermissionOf(type);
    return !!code && hasPermission(code);
  }

  function reset() {
    current.value = 1;
    list.value = [];
    finished.value = false;
    total.value = 0;
    listKey.value++;
  }

  async function onLoad() {
    loading.value = true;
    try {
      const res: any = await getAdApprovalPendingPage({
        current: current.value,
        pageSize: 20,
        type: filterType.value || null,
        keyword: keyword.value || undefined,
      });
      const records = (res?.list || res?.records || []) as AdApprovalTodoItem[];
      list.value.push(...records);
      total.value = res?.total ?? 0;
      finished.value = list.value.length >= total.value;
      current.value++;
    } catch {
      finished.value = true;
    } finally {
      loading.value = false;
    }
  }

  function onTypeChange(value: string) {
    if (filterType.value === value) return;
    filterType.value = value;
    reset();
  }

  // 通过：按 type 调用各业务模块审批接口（参数与 web 一致）
  async function approveOne(row: AdApprovalTodoItem, remark?: string) {
    const id = row.businessId || row.id;
    if (!id) return;
    const { type } = row;
    if (type === 'order') await approveAdOrder(id, { action: 'approve', remark });
    else if (type === 'change') await approveAdOrderChange(id, { remark });
    else if (type === 'seal') await approveAdSeal(id, { approveRemark: remark });
    else if (type === 'archive') await approveArchive(id, remark);
    else if (type === 'void') await approveVoid(id, remark);
    else if (type === 'receipt') await approveAdReceipt(id, { action: 'approve', remark });
    else if (type === 'payout') await approveAdPayout(id, { action: 'approve', remark });
  }

  // 驳回：同上
  async function rejectOne(row: AdApprovalTodoItem, remark?: string) {
    const id = row.businessId || row.id;
    if (!id) return;
    const { type } = row;
    if (type === 'order') await rejectAdOrder(id, { action: 'reject', remark });
    else if (type === 'change') await rejectAdOrderChange(id, { remark });
    else if (type === 'seal') await rejectAdSeal(id, { approveRemark: remark });
    else if (type === 'archive') await rejectArchive(id, remark);
    else if (type === 'void') await rejectVoid(id, remark);
    else if (type === 'receipt') await approveAdReceipt(id, { action: 'reject', remark });
    else if (type === 'payout') await approveAdPayout(id, { action: 'reject', remark });
  }

  async function onApprove(row: AdApprovalTodoItem) {
    if (operatingId.value) return;
    operatingId.value = row.id || '';
    try {
      await approveOne(row);
      showSuccessToast(t('advertising.common.operateSuccess'));
      reset();
    } finally {
      operatingId.value = '';
    }
  }

  function openReject(row: AdApprovalTodoItem) {
    rejectTarget.value = row;
    rejectRemark.value = '';
    rejectVisible.value = true;
  }

  async function confirmReject() {
    if (!rejectTarget.value) return;
    submitting.value = true;
    try {
      await rejectOne(rejectTarget.value, rejectRemark.value);
      showSuccessToast(t('advertising.common.operateSuccess'));
      rejectVisible.value = false;
      reset();
    } finally {
      submitting.value = false;
    }
  }

  // 跳转各类型业务详情（口径对齐 web 抽屉：archive/void 指向合同详情）
  const DETAIL_ROUTE: Record<string, string> = {
    order: AdvertisingRouteEnum.AD_ORDER_DETAIL,
    change: AdvertisingRouteEnum.AD_CHANGE_DETAIL,
    seal: AdvertisingRouteEnum.AD_SEAL_DETAIL,
    archive: AdvertisingRouteEnum.AD_CONTRACT_DETAIL,
    void: AdvertisingRouteEnum.AD_CONTRACT_DETAIL,
    receipt: AdvertisingRouteEnum.AD_RECEIPT_DETAIL,
    payout: AdvertisingRouteEnum.AD_PAYOUT_DETAIL,
  };

  function goDetail(item: AdApprovalTodoItem) {
    const id = item.businessId;
    const name = DETAIL_ROUTE[item.type || ''];
    if (!id || !name) {
      showToast(t('advertising.approval.devTip'));
      return;
    }
    const query: Record<string, string> = { id };
    if (item.type === 'order') {
      query.customerName = item.customerName || '';
      query.businessEntityName = item.businessEntityName || '';
      query.creatorName = item.applicantName || '';
    }
    router.push({ name, query });
  }
</script>
