<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar
      :title="t(isEdit ? 'advertising.order.edit' : 'advertising.order.create')"
      left-arrow
      @click-left="back"
    />

    <div class="flex-1 overflow-auto pb-[80px]">
      <!-- 配置驱动的基础字段区块 -->
      <div
        v-for="(group, gi) of groups"
        :key="gi"
        class="mx-[12px] mt-[12px] rounded-lg bg-white px-[12px] py-[4px]"
      >
        <div class="py-[6px] text-[14px] font-semibold text-[var(--text-n1)]">{{ group.title }}</div>
        <template v-for="f of group.fields.filter((f) => !f.visibleWhen || f.visibleWhen(model))" :key="f.field">
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
            :readonly="f.readonlyWhen ? f.readonlyWhen(model) : false"
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
            @click="openCalendar((ts) => (model[f.field] = ts))"
          />
          <van-field
            v-else-if="f.type === 'select' || f.type === 'enum'"
            readonly
            is-link
            :label="t(f.label)"
            :model-value="labelOf(f)"
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
          <van-field v-else-if="f.type === 'computed'" readonly :label="t(f.label)">
            <template #input>
              <span class="text-[14px] font-semibold text-[var(--van-primary-color)]">
                {{ f.compute ? f.compute(model) : '' }}
              </span>
            </template>
          </van-field>
        </template>
      </div>

      <!-- 6. 付款返点明细（下游客户）：选择下游客户后自动带出 -->
      <div
        v-if="(model.downstreamMediaIds || []).length"
        class="mx-[12px] mt-[12px] rounded-lg bg-white px-[12px] py-[4px]"
      >
        <div class="py-[6px] text-[14px] font-semibold text-[var(--text-n1)]">
          {{ t('advertising.order.form.payableTitle') }}
        </div>
        <div
          v-for="(item, idx) of payables"
          :key="item.downstreamMediaId"
          class="mt-[8px] rounded border border-[var(--text-n8)] p-[10px]"
        >
          <div class="mb-[6px] flex items-center gap-[6px] text-[13px] font-semibold text-[var(--text-n1)]">
            <span class="inline-flex h-[20px] w-[20px] items-center justify-center rounded-full bg-[#1989fa] text-[12px] text-white">{{ idx + 1 }}</span>
            <span>{{ downstreamMediaName(item.downstreamMediaId) }}</span>
          </div>
          <van-field v-model="item.payableAmount" type="number" :label="t('advertising.order.payableAmount')" placeholder="请输入应付金额" />
          <van-field v-model="item.noRebateAmount" type="number" :label="t('advertising.order.noRebateAmount')" placeholder="请输入不记返金额" />
          <van-field
            readonly
            is-link
            :label="t('advertising.order.rebateMode')"
            :model-value="enumLabel(AdModeOptions, item.rebateMode)"
            @click="openEnumPicker(t('advertising.order.rebateMode'), AdModeOptions, item.rebateMode, (v) => (item.rebateMode = v))"
          />
          <van-field v-model="item.rebateValue" type="number" :label="t('advertising.order.rebateValue')" placeholder="比例(%)或固定金额" />
          <div class="flex justify-between px-[16px] py-[6px] text-[12px] text-[var(--text-n3)]">
            <span>{{ t('advertising.order.rebateAmount') }}：{{ fmtAmount(calcRebate(item)) }}</span>
            <span>{{ t('advertising.order.actualPayable') }}：{{ fmtAmount(calcActual(item)) }}</span>
          </div>

          <van-field
            readonly
            is-link
            :label="t('advertising.order.paymentMethod')"
            :model-value="enumLabel(AdPaymentMethodOptions, item.paymentMethod)"
            @click="openEnumPicker(t('advertising.order.paymentMethod'), AdPaymentMethodOptions, item.paymentMethod, (v) => onPaymentMethodChange(item, v))"
          />
          <template v-if="item.paymentMethod === 10">
            <van-field
              readonly
              is-link
              :label="t('advertising.order.paymentPrepayMode')"
              :model-value="enumLabel(AdModeOptions, item.paymentPrepayMode)"
              @click="openEnumPicker(t('advertising.order.paymentPrepayMode'), AdModeOptions, item.paymentPrepayMode, (v) => (item.paymentPrepayMode = v))"
            />
            <van-field
              v-if="item.paymentPrepayMode === 10"
              v-model="item.paymentPrepayRatio"
              type="number"
              :label="t('advertising.order.form.paymentPrepayRatio')"
              placeholder="预付比例(%)"
            />
            <van-field
              v-else-if="item.paymentPrepayMode === 20"
              v-model="item.paymentPrepayRatio"
              type="number"
              :label="t('advertising.order.paymentPrepayAmount')"
              placeholder="预付金额"
            />
            <van-field
              readonly
              is-link
              :label="t('advertising.order.paymentPrepayDeadline')"
              :model-value="fmtDate(item.paymentPrepayDeadline)"
              @click="openCalendar((ts) => (item.paymentPrepayDeadline = ts))"
            />
            <van-field
              v-if="item.paymentPrepayMode === 10"
              :label="t('advertising.order.paymentPrepayAmount')"
              :model-value="fmtAmount(item.paymentPrepayAmount)"
              readonly
            />
          </template>
          <template v-else-if="item.paymentMethod === 20">
            <van-field
              readonly
              is-link
              :label="t('advertising.order.postpayTrigger')"
              :model-value="enumLabel(AdPostpayTriggerOptions, item.paymentPostpayTrigger)"
              @click="openEnumPicker(t('advertising.order.postpayTrigger'), AdPostpayTriggerOptions, item.paymentPostpayTrigger, (v) => onPostpayTriggerChange(item, v))"
            />
            <van-field
              v-if="item.paymentPostpayTrigger === 20"
              v-model="item.paymentPostpayDays"
              type="number"
              :label="t('advertising.order.postpayDays')"
              placeholder="后付天数"
            />
          </template>
        </div>
        <div class="mt-[8px] rounded bg-[var(--text-n8)] px-[12px] py-[8px]">
          <div class="flex justify-between text-[13px]">
            <span class="text-[var(--text-n3)]">{{ t('advertising.order.form.mediaPayableAmountTotal') }}</span>
            <span class="font-semibold text-[var(--van-primary-color)]">{{ fmtAmount(Number(payableTotal)) }}</span>
          </div>
          <div class="mt-[4px] flex justify-between text-[13px]">
            <span class="text-[var(--text-n3)]">{{ t('advertising.order.form.actualPayableTotal') }}</span>
            <span class="font-semibold text-[var(--van-primary-color)]">{{ fmtAmount(Number(actualTotal)) }}</span>
          </div>
        </div>
      </div>

      <!-- 7. 附件与合同 -->
      <div class="mx-[12px] mt-[12px] rounded-lg bg-white px-[12px] py-[4px]">
        <div class="py-[6px] text-[14px] font-semibold text-[var(--text-n1)]">
          {{ t('advertising.order.form.attachments') }}
        </div>
        <van-tabs v-model:active="attachTab">
          <van-tab title="普通附件" name="normal" />
          <van-tab title="邮件记录(eml)" name="eml" />
        </van-tabs>

        <template v-if="attachTab === 'normal'">
          <div class="mt-[6px]">
            <div class="flex items-center justify-between py-[4px]">
              <span class="text-[13px] text-[var(--text-n2)]">{{ scheduleLabel }}</span>
              <van-button size="mini" @click="pickFile(10)">{{ t('advertising.order.form.chooseFile') }}</van-button>
            </div>
            <div
              v-for="it in filesForType(10)"
              :key="it.id"
              class="flex items-center justify-between border-b border-[var(--text-n8)] py-[6px] last:border-b-0"
            >
              <span class="one-line-text flex-1 text-[13px] text-[#1989fa]" @click="previewFile(it)">{{ it.name }}</span>
              <span class="ml-[8px] text-[12px] text-[var(--text-n3)]" @click="downloadFile(it)">{{ t('advertising.download') }}</span>
              <span class="ml-[8px] text-[12px] text-[#ee0a24]" @click="removeFile(it)">{{ t('advertising.order.form.remove') }}</span>
            </div>
          </div>
          <div class="mt-[6px]">
            <div class="flex items-center justify-between py-[4px]">
              <span class="text-[13px] text-[var(--text-n2)]">{{ emailLabel }}</span>
              <van-button size="mini" @click="pickFile(20)">{{ t('advertising.order.form.chooseFile') }}</van-button>
            </div>
            <div
              v-for="it in filesForType(20)"
              :key="it.id"
              class="flex items-center justify-between border-b border-[var(--text-n8)] py-[6px] last:border-b-0"
            >
              <span class="one-line-text flex-1 text-[13px] text-[#1989fa]" @click="previewFile(it)">{{ it.name }}</span>
              <span class="ml-[8px] text-[12px] text-[var(--text-n3)]" @click="downloadFile(it)">{{ t('advertising.download') }}</span>
              <span class="ml-[8px] text-[12px] text-[#ee0a24]" @click="removeFile(it)">{{ t('advertising.order.form.remove') }}</span>
            </div>
          </div>
          <div class="mt-[6px]">
            <div class="flex items-center justify-between py-[4px]">
              <span class="text-[13px] text-[var(--text-n2)]">{{ t('advertising.order.form.attachSupplement') }}</span>
              <van-button size="mini" @click="pickFile(40)">{{ t('advertising.order.form.chooseFile') }}</van-button>
            </div>
            <div
              v-for="it in filesForType(40)"
              :key="it.id"
              class="flex items-center justify-between border-b border-[var(--text-n8)] py-[6px] last:border-b-0"
            >
              <span class="one-line-text flex-1 text-[13px] text-[#1989fa]" @click="previewFile(it)">{{ it.name }}</span>
              <span class="ml-[8px] text-[12px] text-[var(--text-n3)]" @click="downloadFile(it)">{{ t('advertising.download') }}</span>
              <span class="ml-[8px] text-[12px] text-[#ee0a24]" @click="removeFile(it)">{{ t('advertising.order.form.remove') }}</span>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="mt-[6px]">
            <div class="flex items-center justify-between py-[4px]">
              <span class="text-[13px] text-[var(--text-n2)]">{{ t('advertising.order.form.attachEml') }}</span>
              <van-button size="mini" @click="pickFile(60)">{{ t('advertising.order.form.chooseFile') }}</van-button>
            </div>
            <div
              v-for="it in filesForType(60)"
              :key="it.id"
              class="flex items-center justify-between border-b border-[var(--text-n8)] py-[6px] last:border-b-0"
            >
              <span class="one-line-text flex-1 text-[13px] text-[#1989fa]" @click="previewFile(it)">{{ it.name }}</span>
              <span class="ml-[8px] text-[12px] text-[var(--text-n3)]" @click="downloadFile(it)">{{ t('advertising.download') }}</span>
              <span class="ml-[8px] text-[12px] text-[#ee0a24]" @click="removeFile(it)">{{ t('advertising.order.form.remove') }}</span>
            </div>
            <div class="mt-[4px] text-[12px] text-[var(--text-n4)]">{{ t('advertising.order.form.emlTip') }}</div>
          </div>
        </template>

        <input
          ref="fileInputRef"
          type="file"
          class="hidden"
          :multiple="attachTab !== 'eml'"
          :accept="acceptFor(attachTab)"
          @change="onNativeFileChange"
        />
      </div>
    </div>

    <!-- 底部保存 -->
    <div class="flex gap-[12px] border-t border-[var(--text-n8)] bg-white p-[12px]">
      <van-button block plain @click="back">{{ t('advertising.order.cancel') }}</van-button>
      <van-button block type="primary" :loading="submitting" @click="save">
        {{ t('advertising.order.save') }}
      </van-button>
    </div>

    <!-- 通用枚举/单选弹层 -->
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

    <!-- 多选弹层（下游客户） -->
    <van-popup v-model:show="multiShow" position="bottom" round>
      <div class="p-[12px]">
        <div class="mb-[8px] text-center text-[15px] font-semibold text-[var(--text-n1)]">
          {{ multiField?.label && t(multiField.label) }}
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
          <van-button block plain @click="multiShow = false">{{ t('advertising.order.cancel') }}</van-button>
          <van-button block type="primary" @click="confirmMulti">{{ t('advertising.order.confirm') }}</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 日期选择 -->
    <van-calendar v-model:show="calendarShow" :title="calendarTitle" @confirm="onCalendarConfirm" />

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
  </div>
