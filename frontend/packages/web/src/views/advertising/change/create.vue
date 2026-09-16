<template>
  <div class="advertising-page">
    <n-card :bordered="false" :title="t('advertising.change.new')">
      <n-spin :show="loading">
        <n-form label-placement="left" label-width="120px">
          <n-form-item :label="t('advertising.change.form.orderId')" required>
            <n-select
              v-model:value="orderId"
              :options="orderOptions"
              :loading="orderLoading"
              placeholder=""
              filterable
              style="width: 320px"
              @update:value="onOrderChange"
            />
          </n-form-item>
          <n-form-item :label="t('advertising.change.form.reason')">
            <n-input v-model:value="reason" type="textarea" :rows="2" style="max-width: 520px" />
          </n-form-item>
          <n-form-item :label="t('advertising.change.form.changeFields')">
            <n-checkbox-group v-model:value="selectedFields">
              <n-space>
                <n-checkbox v-for="meta in fieldMetas" :key="meta.field" :value="meta.field">{{
                  meta.label
                }}</n-checkbox>
              </n-space>
            </n-checkbox-group>
          </n-form-item>

          <n-form-item label="付款返点明细（下游客户）">
            <n-space vertical>
              <n-checkbox v-model:checked="changeDownstream">变更下游客户明细</n-checkbox>
              <template v-if="changeDownstream">
                <n-select
                  v-model:value="downstreamMediaIds"
                  :options="downstreamMediaOptions"
                  multiple
                  filterable
                  placeholder="请选择下游客户"
                  style="width: 480px"
                  @update:value="onDownstreamMediaChange"
                />
                <n-table
                  v-if="downstreamMediaPayables.length"
                  :bordered="true"
                  size="small"
                  :single-line="false"
                  class="dm-table"
                >
                  <thead>
                    <tr>
                      <th style="width: 140px">下游客户</th>
                      <th style="width: 120px">应付金额</th>
                      <th style="width: 110px">不记返金额</th>
                      <th style="width: 110px">返点方式</th>
                      <th style="width: 110px">返点值</th>
                      <th style="width: 120px">实际应付</th>
                      <th style="width: 120px">付款方式</th>
                      <th style="width: 120px">预付模式</th>
                      <th style="width: 110px">预付比例%</th>
                      <th style="width: 150px">预付截止日</th>
                      <th style="width: 120px">后付触发</th>
                      <th style="width: 110px">后付天数</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in downstreamMediaPayables" :key="item.downstreamMediaId">
                      <td>{{ downstreamMediaName(item.downstreamMediaId) }}</td>
                      <td>
                        <n-input-number
                          v-model:value="item.payableAmount"
                          :min="0"
                          :precision="2"
                          :show-button="false"
                          style="width: 100%"
                        />
                      </td>
                      <td>
                        <n-input-number
                          v-model:value="item.noRebateAmount"
                          :min="0"
                          :precision="2"
                          :show-button="false"
                          style="width: 100%"
                        />
                      </td>
                      <td>
                        <n-select v-model:value="item.rebateMode" :options="AdModeOptions" style="width: 100%" />
                      </td>
                      <td>
                        <n-input-number
                          v-model:value="item.rebateValue"
                          :min="0"
                          :precision="2"
                          :show-button="false"
                          style="width: 100%"
                        />
                      </td>
                      <td>{{ actualPayable(item) }}</td>
                      <td>
                        <n-select
                          v-model:value="item.paymentMethod"
                          :options="AdPaymentMethodOptions"
                          style="width: 100%"
                          @update:value="(v: number) => onPaymentMethodChange(item, v)"
                        />
                      </td>
                      <td>
                        <n-select
                          v-model:value="item.paymentPrepayMode"
                          :options="AdModeOptions"
                          :disabled="!isPrepayEditable(item)"
                          style="width: 100%"
                        />
                      </td>
                      <td>
                        <n-input-number
                          v-model:value="item.paymentPrepayRatio"
                          :min="0"
                          :precision="2"
                          :show-button="false"
                          :disabled="!isPrepayEditable(item)"
                          style="width: 100%"
                        />
                      </td>
                      <td>
                        <n-date-picker
                          v-model:value="item.paymentPrepayDeadline"
                          type="date"
                          clearable
                          :disabled="!isPrepayEditable(item)"
                          style="width: 100%"
                        />
                      </td>
                      <td>
                        <n-select
                          v-model:value="item.paymentPostpayTrigger"
                          :options="AdPostpayTriggerOptions"
                          :disabled="!isPostpayEditable(item)"
                          style="width: 100%"
                          @update:value="(v: number) => onPostpayTriggerChange(item, v)"
                        />
                      </td>
                      <td>
                        <n-input-number
                          v-model:value="item.paymentPostpayDays"
                          :min="0"
                          :precision="0"
                          :show-button="false"
                          :disabled="!isPostpayDaysEditable(item)"
                          style="width: 100%"
                        />
                      </td>
                    </tr>
                  </tbody>
                </n-table>
                <div v-if="downstreamMediaPayables.length" class="payable-total">
                  合计应付金额：{{ payableTotal }}
                </div>
              </template>
            </n-space>
          </n-form-item>

          <template v-if="selectedMetas.length">
            <n-divider title-placement="left"
              >{{ t('advertising.change.detail.snapshotBefore') }} →
              {{ t('advertising.change.detail.snapshotAfter') }}</n-divider
            >
            <n-table :bordered="true" size="small" :single-line="false">
              <thead>
                <tr>
                  <th style="width: 160px">变更字段</th>
                  <th>原值（改前）</th>
                  <th>新值（改后）</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="meta in selectedMetas" :key="meta.field">
                  <td>{{ meta.label }}</td>
                  <td class="before-value">{{ formatOriginal(meta) }}</td>
                  <td>
                    <n-select
                      v-if="meta.control"
                      v-model:value="fieldValues[meta.field]"
                      :options="getOptions(meta.control)"
                      filterable
                      clearable
                      placeholder="请选择"
                    />
                    <n-input-number
                      v-else-if="meta.type === 'number'"
                      v-model:value="fieldValues[meta.field]"
                      :min="0"
                      :precision="2"
                      :show-button="false"
                      style="width: 100%"
                    />
                    <n-date-picker
                      v-else-if="meta.type === 'date'"
                      v-model:value="fieldValues[meta.field]"
                      type="date"
                      clearable
                      style="width: 100%"
                    />
                    <n-input v-else v-model:value="fieldValues[meta.field]" />
                  </td>
                </tr>
              </tbody>
            </n-table>
          </template>
        </n-form>
      </n-spin>
      <template #footer>
        <n-space justify="end">
          <n-button @click="goBack">{{ t('advertising.order.form.cancel') }}</n-button>
          <n-button
            v-permission="['AD_ORDER_CHANGE:CREATE']"
            type="primary"
            :loading="saving"
            @click="handleSave(false)"
            >{{ t('advertising.order.form.save') }}</n-button
          >
          <n-button
            v-if="hasPermission('AD_ORDER_CHANGE:CREATE') && hasPermission('AD_ORDER_CHANGE:SUBMIT')"
            type="primary"
            :loading="saving"
            @click="handleSave(true)"
            >{{ t('advertising.change.submit') }}</n-button
          >
        </n-space>
      </template>
    </n-card>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NCheckbox,
    NCheckboxGroup,
    NDatePicker,
    NDivider,
    NForm,
    NFormItem,
    NInput,
    NInputNumber,
    NSelect,
    NSpace,
    NSpin,
    NTable,
    useMessage,
  } from 'naive-ui';

  import {
    AD_ORDER_CHANGE_FIELD_META,
    AdModeOptions,
    AdOrderTypeOptions,
    AdPaymentMethodOptions,
    AdPostpayTriggerOptions,
    AdReceiptMethodOptions,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderChangeSaveParams, AdOrderInfo, AdOrderListItem } from '@lib/shared/models/advertising';

  import {
    createAdOrderChange,
    getAdBusinessEntityPage,
    getAdCustomerPage,
    getAdDictPage,
    getAdDownstreamMediaPage,
    getAdOrderDetail,
    getAdOrderPage,
    getAdUpstreamAgentPage,
    submitAdOrderChange,
  } from '@/api/modules';
  import { hasPermission } from '@/utils/permission';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { AD_SELECT_PAGE_PARAMS } from '../utils';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const fieldMetas = AD_ORDER_CHANGE_FIELD_META;
  const selectedFields = ref<string[]>([]);
  const fieldValues = reactive<Record<string, any>>({});

  const selectedMetas = computed(() => fieldMetas.filter((m) => selectedFields.value.includes(m.field)));

  const orderId = ref<string | null>(null);
  const orderOptions = ref<{ label: string; value: string }[]>([]);
  const orderLoading = ref(false);
  const reason = ref('');
  const loading = ref(false);
  const saving = ref(false);

  // 选中订单的原值（用于左右对比展示）
  const originalOrder = ref<AdOrderInfo | null>(null);

  // 下游客户付款返点明细（改单整体重填）
  const changeDownstream = ref(false);
  const downstreamMediaIds = ref<string[]>([]);
  const downstreamMediaPayables = ref<any[]>([]);
  const downstreamMediaOptions = ref<SelectItem[]>([]);
  // 原订单的下游客户明细，勾选"变更下游客户明细"时作为默认值回填
  const originalDownstream = ref<{ ids: string[]; payables: any[] }>({ ids: [], payables: [] });

  // 下拉数据源（与建单页一致）
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

  /**
   * 付款字段互斥收口：预付(10) 与后付(20) 两组字段不同时存在。
   * 明细按整表覆盖提交，残留值会原样落库；原订单历史数据也可能带出
   * 「后付 + 预付比例/截止日」这类脏组合，统一在此清理。
   */
  function normalizePaymentFields(item: any) {
    if (item.paymentMethod === 20) {
      item.paymentPrepayMode = null;
      item.paymentPrepayRatio = null;
      item.paymentPrepayDeadline = null;
    } else if (item.paymentMethod === 10) {
      item.paymentPostpayTrigger = null;
      item.paymentPostpayDays = null;
    }
    // 后付天数只在「执行完成X天」有意义
    if (item.paymentPostpayTrigger !== 20) {
      item.paymentPostpayDays = null;
    }
  }

  async function onOrderChange(val: string | null) {
    originalOrder.value = null;
    changeDownstream.value = false;
    downstreamMediaIds.value = [];
    downstreamMediaPayables.value = [];
    // 必须先清空原值缓存：详情接口失败时不会覆盖它，否则勾选明细会沿用上一个订单的数据
    originalDownstream.value = { ids: [], payables: [] };
    if (!val) return;
    try {
      const detail = await getAdOrderDetail(val);
      const o = (detail.order ?? null) as any;
      originalOrder.value = o;
      // 下游客户明细在详情响应顶层（AdOrderDetailResponse），不在 order 实体里
      const ids = (detail.downstreamMediaIds || []).map(String);
      const payables = (detail.downstreamMediaPayables || []).map((p: any) => ({
        downstreamMediaId: String(p.downstreamMediaId),
        payableAmount: p.payableAmount,
        noRebateAmount: p.noRebateAmount,
        rebateMode: p.rebateMode,
        rebateValue: p.rebateValue,
        actualPayableAmount: p.actualPayable ?? p.actualPayableAmount,
        paymentMethod: p.paymentMethod,
        paymentPrepayMode: p.paymentPrepayMode,
        paymentPrepayRatio: p.paymentPrepayRatio,
        paymentPrepayDeadline: p.paymentPrepayDeadline,
        paymentPostpayTrigger: p.paymentPostpayTrigger,
        paymentPostpayDays: p.paymentPostpayDays,
      }));
      // 原值仅缓存，供「变更下游客户明细」勾选时回填、以及"原值"列对比展示，
      // 不自动勾选、不自动填充编辑区（是否变更下游明细由用户显式决定）
      originalDownstream.value = { ids, payables };
    } catch (e) {
      // 拿不到原值不阻塞，仅影响"原值"列展示
    }
  }

  function formatOriginal(meta: { field: string; type: string; control?: string }): string {
    const o = originalOrder.value as any;
    if (!o) return '-';
    const v = o[meta.field];
    if (v === null || v === undefined || v === '') return '-';
    if (meta.type === 'date') {
      return String(v).slice(0, 10);
    }
    // 下拉/枚举字段显示名称，而不是 id/字典值
    if (meta.control) {
      return labelOf(meta.control, v);
    }
    return String(v);
  }

  async function loadSelectOptions() {
    try {
      const [cuRes, uaRes, dictRes, beRes, dmRes] = await Promise.all([
        getAdCustomerPage({ ...AD_SELECT_PAGE_PARAMS }),
        getAdUpstreamAgentPage({ ...AD_SELECT_PAGE_PARAMS, status: 10 }),
        getAdDictPage({ ...AD_SELECT_PAGE_PARAMS, dictCode: 'industry' }),
        getAdBusinessEntityPage({ ...AD_SELECT_PAGE_PARAMS }),
        getAdDownstreamMediaPage({ ...AD_SELECT_PAGE_PARAMS }),
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
      downstreamMediaOptions.value = (dmRes.list || []).map((it: any) => ({
        label: it.resourceName || it.name || it.id,
        value: String(it.id),
      }));
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  // 下游客户明细：勾选"变更"时以原订单当前明细作为默认值回填
  watch(changeDownstream, (v) => {
    if (v) {
      downstreamMediaIds.value = [...originalDownstream.value.ids];
      // 回填原订单值后做一次付款字段互斥清理：库里历史数据可能带出
      // 「后付 + 预付比例/截止日」等脏组合，直接铺到界面会与禁用态自相矛盾
      downstreamMediaPayables.value = originalDownstream.value.payables.map((p) => {
        const item = { ...p };
        normalizePaymentFields(item);
        return item;
      });
    } else {
      downstreamMediaIds.value = [];
      downstreamMediaPayables.value = [];
    }
  });

  function downstreamMediaName(id: string): string {
    const f = downstreamMediaOptions.value.find((o) => String(o.value) === String(id));
    return f ? String(f.label) : id || '-';
  }

  // 实际应付 = (应付金额 - 不记返金额) - 返点（比例/固定金额）
  function actualPayable(item: any): number {
    if (!item) return 0;
    const base = Number(item.payableAmount || 0) - Number(item.noRebateAmount || 0);
    if (item.rebateMode === 10) {
      return base - base * (Number(item.rebateValue || 0) / 100);
    }
    if (item.rebateMode === 20) {
      return base - Number(item.rebateValue || 0);
    }
    return base;
  }

  const payableTotal = computed(() =>
    downstreamMediaPayables.value.reduce((s, it) => s + Number(actualPayable(it) || 0), 0).toFixed(2)
  );

  // 多选变化：同步明细数组（删除未选、新增选中并按原值/默认值初始化）
  function onDownstreamMediaChange(val: string[]) {
    const items = downstreamMediaPayables.value;
    for (let i = items.length - 1; i >= 0; i--) {
      if (!val.includes(items[i].downstreamMediaId)) items.splice(i, 1);
    }
    val.forEach((id) => {
      if (!items.find((it) => it.downstreamMediaId === id)) {
        const orig = originalDownstream.value.payables.find((p) => String(p.downstreamMediaId) === String(id));
        const base = orig
          ? { ...orig }
          : {
              downstreamMediaId: id,
              payableAmount: null,
              noRebateAmount: null,
              rebateMode: 10,
              rebateValue: null,
              actualPayableAmount: 0,
              paymentMethod: null,
              paymentPrepayMode: null,
              paymentPrepayRatio: null,
              paymentPrepayDeadline: null,
              paymentPostpayTrigger: null,
              paymentPostpayDays: null,
            };
        // 原订单回填时同样收口付款字段，避免带出后付/预付并存的脏组合
        normalizePaymentFields(base);
        items.push(base);
      }
    });
  }

  /** 付款方式联动：切到后付(20)清空预付三字段；切到预付(10)清空后付两字段。 */
  function onPaymentMethodChange(item: any, value: number | null) {
    item.paymentMethod = value;
    normalizePaymentFields(item);
  }

  /** 预付字段：仅预付(10)可写 */
  function isPrepayEditable(item: any): boolean {
    return item.paymentMethod === 10;
  }

  /** 后付字段：仅后付(20)可写 */
  function isPostpayEditable(item: any): boolean {
    return item.paymentMethod === 20;
  }

  /** 后付天数：仅「执行完成X天」(20) 可写；「收到上游全款」(10) 无天数概念 */
  function isPostpayDaysEditable(item: any): boolean {
    return item.paymentMethod === 20 && item.paymentPostpayTrigger === 20;
  }

  /** 后付触发联动：切到非「执行完成X天」时清空后付天数，避免残留值落库 */
  function onPostpayTriggerChange(item: any, value: number | null) {
    if (value !== 20) {
      item.paymentPostpayDays = null;
    }
  }

  function buildPayload(): AdOrderChangeSaveParams | null {
    if (!orderId.value) {
      message.warning('请选择关联订单');
      return null;
    }
    const after: Record<string, any> = {};
    const changeFields: string[] = [];
    selectedFields.value.forEach((field) => {
      const meta = fieldMetas.find((m) => m.field === field);
      let val = fieldValues[field];
      let shouldSkip = false;
      if (meta?.type === 'number') {
        if (val === null || val === undefined || val === '') {
          shouldSkip = true;
        } else {
          val = Number(val);
        }
      } else if (meta?.type === 'date') {
        if (val == null) {
          shouldSkip = true;
        }
      } else if (val === null || val === undefined || val === '') {
        shouldSkip = true;
      }
      if (!shouldSkip) {
        after[field] = val;
        changeFields.push(field);
      }
    });
    // 下游客户付款返点明细（整表覆盖式变更）
    if (changeDownstream.value && downstreamMediaIds.value.length) {
      after.downstreamMediaIds = downstreamMediaIds.value.map(String);
      after.downstreamMediaPayables = downstreamMediaPayables.value.map((it) => ({
        downstreamMediaId: String(it.downstreamMediaId),
        payableAmount: it.payableAmount == null ? null : Number(it.payableAmount),
        noRebateAmount: it.noRebateAmount == null ? null : Number(it.noRebateAmount),
        rebateMode: it.rebateMode,
        rebateValue: it.rebateValue == null ? null : Number(it.rebateValue),
        paymentMethod: it.paymentMethod,
        paymentPrepayMode: it.paymentPrepayMode,
        paymentPrepayRatio: it.paymentPrepayRatio == null ? null : Number(it.paymentPrepayRatio),
        paymentPrepayDeadline: it.paymentPrepayDeadline == null ? null : Number(it.paymentPrepayDeadline),
        paymentPostpayTrigger: it.paymentPostpayTrigger,
        paymentPostpayDays: it.paymentPostpayDays == null ? null : Number(it.paymentPostpayDays),
      }));
      changeFields.push('downstreamMediaIds');
      changeFields.push('downstreamMediaPayables');
    }
    if (changeFields.length === 0) {
      message.warning('请至少填写一个变更字段');
      return null;
    }
    return { orderId: orderId.value, reason: reason.value, changeFields, after };
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CHANGE });
  }

  async function handleSave(submit: boolean) {
    const payload = buildPayload();
    if (!payload) return;
    saving.value = true;
    try {
      const created = await createAdOrderChange(payload);
      if (submit) {
        await submitAdOrderChange(created.id);
        message.success(t('advertising.common.submitSuccess'));
      } else {
        message.success(t('advertising.common.saveSuccess'));
      }
      goBack();
    } catch (e) {
      message.error((e as Error).message || '保存失败');
    } finally {
      saving.value = false;
    }
  }

  async function loadOrders() {
    orderLoading.value = true;
    try {
      const res = await getAdOrderPage({ ...AD_SELECT_PAGE_PARAMS });
      // 可提交改单的订单：仅 待执行(45)/执行中(50)/结算中(80)；排除 草稿(0)/审批中(10)/改单审核中(60)/已归档(90)/已作废(100)
      const excluded = [0, 10, 60, 90, 100];
      orderOptions.value = (res.list || [])
        .filter((o: AdOrderListItem) => !excluded.includes(o.status ?? 0))
        .map((o: AdOrderListItem) => ({
          label: `${o.orderNo || ''}（${o.orderName || ''}）`,
          value: o.id,
        }));
    } catch (e) {
      message.error((e as Error).message || '加载订单失败');
    } finally {
      orderLoading.value = false;
    }
  }

  onMounted(() => {
    loadOrders();
    loadSelectOptions();
  });
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .before-value {
    color: #999999;
    word-break: break-all;
  }
  .dm-table {
    min-width: 1400px;
  }
  .payable-total {
    margin-top: 8px;
    font-weight: 600;
  }
</style>
