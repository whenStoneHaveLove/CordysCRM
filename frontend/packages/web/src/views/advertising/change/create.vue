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

          <n-grid v-if="selectedMetas.length" :cols="2" :x-gap="24">
            <n-grid-item v-for="meta in selectedMetas" :key="meta.field">
              <n-form-item :label="meta.label">
                <n-input-number
                  v-if="meta.type === 'number'"
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
              </n-form-item>
            </n-grid-item>
          </n-grid>
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
    NForm,
    NFormItem,
    NGrid,
    NGridItem,
    NInput,
    NInputNumber,
    NSelect,
    NSpace,
    NSpin,
    useMessage,
  } from 'naive-ui';

  import { AD_ORDER_CHANGE_FIELD_META } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderChangeSaveParams, AdOrderListItem } from '@lib/shared/models/advertising';

  import { createAdOrderChange, getAdOrderPage, submitAdOrderChange } from '@/api/modules';

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
      orderOptions.value = (res.list || []).map((o: AdOrderListItem) => ({
        label: `${o.orderNo || o.orderName}（${o.orderName || ''}）`,
        value: o.id,
      }));
    } catch (e) {
      message.error((e as Error).message || '加载订单失败');
    } finally {
      orderLoading.value = false;
    }
  }

  onMounted(loadOrders);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
</style>
