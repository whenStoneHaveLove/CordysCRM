<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="summary-card">
      <n-space justify="space-between" align="center">
        <n-statistic :label="t('advertising.workbench.todo.pendingApprove')" :value="pendingApproveCount" />
        <n-button text type="primary" @click="fetchData">{{ t('advertising.common.cancel') }}</n-button>
      </n-space>
    </n-card>

    <div class="todo-grid">
      <n-card :title="t('advertising.workbench.card.media')" :bordered="false" class="todo-card">
        <n-spin :show="loading">
          <n-empty v-if="!mediaTodos.length" :description="t('advertising.workbench.empty')" />
          <n-list v-else>
            <n-list-item v-for="(item, idx) in mediaTodos" :key="idx">
              <n-thing :title="item.todoLabel || item.todoType">
                <template #description>
                  <n-space :size="8" align="center">
                    <n-tag v-if="item.count != null" :bordered="false" type="warning">{{ item.count }}</n-tag>
                    <span v-if="item.amount != null" class="amount">{{ fmtAmount(item.amount) }}</span>
                    <span v-if="item.refNo" class="ref">#{{ item.refNo }}</span>
                  </n-space>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-spin>
      </n-card>

      <n-card :title="t('advertising.workbench.card.boss')" :bordered="false" class="todo-card">
        <n-spin :show="loading">
          <n-empty v-if="!bossTodos.length" :description="t('advertising.workbench.empty')" />
          <n-list v-else>
            <n-list-item v-for="(item, idx) in bossTodos" :key="idx">
              <n-thing :title="item.todoLabel || item.todoType">
                <template #description>
                  <n-space :size="8" align="center">
                    <n-tag v-if="item.count != null" :bordered="false" type="warning">{{ item.count }}</n-tag>
                    <span v-if="item.amount != null" class="amount">{{ fmtAmount(item.amount) }}</span>
                    <span v-if="item.refNo" class="ref">#{{ item.refNo }}</span>
                  </n-space>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-spin>
      </n-card>

      <n-card :title="t('advertising.workbench.card.finance')" :bordered="false" class="todo-card">
        <n-spin :show="loading">
          <n-empty v-if="!financeTodos.length" :description="t('advertising.workbench.empty')" />
          <n-list v-else>
            <n-list-item v-for="(item, idx) in financeTodos" :key="idx">
              <n-thing :title="item.todoLabel || item.todoType">
                <template #description>
                  <n-space :size="8" align="center">
                    <n-tag v-if="item.count != null" :bordered="false" type="warning">{{ item.count }}</n-tag>
                    <span v-if="item.amount != null" class="amount">{{ fmtAmount(item.amount) }}</span>
                    <span v-if="item.refNo" class="ref">#{{ item.refNo }}</span>
                  </n-space>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-spin>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import {
    NButton,
    NCard,
    NEmpty,
    NList,
    NListItem,
    NSpace,
    NSpin,
    NStatistic,
    NTag,
    NThing,
    useMessage,
  } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type {
    AdApprovalPendingCountItem,
    AdWorkbenchTodoItem,
    AdWorkbenchTodoResult,
  } from '@lib/shared/models/advertising';

  import { getAdApprovalPendingCount, getAdWorkbenchTodo } from '@/api/modules';

  import { fmtAmount } from '../utils';

  const { t } = useI18n();
  const message = useMessage();

  const loading = ref(false);
  const pendingApproveCount = ref(0);
  const mediaTodos = ref<AdWorkbenchTodoItem[]>([]);
  const bossTodos = ref<AdWorkbenchTodoItem[]>([]);
  const financeTodos = ref<AdWorkbenchTodoItem[]>([]);

  async function fetchData() {
    loading.value = true;
    try {
      const res = await getAdWorkbenchTodo();
      const data = (res || {}) as AdWorkbenchTodoResult;
      mediaTodos.value = data.media || [];
      bossTodos.value = data.boss || [];
      financeTodos.value = data.finance || [];
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }

    // 待我审批数字（复用真实端点 /approval-todo/pending/count）
    try {
      const countRes = await getAdApprovalPendingCount();
      if (Array.isArray(countRes)) {
        pendingApproveCount.value = (countRes as AdApprovalPendingCountItem[]).reduce(
          (sum, it) => sum + (it.count || 0),
          0
        );
      }
    } catch {
      /* 计数失败不影响主列表 */
    }
  }

  onMounted(fetchData);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .summary-card {
    margin-bottom: 16px;
  }
  .todo-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
  .todo-card {
    min-height: 200px;
  }
  .amount {
    font-weight: 600;
    color: #d03050;
  }
  .ref {
    color: #999999;
  }
</style>
