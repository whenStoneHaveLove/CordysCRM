<template>
  <ComingSoon v-if="COMING_SOON" />
  <Suspense v-else>
    <RouterView />
  </Suspense>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';
  import { showLoadingToast } from 'vant';

  import useLocale from '@lib/shared/locale/useLocale';
  import { getQueryVariable } from '@lib/shared/method';
  import { hasToken } from '@lib/shared/method/auth';
  import { LocaleType } from '@lib/shared/types/global';

  import useLicenseStore from '@/store/modules/setting/license';
  import useUserStore from '@/store/modules/user';

  import { getHomeRouteName } from '@/utils/permission';

  import useLogin from './hooks/useLogin';
  import ComingSoon from '@/views/base/coming-soon/index.vue';

  // 上线开关：true=展示「敬请期待」占位页（暂不对外）；上线时改为 false 即可，无需改动其它代码
  const COMING_SOON = true;

  const router = useRouter();
  const userStore = useUserStore();
  const { oAuthLogin } = useLogin();
  const licenseStore = useLicenseStore();
  const { changeLocale } = useLocale(showLoadingToast);

  onBeforeMount(async () => {
    if (COMING_SOON) return; // 占位页模式：跳过登录/重定向，仅展示「敬请期待」
    changeLocale(navigator.language as LocaleType);
    const loginStatus = await userStore.isLogin(true);

    const ua = navigator.userAgent.toLowerCase();
    const isWXWork = ua.includes('wxwork');

    const isDingTalk =
      ua.includes('dingtalk') ||
      ua.includes('aliapp(dingtalk') ||
      (getQueryVariable('authCode') !== '' &&
        getQueryVariable('authCode') !== undefined &&
        getQueryVariable('authCode') !== null);

    const isLark = ua.includes('feishu') || ua.includes('lark') || getQueryVariable('state') === 'LARK';

    if (!loginStatus && !hasToken() && (isWXWork || isDingTalk || isLark)) {
      await oAuthLogin();
      return;
    }
    router.replace({ name: getHomeRouteName() });
    licenseStore.getValidateLicense();
  });
</script>

<style lang="less" scoped></style>
