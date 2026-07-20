<template>
  <n-menu
    v-model:value="activeMenu"
    class="crm-top-menu"
    mode="horizontal"
    :options="topMenuList"
    :node-props="getNodeProps"
    responsive
    @update:value="handleSelected"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { RouteRecordName, RouteRecordRaw, useRouter } from 'vue-router';
  import { MenuGroupOption, MenuOption, NMenu } from 'naive-ui';
  import { debounce } from 'lodash-es';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { listenerRouteChange } from '@lib/shared/method/route-listener';

  import usePermission from '@/hooks/usePermission';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  const { t } = useI18n();
  const permission = usePermission();

  const appStore = useAppStore();

  const router = useRouter();

  const topMenuList = computed<MenuOption[]>(() => {
    return appStore.getTopMenus
      .map((e: any) => {
        return {
          key: e.name,
          label: t(e?.meta?.locale ?? ''),
          hasPermission: hasAnyPermission(e?.meta?.permissions),
        };
      })
      .filter((item) => item.hasPermission);
  });

  const activeMenu: Ref<string | null> = ref('');

  function getNodeProps(option: MenuOption | MenuGroupOption) {
    return {
      class: `${option.key === activeMenu.value ? 'crm-top-menu-selected-item' : ''}`,
    };
  }

  function checkAuthMenu() {
    const topMenus = appStore.getTopMenus;
    appStore.setTopMenus(topMenus);
  }

  watch(
    () => appStore.getCurrentTopMenu?.name,
    (val) => {
      checkAuthMenu();
      activeMenu.value = val as string;
    },
    {
      immediate: true,
    }
  );

  function setCurrentTopMenu(key: string) {
    // 先判断全等，避免同级路由出现命名包含情况
    const secParentFullSame = appStore.topMenus.find((route: RouteRecordRaw) => {
      return key === route?.name;
    });

    // 非全等的情况下，一定是父子路由包含关系
    const secParentLike = appStore.topMenus.find((route: RouteRecordRaw) => {
      return key.includes(route?.name as string);
    });

    if (secParentFullSame) {
      appStore.setCurrentTopMenu(secParentFullSame);
    } else if (secParentLike) {
      appStore.setCurrentTopMenu(secParentLike);
    }
  }

  const handleSelected = debounce((route: RouteRecordName | undefined) => {
    router.push({ name: route });
  }, 150);

  /**
   * 监听路由变化，存储打开的顶部菜单
   * 通过路由匹配链（matched）定位包含 isTopMenu 子节点的父级路由，
   * 兼容多个并列父模块（如广告拆分为订单/合同/资源等独立父模块）的场景。
   */
  listenerRouteChange((newRoute) => {
    const { name, matched } = newRoute;
    const currentParent = matched?.find((r) => r.children?.some((c) => c.meta?.isTopMenu));
    const filterMenuTopRouter = currentParent?.children?.filter((item) => item.meta?.isTopMenu) || [];
    appStore.setTopMenus(filterMenuTopRouter);
    setCurrentTopMenu(name as string);
  }, true);
</script>

<style lang="less">
  .crm-top-menu {
    &.n-menu {
      .n-menu-item {
        height: 32px !important;
        &.crm-top-menu-selected-item {
          border-radius: --border-radius-small;
          background: var(--primary-7);
        }
        .n-menu-item-content {
          padding: 0 16px;
        }
      }
    }
  }
</style>
