<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{ isEdit ? t('advertising.order.form.title.edit') : t('advertising.order.form.title.create') }}
          </div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.order.form.cancel') }}</n-button>
            <n-button type="primary" :loading="saving" @click="handleSave">{{
              t('advertising.order.form.save')
            }}</n-button>
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-form ref="formRef" :model="form" label-placement="left" :label-width="120">
            <n-grid :cols="2" :x-gap="16" item-responsive>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.orderName')" path="orderName">
                <n-input v-model:value="form.orderName" placeholder="请输入订单名称" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.businessEntityId')" path="businessEntityId">
                <n-input v-model:value="form.businessEntityId" placeholder="业务主体ID（必填）" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.customerId')" path="customerId">
                <n-input v-model:value="form.customerId" placeholder="客户ID（必填）" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.industryCode')">
                <n-input v-model:value="form.industryCode" placeholder="行业类别(字典)" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.signingEntity')">
                <n-input v-model:value="form.signingEntity" placeholder="签约主体" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.orderType')" path="orderType">
                <n-select v-model:value="form.orderType" :options="orderTypeOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.upstreamAgentId')">
                <n-input v-model:value="form.upstreamAgentId" placeholder="上游代理" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.agentOrderNo')">
                <n-input v-model:value="form.agentOrderNo" placeholder="代理订单号" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.totalAmount')" path="totalAmount">
                <n-input-number v-model:value="form.totalAmount" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.rebateMode')">
                <n-select v-model:value="form.rebateMode" :options="rebateModeOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.rebateValue')">
                <n-input-number v-model:value="form.rebateValue" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.noRebateAmount')">
                <n-input-number v-model:value="form.noRebateAmount" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.mediaPayableAmount')">
                <n-input-number v-model:value="form.mediaPayableAmount" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.deliveryStart')">
                <n-date-picker v-model:value="form.deliveryStartDate" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.deliveryEnd')">
                <n-date-picker v-model:value="form.deliveryEndDate" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.deliveryVolume')">
                <n-input v-model:value="form.deliveryVolume" placeholder="投放量+单位" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.receiptMethod')" path="receiptMethod">
                <n-select v-model:value="form.receiptMethod" :options="receiptMethodOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.receiptPrepayMode')">
                <n-select v-model:value="form.receiptPrepayMode" :options="prepayModeOptions" placeholder="预收模式" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.receiptPrepayRatio')">
                <n-input-number v-model:value="form.receiptPrepayRatio" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.receiptPrepayDeadline')">
                <n-date-picker v-model:value="form.receiptPrepayDeadline" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.receiptAccountPeriodDays')">
                <n-input-number v-model:value="form.receiptAccountPeriodDays" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentMethod')" path="paymentMethod">
                <n-select v-model:value="form.paymentMethod" :options="paymentMethodOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPrepayMode')">
                <n-select
                  v-model:value="form.paymentPrepayMode"
                  :options="prepayModeOptions"
                  placeholder="媒体预付模式"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPrepayRatio')">
                <n-input-number v-model:value="form.paymentPrepayRatio" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPrepayDeadline')">
                <n-date-picker v-model:value="form.paymentPrepayDeadline" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPostpayTrigger')">
                <n-select
                  v-model:value="form.paymentPostpayTrigger"
                  :options="postpayTriggerOptions"
                  placeholder="后付触发"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPostpayDays')">
                <n-input-number v-model:value="form.paymentPostpayDays" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.currency')">
                <n-input v-model:value="form.currency" placeholder="币种，默认 CNY" />
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.order.form.remark')">
                <n-input v-model:value="form.remark" type="textarea" placeholder="备注" />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </div>
      </div>
    </CrmCard>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, NDatePicker, NForm, NFormItemGi, NGrid, NInput, NInputNumber, NSelect, useMessage } from 'naive-ui';

  import {
    AdModeOptions,
    AdOrderTypeOptions,
    AdPaymentMethodOptions,
    AdPostpayTriggerOptions,
    AdReceiptMethodOptions,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderSaveParams } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import { createAdOrder, getAdOrderDetail, updateAdOrder } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const orderTypeOptions = AdOrderTypeOptions;
  const receiptMethodOptions = AdReceiptMethodOptions;
  const paymentMethodOptions = AdPaymentMethodOptions;
  const rebateModeOptions = AdModeOptions;
  const prepayModeOptions = AdModeOptions;
  const postpayTriggerOptions = AdPostpayTriggerOptions;

  /** 表单本地类型：数值/日期使用 number | null 以适配 Naive UI 控件 */
  interface AdOrderForm {
    orderName?: string;
    businessEntityId?: string;
    customerId?: string;
    industryCode?: string;
    signingEntity?: string;
    orderType?: number | null;
    upstreamAgentId?: string;
    agentOrderNo?: string;
    totalAmount?: number | null;
    rebateMode?: number | null;
    rebateValue?: number | null;
    noRebateAmount?: number | null;
    mediaPayableAmount?: number | null;
    deliveryStartDate?: number | null;
    deliveryEndDate?: number | null;
    deliveryVolume?: string;
    remark?: string;
    receiptMethod?: number | null;
    receiptPrepayMode?: number | null;
    receiptPrepayRatio?: number | null;
    receiptPrepayDeadline?: number | null;
    receiptAccountPeriodDays?: number | null;
    paymentMethod?: number | null;
    paymentPrepayMode?: number | null;
    paymentPrepayRatio?: number | null;
    paymentPrepayDeadline?: number | null;
    paymentPostpayTrigger?: number | null;
    paymentPostpayDays?: number | null;
    currency?: string;
  }

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const id = (route.params.id as string) || '';
  const isEdit = computed(() => !!id);
  const saving = ref(false);

  const form = reactive<AdOrderForm>({
    orderName: undefined,
    businessEntityId: undefined,
    customerId: undefined,
    industryCode: undefined,
    signingEntity: undefined,
    orderType: null,
    upstreamAgentId: undefined,
    agentOrderNo: undefined,
    totalAmount: null,
    rebateMode: null,
    rebateValue: null,
    noRebateAmount: null,
    mediaPayableAmount: null,
    deliveryStartDate: null,
    deliveryEndDate: null,
    deliveryVolume: undefined,
    remark: undefined,
    receiptMethod: null,
    receiptPrepayMode: null,
    receiptPrepayRatio: null,
    receiptPrepayDeadline: null,
    receiptAccountPeriodDays: null,
    paymentMethod: null,
    paymentPrepayMode: null,
    paymentPrepayRatio: null,
    paymentPrepayDeadline: null,
    paymentPostpayTrigger: null,
    paymentPostpayDays: null,
    currency: 'CNY',
  });

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_ORDER });
  }

  function buildPayload(): AdOrderSaveParams {
    return {
      orderName: form.orderName,
      businessEntityId: form.businessEntityId,
      customerId: form.customerId,
      industryCode: form.industryCode,
      signingEntity: form.signingEntity,
      orderType: form.orderType ?? undefined,
      upstreamAgentId: form.upstreamAgentId,
      agentOrderNo: form.agentOrderNo,
      totalAmount: form.totalAmount ?? undefined,
      rebateMode: form.rebateMode ?? undefined,
      rebateValue: form.rebateValue ?? undefined,
      noRebateAmount: form.noRebateAmount ?? undefined,
      mediaPayableAmount: form.mediaPayableAmount ?? undefined,
      deliveryStartDate: form.deliveryStartDate ?? undefined,
      deliveryEndDate: form.deliveryEndDate ?? undefined,
      deliveryVolume: form.deliveryVolume,
      remark: form.remark,
      receiptMethod: form.receiptMethod ?? undefined,
      receiptPrepayMode: form.receiptPrepayMode ?? undefined,
      receiptPrepayRatio: form.receiptPrepayRatio ?? undefined,
      receiptPrepayDeadline: form.receiptPrepayDeadline ?? undefined,
      receiptAccountPeriodDays: form.receiptAccountPeriodDays ?? undefined,
      paymentMethod: form.paymentMethod ?? undefined,
      paymentPrepayMode: form.paymentPrepayMode ?? undefined,
      paymentPrepayRatio: form.paymentPrepayRatio ?? undefined,
      paymentPrepayDeadline: form.paymentPrepayDeadline ?? undefined,
      paymentPostpayTrigger: form.paymentPostpayTrigger ?? undefined,
      paymentPostpayDays: form.paymentPostpayDays ?? undefined,
      currency: form.currency || 'CNY',
    };
  }

  function validate(): boolean {
    if (!form.orderName) {
      message.warning(`${t('advertising.order.form.orderName')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (!form.businessEntityId) {
      message.warning(`${t('advertising.order.form.businessEntityId')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (!form.customerId) {
      message.warning(`${t('advertising.order.form.customerId')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.orderType === null || form.orderType === undefined) {
      message.warning(`${t('advertising.order.form.orderType')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.receiptMethod === null || form.receiptMethod === undefined) {
      message.warning(`${t('advertising.order.form.receiptMethod')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.paymentMethod === null || form.paymentMethod === undefined) {
      message.warning(`${t('advertising.order.form.paymentMethod')} ${t('advertising.order.form.required')}`);
      return false;
    }
    return true;
  }

  async function handleSave() {
    if (!validate()) return;
    try {
      saving.value = true;
      const payload = buildPayload();
      if (isEdit.value) {
        payload.id = id;
        await updateAdOrder(payload);
      } else {
        await createAdOrder(payload);
      }
      message.success(t('advertising.common.saveSuccess'));
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_ORDER });
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    } finally {
      saving.value = false;
    }
  }

  /** 后端日期可能是 number(ms) 或 string(ISO)，统一转 number 供 NDatePicker 使用 */
  function toDateValue(value?: number | string | null): number | null {
    if (value === undefined || value === null || value === '') return null;
    if (typeof value === 'number') return value;
    const parsed = Date.parse(value);
    return Number.isNaN(parsed) ? null : parsed;
  }

  async function loadForEdit() {
    try {
      const res = await getAdOrderDetail(id);
      const o = res.order;
      if (!o) return;
      form.orderName = o.orderName;
      form.businessEntityId = o.businessEntityId;
      form.customerId = o.customerId;
      form.industryCode = o.industryCode;
      form.signingEntity = o.signingEntity;
      form.orderType = o.orderType ?? null;
      form.upstreamAgentId = o.upstreamAgentId;
      form.agentOrderNo = o.agentOrderNo;
      form.totalAmount = o.totalAmount ?? null;
      form.rebateMode = o.rebateMode ?? null;
      form.rebateValue = o.rebateValue ?? null;
      form.noRebateAmount = o.noRebateAmount ?? null;
      form.mediaPayableAmount = o.mediaPayableAmount ?? null;
      form.deliveryStartDate = toDateValue(o.deliveryStartDate);
      form.deliveryEndDate = toDateValue(o.deliveryEndDate);
      form.deliveryVolume = o.deliveryVolume;
      form.remark = o.remark;
      form.receiptMethod = o.receiptMethod ?? null;
      form.receiptPrepayMode = o.receiptPrepayMode ?? null;
      form.receiptPrepayRatio = o.receiptPrepayRatio ?? null;
      form.receiptPrepayDeadline = toDateValue(o.receiptPrepayDeadline);
      form.receiptAccountPeriodDays = o.receiptAccountPeriodDays ?? null;
      form.paymentMethod = o.paymentMethod ?? null;
      form.paymentPrepayMode = o.paymentPrepayMode ?? null;
      form.paymentPrepayRatio = o.paymentPrepayRatio ?? null;
      form.paymentPrepayDeadline = toDateValue(o.paymentPrepayDeadline);
      form.paymentPostpayTrigger = o.paymentPostpayTrigger ?? null;
      form.paymentPostpayDays = o.paymentPostpayDays ?? null;
      form.currency = o.currency || 'CNY';
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  onMounted(() => {
    if (isEdit.value) {
      loadForEdit();
    }
  });
</script>
