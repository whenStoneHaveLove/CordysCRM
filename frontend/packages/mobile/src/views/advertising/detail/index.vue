<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar left-arrow @click-left="$router.back()">
      <template #title>
        <span>{{ t(config.title) }}</span>
        <van-tag v-if="isDeleted" type="danger" plain class="ml-[4px]">
          {{ t('advertising.contract.tab.voided') }}
        </van-tag>
      </template>
      <template #right>
        <span
          v-if="canEditRow"
          class="px-[4px] text-[14px] text-[#1989fa]"
          @click="onEdit"
          >{{ t('advertising.form.edit') }}</span
        >
      </template>
    </van-nav-bar>
    <div class="flex-1 overflow-auto pb-[16px]">
      <div v-if="loading" class="flex justify-center py-[40px]">
        <van-loading />
      </div>
      <van-empty v-else-if="!detail" :description="t('common.noData')" />
      <template v-else>
        <div
          v-for="(section, index) of sections"
          :key="index"
          class="mx-[12px] mt-[12px] rounded-lg bg-white px-[12px] py-[6px]"
        >
          <div v-if="section.title" class="py-[6px] text-[14px] font-semibold text-[var(--text-n1)]">
            {{ section.title }}
          </div>
          <div
            v-for="row of section.rows"
            :key="row.label"
            class="flex gap-[12px] border-t border-[var(--text-n8)] py-[7px] text-[13px]"
            @click="row.to && onRowLink(row)"
          >
            <span class="w-[92px] flex-shrink-0 text-[var(--text-n3)]">{{ row.label }}</span>
            <span
              v-if="row.label === t('advertising.field.voucher') && row.value && row.value !== '-'"
              class="flex-1 break-all text-[13px] text-[#1989fa]"
              @click.stop="previewAttachment(String(row.value))"
            >
              {{ t('advertising.preview') }}
            </span>
            <template v-else-if="row.to">
              <span class="flex-1 break-all text-[13px] text-[#1989fa]">{{ row.value ?? '-' }}</span>
              <span class="flex-shrink-0 text-[16px] text-[var(--text-n4)]">›</span>
            </template>
            <span v-else class="flex-1 break-all text-[var(--text-n1)]">{{ row.value ?? '-' }}</span>
          </div>
          <div v-if="section.compareRows && section.compareRows.length">
            <div
              v-for="c of section.compareRows"
              :key="c.label"
              class="flex gap-[12px] border-t border-[var(--text-n8)] py-[7px] text-[13px]"
            >
              <span class="w-[92px] flex-shrink-0 text-[var(--text-n3)]">{{ c.label }}</span>
              <div class="flex-1 break-all">
                <div class="text-[12px] text-[var(--text-n3)]">
                  {{ t('advertising.change.detail.before') }} {{ c.before }}
                </div>
                <div class="text-[12px] text-[#18a058]">
                  {{ t('advertising.change.detail.after') }} {{ c.after }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 子表区块（附件 / 记录列表） -->
        <template v-for="block of extraBlocks" :key="block.title">
          <div class="mx-[12px] mt-[12px] rounded-lg bg-white px-[12px] py-[6px]">
            <div class="py-[6px] text-[14px] font-semibold text-[var(--text-n1)]">{{ block.title }}</div>
            <template v-if="block.kind === 'attachment'">
              <div
                v-for="(it, i) of block.items"
                :key="i"
                class="flex items-center justify-between gap-[8px] border-t border-[var(--text-n8)] py-[8px]"
              >
                <div class="min-w-0 flex-1">
                  <div
                    class="one-line-text text-[13px] text-[#1989fa]"
                    @click="previewAttachment(it.fileUrl, it.fileName)"
                  >
                    {{ it.fileName || it.fileUrl || '-' }}
                  </div>
                  <div v-if="it.sub" class="mt-[2px] text-[12px] text-[var(--text-n3)]">{{ it.sub }}</div>
                </div>
                <span
                  class="flex-shrink-0 text-[12px] text-[var(--text-n3)]"
                  @click="downloadAttachment(it.fileUrl)"
                >
                  {{ t('advertising.download') }}
                </span>
              </div>
            </template>
            <template v-else>
              <div
                v-for="(it, i) of block.items"
                :key="i"
                class="flex items-center justify-between gap-[8px] border-t border-[var(--text-n8)] py-[8px]"
                @click="it.to && router.push({ name: it.to.name, query: it.to.query })"
              >
                <div class="min-w-0 flex-1">
                  <div class="one-line-text text-[13px] text-[var(--text-n1)]">{{ it.primary }}</div>
                  <div v-if="it.secondary" class="mt-[2px] one-line-text text-[12px] text-[var(--text-n3)]">
                    {{ it.secondary }}
                  </div>
                  <div v-if="it.tertiary" class="mt-[2px] one-line-text text-[12px] text-[var(--text-n3)]">
                    {{ it.tertiary }}
                  </div>
                </div>
                <CrmTag v-if="it.tag" class="flex-shrink-0" :tag="it.tag" v-bind="it.tagStyle" />
                <span v-if="it.to" class="flex-shrink-0 text-[16px] text-[var(--text-n4)]">›</span>
              </div>
            </template>
          </div>
        </template>
      </template>
    </div>

    <!-- 附件 iframe 预览（非图片类） -->
    <van-popup v-model:show="previewIframeVisible" position="bottom" round :style="{ height: '80%' }">
      <div class="flex h-full flex-col bg-white">
        <div class="flex items-center justify-between border-b border-[var(--text-n8)] p-[12px]">
          <span class="one-line-text flex-1 text-[14px] font-semibold text-[var(--text-n1)]">
            {{ previewIframeName || t('advertising.preview') }}
          </span>
          <span
            class="ml-[12px] flex-shrink-0 text-[13px] text-[#1989fa]"
            @click="downloadAttachment(previewIframeFileUrl)"
          >
            {{ t('advertising.download') }}
          </span>
        </div>
        <iframe :src="previewIframeUrl" class="flex-1 w-full border-0 bg-white" />
      </div>
    </van-popup>

    <!-- 流程动作栏（权限 + 状态门控，对齐 web） -->
    <div
      v-if="actionList.length || uploadBlock"
      class="flex gap-[10px] border-t border-[var(--text-n8)] bg-white px-[12px] py-[8px] shadow-[0_-2px_10px_rgba(50,53,53,0.05)]"
    >
      <!-- 附件管理入口（合同「上传双盖附件」，对齐 web 顶部按钮） -->
      <van-button
        v-if="uploadBlock"
        type="default"
        size="small"
        block
        class="flex-1 !px-[6px]"
        @click="attachmentPopupShow = true"
      >
        {{ uploadLabel }}
      </van-button>
      <van-button
        v-for="a of actionList"
        :key="a.key"
        :type="a.type || 'primary'"
        size="small"
        block
        class="flex-1 !px-[6px]"
        :loading="actionLoading"
        @click="onActionClick(a)"
        >{{ t(a.labelKey) }}</van-button
      >
    </div>

    <!-- 附件管理弹层（上传 / 删除双盖附件，对齐 web 的「上传双盖附件」） -->
    <van-popup v-model:show="attachmentPopupShow" position="bottom" round :style="{ height: '60%' }">
      <div class="flex h-full flex-col bg-white">
        <div class="flex items-center justify-between gap-[12px] border-b border-[var(--text-n8)] p-[12px]">
          <span class="one-line-text min-w-0 flex-1 text-[15px] font-semibold text-[var(--text-n1)]">{{
            uploadTitle
          }}</span>
          <van-uploader :accept="uploadAccept" :preview-image="false" :after-read="onUploadFile" class="flex-shrink-0">
            <van-button type="primary" size="small" :loading="uploading">{{ uploadLabel }}</van-button>
          </van-uploader>
        </div>
        <div class="flex-1 overflow-auto px-[12px]">
          <div
            v-for="(it, i) of managedItems"
            :key="i"
            class="flex items-center justify-between gap-[8px] border-b border-[var(--text-n8)] py-[10px]"
          >
            <span
              class="one-line-text min-w-0 flex-1 text-[13px] text-[#1989fa]"
              @click="previewAttachment(it.fileUrl, it.fileName)"
            >
              {{ it.fileName || '-' }}
            </span>
            <span class="flex-shrink-0 text-[12px] text-[#d03050]" @click="onDeleteAttachment(it)">
              {{ t('advertising.form.remove') }}
            </span>
          </div>
          <van-empty v-if="!managedItems.length" :description="t('common.noData')" />
        </div>
      </div>
    </van-popup>

    <!-- 审批意见 / 原因 弹层 -->
    <van-popup v-model:show="actionPopupShow" position="bottom" round :style="{ height: '42%' }">
      <div class="flex h-full flex-col p-[12px]">
        <div class="mb-[8px] text-[15px] font-semibold text-[var(--text-n1)]">
          {{ t(actionTarget?.labelKey || '') }}
        </div>
        <van-field
          v-model="actionRemark"
          type="textarea"
          rows="3"
          :placeholder="t('advertising.detail.remarkPlaceholder')"
        />
        <van-button type="primary" block class="mt-auto" :loading="actionLoading" @click="confirmAction">
          {{ t('advertising.confirm') }}
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { showFailToast, showSuccessToast, showToast } from 'vant';

  import CrmTag from '@/components/pure/crm-tag/index.vue';
  import { downloadAttachment, previewAttachment, useAttachmentPreview } from '@/views/advertising/attachment';

  import {
    type AdDetailExtraAttachmentBlock,
    type AdDetailExtraAttachmentItem,
    getAdDetailConfig,
  } from '@/views/advertising/detail/config';
  import { AD_MODULE_CONFIG, type AdActionDef } from '@/views/advertising/moduleConfig';
  import { loadUserMap } from '@/views/advertising/useUserMap';
  import { hasPermission } from '@/utils/permission';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  const config = computed(() => getAdDetailConfig(String(route.name)));
  const detail = ref<any>(null);
  const loading = ref(true);

  const id = computed(() => String(route.query.id || ''));
  const editRoute = computed(() => config.value.editRoute);
  // 已作废（列表「已作废」tab 进入）：只读查看，不提供编辑与流程动作
  const isDeleted = computed(() => route.query.deleted === '1');

  // 模块级配置：权限 + 状态 → 编辑/动作
  const moduleCfg = computed(() => AD_MODULE_CONFIG[String(route.name)] || null);

  // 编辑：需有编辑路由 + 编辑权限 + 满足状态条件（对齐 web）
  const canEditRow = computed(() => {
    const cfg = moduleCfg.value;
    if (isDeleted.value) return false;
    if (!cfg || !cfg.updateCode || !editRoute.value) return false;
    return !!cfg.canEdit(detail.value) && hasPermission(cfg.updateCode);
  });

  // 流程动作栏：按权限 + 状态过滤
  const actionList = computed<AdActionDef[]>(() => {
    const cfg = moduleCfg.value;
    if (!cfg || isDeleted.value) return [];
    return cfg.actions.filter((a) => a.show(detail.value) && hasPermission(a.permission));
  });

  function onEdit() {
    if (editRoute.value && id.value) router.push({ name: editRoute.value, query: { id: id.value } });
  }

  /** 主区块关联字段（如订单编号/合同编号）点击跳详情，对齐 web 超链接 */
  function onRowLink(row: { to?: { name: string; query: Record<string, string> } }) {
    if (row.to) router.push({ name: row.to.name, query: row.to.query });
  }

  const sections = computed(() => (detail.value ? config.value.build(detail.value, t) : []));

  const { previewIframeVisible, previewIframeUrl, previewIframeName, previewIframeFileUrl } =
    useAttachmentPreview();

  const extraBlocks = computed(() =>
    detail.value && config.value.extra ? config.value.extra(detail.value, t) : []
  );

  // 附件管理（合同双盖附件）：区块自带 uploader 时动作栏出现上传入口，并按 deletable 管理附件
  const attachmentPopupShow = ref(false);
  const uploading = ref(false);
  const deleting = ref(false);
  const uploadBlock = computed<AdDetailExtraAttachmentBlock | null>(() => {
    // 已作废合同只读：不出现「上传双盖附件」入口
    if (isDeleted.value) return null;
    const block = extraBlocks.value.find(
      (it): it is AdDetailExtraAttachmentBlock => it.kind === 'attachment' && !!it.uploader
    );
    return block || null;
  });
  const uploadTitle = computed(() => uploadBlock.value?.uploader?.title || '');
  const uploadLabel = computed(() => uploadBlock.value?.uploader?.label || '');
  const uploadAccept = computed(() => uploadBlock.value?.uploader?.accept || '');
  const managedItems = computed(() => (uploadBlock.value?.items || []).filter((it) => it.deletable));

  /** 拉取详情（流程动作 / 附件变更后刷新） */
  async function loadDetail() {
    const cur = String(route.query.id || '');
    if (!cur) return;
    detail.value = await config.value.fetch(cur, { deleted: isDeleted.value });
    if (config.value.preload) await config.value.preload(detail.value);
  }

  async function onUploadFile(file: any) {
    const up = uploadBlock.value?.uploader;
    if (!up || uploading.value) return;
    uploading.value = true;
    try {
      await up.onUpload(file?.file as File);
      showSuccessToast(t('advertising.common.operateSuccess'));
      await loadDetail();
    } catch (e) {
      showFailToast((e as Error).message || t('advertising.common.operateFailed'));
    } finally {
      uploading.value = false;
    }
  }

  async function onDeleteAttachment(it: AdDetailExtraAttachmentItem) {
    const del = uploadBlock.value?.onDelete;
    if (!del || deleting.value) return;
    deleting.value = true;
    try {
      await del(it);
      showSuccessToast(t('advertising.common.operateSuccess'));
      await loadDetail();
    } catch {
      // 错误提示由接口拦截器统一处理
    } finally {
      deleting.value = false;
    }
  }

  // 流程动作：需要备注/原因的先弹层收集，否则直接执行
  const actionLoading = ref(false);
  const actionPopupShow = ref(false);
  const actionTarget = ref<AdActionDef | null>(null);
  const actionRemark = ref('');

  function onActionClick(a: AdActionDef) {
    // 前置校验（如「提交归档审批」需先上传双盖附件），返回 i18n key 即拦截
    const blocked = a.guard?.(detail.value);
    if (blocked) {
      showToast(t(blocked));
      return;
    }
    actionTarget.value = a;
    if (a.input) {
      actionRemark.value = '';
      actionPopupShow.value = true;
    } else {
      runAction(a);
    }
  }

  async function confirmAction() {
    if (!actionTarget.value) return;
    await runAction(actionTarget.value, actionRemark.value);
    actionPopupShow.value = false;
  }

  async function runAction(a: AdActionDef, value?: string) {
    if (actionLoading.value) return;
    actionLoading.value = true;
    try {
      await a.run(String(id.value), value || undefined);
      showSuccessToast(t('advertising.common.operateSuccess'));
      await loadDetail();
    } catch {
      // 错误提示由接口拦截器统一处理
    } finally {
      actionLoading.value = false;
    }
  }

  onMounted(async () => {
    // 预加载用户映射，创建人/审批人/付款人由 ID 渲染为姓名（加载完成后自动重算）
    loadUserMap();
    if (!String(route.query.id || '')) {
      loading.value = false;
      return;
    }
    try {
      await loadDetail();
    } finally {
      loading.value = false;
    }
  });
</script>
