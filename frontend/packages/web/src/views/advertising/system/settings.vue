<template>
  <div class="advertising-page">
    <n-card :bordered="false">
      <div class="mb-[16px] text-[16px] font-semibold">{{ t('module.advertising.systemSettings') }}</div>
      <n-form :model="form" label-placement="left" :label-width="140" style="max-width: 480px">
        <n-form-item :label="t('advertising.system.settings.approvalSwitch')">
          <n-space vertical :size="4">
            <n-switch v-model:value="approvalOn" />
            <span class="text-[12px] text-gray-400">{{ t('advertising.system.settings.approvalSwitch.help') }}</span>
          </n-space>
        </n-form-item>
        <n-form-item :label="t('advertising.system.settings.accountPeriod')">
          <n-input-number v-model:value="form.defaultAccountPeriodDays" :min="0" :max="365" style="width: 100%" />
        </n-form-item>
        <n-space>
          <n-button type="primary" :loading="saving" @click="handleSave">{{
            t('advertising.system.settings.save')
          }}</n-button>
        </n-space>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue';
  import { NButton, NCard, NForm, NFormItem, NInputNumber, NSpace, NSwitch, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdSettingInfo } from '@lib/shared/models/advertising';

  import { getAdSetting, saveAdSetting } from '@/api/modules';

  const { t } = useI18n();
  const message = useMessage();

  const saving = ref(false);
  const form = reactive<AdSettingInfo>({
    approvalSwitch: 1,
    defaultAccountPeriodDays: 30,
  });

  const approvalOn = computed({
    get: () => form.approvalSwitch === 1,
    set: (v: boolean) => {
      form.approvalSwitch = v ? 1 : 0;
    },
  });

  async function load() {
    try {
      const res = await getAdSetting();
      if (res) {
        form.approvalSwitch = res.approvalSwitch ?? 1;
        form.defaultAccountPeriodDays = res.defaultAccountPeriodDays ?? 30;
      }
    } catch (e) {
      // 后端待补，加载失败不阻断页面渲染
      // eslint-disable-next-line no-console
      console.warn('ad setting not ready:', (e as Error).message);
    }
  }

  async function handleSave() {
    try {
      saving.value = true;
      await saveAdSetting({
        approvalSwitch: form.approvalSwitch ?? 1,
        defaultAccountPeriodDays: form.defaultAccountPeriodDays ?? 0,
      });
      message.success(t('advertising.common.saveSuccess'));
    } catch (e) {
      message.error((e as Error).message || '保存失败');
    } finally {
      saving.value = false;
    }
  }

  onMounted(load);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
</style>
