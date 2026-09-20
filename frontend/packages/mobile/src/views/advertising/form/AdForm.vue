<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar :title="t(isEdit ? config.titleEdit : config.titleCreate)" left-arrow @click-left="back" />

    <div class="flex-1 overflow-auto pb-[80px]">
      <div
        v-for="(group, gi) of config.groups"
        :key="gi"
        class="mx-[12px] mt-[12px] rounded-lg bg-white px-[12px] py-[4px]"
      >
        <div v-if="group.title" class="py-[6px] text-[14px] font-semibold text-[var(--text-n1)]">
          {{ t(group.title) }}
        </div>
        <template v-for="f of group.fields" :key="f.field">
          <van-field
            v-if="f.type === 'text'"
            v-model="model[f.field]"
            :label="t(f.label)"
            :placeholder="f.placeholder || `请输入${t(f.label)}`"
            :required="f.required"
          />
          <van-field
            v-else-if="f.type === 'number'"
            v-model="model[f.field]"
            type="number"
            :label="t(f.label)"
            :placeholder="f.placeholder || `请输入${t(f.label)}`"
          />
          <van-field
            v-else-if="f.type === 'textarea'"
            v-model="model[f.field]"
            type="textarea"
            rows="3"
            autosize
            :label="t(f.label)"
            :placeholder="f.placeholder || `请输入${t(f.label)}`"
          />
          <van-field
            v-else-if="f.type === 'date'"
            readonly
            is-link
            :label="t(f.label)"
            :model-value="fmtDate(model[f.field])"
            @click="openCalendar(f)"
          />
          <van-field
            v-else-if="f.type === 'select' || f.type === 'enum'"
            readonly
            is-link
            :label="t(f.label)"
            :model-value="labelOf(f)"
            :required="f.required"
            @click="openPicker(f)"
          />
          <van-field
            v-else-if="f.type === 'multi'"
            readonly
            is-link
            :label="t(f.label)"
            :model-value="labelOf(f)"
            @click="openMulti(f)"
          />
          <div
            v-else-if="f.type === 'switch'"
            class="flex items-center justify-between border-t border-[var(--text-n8)] py-[8px]"
          >
            <span class="text-[13px] text-[var(--text-n1)]">{{ t(f.label) }}</span>
            <van-switch :model-value="model[f.field] === 1" @change="onSwitchChange(f.field, $event)" />
          </div>
        </template>
      </div>

      <div
        v-if="hintText"
        class="mx-[12px] mt-[12px] rounded-lg bg-white px-[12px] py-[8px] text-[13px] text-[var(--text-n3)]"
      >
        {{ hintText }}
      </div>

      <!-- 附件（合同用印文件，临时上传，保存时由后端 reconcile） -->
      <div
        v-if="config.attachments"
        class="mx-[12px] mt-[12px] rounded-lg bg-white px-[12px] py-[4px]"
      >
        <div class="flex items-center justify-between py-[6px]">
          <span class="text-[14px] font-semibold text-[var(--text-n1)]">{{ t('advertising.form.attachmentTitle') }}</span>
          <van-button size="mini" @click="pickFile">{{ t('advertising.form.chooseFile') }}</van-button>
        </div>
        <div
          v-for="it in attachmentList"
          :key="it.key"
          class="flex items-center justify-between border-t border-[var(--text-n8)] py-[8px]"
        >
          <span class="one-line-text flex-1 text-[13px] text-[#1989fa]" @click="previewFile(it)">{{ it.name }}</span>
          <span class="ml-[8px] text-[12px] text-[var(--text-n3)]" @click="downloadFile(it)">{{ t('advertising.download') }}</span>
          <span class="ml-[8px] text-[12px] text-[#ee0a24]" @click="removeFile(it)">{{ t('advertising.form.remove') }}</span>
        </div>
        <input ref="fileInputRef" type="file" class="hidden" multiple @change="onNativeFileChange" />
      </div>
    </div>

    <!-- 底部保存 -->
    <div class="flex gap-[12px] border-t border-[var(--text-n8)] bg-white p-[12px]">
      <van-button block plain @click="back">{{ t('advertising.form.cancel') }}</van-button>
      <van-button block type="primary" :loading="submitting" @click="save">{{ t('advertising.form.save') }}</van-button>
    </div>

    <!-- 枚举 / 单选弹层 -->
    <van-popup v-model:show="pickerShow" position="bottom" round>
      <van-picker
        :title="pickerTitle"
        :columns="pickerColumns"
        :default-index="pickerDefaultIndex"
        show-toolbar
        @confirm="onPickerConfirm"
        @cancel="pickerShow = false"
      />
    </van-popup>

    <!-- 多选弹层 -->
    <van-popup v-model:show="multiShow" position="bottom" round>
      <div class="p-[12px]">
        <div class="mb-[8px] text-center text-[15px] font-semibold text-[var(--text-n1)]">
          {{ t(multiField?.label || '') }}
        </div>
        <van-checkbox-group v-model="multiTemp">
          <van-cell
            v-for="opt of multiColumns"
            :key="opt.value"
            :title="opt.text"
            clickable
            @click="toggleMulti(opt.value)"
          >
            <template #right-icon>
              <van-checkbox :name="opt.value" @click.stop />
            </template>
          </van-cell>
        </van-checkbox-group>
        <div class="mt-[12px] flex gap-[12px]">
          <van-button block plain @click="multiShow = false">{{ t('advertising.form.cancel') }}</van-button>
          <van-button block type="primary" @click="confirmMulti">{{ t('advertising.confirm') }}</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 日期选择 -->
    <van-calendar v-model:show="calendarShow" :title="calendarTitle" @confirm="onCalendarConfirm" />

    <!-- 附件 iframe 预览 -->
    <van-popup v-model:show="previewIframeVisible" position="bottom" round :style="{ height: '80%' }">
      <div class="flex h-full flex-col bg-white">
        <div class="flex items-center justify-between border-b border-[var(--text-n8)] p-[12px]">
          <span class="one-line-text flex-1 text-[14px] font-semibold text-[var(--text-n1)]">
            {{ previewIframeName || t('advertising.preview') }}
          </span>
          <span class="ml-[12px] flex-shrink-0 text-[13px] text-[#1989fa]" @click="downloadAttachment(previewIframeFileUrl)">
            {{ t('advertising.download') }}
          </span>
        </div>
        <iframe :src="previewIframeUrl" class="flex-1 w-full border-0 bg-white" />
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { showImagePreview, showSuccessToast, showToast } from 'vant';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import { downloadAttachment, previewAttachment, useAttachmentPreview } from '@/views/advertising/attachment';
  import { fmtDate } from '@/views/advertising/utils';
  import { AD_FORM_CONFIG, type AdFormField } from '@/views/advertising/form/config';

  const props = defineProps<{ moduleKey: string }>();

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  const config = computed(() => AD_FORM_CONFIG[props.moduleKey]);
  const id = (route.query.id as string) || '';
  const isEdit = computed(() => !!id);
  const submitting = ref(false);
  const hintText = computed(() => (config.value.hint ? config.value.hint(model, t) : ''));
  const model = reactive<Record<string, any>>({});

  const optionsMap = ref<Record<string, { text: any; value: any }[]>>({});

  const { previewIframeVisible, previewIframeUrl, previewIframeName, previewIframeFileUrl } = useAttachmentPreview();

  // —— 选项加载 ——
  async function loadOptionsFor(
    apiKey: string,
    api: (p: any) => Promise<any>,
    labelKey?: string,
    valueKey?: string,
    text?: (i: any) => string
  ) {
    try {
      const res: any = await api({ keyword: '', pageSize: 200, current: 1 });
      const list = res?.list || [];
      optionsMap.value[apiKey] = list.map((i: any) => ({
        text: text ? text(i) : (labelKey ? i[labelKey] : undefined) || i.id,
        value: valueKey ? i[valueKey] : i.id,
      }));
    } catch {
      optionsMap.value[apiKey] = [];
    }
  }

  async function loadStaticOptions() {
    Object.keys(config.value.selectApis || {}).forEach(async (key) => {
      const def = (config.value.selectApis || {})[key];
      if (def) await loadOptionsFor(key, def.api, def.labelKey, def.valueKey, def.text);
    });
  }

  // —— 动态下拉（合同关联方） ——
  async function loadDynamicOptions() {
    const dyn = config.value.dynamicOptions;
    if (!dyn) return;
    const dep = model[dyn.dependsOn];
    const def = dyn.resolver(dep);
    if (!def) {
      optionsMap.value[dyn.field] = [];
      return;
    }
    await loadOptionsFor(dyn.field, def.api, def.labelKey, def.valueKey, def.text);
  }

  // —— 标签回显 ——
  function fieldColumns(f: AdFormField): { text: any; value: any }[] {
    if (f.type === 'enum') return ((f.filter ? f.filter(model) : f.options) || []).map((o) => ({ text: o.label, value: o.value }));
    if (f.type === 'select' || f.type === 'multi') return optionsMap.value[f.apiKey!] || [];
    return [];
  }
  function labelOf(f: AdFormField): string {
    const v = model[f.field];
    if (f.type === 'multi') {
      const arr = Array.isArray(v) ? v : [];
      const opts = optionsMap.value[f.apiKey!] || [];
      return arr.map((id) => opts.find((o) => o.value === id)?.text).filter(Boolean).join('、') || '';
    }
    if (f.type === 'select') {
      const opts = optionsMap.value[f.apiKey!] || [];
      return opts.find((o) => o.value === v)?.text || '';
    }
    if (f.type === 'enum') {
      const opts = (f.filter ? f.filter(model) : f.options) || [];
      return opts.find((o) => o.value === v)?.label || '';
    }
    return '';
  }

  // —— 枚举 / 单选弹层 ——
  const pickerShow = ref(false);
  const pickerTitle = ref('');
  const pickerColumns = ref<{ text: any; value: any }[]>([]);
  const pickerDefaultIndex = ref(0);
  const pickerCallback = ref<(v: any) => void>(() => {});
  const pickerField = ref('');
  function openPicker(f: AdFormField) {
    pickerTitle.value = t(f.label);
    pickerColumns.value = fieldColumns(f);
    const idx = pickerColumns.value.findIndex((o) => o.value === model[f.field]);
    pickerDefaultIndex.value = idx >= 0 ? idx : 0;
    pickerCallback.value = (v) => (model[f.field] = v);
    pickerField.value = f.field;
    pickerShow.value = true;
  }
  function onPickerConfirm({ selectedOptions }: any) {
    const v = selectedOptions?.[0]?.value;
    pickerCallback.value(v);
    if (pickerField.value) config.value.afterSelect?.[pickerField.value]?.(v, model);
    pickerShow.value = false;
  }

  // —— 多选弹层 ——
  const multiShow = ref(false);
  const multiField = ref<AdFormField | null>(null);
  const multiTemp = ref<any[]>([]);
  const multiColumns = ref<{ text: any; value: any }[]>([]);
  function openMulti(f: AdFormField) {
    multiField.value = f;
    multiTemp.value = Array.isArray(model[f.field]) ? [...model[f.field]] : [];
    multiColumns.value = fieldColumns(f);
    multiShow.value = true;
  }
  function toggleMulti(value: any) {
    const idx = multiTemp.value.indexOf(value);
    if (idx === -1) multiTemp.value.push(value);
    else multiTemp.value.splice(idx, 1);
  }
  function confirmMulti() {
    if (multiField.value) {
      model[multiField.value.field] = [...multiTemp.value];
      config.value.afterSelect?.[multiField.value.field]?.(multiTemp.value, model);
    }
    multiShow.value = false;
  }

  // —— 日期弹层 ——
  const calendarShow = ref(false);
  const calendarTitle = ref('');
  const calendarCallback = ref<(ts: number) => void>(() => {});
  function openCalendar(f: AdFormField) {
    calendarTitle.value = t(f.label);
    calendarCallback.value = (ts) => (model[f.field] = ts);
    calendarShow.value = true;
  }
  function onCalendarConfirm(date: any) {
    calendarCallback.value(new Date(date).getTime());
    calendarShow.value = false;
  }

  // —— 附件（合同用印文件） ——
  const savedAttachments = ref<{ key: string; id: string; name: string; fileUrl?: string; saved: boolean }[]>([]);
  const pendingFiles = ref<{ key: string; id: string; name: string }[]>([]);
  const fileInputRef = ref<HTMLInputElement>();
  let seq = 0;
  const attachmentList = computed(() => [...savedAttachments.value, ...pendingFiles.value]);
  function pickFile() {
    fileInputRef.value?.click();
  }
  async function onNativeFileChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const files = input.files;
    if (!files || !files.length) return;
    Array.from(files).forEach(async (file) => {
      try {
        const res: any = await config.value.attachments!.uploadTemp(file);
        const fileId = res?.data?.[0] || res?.data || '';
        if (!fileId) {
          showToast('上传失败');
          return;
        }
        pendingFiles.value.push({ key: `f_${++seq}`, id: fileId, name: file.name });
      } catch {
        showToast('上传失败');
      }
    });
    input.value = '';
  }
  function removeFile(it: { key: string; saved: boolean }) {
    if (it.saved) savedAttachments.value = savedAttachments.value.filter((a) => a.key !== it.key);
    else pendingFiles.value = pendingFiles.value.filter((p) => p.key !== it.key);
  }
  function previewFile(it: { name: string; fileUrl?: string; saved: boolean }) {
    if (it.saved && it.fileUrl) previewAttachment(it.fileUrl, it.name);
    else showToast('保存后可预览');
  }
  function downloadFile(it: { fileUrl?: string; saved: boolean }) {
    if (it.saved && it.fileUrl) downloadAttachment(it.fileUrl);
  }

  function back() {
    router.back();
  }

  function onSwitchChange(field: string, v: boolean) {
    model[field] = v ? 1 : 0;
  }

  async function save() {
    const err = config.value.validate(model, t);
    if (err) {
      showToast(err);
      return;
    }
    let attachments;
    if (config.value.attachments) {
      const items = [
        ...savedAttachments.value.map((s) => ({ id: s.id, name: s.name })),
        ...pendingFiles.value.map((p) => ({ id: p.id, name: p.name })),
      ];
      attachments = config.value.attachments.buildSealFileUrls(items);
    }
    const payload: any = config.value.buildPayload(model, attachments);
    if (isEdit.value) payload.id = id;
    submitting.value = true;
    try {
      if (isEdit.value) await config.value.update(payload);
      else await config.value.create(payload);
      showSuccessToast(t('advertising.common.saveSuccess'));
      router.back();
    } finally {
      submitting.value = false;
    }
  }

  onMounted(async () => {
    await loadStaticOptions();
    if (isEdit.value) {
      const res: any = await config.value.fetchDetail(id);
      Object.assign(model, config.value.mapDetail(res));
      if (config.value.attachments) {
        savedAttachments.value = config.value.attachments.read(res).map((a) => ({
          key: `s_${a.id}`,
          id: a.id,
          name: a.name,
          fileUrl: a.fileUrl,
          saved: true,
        }));
      }
    }
    await loadDynamicOptions();
  });

  // 动态下拉依赖变化 → 重新加载
  if (config.value.dynamicOptions) {
    watch(
      () => model[config.value.dynamicOptions!.dependsOn],
      () => loadDynamicOptions()
    );
  }
  // 父字段变化 → 清空关联字段
  (config.value.resetOn || []).forEach((rule) => {
    watch(
      () => model[rule.field],
      () => {
        rule.clear.forEach((c) => (model[c] = undefined));
      }
    );
  });
</script>
