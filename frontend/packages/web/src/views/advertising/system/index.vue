<template>
  <div class="advertising-page">
    <n-card :bordered="false">
      <div class="mb-[16px] text-[16px] font-semibold">{{ t('module.advertising.system') }}</div>
      <n-grid :cols="3" :x-gap="16" :y-gap="16" item-responsive>
        <n-gi v-for="entry in entries" :key="entry.key">
          <n-card hoverable @click="go(entry)">
            <div class="flex flex-col items-center justify-center py-[24px]">
              <div class="text-[15px] font-medium">{{ entry.label }}</div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>
    </n-card>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { NCard, NGi, NGrid } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const router = useRouter();

  interface SystemEntry {
    key: string;
    label: string;
    route: AdvertisingRouteEnum;
  }

  const entries: SystemEntry[] = [
    {
      key: 'be',
      label: t('advertising.system.entry.businessEntity'),
      route: AdvertisingRouteEnum.ADVERTISING_SYSTEM_BE,
    },
    { key: 'dict', label: t('advertising.system.entry.dict'), route: AdvertisingRouteEnum.ADVERTISING_SYSTEM_DICT },
    {
      key: 'settings',
      label: t('advertising.system.entry.settings'),
      route: AdvertisingRouteEnum.ADVERTISING_SYSTEM_SETTINGS,
    },
  ];

  function go(entry: SystemEntry) {
    router.push({ name: entry.route });
  }
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
</style>
