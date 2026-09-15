<template>
  <div class="h-full">
    <CrmCard no-content-padding hide-footer>
      <div class="flex h-full flex-col px-[16px] py-[16px]">
        <n-space class="mb-[12px]" justify="space-between" align="center">
          <div class="text-[16px] font-semibold">
            {{ isEdit ? t('advertising.contract.form.title.edit') : t('advertising.contract.form.title.create') }}
          </div>
          <n-space>
            <n-button @click="goBack">{{ t('advertising.order.form.cancel') }}</n-button>
            <n-button
              v-permission="[isEdit ? 'AD_CONTRACT:UPDATE' : 'AD_CONTRACT:CREATE']"
              type="primary"
              :loading="saving"
              @click="handleSave"
              >{{ t('advertising.order.form.save') }}</n-button
            >
          </n-space>
        </n-space>

        <div class="flex-1 overflow-auto">
          <n-form ref="formRef" :model="form" label-placement="left" :label-width="120">
            <n-grid :cols="2" :x-gap="16" item-responsive>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.contractName')" path="contractName">
                <n-input v-model:value="form.contractName" placeholder="请输入合同名称" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.contractNo')" path="contractNo">
                <n-input v-model:value="form.contractNo" placeholder="留空由系统生成" />
              </n-form-item-gi>
              <n-form-item-gi
                :span="1"
                :label="t('advertising.contract.form.businessEntityId')"
                path="businessEntityId"
              >
                <n-select
                  v-model:value="form.businessEntityId"
                  :options="businessEntityOptions"
                  filterable
                  placeholder="请选择业务主体"
                />
              </n-form-item-gi>
              <n-form-item-gi
                :span="1"
                :label="t('advertising.contract.form.contractDirection')"
                path="contractDirection"
              >
                <n-select v-model:value="form.contractDirection" :options="directionOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.contractType')" path="contractType">
                <n-select v-model:value="form.contractType" :options="typeOptions" placeholder="请选择" />
              </n-form-item-gi>
              <n-form-item-gi
                :span="1"
                :label="t('advertising.contract.form.relatedPartyType')"
                path="relatedPartyType"
              >
                <n-select
                  v-model:value="form.relatedPartyType"
                  :options="filteredRelatedPartyTypeOptions"
                  placeholder="请选择关联方类型"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.relatedPartyId')" path="relatedPartyId">
                <n-select
                  v-model:value="form.relatedPartyId"
                  :options="relatedPartyOptions"
                  filterable
                  placeholder="请先选择关联方类型"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.orderId')">
                <n-select
                  v-model:value="form.orderIds"
                  :options="orderOptions"
                  multiple
                  filterable
                  placeholder="请选择关联订单（可多选）"
                />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.amount')" path="amount">
                <n-input-number v-model:value="form.amount" :min="0" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.signingEntity')">
                <n-input v-model:value="form.signingEntity" placeholder="签约主体" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.validFrom')">
                <n-date-picker v-model:value="form.validFrom" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="1" :label="t('advertising.contract.form.validTo')">
                <n-date-picker v-model:value="form.validTo" type="date" style="width: 100%" />
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.contract.form.fileUrl')">
                <div class="flex flex-col gap-2">
                  <n-upload
                    :custom-request="handleSealUpload"
                    :multiple="true"
                    :show-file-list="false"
                    accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.png"
                  >
                    <n-button size="small" :loading="uploading">+ 添加合同文件</n-button>
                  </n-upload>
                  <div v-if="sealFileList.length" class="contract-file-list">
                    <div v-for="file in sealFileList" :key="(file.id as string)" class="contract-file-item">
                      <span class="file-name" :title="file.name">{{ file.name }}</span>
                      <n-space size="small">
                        <n-button size="tiny" type="primary" ghost @click="handleSealPreview(file)"> 预览 </n-button>
                        <n-button size="tiny" type="primary" ghost @click="handleSealDownload(file)"> 下载 </n-button>
                        <n-button text size="tiny" type="error" @click="handleRemoveSeal(file)">删除</n-button>
                      </n-space>
                    </div>
                  </div>
                </div>
              </n-form-item-gi>
              <n-form-item-gi :span="2" :label="t('advertising.contract.form.rebateTerms')">
                <n-input v-model:value="form.rebateTerms" type="textarea" placeholder="返点条款" />
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
    NForm,
    NFormItemGi,
    NGrid,
    NInput,
    NInputNumber,
    NSelect,
    NUpload,
    useMessage,
  } from 'naive-ui';

  import {
    AdContractDirectionEnum,
    AdContractDirectionOptions,
    AdContractTypeOptions,
    AdRelatedPartyTypeEnum,
    AdRelatedPartyTypeOptions,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdContractSaveParams } from '@lib/shared/models/advertising';

  import CrmCard from '@/components/pure/crm-card/index.vue';

  import {
    createAdContract,
    getAdBusinessEntityPage,
    getAdContractDetail,
    getAdCustomerPage,
    getAdDownstreamMediaPage,
    getAdOrderPage,
    getAdUpstreamAgentPage,
    updateAdContract,
    uploadTempAttachment,
  } from '@/api/modules';
  import useUserStore from '@/store/modules/user';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { AD_SELECT_PAGE_PARAMS } from '../utils';
  import type { UploadFileInfo } from 'naive-ui';

  const directionOptions = AdContractDirectionOptions;
  const typeOptions = AdContractTypeOptions;
  const relatedPartyTypeOptions = AdRelatedPartyTypeOptions;

  type SelectItem = { label: string; value: string };

  const businessEntityOptions = ref<SelectItem[]>([]);
  const relatedPartyOptions = ref<SelectItem[]>([]);
  const orderOptions = ref<SelectItem[]>([]);
  /** 编辑回填期间抑制 watch，避免程序化设置 relatedPartyType 时清空已回填的 relatedPartyId */
  const suppressWatch = ref(false);

  /** 表单本地类型 */
  interface AdContractForm {
    contractNo?: string;
    contractName?: string;
    businessEntityId?: string;
    contractDirection?: number | null;
    contractType?: number | null;
    relatedPartyType?: number | null;
    relatedPartyId?: string;
    orderIds?: string[];
    signingEntity?: string;
    validFrom?: number | null;
    validTo?: number | null;
    amount?: number | null;
    rebateTerms?: string;
  }

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();
  const userStore = useUserStore();

  const id = (route.params.id as string) || '';
  const isEdit = computed(() => !!id);
  const saving = ref(false);
  const uploading = ref(false);
  /** 用印附件列表（多文件，每个元素的 id 为临时文件ID / 已转存附件ID） */
  const sealFileList = ref<UploadFileInfo[]>([]);

  const form = reactive<AdContractForm>({
    contractNo: undefined,
    contractName: undefined,
    businessEntityId: undefined,
    contractDirection: null,
    contractType: null,
    relatedPartyType: null,
    relatedPartyId: undefined,
    orderIds: [],
    signingEntity: undefined,
    validFrom: null,
    validTo: null,
    amount: null,
    rebateTerms: undefined,
  });

  /** 根据合同方向过滤关联方类型选项：
   *  上游(10) → 客户 + 上游代理；下游(20) → 下游客户 */
  const filteredRelatedPartyTypeOptions = computed(() => {
    if (form.contractDirection === AdContractDirectionEnum.UPSTREAM) {
      return relatedPartyTypeOptions.filter(
        (it) => it.value === AdRelatedPartyTypeEnum.CUSTOMER || it.value === AdRelatedPartyTypeEnum.UPSTREAM_AGENT
      );
    }
    if (form.contractDirection === AdContractDirectionEnum.DOWNSTREAM) {
      return relatedPartyTypeOptions.filter((it) => it.value === AdRelatedPartyTypeEnum.DOWNSTREAM_MEDIA);
    }
    return relatedPartyTypeOptions;
  });

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT });
  }

  /** 当前用户 id（用于附件预览/下载鉴权） */
  const currentUserId = computed(() => userStore.userInfo?.id || '');

  /** 上传用印附件（多文件） */
  async function handleSealUpload(opts: { file: any; onFinish: () => void; onError: () => void }) {
    uploading.value = true;
    try {
      const rawFile = opts.file.file as File;
      const res: any = await uploadTempAttachment(rawFile);
      const fileId = res?.data?.[0] || res?.data || '';
      if (!fileId) {
        throw new Error('上传返回异常');
      }
      sealFileList.value.push({ id: fileId, name: rawFile.name, status: 'finished' } as UploadFileInfo);
      message.success('合同文件上传成功');
      opts.onFinish();
    } catch (e) {
      message.error((e as Error).message || '上传失败');
      opts.onError();
    } finally {
      uploading.value = false;
    }
  }

  /** 移除某个用印附件（仅前端列表，保存时由后端 reconcile 删除） */
  function handleRemoveSeal(file: UploadFileInfo) {
    sealFileList.value = sealFileList.value.filter((f) => f.id !== file.id);
  }

  /** 预览：新窗口打开，Cookie 继承当前页面 */
  function handleSealPreview(file: UploadFileInfo) {
    window.open(`/attachment/preview/${file.id}?userId=${currentUserId.value}`, '_blank');
  }

  /** 下载：隐藏 iframe 触发下载，Cookie 继承当前页面 */
  function handleSealDownload(file: UploadFileInfo) {
    const a = document.createElement('a');
    a.href = `/attachment/download/${file.id}?userId=${currentUserId.value}`;
    a.download = file.name || 'contract';
    a.target = '_blank';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  /** 业务主体 / 关联订单 选项（不依赖关联方类型，可预加载） */
  async function loadCommonOptions() {
    try {
      const [beRes, orderRes] = await Promise.all([
        getAdBusinessEntityPage({ ...AD_SELECT_PAGE_PARAMS }),
        getAdOrderPage({ current: 1, pageSize: 1000 }),
      ]);
      businessEntityOptions.value = (beRes.list || []).map((it) => ({
        label: it.name || it.id,
        value: it.id,
      }));
      orderOptions.value = (orderRes.list || []).map((it) => ({
        label: [it.orderNo, it.orderName].filter(Boolean).join(' ') || it.id,
        value: it.id,
      }));
      // 编辑回填时若关联订单已被删除/不在列表中，补一个占位 option，避免 label 空白
      const formOrderIds = form.orderIds || [];
      if (isEdit.value && formOrderIds.length) {
        const knownIds = new Set(orderOptions.value.map((it) => it.value));
        const missing = formOrderIds.filter((oid) => !knownIds.has(oid));
        if (missing.length) {
          orderOptions.value = orderOptions.value.concat(missing.map((oid) => ({ label: oid, value: oid })));
        }
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  /** 关联方选项：依据 relatedPartyType 决定数据源
   *  10 客户 → 客户表；20 上游代理 / 30 下游客户 → 资源表(resourceType 对应) */
  async function loadRelatedPartyOptions(type?: number | null) {
    if (type === 10) {
      try {
        const res = await getAdCustomerPage({ ...AD_SELECT_PAGE_PARAMS });
        relatedPartyOptions.value = (res.list || []).map((it) => ({
          label: it.customerName || it.id,
          value: it.id,
        }));
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(e);
      }
    } else if (type === 20) {
      try {
        const res = await getAdUpstreamAgentPage({ ...AD_SELECT_PAGE_PARAMS, status: 10 });
        relatedPartyOptions.value = (res.list || []).map((it: any) => ({
          label: it.name || it.id,
          value: it.id,
        }));
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(e);
      }
    } else if (type === 30) {
      try {
        const res = await getAdDownstreamMediaPage({ ...AD_SELECT_PAGE_PARAMS, status: 10 });
        relatedPartyOptions.value = (res.list || []).map((it: any) => ({
          label: it.name || it.id,
          value: it.id,
        }));
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(e);
      }
    } else {
      relatedPartyOptions.value = [];
    }
  }

  function buildPayload(): AdContractSaveParams {
    return {
      contractNo: form.contractNo,
      contractName: form.contractName,
      businessEntityId: form.businessEntityId,
      contractDirection: form.contractDirection ?? undefined,
      contractType: form.contractType ?? undefined,
      relatedPartyType: form.relatedPartyType ?? undefined,
      relatedPartyId: form.relatedPartyId,
      orderIds: form.orderIds,
      signingEntity: form.signingEntity,
      validFrom: form.validFrom ?? undefined,
      validTo: form.validTo ?? undefined,
      amount: form.amount ?? undefined,
      rebateTerms: form.rebateTerms,
      sealFileUrls: sealFileList.value.map((f) => ({
        tempFileId: f.id as string,
        fileName: f.name || '',
      })),
    };
  }

  function validate(): boolean {
    if (!form.contractName) {
      message.warning(`${t('advertising.contract.form.contractName')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (!form.businessEntityId) {
      message.warning(`${t('advertising.contract.form.businessEntityId')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.contractDirection === null || form.contractDirection === undefined) {
      message.warning(`${t('advertising.contract.form.contractDirection')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.contractType === null || form.contractType === undefined) {
      message.warning(`${t('advertising.contract.form.contractType')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (form.relatedPartyType === null || form.relatedPartyType === undefined) {
      message.warning(`${t('advertising.contract.form.relatedPartyType')} ${t('advertising.order.form.required')}`);
      return false;
    }
    if (!form.relatedPartyId) {
      message.warning(`${t('advertising.contract.form.relatedPartyId')} ${t('advertising.order.form.required')}`);
      return false;
    }
    // 关联订单非必填：合同与订单是多对多关系（ad_order_contract 中间表），合同可以先建、订单后建
    return true;
  }

  async function handleSave() {
    if (!validate()) return;
    try {
      saving.value = true;
      const payload = buildPayload();
      if (isEdit.value) {
        payload.id = id;
        await updateAdContract(payload);
      } else {
        await createAdContract(payload);
      }
      message.success(t('advertising.common.saveSuccess'));
      router.push({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT });
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    } finally {
      saving.value = false;
    }
  }

  /** 后端日期可能是 number(ms) 或 string(ISO)，统一转 number 供 NDatePicker 使用 */
  function toDateValue(value?: number | string | null): number | null {
    if (value === undefined || value === null || value === '') return null;
    if (typeof value === 'number') return value;
    const parsed = Date.parse(value);
    return Number.isNaN(parsed) ? null : parsed;
  }

  async function loadForEdit() {
    try {
      const res = await getAdContractDetail(id);
      const o = res.contract;
      if (!o) return;
      form.contractNo = o.contractNo;
      form.contractName = o.contractName;
      form.businessEntityId = o.businessEntityId;
      form.contractDirection = o.contractDirection ?? null;
      form.contractType = o.contractType ?? null;
      form.relatedPartyType = o.relatedPartyType ?? null;
      form.relatedPartyId = o.relatedPartyId;
      // 关联订单：AdContract 实体没有 orderIds 字段，需要从详情接口的 orderList 里提取 orderId
      form.orderIds = (res.orderList || []).map((it: any) => it.orderId).filter(Boolean);
      form.signingEntity = o.signingEntity;
      form.validFrom = toDateValue(o.validFrom);
      form.validTo = toDateValue(o.validTo);
      form.amount = o.amount ?? null;
      form.rebateTerms = o.rebateTerms;
      // 用印附件(type=10)：编辑回填到多文件列表（id 为已转存附件ID，可用于预览/下载）
      sealFileList.value = (res.attachments || [])
        .filter((a: any) => a.type === 10)
        .map((a: any) => ({ id: a.fileUrl, name: a.fileName, status: 'finished' } as UploadFileInfo));

      // 防御：若历史数据的关联方类型不在当前方向的允许范围内，则清空
      const allowedTypes = filteredRelatedPartyTypeOptions.value.map((it) => it.value);
      if (form.relatedPartyType != null && !allowedTypes.includes(form.relatedPartyType)) {
        form.relatedPartyType = null;
        form.relatedPartyId = undefined;
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  }

  // 合同方向变化 → 清空已选关联方类型/关联方，避免方向切换后残留旧数据
  watch(
    () => form.contractDirection,
    () => {
      if (suppressWatch.value) return;
      form.relatedPartyType = null;
      form.relatedPartyId = undefined;
      relatedPartyOptions.value = [];
    }
  );

  // 关联方类型变化 → 清空已选关联方并重新拉取对应数据源
  watch(
    () => form.relatedPartyType,
    (val) => {
      if (suppressWatch.value) return;
      form.relatedPartyId = undefined;
      loadRelatedPartyOptions(val);
    }
  );

  onMounted(async () => {
    await loadCommonOptions();
    if (isEdit.value) {
      suppressWatch.value = true;
      await loadForEdit();
      suppressWatch.value = false;
      // 编辑回填后，依据(已回填的)关联方类型加载对应选项
      loadRelatedPartyOptions(form.relatedPartyType);
    } else {
      // 新建：类型未选，选项置空
      loadRelatedPartyOptions(form.relatedPartyType);
    }
  });
</script>

<style scoped>
  .contract-file-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }
  .contract-file-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 4px 10px;
    border: 1px solid var(--text-n8);
    border-radius: 4px;
    background: var(--text-n10);
    font-size: 12px;
  }
  .contract-file-item .file-name {
    max-width: 320px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--text-n2);
  }
</style>
