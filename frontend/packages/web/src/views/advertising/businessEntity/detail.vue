<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{ t('advertising.businessEntity.detail') }} · {{ detail.entity?.name || id }}
          </div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.businessEntity.form.cancel') }}</n-button>
            <n-button type="primary" @click="goEdit">{{ t('advertising.businessEntity.edit') }}</n-button>
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-descriptions bordered :column="2" label-placement="left">
            <n-descriptions-item :label="t('advertising.businessEntity.form.name')">{{
              detail.entity?.name || '-'
            }}</n-descriptions-item>
            <n-descriptions-item :label="t('advertising.businessEntity.form.code')">{{
              detail.entity?.code || '-'
            }}</n-descriptions-item>
            <n-descriptions-item :label="t('advertising.businessEntity.form.status')">
              <n-tag :type="detail.entity?.status === 10 ? 'success' : 'default'">{{
                getAdBusinessEntityStatusLabel(detail.entity?.status)
              }}</n-tag>
            </n-descriptions-item>
            <n-descriptions-item :label="t('advertising.businessEntity.form.isCrossEntity')">
              {{ detail.entity?.isCrossEntity === 1 ? t('advertising.common.yes') : t('advertising.common.no') }}
            </n-descriptions-item>
            <n-descriptions-item :label="t('advertising.businessEntity.column.userCount')">{{
              detail.userCount ?? '-'
            }}</n-descriptions-item>
            <n-descriptions-item :label="t('advertising.businessEntity.column.orderCount')">{{
              detail.orderCount ?? '-'
            }}</n-descriptions-item>
            <n-descriptions-item :label="t('advertising.businessEntity.column.remark')">{{
              detail.entity?.remark || '-'
            }}</n-descriptions-item>
            <n-descriptions-item :label="t('advertising.businessEntity.column.createTime')">{{
              fmtDateTime(detail.createTime)
            }}</n-descriptions-item>
          </n-descriptions>
        </div>
      </div>
    </CrmCard>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, reactive } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, NDescriptions, NDescriptionsItem, NTag, useMessage } from 'naive-ui';

  import { getAdBusinessEntityStatusLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdBusinessEntityDetail } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import { getAdBusinessEntityDetail } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const id = (route.params.id as string) || '';

  const detail = reactive<AdBusinessEntityDetail>({
    entity: {
      id,
      name: undefined,
      code: undefined,
      status: undefined,
      isCrossEntity: undefined,
      remark: undefined,
    },
    statusLabel: undefined,
    userCount: undefined,
    orderCount: undefined,
    createTime: undefined,
  });

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY });
  }
  function goEdit() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY_EDIT, params: { id } });
  }

  async function load() {
    try {
      const res = await getAdBusinessEntityDetail(id);
      Object.assign(detail, res);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    }
  }

  onMounted(load);
</script>
