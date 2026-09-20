<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <div class="flex-1 overflow-auto pb-[16px]">
      <!-- 问候栏（沿用 web 的问候 + 日期口径，去掉顶部标题栏） -->
      <div class="bg-gradient-to-b from-[var(--primary-6)] to-[var(--text-n9)] px-[16px] pb-[16px] pt-[20px]">
        <div class="flex items-center justify-between gap-[12px]">
          <div class="min-w-0">
            <div class="one-line-text text-[20px] font-semibold text-[var(--text-n1)]">
              {{ greeting }}<template v-if="userName">，{{ userName }}</template>
            </div>
            <div class="mt-[4px] text-[12px] text-[var(--text-n3)]">{{ todayText }}</div>
          </div>
          <div
            class="flex h-[44px] w-[44px] flex-shrink-0 items-center justify-center rounded-full bg-white text-[16px] font-semibold text-[var(--primary-8)] shadow-[0_2px_8px_rgba(50,53,53,0.08)]"
          >
            {{ avatarText }}
          </div>
        </div>
      </div>

      <!-- 数据概览统计卡 -->
      <div class="grid grid-cols-2 gap-[12px] px-[16px]">
        <div
          v-for="s of statCards"
          :key="s.key"
          class="flex items-center gap-[10px] rounded-[12px] bg-white p-[12px] shadow-[0_2px_10px_rgba(50,53,53,0.04)]"
        >
          <div
            class="flex h-[38px] w-[38px] flex-shrink-0 items-center justify-center rounded-[10px]"
            :style="{ background: s.bg }"
          >
            <CrmIcon :name="s.icon" width="20px" height="20px" :color="s.color" />
          </div>
          <div class="min-w-0">
            <div class="one-line-text text-[12px] text-[var(--text-n3)]">{{ s.label }}</div>
            <div class="one-line-text text-[17px] font-semibold tabular-nums text-[var(--text-n1)]">
              {{ s.value }}
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷入口（图标宫格，按模块 READ 权限过滤） -->
      <div v-if="entries.length" class="mx-[16px] mt-[12px] rounded-[12px] bg-white p-[12px]">
        <div class="mb-[10px] text-[15px] font-semibold text-[var(--text-n1)]">
          {{ t('advertising.quickEntry') }}
        </div>
        <div class="grid grid-cols-4 gap-y-[14px]">
          <div
            v-for="entry of entries"
            :key="entry.name"
            class="flex flex-col items-center gap-[6px] active:opacity-70"
            @click="goEntry(entry.name)"
          >
            <div
              class="flex h-[44px] w-[44px] items-center justify-center rounded-[14px]"
              :style="{ background: entry.bg }"
            >
              <CrmIcon :name="entry.icon" width="22px" height="22px" :color="entry.color" />
            </div>
            <span class="one-line-text max-w-full px-[2px] text-[11px] text-[var(--text-n2)]">
              {{ entry.label }}
            </span>
          </div>
        </div>
      </div>

      <!-- 分角色待办板块（MEDIA/BOSS/FINANCE，由后端按权限返回） -->
      <div
        v-for="section of roleSections"
        :key="section.role"
        class="mx-[16px] mt-[12px] rounded-[12px] bg-white p-[12px]"
      >
        <div class="mb-[10px] flex items-center gap-[6px]">
          <CrmIcon :name="section.icon" width="16px" height="16px" color="var(--primary-8)" />
          <span class="text-[15px] font-semibold text-[var(--text-n1)]">{{ section.title }}</span>
        </div>
        <van-empty v-if="!section.items.length" :description="t('advertising.noTodo')" image-size="80" />
        <div v-else class="grid grid-cols-2 gap-[10px]">
          <div
            v-for="item of section.items"
            :key="item.key"
            class="flex items-center gap-[8px] rounded-[10px] bg-[var(--text-n8)] p-[10px] active:opacity-70"
            @click="goLink(item)"
          >
            <div
              class="flex h-[32px] w-[32px] flex-shrink-0 items-center justify-center rounded-[9px]"
              :style="{ background: todoColor(item.key).bg }"
            >
              <CrmIcon :name="todoIcon(item.key)" width="17px" height="17px" :color="todoColor(item.key).color" />
            </div>
            <div class="min-w-0">
              <div class="one-line-text text-[12px] text-[var(--text-n2)]">{{ item.label }}</div>
              <div class="text-[16px] font-semibold tabular-nums text-[var(--text-n1)]">
                {{ item.count != null ? item.count : 0 }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 月度趋势（轻量柱图，无需额外依赖） -->
      <div class="mx-[16px] mt-[12px] rounded-[12px] bg-white p-[12px]">
        <div class="mb-[10px] flex items-center gap-[6px]">
          <CrmIcon name="iconicon_chart_pie" width="16px" height="16px" color="var(--primary-8)" />
          <span class="text-[15px] font-semibold text-[var(--text-n1)]">{{ t('advertising.trend.title') }}</span>
        </div>
        <van-empty v-if="!trend.length" :description="t('advertising.trend.empty')" image-size="80" />
        <div v-else class="flex h-[140px] items-end gap-[3px]">
          <div
            v-for="(it, idx) of trend"
            :key="idx"
            class="flex h-full flex-1 flex-col items-center justify-end"
          >
            <div class="text-[10px] tabular-nums text-[var(--text-n3)]">{{ it.orderCount }}</div>
            <div
              class="w-full rounded-t-[3px] bg-[var(--primary-8)] opacity-90"
              :style="{ height: trendHeight(it.orderCount), minHeight: '2px' }"
            />
            <div class="mt-[4px] text-[10px] text-[var(--text-n4)]">{{ (it.month || '').slice(-2) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import { getAdDashboardSummary, getAdWorkbenchTodo } from '@/api/modules';
  import { AdvertisingRouteEnum } from '@/enums/routeEnum';
  import useUserStore from '@/store/modules/user';
  import { hasPermission } from '@/utils/permission';

  import type {
    AdDashboardSummary,
    AdWorkbenchTodoItem,
    AdWorkbenchTodoResult,
  } from '@lib/shared/models/advertising';

  const { t } = useI18n();
  const router = useRouter();
  const userStore = useUserStore();

  const summary = ref<AdDashboardSummary>({});
  const roles = ref<string[]>([]);
  const todos = ref<Record<string, AdWorkbenchTodoItem[]>>({});

  const userName = computed(() => userStore.userInfo?.name || '');
  const avatarText = computed(() => (userName.value || '').slice(0, 1) || '广');

  const greeting = computed(() => {
    const h = new Date().getHours();
    if (h < 6 || h >= 18) return t('advertising.greeting.evening');
    if (h < 12) return t('advertising.greeting.morning');
    return t('advertising.greeting.afternoon');
  });

  const todayText = computed(() => {
    const d = new Date();
    const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()];
    return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${week}`;
  });

  const roleTitleMap: Record<string, string> = {
    MEDIA: t('advertising.roleMedia'),
    BOSS: t('advertising.roleBoss'),
    FINANCE: t('advertising.roleFinance'),
  };
  const roleIconMap: Record<string, string> = {
    MEDIA: 'iconicon_user_talk',
    BOSS: 'iconicon_check',
    FINANCE: 'iconicon_wallet',
  };

  const roleSections = computed(() =>
    (roles.value || []).map((role) => ({
      role,
      title: roleTitleMap[role] || role,
      icon: roleIconMap[role] || 'iconicon_file',
      items: (todos.value?.[role] as AdWorkbenchTodoItem[]) || [],
    }))
  );

  // 统计卡图标取自移动端 iconfont 中确实存在的字形（iconicon_order_form 等 web 专有字形在移动端缺失）
  const statCards = computed(() => [
    {
      key: 'activeOrder',
      label: t('advertising.activeOrderCount'),
      value: summary.value.activeOrderCount ?? 0,
      icon: 'iconicon_cart',
      color: '#3370FF',
      bg: 'rgba(51, 112, 255, 0.12)',
    },
    {
      key: 'totalReceivable',
      label: t('advertising.totalReceivable'),
      value: fmt(summary.value.totalReceivable),
      icon: 'iconicon_wallet',
      color: '#00C261',
      bg: 'rgba(0, 194, 97, 0.12)',
    },
    {
      key: 'totalMediaPayable',
      label: t('advertising.totalMediaPayable'),
      value: fmt(summary.value.totalMediaPayable),
      icon: 'iconicon_creditcard',
      color: '#9175FF',
      bg: 'rgba(145, 117, 255, 0.12)',
    },
    {
      key: 'pendingReceivable',
      label: t('advertising.pendingReceivable'),
      value: fmt(summary.value.pendingReceivable),
      icon: 'iconicon_money_circle',
      color: '#FFA200',
      bg: 'rgba(255, 162, 0, 0.12)',
    },
    {
      key: 'pendingPayable',
      label: t('advertising.pendingPayable'),
      value: fmt(summary.value.pendingPayable),
      icon: 'iconicon_creditcard',
      color: '#E22E23',
      bg: 'rgba(226, 46, 35, 0.12)',
    },
  ]);

  const trend = computed<NonNullable<AdDashboardSummary['monthlyTrend']>>(() => summary.value.monthlyTrend || []);
  const trendMax = computed(() => {
    const arr = trend.value || [];
    if (!arr.length) return 0;
    return Math.max(...arr.map((it) => Number(it.orderCount || 0)));
  });

  function trendHeight(count?: number): string {
    const max = trendMax.value || 1;
    return `${Math.max((Number(count || 0) / max) * 100, 2)}%`;
  }

  function fmt(v: number | undefined) {
    if (v == null) return '0';
    return v.toLocaleString();
  }

  // 后端 link 为 web 路由串，移动端按 key 映射到本端列表路由（带初始筛选 query，对齐 web 跳转）
  const TODO_ROUTE_MAP: Record<string, { name: string; query?: Record<string, string> }> = {
    pendingSubmit: { name: AdvertisingRouteEnum.AD_ORDER_INDEX, query: { status: '0' } },
    pendingExecute: { name: AdvertisingRouteEnum.AD_ORDER_INDEX, query: { status: '45' } },
    missingContract: { name: AdvertisingRouteEnum.AD_ORDER_INDEX, query: { missingContract: '1' } },
    pendingApprove: { name: AdvertisingRouteEnum.AD_ORDER_INDEX, query: { status: '10' } },
    changeExecute: { name: AdvertisingRouteEnum.AD_CHANGE_INDEX, query: { status: '20' } },
    changeApprove: { name: AdvertisingRouteEnum.AD_CHANGE_INDEX, query: { status: '10' } },
    payoutDraft: { name: AdvertisingRouteEnum.AD_PAYOUT_INDEX, query: { status: '0' } },
    payoutApprove: { name: AdvertisingRouteEnum.AD_PAYOUT_INDEX, query: { status: '10' } },
    payoutPending: { name: AdvertisingRouteEnum.AD_PAYOUT_INDEX, query: { status: '20' } },
    sealApply: { name: AdvertisingRouteEnum.AD_CONTRACT_INDEX, query: { sealStatus: '0' } },
    archiveSubmit: { name: AdvertisingRouteEnum.AD_CONTRACT_INDEX, query: { sealStatus: '20' } },
    archiveApprove: { name: AdvertisingRouteEnum.AD_CONTRACT_INDEX, query: { sealStatus: '40' } },
    sealApprove: { name: AdvertisingRouteEnum.AD_SEAL_INDEX, query: { status: '0' } },
    receiptApprove: { name: AdvertisingRouteEnum.AD_RECEIPT_INDEX, query: { status: '10' } },
    receiptDraft: { name: AdvertisingRouteEnum.AD_RECEIPT_INDEX, query: { status: '0' } },
  };

  // 待办项图标（对齐 web 的 todoIcon，字形统一换成移动端 iconfont 已有的）
  const TODO_ICON_MAP: Record<string, string> = {
    pendingSubmit: 'iconicon_file',
    pendingExecute: 'iconicon_hourglass',
    missingContract: 'iconicon_tips',
    pendingApprove: 'iconicon_check',
    changeExecute: 'iconicon_swap',
    changeApprove: 'iconicon_check',
    payoutDraft: 'iconicon_send_colorful',
    payoutApprove: 'iconicon_money_circle',
    payoutPending: 'iconicon_creditcard',
    sealApply: 'iconicon_edit',
    sealApprove: 'iconicon_check',
    archiveSubmit: 'iconicon_folder',
    archiveApprove: 'iconicon_folder_open',
    receiptApprove: 'iconicon_money_circle',
    receiptDraft: 'iconicon_money_circle',
  };

  function todoIcon(key?: string): string {
    return TODO_ICON_MAP[key || ''] || 'iconicon_file';
  }

  // 待办项图标配色（对齐 web：按 key 做稳定散列，保证同 key 颜色固定）
  const COLOR_PALETTE: { color: string; bg: string }[] = [
    { color: '#3370FF', bg: 'rgba(51, 112, 255, 0.12)' },
    { color: '#00C261', bg: 'rgba(0, 194, 97, 0.12)' },
    { color: '#FFA200', bg: 'rgba(255, 162, 0, 0.12)' },
    { color: '#9175FF', bg: 'rgba(145, 117, 255, 0.12)' },
    { color: '#E22E23', bg: 'rgba(226, 46, 35, 0.12)' },
    { color: '#00B8D9', bg: 'rgba(0, 184, 217, 0.12)' },
    { color: '#F0578B', bg: 'rgba(240, 87, 139, 0.12)' },
  ];

  function todoColor(key?: string): { color: string; bg: string } {
    let sum = 0;
    const k = key || '';
    for (let i = 0; i < k.length; i++) {
      sum += k.charCodeAt(i);
    }
    return COLOR_PALETTE[sum % COLOR_PALETTE.length];
  }

  function goLink(item: AdWorkbenchTodoItem) {
    const target = TODO_ROUTE_MAP[item?.key || ''];
    if (target) {
      router.push({ name: target.name, query: target.query });
    }
  }

  // 快捷入口：图标 + 配色 + 进入所需的模块 READ 权限（无权限不展示，避免点进去 403）
  const ALL_ENTRIES = [
    {
      label: t('advertising.order'),
      name: AdvertisingRouteEnum.AD_ORDER_INDEX,
      icon: 'iconicon_cart',
      color: '#3370FF',
      bg: 'rgba(51, 112, 255, 0.1)',
      permission: 'AD_ORDER:READ',
    },
    {
      label: t('advertising.approval'),
      name: AdvertisingRouteEnum.AD_APPROVAL_INDEX,
      icon: 'iconicon_check_circle',
      color: '#00C261',
      bg: 'rgba(0, 194, 97, 0.1)',
      permission: 'AD_APPROVAL:READ',
    },
    {
      label: t('advertising.contract'),
      name: AdvertisingRouteEnum.AD_CONTRACT_INDEX,
      icon: 'iconicon_books',
      color: '#9175FF',
      bg: 'rgba(145, 117, 255, 0.1)',
      permission: 'AD_CONTRACT:READ',
    },
    {
      label: t('advertising.customer'),
      name: AdvertisingRouteEnum.AD_CUSTOMER_INDEX,
      icon: 'iconicon_customer',
      color: '#FFA200',
      bg: 'rgba(255, 162, 0, 0.1)',
      permission: 'AD_CUSTOMER:READ',
    },
    {
      label: t('advertising.receipt'),
      name: AdvertisingRouteEnum.AD_RECEIPT_INDEX,
      icon: 'iconicon_money_circle',
      color: '#00C261',
      bg: 'rgba(0, 194, 97, 0.1)',
      permission: 'AD_RECEIPT:READ',
    },
    {
      label: t('advertising.payout'),
      name: AdvertisingRouteEnum.AD_PAYOUT_INDEX,
      icon: 'iconicon_creditcard',
      color: '#E22E23',
      bg: 'rgba(226, 46, 35, 0.1)',
      permission: 'AD_PAYOUT:READ',
    },
    {
      label: t('advertising.downstreamMedia'),
      name: AdvertisingRouteEnum.AD_DOWNSTREAM_MEDIA_INDEX,
      icon: 'iconicon_shop',
      color: '#00B8D9',
      bg: 'rgba(0, 184, 217, 0.1)',
      permission: 'AD_DOWNSTREAM_MEDIA:READ',
    },
    {
      label: t('advertising.upstreamAgent'),
      name: AdvertisingRouteEnum.AD_UPSTREAM_AGENT_INDEX,
      icon: 'iconicon_usergroup',
      color: '#F0578B',
      bg: 'rgba(240, 87, 139, 0.1)',
      permission: 'AD_UPSTREAM_AGENT:READ',
    },
    {
      label: t('advertising.businessEntity'),
      name: AdvertisingRouteEnum.AD_BUSINESS_ENTITY_INDEX,
      icon: 'iconicon_enterprise',
      color: '#3370FF',
      bg: 'rgba(51, 112, 255, 0.1)',
      permission: 'AD_BUSINESS_ENTITY:READ',
    },
    {
      label: t('advertising.seal'),
      name: AdvertisingRouteEnum.AD_SEAL_INDEX,
      icon: 'iconicon_handwritten_signature',
      color: '#9175FF',
      bg: 'rgba(145, 117, 255, 0.1)',
      permission: 'AD_SEAL:READ',
    },
    {
      label: t('advertising.change'),
      name: AdvertisingRouteEnum.AD_CHANGE_INDEX,
      icon: 'iconicon_swap',
      color: '#FFA200',
      bg: 'rgba(255, 162, 0, 0.1)',
      permission: 'AD_ORDER_CHANGE:READ',
    },
  ];

  const entries = computed(() => ALL_ENTRIES.filter((entry) => hasPermission(entry.permission)));

  function goEntry(name: string) {
    router.push({ name });
  }

  onMounted(async () => {
    try {
      const [todoRes, summaryRes] = await Promise.all([getAdWorkbenchTodo(), getAdDashboardSummary()]);
      const todoData = (todoRes || {}) as AdWorkbenchTodoResult;
      roles.value = todoData.roles || [];
      todos.value = todoData.todos || {};
      summary.value = (summaryRes || {}) as AdDashboardSummary;
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  });
</script>
