/**
 * 共享用户 ID → 名称 映射（广告模块详情页用于展示创建人 / 审批人 / 付款人）。
 * 首次调用时拉取全量用户选项并缓存，后续直接读缓存。口径与 web 端 useUserMap 一致。
 */
import { ref } from 'vue';

import { getUserOptions } from '@/api/modules';

let pending: Promise<Map<string, string>> | null = null;
const userMap = ref<Map<string, string>>(new Map());

async function fetchUserMap(): Promise<Map<string, string>> {
  try {
    const list = (await getUserOptions()) as unknown as Array<{ id: string | number; name?: string }>;
    const map = new Map<string, string>();
    (list || []).forEach((u) => {
      if (u.id != null) map.set(String(u.id), u.name || String(u.id));
    });
    return map;
  } catch {
    return new Map<string, string>();
  }
}

/** 预加载用户映射（幂等，多次调用只请求一次） */
export async function loadUserMap(): Promise<void> {
  if (!pending) {
    pending = fetchUserMap();
  }
  userMap.value = await pending;
}

/** 用户 ID → 名称；空值返回 '-'（与详情区块空值口径一致） */
export function getUserName(id?: string | number | null): string {
  if (id == null || id === '') return '-';
  return userMap.value.get(String(id)) || String(id);
}

export default function useUserMap() {
  return { userMap, loadUserMap, getUserName };
}
