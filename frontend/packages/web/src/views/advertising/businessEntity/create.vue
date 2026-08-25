<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{
              isEdit
                ? t('advertising.businessEntity.form.title.edit')
                : t('advertising.businessEntity.form.title.create')
            }}
          </div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.businessEntity.form.cancel') }}</n-button>
            <n-button
              v-permission="[isEdit ? 'AD_BUSINESS_ENTITY:UPDATE' : 'AD_BUSINESS_ENTITY:CREATE']"
              type="primary"
              :loading="saving"
              @click="handleSave"
              >{{ t('advertising.businessEntity.form.save') }}</n-button
            >
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-form :model="form" label-placement="left" :label-width="120">
            <n-grid :cols="2" :x-gap="16" item-responsive>
              <n-form-item-gi :span="1" :label="t('advertising.businessEntity.form.name')" path="name">
                <n-input v-model:value="form.name" placeholder="请输入主体名称" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.businessEntity.form.code')" path="code">
                <n-input v-model:value="form.code" placeholder="如 JS / TH" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.businessEntity.form.status')" path="status">
                <n-select v-model:value="form.status" :options="statusOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.businessEntity.form.isCrossEntity')">
                <n-switch v-model:value="isCrossEntity" />
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.businessEntity.form.remark')">
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
  import { NButton, NForm, NFormItemGi, NGrid, NInput, NSelect, NSwitch, useMessage } from 'naive-ui';

  import { AdBusinessEntityStatusOptions } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdBusinessEntitySaveParams } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import { createAdBusinessEntity, getAdBusinessEntityDetail, updateAdBusinessEntity } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const statusOptions = AdBusinessEntityStatusOptions;

  interface AdBusinessEntityForm {
    name?: string;
    code?: string;
    status?: number | null;
    isCrossEntity?: number;
    remark?: string;
  }

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const id = (route.params.id as string) || '';
  const isEdit = computed(() => !!id);
  const saving = ref(false);

  const form = reactive<AdBusinessEntityForm>({
    name: undefined,
    code: undefined,
    status: 10,
    isCrossEntity: 0,
    remark: undefined,
  });

  const isCrossEntity = computed({
    get: () => form.isCrossEntity === 1,
    set: (v: boolean) => {
      form.isCrossEntity = v ? 1 : 0;
    },
  });

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY });
  }

  function buildPayload(): AdBusinessEntitySaveParams {
    return {
      name: form.name,
      code: form.code,
      status: form.status ?? 10,
      isCrossEntity: form.isCrossEntity ?? 0,
      remark: form.remark,
    };
  }

  function validate(): boolean {
    if (!form.name) {
      message.warning(`${t('advertising.businessEntity.form.name')} ${t('advertising.businessEntity.form.required')}`);
      return false;
    }
    if (!form.code) {
      message.warning(`${t('advertising.businessEntity.form.code')} ${t('advertising.businessEntity.form.required')}`);
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
        await updateAdBusinessEntity(payload);
      } else {
        await createAdBusinessEntity(payload);
      }
      message.success(t('advertising.common.saveSuccess'));
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY });
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    } finally {
      saving.value = false;
    }
  }

  async function loadForEdit() {
    try {
      const res = await getAdBusinessEntityDetail(id);
      form.name = res.entity?.name;
      form.code = res.entity?.code;
      form.status = res.entity?.status ?? 10;
      form.isCrossEntity = res.entity?.isCrossEntity ?? 0;
      form.remark = res.entity?.remark;
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
