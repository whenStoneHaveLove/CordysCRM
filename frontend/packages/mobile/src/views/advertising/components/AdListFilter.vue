<template>
  <van-popup v-model:show="visible" position="bottom" round :style="{ maxHeight: '82%' }">
    <div class="flex flex-col p-[16px]">
      <div class="mb-[8px] text-center text-[16px] font-semibold text-[var(--text-n1)]">{{ title }}</div>
      <van-field
        v-for="f of fields"
        :key="f.key"
        readonly
        is-link
        :label="f.label"
        :model-value="displayOf(f)"
        @click="active = f"
      />
      <div class="mt-[20px] flex gap-[12px]">
        <van-button block plain @click="reset">{{ resetText }}</van-button>
        <van-button block type="primary" @click="confirm">{{ confirmText }}</van-button>
      </div>
    </div>
    <van-action-sheet v-model:show="pickerVisible" :actions="actions" @select="onSelect" />
  </van-popup>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';

  interface FilterField {
    key: string;
    label: string;
    options: { label: string; value: number }[];
  }

  const props = defineProps<{
    fields: FilterField[];
    title?: string;
    resetText?: string;
    confirmText?: string;
  }>();

  const emit = defineEmits<{
    (e: 'confirm', values: Record<string, number | null>): void;
  }>();

  const visible = defineModel<boolean>('show', { default: false });

  const selected = ref<Record<string, number | null>>({});
  const active = ref<FilterField | null>(null);
  const pickerVisible = ref(false);

  const actions = computed(() => [
    { name: '全部', value: null },
    ...(active.value?.options || []).map((o) => ({ name: o.label, value: o.value })),
  ]);

  function displayOf(f: FilterField): string {
    const v = selected.value[f.key];
    if (v === null || v === undefined) return '全部';
    return f.options.find((o) => o.value === v)?.label ?? '全部';
  }

  function onSelect(action: { name: string; value: number | null }) {
    if (active.value) selected.value[active.value.key] = action.value ?? null;
    pickerVisible.value = false;
  }

  function reset() {
    selected.value = {};
    props.fields.forEach((f) => (selected.value[f.key] = null));
  }

  function confirm() {
    emit('confirm', { ...selected.value });
    visible.value = false;
  }
</script>
