<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{ isEdit ? t('advertising.customer.form.title.edit') : t('advertising.customer.form.title.create') }}
          </div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.customer.form.cancel') }}</n-button>
            <n-button
              v-permission="[isEdit ? 'AD_CUSTOMER:UPDATE' : 'AD_CUSTOMER:CREATE']"
              type="primary"
              :loading="saving"
              @click="handleSave"
            >{{ t('advertising.customer.form.save') }}</n-button>
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-form ref="formRef" :model="form" label-placement="left" :label-width="120">
            <n-grid :cols="2" :x-gap="16" item-responsive>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.name')" path="customerName">
                <n-input v-model:value="form.customerName" placeholder="请输入客户名称（全局唯一）" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.brand')">
                <n-input v-model:value="form.brand" placeholder="品牌" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.industryCode')">
                <n-select
                  v-model:value="form.industryCode"
                  :options="industryOptions"
                  filterable
                  placeholder="请选择行业类别"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.signingEntity')">
                <n-input v-model:value="form.signingEntity" placeholder="签约主体" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.contactPerson')">
                <n-input v-model:value="form.contactPerson" placeholder="联系人" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.contactPhone')">
                <n-input v-model:value="form.contactPhone" placeholder="联系电话" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.email')">
                <n-input v-model:value="form.email" placeholder="邮箱" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.address')">
                <n-input v-model:value="form.address" placeholder="地址" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.customerLevel')" path="customerLevel">
                <n-select v-model:value="form.customerLevel" :options="levelOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.customer.form.status')" path="status">
                <n-select v-model:value="form.status" :options="statusOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.customer.form.remark')">
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
  import { NButton, NForm, NFormItemGi, NGrid, NInput, NSelect, useMessage } from 'naive-ui';

  import { AdCustomerLevelOptions, AdCustomerStatusOptions } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdCustomerSaveParams } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import { createAdCustomer, getAdCustomerDetail, getAdDictPage, updateAdCustomer } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const levelOptions = AdCustomerLevelOptions;
  const statusOptions = AdCustomerStatusOptions;

  type SelectItem = { label: string; value: string };

  const industryOptions = ref<SelectItem[]>([]);

  /** 表单本地类型：等级/状态使用 number | null 以适配 Naive UI 控件 */
  interface AdCustomerForm {
    customerName?: string;
    brand?: string;
    industryCode?: string;
    signingEntity?: string;
    contactPerson?: string;
    contactPhone?: string;
    email?: string;
    address?: string;
    customerLevel?: number | null;
    status?: number | null;
    remark?: string;
  }

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const id = (route.params.id as string) || '';
  const isEdit = computed(() => !!id);
  const saving = ref(false);

  const form = reactive<AdCustomerForm>({
    customerName: undefined,
    brand: undefined,
    industryCode: undefined,
    signingEntity: undefined,
    contactPerson: undefined,
    contactPhone: undefined,
    email: undefined,
    address: undefined,
    customerLevel: 20,
    status: 0,
    remark: undefined,
  });

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER });
  }

  /** 拉取行业类别(字典 industry)下拉选项 */
  async function loadIndustryOptions() {
    try {
      const res = await getAdDictPage({ current: 1, pageSize: 200, dictCode: 'industry' });
      industryOptions.value = (res.list || []).map((it) => ({
        label: it.dictLabel || it.dictValue || it.id,
        value: it.dictValue || it.id,
      }));
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  function buildPayload(): AdCustomerSaveParams {
    return {
      customerName: form.customerName,
      brand: form.brand,
      industryCode: form.industryCode,
      signingEntity: form.signingEntity,
      contactPerson: form.contactPerson,
      contactPhone: form.contactPhone,
      email: form.email,
      address: form.address,
      customerLevel: form.customerLevel ?? undefined,
      status: form.status ?? undefined,
      remark: form.remark,
    };
  }

  function validate(): boolean {
    if (!form.customerName) {
      message.warning(`${t('advertising.customer.form.name')} ${t('advertising.customer.form.required')}`);
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
        await updateAdCustomer(payload);
      } else {
        await createAdCustomer(payload);
      }
      message.success(t('advertising.common.saveSuccess'));
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER });
    } catch (e) {
      message.error((e as Error).message || '保存失败');
    } finally {
      saving.value = false;
    }
  }

  async function loadForEdit() {
    try {
      const res = await getAdCustomerDetail(id);
      form.customerName = res.customer?.name;
      form.brand = res.customer?.brand;
      form.industryCode = res.customer?.industryCode;
      form.signingEntity = res.customer?.signingEntity;
      form.contactPerson = res.customer?.contactPerson;
      form.contactPhone = res.customer?.contactPhone;
      form.email = res.customer?.email;
      form.address = res.customer?.address;
      form.customerLevel = res.customer?.customerLevel ?? 20;
      form.status = res.customer?.status ?? 0;
      form.remark = res.customer?.remark;
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    }
  }

  onMounted(async () => {
    await loadIndustryOptions();
    if (isEdit.value) {
      loadForEdit();
    }
  });
</script>
