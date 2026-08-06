/**
 * 共享用户 ID → 名称 映射（广告模块详情页用于展示创建人/修改人）。
 * 首次调用时拉取全量用户选项并缓存，后续直接读缓存。
 */
import { ref } from 'vue';

import { getUserOptions } from '@/api/modules';

let pending: Promise<Map<string, string>> | null = null;
const userMap = ref<Map<string, string>>(new Map());

async function fetchUserMap(): Promise<Map<string, string>> {
  try {
    const list = (await getUserOptions()) as Array<{ id: string | number; name?: string }>;
    const map = new Map<string, string>();
    (list || []).forEach((u) => {
      if (u.id != null) map.set(String(u.id), u.name || String(u.id));
    });
    return map;
  } catch {
    return new Map<string, string>();
  }
}

export default function useUserMap() {
  async function loadUserMap() {
    if (!pending) {
      pending = fetchUserMap();
    }
    const map = await pending;
    userMap.value = map;
  }

  function getUserName(id?: string | null): string {
    if (id == null || id === '') return '-';
    return userMap.value.get(String(id)) || String(id);
  }

  return { userMap, loadUserMap, getUserName };
}
