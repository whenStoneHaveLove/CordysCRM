<template>
  <div class="advertising-page">
    <!-- 问候栏 -->
    <div class="greeting-bar">
      <div class="greeting-left">
        <div class="greeting-title">{{ greeting }}{{ userName ? `，${userName}` : '' }}</div>
        <div class="greeting-date">{{ todayText }}</div>
      </div>
    </div>

    <!-- 数据概览统计卡 -->
    <div class="stat-grid">
      <div v-for="stat in statCards" :key="stat.key" class="stat-card">
        <div class="stat-icon" :style="{ background: stat.bg, color: stat.color }">
          <CrmIcon :type="stat.icon" :size="22" />
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value">{{ stat.value }}</div>
        </div>
      </div>
    </div>

    <!-- 待办区 -->
    <div class="todo-grid">
      <n-card v-for="card in todoCards" :key="card.role" :bordered="false" class="todo-card">
        <template #header>
          <div class="todo-card-header">
            <CrmIcon :type="card.icon" :size="18" class="todo-card-icon" />
            <span>{{ card.title }}</span>
          </div>
        </template>
        <n-spin :show="loading">
          <n-empty v-if="!card.items.length" :description="t('advertising.workbench.empty')" />
          <div v-else class="todo-item-grid">
            <div v-for="(item, idx) in card.items" :key="idx" class="todo-item-card" @click="go(item)">
              <div
                class="todo-item-icon"
                :style="{ background: todoColor(item.key).bg, color: todoColor(item.key).color }"
              >
                <CrmIcon :type="todoIcon(item.key)" :size="20" />
              </div>
              <div class="todo-item-meta">
                <div class="todo-item-label">{{ item.label }}</div>
                <div class="todo-item-count">{{ item.count != null ? item.count : 0 }}</div>
              </div>
            </div>
          </div>
        </n-spin>
      </n-card>
    </div>

    <!-- 月度趋势图 -->
    <n-card :bordered="false" class="trend-card">
      <template #header>
        <div class="todo-card-header">
          <CrmIcon type="iconicon_chart_pie" :size="18" class="todo-card-icon" />
          <span>{{ t('advertising.workbench.trend.title') }}</span>
        </div>
      </template>
      <div ref="trendRef" class="trend-chart"></div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
  import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { NCard, NEmpty, NSpin, useMessage } from 'naive-ui';
  import { BarChart, LineChart } from 'echarts/charts';
  import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
  import * as echarts from 'echarts/core';
  import { CanvasRenderer } from 'echarts/renderers';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type {
    AdDashboardSummary,
    AdDashboardTrendItem,
    AdWorkbenchTodoItem,
    AdWorkbenchTodoResult,
  } from '@lib/shared/models/advertising';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';

  import { getAdDashboardSummary, getAdWorkbenchTodo } from '@/api/modules';
  import useUserStore from '@/store/modules/user';

  import { fmtAmount } from '../utils';

  echarts.use([BarChart, LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

  const { t } = useI18n();
  const message = useMessage();
  const router = useRouter();
  const userStore = useUserStore();

  const userName = computed(() => userStore.userInfo?.name || '');

  const greeting = computed(() => {
    const h = new Date().getHours();
    if (h < 6) return t('advertising.workbench.greeting.evening');
    if (h < 12) return t('advertising.workbench.greeting.morning');
    if (h < 18) return t('advertising.workbench.greeting.afternoon');
    return t('advertising.workbench.greeting.evening');
  });

  const todayText = computed(() => {
    const d = new Date();
    const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()];
    return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${week}`;
  });

  function go(item: AdWorkbenchTodoItem) {
    if (item.link) {
      router.push(item.link);
    }
  }

  const loading = ref(false);
  const visibleRoles = ref<string[]>([]);
  const mediaTodos = ref<AdWorkbenchTodoItem[]>([]);
  const bossTodos = ref<AdWorkbenchTodoItem[]>([]);
  const financeTodos = ref<AdWorkbenchTodoItem[]>([]);

  // 统计卡数据
  const summary = ref<AdDashboardSummary>({});

  const statCards = computed(() => [
    {
      key: 'activeOrder',
      label: t('advertising.workbench.stat.activeOrder'),
      value: summary.value.activeOrderCount ?? 0,
      icon: 'iconicon_order_form',
      color: '#3370FF',
      bg: 'rgba(51, 112, 255, 0.12)',
    },
    {
      key: 'totalReceivable',
      label: t('advertising.workbench.stat.totalReceivable'),
      value: fmtAmount(summary.value.totalReceivable),
      icon: 'iconicon_wallet',
      color: '#00C261',
      bg: 'rgba(0, 194, 97, 0.12)',
    },
    {
      key: 'totalMediaPayable',
      label: t('advertising.workbench.stat.totalMediaPayable'),
      value: fmtAmount(summary.value.totalMediaPayable),
      icon: 'iconicon_creditcard',
      color: '#9175FF',
      bg: 'rgba(145, 117, 255, 0.12)',
    },
    {
      key: 'pendingReceivable',
      label: t('advertising.workbench.stat.pendingReceivable'),
      value: fmtAmount(summary.value.pendingReceivable),
      icon: 'iconicon_money_circle',
      color: '#FFA200',
      bg: 'rgba(255, 162, 0, 0.12)',
    },
    {
      key: 'pendingPayable',
      label: t('advertising.workbench.stat.pendingPayable'),
      value: fmtAmount(summary.value.pendingPayable),
      icon: 'iconicon_creditcard',
      color: '#E22E23',
      bg: 'rgba(226, 46, 35, 0.12)',
    },
  ]);

  // 待办项图标映射（按 key）
  function todoIcon(key?: string): string {
    const map: Record<string, string> = {
      pendingSubmit: 'iconicon_file',
      pendingExecute: 'iconicon_wait',
      changeExecute: 'iconicon_swap',
      payoutDraft: 'iconicon_send',
      sealApply: 'iconicon_edit',
      archiveSubmit: 'iconicon_folder',
      missingContract: 'iconicon_tips',
      pendingApprove: 'iconicon_check',
      changeApprove: 'iconicon_check',
      sealApprove: 'iconicon_check',
      archiveApprove: 'iconicon_folder_open',
      receiptApprove: 'iconicon_money_circle',
      payoutApprove: 'iconicon_money_circle',
      receiptDraft: 'iconicon_money_circle',
    };
    return map[key || ''] || 'iconicon_file';
  }

  // 待办项图标配色（按 key 映射到固定色，营造彩色图标方块）
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
    // 用 key 的字符码累加做稳定散列，保证同一 key 颜色固定
    let sum = 0;
    const k = key || '';
    for (let i = 0; i < k.length; i++) {
      sum += k.charCodeAt(i);
    }
    return COLOR_PALETTE[sum % COLOR_PALETTE.length];
  }

  // 分角色待办卡片（标题+图标+待办项），仅渲染有权限的角色
  const todoCards = computed(() => {
    const cards: { role: string; title: string; icon: string; items: AdWorkbenchTodoItem[] }[] = [];
    if (visibleRoles.value.includes('MEDIA')) {
      cards.push({
        role: 'MEDIA',
        title: t('advertising.workbench.card.media'),
        icon: 'iconicon_user_talk',
        items: mediaTodos.value,
      });
    }
    if (visibleRoles.value.includes('BOSS')) {
      cards.push({
        role: 'BOSS',
        title: t('advertising.workbench.card.boss'),
        icon: 'iconicon_check',
        items: bossTodos.value,
      });
    }
    if (visibleRoles.value.includes('FINANCE')) {
      cards.push({
        role: 'FINANCE',
        title: t('advertising.workbench.card.finance'),
        icon: 'iconicon_wallet',
        items: financeTodos.value,
      });
    }
    return cards;
  });

  // 趋势图
  const trendRef = ref<HTMLElement | null>(null);
  let chart: echarts.ECharts | null = null;
  let trendObserver: ResizeObserver | null = null;

  function renderTrend() {
    const trend: AdDashboardTrendItem[] = summary.value.monthlyTrend || [];
    const months = trend.map((it) => it.month || '');
    const counts = trend.map((it) => it.orderCount || 0);
    const amounts = trend.map((it) => Number(it.amount || 0));

    if (!trendRef.value) return;

    if (!chart) {
      chart = echarts.init(trendRef.value);
    }

    // 强制 resize 确保 echarts 拿到当前容器实际尺寸（防止初始化时拿到 0 宽度）
    chart.resize();

    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: {
        data: [t('advertising.workbench.trend.orderCount'), t('advertising.workbench.trend.amount')],
        top: 0,
      },
      grid: { left: 56, right: 56, top: 40, bottom: 60, containLabel: false },
      xAxis: {
        type: 'category',
        data: months,
        axisLabel: { interval: 0, fontSize: 11, rotate: 45, margin: 12 },
      },
      yAxis: [
        {
          type: 'value',
          name: t('advertising.workbench.trend.orderCount'),
          position: 'left',
        },
        {
          type: 'value',
          name: t('advertising.workbench.trend.amount'),
          position: 'right',
        },
      ],
      series: [
        {
          name: t('advertising.workbench.trend.orderCount'),
          type: 'bar',
          data: counts,
          itemStyle: { color: '#3370FF' },
          barMaxWidth: 24,
        },
        {
          name: t('advertising.workbench.trend.amount'),
          type: 'line',
          yAxisIndex: 1,
          smooth: true,
          data: amounts,
          itemStyle: { color: '#00C261' },
        },
      ],
    });
  }

  function handleResize() {
    chart?.resize();
  }

  async function fetchData() {
    loading.value = true;
    try {
      const [todoRes, summaryRes] = await Promise.allSettled([getAdWorkbenchTodo(), getAdDashboardSummary()]);

      if (todoRes.status === 'fulfilled') {
        const data = (todoRes.value || {}) as AdWorkbenchTodoResult;
        const todos = data.todos || {};
        visibleRoles.value = data.roles || [];
        mediaTodos.value = todos.MEDIA || [];
        bossTodos.value = todos.BOSS || [];
        financeTodos.value = todos.FINANCE || [];
      }

      if (summaryRes.status === 'fulfilled') {
        summary.value = (summaryRes.value || {}) as AdDashboardSummary;
      }
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
      // 延迟初始化：等 n-card body 完成布局、容器有真实尺寸后再 init echarts
      nextTick(() => {
        setTimeout(() => {
          renderTrend();
          if (trendRef.value && !trendObserver) {
            trendObserver = new ResizeObserver(() => {
              chart?.resize();
            });
            trendObserver.observe(trendRef.value);
          }
        }, 100);
      });
    }
  }

  onMounted(() => {
    fetchData();
    window.addEventListener('resize', handleResize);
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize);
    trendObserver?.disconnect();
    trendObserver = null;
    chart?.dispose();
    chart = null;
  });
</script>

<style scoped>
  .advertising-page {
    height: 100%;
    padding: 16px;
    overflow-y: auto;
  }

  .greeting-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }
  .greeting-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-n1);
  }
  .greeting-date {
    margin-top: 4px;
    font-size: 13px;
    color: var(--text-n3);
  }

  .stat-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 16px;
  }
  .stat-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px;
    border: 1px solid var(--text-n8);
    border-radius: 8px;
    background: #fff;
  }
  .stat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 46px;
    height: 46px;
    border-radius: 10px;
    flex-shrink: 0;
  }
  .stat-label {
    font-size: 13px;
    color: var(--text-n3);
    margin-bottom: 4px;
  }
  .stat-value {
    font-size: 22px;
    font-weight: 600;
    color: var(--text-n1);
    line-height: 1.2;
  }

  .todo-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 16px;
    margin-bottom: 16px;
  }
  .todo-card {
    min-height: 200px;
  }
  .todo-card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--text-n1);
  }
  .todo-card-icon {
    color: #3370ff;
  }
  .todo-item-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }
  .todo-item-card {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px;
    border-radius: 8px;
    background: var(--text-n8);
    cursor: pointer;
    transition: all 0.2s;
  }
  .todo-item-card:hover {
    background: var(--text-n7);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  }
  .todo-item-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 10px;
    flex-shrink: 0;
  }
  .todo-item-meta {
    min-width: 0;
  }
  .todo-item-label {
    font-size: 13px;
    color: var(--text-n2);
    line-height: 1.4;
    word-break: break-all;
  }
  .todo-item-count {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-n1);
    line-height: 1.2;
  }

  .trend-card {
    min-height: 320px;
  }
  .trend-chart {
    width: 100%;
    height: 320px;
  }
</style>
