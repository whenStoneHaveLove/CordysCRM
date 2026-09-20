<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar :title="t('advertising.contract')" left-arrow @click-left="$router.back()">
      <template #right>
        <CrmIcon
          v-if="activeTab === 'contract'"
          name="iconicon_filter"
          width="20px"
          height="20px"
          @click="filterVisible = true"
        />
        <van-icon
          v-if="hasPermission('AD_CONTRACT:CREATE') && activeTab === 'contract'"
          name="plus"
          class="ml-[14px]"
          @click="onCreate"
        />
      </template>
    </van-nav-bar>
    <!-- 合同 / 已作废 双 tab，对齐 web 的 tabs（共用下方搜索与列表） -->
    <van-tabs v-model:active="activeTab" class="contract-tabs flex-shrink-0" @change="onTabChange">
      <van-tab v-for="tab of tabList" :key="tab.name" :name="tab.name">
        <template #title>
          <div class="text-[16px]" :class="activeTab === tab.name ? 'text-[var(--primary-8)]' : ''">
            {{ tab.title }}
          </div>
        </template>
      </van-tab>
    </van-tabs>
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
      :load-list-api="listApi"
      :keyword="keyword"
      :list-params="listParams"
    >
      <template #item="{ item }">
        <div class="mb-[12px] rounded-lg bg-white px-[12px] py-[10px]" @click="onItemClick(item)">
          <div class="flex items-center justify-between gap-[8px]">
            <span class="one-line-text min-w-0 flex-1 text-[14px] font-medium text-[var(--text-n1)]">
              {{ item.contractName || item.contractNo || '-' }}
            </span>
            <div class="flex shrink-0 items-center gap-[4px]">
              <CrmTag
                v-if="item.sealStatus != null"
                :tag="getAdSealStatusLabel(item.sealStatus)"
                v-bind="getAdSealStatusTagStyle(item.sealStatus)"
              />
              <CrmTag v-if="item.statusLabel" :tag="item.statusLabel" />
            </div>
          </div>
          <div class="mt-[4px] one-line-text text-[12px] text-[var(--text-n3)]">
            {{ [item.contractNo, item.businessEntityName, item.relatedPartyTypeLabel].filter(Boolean).join(' · ') || '-' }}
          </div>
          <div v-if="item.amount != null" class="mt-[2px] text-[13px] font-semibold text-[var(--text-n1)]">
            {{ fmtAmount(item.amount) }}
          </div>
          <!-- 已作废 tab：展示作废原因（对齐 web 已删除列表的作废原因列） -->
          <div v-if="isVoidedTab && item.voidReason" class="mt-[2px] one-line-text text-[12px] text-[var(--text-n3)]">
            {{ t('advertising.field.voidReason') }}：{{ item.voidReason }}
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
  import { computed, nextTick, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { AdvertisingRouteEnum } from '@/enums/routeEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import {
    AdContractDirectionOptions,
    AdContractTypeOptions,
    AdRelatedPartyTypeOptions,
    AdSealStatusOptions,
    getAdSealStatusLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { hasPermission } from '@/utils/permission';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import CrmList from '@/components/pure/crm-list/index.vue';
  import CrmTag from '@/components/pure/crm-tag/index.vue';
  import { getAdContractDeletedPage, getAdContractPage } from '@/api/modules';
  import AdListFilter from '@/views/advertising/components/AdListFilter.vue';
  import { fmtAmount, getAdSealStatusTagStyle } from '@/views/advertising/utils';
  import useAdListQueryFilter from '@/views/advertising/components/useListQueryFilter';

  type ContractTabName = 'contract' | 'voided';

  const { t } = useI18n();
  const router = useRouter();

  const keyword = ref('');
  const listRef = ref<{ loadList: (refresh?: boolean) => void }>();
  const filterVisible = ref(false);

  const activeTab = ref<ContractTabName>('contract');
  const isVoidedTab = computed(() => activeTab.value === 'voided');
  const tabList = [
    { name: 'contract' as ContractTabName, title: t('advertising.contract.tab.contract') },
    { name: 'voided' as ContractTabName, title: t('advertising.contract.tab.voided') },
  ];
  // 已作废走「已删除」分页接口（后端忽略 deleted 标记的列表），与 web 的 deleted tab 同源
  const listApi = computed(() => (isVoidedTab.value ? getAdContractDeletedPage : getAdContractPage));

  const searchForm = ref<Record<string, number | null>>({
    contractDirection: null,
    contractType: null,
    relatedPartyType: null,
    sealStatus: null,
  });

  const listParams = computed(() => ({
    contractDirection: searchForm.value.contractDirection ?? null,
    contractType: searchForm.value.contractType ?? null,
    relatedPartyType: searchForm.value.relatedPartyType ?? null,
    sealStatus: searchForm.value.sealStatus ?? null,
  }));

  // 工作台待办跳转携带 query 时，自动套用用印状态筛选（对齐 web）
  useAdListQueryFilter(searchForm, listRef, ['sealStatus']);

  const filterFields = [
    { key: 'contractDirection', label: t('advertising.field.direction'), options: AdContractDirectionOptions },
    { key: 'contractType', label: t('advertising.field.contractType'), options: AdContractTypeOptions },
    { key: 'relatedPartyType', label: t('advertising.field.relatedParty'), options: AdRelatedPartyTypeOptions },
    { key: 'sealStatus', label: t('advertising.field.sealStatus'), options: AdSealStatusOptions },
  ];

  function refresh() {
    listRef.value?.loadList(true);
  }

  function onFilterConfirm(values: Record<string, number | null>) {
    searchForm.value = { ...searchForm.value, ...values };
    filterVisible.value = false;
    refresh();
  }

  // 切 tab 后先等 load-list-api 更新到新的接口（prop 下发需一个 tick），再重拉列表
  async function onTabChange() {
    await nextTick();
    refresh();
  }

  function onItemClick(item: { id?: string }) {
    if (!item?.id) return;
    const name = String(router.currentRoute.value.name || '').replace(/List$/, 'Detail');
    router.push({ name, query: isVoidedTab.value ? { id: item.id, deleted: '1' } : { id: item.id } });
  }

  function onCreate() {
    router.push({ name: AdvertisingRouteEnum.AD_CONTRACT_FORM });
  }
</script>

<style lang="less" scoped>
  // tab 仅作为列表数据源切换器：搜索框与列表在 tabs 之外共享，隐藏空内容区
  .contract-tabs {
    :deep(.van-tabs__content) {
      @apply hidden;
    }
  }
</style>
