<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{ isEdit ? t('advertising.order.form.title.edit') : t('advertising.order.form.title.create') }}
          </div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.order.form.cancel') }}</n-button>
            <n-button @click="handleSave('draft')">{{ t('advertising.order.form.saveDraft') }}</n-button>
            <n-button type="primary" :loading="saving" @click="handleSave('submit')">{{
              t('advertising.order.form.submit')
            }}</n-button>
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-form ref="formRef" :model="form" label-placement="left" :label-width="120">
            <!-- 1. 基础信息 -->
            <n-divider title-placement="left">
              <span class="section-title">1. 基础信息</span>
            </n-divider>
            <n-grid :cols="2" :x-gap="16">
              <n-form-item-gi :span="1" :label="t('advertising.order.form.businessEntityId')" path="businessEntityId">
                <n-select
                  v-model:value="form.businessEntityId"
                  :options="businessEntityOptions"
                  filterable
                  placeholder="请选择业务主体"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.orderName')" path="orderName">
                <n-input v-model:value="form.orderName" placeholder="请输入订单名称" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.orderType')" path="orderType">
                <n-radio-group v-model:value="form.orderType" name="orderType">
                  <n-radio :value="10">{{ t('advertising.order.form.orderTypeFramework') }}</n-radio>
                  <n-radio :value="20">{{ t('advertising.order.form.orderTypeSingle') }}</n-radio>
                </n-radio-group>
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.customerId')" path="customerId">
                <n-select
                  v-model:value="form.customerId"
                  :options="customerOptions"
                  filterable
                  placeholder="请选择客户"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.industryCode')">
                <n-select
                  v-model:value="form.industryCode"
                  :options="industryOptions"
                  filterable
                  placeholder="请选择行业类别"
                />
              </n-form-item-gi>
              <n-form-item-gi v-if="false" :span="1" :label="t('advertising.order.form.signingEntity')">
                <n-input v-model:value="form.signingEntity" placeholder="签约主体" />
              </n-form-item-gi>
            </n-grid>

            <!-- 2. 合作方 -->
            <n-divider title-placement="left">
              <span class="section-title">2. 合作方</span>
            </n-divider>
            <n-grid :cols="2" :x-gap="16">
              <n-form-item-gi :span="1" :label="t('advertising.order.form.upstreamAgentId')">
                <n-select
                  v-model:value="form.upstreamAgentId"
                  :options="upstreamAgentOptions"
                  filterable
                  clearable
                  placeholder="请选择上游代理（可留空）"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.agentOrderNo')">
                <n-input v-model:value="form.agentOrderNo" placeholder="代理订单号" />
              </n-form-item-gi>
              <n-form-item-gi :span="2" label="下游媒体">
                <n-select
                  v-model:value="form.downstreamMediaIds"
                  :options="downstreamMediaOptions"
                  filterable
                  multiple
                  clearable
                  placeholder="请选择下游媒体（至少选一个）"
                />
              </n-form-item-gi>
            </n-grid>

            <!-- 3. 收款方式（上游） -->
            <n-divider title-placement="left">
              <span class="section-title">3. 收款方式（上游）</span>
            </n-divider>
            <n-grid :cols="2" :x-gap="16">
              <n-form-item-gi :span="2" :label="t('advertising.order.form.receiptMethod')" path="receiptMethod">
                <n-radio-group v-model:value="form.receiptMethod" name="receiptMethod">
                  <n-radio :value="10">预付款</n-radio>
                  <n-radio :value="20">执行后账期</n-radio>
                </n-radio-group>
              </n-form-item-gi>
              <!-- 预付款时：预付比例/金额(自动)/预付截止日 -->
              <template v-if="form.receiptMethod === 10">
                <n-form-item-gi :span="1" :label="t('advertising.order.form.receiptPrepayMode')">
                  <n-select
                    v-model:value="form.receiptPrepayMode"
                    :options="prepayModeOptions"
                    placeholder="预收模式"
                  />
                </n-form-item-gi>
                <n-form-item-gi
                  v-if="form.receiptPrepayMode === 10"
                  :span="1"
                  :label="t('advertising.order.form.receiptPrepayRatio')"
                >
                  <n-input-number v-model:value="form.receiptPrepayRatio" :min="0" :max="100" style="width: 100%">
                    <template #suffix>%</template>
                  </n-input-number>
                </n-form-item-gi>
                <n-form-item-gi :span="1" :label="t('advertising.order.form.receiptPrepayAmount')">
                  <n-input-number
                    v-model:value="form.receiptPrepayAmount"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    :disabled="form.receiptPrepayMode === 10"
                    placeholder="自动计算"
                  />
                </n-form-item-gi>
                <n-form-item-gi :span="1" :label="t('advertising.order.form.receiptPrepayDeadline')">
                  <n-date-picker v-model:value="form.receiptPrepayDeadline" type="date" style="width: 100%" />
                </n-form-item-gi>
              </template>
              <!-- 账期时：账期天数 -->
              <n-form-item-gi
                v-if="form.receiptMethod === 20"
                :span="1"
                :label="t('advertising.order.form.receiptAccountPeriodDays')"
              >
                <n-input-number v-model:value="form.receiptAccountPeriodDays" :min="0" style="width: 100%" />
              </n-form-item-gi>
            </n-grid>

            <!-- 4. 付款方式（下游媒体） -->
            <n-divider title-placement="left">
              <span class="section-title">4. 付款方式（下游媒体）</span>
            </n-divider>
            <n-grid :cols="2" :x-gap="16">
              <n-form-item-gi :span="2" :label="t('advertising.order.form.paymentMethod')" path="paymentMethod">
                <n-radio-group v-model:value="form.paymentMethod" name="paymentMethod">
                  <n-radio :value="10">预付媒体</n-radio>
                  <n-radio :value="20">后付媒体</n-radio>
                </n-radio-group>
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.mediaPayableAmount')">
                <n-input-number v-model:value="form.mediaPayableAmount" :min="0" :precision="2" style="width: 100%" />
              </n-form-item-gi>
              <template v-if="form.paymentMethod === 10">
                <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPrepayMode')">
                  <n-select
                    v-model:value="form.paymentPrepayMode"
                    :options="prepayModeOptions"
                    placeholder="媒体预付模式"
                  />
                </n-form-item-gi>
                <n-form-item-gi
                  v-if="form.paymentPrepayMode === 10"
                  :span="1"
                  :label="t('advertising.order.form.paymentPrepayRatio')"
                >
                  <n-input-number v-model:value="form.paymentPrepayRatio" :min="0" :max="100" style="width: 100%">
                    <template #suffix>%</template>
                  </n-input-number>
                </n-form-item-gi>
                <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPrepayAmount')">
                  <n-input-number
                    v-model:value="form.paymentPrepayAmount"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    :disabled="form.paymentPrepayMode === 10"
                    placeholder="自动计算"
                  />
                </n-form-item-gi>
                <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPrepayDeadline')">
                  <n-date-picker v-model:value="form.paymentPrepayDeadline" type="date" style="width: 100%" />
                </n-form-item-gi>
              </template>
              <template v-if="form.paymentMethod === 20">
                <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPostpayTrigger')">
                  <n-select
                    v-model:value="form.paymentPostpayTrigger"
                    :options="postpayTriggerOptions"
                    placeholder="后付触发"
                  />
                </n-form-item-gi>
                <n-form-item-gi
                  v-if="form.paymentPostpayTrigger === 20"
                  :span="1"
                  :label="t('advertising.order.form.paymentPostpayDays')"
                >
                  <n-input-number v-model:value="form.paymentPostpayDays" :min="0" style="width: 100%" />
                </n-form-item-gi>
              </template>
            </n-grid>

            <!-- 5. 金额与返点 -->
            <n-divider title-placement="left">
              <span class="section-title">5. 金额与返点</span>
            </n-divider>
            <n-grid :cols="2" :x-gap="16">
              <n-form-item-gi :span="1" :label="t('advertising.order.form.totalAmount')" path="totalAmount">
                <n-input-number v-model:value="form.totalAmount" :min="0" :precision="2" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.noRebateAmount')">
                <n-input-number v-model:value="form.noRebateAmount" :min="0" :precision="2" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.rebateMode')">
                <n-radio-group v-model:value="form.rebateMode" name="rebateMode">
                  <n-radio :value="10">比例</n-radio>
                  <n-radio :value="20">固定金额</n-radio>
                </n-radio-group>
              </n-form-item-gi>
              <n-form-item-gi v-if="form.rebateMode === 10" :span="1" :label="t('advertising.order.form.rebateRatio')">
                <n-input-number v-model:value="form.rebateValue" :min="0" :max="100" :precision="2" style="width: 100%">
                  <template #suffix>%</template>
                </n-input-number>
              </n-form-item-gi>
              <n-form-item-gi
                v-else-if="form.rebateMode === 20"
                :span="1"
                :label="t('advertising.order.form.rebateAmount')"
              >
                <n-input-number v-model:value="form.rebateValue" :min="0" :precision="2" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.rebateAmountAuto')">
                <span class="readonly-field">
                  {{ formatMoney(autoCalc.rebateAmount) }}
                </span>
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.receivableAmount')">
                <span class="readonly-field">
                  {{ formatMoney(autoCalc.receivableAmount) }}
                </span>
              </n-form-item-gi>
            </n-grid>

            <!-- 6. 投放信息 -->
            <n-divider title-placement="left">
              <span class="section-title">6. 投放信息</span>
            </n-divider>
            <n-grid :cols="2" :x-gap="16">
              <n-form-item-gi :span="1" :label="t('advertising.order.form.deliveryStart')">
                <n-date-picker v-model:value="form.deliveryStartDate" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.deliveryEnd')">
                <n-date-picker v-model:value="form.deliveryEndDate" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.deliveryVolume')">
                <n-input v-model:value="form.deliveryVolume" placeholder="投放量+单位" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.order.form.currency')">
                <n-input v-model:value="form.currency" placeholder="币种，默认 CNY" />
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.order.form.remark')">
                <n-input v-model:value="form.remark" type="textarea" placeholder="备注" />
              </n-form-item-gi>
            </n-grid>

            <!-- 7. 附件与合同 -->
            <n-divider title-placement="left">
              <span class="section-title">7. 附件与合同</span>
            </n-divider>
            <n-grid :cols="2" :x-gap="16">
              <n-form-item-gi :span="2" label="关联合同" path="contractId">
                <n-select
                  v-model:value="form.contractId"
                  :options="contractOptions"
                  filterable
                  clearable
                  :placeholder="form.orderType === 10 ? '请选择已生效的上游框架合同' : '请选择上游单笔合同（可后补）'"
                />
              </n-form-item-gi>
            </n-grid>
            <!-- 附件上传区 -->
            <n-grid :cols="1" :x-gap="16">
              <n-form-item-gi :span="1" label="盖章排期（必传）">
                <n-upload
                  v-model:file-list="fileListType10"
                  :custom-request="(opts: any) => handleFileSelect(10, opts)"
                  :show-file-list="true"
                  :on-remove="(opts: any) => handleFileRemove(10, opts)"
                  accept=".pdf,.jpg,.png,.doc,.docx,.xls,.xlsx"
                >
                  <n-button size="small">选择文件</n-button>
                </n-upload>
              </n-form-item-gi>
              <n-form-item-gi :span="1" label="邮件截图（必传）">
                <n-upload
                  v-model:file-list="fileListType20"
                  :custom-request="(opts: any) => handleFileSelect(20, opts)"
                  :show-file-list="true"
                  :on-remove="(opts: any) => handleFileRemove(20, opts)"
                  accept=".pdf,.jpg,.png,.doc,.docx,.xls,.xlsx"
                >
                  <n-button size="small">选择文件</n-button>
                </n-upload>
              </n-form-item-gi>
              <n-form-item-gi :span="1" label="补充协议（选填）">
                <n-upload
                  v-model:file-list="fileListType40"
                  :custom-request="(opts: any) => handleFileSelect(40, opts)"
                  :show-file-list="true"
                  :on-remove="(opts: any) => handleFileRemove(40, opts)"
                  accept=".pdf,.jpg,.png,.doc,.docx,.xls,.xlsx"
                >
                  <n-button size="small">选择文件</n-button>
                </n-upload>
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </div>
      </div>
    </CrmCard>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    NButton,
    NDatePicker,
    NDivider,
    NForm,
    NFormItemGi,
    NGrid,
    NInput,
    NInputNumber,
    NRadio,
    NRadioGroup,
    NSelect,
    NText,
    NUpload,
    useMessage,
  } from 'naive-ui';

  import {
    AdModeOptions,
    AdOrderTypeOptions,
    AdPaymentMethodOptions,
    AdPostpayTriggerOptions,
    AdReceiptMethodOptions,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderSaveParams } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

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
    submitAdOrder,
    updateAdOrder,
    uploadAdOrderAttachment,
  } from '@/api/modules';
  import useUserStore from '@/store/modules/user';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();
  const userStore = useUserStore();

  const orderTypeOptions = AdOrderTypeOptions;
  const rebateModeOptions = AdModeOptions;
  const receiptMethodOptions = AdReceiptMethodOptions;
  const paymentMethodOptions = AdPaymentMethodOptions;
  const postpayTriggerOptions = AdPostpayTriggerOptions;
  const prepayModeOptions = AdModeOptions;

  const isEdit = computed(() => !!route.params.id);
  const orderId = computed(() => (route.params.id as string) || '');
  let isRestoringFromDetail = false;
  const saving = ref(false);
  const loading = ref(false);

  type SelectItem = { label: string; value: string | number };
  const businessEntityOptions = ref<SelectItem[]>([]);
  const customerOptions = ref<SelectItem[]>([]);
  const upstreamAgentOptions = ref<SelectItem[]>([]);
  const downstreamMediaOptions = ref<SelectItem[]>([]);
  const industryOptions = ref<SelectItem[]>([]);
  const contractOptions = ref<SelectItem[]>([]);
  const selectedContract = ref<{ id: string; contractNo?: string; contractName?: string } | null>(null);

  // 已有附件（编辑时从 API 加载，已上传到服务器的）
  interface SavedAttach {
    id: string;
    type: number;
    fileName: string;
    fileUrl: string;
  }
  const savedAttachments = ref<SavedAttach[]>([]);

  // 暂存待上传的新附件（创建/编辑订单后再上传）
  interface PendingFile {
    _key: string;
    type: number; // 10盖章排期 / 20邮件截图 / 40补充协议
    file: File;
  }
  const pendingFiles = ref<PendingFile[]>([]);
  let pendingFileSeq = 0;

  // 响应式 fileList（按 type 分组），给 n-upload 用 v-model:file-list
  const fileListType10 = ref<any[]>([]);
  const fileListType20 = ref<any[]>([]);
  const fileListType40 = ref<any[]>([]);

  function buildFileListForType(type: number) {
    const saved = savedAttachments.value
      .filter((a) => a.type === type)
      .map((a) => ({
        id: a.id,
        name: a.fileName,
        status: 'finished',
        url: a.fileUrl,
        // naive-ui 内部会把缺省 file 规范化为 null，随后渲染时 isImageFile(null) 读 null.type 崩溃。
        // 已上传附件没有真实 File 对象，这里用占位 File 避免崩溃（合同页用 show-file-list=false 故不触发此问题）。
        file: new File([], a.fileName || 'file'),
      }));
    const pending = pendingFiles.value
      .filter((f) => f.type === type)
      .map((f) => ({ id: f._key, name: f.file.name, status: 'finished' }));
    return [...saved, ...pending];
  }

  function handleFileSelect(type: number, opts: { file: any; onFinish: () => void; onError: () => void }) {
    const rawFile = opts.file.file as File;
    const _key = `f_${++pendingFileSeq}_${Date.now()}`;
    // n-upload 在 custom-request + v-model:file-list 模式下已自动把选中文件加入 file-list，
    // 手动再 push 会造成"点一个出两个"。这里只维护 pendingFiles，并把自动项的 id 设为 _key，
    // 这样删除时能通过 removed.id === _key 精确匹配。
    opts.file.id = _key;
    pendingFiles.value.push({ _key, type, file: rawFile });
    opts.onFinish();
  }

  /** 处理 n-upload 的文件移除：已有附件调后端删除接口，新文件只本地移除 */
  async function handleFileRemove(type: number, opts: { file: any }) {
    const removed = opts.file;
    if (!removed) return false;
    // 是否已有附件（已上传到服务器的，id 是后端生成的）
    const isSaved = savedAttachments.value.some((a) => a.id === removed.id);
    if (isSaved) {
      // 调后端删除
      try {
        await deleteAdOrderAttachment(orderId.value, removed.id);
        savedAttachments.value = savedAttachments.value.filter((a) => a.id !== removed.id);
        message.success('附件已删除');
      } catch (e) {
        message.error((e as Error).message || '删除失败');
        return false; // 返回 false 阻止 n-upload 移除
      }
    } else {
      // 新暂存文件，仅本地移除
      pendingFiles.value = pendingFiles.value.filter((f) => f._key !== removed.id);
    }
    return true;
  }

  interface AdOrderForm {
    orderName?: string;
    businessEntityId?: string;
    customerId?: string;
    industryCode?: string;
    signingEntity?: string;
    orderType?: number | null;
    contractId?: string;
    downstreamMediaIds?: string[];
    upstreamAgentId?: string;
    agentOrderNo?: string;
    totalAmount?: number | null;
    noRebateAmount?: number | null;
    rebateMode?: number | null;
    rebateValue?: number | null;
    mediaPayableAmount?: number | null;
    deliveryStartDate?: number | null;
    deliveryEndDate?: number | null;
    deliveryVolume?: string;
    receiptMethod?: number | null;
    receiptPrepayMode?: number | null;
    receiptPrepayRatio?: number | null;
    receiptPrepayAmount?: number | null;
    receiptPrepayDeadline?: number | null;
    receiptAccountPeriodDays?: number | null;
    paymentMethod?: number | null;
    paymentPrepayMode?: number | null;
    paymentPrepayRatio?: number | null;
    paymentPrepayAmount?: number | null;
    paymentPrepayDeadline?: number | null;
    paymentPostpayTrigger?: number | null;
    paymentPostpayDays?: number | null;
    currency?: string;
    remark?: string;
  }

  const form = reactive<AdOrderForm>({
    orderName: undefined,
    businessEntityId: undefined,
    customerId: undefined,
    industryCode: undefined,
    signingEntity: undefined,
    orderType: null,
    contractId: undefined,
    downstreamMediaIds: [],
    upstreamAgentId: undefined,
    agentOrderNo: undefined,
    totalAmount: null,
    noRebateAmount: 0,
    rebateMode: null,
    rebateValue: null,
    mediaPayableAmount: null,
    deliveryStartDate: null,
    deliveryEndDate: null,
    deliveryVolume: undefined,
    receiptMethod: null,
    receiptPrepayMode: null,
    receiptPrepayRatio: null,
    receiptPrepayAmount: null,
    receiptPrepayDeadline: null,
    receiptAccountPeriodDays: null,
    paymentMethod: null,
    paymentPrepayMode: null,
    paymentPrepayRatio: null,
    paymentPrepayAmount: null,
    paymentPrepayDeadline: null,
    paymentPostpayTrigger: null,
    paymentPostpayDays: null,
    currency: 'CNY',
    remark: undefined,
  });

  // 自动计算：返点金额、实际应收
  const autoCalc = computed(() => {
    const total = Number(form.totalAmount || 0);
    const noRebate = Number(form.noRebateAmount || 0);
    const rebateBase = Math.max(total - noRebate, 0);
    let rebate = 0;
    if (form.rebateMode === 10 && form.rebateValue) {
      rebate = (rebateBase * Number(form.rebateValue)) / 100;
    } else if (form.rebateMode === 20 && form.rebateValue) {
      rebate = Number(form.rebateValue);
    }
    rebate = Math.min(rebate, rebateBase); // 返点不超过返点基数
    return {
      rebateAmount: rebate.toFixed(2),
      receivableAmount: (total - rebate).toFixed(2),
    };
  });

  // 比例模式下，实时计算预收金额（应收 × 比例%）
  watch(
    [() => form.receiptPrepayMode, () => form.receiptPrepayRatio, () => autoCalc.value.receivableAmount],
    ([mode, ratio, receivable]) => {
      if (mode === 10) {
        form.receiptPrepayAmount = +((Number(receivable) * Number(ratio || 0)) / 100).toFixed(2);
      }
    }
  );
  // 比例模式下，实时计算媒体预付金额（媒体应付 × 比例%）
  watch(
    [() => form.paymentPrepayMode, () => form.paymentPrepayRatio, () => form.mediaPayableAmount],
    ([mode, ratio, base]) => {
      if (mode === 10) {
        form.paymentPrepayAmount = +((Number(base || 0) * Number(ratio || 0)) / 100).toFixed(2);
      }
    }
  );

  function formatMoney(v: string | number) {
    if (v == null || v === '') return '-';
    const num = typeof v === 'string' ? parseFloat(v) : v;
    if (Number.isNaN(num)) return '-';
    return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function fetchData() {
    // placeholder for pagination callback
  }

  const pagination = reactive({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    onUpdatePage: () => fetchData(),
  });

  function resetForm() {
    Object.assign(form, {
      orderName: undefined,
      businessEntityId: undefined,
      customerId: undefined,
      industryCode: undefined,
      signingEntity: undefined,
      orderType: null,
      contractId: undefined,
      upstreamAgentId: undefined,
      agentOrderNo: undefined,
      totalAmount: null,
      noRebateAmount: 0,
      rebateMode: null,
      rebateValue: null,
      mediaPayableAmount: null,
      deliveryStartDate: null,
      deliveryEndDate: null,
      deliveryVolume: undefined,
      receiptMethod: null,
      receiptPrepayMode: null,
      receiptPrepayRatio: null,
      receiptPrepayAmount: null,
      receiptPrepayDeadline: null,
      receiptAccountPeriodDays: null,
      paymentMethod: null,
      paymentPrepayMode: null,
      paymentPrepayRatio: null,
      paymentPrepayAmount: null,
      paymentPrepayDeadline: null,
      paymentPostpayTrigger: null,
      paymentPostpayDays: null,
      currency: 'CNY',
      remark: undefined,
    });
  }

  function validate(): boolean {
    if (!form.orderName) {
      message.warning(`订单名称 ${t('advertising.order.form.required')}`);
      return false;
    }
    if (!form.businessEntityId) {
      message.warning(`业务主体 ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.orderType === null || form.orderType === undefined) {
      message.warning(`订单类型 ${t('advertising.order.form.required')}`);
      return false;
    }
    if (!form.totalAmount || form.totalAmount <= 0) {
      message.warning(`订单总金额 ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.mediaPayableAmount === null || form.mediaPayableAmount === undefined || form.mediaPayableAmount < 0) {
      message.warning(`媒体应付总额 ${t('advertising.order.form.required')}`);
      return false;
    }
    return true;
  }

  async function loadSelectOptions() {
    try {
      const [beRes, cuRes, uaRes, dictRes, dmRes] = await Promise.all([
        getAdBusinessEntityPage({ current: 1, pageSize: 200 }),
        getAdCustomerPage({ current: 1, pageSize: 200 }),
        getAdUpstreamAgentPage({ current: 1, pageSize: 200, status: 10 }),
        getAdDictPage({ current: 1, pageSize: 200, dictCode: 'industry' }),
        getAdDownstreamMediaPage({ current: 1, pageSize: 200, status: 10 }),
      ]);
      businessEntityOptions.value = (beRes.list || []).map((it: any) => ({
        label: it.name || it.id,
        value: it.id,
      }));
      customerOptions.value = (cuRes.list || []).map((it: any) => ({
        label: it.customerName || it.name || it.id,
        value: it.id,
      }));
      upstreamAgentOptions.value = (uaRes.list || []).map((it: any) => ({
        label: it.resourceName || it.name || it.id,
        value: it.id,
      }));
      industryOptions.value = (dictRes.list || []).map((it: any) => ({
        label: it.dictLabel || it.dictValue || it.id,
        value: it.dictValue || it.id,
      }));
      downstreamMediaOptions.value = (dmRes.list || []).map((it: any) => ({
        label: it.name || it.id,
        value: it.id,
      }));
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  /** 加载合同列表（按订单类型 + 已归档过滤） */
  async function loadContractOptions() {
    try {
      const res = await getAdContractPage({
        current: 1,
        pageSize: 200,
        contractType: form.orderType ?? undefined,
        sealStatus: 60, // 只可选已归档（双盖完成）的合同
      });
      contractOptions.value = (res.list || []).map((it: any) => ({
        label: [it.contractNo, it.contractName].filter(Boolean).join(' ') || it.id,
        value: it.id,
      }));
      // 编辑回填时，若已选合同不在当前过滤结果中（如未归档或类型不匹配），
      // 追加该选项避免被清空，保证再次保存时关联关系不丢失
      if (
        form.contractId &&
        selectedContract.value &&
        !contractOptions.value.some((opt) => opt.value === form.contractId)
      ) {
        contractOptions.value.push({
          label:
            [selectedContract.value.contractNo, selectedContract.value.contractName].filter(Boolean).join(' ') ||
            selectedContract.value.id,
          value: selectedContract.value.id,
        });
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  watch(
    () => form.orderType,
    (newType, oldType) => {
      if (isRestoringFromDetail) {
        return;
      }
      if (newType === oldType) {
        return;
      }
      // 编辑页首次回填 orderType（从 undefined 变为实际值）时，不清空已回填的合同
      if (isEdit.value && oldType == null) {
        return;
      }
      form.contractId = undefined;
      selectedContract.value = null;
      if (newType === 10 || newType === 20) {
        loadContractOptions();
      } else {
        contractOptions.value = [];
      }
    }
  );

  // 媒体应付总额默认联动订单总额，可手动改
  watch(
    () => form.totalAmount,
    (val) => {
      if (form.mediaPayableAmount === null || form.mediaPayableAmount === undefined) {
        form.mediaPayableAmount = val ?? null;
      }
    }
  );

  function toDateValue(v: any): number | null {
    if (v == null || v === '') return null;
    if (typeof v === 'number') return v;
    const ts = new Date(v).getTime();
    return Number.isNaN(ts) ? null : ts;
  }

  async function loadForEdit() {
    if (!orderId.value) return;
    isRestoringFromDetail = true;
    try {
      const res = await getAdOrderDetail(orderId.value);
      const o = res.order;
      form.orderName = o.orderName;
      form.businessEntityId = o.businessEntityId;
      form.customerId = o.customerId;
      form.industryCode = o.industryCode;
      form.signingEntity = o.signingEntity;
      form.orderType = o.orderType ?? null;
      form.contractId = res.contractId || undefined;
      selectedContract.value = res.contractId
        ? { id: res.contractId, contractNo: res.contractNo, contractName: res.contractName }
        : null;
      form.downstreamMediaIds = res.downstreamMediaIds || [];
      form.upstreamAgentId = o.upstreamAgentId;
      form.agentOrderNo = o.agentOrderNo;
      form.totalAmount = o.totalAmount;
      form.noRebateAmount = o.noRebateAmount ?? 0;
      form.rebateMode = o.rebateMode ?? null;
      form.rebateValue = o.rebateValue;
      form.mediaPayableAmount = o.mediaPayableAmount;
      form.deliveryStartDate = toDateValue(o.deliveryStartDate);
      form.deliveryEndDate = toDateValue(o.deliveryEndDate);
      form.deliveryVolume = o.deliveryVolume;
      form.receiptMethod = o.receiptMethod ?? null;
      form.receiptPrepayMode = o.receiptPrepayMode ?? null;
      form.receiptPrepayRatio = o.receiptPrepayRatio ?? null;
      form.receiptPrepayAmount = o.receiptPrepayAmount ?? null;
      form.receiptPrepayDeadline = toDateValue(o.receiptPrepayDeadline);
      form.receiptAccountPeriodDays = o.receiptAccountPeriodDays ?? null;
      form.paymentMethod = o.paymentMethod ?? null;
      form.paymentPrepayMode = o.paymentPrepayMode ?? null;
      form.paymentPrepayRatio = o.paymentPrepayRatio ?? null;
      form.paymentPrepayAmount = o.paymentPrepayAmount ?? null;
      form.paymentPrepayDeadline = toDateValue(o.paymentPrepayDeadline);
      form.paymentPostpayTrigger = o.paymentPostpayTrigger ?? null;
      form.paymentPostpayDays = o.paymentPostpayDays ?? null;
      form.currency = o.currency || 'CNY';
      form.remark = o.remark;
      // 回填已有附件
      savedAttachments.value = (res.attachments || []).map((att: any) => ({
        id: att.id,
        type: att.type,
        fileName: att.fileName || att.fileUrl || '未知文件',
        fileUrl: att.fileUrl,
      }));
      fileListType10.value = buildFileListForType(10);
      fileListType20.value = buildFileListForType(20);
      fileListType40.value = buildFileListForType(40);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      isRestoringFromDetail = false;
    }
  }

  async function buildPayload() {
    const payload: any = {
      orderName: form.orderName,
      businessEntityId: form.businessEntityId,
      customerId: form.customerId,
      industryCode: form.industryCode,
      signingEntity: form.signingEntity,
      orderType: form.orderType ?? undefined,
      contractId: form.contractId,
      downstreamMediaIds: form.downstreamMediaIds,
      upstreamAgentId: form.upstreamAgentId,
      agentOrderNo: form.agentOrderNo,
      totalAmount: form.totalAmount ?? undefined,
      noRebateAmount: form.noRebateAmount ?? undefined,
      rebateMode: form.rebateMode ?? undefined,
      rebateValue: form.rebateValue ?? undefined,
      mediaPayableAmount: form.mediaPayableAmount ?? undefined,
      deliveryStartDate: form.deliveryStartDate ?? undefined,
      deliveryEndDate: form.deliveryEndDate ?? undefined,
      deliveryVolume: form.deliveryVolume,
      receiptMethod: form.receiptMethod ?? undefined,
      receiptPrepayMode: form.receiptPrepayMode ?? undefined,
      receiptPrepayRatio: form.receiptPrepayRatio ?? undefined,
      receiptPrepayAmount: form.receiptPrepayAmount ?? undefined,
      receiptPrepayDeadline: form.receiptPrepayDeadline ?? undefined,
      receiptAccountPeriodDays: form.receiptAccountPeriodDays ?? undefined,
      paymentMethod: form.paymentMethod ?? undefined,
      paymentPrepayMode: form.paymentPrepayMode ?? undefined,
      paymentPrepayRatio: form.paymentPrepayRatio ?? undefined,
      paymentPrepayAmount: form.paymentPrepayAmount ?? undefined,
      paymentPrepayDeadline: form.paymentPrepayDeadline ?? undefined,
      paymentPostpayTrigger: form.paymentPostpayTrigger ?? undefined,
      paymentPostpayDays: form.paymentPostpayDays ?? undefined,
      currency: form.currency,
      remark: form.remark,
    };
    if (isEdit.value) {
      payload.id = orderId.value;
    }
    return payload;
  }

  async function handleSave(action: 'draft' | 'submit') {
    if (!validate()) return;
    // 提交时校验必传附件（已有 + 新暂存）
    if (action === 'submit') {
      const hasSchedule =
        savedAttachments.value.some((a) => a.type === 10) || pendingFiles.value.some((f) => f.type === 10);
      const hasEmail =
        savedAttachments.value.some((a) => a.type === 20) || pendingFiles.value.some((f) => f.type === 20);
      if (!hasSchedule) {
        message.warning('请上传【盖章排期】附件');
        return;
      }
      if (!hasEmail) {
        message.warning('请上传【邮件截图】附件');
        return;
      }
    }
    saving.value = true;
    try {
      const payload = await buildPayload();
      let newOrderId = orderId.value;
      if (isEdit.value) {
        await updateAdOrder(payload);
      } else {
        const created = await createAdOrder(payload);
        newOrderId = created.id || (created as any).order?.id;
      }
      // 上传附件：若本次重新上传了某类型，先删除该类型已存的旧附件（替换语义，避免草稿附件重复）
      if (pendingFiles.value.length > 0 && newOrderId) {
        const pendingTypes = [...new Set(pendingFiles.value.map((f) => f.type))];
        const staleIds = savedAttachments.value
          .filter((a) => pendingTypes.includes(a.type) && a.id)
          .map((a) => a.id as string);
        await Promise.all(
          staleIds.map((id) =>
            deleteAdOrderAttachment(newOrderId, id).catch(() => {
              // ignore
            })
          )
        );
        await Promise.all(pendingFiles.value.map((f) => uploadAdOrderAttachment(newOrderId, f.type, f.file)));
        pendingFiles.value = []; // 上传完成后清空，避免再次进入/重复上传
      }
      // 提交时调提交接口
      if (action === 'submit' && newOrderId) {
        await submitAdOrder(newOrderId);
      }
      message.success(action === 'submit' ? '保存并提交成功' : '保存草稿成功');
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_ORDER });
    } catch (e) {
      message.error((e as Error).message || '保存失败');
    } finally {
      saving.value = false;
    }
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_ORDER });
  }

  onMounted(async () => {
    await loadSelectOptions();
    if (isEdit.value) {
      await loadForEdit();
    }
    if (form.orderType === 10 || form.orderType === 20) {
      await loadContractOptions();
    }
  });
</script>

<style scoped>
  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--primary-color);
  }
  .readonly-field {
    display: inline-block;
    padding: 4px 12px;
    background: var(--text-n10);
    border-radius: 4px;
    color: var(--text-n2);
    font-weight: 500;
  }
  .attach-tip {
    margin: 0 0 16px 120px;
  }
</style>
