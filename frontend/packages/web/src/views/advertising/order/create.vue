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
            <n-button v-permission="['AD_ORDER:CREATE']" @click="handleSave('draft')">{{
              t('advertising.order.form.saveDraft')
            }}</n-button>
            <n-button
              v-if="hasPermission('AD_ORDER:CREATE') && hasPermission('AD_ORDER:SUBMIT')"
              type="primary"
              :loading="saving"
              @click="handleSave('submit')"
              >{{ t('advertising.order.form.submit') }}</n-button
            >
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
              <n-form-item-gi :span="2" label="下游客户">
                <n-select
                  v-model:value="form.downstreamMediaIds"
                  :options="downstreamMediaOptions"
                  filterable
                  multiple
                  clearable
                  placeholder="请选择下游客户（至少选一个）"
                />
              </n-form-item-gi>
            </n-grid>

            <!-- 3. 金额与返点 -->
            <n-divider title-placement="left">
              <span class="section-title">3. 金额与返点</span>
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

            <!-- 4. 投放信息 -->
            <n-divider title-placement="left">
              <span class="section-title">4. 投放信息</span>
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

            <!-- 5. 收款方式（上游） -->
            <n-divider title-placement="left">
              <span class="section-title">5. 收款方式（上游）</span>
            </n-divider>
            <n-grid :cols="2" :x-gap="16">
              <n-form-item-gi :span="2" :label="t('advertising.order.form.receiptMethod')" path="receiptMethod">
                <n-radio-group v-model:value="form.receiptMethod" name="receiptMethod">
                  <n-radio :value="10">预收</n-radio>
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

            <!-- 6. 付款返点明细（下游客户） -->
            <n-divider title-placement="left">
              <span class="section-title">6. 付款返点明细（下游客户）</span>
            </n-divider>
            <template v-if="(form.downstreamMediaIds || []).length > 0">
              <div
                v-for="(item, idx) in form.downstreamMediaPayables"
                :key="item.downstreamMediaId"
                class="payable-card"
              >
                <div class="payable-card-title">
                  <span class="payable-card-index">{{ idx + 1 }}</span>
                  <span class="payable-card-name">{{ downstreamMediaName(item.downstreamMediaId) }}</span>
                </div>
                <n-grid :cols="2" :x-gap="16">
                  <!-- 基础返点 -->
                  <n-form-item-gi :span="1" :label="t('advertising.order.form.payableAmount')">
                    <n-input-number v-model:value="item.payableAmount" :min="0" :precision="2" style="width: 100%" />
                  </n-form-item-gi>
                  <n-form-item-gi :span="1" :label="t('advertising.order.form.noRebateAmount')">
                    <n-input-number v-model:value="item.noRebateAmount" :min="0" :precision="2" style="width: 100%" />
                  </n-form-item-gi>
                  <n-form-item-gi :span="1" :label="t('advertising.order.form.rebateMode')">
                    <n-radio-group v-model:value="item.rebateMode" name="payableRebateMode">
                      <n-radio :value="10">比例</n-radio>
                      <n-radio :value="20">固定金额</n-radio>
                    </n-radio-group>
                  </n-form-item-gi>
                  <n-form-item-gi
                    v-if="item.rebateMode === 10"
                    :span="1"
                    :label="t('advertising.order.form.rebateRatio')"
                  >
                    <n-input-number
                      v-model:value="item.rebateValue"
                      :min="0"
                      :max="100"
                      :precision="2"
                      style="width: 100%"
                    >
                      <template #suffix>%</template>
                    </n-input-number>
                  </n-form-item-gi>
                  <n-form-item-gi
                    v-else-if="item.rebateMode === 20"
                    :span="1"
                    :label="t('advertising.order.form.rebateAmount')"
                  >
                    <n-input-number v-model:value="item.rebateValue" :min="0" :precision="2" style="width: 100%" />
                  </n-form-item-gi>
                  <n-form-item-gi :span="1" :label="t('advertising.order.form.rebateAmountAuto')">
                    <span class="readonly-field">{{ payableAutoCalc(item).rebateAmount }}</span>
                  </n-form-item-gi>
                  <n-form-item-gi :span="1" :label="t('advertising.order.form.actualPayable')">
                    <span class="readonly-field">{{ payableAutoCalc(item).actualPayable }}</span>
                  </n-form-item-gi>

                  <!-- 每个客户的付款方式 -->
                  <n-form-item-gi :span="2" :label="t('advertising.order.form.paymentMethod')">
                    <n-radio-group v-model:value="item.paymentMethod" name="payablePaymentMethod">
                      <n-radio :value="10">预付</n-radio>
                      <n-radio :value="20">后付</n-radio>
                    </n-radio-group>
                  </n-form-item-gi>
                  <template v-if="item.paymentMethod === 10">
                    <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPrepayMode')">
                      <n-select
                        v-model:value="item.paymentPrepayMode"
                        :options="prepayModeOptions"
                        placeholder="预付模式"
                      />
                    </n-form-item-gi>
                    <n-form-item-gi
                      v-if="item.paymentPrepayMode === 10"
                      :span="1"
                      :label="t('advertising.order.form.paymentPrepayRatio')"
                    >
                      <n-input-number v-model:value="item.paymentPrepayRatio" :min="0" :max="100" style="width: 100%">
                        <template #suffix>%</template>
                      </n-input-number>
                    </n-form-item-gi>
                    <n-form-item-gi
                      v-if="item.paymentPrepayMode === 10"
                      :span="1"
                      :label="t('advertising.order.form.paymentPrepayAmount')"
                    >
                      <span class="readonly-field">{{ item.paymentPrepayAmount ?? 0 }}</span>
                    </n-form-item-gi>
                    <n-form-item-gi
                      v-else-if="item.paymentPrepayMode === 20"
                      :span="1"
                      :label="t('advertising.order.form.paymentPrepayAmount')"
                    >
                      <n-input-number
                        v-model:value="item.paymentPrepayRatio"
                        :min="0"
                        :precision="2"
                        style="width: 100%"
                        placeholder="固定金额"
                      />
                    </n-form-item-gi>
                    <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPrepayDeadline')">
                      <n-date-picker v-model:value="item.paymentPrepayDeadline" type="date" style="width: 100%" />
                    </n-form-item-gi>
                  </template>
                  <template v-else-if="item.paymentMethod === 20">
                    <n-form-item-gi :span="1" :label="t('advertising.order.form.paymentPostpayTrigger')">
                      <n-select
                        v-model:value="item.paymentPostpayTrigger"
                        :options="postpayTriggerOptions"
                        placeholder="后付触发"
                      />
                    </n-form-item-gi>
                    <n-form-item-gi
                      v-if="item.paymentPostpayTrigger === 20"
                      :span="1"
                      :label="t('advertising.order.form.paymentPostpayDays')"
                    >
                      <n-input-number v-model:value="item.paymentPostpayDays" :min="0" style="width: 100%" />
                    </n-form-item-gi>
                  </template>
                </n-grid>
              </div>

              <!-- 应付总额（各客户应付金额累加） -->
              <n-grid :cols="2" :x-gap="16" class="payable-total-row">
                <n-form-item-gi :span="2" :label="t('advertising.order.form.mediaPayableAmountTotal')">
                  <span class="readonly-field total-field">{{ payableTotal }}</span>
                </n-form-item-gi>
              </n-grid>
            </template>

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
                <div class="flex flex-col gap-2">
                  <n-upload
                    v-model:file-list="fileListType10"
                    :custom-request="(opts: any) => handleFileSelect(10, opts)"
                    :show-file-list="false"
                    :on-remove="(opts: any) => handleFileRemove(10, opts)"
                    accept=".pdf,.jpg,.png,.doc,.docx,.xls,.xlsx"
                  >
                    <n-button size="small">选择文件</n-button>
                  </n-upload>
                  <div v-if="fileListType10.length" class="attach-file-list">
                    <div v-for="file in fileListType10" :key="file.id" class="attach-file-item">
                      <span class="attach-file-name" :title="file.name">{{ file.name }}</span>
                      <div class="attach-file-actions">
                        <n-button size="tiny" type="primary" ghost @click="handleAttachPreview(file)">预览</n-button>
                        <n-button size="tiny" type="primary" ghost @click="handleAttachDownload(file)">下载</n-button>
                        <n-button text size="tiny" type="error" @click="handleAttachRemove(10, file)">删除</n-button>
                      </div>
                    </div>
                  </div>
                </div>
              </n-form-item-gi>
              <n-form-item-gi :span="1" label="邮件截图（必传）">
                <div class="flex flex-col gap-2">
                  <n-upload
                    v-model:file-list="fileListType20"
                    :custom-request="(opts: any) => handleFileSelect(20, opts)"
                    :show-file-list="false"
                    :on-remove="(opts: any) => handleFileRemove(20, opts)"
                    accept=".pdf,.jpg,.png,.doc,.docx,.xls,.xlsx"
                  >
                    <n-button size="small">选择文件</n-button>
                  </n-upload>
                  <div v-if="fileListType20.length" class="attach-file-list">
                    <div v-for="file in fileListType20" :key="file.id" class="attach-file-item">
                      <span class="attach-file-name" :title="file.name">{{ file.name }}</span>
                      <div class="attach-file-actions">
                        <n-button size="tiny" type="primary" ghost @click="handleAttachPreview(file)">预览</n-button>
                        <n-button size="tiny" type="primary" ghost @click="handleAttachDownload(file)">下载</n-button>
                        <n-button text size="tiny" type="error" @click="handleAttachRemove(20, file)">删除</n-button>
                      </div>
                    </div>
                  </div>
                </div>
              </n-form-item-gi>
              <n-form-item-gi :span="1" label="补充协议（选填）">
                <div class="flex flex-col gap-2">
                  <n-upload
                    v-model:file-list="fileListType40"
                    :custom-request="(opts: any) => handleFileSelect(40, opts)"
                    :show-file-list="false"
                    :on-remove="(opts: any) => handleFileRemove(40, opts)"
                    accept=".pdf,.jpg,.png,.doc,.docx,.xls,.xlsx"
                  >
                    <n-button size="small">选择文件</n-button>
                  </n-upload>
                  <div v-if="fileListType40.length" class="attach-file-list">
                    <div v-for="file in fileListType40" :key="file.id" class="attach-file-item">
                      <span class="attach-file-name" :title="file.name">{{ file.name }}</span>
                      <div class="attach-file-actions">
                        <n-button size="tiny" type="primary" ghost @click="handleAttachPreview(file)">预览</n-button>
                        <n-button size="tiny" type="primary" ghost @click="handleAttachDownload(file)">下载</n-button>
                        <n-button text size="tiny" type="error" @click="handleAttachRemove(40, file)">删除</n-button>
                      </div>
                    </div>
                  </div>
                </div>
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
  import { hasPermission } from '@/utils/permission';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { AD_SELECT_PAGE_PARAMS } from '../utils';

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

  /** 取某个附件类型对应的 n-upload 文件列表 ref */
  function getAttachFileList(type: number) {
    if (type === 10) return fileListType10;
    if (type === 20) return fileListType20;
    return fileListType40;
  }

  function handleFileSelect(type: number, opts: { file: any; onFinish: () => void; onError: () => void }) {
    const rawFile = opts.file.file as File;
    const _key = `f_${++pendingFileSeq}_${Date.now()}`;
    // n-upload 在 custom-request + v-model:file-list 模式下已自动把选中文件加入 file-list，
    // 手动再 push 会造成"点一个出两个"，所以这里只维护 pendingFiles。
    // 但 n-upload 对 custom-request 收到的 file 做了浅拷贝，直接改 opts.file.id 改不到列表里的对象，
    // 需按原始 File 定位真实列表项并把 id 设为 _key，删除时才能精确匹配。
    opts.file.id = _key;
    const target = getAttachFileList(type).value.find((f: any) => f.file === rawFile);
    if (target) target.id = _key;
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

  /** 是否支持浏览器内联预览（PDF / 图片） */
  function isPreviewableAttach(name?: string) {
    return !!name && /\.(pdf|png|jpe?g|gif|webp|bmp|svg)$/i.test(name);
  }

  /** 取附件地址：新选文件用本地 blob，已上传附件用附件接口（preview 内联预览 / download 触发下载） */
  function resolveAttachUrl(file: any, action: 'preview' | 'download') {
    const pending = pendingFiles.value.find((f) => f._key === file?.id || f.file === file?.file);
    if (pending) {
      return { url: URL.createObjectURL(pending.file), fileName: pending.file.name, isBlob: true };
    }
    const saved = savedAttachments.value.find((a) => a.id === file?.id);
    if (!saved?.fileUrl) return null;
    const userId = userStore.userInfo?.id || '';
    return {
      url: `/attachment/${action}/${saved.fileUrl}?userId=${userId}`,
      fileName: saved.fileName || file?.name || '附件',
      isBlob: false,
    };
  }

  function triggerBrowserDownload(url: string, fileName: string) {
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.target = '_blank';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  /** 附件下载：新选文件走本地 blob，已上传附件走附件下载接口 */
  function handleAttachDownload(file: any) {
    const target = resolveAttachUrl(file, 'download');
    if (!target) return;
    triggerBrowserDownload(target.url, target.fileName);
    if (target.isBlob) setTimeout(() => URL.revokeObjectURL(target.url), 60000);
  }

  /** 附件预览：PDF / 图片内联打开，其它格式自动转为下载 */
  function handleAttachPreview(file: any) {
    if (!isPreviewableAttach(file?.name)) {
      handleAttachDownload(file);
      return;
    }
    const target = resolveAttachUrl(file, 'preview');
    if (!target) {
      message.warning('附件尚未保存，请先保存后再预览');
      return;
    }
    window.open(target.url, '_blank');
    if (target.isBlob) setTimeout(() => URL.revokeObjectURL(target.url), 60000);
  }

  /** 自定义文件项的删除按钮：先走原有删除校验，再从列表移除 */
  async function handleAttachRemove(type: number, file: any) {
    const isRemovable = await handleFileRemove(type, { file });
    if (!isRemovable) return;
    // 兜底：n-upload 内部 id 与暂存 _key 不一致时，按原始 File 对象再清理一次
    if (file?.file instanceof File) {
      pendingFiles.value = pendingFiles.value.filter((f) => f.file !== file.file);
    }
    const listRef = getAttachFileList(type);
    listRef.value = listRef.value.filter((f) => f.id !== file?.id);
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
    downstreamMediaPayables?: DownstreamPayable[];
  }

  /** 单个下游客户的付款返点明细 */
  interface DownstreamPayable {
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
    downstreamMediaPayables: [],
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

  // 根据下游客户 id 取名称（下拉选项 label）
  function downstreamMediaName(id?: string): string {
    if (!id) return '';
    const found = downstreamMediaOptions.value.find((o) => o.value === id);
    return found ? found.label : '';
  }

  // 监听下游客户多选变化，同步每个客户的付款返点明细卡片
  watch(
    () => form.downstreamMediaIds,
    (ids) => {
      const list = ids || [];
      const existing = new Map((form.downstreamMediaPayables || []).map((p) => [p.downstreamMediaId, p]));
      form.downstreamMediaPayables = list.map((id) => {
        const prev = existing.get(id);
        if (prev) return prev;
        return {
          downstreamMediaId: id,
          payableAmount: null,
          noRebateAmount: null,
          rebateMode: null,
          rebateValue: null,
        } as DownstreamPayable;
      });
    },
    { deep: true }
  );

  // 单个下游客户付款返点明细的自动计算
  function payableAutoCalc(item: DownstreamPayable) {
    const payable = Number(item.payableAmount || 0);
    const noRebate = Number(item.noRebateAmount || 0);
    const base = Math.max(payable - noRebate, 0);
    let rebate = 0;
    if (item.rebateMode === 10 && item.rebateValue) {
      rebate = (base * Number(item.rebateValue)) / 100;
    } else if (item.rebateMode === 20 && item.rebateValue) {
      rebate = Number(item.rebateValue);
    }
    const actual = payable - rebate;
    return {
      rebateAmount: rebate.toFixed(2),
      actualPayable: actual.toFixed(2),
    };
  }

  // 比例模式下，实时计算预收金额（应收 × 比例%）
  watch(
    [() => form.receiptPrepayMode, () => form.receiptPrepayRatio, () => autoCalc.value.receivableAmount],
    ([mode, ratio, receivable]) => {
      if (mode === 10) {
        form.receiptPrepayAmount = +((Number(receivable) * Number(ratio || 0)) / 100).toFixed(2);
      }
    }
  );
  // 各下游客户应付金额累加 = 订单应付总额
  const payableTotal = computed(() => {
    return (form.downstreamMediaPayables || []).reduce((sum, it) => sum + Number(it.payableAmount || 0), 0).toFixed(2);
  });

  // 每个客户：比例模式下，实时计算预付金额（应付金额 × 比例%）
  watch(
    () =>
      (form.downstreamMediaPayables || []).map((p: DownstreamPayable) => [
        p.paymentMethod,
        p.paymentPrepayMode,
        p.paymentPrepayRatio,
        p.payableAmount,
      ]),
    () => {
      (form.downstreamMediaPayables || []).forEach((p: DownstreamPayable) => {
        if (p.paymentMethod === 10 && p.paymentPrepayMode === 10 && p.payableAmount != null) {
          p.paymentPrepayAmount = +((Number(p.payableAmount) * Number(p.paymentPrepayRatio || 0)) / 100).toFixed(2);
        }
      });
    },
    { deep: true }
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

  /** 校验数据库 NOT NULL 且无默认值的基础字段（草稿/提交都要过，否则后端 500） */
  function validateRequired(): boolean {
    const checks: Array<[unknown, string]> = [
      [form.orderName, t('advertising.order.form.orderName')],
      [form.businessEntityId, t('advertising.order.form.businessEntityId')],
      [form.customerId, t('advertising.order.form.customerId')],
      [form.orderType, t('advertising.order.form.orderType')],
      [form.totalAmount, t('advertising.order.form.totalAmount')],
      [form.rebateMode, t('advertising.order.form.rebateMode')],
      [form.rebateValue, t('advertising.order.form.rebateValue')],
      [form.deliveryStartDate, t('advertising.order.form.deliveryStart')],
      [form.deliveryEndDate, t('advertising.order.form.deliveryEnd')],
      [form.receiptMethod, t('advertising.order.form.receiptMethod')],
    ];
    const missing = checks.find(([val]) => val === undefined || val === null || val === '');
    if (missing) {
      message.warning(`${missing[1]} ${t('advertising.order.form.required')}`);
      return false;
    }
    return true;
  }

  /** 业务级校验（仅提交时校验） */
  function validateBusiness(): boolean {
    if (!form.totalAmount || form.totalAmount <= 0) {
      message.warning(`${t('advertising.order.form.totalAmount')} 需大于 0`);
      return false;
    }
    if (form.orderType === 10 && !form.contractId) {
      message.warning('框架合同订单需先关联已生效的框架合同');
      return false;
    }
    return true;
  }

  function validate(action: 'draft' | 'submit'): boolean {
    if (!validateRequired()) return false;
    if (action === 'submit' && !validateBusiness()) return false;
    return true;
  }

  async function loadSelectOptions() {
    try {
      const [beRes, cuRes, uaRes, dictRes, dmRes] = await Promise.all([
        getAdBusinessEntityPage({ ...AD_SELECT_PAGE_PARAMS }),
        getAdCustomerPage({ ...AD_SELECT_PAGE_PARAMS }),
        getAdUpstreamAgentPage({ ...AD_SELECT_PAGE_PARAMS, status: 10 }),
        getAdDictPage({ ...AD_SELECT_PAGE_PARAMS, dictCode: 'industry' }),
        getAdDownstreamMediaPage({ ...AD_SELECT_PAGE_PARAMS, status: 10 }),
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
        ...AD_SELECT_PAGE_PARAMS,
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
      // 回填各下游客户付款返点明细（watch 已将数组按 ids 初始化为默认值，这里覆盖真实值）
      const payVo = res.downstreamMediaPayables || [];
      form.downstreamMediaPayables = (form.downstreamMediaIds || []).map((id: string) => {
        const vo: any = payVo.find((p: any) => p.downstreamMediaId === id) || {};
        return {
          downstreamMediaId: id,
          payableAmount: vo.payableAmount ?? null,
          noRebateAmount: vo.noRebateAmount ?? null,
          rebateMode: vo.rebateMode ?? null,
          rebateValue: vo.rebateValue ?? null,
          paymentMethod: vo.paymentMethod ?? null,
          paymentPrepayMode: vo.paymentPrepayMode ?? null,
          paymentPrepayRatio: vo.paymentPrepayRatio ?? null,
          paymentPrepayDeadline: toDateValue(vo.paymentPrepayDeadline),
          paymentPostpayTrigger: vo.paymentPostpayTrigger ?? null,
          paymentPostpayDays: vo.paymentPostpayDays ?? null,
        } as DownstreamPayable;
      });
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
      form.mediaPayableAmount = o.mediaPayableAmount;
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
      downstreamMediaPayables: (form.downstreamMediaPayables || []).map((p) => ({
        downstreamMediaId: p.downstreamMediaId,
        payableAmount: p.payableAmount ?? undefined,
        noRebateAmount: p.noRebateAmount ?? undefined,
        rebateMode: p.rebateMode ?? undefined,
        rebateValue: p.rebateValue ?? undefined,
        paymentMethod: p.paymentMethod ?? undefined,
        paymentPrepayMode: p.paymentPrepayMode ?? undefined,
        paymentPrepayRatio: p.paymentPrepayRatio ?? undefined,
        paymentPrepayDeadline: p.paymentPrepayDeadline ?? undefined,
        paymentPostpayTrigger: p.paymentPostpayTrigger ?? undefined,
        paymentPostpayDays: p.paymentPostpayDays ?? undefined,
      })),
      upstreamAgentId: form.upstreamAgentId,
      agentOrderNo: form.agentOrderNo,
      totalAmount: form.totalAmount ?? undefined,
      noRebateAmount: form.noRebateAmount ?? undefined,
      rebateMode: form.rebateMode ?? undefined,
      rebateValue: form.rebateValue ?? undefined,
      deliveryStartDate: form.deliveryStartDate ?? undefined,
      deliveryEndDate: form.deliveryEndDate ?? undefined,
      deliveryVolume: form.deliveryVolume,
      receiptMethod: form.receiptMethod ?? undefined,
      receiptPrepayMode: form.receiptPrepayMode ?? undefined,
      receiptPrepayRatio: form.receiptPrepayRatio ?? undefined,
      receiptPrepayAmount: form.receiptPrepayAmount ?? undefined,
      receiptPrepayDeadline: form.receiptPrepayDeadline ?? undefined,
      receiptAccountPeriodDays: form.receiptAccountPeriodDays ?? undefined,
      currency: form.currency,
      remark: form.remark,
    };
    if (isEdit.value) {
      payload.id = orderId.value;
    }
    return payload;
  }

  async function handleSave(action: 'draft' | 'submit') {
    if (!validate(action)) return;
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
  .attach-file-list {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  .attach-file-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 4px 8px;
    border-radius: 4px;
    background: var(--text-n10);
  }
  .attach-file-name {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    color: var(--text-n2);
  }
  .attach-file-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-shrink: 0;
  }
  .payable-card {
    border: 1px solid var(--border-color);
    border-left: 3px solid var(--primary-color, #18a058);
    border-radius: 6px;
    padding: 14px 16px 4px;
    margin: 0 0 12px 12px;
    background: var(--card-color);
  }
  .payable-card-title {
    display: flex;
    align-items: center;
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 10px;
    color: var(--text-n1);
  }
  .payable-card-index {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: var(--primary-color, #18a058);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    margin-right: 8px;
    flex-shrink: 0;
  }
  .payable-card-name {
    padding-left: 4px;
  }
  .payable-subtitle-divider :deep(.n-divider__title) {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-n2, #606266);
    letter-spacing: 0.5px;
  }
  .payable-subtitle {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-n2, #606266);
  }
</style>
