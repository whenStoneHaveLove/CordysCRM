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
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.name')" path="name">
                <n-input v-model:value="form.name" placeholder="请输入资源名称" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.type')" path="resourceType">
                <n-select v-model:value="form.resourceType" :options="typeOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.mediaType')">
                <n-input v-model:value="form.mediaType" placeholder="媒体类型(字典 media_type)" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.channel')">
                <n-input v-model:value="form.channel" placeholder="渠道" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.resource.form.businessEntityId')">
                <n-input v-model:value="form.businessEntityId" placeholder="归属业务主体ID" />
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

  import { createAdResource, getAdResourceDetail, updateAdResource } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const typeOptions = AdResourceTypeOptions;

  /** 表单本地类型：数值使用 number | null 以适配 Naive UI 控件 */
  interface AdResourceForm {
    name?: string;
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
    name: undefined,
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

  function buildPayload(): AdResourceSaveParams {
    return {
      name: form.name,
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
    if (!form.name) {
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
      form.name = res.name;
      form.resourceType = res.resourceType ?? null;
      form.mediaType = res.mediaType;
      form.channel = res.channel;
      form.rateCard = res.rateCard ?? null;
      form.discountPolicy = res.discountPolicy;
      form.creditCode = res.creditCode;
      form.signingEntity = res.signingEntity;
      form.businessEntityId = res.businessEntityId;
      form.position = res.position;
      form.dailyImpressions = res.dailyImpressions ?? null;
      form.unitPrice = res.unitPrice ?? null;
      form.remark = res.remark;
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    }
  }

  onMounted(() => {
    if (isEdit.value) {
      loadForEdit();
    }
  });
</script>
