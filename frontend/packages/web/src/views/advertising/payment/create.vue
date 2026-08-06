<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">{{ t('advertising.payment.new') }}</div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.order.form.cancel') }}</n-button>
            <n-button type="primary" :loading="saving" @click="handleSave">{{
              t('advertising.order.form.save')
            }}</n-button>
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-form :model="form" label-placement="left" :label-width="120">
            <n-grid :cols="2" :x-gap="16" item-responsive>
              <n-form-item-gi :span="1" :label="t('advertising.payment.form.orderId')" path="orderId">
                <n-select
                  v-model:value="form.orderId"
                  :options="orderOptions"
                  filterable
                  clearable
                  :placeholder="id ? '' : '请选择订单'"
                  :disabled="!!id"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.payment.form.direction')" path="direction">
                <n-select
                  v-model:value="form.direction"
                  :options="paymentDirectionOptions"
                  placeholder="方向（必填）"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.payment.form.type')" path="type">
                <n-select v-model:value="form.type" :options="paymentTypeOptions" placeholder="类型（必填）" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.payment.form.amount')" path="amount">
                <n-input-number v-model:value="form.amount" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.payment.form.occurDate')">
                <n-date-picker v-model:value="form.occurDate" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.payment.form.invoiceNo')">
                <n-input v-model:value="form.invoiceNo" placeholder="发票号（type=开票收款时填写）" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.payment.form.resourceId')">
                <n-input v-model:value="form.resourceId" placeholder="关联媒体/代理" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.payment.form.settleType')">
                <n-select
                  v-model:value="form.settleType"
                  clearable
                  :options="[
                    { label: '仅开票 INVOICE', value: 'INVOICE' },
                    { label: '仅收款 RECEIPT', value: 'RECEIPT' },
                    { label: '同时 BOTH', value: 'BOTH' },
                  ]"
                  placeholder="结算口径（上游开票收款生效）"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.payment.form.remark')">
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
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, NDatePicker, NForm, NFormItemGi, NGrid, NInput, NInputNumber, NSelect, useMessage } from 'naive-ui';

  import { AdPaymentDirectionOptions, AdPaymentTypeOptions } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdPaymentRecordCreateParams } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import { createAdPayment, getAdOrderPage } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const paymentDirectionOptions = AdPaymentDirectionOptions;
  const paymentTypeOptions = AdPaymentTypeOptions;

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const id = (route.params.id as string) || '';
  const saving = ref(false);
  const orderOptions = ref<Array<{ label: string; value: string }>>([]);

  const form = reactive({
    orderId: undefined as string | undefined,
    direction: null as number | null,
    type: null as number | null,
    amount: null as number | null,
    occurDate: null as number | null,
    invoiceNo: undefined as string | undefined,
    resourceId: undefined as string | undefined,
    remark: undefined as string | undefined,
    settleType: undefined as string | undefined,
  });

  function buildPayload(): AdPaymentRecordCreateParams {
    return {
      orderId: form.orderId,
      direction: form.direction ?? undefined,
      type: form.type ?? undefined,
      amount: form.amount ?? undefined,
      occurDate: form.occurDate ?? undefined,
      invoiceNo: form.invoiceNo,
      resourceId: form.resourceId,
      remark: form.remark,
      settleType: form.settleType,
    };
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_PAYMENT });
  }

  function handleSave() {
    if (!form.orderId) {
      message.warning(`${t('advertising.payment.form.orderId')} ${t('advertising.order.form.required')}`);
      return;
    }
    if (form.direction === null || form.direction === undefined) {
      message.warning(`${t('advertising.payment.form.direction')} ${t('advertising.order.form.required')}`);
      return;
    }
    if (form.type === null || form.type === undefined) {
      message.warning(`${t('advertising.payment.form.type')} ${t('advertising.order.form.required')}`);
      return;
    }
    if (form.amount === null || form.amount === undefined || form.amount <= 0) {
      message.warning(`${t('advertising.payment.form.amount')} > 0`);
      return;
    }
    saving.value = true;
    createAdPayment(buildPayload())
      .then(() => {
        message.success(t('advertising.common.saveSuccess'));
        router.push({ name: AdvertisingRouteEnum.ADVERTISING_PAYMENT });
      })
      .catch((e) => {
        // eslint-disable-next-line no-console
        console.error(e);
      })
      .finally(() => {
        saving.value = false;
      });
  }

  async function loadOrders() {
    try {
      const res = await getAdOrderPage({ current: 1, pageSize: 200 });
      orderOptions.value = (res.list || []).map((it: any) => ({
        label: `${it.orderNo || it.id}（${it.orderName || ''}）`,
        value: it.id,
      }));
      if (id) {
        form.orderId = id;
        // 如果路由带的 id 不在列表中，追加一条确保能选中
        if (!orderOptions.value.some((o) => o.value === id)) {
          orderOptions.value.unshift({ label: `${id}（路由传入）`, value: id });
        }
      }
    } catch {
      // 静默失败
    }
  }

  onMounted(() => {
    loadOrders();
  });
</script>