</template>

<script setup lang="ts">
  import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { showImagePreview, showSuccessToast, showToast } from 'vant';
 
  import {
    AdModeOptions,
    AdOrderTypeOptions,
    AdPaymentMethodOptions,
    AdPostpayTriggerOptions,
    AdReceiptMethodOptions,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderSaveParams } from '@lib/shared/models/advertising';

  import {
    createAdOrder,
    deleteAdOrderAttachment,
    getAdBusinessEntityPage,
    getAdContractPage,
    getAdCustomerPage,
    getAdDictPage,
    getAdDownstreamMediaPage,
    getAdOrderDetail,
    getAdUpstreamAgentPage,
    updateAdOrder,
    uploadAdOrderAttachment,
  } from '@/api/modules';
  import {
    downloadAttachment,
    previewAttachment,
    useAttachmentPreview,
  } from '@/views/advertising/attachment';
  import { fmtAmount, fmtDate } from '@/views/advertising/utils';

  interface FieldDef {
    field: string;
    label: string; // i18n key
    type: 'text' | 'number' | 'textarea' | 'date' | 'select' | 'enum' | 'multi' | 'computed';
    required?: boolean;
    options?: { label: string; value: number }[]; // enum
    apiKey?: string; // select / multi
    visibleWhen?: (m: Record<string, any>) => boolean;
    readonlyWhen?: (m: Record<string, any>) => boolean;
    /** computed 类型：只读计算项展示值（对齐 web 只读计算项） */
    compute?: (m: Record<string, any>) => string;
  }
  interface GroupDef {
    title: string;
    fields: FieldDef[];
  }
  interface PayableItem {
    downstreamMediaId: string;
    payableAmount?: number | null;
    noRebateAmount?: number | null;
    rebateMode?: number | null;
    rebateValue?: number | null;
    paymentMethod?: number | null;
    paymentPrepayMode?: number | null;
    paymentPrepayRatio?: number | null;
    paymentPrepayAmount?: number | null;
    paymentPrepayDeadline?: number | null;
    paymentPostpayTrigger?: number | null;
    paymentPostpayDays?: number | null;
  }
  interface FileItem {
    id: string;
    name: string;
    saved: boolean;
    fileUrl?: string;
    file?: File;
  }
  interface SavedAttach {
    id: string;
    type: number;
    fileName: string;
    fileUrl: string;
  }
  interface PendingFile {
    _key: string;
    type: number;
    file: File;
  }

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  const isEdit = computed(() => !!route.query.id);
  const submitting = ref(false);
  const model = reactive<Record<string, any>>({});

  const optionsMap = ref<Record<string, { text: string; value: any }[]>>({});

  // 下拉选项文案口径完全对齐 web（web 端 order/create.vue）：
  // 客户 customerName||name；上游 resourceName||name；下游 resourceName||name；合同 合同编号+名称
  const selectApis: Record<string, {
    api: (p: any) => Promise<any>;
    text: (item: any) => string;
    value?: (item: any) => any;
  }> = {
    businessEntityId: { api: getAdBusinessEntityPage, text: (i) => i.name || i.id },
    customerId: { api: getAdCustomerPage, text: (i) => i.customerName || i.name || i.id },
    contractId: {
      api: getAdContractPage,
      text: (i) => [i.contractNo, i.contractName].filter(Boolean).join(' ') || i.id,
    },
    upstreamAgentId: { api: getAdUpstreamAgentPage, text: (i) => i.resourceName || i.name || i.id },
    downstreamMediaIds: { api: getAdDownstreamMediaPage, text: (i) => i.resourceName || i.name || i.id },
    // 行业类别：字典下拉，值用 dictValue（与 web 端 order/create.vue 一致，非 id）
    industry: {
      api: (p) => getAdDictPage({ ...p, dictCode: 'industry' }),
      text: (i) => i.dictLabel || i.dictValue,
      value: (i) => i.dictValue || i.id,
    },
  };

  const groups: GroupDef[] = [
    {
      title: t('advertising.order.detail.base'),
      fields: [
        { field: 'orderName', label: 'advertising.orderName', type: 'text', required: true },
        { field: 'businessEntityId', label: 'advertising.order.businessEntity', type: 'select', required: true, apiKey: 'businessEntityId' },
        { field: 'customerId', label: 'advertising.customer', type: 'select', required: true, apiKey: 'customerId' },
        { field: 'industryCode', label: 'advertising.order.industry', type: 'select', apiKey: 'industry' },
        { field: 'signingEntity', label: 'advertising.order.signingEntity', type: 'text' },
        { field: 'orderType', label: 'advertising.order.orderType', type: 'enum', options: AdOrderTypeOptions },
        { field: 'contractId', label: 'advertising.order.contract', type: 'select', apiKey: 'contractId' },
        { field: 'upstreamAgentId', label: 'advertising.order.upstreamAgent', type: 'select', apiKey: 'upstreamAgentId' },
        { field: 'agentOrderNo', label: 'advertising.order.agentOrderNo', type: 'text' },
        { field: 'downstreamMediaIds', label: 'advertising.order.downstream', type: 'multi', apiKey: 'downstreamMediaIds' },
        { field: 'currency', label: 'advertising.order.currency', type: 'text' },
      ],
    },
    {
      title: t('advertising.order.detail.delivery'),
      fields: [
        { field: 'deliveryStartDate', label: 'advertising.order.deliveryStart', type: 'date' },
        { field: 'deliveryEndDate', label: 'advertising.order.deliveryEnd', type: 'date' },
        { field: 'deliveryVolume', label: 'advertising.order.deliveryVolume', type: 'text' },
      ],
    },
    {
      title: t('advertising.order.detail.amount'),
      fields: [
        { field: 'totalAmount', label: 'advertising.order.totalAmount', type: 'number' },
        { field: 'rebateMode', label: 'advertising.order.rebateMode', type: 'enum', options: AdModeOptions },
        { field: 'rebateValue', label: 'advertising.order.rebateValue', type: 'number' },
        { field: 'noRebateAmount', label: 'advertising.order.noRebateAmount', type: 'number' },
        // 只读计算项（对齐 web 金额与返点区块）：返点金额(自动) / 实际应收
        {
          field: 'rebateAmountAuto',
          label: 'advertising.order.form.rebateAmountAuto',
          type: 'computed',
          compute: (m) => fmtAmount(autoCalcOf(m).rebateAmount),
        },
        {
          field: 'receivableAmountAuto',
          label: 'advertising.order.receivableAmount',
          type: 'computed',
          compute: (m) => fmtAmount(autoCalcOf(m).receivableAmount),
        },
      ],
    },
    {
      title: t('advertising.order.detail.receipt'),
      fields: [
        { field: 'receiptMethod', label: 'advertising.order.receiptMethod', type: 'enum', options: AdReceiptMethodOptions },
        {
          field: 'receiptPrepayMode',
          label: 'advertising.order.receiptPrepayMode',
          type: 'enum',
          options: AdModeOptions,
          visibleWhen: (m) => m.receiptMethod === 10,
        },
        {
          field: 'receiptPrepayRatio',
          label: 'advertising.order.receiptPrepayRatio',
          type: 'number',
          visibleWhen: (m) => m.receiptMethod === 10 && m.receiptPrepayMode === 10,
        },
        {
          field: 'receiptPrepayAmount',
          label: 'advertising.order.receiptPrepayAmount',
          type: 'number',
          visibleWhen: (m) => m.receiptMethod === 10,
          readonlyWhen: (m) => m.receiptPrepayMode === 10,
        },
        {
          field: 'receiptPrepayDeadline',
          label: 'advertising.order.receiptPrepayDeadline',
          type: 'date',
          visibleWhen: (m) => m.receiptMethod === 10,
        },
        {
          field: 'receiptAccountPeriodDays',
          label: 'advertising.order.receiptAccountPeriodDays',
          type: 'number',
          visibleWhen: (m) => m.receiptMethod === 20,
        },
      ],
    },
    {
      title: t('advertising.order.detail.other'),
      fields: [{ field: 'remark', label: 'advertising.order.remark', type: 'textarea' }],
    },
  ];

  const NUMBER_FIELDS = [
    'totalAmount',
    'rebateValue',
    'noRebateAmount',
    'receiptPrepayRatio',
    'receiptPrepayAmount',
    'receiptAccountPeriodDays',
  ];
  const SAVE_KEYS = [
    'orderName',
    'businessEntityId',
    'customerId',
    'industryCode',
    'signingEntity',
    'orderType',
    'contractId',
    'upstreamAgentId',
    'agentOrderNo',
    'totalAmount',
    'rebateMode',
    'rebateValue',
    'noRebateAmount',
    'deliveryStartDate',
    'deliveryEndDate',
    'deliveryVolume',
    'remark',
    'receiptMethod',
    'receiptPrepayMode',
    'receiptPrepayRatio',
    'receiptPrepayAmount',
    'receiptPrepayDeadline',
    'receiptAccountPeriodDays',
    'currency',
  ];

  async function loadOptions() {
    for (const key of Object.keys(selectApis)) {
      try {
        const { api, text, value } = selectApis[key];
        const res: any = await api({ keyword: '', pageSize: 200, current: 1 });
        const list = res?.list || [];
        optionsMap.value[key] = list.map((i: any) => ({ text: text(i), value: value ? value(i) : i.id }));
      } catch {
        optionsMap.value[key] = [];
      }
    }
  }

  function fieldColumns(f: FieldDef): { text: any; value: any }[] {
    if (f.type === 'enum') return (f.options || []).map((o) => ({ text: o.label, value: o.value }));
    if (f.type === 'select' || f.type === 'multi') return optionsMap.value[f.apiKey!] || [];
    return [];
  }

  function enumLabel(options: { label: string; value: number }[], value: any): string {
    if (value === null || value === undefined || value === '') return '';
    return options.find((o) => o.value === value)?.label || '';
  }

  function labelOf(f: FieldDef): string {
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
    if (f.type === 'enum') return enumLabel(f.options || [], v);
    return '';
  }

  // 通用枚举/单选弹层（支持任意字段及下游客户明细项）
  const pickerShow = ref(false);
  const pickerTitle = ref('');
  const pickerColumns = ref<{ text: any; value: any }[]>([]);
  const pickerDefaultIndex = ref(0);
  const pickerCallback = ref<(v: any) => void>(() => {});
  function openPicker(f: FieldDef) {
    openEnumPicker(t(f.label), fieldColumns(f), model[f.field], (v) => (model[f.field] = v));
  }
  function openEnumPicker(title: string, options: { text: any; value: any }[], current: any, cb: (v: any) => void) {
    pickerTitle.value = title;
    pickerColumns.value = options.map((o) => ({ text: o.text ?? o.label, value: o.value }));
    const idx = options.findIndex((o) => o.value === current);
    pickerDefaultIndex.value = idx >= 0 ? idx : 0;
    pickerCallback.value = cb;
    pickerShow.value = true;
  }
  function onPickerConfirm({ selectedOptions }: any) {
    const v = selectedOptions?.[0]?.value;
    pickerCallback.value(v);
    pickerShow.value = false;
  }

  // 多选
  const multiShow = ref(false);
  const multiField = ref<FieldDef | null>(null);
  const multiTemp = ref<any[]>([]);
  const multiColumns = ref<{ text: any; value: any }[]>([]);
  function openMulti(f: FieldDef) {
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
    if (multiField.value) model[multiField.value.field] = [...multiTemp.value];
    multiShow.value = false;
  }

  // 日期
  const calendarShow = ref(false);
  const calendarTitle = ref('');
  const calendarCallback = ref<(ts: number) => void>(() => {});
  function openCalendar(cb: (ts: number) => void) {
    calendarTitle.value = '';
    calendarCallback.value = cb;
    calendarShow.value = true;
  }
  function onCalendarConfirm(date: any) {
    calendarCallback.value(new Date(date).getTime());
    calendarShow.value = false;
  }

  // —— 下游客户付款返点明细（选择下游客户后自动带出） ——
  const payables = ref<PayableItem[]>([]);
  function downstreamMediaName(id?: string): string {
    if (!id) return '';
    return optionsMap.value.downstreamMediaIds?.find((o) => o.value === id)?.text || '';
  }
  watch(
    () => model.downstreamMediaIds,
    (ids) => {
      const list = ids || [];
      const existing = new Map(payables.value.map((p) => [p.downstreamMediaId, p]));
      payables.value = list.map((id: string) => {
        const prev = existing.get(id);
        if (prev) return prev;
        return {
          downstreamMediaId: id,
          payableAmount: null,
          noRebateAmount: null,
          rebateMode: null,
          rebateValue: null,
          paymentMethod: null,
          paymentPrepayMode: null,
          paymentPrepayRatio: null,
          paymentPrepayDeadline: null,
          paymentPostpayTrigger: null,
          paymentPostpayDays: null,
        } as PayableItem;
      });
    },
    { deep: true }
  );
  function calcRebate(p: PayableItem): number {
    const payable = Number(p.payableAmount || 0);
    const noRebate = Number(p.noRebateAmount || 0);
    const base = Math.max(payable - noRebate, 0);
    let r = 0;
    if (p.rebateMode === 10 && p.rebateValue) r = (base * Number(p.rebateValue)) / 100;
    else if (p.rebateMode === 20 && p.rebateValue) r = Number(p.rebateValue);
    return Math.min(r, base);
  }
  function calcActual(p: PayableItem): number {
    return Number(p.payableAmount || 0) - calcRebate(p);
  }
  // 订单金额与返点自动计算（对齐 web order/create.vue 的 autoCalc）
  function autoCalcOf(m: Record<string, any>): { rebateAmount: number; receivableAmount: number } {
    const total = Number(m.totalAmount || 0);
    const noRebate = Number(m.noRebateAmount || 0);
    const rebateBase = Math.max(total - noRebate, 0);
    let rebate = 0;
    if (m.rebateMode === 10 && m.rebateValue) rebate = (rebateBase * Number(m.rebateValue)) / 100;
    else if (m.rebateMode === 20 && m.rebateValue) rebate = Number(m.rebateValue);
    rebate = Math.min(rebate, rebateBase); // 返点不超过返点基数
    return { rebateAmount: rebate, receivableAmount: total - rebate };
  }
  const payableTotal = computed(() =>
    (payables.value || []).reduce((sum, it) => sum + Number(it.payableAmount || 0), 0)
  );
  // 实际应付总额 = 各客户 (应付金额 - 返点金额) 累加，对齐 web 订单详情下游明细合计
  const actualTotal = computed(() =>
    (payables.value || []).reduce((sum, it) => sum + calcActual(it), 0)
  );
  function onPaymentMethodChange(item: PayableItem, value: number | null) {
    item.paymentMethod = value;
    if (value === 20) {
      item.paymentPrepayMode = null;
      item.paymentPrepayRatio = null;
      item.paymentPrepayDeadline = null;
      item.paymentPrepayAmount = null;
    } else if (value === 10) {
      item.paymentPostpayTrigger = null;
      item.paymentPostpayDays = null;
    }
  }
  function onPostpayTriggerChange(item: PayableItem, value: number | null) {
    item.paymentPostpayTrigger = value;
    if (value !== 20) item.paymentPostpayDays = null;
  }
  // 比例预付模式：实时计算预付金额 = 应付金额 × 比例%
  watch(
    () => payables.value.map((p) => [p.paymentMethod, p.paymentPrepayMode, p.paymentPrepayRatio, p.payableAmount]),
    () => {
      payables.value.forEach((p) => {
        if (p.paymentMethod === 10 && p.paymentPrepayMode === 10 && p.payableAmount != null) {
          p.paymentPrepayAmount = +((Number(p.payableAmount) * Number(p.paymentPrepayRatio || 0)) / 100).toFixed(2);
        }
      });
    },
    { deep: true }
  );

  // —— 订单收款方式联动（对齐 web order/create.vue） ——
  function clearReceiptOpposite(method: number | null) {
    if (method === 20) {
      model.receiptPrepayMode = null;
      model.receiptPrepayRatio = null;
      model.receiptPrepayAmount = null;
      model.receiptPrepayDeadline = null;
    } else if (method === 10) {
      model.receiptAccountPeriodDays = null;
    }
  }
  watch(() => model.receiptMethod, () => clearReceiptOpposite(model.receiptMethod));
  // 预收比例模式：自动计算预收金额 = 应收总额 × 比例%
  watch(
    () => [model.receiptMethod, model.receiptPrepayMode, model.receiptPrepayRatio, model.totalAmount],
    () => {
      if (model.receiptMethod === 10 && model.receiptPrepayMode === 10) {
        const base = Number(model.totalAmount || 0);
        model.receiptPrepayAmount = +((base * Number(model.receiptPrepayRatio || 0)) / 100).toFixed(2);
      }
    }
  );

  // —— 附件上传 ——
  const { previewIframeVisible, previewIframeUrl, previewIframeName, previewIframeFileUrl } = useAttachmentPreview();
  const savedAttachments = ref<SavedAttach[]>([]);
  const pendingFiles = ref<PendingFile[]>([]);
  let pendingSeq = 0;
  const attachTab = ref<'normal' | 'eml'>('normal');
  const fileInputRef = ref<HTMLInputElement>();
  const currentUploadType = ref<number>(10);
  const hasEmailRecord = computed(
    () => savedAttachments.value.some((a) => a.type === 60) || pendingFiles.value.some((f) => f.type === 60)
  );
  const scheduleLabel = computed(() =>
    hasEmailRecord.value
      ? t('advertising.order.form.attachOptionalSchedule')
      : t('advertising.order.form.attachRequiredSchedule')
  );
  const emailLabel = computed(() =>
    hasEmailRecord.value
      ? t('advertising.order.form.attachOptionalEmail')
      : t('advertising.order.form.attachRequiredEmail')
  );
  function acceptFor(tab: 'normal' | 'eml'): string {
    return tab === 'eml' ? '.eml' : '.pdf,.jpg,.png,.doc,.docx,.xls,.xlsx';
  }
  function filesForType(type: number): FileItem[] {
    const saved = savedAttachments.value
      .filter((a) => a.type === type)
      .map((a) => ({ id: a.id, name: a.fileName, saved: true, fileUrl: a.fileUrl }));
    const pending = pendingFiles.value
      .filter((f) => f.type === type)
      .map((f) => ({ id: f._key, name: f.file.name, saved: false, file: f.file }));
    return [...saved, ...pending];
  }
  function pickFile(type: number) {
    currentUploadType.value = type;
    fileInputRef.value?.click();
  }
  function onNativeFileChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const files = input.files;
    if (!files || !files.length) return;
    for (const f of Array.from(files)) {
      pendingFiles.value.push({ _key: `f_${++pendingSeq}_${Date.now()}`, type: currentUploadType.value, file: f });
    }
    input.value = '';
  }
  async function removeFile(it: FileItem) {
    if (it.saved && it.fileUrl) {
      const id = it.id;
      if (isEdit.value && route.query.id) {
        await deleteAdOrderAttachment(route.query.id as string, id).catch(() => {});
      }
      savedAttachments.value = savedAttachments.value.filter((a) => a.id !== id);
    } else {
      pendingFiles.value = pendingFiles.value.filter((f) => f._key !== it.id);
    }
  }
  function previewFile(it: FileItem) {
    if (it.saved) {
      previewAttachment(it.fileUrl, it.name);
    } else if (it.file) {
      const url = URL.createObjectURL(it.file);
      if (/\.(png|jpe?g|gif|webp|bmp)$/i.test(it.file.name)) {
        showImagePreview([url]);
      } else {
        window.open(url, '_blank');
      }
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    }
  }
  function downloadFile(it: FileItem) {
    if (it.saved) {
      downloadAttachment(it.fileUrl);
    } else if (it.file) {
      const url = URL.createObjectURL(it.file);
      const a = document.createElement('a');
      a.href = url;
      a.download = it.name;
      a.click();
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    }
  }

  function back() {
    router.back();
  }

  function numOrUndef(v: any): number | undefined {
    return v === '' || v === null || v === undefined ? undefined : Number(v);
  }

  async function save() {
    if (!model.orderName?.trim()) {
      showToast(t('advertising.order.nameRequired'));
      return;
    }
    if (!model.businessEntityId) {
      showToast(t('advertising.order.entityRequired'));
      return;
    }
    if (!model.customerId) {
      showToast(t('advertising.order.customerRequired'));
      return;
    }
    if (model.receiptMethod == null) {
      showToast(t('advertising.order.receiptMethodRequired'));
      return;
    }
    NUMBER_FIELDS.forEach((k) => {
      const v = model[k];
      model[k] = v === '' || v === null || v === undefined ? undefined : Number(v);
    });
    const payload: any = {};
    SAVE_KEYS.forEach((k) => {
      payload[k] = model[k];
    });
    // 编辑时必须回传订单 id（后端 PUT /ad/order 以 body.id 定位订单，缺 id 会报「订单不存在」）
    if (isEdit.value) payload.id = (model.id || route.query.id) as string;
    payload.downstreamMediaIds = model.downstreamMediaIds || [];
    payload.downstreamMediaPayables = payables.value.map((p) => ({
      downstreamMediaId: p.downstreamMediaId,
      payableAmount: numOrUndef(p.payableAmount),
      noRebateAmount: numOrUndef(p.noRebateAmount),
      rebateMode: p.rebateMode ?? undefined,
      rebateValue: numOrUndef(p.rebateValue),
      paymentMethod: p.paymentMethod ?? undefined,
      paymentPrepayMode: p.paymentPrepayMode ?? undefined,
      paymentPrepayRatio: numOrUndef(p.paymentPrepayRatio),
      paymentPrepayDeadline: p.paymentPrepayDeadline ?? undefined,
      paymentPostpayTrigger: p.paymentPostpayTrigger ?? undefined,
      paymentPostpayDays: numOrUndef(p.paymentPostpayDays),
    }));
    payload.mediaPayableAmount = Number(payableTotal.value) || undefined;

    submitting.value = true;
    try {
      let newId = (route.query.id as string) || '';
      if (isEdit.value) {
        await updateAdOrder(payload);
      } else {
        const res: any = await createAdOrder(payload);
        newId = res.id || res.order?.id || '';
      }
      if (pendingFiles.value.length && newId) {
        if (isEdit.value) {
          const types = [...new Set(pendingFiles.value.map((f) => f.type))];
          const stale = savedAttachments.value.filter((a) => types.includes(a.type)).map((a) => a.id);
          await Promise.all(stale.map((id) => deleteAdOrderAttachment(newId, id).catch(() => {})));
        }
        await Promise.all(pendingFiles.value.map((f) => uploadAdOrderAttachment(newId, f.type, f.file)));
        pendingFiles.value = [];
      }
      showSuccessToast(isEdit.value ? t('advertising.order.saveSuccess') : t('advertising.order.createSuccess'));
      router.back();
    } finally {
      submitting.value = false;
    }
  }

  onMounted(async () => {
    await loadOptions();
    if (isEdit.value) {
      const detail: any = await getAdOrderDetail(route.query.id as string);
      const order = detail.order || {};
      SAVE_KEYS.forEach((k) => {
        if (k in order) model[k] = order[k];
      });
      model.id = order.id;
      model.contractId = detail.contractId;
      model.downstreamMediaIds = detail.downstreamMediaIds || [];
      await nextTick();
      payables.value = (detail.downstreamMediaPayables || []).map((p: any) => ({
        downstreamMediaId: p.downstreamMediaId,
        payableAmount: p.payableAmount ?? null,
        noRebateAmount: p.noRebateAmount ?? null,
        rebateMode: p.rebateMode ?? null,
        rebateValue: p.rebateValue ?? null,
        paymentMethod: p.paymentMethod ?? null,
        paymentPrepayMode: p.paymentPrepayMode ?? null,
        paymentPrepayRatio: p.paymentPrepayRatio ?? null,
        paymentPrepayDeadline: p.paymentPrepayDeadline != null ? new Date(p.paymentPrepayDeadline).getTime() : null,
        paymentPostpayTrigger: p.paymentPostpayTrigger ?? null,
        paymentPostpayDays: p.paymentPostpayDays ?? null,
      }));
      savedAttachments.value = (detail.attachments || []).map((att: any) => ({
        id: att.id,
        type: att.type,
        fileName: att.fileName || att.fileUrl || '未知文件',
        fileUrl: att.fileUrl,
      }));
    }
  });
</script>
