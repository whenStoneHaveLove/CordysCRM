<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar :title="t('advertising.change.create')" left-arrow @click-left="back">
      <template #right>
        <span class="px-[4px] text-[14px] text-[#1989fa]" @click="onSave(false)">{{ t('advertising.form.save') }}</span>
        <span
          v-if="hasPermission('AD_ORDER_CHANGE:SUBMIT')"
          class="ml-[10px] px-[4px] text-[14px] text-[#1989fa]"
          @click="onSave(true)"
          >{{ t('advertising.order.detail.submit') }}</span
        >
      </template>
    </van-nav-bar>

    <div class="flex-1 overflow-auto px-[12px] py-[12px]">
      <!-- 关联订单 -->
      <van-field
        :label="t('advertising.change.form.orderId')"
        :model-value="orderLabel"
        readonly
        required
        is-link
        :placeholder="t('advertising.form.required')"
        @click="openOrderPicker"
      />
      <van-field
        :label="t('advertising.change.form.reason')"
        v-model="reason"
        type="textarea"
        rows="2"
        :placeholder="t('advertising.change.form.reason')"
      />

      <!-- 变更字段 -->
      <div class="mt-[12px] rounded-lg bg-white p-[12px]">
        <div class="mb-[8px] text-[14px] font-medium text-[var(--text-n1)]">
          {{ t('advertising.change.form.changeFields') }}
        </div>
        <van-checkbox-group v-model="selectedFields">
          <van-cell-group>
            <van-cell
              v-for="meta in fieldMetas"
              :key="meta.field"
              :title="meta.label"
              clickable
              @click="toggleField(meta.field)"
            >
              <template #right-icon>
                <van-checkbox :name="meta.field" @click.stop />
              </template>
            </van-cell>
          </van-cell-group>
        </van-checkbox-group>

        <div v-for="meta in selectedMetas" :key="meta.field" class="mt-[8px]">
          <van-field
            v-if="meta.type === 'text'"
            :label="meta.label"
            v-model="fieldValues[meta.field]"
            :placeholder="meta.label"
          />
          <van-field
            v-else-if="meta.type === 'number'"
            :label="meta.label"
            v-model="fieldValues[meta.field]"
            type="number"
            :placeholder="meta.label"
          />
          <van-field
            v-else-if="meta.type === 'date'"
            :label="meta.label"
            :model-value="fieldValues[meta.field] ? fmtDate(fieldValues[meta.field]) : ''"
            readonly
            @click="openDatePicker(meta.field)"
            :placeholder="meta.label"
          />
          <van-field
            v-else-if="meta.control === 'select-customer'"
            :label="meta.label"
            :model-value="labelOf(customerOptions, fieldValues[meta.field])"
            readonly
            is-link
            @click="openRelationalPicker(meta.field, customerOptions)"
          />
          <van-field
            v-else-if="meta.control === 'select-upstream'"
            :label="meta.label"
            :model-value="labelOf(upstreamOptions, fieldValues[meta.field])"
            readonly
            is-link
            @click="openRelationalPicker(meta.field, upstreamOptions)"
          />
          <van-field
            v-else-if="meta.control === 'select-industry'"
            :label="meta.label"
            :model-value="labelOf(industryOptions, fieldValues[meta.field])"
            readonly
            is-link
            @click="openRelationalPicker(meta.field, industryOptions)"
          />
          <van-field
            v-else-if="meta.control && meta.control.startsWith('enum-')"
            :label="meta.label"
            :model-value="enumLabel(meta.control, fieldValues[meta.field])"
            readonly
            is-link
            @click="openEnumPicker(meta.field, enumOptionsOf(meta.control))"
          />
          <van-field
            :label="t('advertising.change.form.before')"
            :model-value="formatOriginal(meta)"
            readonly
            class="change-before"
          />
        </div>
      </div>

      <!-- 下游客户付款返点明细变更 -->
      <van-cell class="mt-[12px] rounded-lg" :title="t('advertising.change.form.changeDownstream')" clickable>
        <template #right-icon>
          <van-switch v-model="changeDownstream" size="20px" />
        </template>
      </van-cell>

      <template v-if="changeDownstream">
        <van-field
          :label="t('advertising.field.mediaNames')"
          :model-value="downstreamLabels"
          readonly
          is-link
          :placeholder="t('advertising.form.required')"
          @click="openDownstreamPicker"
        />
        <div
          v-for="(row, idx) in downstreamRows"
          :key="row.downstreamMediaId"
          class="mb-[10px] rounded-lg bg-white p-[10px]"
        >
          <div class="mb-[6px] flex items-center justify-between">
            <span class="text-[13px] font-medium">{{ row.mediaName }}</span>
            <van-icon name="delete" class="text-[16px] text-[var(--text-n4)]" @click="removeDownstream(idx)" />
          </div>
          <!-- 改前（原值） -->
          <div class="mb-[8px] rounded bg-[var(--text-n8)] px-[10px] py-[6px]">
            <div class="mb-[2px] text-[12px] font-medium text-[var(--text-n3)]">
              {{ t('advertising.change.form.before') }}
            </div>
            <div class="flex flex-wrap gap-x-[12px] gap-y-[2px] text-[12px] text-[var(--text-n3)]">
              <span>{{ t('advertising.order.payableAmount') }} {{ fmtAmount(beforeOf(row).payableAmount) }}</span>
              <span>{{ t('advertising.order.noRebateAmount') }} {{ fmtAmount(beforeOf(row).noRebateAmount) }}</span>
              <span>{{ t('advertising.order.rebateMode') }} {{ enumLabel('enum-rebateMode', beforeOf(row).rebateMode) }}</span>
              <span>{{ t('advertising.order.rebateValue') }} {{ fmtAmount(beforeOf(row).rebateValue) }}</span>
              <span>{{ t('advertising.order.rebateAmount') }} {{ fmtAmount(beforeComputed(row).rebateAmount) }}</span>
              <span>{{ t('advertising.order.actualPayable') }} {{ fmtAmount(beforeComputed(row).actualPayable) }}</span>
              <span>{{ t('advertising.order.paymentMethod') }} {{ enumLabel('enum-paymentMethod', beforeOf(row).paymentMethod) }}</span>
              <template v-if="beforeOf(row).paymentMethod === 10">
                <span>{{ t('advertising.order.paymentPrepayMode') }} {{ enumLabel(AdModeOptions, beforeOf(row).paymentPrepayMode) }}</span>
                <span>{{ t('advertising.order.paymentPrepayRatio') }} {{ beforeOf(row).paymentPrepayRatio }}</span>
                <span>{{ t('advertising.order.paymentPrepayDeadline') }} {{ fmtDate(beforeOf(row).paymentPrepayDeadline) }}</span>
              </template>
              <template v-else-if="beforeOf(row).paymentMethod === 20">
                <span>{{ t('advertising.order.postpayTrigger') }} {{ enumLabel(AdPostpayTriggerOptions, beforeOf(row).paymentPostpayTrigger) }}</span>
                <span v-if="beforeOf(row).paymentPostpayTrigger === 20">{{ t('advertising.order.postpayDays') }} {{ beforeOf(row).paymentPostpayDays }}</span>
              </template>
            </div>
          </div>
          <van-field
            :label="t('advertising.order.payableAmount')"
            v-model="row.payableAmount"
            type="number"
            size="small"
          />
          <van-field
            :label="t('advertising.order.noRebateAmount')"
            v-model="row.noRebateAmount"
            type="number"
            size="small"
          />
          <van-field
            :label="t('advertising.order.rebateMode')"
            :model-value="enumLabel('enum-rebateMode', row.rebateMode)"
            readonly
            is-link
            size="small"
            @click="openRowEnumPicker(idx, 'rebateMode', AdModeOptions)"
          />
          <van-field
            :label="t('advertising.order.rebateValue')"
            v-model="row.rebateValue"
            type="number"
            size="small"
          />
          <div class="flex justify-between px-[16px] py-[4px] text-[12px] text-[var(--text-n3)]">
            <span>{{ t('advertising.order.rebateAmount') }}：{{ fmtAmount(downstreamComputed(row).rebateAmount) }}</span>
            <span>{{ t('advertising.order.actualPayable') }}：{{ fmtAmount(downstreamComputed(row).actualPayable) }}</span>
          </div>
          <van-field
            :label="t('advertising.order.paymentMethod')"
            :model-value="enumLabel('enum-paymentMethod', row.paymentMethod)"
            readonly
            is-link
            size="small"
            @click="openRowEnumPicker(idx, 'paymentMethod', AdPaymentMethodOptions)"
          />
          <template v-if="row.paymentMethod === 10">
            <van-field
              :label="t('advertising.order.paymentPrepayMode')"
              :model-value="enumLabel(AdModeOptions, row.paymentPrepayMode)"
              readonly
              is-link
              size="small"
              @click="openRowEnumPicker(idx, 'paymentPrepayMode', AdModeOptions)"
            />
            <van-field
              :label="t('advertising.order.paymentPrepayRatio')"
              v-model="row.paymentPrepayRatio"
              type="number"
              size="small"
            />
            <van-field
              :label="t('advertising.order.paymentPrepayDeadline')"
              :model-value="fmtDate(row.paymentPrepayDeadline)"
              readonly
              is-link
              size="small"
              @click="openRowDatePicker(idx, 'paymentPrepayDeadline')"
            />
          </template>
          <template v-else-if="row.paymentMethod === 20">
            <van-field
              :label="t('advertising.order.postpayTrigger')"
              :model-value="enumLabel(AdPostpayTriggerOptions, row.paymentPostpayTrigger)"
              readonly
              is-link
              size="small"
              @click="openRowEnumPicker(idx, 'paymentPostpayTrigger', AdPostpayTriggerOptions)"
            />
            <van-field
              v-if="row.paymentPostpayTrigger === 20"
              :label="t('advertising.order.postpayDays')"
              v-model="row.paymentPostpayDays"
              type="number"
              size="small"
            />
          </template>
        </div>
      </template>
    </div>

    <!-- 订单选择 -->
    <van-popup v-model:show="orderPickerShow" position="bottom" round :style="{ height: '70%' }">
      <div class="flex h-full flex-col">
        <van-search v-model="orderKeyword" :placeholder="t('advertising.searchPlaceholder')" />
        <van-radio-group v-model="draftOrderId" class="flex-1 overflow-auto">
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

    <!-- 下游客户多选 -->
    <van-popup v-model:show="downstreamPickerShow" position="bottom" round :style="{ height: '70%' }">
      <div class="flex h-full flex-col">
        <van-search v-model="downstreamKeyword" :placeholder="t('advertising.searchPlaceholder')" />
        <van-checkbox-group v-model="draftDownstreamIds" class="flex-1 overflow-auto p-[12px]">
          <van-cell
            v-for="m in filteredDownstream"
            :key="m.id"
            :title="m.name"
            clickable
            @click="toggleDownstream(m.id)"
          >
            <template #right-icon>
              <van-checkbox :name="m.id" @click.stop />
            </template>
          </van-cell>
        </van-checkbox-group>
        <van-button type="primary" block @click="confirmDownstream">{{ t('advertising.confirm') }}</van-button>
      </div>
    </van-popup>

    <!-- 通用枚举/关联选择 -->
    <van-popup v-model:show="relationalShow" position="bottom" round :style="{ height: '60%' }">
      <div class="flex h-full flex-col">
        <div class="p-[12px] text-[15px] font-semibold">{{ relationalTitle }}</div>
        <van-radio-group v-model="relationalValue" class="flex-1 overflow-auto">
          <van-cell
            v-for="opt in relationalOptions"
            :key="opt.value"
            :title="opt.label"
            clickable
            @click="pickRelational(opt)"
          >
            <template #right-icon>
              <van-radio :name="opt.value" />
            </template>
          </van-cell>
        </van-radio-group>
      </div>
    </van-popup>

    <!-- 日期选择 -->
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
  import { useRouter } from 'vue-router';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { showToast } from 'vant';

  import {
    AD_ORDER_CHANGE_FIELD_META,
    AdModeOptions,
    AdPaymentMethodOptions,
    AdPostpayTriggerOptions,
    AdReceiptMethodOptions,
  } from '@lib/shared/enums/advertisingEnum';
  import { AdvertisingRouteEnum } from '@/enums/routeEnum';
  import { hasPermission } from '@/utils/permission';
  import { fmtAmount, fmtDate } from '@/views/advertising/utils';

  import {
    createAdOrderChange,
    getAdCustomerPage,
    getAdDictPage,
    getAdDownstreamMediaPage,
    getAdOrderDetail,
    getAdOrderPage,
    getAdUpstreamAgentPage,
    submitAdOrderChange,
  } from '@/api/modules';

  interface Option {
    label: string;
    value: number | string;
  }
  interface DownstreamRow {
    downstreamMediaId: string;
    mediaName: string;
    payableAmount?: number | string;
    noRebateAmount?: number | string;
    rebateMode?: number;
    rebateValue?: number | string;
    paymentMethod?: number;
    paymentPrepayMode?: number;
    paymentPrepayRatio?: number | string;
    paymentPrepayDeadline?: number | string;
    paymentPostpayTrigger?: number;
    paymentPostpayDays?: number | string;
  }

  const { t } = useI18n();
  const router = useRouter();

  const fieldMetas = AD_ORDER_CHANGE_FIELD_META;
  const selectedFields = ref<string[]>([]);
  const fieldValues = ref<Record<string, any>>({});
  const reason = ref('');
  const orderId = ref('');
  const draftOrderId = ref('');
  const orderKeyword = ref('');
  const orders = ref<any[]>([]);
  const orderPickerShow = ref(false);

  const changeDownstream = ref(false);
  const downstreamRows = ref<DownstreamRow[]>([]);
  const draftDownstreamIds = ref<string[]>([]);
  const downstreamKeyword = ref('');
  const downstreamMedia = ref<any[]>([]);
  const downstreamPickerShow = ref(false);

  const customerOptions = ref<Option[]>([]);
  const upstreamOptions = ref<Option[]>([]);
  const industryOptions = ref<Option[]>([]);

  // 改单：进入时拉取并缓存原订单值（对齐 web order/create.vue 的 originalOrder / originalDownstream）
  const originalOrder = ref<Record<string, any>>({});
  const originalDownstream = ref<any[]>([]);

  const relationalShow = ref(false);
  const relationalOptions = ref<Option[]>([]);
  const relationalValue = ref<number | string>();
  const relationalField = ref('');
  const relationalRowIdx = ref(-1);
  const relationalTitle = ref('');

  const dateShow = ref(false);
  const dateField = ref('');
  /** 当天日期模型：日期选择器未选值时默认定位到今天 */
  function todayModel(): [number, number, number] {
    const d = new Date();
    return [d.getFullYear(), d.getMonth() + 1, d.getDate()];
  }
  /** 已存值 → 日期模型；无法解析时回退今天 */
  function toDateModel(v: any): [number, number, number] {
    const d = new Date(Number(v) || v);
    return Number.isNaN(d.getTime()) ? todayModel() : [d.getFullYear(), d.getMonth() + 1, d.getDate()];
  }
  const dateModel = ref<[number, number, number]>(todayModel());

  const selectedMetas = computed(() => fieldMetas.filter((m) => selectedFields.value.includes(m.field)));
  const orderLabel = computed(() => {
    const o = orders.value.find((it) => it.id === orderId.value);
    return o ? [o.orderName, o.orderNo].filter(Boolean).join(' ') : '';
  });
  const filteredOrders = computed(() => {
    const kw = orderKeyword.value.trim();
    if (!kw) return orders.value;
    return orders.value.filter((o) => `${o.orderName || ''}${o.orderNo || ''}`.includes(kw));
  });
  const filteredDownstream = computed(() => {
    const kw = downstreamKeyword.value.trim();
    if (!kw) return downstreamMedia.value;
    return downstreamMedia.value.filter((m) => (m.name || '').includes(kw));
  });
  const downstreamLabels = computed(() =>
    downstreamRows.value.map((r) => r.mediaName).filter(Boolean).join('、')
  );

  function enumOptionsOf(control: string): Option[] {
    if (control === 'enum-rebateMode') return AdModeOptions as Option[];
    if (control === 'enum-receiptMethod') return AdReceiptMethodOptions as Option[];
    if (control === 'enum-prepayMode') return AdModeOptions as Option[];
    if (control === 'enum-paymentMethod') return AdPaymentMethodOptions as Option[];
    if (control === 'enum-postpayTrigger') return AdPostpayTriggerOptions as Option[];
    return [];
  }
  // 兼容两种入参：字符串 control key（'enum-xxx'）或直接传选项数组
  function enumLabel(control: string | Option[], value: any): string {
    if (value === null || value === undefined || value === '') return '';
    const opts = Array.isArray(control) ? control : enumOptionsOf(control);
    const opt = opts.find((o) => o.value === value);
    return opt?.label || String(value);
  }
  function labelOf(options: Option[], value: any): string {
    if (value === null || value === undefined || value === '') return '';
    const opt = options.find((o) => o.value === value);
    return opt?.label || String(value);
  }

  function back() {
    router.back();
  }

  function openOrderPicker() {
    orderPickerShow.value = true;
  }
  async function pickOrder(o: any) {
    orderId.value = o.id;
    draftOrderId.value = o.id;
    orderPickerShow.value = false;
    // 带出原订单值（失败不阻断，仅影响改前展示）
    try {
      const detail: any = await getAdOrderDetail(o.id);
      originalOrder.value = detail?.order || {};
      originalDownstream.value = detail?.downstreamMediaPayables || [];
    } catch {
      originalOrder.value = {};
      originalDownstream.value = [];
    }
  }

  /** 改前原值展示（对齐 web formatOriginal） */
  function formatOriginal(meta: any): string {
    const v = originalOrder.value?.[meta.field];
    if (v === null || v === undefined || v === '') return '-';
    if (meta.control === 'select-customer') return labelOf(customerOptions.value, v);
    if (meta.control === 'select-upstream') return labelOf(upstreamOptions.value, v);
    if (meta.control === 'select-industry') return labelOf(industryOptions.value, v);
    if (meta.control && meta.control.startsWith('enum-')) return enumLabel(meta.control, v);
    if (meta.type === 'date') return fmtDate(v);
    return String(v);
  }

  /** 下游行返点金额/实际应付前端计算（对齐 web payableAutoCalc/actualPayable，仅展示不入库） */
  function downstreamComputed(row: DownstreamRow): { rebateAmount: number; actualPayable: number } {
    const base = Number(row.payableAmount || 0) - Number(row.noRebateAmount || 0);
    let rebate = 0;
    if (row.rebateMode === 10) {
      rebate = (base * Number(row.rebateValue || 0)) / 100;
    } else if (row.rebateMode === 20) {
      rebate = Number(row.rebateValue || 0);
    }
    rebate = Math.min(rebate, base);
    return { rebateAmount: rebate, actualPayable: base - rebate };
  }
  /** 某下游客户在「改前」原订单明细中的记录（用于展示原值） */
  function beforeOf(row: DownstreamRow): any {
    return originalDownstream.value.find((o) => o.downstreamMediaId === row.downstreamMediaId) || {};
  }
  /** 改前原值的计算项（返点金额 / 实际应付），复用下游计算逻辑 */
  function beforeComputed(row: DownstreamRow) {
    return downstreamComputed(beforeOf(row));
  }

  function openDownstreamPicker() {
    draftDownstreamIds.value = downstreamRows.value.map((r) => r.downstreamMediaId);
    downstreamPickerShow.value = true;
  }
  function toggleDownstream(id: string) {
    const i = draftDownstreamIds.value.indexOf(id);
    if (i >= 0) draftDownstreamIds.value.splice(i, 1);
    else draftDownstreamIds.value.push(id);
  }
  function toggleField(field: string) {
    const i = selectedFields.value.indexOf(field);
    if (i >= 0) selectedFields.value.splice(i, 1);
    else selectedFields.value.push(field);
  }
  function confirmDownstream() {
    const existing = downstreamRows.value;
    const next: DownstreamRow[] = [];
    draftDownstreamIds.value.forEach((id) => {
      const m = downstreamMedia.value.find((it) => it.id === id);
      const prev = existing.find((e) => e.downstreamMediaId === id);
      const orig = originalDownstream.value.find((o) => o.downstreamMediaId === id);
      next.push(
        prev || {
          downstreamMediaId: id,
          mediaName: m?.name || orig?.downstreamMediaName || '',
          payableAmount: orig?.payableAmount ?? '',
          noRebateAmount: orig?.noRebateAmount ?? '',
          rebateMode: orig?.rebateMode ?? 10,
          rebateValue: orig?.rebateValue ?? '',
          paymentMethod: orig?.paymentMethod ?? 10,
          paymentPrepayMode: orig?.paymentPrepayMode ?? 10,
          paymentPrepayRatio: orig?.paymentPrepayRatio ?? '',
        }
      );
    });
    downstreamRows.value = next;
    downstreamPickerShow.value = false;
  }
  function removeDownstream(idx: number) {
    downstreamRows.value.splice(idx, 1);
  }

  function openRelationalPicker(field: string, options: Option[]) {
    relationalField.value = field;
    relationalRowIdx.value = -1;
    relationalOptions.value = options;
    relationalValue.value = fieldValues.value[field];
    relationalTitle.value = fieldMetas.find((m) => m.field === field)?.label || '';
    relationalShow.value = true;
  }
  function openEnumPicker(field: string, options: Option[]) {
    relationalField.value = field;
    relationalRowIdx.value = -1;
    relationalOptions.value = options;
    relationalValue.value = fieldValues.value[field];
    relationalTitle.value = fieldMetas.find((m) => m.field === field)?.label || '';
    relationalShow.value = true;
  }
  function openRowEnumPicker(idx: number, field: string, options: Option[]) {
    relationalField.value = field;
    relationalRowIdx.value = idx;
    relationalOptions.value = options;
    relationalValue.value = (downstreamRows.value[idx] as any)[field];
    relationalTitle.value = '';
    relationalShow.value = true;
  }
  function pickRelational(opt: Option) {
    relationalValue.value = opt.value;
    applyRelational();
  }
  function applyRelational() {
    if (relationalRowIdx.value >= 0) {
      (downstreamRows.value[relationalRowIdx.value] as any)[relationalField.value] = relationalValue.value;
    } else {
      fieldValues.value[relationalField.value] = relationalValue.value;
    }
    relationalShow.value = false;
  }

  function openDatePicker(field: string) {
    dateField.value = field;
    // 字段选择器：重置行上下文，避免误写进下游明细
    rowDateIdx.value = -1;
    const v = fieldValues.value[field];
    dateModel.value = v ? toDateModel(v) : todayModel();
    dateShow.value = true;
  }
  function pickDate(val: { selectedValues: number[] }) {
    const [y, m, d] = val.selectedValues;
    const ts = new Date(y, m - 1, d).getTime();
    if (rowDateIdx.value >= 0) {
      (downstreamRows.value[rowDateIdx.value] as any)[rowDateField.value] = ts;
      rowDateIdx.value = -1;
      dateShow.value = false;
      return;
    }
    fieldValues.value[dateField.value] = ts;
    dateShow.value = false;
  }

  const rowDateIdx = ref(-1);
  const rowDateField = ref('');
  function openRowDatePicker(idx: number, field: string) {
    rowDateIdx.value = idx;
    rowDateField.value = field;
    const v = (downstreamRows.value[idx] as any)?.[field];
    dateModel.value = v ? toDateModel(v) : todayModel();
    dateShow.value = true;
  }

  function normalizePaymentFields(row: DownstreamRow) {
    if (row.paymentMethod === 20) {
      row.paymentPrepayMode = undefined;
      row.paymentPrepayRatio = '';
      row.paymentPrepayDeadline = undefined;
    } else if (row.paymentMethod === 10) {
      // 预付：后付相关不填
      row.paymentPostpayTrigger = undefined;
      row.paymentPostpayDays = '';
    }
    if (row.paymentPostpayTrigger !== 20) {
      row.paymentPostpayDays = '';
    }
  }

  async function onSave(submit: boolean) {
    if (!orderId.value) {
      showToast(t('advertising.change.form.orderId') + t('advertising.form.required'));
      return;
    }
    const after: Record<string, any> = {};
    const changeFields: string[] = [];
    selectedFields.value.forEach((f) => {
      const mv = fieldValues.value[f];
      if (mv === '' || mv === null || mv === undefined) return;
      after[f] = mv;
      changeFields.push(f);
    });
    // 下游客户付款返点明细（整表覆盖式变更）：两个特殊字段须随 changeFields 一起提交
    // （后端 SPECIAL_FIELDS 白名单），否则只开下游变更时清单为空会被拒（对齐 web buildPayload）
    if (changeDownstream.value && downstreamRows.value.length) {
      downstreamRows.value.forEach(normalizePaymentFields);
      after.downstreamMediaIds = downstreamRows.value.map((r) => r.downstreamMediaId);
      after.downstreamMediaPayables = downstreamRows.value.map((r) => ({
        downstreamMediaId: r.downstreamMediaId,
        payableAmount: Number(r.payableAmount) || 0,
        noRebateAmount: Number(r.noRebateAmount) || 0,
        rebateMode: r.rebateMode ?? 10,
        rebateValue: Number(r.rebateValue) || 0,
        paymentMethod: r.paymentMethod ?? 10,
        paymentPrepayMode: r.paymentMethod === 10 ? r.paymentPrepayMode : undefined,
        paymentPrepayRatio: r.paymentMethod === 10 ? Number(r.paymentPrepayRatio) || 0 : undefined,
        paymentPrepayDeadline:
          r.paymentMethod === 10 ? (r.paymentPrepayDeadline ? Number(r.paymentPrepayDeadline) : undefined) : undefined,
        paymentPostpayTrigger: r.paymentMethod === 20 ? r.paymentPostpayTrigger : undefined,
        paymentPostpayDays:
          r.paymentMethod === 20 && r.paymentPostpayTrigger === 20
            ? Number(r.paymentPostpayDays) || undefined
            : undefined,
      }));
      changeFields.push('downstreamMediaIds', 'downstreamMediaPayables');
    }
    // 标量字段全空 + 未变更下游明细 → 清单为空，后端会拒绝
    if (!changeFields.length) {
      showToast(t('advertising.change.form.changeFields') + t('advertising.form.required'));
      return;
    }
    const payload = { orderId: orderId.value, reason: reason.value, changeFields, after };
    const created = await createAdOrderChange(payload);
    if (submit) await submitAdOrderChange(created?.id || created);
    showToast(t('advertising.common.operateSuccess'));
    router.back();
  }

  onMounted(() => {
    loadSelectOptions();
  });

  // 各下拉选项独立加载：单个接口失败只影响自己，不会像 Promise.all 那样一处报错整组清空
  function loadSelectOptions() {
    getAdOrderPage({ keyword: '', pageSize: 200, current: 1, statusList: [45, 50, 80] })
      .then((ord: any) => (orders.value = ord?.list || ord?.records || []))
      .catch(() => (orders.value = []));
    getAdCustomerPage({ keyword: '', pageSize: 200, current: 1 })
      .then((cust: any) =>
        (customerOptions.value = (cust?.list || cust?.records || []).map((i: any) => ({
          label: i.customerName || i.name || i.id,
          value: i.id,
        })))
      )
      .catch(() => (customerOptions.value = []));
    getAdUpstreamAgentPage({ keyword: '', pageSize: 200, current: 1, status: 10 })
      .then((up: any) =>
        (upstreamOptions.value = (up?.list || up?.records || []).map((i: any) => ({
          label: i.resourceName || i.name || i.id,
          value: i.id,
        })))
      )
      .catch(() => (upstreamOptions.value = []));
    getAdDictPage({ keyword: '', pageSize: 200, current: 1, dictCode: 'industry' })
      .then((ind: any) =>
        (industryOptions.value = (ind?.list || ind?.records || []).map((i: any) => ({
          label: i.dictLabel || i.dictValue,
          value: i.dictValue,
        })))
      )
      .catch(() => (industryOptions.value = []));
    getAdDownstreamMediaPage({ keyword: '', pageSize: 200, current: 1 })
      .then((down: any) => (downstreamMedia.value = down?.list || down?.records || []))
      .catch(() => (downstreamMedia.value = []));
  }
</script>
