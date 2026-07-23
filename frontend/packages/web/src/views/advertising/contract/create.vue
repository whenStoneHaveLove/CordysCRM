<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{ isEdit ? t('advertising.contract.form.title.edit') : t('advertising.contract.form.title.create') }}
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
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.contractName')" path="contractName">
                <n-input v-model:value="form.contractName" placeholder="请输入合同名称" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.contractNo')" path="contractNo">
                <n-input v-model:value="form.contractNo" placeholder="留空由系统生成" />
              </n-form-item-gi>
              <n-form-item-gi
                :span="1"
                :label="t('advertising.contract.form.businessEntityId')"
                path="businessEntityId"
              >
                <n-select
                  v-model:value="form.businessEntityId"
                  :options="businessEntityOptions"
                  filterable
                  placeholder="请选择业务主体"
                />
              </n-form-item-gi>
              <n-form-item-gi
                :span="1"
                :label="t('advertising.contract.form.contractDirection')"
                path="contractDirection"
              >
                <n-select v-model:value="form.contractDirection" :options="directionOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.contractType')" path="contractType">
                <n-select v-model:value="form.contractType" :options="typeOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi
                :span="1"
                :label="t('advertising.contract.form.relatedPartyType')"
                path="relatedPartyType"
              >
                <n-select
                  v-model:value="form.relatedPartyType"
                  :options="relatedPartyTypeOptions"
                  placeholder="请选择关联方类型"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.relatedPartyId')" path="relatedPartyId">
                <n-select
                  v-model:value="form.relatedPartyId"
                  :options="relatedPartyOptions"
                  filterable
                  placeholder="请先选择关联方类型"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.orderId')">
                <n-select
                  v-model:value="form.orderId"
                  :options="orderOptions"
                  filterable
                  placeholder="请选择关联订单"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.amount')" path="amount">
                <n-input-number v-model:value="form.amount" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.signingEntity')">
                <n-input v-model:value="form.signingEntity" placeholder="签约主体" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.validFrom')">
                <n-date-picker v-model:value="form.validFrom" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.validTo')">
                <n-date-picker v-model:value="form.validTo" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.fileUrl')">
                <n-input v-model:value="form.fileUrl" placeholder="合同文件地址" />
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.contract.form.rebateTerms')">
                <n-input v-model:value="form.rebateTerms" type="textarea" placeholder="返点条款" />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </div>
      </div>
    </CrmCard>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, NDatePicker, NForm, NFormItemGi, NGrid, NInput, NInputNumber, NSelect, useMessage } from 'naive-ui';

  import {
    AdContractDirectionOptions,
    AdContractTypeOptions,
    AdRelatedPartyTypeOptions,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdContractSaveParams } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import {
    createAdContract,
    getAdBusinessEntityPage,
    getAdContractDetail,
    getAdCustomerPage,
    getAdOrderPage,
    getAdResourcePage,
    updateAdContract,
  } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const directionOptions = AdContractDirectionOptions;
  const typeOptions = AdContractTypeOptions;
  const relatedPartyTypeOptions = AdRelatedPartyTypeOptions;

  type SelectItem = { label: string; value: string };

  const businessEntityOptions = ref<SelectItem[]>([]);
  const relatedPartyOptions = ref<SelectItem[]>([]);
  const orderOptions = ref<SelectItem[]>([]);
  /** 编辑回填期间抑制 watch，避免程序化设置 relatedPartyType 时清空已回填的 relatedPartyId */
  const suppressWatch = ref(false);

  /** 表单本地类型 */
  interface AdContractForm {
    contractNo?: string;
    contractName?: string;
    businessEntityId?: string;
    contractDirection?: number | null;
    contractType?: number | null;
    relatedPartyType?: number | null;
    relatedPartyId?: string;
    orderId?: string;
    signingEntity?: string;
    validFrom?: number | null;
    validTo?: number | null;
    amount?: number | null;
    rebateTerms?: string;
    fileUrl?: string;
  }

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const id = (route.params.id as string) || '';
  const isEdit = computed(() => !!id);
  const saving = ref(false);

  const form = reactive<AdContractForm>({
    contractNo: undefined,
    contractName: undefined,
    businessEntityId: undefined,
    contractDirection: null,
    contractType: null,
    relatedPartyType: null,
    relatedPartyId: undefined,
    orderId: undefined,
    signingEntity: undefined,
    validFrom: null,
    validTo: null,
    amount: null,
    rebateTerms: undefined,
    fileUrl: undefined,
  });

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT });
  }

  /** 业务主体 / 关联订单 选项（不依赖关联方类型，可预加载） */
  async function loadCommonOptions() {
    try {
      const [beRes, orderRes] = await Promise.all([
        getAdBusinessEntityPage({ current: 1, pageSize: 200 }),
        getAdOrderPage({ current: 1, pageSize: 200 }),
      ]);
      businessEntityOptions.value = (beRes.list || []).map((it) => ({
        label: it.name || it.id,
        value: it.id,
      }));
      orderOptions.value = (orderRes.list || []).map((it) => ({
        label: [it.orderNo, it.orderName].filter(Boolean).join(' ') || it.id,
        value: it.id,
      }));
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  /** 关联方选项：依据 relatedPartyType 决定数据源
   *  10 客户 → 客户表；20 上游代理 / 30 下游媒体 → 资源表(resourceType 对应) */
  async function loadRelatedPartyOptions(type?: number | null) {
    if (type === 10) {
      try {
        const res = await getAdCustomerPage({ current: 1, pageSize: 200 });
        relatedPartyOptions.value = (res.list || []).map((it) => ({
          label: it.customerName || it.id,
          value: it.id,
        }));
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(e);
      }
    } else if (type === 20 || type === 30) {
      try {
        const res = await getAdResourcePage({
          current: 1,
          pageSize: 200,
          resourceType: type === 20 ? 10 : 20,
        });
        relatedPartyOptions.value = (res.list || []).map((it) => ({
          label: it.resourceName || it.id,
          value: it.id,
        }));
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(e);
      }
    } else {
      relatedPartyOptions.value = [];
    }
  }

  function buildPayload(): AdContractSaveParams {
    return {
      contractNo: form.contractNo,
      contractName: form.contractName,
      businessEntityId: form.businessEntityId,
      contractDirection: form.contractDirection ?? undefined,
      contractType: form.contractType ?? undefined,
      relatedPartyType: form.relatedPartyType ?? undefined,
      relatedPartyId: form.relatedPartyId,
      orderId: form.orderId,
      signingEntity: form.signingEntity,
      validFrom: form.validFrom ?? undefined,
      validTo: form.validTo ?? undefined,
      amount: form.amount ?? undefined,
      rebateTerms: form.rebateTerms,
      fileUrl: form.fileUrl,
    };
  }

  function validate(): boolean {
    if (!form.contractName) {
      message.warning(`${t('advertising.contract.form.contractName')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (!form.businessEntityId) {
      message.warning(`${t('advertising.contract.form.businessEntityId')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.contractDirection === null || form.contractDirection === undefined) {
      message.warning(`${t('advertising.contract.form.contractDirection')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.contractType === null || form.contractType === undefined) {
      message.warning(`${t('advertising.contract.form.contractType')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.relatedPartyType === null || form.relatedPartyType === undefined) {
      message.warning(`${t('advertising.contract.form.relatedPartyType')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (!form.relatedPartyId) {
      message.warning(`${t('advertising.contract.form.relatedPartyId')} ${t('advertising.order.form.required')}`);
      return false;
    }
    // 单笔合同(contractType=20) 关联订单必填
    if (form.contractType === 20 && !form.orderId) {
      message.warning(`${t('advertising.contract.form.orderId')} ${t('advertising.order.form.required')}`);
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
        await updateAdContract(payload);
      } else {
        await createAdContract(payload);
      }
      message.success(t('advertising.common.saveSuccess'));
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT });
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
      const res = await getAdContractDetail(id);
      const o = res.contract;
      if (!o) return;
      form.contractNo = o.contractNo;
      form.contractName = o.contractName;
      form.businessEntityId = o.businessEntityId;
      form.contractDirection = o.contractDirection ?? null;
      form.contractType = o.contractType ?? null;
      form.relatedPartyType = o.relatedPartyType ?? null;
      form.relatedPartyId = o.relatedPartyId;
      form.orderId = o.orderId;
      form.signingEntity = o.signingEntity;
      form.validFrom = toDateValue(o.validFrom);
      form.validTo = toDateValue(o.validTo);
      form.amount = o.amount ?? null;
      form.rebateTerms = o.rebateTerms;
      form.fileUrl = o.fileUrl;
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  // 关联方类型变化 → 清空已选关联方并重新拉取对应数据源
  watch(
    () => form.relatedPartyType,
    (val) => {
      if (suppressWatch.value) return;
      form.relatedPartyId = undefined;
      loadRelatedPartyOptions(val);
    }
  );

  onMounted(async () => {
    await loadCommonOptions();
    if (isEdit.value) {
      suppressWatch.value = true;
      await loadForEdit();
      suppressWatch.value = false;
      // 编辑回填后，依据(已回填的)关联方类型加载对应选项
      loadRelatedPartyOptions(form.relatedPartyType);
    } else {
      // 新建：类型未选，选项置空
      loadRelatedPartyOptions(form.relatedPartyType);
    }
  });
</script>
