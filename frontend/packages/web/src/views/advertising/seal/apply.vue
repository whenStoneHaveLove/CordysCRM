<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{ t('advertising.seal.form.title.apply') }}
          </div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.order.form.cancel') }}</n-button>
            <n-button v-permission="['AD_SEAL:APPLY']" type="primary" :loading="saving" @click="handleSave">{{ t('advertising.seal.submit') }}</n-button>
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-form ref="formRef" :model="form" label-placement="left" :label-width="120">
            <n-grid :cols="2" :x-gap="16" item-responsive>
              <n-form-item-gi :span="1" :label="t('advertising.seal.form.contractId')" path="contractId">
                <n-select
                  v-model:value="form.contractId"
                  :options="contractOptions"
                  filterable
                  placeholder="请选择待用印合同"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.seal.form.sealType')" path="sealType">
                <n-select v-model:value="form.sealType" :options="sealTypeOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.seal.form.appliedCopies')" path="appliedCopies">
                <n-input-number v-model:value="form.appliedCopies" :min="1" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.seal.form.applyRemark')">
                <n-input v-model:value="form.applyRemark" type="textarea" placeholder="申请备注" />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </div>
      </div>
    </CrmCard>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { NButton, NForm, NFormItemGi, NGrid, NInput, NInputNumber, NSelect, useMessage } from 'naive-ui';

  import { AdSealTypeOptions } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdSealApplyParams } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import { applyAdSeal, getAdContractPage } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  type SelectItem = { label: string; value: string };

  const sealTypeOptions = AdSealTypeOptions;
  const contractOptions = ref<SelectItem[]>([]);

  /** 表单本地类型 */
  interface AdSealApplyForm {
    contractId?: string;
    sealType?: number | null;
    appliedCopies?: number | null;
    applyRemark?: string;
  }

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const saving = ref(false);

  const form = reactive<AdSealApplyForm>({
    contractId: undefined,
    sealType: null,
    appliedCopies: 1,
    applyRemark: undefined,
  });

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_SEAL });
  }

  /** 加载「待用印合同」：状态=生效 且 用印状态=未申请 */
  async function loadPendingContracts() {
    try {
      const [pending, rejected] = await Promise.all([
        getAdContractPage({ current: 1, pageSize: 200, status: 10, sealStatus: 0 }),
        getAdContractPage({ current: 1, pageSize: 200, status: 10, sealStatus: 30 }),
      ]);
      const list = [...(pending.list || []), ...(rejected.list || [])];
      contractOptions.value = list.map((it) => ({
        label: `${it.contractNo} - ${it.contractName}`,
        value: it.id,
      }));
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  function buildPayload(): AdSealApplyParams {
    return {
      contractId: form.contractId,
      sealType: form.sealType ?? undefined,
      appliedCopies: form.appliedCopies ?? undefined,
      applyRemark: form.applyRemark,
    };
  }

  function validate(): boolean {
    if (!form.contractId) {
      message.warning(`${t('advertising.seal.form.contractId')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.sealType === null || form.sealType === undefined) {
      message.warning(`${t('advertising.seal.form.sealType')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (!form.appliedCopies || form.appliedCopies < 1) {
      message.warning(`${t('advertising.seal.form.appliedCopies')} ${t('advertising.order.form.required')}`);
      return false;
    }
    return true;
  }

  async function handleSave() {
    if (!validate()) return;
    try {
      saving.value = true;
      await applyAdSeal(buildPayload());
      message.success(t('advertising.common.operateSuccess'));
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_SEAL });
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    } finally {
      saving.value = false;
    }
  }

  onMounted(() => {
    loadPendingContracts();
  });
</script>
