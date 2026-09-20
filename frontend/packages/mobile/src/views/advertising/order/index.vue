<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar :title="t('advertising.order')" left-arrow @click-left="$router.back()">
      <template #right>
        <CrmIcon name="iconicon_filter" width="20px" height="20px" @click="filterVisible = true" />
        <van-icon
          v-if="hasPermission('AD_ORDER:CREATE')"
          name="plus"
          size="20"
          class="ml-[14px]"
          @click="goCreate"
        />
      </template>
    </van-nav-bar>

    <div class="bg-white px-[12px] pb-[8px]">
      <van-search
        v-model="keyword"
        :placeholder="t('advertising.order.searchPlaceholder')"
        shape="round"
        @search="handleSearch"
        @clear="handleSearch"
      />
    </div>

    <div class="flex-1 overflow-hidden pt-[8px]">
      <CrmList
        ref="listRef"
        v-model="list"
        v-model:loading="loading"
        :keyword="keyword"
        :list-params="listParams"
        :load-list-api="getAdOrderPage"
      >
        <template #item="{ item }">
          <div class="mx-[12px] mb-[8px] rounded-lg bg-white p-[12px]" @click="goDetail(item)">
            <div class="flex items-center justify-between gap-[8px]">
              <span class="one-line-text text-[12px] text-[var(--text-n3)]">{{ item.orderNo || '-' }}</span>
              <CrmTag :tag="getAdOrderStatusLabel(item.status)" v-bind="getAdOrderStatusTagStyle(item.status)" />
            </div>
            <div class="one-line-text mt-[6px] text-[15px] font-semibold text-[var(--text-n1)]">
              {{ item.orderName || '-' }}
            </div>
            <div class="mt-[4px] flex flex-wrap items-center gap-x-[8px] gap-y-[2px] text-[12px] text-[var(--text-n3)]">
              <span class="one-line-text max-w-[55%]">{{ item.customerName || '-' }}</span>
              <span v-if="item.creatorName">· {{ item.creatorName }}</span>
              <span v-if="item.deliveryStartDate">· {{ fmtDate(item.deliveryStartDate) }}</span>
            </div>
            <div class="mt-[10px] flex items-center gap-[32px]">
              <div>
                <div class="text-[12px] text-[var(--text-n3)]">{{ t('advertising.order.totalAmount') }}</div>
                <div class="text-[14px] font-semibold text-[var(--text-n1)]">{{ fmtAmount(item.totalAmount) }}</div>
              </div>
              <div>
                <div class="text-[12px] text-[var(--text-n3)]">{{ t('advertising.order.receivableAmount') }}</div>
                <div class="text-[14px] font-semibold text-[var(--text-n1)]">
                  {{ fmtAmount(item.receivableAmount) }}
                </div>
              </div>
            </div>
            <div class="mt-[10px] flex flex-wrap items-center gap-[6px]">
              <CrmTag
                :tag="item.receiptDone === 1 ? t('advertising.order.received') : t('advertising.order.notReceived')"
                v-bind="getDoneTagStyle(item.receiptDone)"
              />
              <CrmTag
                :tag="item.paymentDone === 1 ? t('advertising.order.paid') : t('advertising.order.notPaid')"
                v-bind="getDoneTagStyle(item.paymentDone)"
              />
              <CrmTag
                :tag="item.missingContract === 1 ? t('advertising.order.notSubmitted') : t('advertising.order.submitted')"
                v-bind="getDoneTagStyle(item.missingContract === 1 ? 0 : 1)"
              />
            </div>
          </div>
        </template>
      </CrmList>
    </div>

    <!-- 筛选弹层 -->
    <van-popup v-model:show="filterVisible" position="bottom" round :style="{ maxHeight: '82%' }">
      <div class="flex flex-col p-[16px]">
        <div class="mb-[8px] text-center text-[16px] font-semibold text-[var(--text-n1)]">
          {{ t('advertising.order.filter') }}
        </div>
        <van-field
          :model-value="statusText"
          readonly
          is-link
          :label="t('advertising.order.status')"
          @click="openPicker('status')"
        />
        <van-field
          :model-value="orderTypeText"
          readonly
          is-link
          :label="t('advertising.order.orderType')"
          @click="openPicker('orderType')"
        />
        <van-field
          :model-value="receiptMethodText"
          readonly
          is-link
          :label="t('advertising.order.receiptMethod')"
          @click="openPicker('receiptMethod')"
        />
        <van-field
          :model-value="paymentMethodText"
          readonly
          is-link
          :label="t('advertising.order.paymentMethod')"
          @click="openPicker('paymentMethod')"
        />
        <van-field
          :model-value="missingContractText"
          readonly
          is-link
          :label="t('advertising.order.contractStatus')"
          @click="openPicker('missingContract')"
        />
        <van-field
          :model-value="deliveryRangeText"
          readonly
          is-link
          :label="t('advertising.order.deliveryDate')"
          @click="calendarVisible = true"
        />
        <div class="mt-[20px] flex gap-[12px]">
          <van-button block plain @click="handleReset">{{ t('advertising.order.reset') }}</van-button>
          <van-button block type="primary" @click="handleFilterConfirm">
            {{ t('advertising.order.confirm') }}
          </van-button>
        </div>
      </div>
    </van-popup>

    <van-action-sheet v-model:show="pickerVisible" :actions="pickerActions" @select="onPickerSelect" />
    <van-calendar v-model:show="calendarVisible" type="range" @confirm="onCalendarConfirm" />
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import {
    AdOrderStatusOptions,
    AdOrderTypeOptions,
    AdPaymentMethodOptions,
    AdReceiptMethodOptions,
    getAdOrderStatusLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import CrmList from '@/components/pure/crm-list/index.vue';
  import CrmTag from '@/components/pure/crm-tag/index.vue';
  import { getAdOrderPage } from '@/api/modules';
  import { AdvertisingRouteEnum } from '@/enums/routeEnum';
  import { hasPermission } from '@/utils/permission';
  import { fmtAmount, fmtDate, getAdOrderStatusTagStyle, getDoneTagStyle } from '@/views/advertising/utils';
  import useAdListQueryFilter from '@/views/advertising/components/useListQueryFilter';

  type FilterField = 'status' | 'orderType' | 'receiptMethod' | 'paymentMethod' | 'missingContract';

  const { t } = useI18n();
  const router = useRouter();

  const listRef = ref();
  const list = ref<Record<string, any>[]>([]);
  const loading = ref(false);
  const keyword = ref('');

  const searchForm = ref<Record<FilterField, number | null>>({
    status: null,
    orderType: null,
    receiptMethod: null,
    paymentMethod: null,
    missingContract: null,
  });
  const deliveryRange = ref<[number, number] | null>(null);

  const filterVisible = ref(false);
  const pickerVisible = ref(false);
  const calendarVisible = ref(false);
  const activePicker = ref<FilterField>('status');

  const listParams = computed(() => ({
    status: searchForm.value.status ?? null,
    orderType: searchForm.value.orderType ?? null,
    receiptMethod: searchForm.value.receiptMethod ?? null,
    paymentMethod: searchForm.value.paymentMethod ?? null,
    missingContract: searchForm.value.missingContract ?? null,
    deliveryStartFrom: deliveryRange.value?.[0] ?? null,
    deliveryStartTo: deliveryRange.value?.[1] ?? null,
  }));

  // 工作台待办跳转携带 query 时，自动套用状态/补单筛选（对齐 web）
  useAdListQueryFilter(searchForm, listRef, ['status', 'missingContract']);

  function labelOf(options: { label: string; value: number }[], value: number | null): string {
    if (value === null || value === undefined) return t('advertising.order.all');
    return options.find((o) => o.value === value)?.label ?? t('advertising.order.all');
  }

  const statusText = computed(() => labelOf(AdOrderStatusOptions, searchForm.value.status));
  const orderTypeText = computed(() => labelOf(AdOrderTypeOptions, searchForm.value.orderType));
  const receiptMethodText = computed(() => labelOf(AdReceiptMethodOptions, searchForm.value.receiptMethod));
  const paymentMethodText = computed(() => labelOf(AdPaymentMethodOptions, searchForm.value.paymentMethod));
  const missingContractText = computed(() => {
    if (searchForm.value.missingContract === null) return t('advertising.order.all');
    return searchForm.value.missingContract === 1
      ? t('advertising.order.notSubmitted')
      : t('advertising.order.submitted');
  });
  const deliveryRangeText = computed(() => {
    if (!deliveryRange.value) return t('advertising.order.unlimited');
    return `${fmtDate(deliveryRange.value[0])} ~ ${fmtDate(deliveryRange.value[1])}`;
  });

  const pickerActions = computed(() => {
    const all: { name: string; value: number | null } = { name: t('advertising.order.all'), value: null };
    switch (activePicker.value) {
      case 'status':
        return [all, ...AdOrderStatusOptions.map((o) => ({ name: o.label, value: o.value as number | null }))];
      case 'orderType':
        return [all, ...AdOrderTypeOptions.map((o) => ({ name: o.label, value: o.value as number | null }))];
      case 'receiptMethod':
        return [all, ...AdReceiptMethodOptions.map((o) => ({ name: o.label, value: o.value as number | null }))];
      case 'paymentMethod':
        return [all, ...AdPaymentMethodOptions.map((o) => ({ name: o.label, value: o.value as number | null }))];
      case 'missingContract':
        return [
          all,
          { name: t('advertising.order.notSubmitted'), value: 1 },
          { name: t('advertising.order.submitted'), value: 0 },
        ];
      default:
        return [all];
    }
  });

  function openPicker(field: FilterField) {
    activePicker.value = field;
    pickerVisible.value = true;
  }

  function onPickerSelect(action: Record<string, any>) {
    searchForm.value[activePicker.value] = (action.value ?? null) as number | null;
    pickerVisible.value = false;
  }

  function onCalendarConfirm(dates: any) {
    const range = Array.isArray(dates) ? dates : [dates, dates];
    deliveryRange.value = [new Date(range[0]).getTime(), new Date(range[1]).getTime()];
    calendarVisible.value = false;
  }

  function reload() {
    listRef.value?.loadList(true);
  }

  function handleSearch() {
    reload();
  }

  function handleFilterConfirm() {
    filterVisible.value = false;
    reload();
  }

  function handleReset() {
    searchForm.value = {
      status: null,
      orderType: null,
      receiptMethod: null,
      paymentMethod: null,
      missingContract: null,
    };
    deliveryRange.value = null;
  }

  function goDetail(item: Record<string, any>) {
    // 详情接口不返回名称字段，列表跳转时顺带携带用于回显
    router.push({
      name: AdvertisingRouteEnum.AD_ORDER_DETAIL,
      query: {
        id: item.id,
        customerName: item.customerName || '',
        businessEntityName: item.businessEntityName || '',
        upstreamAgentName: item.upstreamAgentName || '',
        creatorName: item.creatorName || '',
      },
    });
  }

  function goCreate() {
    router.push({ name: AdvertisingRouteEnum.AD_ORDER_FORM });
  }
</script>
