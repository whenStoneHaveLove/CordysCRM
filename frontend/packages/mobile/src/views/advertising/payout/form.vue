<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar :title="t('advertising.payout.form.title')" left-arrow @click-left="back">
      <template #right>
        <span class="px-[4px] text-[14px] text-[#1989fa]" @click="onSave(false)">{{ t('advertising.form.save') }}</span>
        <span
          v-if="hasPermission('AD_PAYOUT:CREATE')"
          class="ml-[10px] px-[4px] text-[14px] text-[#1989fa]"
          @click="onSave(true)"
          >{{ t('advertising.order.detail.submit') }}</span
        >
      </template>
    </van-nav-bar>

    <div class="flex-1 overflow-auto px-[12px] py-[12px]">
      <van-cell class="mb-[10px] rounded-lg" :title="t('advertising.payout.form.billType')" required>
        <template #value>
          <van-radio-group v-model="billType" direction="horizontal">
            <van-radio
              v-for="o in AdPayoutBillTypeOptions"
              :key="o.value"
              :name="o.value"
              @click="onBillTypeChange"
              >{{ o.label }}</van-radio
            >
          </van-radio-group>
        </template>
      </van-cell>

      <van-field
        v-if="billType === 10 && !editId"
        :label="t('advertising.payout.form.orderId')"
        :model-value="orderLabel"
        readonly
        required
        is-link
        @click="openOrderPicker"
      />
      <!-- 非订单类型：下游客户 + 收款账户（账户取自下游客户模块，对齐 web） -->
      <van-field
        v-if="billType === 20"
        :label="t('advertising.payout.form.customerId')"
        :model-value="manualMediaName"
        readonly
        required
        is-link
        :placeholder="t('advertising.form.required')"
        @click="openMediaPicker"
      />
      <van-field
        v-if="billType === 20"
        :label="t('advertising.payout.form.accountId')"
        :model-value="manualAccountLabel"
        readonly
        required
        is-link
        :placeholder="t('advertising.form.required')"
        @click="openManualAccountPicker"
      />

      <van-field
        :label="t('advertising.payout.form.amount')"
        v-model="amount"
        type="number"
        required
        :placeholder="t('advertising.payout.form.amount')"
      />
      <van-field
        :label="t('advertising.payout.form.type')"
        :model-value="enumLabel(AdPayoutTypeOptions, type)"
        readonly
        is-link
        @click="openEnumPicker('type', AdPayoutTypeOptions, t('advertising.payout.form.type'))"
      />
      <van-field
        :label="t('advertising.payout.form.paymentDate')"
        :model-value="dateLabel"
        readonly
        is-link
        @click="openDatePicker"
      />
      <van-field
        :label="t('advertising.payout.form.remark')"
        v-model="remark"
        type="textarea"
        rows="2"
        :placeholder="t('advertising.payout.form.remark')"
      />

      <!-- 下游客户付款明细（订单类型，对齐 web「各下游客户返点信息与本次付款」） -->
      <div v-if="mediaRows.length" class="mt-[12px] text-[14px] font-medium text-[var(--text-n1)]">
        {{ t('advertising.payout.form.mediaList') }}
      </div>
      <div
        v-for="(row, idx) in mediaRows"
        :key="row.mediaId || idx"
        class="mb-[10px] rounded-lg bg-white p-[10px]"
      >
        <div class="mb-[6px] text-[13px] font-medium">{{ row.mediaName }}</div>
        <van-field :label="t('advertising.order.payableAmount')" :model-value="fmtAmount(row.payableAmount)" readonly size="small" />
        <van-field :label="t('advertising.payout.form.noRebateAmount')" :model-value="fmtAmount(row.noRebateAmount)" readonly size="small" />
        <van-field
          :label="t('advertising.order.rebateMode')"
          :model-value="enumLabel(AdModeOptions, row.rebateMode)"
          readonly
          is-link
          size="small"
          @click="openRowEnumPicker(idx, 'rebateMode', AdModeOptions)"
        />
        <van-field :label="t('advertising.order.rebateValue')" v-model="row.rebateValue" type="number" size="small" />
        <van-field :label="t('advertising.payout.form.rebateAmount')" :model-value="fmtAmount(row.rebateAmount)" readonly size="small" />
        <van-field :label="t('advertising.payout.form.actualPayable')" v-model="row.actualPayable" type="number" size="small" />
        <van-field :label="t('advertising.payout.form.paidAmount')" :model-value="fmtAmount(row.paidAmount)" readonly size="small" />
        <van-field :label="t('advertising.payout.form.remaining')" :model-value="fmtAmount(remainingOf(row))" readonly size="small" />
        <van-field :label="t('advertising.payout.form.thisPayable')" v-model="row.thisPayable" type="number" size="small" />
        <van-field
          :label="t('advertising.payout.form.accountId')"
          :model-value="accountLabel(row)"
          readonly
          is-link
          size="small"
          @click="openAccountPicker(idx)"
        />
      </div>
    </div>

    <!-- 订单选择 -->
    <van-popup v-model:show="orderPickerShow" position="bottom" round :style="{ height: '70%' }">
      <div class="flex h-full flex-col">
        <van-search v-model="orderKeyword" :placeholder="t('advertising.searchPlaceholder')" />
        <van-radio-group v-model="orderId" class="flex-1 overflow-auto">
          <van-cell
            v-for="o in filteredOrders"
            :key="o.id"
            :title="o.orderName || o.orderNo"
            :label="o.orderNo"
            clickable
            @click="pickOrder(o)"
          >
            <template #right-icon>
              <van-radio :name="o.id" />
            </template>
          </van-cell>
        </van-radio-group>
      </div>
    </van-popup>

    <!-- 下游客户选择（非订单类型；对齐 web，取自下游客户模块） -->
    <van-popup v-model:show="mediaPickerShow" position="bottom" round :style="{ height: '70%' }">
      <div class="flex h-full flex-col">
        <van-search v-model="mediaKeyword" :placeholder="t('advertising.searchPlaceholder')" />
        <van-radio-group v-model="manualMediaId" class="flex-1 overflow-auto">
          <van-cell v-for="m in filteredMedia" :key="m.id" :title="m.name" clickable @click="pickMedia(m)">
            <template #right-icon>
              <van-radio :name="m.id" />
            </template>
          </van-cell>
        </van-radio-group>
      </div>
    </van-popup>

    <!-- 枚举选择 -->
    <van-popup v-model:show="enumShow" position="bottom" round :style="{ height: '60%' }">
      <div class="flex h-full flex-col">
        <div class="p-[12px] text-[15px] font-semibold">{{ enumTitle }}</div>
        <van-radio-group v-model="enumValue" class="flex-1 overflow-auto">
          <van-cell
            v-for="opt in enumOptions"
            :key="opt.value"
            :title="opt.label"
            clickable
            @click="pickEnum(opt)"
          >
            <template #right-icon>
              <van-radio :name="opt.value" />
            </template>
          </van-cell>
        </van-radio-group>
      </div>
    </van-popup>

    <!-- 账户选择 -->
    <van-popup v-model:show="accountShow" position="bottom" round :style="{ height: '60%' }">
      <div class="flex h-full flex-col">
        <div class="p-[12px] text-[15px] font-semibold">{{ t('advertising.payout.form.accountId') }}</div>
        <van-radio-group v-model="accountValue" class="flex-1 overflow-auto">
          <van-cell
            v-for="a in currentAccounts"
            :key="a.value"
            :title="a.label"
            clickable
            @click="pickAccount(a)"
          >
            <template #right-icon>
              <van-radio :name="a.value" />
            </template>
          </van-cell>
        </van-radio-group>
      </div>
    </van-popup>

    <!-- 非订单类型：收款账户选择（取自下游客户模块的银行账户） -->
    <van-popup v-model:show="manualAccountShow" position="bottom" round :style="{ height: '60%' }">
      <div class="flex h-full flex-col">
        <div class="p-[12px] text-[15px] font-semibold">{{ t('advertising.payout.form.accountId') }}</div>
        <van-radio-group v-model="manualAccountValue" class="flex-1 overflow-auto">
          <van-cell
            v-for="a in manualAccounts"
            :key="a.value"
            :title="a.label"
            clickable
            @click="pickManualAccount(a)"
          >
            <template #right-icon>
              <van-radio :name="a.value" />
            </template>
          </van-cell>
        </van-radio-group>
      </div>
    </van-popup>

    <!-- 日期 -->
    <van-popup v-model:show="dateShow" position="bottom" round>
      <van-date-picker
        :model-value="dateModel"
        @confirm="pickDate"
        @cancel="dateShow = false"
        :columns-type="['year', 'month', 'day']"
        title=""
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { showToast } from 'vant';

  import { AdModeOptions, AdPayoutBillTypeOptions, AdPayoutTypeOptions } from '@lib/shared/enums/advertisingEnum';
  import { AdvertisingRouteEnum } from '@/enums/routeEnum';
  import { hasPermission } from '@/utils/permission';

  import {
    createAdPayout,
    getAdDownstreamMediaAccounts,
    getAdDownstreamMediaPage,
    getAdOrderPage,
    getAdPayoutDetail,
    getAdPayoutMedia,
    getAdPayoutRemaining,
    submitAdPayout,
    updateAdPayout,
  } from '@/api/modules';
  import { fmtAmount } from '@/views/advertising/utils';

  interface Option {
    label: string;
    value: number | string;
  }
  interface MediaRow {
    id?: string;
    mediaId: string;
    mediaName: string;
    payableAmount?: number | string;
    noRebateAmount?: number | string;
    rebateMode?: number;
    rebateValue?: number | string;
    rebateAmount?: number | string;
    actualPayable?: number | string;
    /** 累计已付（只读，来自订单） */
    paidAmount?: number | string;
    /** 本次付款（可编辑，默认 = 剩余应付，回写后端 paidAmount） */
    thisPayable?: number | string;
    accounts: Option[];
    accountId?: string;
  }

  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();
  const editId = ref<string>((route.query.id as string) || '');

  const billType = ref<number>(10);
  const orderId = ref('');
  /** 非订单类型：下游客户（对齐 web，取自下游客户模块） */
  const manualMediaId = ref('');
  const manualMediaName = ref('');
  const mediaOptions = ref<any[]>([]);
  const mediaKeyword = ref('');
  const mediaPickerShow = ref(false);
  /** 非订单类型：收款账户（取自所选下游客户的银行账户） */
  const manualAccountId = ref('');
  const manualAccountValue = ref<number | string>();
  const manualAccounts = ref<Option[]>([]);
  const manualAccountShow = ref(false);
  const amount = ref<number | string>('');
  const type = ref<number>(10);
  const paymentTime = ref<number | string | null>(null);
  const remark = ref('');
  const mediaRows = ref<MediaRow[]>([]);

  const orders = ref<any[]>([]);
  const orderKeyword = ref('');
  const orderPickerShow = ref(false);

  const enumShow = ref(false);
  const enumOptions = ref<Option[]>([]);
  const enumValue = ref<number | string>();
  const enumField = ref('');
  const enumTitle = ref('');
  const enumRowIdx = ref(-1);

  const accountShow = ref(false);
  const currentAccounts = ref<Option[]>([]);
  const accountValue = ref<number | string>();
  const accountRowIdx = ref(-1);

  const dateShow = ref(false);
  /** 当天日期模型：未选值时默认定位今天（否则 vant 默认停在 2020-01-01），vant 日期选择器模型为 string[] */
  function todayModel(): string[] {
    const d = new Date();
    return [String(d.getFullYear()), String(d.getMonth() + 1), String(d.getDate())];
  }
  const dateModel = ref<string[]>(todayModel());

  const orderLabel = computed(() => {
    const o = orders.value.find((it) => it.id === orderId.value);
    return o ? [o.orderName, o.orderNo].filter(Boolean).join(' ') : '';
  });
  const manualAccountLabel = computed(
    () => manualAccounts.value.find((a) => a.value === manualAccountId.value)?.label || ''
  );
  const filteredOrders = computed(() => {
    const kw = orderKeyword.value.trim();
    if (!kw) return orders.value;
    return orders.value.filter((o) => `${o.orderName || ''}${o.orderNo || ''}`.includes(kw));
  });
  const filteredMedia = computed(() => {
    const kw = mediaKeyword.value.trim();
    if (!kw) return mediaOptions.value;
    return mediaOptions.value.filter((m) => `${m.name || ''}`.includes(kw));
  });
  const dateLabel = computed(() => (paymentTime.value ? new Date(paymentTime.value).toLocaleDateString('zh-CN') : ''));

  function enumLabel(options: Option[], value: any): string {
    if (value === null || value === undefined || value === '') return '';
    return options.find((o) => o.value === value)?.label || String(value);
  }
  function accountLabel(row: MediaRow): string {
    if (!row.accountId) return '';
    return row.accounts.find((a) => a.value === row.accountId)?.label || String(row.accountId);
  }

  function back() {
    router.back();
  }

  function onBillTypeChange() {
    mediaRows.value = [];
    if (billType.value === 10) {
      manualMediaId.value = '';
      manualMediaName.value = '';
      manualAccountId.value = '';
      manualAccounts.value = [];
    } else {
      orderId.value = '';
    }
  }

  function openOrderPicker() {
    orderPickerShow.value = true;
  }
  async function pickOrder(o: any) {
    orderId.value = o.id;
    orderPickerShow.value = false;
    await loadMedia(o.id);
    if (!editId.value) {
      try {
        const remaining = await getAdPayoutRemaining(o.id);
        amount.value = Number(remaining ?? 0);
      } catch {
        amount.value = mediaRows.value.reduce((s, r) => s + (Number(r.thisPayable) || 0), 0);
      }
    }
  }
  function openMediaPicker() {
    mediaPickerShow.value = true;
  }
  /** 加载所选下游客户的银行账户（账户信息来自下游客户模块，丢弃已停用账户） */
  async function loadManualAccounts() {
    manualAccounts.value = [];
    if (!manualMediaId.value) return;
    try {
      const accounts: any[] = (await getAdDownstreamMediaAccounts(manualMediaId.value)) || [];
      manualAccounts.value = accounts
        .filter((a) => a.disabled !== 1)
        .map((a) => ({
          label: [a.payeeName, a.bankName, a.bankAccount].filter(Boolean).join(' ') || a.id,
          value: a.id,
        }));
    } catch {
      manualAccounts.value = [];
    }
  }
  /** 选择下游客户后加载账户并默认选中第一条可用账户（对齐 web onManualMediaChange） */
  async function pickMedia(m: any) {
    manualMediaId.value = m.id;
    manualMediaName.value = m.name || '';
    manualAccountId.value = '';
    mediaPickerShow.value = false;
    await loadManualAccounts();
    if (manualAccounts.value.length) {
      manualAccountId.value = String(manualAccounts.value[0].value);
      manualAccountValue.value = manualAccountId.value;
    }
  }
  function openManualAccountPicker() {
    if (!manualMediaId.value) {
      showToast(t('advertising.payout.form.customerId') + t('advertising.form.required'));
      return;
    }
    manualAccountValue.value = manualAccountId.value;
    manualAccountShow.value = true;
  }
  function pickManualAccount(a: Option) {
    manualAccountValue.value = a.value;
    manualAccountId.value = String(a.value);
    manualAccountShow.value = false;
  }

  function remainingOf(row: MediaRow): number {
    return Number(row.actualPayable || 0) - Number(row.paidAmount || 0);
  }

  async function loadMedia(orderIdVal: string) {
    if (!orderIdVal) return;
    // 该接口入参是订单 id 字符串（路径参数）；传对象会拼成 /ad/payout/media/[object Object] 而 400
    const res: any = await getAdPayoutMedia(orderIdVal);
    const list: any[] = res?.list || res?.records || res || [];
    mediaRows.value = list.map((m: any) => {
      const accList: any[] = m.accountList || [];
      const actualPayable = Number(m.actualPayable ?? m.payableAmount ?? 0);
      const paidAmount = Number(m.paidAmount ?? 0);
      return {
        id: m.id,
        mediaId: m.mediaId,
        mediaName: m.mediaName,
        payableAmount: m.payableAmount,
        noRebateAmount: m.noRebateAmount,
        rebateMode: m.rebateMode ?? 10,
        rebateValue: m.rebateValue ?? 0,
        rebateAmount: m.rebateAmount,
        actualPayable: m.actualPayable ?? m.payableAmount,
        paidAmount: m.paidAmount,
        thisPayable: Math.max(actualPayable - paidAmount, 0),
        accounts: accList.map((a: any) => ({
          label: [a.payeeName, a.bankName, a.bankAccount].filter(Boolean).join(' '),
          value: a.id,
        })),
        accountId:
          accList.find((a: any) => !a.disabled && a.isDefault)?.id ||
          accList.find((a: any) => !a.disabled)?.id,
      };
    });
  }

  function openEnumPicker(field: string, options: Option[], title: string) {
    enumField.value = field;
    enumRowIdx.value = -1;
    enumOptions.value = options;
    enumValue.value = type.value as any;
    enumTitle.value = title;
    enumShow.value = true;
  }
  function openRowEnumPicker(idx: number, field: string, options: Option[]) {
    enumField.value = field;
    enumRowIdx.value = idx;
    enumOptions.value = options;
    enumValue.value = (mediaRows.value[idx] as any)[field];
    enumTitle.value = '';
    enumShow.value = true;
  }
  function pickEnum(opt: Option) {
    enumValue.value = opt.value;
    applyEnum();
  }
  function applyEnum() {
    if (enumRowIdx.value >= 0) {
      (mediaRows.value[enumRowIdx.value] as any)[enumField.value] = enumValue.value;
    } else if (enumField.value === 'type') {
      type.value = enumValue.value as number;
    }
    enumShow.value = false;
  }

  function openAccountPicker(idx: number) {
    accountRowIdx.value = idx;
    currentAccounts.value = mediaRows.value[idx].accounts;
    accountValue.value = mediaRows.value[idx].accountId;
    accountShow.value = true;
  }
  function pickAccount(a: Option) {
    accountValue.value = a.value;
    mediaRows.value[accountRowIdx.value].accountId = String(a.value);
    accountShow.value = false;
  }

  function openDatePicker() {
    // 无值时默认定位今天，避免 vant 日期选择器停在 2020-01-01
    const d = paymentTime.value ? new Date(paymentTime.value as any) : null;
    dateModel.value =
      d && !Number.isNaN(d.getTime())
        ? [String(d.getFullYear()), String(d.getMonth() + 1), String(d.getDate())]
        : todayModel();
    dateShow.value = true;
  }
  function pickDate(val: { selectedValues: string[] }) {
    const [y, m, d] = val.selectedValues.map(Number);
    paymentTime.value = new Date(y, m - 1, d).toISOString();
    dateShow.value = false;
  }

  function buildPayload() {
    const payload: Record<string, any> = {
      billType: billType.value,
      amount: Number(amount.value) || 0,
      type: type.value,
      paymentTime: paymentTime.value,
      remark: remark.value,
    };
    if (billType.value === 10) {
      payload.orderId = orderId.value;
      if (mediaRows.value.length) {
        payload.mediaDetails = mediaRows.value.map((r) => ({
          orderDownstreamMediaId: r.id,
          mediaId: r.mediaId,
          mediaName: r.mediaName,
          payableAmount: Number(r.payableAmount) || 0,
          noRebateAmount: Number(r.noRebateAmount) || 0,
          rebateMode: r.rebateMode ?? 10,
          rebateValue: Number(r.rebateValue) || 0,
          rebateAmount: Number(r.rebateAmount) || 0,
          actualPayable: Number(r.actualPayable) || 0,
          paidAmount: Number(r.thisPayable) || 0,
          accountId: r.accountId,
        }));
        payload.mediaIds = mediaRows.value.map((r) => r.mediaId).filter(Boolean);
      }
    } else {
      // 非订单类型：仅一条明细（本次付款=付款金额），下游客户与收款账户取自下游客户模块（对齐 web）
      payload.mediaIds = manualMediaId.value ? [manualMediaId.value] : [];
      payload.mediaDetails = manualMediaId.value
        ? [
            {
              mediaId: manualMediaId.value,
              mediaName: manualMediaName.value,
              paidAmount: Number(amount.value) || 0,
              accountId: manualAccountId.value,
            },
          ]
        : [];
    }
    return payload;
  }

  async function onSave(submit: boolean) {
    if (!amount.value || Number(amount.value) <= 0) {
      showToast(t('advertising.payout.form.amount') + t('advertising.form.required'));
      return;
    }
    if (billType.value === 10) {
      if (!orderId.value) {
        showToast(t('advertising.payout.form.orderId') + t('advertising.form.required'));
        return;
      }
      if (submit && mediaRows.value.some((r) => !r.accountId)) {
        showToast(t('advertising.payout.form.accountId') + t('advertising.form.required'));
        return;
      }
    } else {
      if (!manualMediaId.value) {
        showToast(t('advertising.payout.form.customerId') + t('advertising.form.required'));
        return;
      }
      if (!manualAccountId.value) {
        showToast(t('advertising.payout.form.accountId') + t('advertising.form.required'));
        return;
      }
    }
    const payload = buildPayload();
    const saved = editId.value
      ? await updateAdPayout({ id: editId.value, ...payload })
      : await createAdPayout(payload);
    const id = editId.value || saved?.id || saved;
    if (submit && id) await submitAdPayout(String(id));
    showToast(t('advertising.common.operateSuccess'));
    router.back();
  }

  async function loadEdit() {
    if (!editId.value) return;
    const d = await getAdPayoutDetail(editId.value);
    billType.value = d.billType ?? 10;
    orderId.value = d.orderId || '';
    amount.value = d.amount ?? '';
    type.value = d.type ?? 10;
    paymentTime.value = d.paymentTime ?? null;
    remark.value = d.remark || '';
    // 非订单类型：回显下游客户与收款账户
    if (d.billType === 20) {
      const first = (d.mediaDetails || [])[0];
      if (first?.mediaId) {
        manualMediaId.value = first.mediaId;
        manualMediaName.value =
          first.mediaName || mediaOptions.value.find((m) => m.id === first.mediaId)?.name || '';
        await loadManualAccounts();
        if (first.accountId) {
          manualAccountId.value = String(first.accountId);
          manualAccountValue.value = manualAccountId.value;
        }
      }
      return;
    }
    let orderMediaOptions: any[] = [];
    if (d.orderId) {
      const res: any = await getAdPayoutMedia(d.orderId);
      orderMediaOptions = res?.list || res?.records || res || [];
    }
    if (d.mediaDetails?.length) {
      mediaRows.value = d.mediaDetails.map((m: any) => {
        const opt = orderMediaOptions.find((o) => (o.mediaId || o.id) === m.mediaId);
        const accList: any[] = opt?.accountList || [];
        const actualPayable = Number(m.actualPayable ?? 0);
        return {
          id: m.orderDownstreamMediaId || m.id,
          mediaId: m.mediaId,
          mediaName: m.mediaName,
          payableAmount: m.payableAmount,
          noRebateAmount: m.noRebateAmount,
          rebateMode: m.rebateMode ?? 10,
          rebateValue: m.rebateValue ?? 0,
          rebateAmount: m.rebateAmount,
          actualPayable: m.actualPayable,
          paidAmount: m.paidAmount,
          thisPayable: Number(m.paidAmount) || 0,
          accounts: accList.map((a: any) => ({
            label: [a.payeeName, a.bankName, a.bankAccount].filter(Boolean).join(' '),
            value: a.id,
          })),
          accountId: m.accountId,
        };
      });
    } else if (d.orderId) {
      await loadMedia(d.orderId);
    }
  }

  onMounted(async () => {
    const [ord, media] = await Promise.all([
      getAdOrderPage({ keyword: '', pageSize: 200, current: 1, statusList: [45, 50, 80] }),
      getAdDownstreamMediaPage({ keyword: '', pageSize: 200, current: 1 }),
    ]);
    orders.value = ord?.list || ord?.records || [];
    mediaOptions.value = media?.list || media?.records || [];
    await loadEdit();
  });
</script>
