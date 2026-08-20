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
          <n-button type="primary" :loading="saving" @click="handleSave(false)">{{
            t('advertising.order.form.save')
          }}</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave(true)">{{
            t('advertising.change.submit')
          }}</n-button>
        </n-space>
      </template>
    </n-card>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue';
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
    getAdOrderDetail,
    getAdOrderPage,
    getAdUpstreamAgentPage,
    submitAdOrderChange,
  } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

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

  async function onOrderChange(val: string | null) {
    originalOrder.value = null;
    if (!val) return;
    try {
      const detail = await getAdOrderDetail(val);
      originalOrder.value = detail.order ?? null;
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
      const res = await getAdOrderPage({ current: 1, pageSize: 200 });
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
    color: #999;
    word-break: break-all;
  }
</style>
