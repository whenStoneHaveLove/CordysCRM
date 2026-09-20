<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar :title="t('advertising.customer')" left-arrow @click-left="$router.back()">
      <template #right>
        <CrmIcon name="iconicon_filter" width="20px" height="20px" @click="filterVisible = true" />
        <van-icon
          v-if="hasPermission('AD_CUSTOMER:CREATE')"
          name="plus"
          class="ml-[14px]"
          @click="onCreate"
        />
      </template>
    </van-nav-bar>
    <van-search
      v-model="keyword"
      :placeholder="t('advertising.searchPlaceholder')"
      shape="round"
      @search="refresh"
      @clear="refresh"
    />
    <CrmList
      ref="listRef"
      class="flex-1"
      :load-list-api="getAdCustomerPage"
      :keyword="keyword"
      :list-params="listParams"
    >
      <template #item="{ item }">
        <div class="mb-[12px] rounded-lg bg-white px-[12px] py-[10px]" @click="onItemClick(item)">
          <div class="flex items-center justify-between gap-[8px]">
            <span class="one-line-text text-[14px] font-medium text-[var(--text-n1)]">
              {{ item.customerName || '-' }}
            </span>
            <CrmTag v-if="item.statusLabel" :tag="item.statusLabel" />
          </div>
          <div class="mt-[4px] one-line-text text-[12px] text-[var(--text-n3)]">
            {{ [item.industryLabel, item.customerLevelLabel].filter(Boolean).join(' · ') || '-' }}
          </div>
        </div>
      </template>
    </CrmList>

    <AdListFilter
      v-model:show="filterVisible"
      :fields="filterFields"
      :title="t('advertising.filter')"
      :reset-text="t('advertising.reset')"
      :confirm-text="t('advertising.confirm')"
      @confirm="onFilterConfirm"
    />
  </div>

</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { AdvertisingRouteEnum } from '@/enums/routeEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { AdCustomerLevelOptions, AdCustomerStatusOptions } from '@lib/shared/enums/advertisingEnum';
  import { hasPermission } from '@/utils/permission';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import CrmList from '@/components/pure/crm-list/index.vue';
  import CrmTag from '@/components/pure/crm-tag/index.vue';
  import { getAdCustomerPage } from '@/api/modules';
  import AdListFilter from '@/views/advertising/components/AdListFilter.vue';

  const { t } = useI18n();
  const router = useRouter();
  const keyword = ref('');
  const listRef = ref<{ loadList: (refresh?: boolean) => void }>();
  const filterVisible = ref(false);

  const searchForm = ref<Record<string, number | null>>({
    status: null,
    customerLevel: null,
  });

  const listParams = computed(() => ({
    status: searchForm.value.status ?? null,
    customerLevel: searchForm.value.customerLevel ?? null,
  }));

  const filterFields = [
    { key: 'status', label: t('advertising.field.status'), options: AdCustomerStatusOptions },
    { key: 'customerLevel', label: t('advertising.field.customerLevel'), options: AdCustomerLevelOptions },
  ];

  function refresh() {
    listRef.value?.loadList(true);
  }

  function onFilterConfirm(values: Record<string, number | null>) {
    searchForm.value = { ...searchForm.value, ...values };
    filterVisible.value = false;
    refresh();
  }

  function onItemClick(item: { id?: string }) {
    if (!item?.id) return;
    const name = String(router.currentRoute.value.name || '').replace(/List$/, 'Detail');
    router.push({ name, query: { id: item.id } });
  }

  function onCreate() {
    router.push({ name: AdvertisingRouteEnum.AD_CUSTOMER_FORM });
  }
</script>
