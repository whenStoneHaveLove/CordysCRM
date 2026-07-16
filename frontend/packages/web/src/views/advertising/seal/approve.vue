<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{ t('advertising.seal.form.title.approve') }}
          </div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.order.form.cancel') }}</n-button>
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-spin :show="loading">
            <n-card v-if="detail" :bordered="false" size="small" class="mb-[16px]">
              <n-descriptions label-placement="left" :column="2" bordered size="small">
                <n-descriptions-item label="合同编号">{{ detail.contractNo || '-' }}</n-descriptions-item>
                <n-descriptions-item label="用印类型">{{
                  getAdSealTypeLabel(detail.record.sealType)
                }}</n-descriptions-item>
                <n-descriptions-item label="申请份数">{{ detail.record.appliedCopies || '-' }}</n-descriptions-item>
                <n-descriptions-item label="申请备注">{{ detail.record.applyRemark || '-' }}</n-descriptions-item>
                <n-descriptions-item label="申请人">{{ detail.record.applicantId || '-' }}</n-descriptions-item>
                <n-descriptions-item label="状态">{{ detail.statusLabel || '-' }}</n-descriptions-item>
              </n-descriptions>
            </n-card>

            <n-form ref="formRef" :model="form" label-placement="left" :label-width="120">
              <n-grid :cols="2" :x-gap="16" item-responsive>
                <n-form-item-gi :span="1" :label="t('advertising.seal.form.actualCopies')">
                  <n-input-number v-model:value="form.actualCopies" :min="0" style="width: 100%" />
                </n-form-item-gi>
                <n-form-item-gi :span="2" :label="t('advertising.seal.form.approveRemark')">
                  <n-input v-model:value="form.approveRemark" type="textarea" placeholder="审批备注" />
                </n-form-item-gi>
              </n-grid>
            </n-form>

            <n-space justify="center" class="mt-[16px]">
              <n-button type="success" :loading="saving" @click="handleApprove">{{
                t('advertising.seal.approve')
              }}</n-button>
              <n-button type="error" :loading="saving" @click="handleReject">{{
                t('advertising.seal.reject')
              }}</n-button>
            </n-space>
          </n-spin>
        </div>
      </div>
    </CrmCard>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDescriptions,
    NDescriptionsItem,
    NForm,
    NFormItemGi,
    NGrid,
    NInput,
    NInputNumber,
    NSpace,
    NSpin,
    useMessage,
  } from 'naive-ui';

  import { getAdSealTypeLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdSealApproveParams, AdSealRecordDetailResponse } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import { approveAdSeal, getAdSealDetail, rejectAdSeal } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const sealId = route.params.id as string;
  const loading = ref(false);
  const saving = ref(false);
  const detail = ref<AdSealRecordDetailResponse | null>(null);

  const form = reactive<{ actualCopies: number | null; approveRemark: string }>({
    actualCopies: null,
    approveRemark: '',
  });

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_SEAL });
  }

  function buildPayload(): AdSealApproveParams {
    return {
      actualCopies: form.actualCopies ?? undefined,
      approveRemark: form.approveRemark || undefined,
    };
  }

  async function handleApprove() {
    saving.value = true;
    try {
      await approveAdSeal(sealId, buildPayload());
      message.success(t('advertising.common.operateSuccess'));
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_SEAL });
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    } finally {
      saving.value = false;
    }
  }

  async function handleReject() {
    saving.value = true;
    try {
      await rejectAdSeal(sealId, buildPayload());
      message.success(t('advertising.common.operateSuccess'));
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_SEAL });
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    } finally {
      saving.value = false;
    }
  }

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdSealDetail(sealId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  onMounted(fetchDetail);
</script>
