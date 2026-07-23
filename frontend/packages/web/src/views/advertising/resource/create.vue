<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{ isEdit ? t('advertising.resource.form.title.edit') : t('advertising.resource.form.title.create') }}
          </div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.resource.form.cancel') }}</n-button>
            <n-button type="primary" :loading="saving" @click="handleSave">{{
              t('advertising.resource.form.save')
            }}</n-button>
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-form ref="formRef" :model="form" label-placement="left" :label-width="120">
            <n-grid :cols="2" :x-gap="16" item-responsive>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.name')" path="resourceName">
                <n-input v-model:value="form.resourceName" placeholder="请输入资源名称" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.type')" path="resourceType">
                <n-select v-model:value="form.resourceType" :options="typeOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.mediaType')">
                <n-select
                  v-model:value="form.mediaType"
                  :options="mediaTypeOptions"
                  filterable
                  placeholder="请选择媒体类型"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.channel')">
                <n-input v-model:value="form.channel" placeholder="渠道" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.businessEntityId')">
                <n-select
                  v-model:value="form.businessEntityId"
                  :options="businessEntityOptions"
                  filterable
                  placeholder="请选择归属业务主体"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.signingEntity')">
                <n-input v-model:value="form.signingEntity" placeholder="签约主体" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.creditCode')">
                <n-input v-model:value="form.creditCode" placeholder="信用代码" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.rateCard')">
                <n-input-number v-model:value="form.rateCard" :min="0" :precision="2" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.discountPolicy')">
                <n-input v-model:value="form.discountPolicy" placeholder="折扣政策" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.position')">
                <n-input v-model:value="form.position" placeholder="广告位" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.dailyImpressions')">
                <n-input-number v-model:value="form.dailyImpressions" :min="0" :precision="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.unitPrice')">
                <n-input-number v-model:value="form.unitPrice" :min="0" :precision="2" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.resource.form.remark')">
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
  import { NButton, NForm, NFormItemGi, NGrid, NInput, NInputNumber, NSelect, useMessage } from 'naive-ui';

  import { AdResourceTypeOptions } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdResourceSaveParams } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import {
    createAdResource,
    getAdBusinessEntityPage,
    getAdDictPage,
    getAdResourceDetail,
    updateAdResource,
  } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { toNumberOrNull } from '../utils';

  const typeOptions = AdResourceTypeOptions;

  type SelectItem = { label: string; value: string };

  const businessEntityOptions = ref<SelectItem[]>([]);
  const mediaTypeOptions = ref<SelectItem[]>([]);

  /** 表单本地类型：数值使用 number | null 以适配 Naive UI 控件 */
  interface AdResourceForm {
    resourceName?: string;
    resourceType?: number | null;
    mediaType?: string;
    channel?: string;
    rateCard?: number | null;
    discountPolicy?: string;
    creditCode?: string;
    signingEntity?: string;
    businessEntityId?: string;
    position?: string;
    dailyImpressions?: number | null;
    unitPrice?: number | null;
    remark?: string;
  }

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const id = (route.params.id as string) || '';
  const isEdit = computed(() => !!id);
  const saving = ref(false);

  const form = reactive<AdResourceForm>({
    resourceName: undefined,
    resourceType: null,
    mediaType: undefined,
    channel: undefined,
    rateCard: null,
    discountPolicy: undefined,
    creditCode: undefined,
    signingEntity: undefined,
    businessEntityId: undefined,
    position: undefined,
    dailyImpressions: null,
    unitPrice: null,
    remark: undefined,
  });

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_RESOURCE });
  }

  /** 拉取归属业务主体 / 媒体类型(字典)下拉选项 */
  async function loadSelectOptions() {
    try {
      const [beRes, dictRes] = await Promise.all([
        getAdBusinessEntityPage({ current: 1, pageSize: 200 }),
        getAdDictPage({ current: 1, pageSize: 200, dictCode: 'media_type' }),
      ]);
      businessEntityOptions.value = (beRes.list || []).map((it) => ({
        label: it.name || it.id,
        value: it.id,
      }));
      mediaTypeOptions.value = (dictRes.list || []).map((it) => ({
        label: it.dictLabel || it.dictValue || it.id,
        value: it.dictValue || it.id,
      }));
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  function buildPayload(): AdResourceSaveParams {
    return {
      resourceName: form.resourceName,
      resourceType: form.resourceType ?? undefined,
      mediaType: form.mediaType,
      channel: form.channel,
      rateCard: form.rateCard ?? undefined,
      discountPolicy: form.discountPolicy,
      creditCode: form.creditCode,
      signingEntity: form.signingEntity,
      businessEntityId: form.businessEntityId,
      position: form.position,
      dailyImpressions: form.dailyImpressions ?? undefined,
      unitPrice: form.unitPrice ?? undefined,
      remark: form.remark,
    };
  }

  function validate(): boolean {
    if (!form.resourceName) {
      message.warning(`${t('advertising.resource.form.name')} ${t('advertising.resource.form.required')}`);
      return false;
    }
    if (form.resourceType === null || form.resourceType === undefined) {
      message.warning(`${t('advertising.resource.form.type')} ${t('advertising.resource.form.required')}`);
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
        await updateAdResource(payload);
      } else {
        await createAdResource(payload);
      }
      message.success(t('advertising.common.saveSuccess'));
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_RESOURCE });
    } catch (e) {
      message.error((e as Error).message || '保存失败');
    } finally {
      saving.value = false;
    }
  }

  async function loadForEdit() {
    try {
      const res = await getAdResourceDetail(id);
      form.resourceName = res.resource?.resourceName;
      form.resourceType = res.resource?.resourceType ?? null;
      form.mediaType = res.resource?.mediaType;
      form.channel = res.resource?.channel;
      form.rateCard = toNumberOrNull(res.resource?.rateCard);
      form.discountPolicy = res.resource?.discountPolicy;
      form.creditCode = res.resource?.creditCode;
      form.signingEntity = res.resource?.signingEntity;
      form.businessEntityId = res.resource?.businessEntityId;
      form.position = res.resource?.position;
      form.dailyImpressions = res.resource?.dailyImpressions ?? null;
      form.unitPrice = res.resource?.unitPrice ?? null;
      form.remark = res.resource?.remark;
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    }
  }

  onMounted(async () => {
    await loadSelectOptions();
    if (isEdit.value) {
      loadForEdit();
    }
  });
</script>
