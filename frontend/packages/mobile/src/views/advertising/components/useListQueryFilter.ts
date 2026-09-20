/**
 * 广告列表页「路由 query 初始筛选」。
 * 工作台待办跳转携带 status / sealStatus / missingContract 等 query，
 * 进入列表页时按 query 自动套用筛选并刷新，对齐 web 端行为。
 *
 * 仅套用 query 中存在的键，不会清空用户手动设置的其它筛选；
 * 列表页被缓存时仍可经 watch(route.query) 生效。
 */
import { watch } from 'vue';
import { useRoute } from 'vue-router';

import type { Ref } from 'vue';

type ListRef = { loadList: (refresh?: boolean) => void } | undefined;

export default function useAdListQueryFilter(
  searchForm: Ref<Record<string, number | null>>,
  listRef: Ref<ListRef>,
  keys: string[]
): void {
  const route = useRoute();

  function apply() {
    const q = route.query;
    let changed = false;
    keys.forEach((key) => {
      const raw = q[key];
      if (raw === undefined || raw === null || raw === '') return;
      const num = Number(raw);
      if (Number.isNaN(num)) return;
      if (searchForm.value[key] !== num) {
        searchForm.value[key] = num;
        changed = true;
      }
    });
    if (changed) listRef.value?.loadList(true);
  }

  watch(() => route.query, apply, { immediate: true });
}
