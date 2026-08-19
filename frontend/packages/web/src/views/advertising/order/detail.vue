<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ detail.order.orderName || detail.order.orderNo }}</span>
            <n-tag :type="statusTagType(detail.order.status)">{{ getAdOrderStatusLabel(detail.order.status) }}</n-tag>
            <n-tag v-if="detail.order.needsRedInvoice === 1" type="error">{{
              t('advertising.order.detail.redInvoice')
            }}</n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <n-button
              v-if="detail.order.status === 0"
              v-permission="['AD_ORDER:SUBMIT']"
              type="primary"
              @click="handleSubmit"
              >提交</n-button
            >
            <n-button
              v-if="detail.order.status === 10"
              v-permission="['AD_ORDER:APPROVE']"
              type="primary"
              @click="openModal('approve')"
              >审核通过</n-button
            >
            <n-button v-if="detail.order.status === 10" v-permission="['AD_ORDER:REJECT']" @click="openModal('reject')"
              >驳回</n-button
            >
            <n-button
              v-if="detail.order.status === 45"
              v-permission="['AD_ORDER:CONFIRM_EXECUTE']"
              type="primary"
              @click="handleConfirmExecute"
              >确认执行</n-button
            >
            <n-button
              v-if="detail.order.status !== 90 && detail.order.status !== 100"
              v-permission="['AD_ORDER:VOID']"
              type="warning"
              @click="openModal('void')"
            >
              作废
            </n-button>
            <n-button
              v-if="detail.order.status === 80"
              v-permission="['AD_ORDER:FORCE_ARCHIVE']"
              type="error"
              @click="openModal('forceArchive')"
              >强制归档</n-button
            >
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-divider title-placement="left">{{ t('advertising.order.detail.tab.base') }}</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="订单编号">{{ detail.order.orderNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="订单名称">{{ detail.order.orderName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="业务主体">{{
            getEntityName('entity', detail.order.businessEntityId) || '-'
          }}</n-descriptions-item>
          <n-descriptions-item label="客户名称">{{
            getEntityName('customer', detail.order.customerId) || '-'
          }}</n-descriptions-item>
          <n-descriptions-item label="行业类别">{{ industryLabelMap[detail.order.industryCode] || detail.order.industryCode || '-' }}</n-descriptions-item>
          <n-descriptions-item label="签约主体">{{ detail.order.signingEntity || '-' }}</n-descriptions-item>
          <n-descriptions-item label="下游媒体">{{ downstreamMediaNames || '-' }}</n-descriptions-item>
          <n-descriptions-item label="关联合同">{{
            [detail.contractNo, detail.contractName].filter(Boolean).join(' ') || '-'
          }}</n-descriptions-item>
          <n-descriptions-item label="订单类型">{{ getAdOrderTypeLabel(detail.order.orderType) }}</n-descriptions-item>
          <n-descriptions-item label="收款方式">{{
            getAdReceiptMethodLabel(detail.order.receiptMethod)
          }}</n-descriptions-item>
          <n-descriptions-item label="付款方式">{{
            getAdPaymentMethodLabel(detail.order.paymentMethod)
          }}</n-descriptions-item>
          <n-descriptions-item label="投放起始">{{ fmtDate(detail.order.deliveryStartDate) }}</n-descriptions-item>
          <n-descriptions-item label="投放结束">{{ fmtDate(detail.order.deliveryEndDate) }}</n-descriptions-item>
          <n-descriptions-item label="投放量">{{ detail.order.deliveryVolume || '-' }}</n-descriptions-item>
          <n-descriptions-item label="币种">{{ detail.order.currency || '-' }}</n-descriptions-item>
          <n-descriptions-item label="下单人">{{ getUserName(detail.order.creatorId) }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">{{ t('advertising.order.detail.tab.base') }} - 金额</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item :label="t('advertising.order.column.totalAmount')">{{
            fmtAmount(detail.order.totalAmount)
          }}</n-descriptions-item>
          <n-descriptions-item label="应收金额">{{ fmtAmount(detail.order.receivableAmount) }}</n-descriptions-item>
          <n-descriptions-item label="返点金额">{{ fmtAmount(detail.order.rebateAmount) }}</n-descriptions-item>
          <n-descriptions-item label="媒体应付">{{ fmtAmount(detail.order.mediaPayableAmount) }}</n-descriptions-item>
          <n-descriptions-item :label="t('advertising.order.detail.amount.received')">{{
            fmtAmount(detail.order.receivedAmount)
          }}</n-descriptions-item>
          <n-descriptions-item :label="t('advertising.order.detail.amount.invoiced')">{{
            fmtAmount(detail.order.invoicedAmount)
          }}</n-descriptions-item>
          <n-descriptions-item :label="t('advertising.order.detail.amount.mediaPaid')">{{
            fmtAmount(detail.order.mediaPaidAmount)
          }}</n-descriptions-item>
          <n-descriptions-item :label="t('advertising.order.detail.amount.badDebt')">{{
            fmtAmount(detail.order.badDebtAmount)
          }}</n-descriptions-item>
        </n-descriptions>

        <n-divider title-placement="left">{{ t('advertising.order.detail.tab.attachment') }}</n-divider>

        <!-- 上传区域 -->
        <n-space v-if="detail.order.status === 0" justify="start" style="margin-bottom: 12px">
          <n-upload
            :custom-request="(opts: any) => handleUpload(10, opts)"
            :show-file-list="false"
            accept=".pdf,.jpg,.png,.doc,.docx,.xls,.xlsx"
          >
            <n-button type="primary" size="small" :loading="uploading">上传盖章排期（必需）</n-button>
          </n-upload>
          <n-upload
            :custom-request="(opts: any) => handleUpload(20, opts)"
            :show-file-list="false"
            accept=".pdf,.jpg,.png,.doc,.docx,.xls,.xlsx"
          >
            <n-button type="primary" size="small" :loading="uploading">上传邮件截图（必需）</n-button>
          </n-upload>
          <n-upload
            :custom-request="(opts: any) => handleUpload(40, opts)"
            :show-file-list="false"
            accept=".pdf,.jpg,.png,.doc,.docx,.xls,.xlsx"
          >
            <n-button size="small" :loading="uploading">补充协议（选填）</n-button>
          </n-upload>
        </n-space>

        <n-empty
          v-if="!detail.attachments || detail.attachments.length === 0"
          :description="'请上传盖章排期和邮件截图'"
        />
        <n-space v-else vertical>
          <div v-for="att in detail.attachments" :key="att.id" class="flex items-center gap-2">
            <span class="max-w-[300px] truncate text-[var(--text-n2)]" :title="att.fileName || att.fileUrl">
              {{ att.fileName || att.fileUrl }}
            </span>
            <n-tag size="small">{{ attTypeLabel(att.type) }}</n-tag>
            <n-button size="tiny" type="primary" ghost @click.prevent="handlePreviewAtt(att.fileUrl!)"> 预览 </n-button>
            <n-button size="tiny" type="primary" ghost @click.prevent="handleDownloadAtt(att)"> 下载 </n-button>
            <n-button
              v-if="detail.order.status === 0"
              text
              size="tiny"
              type="error"
              @click="handleDeleteAttachment(att.id)"
            >
              删除
            </n-button>
          </div>
        </n-space>

        <n-divider title-placement="left">{{ t('advertising.order.detail.tab.change') }}</n-divider>
        <n-empty v-if="!executedChanges.length" description="暂无已执行的改单记录" />
        <n-space v-else vertical>
          <n-card v-for="ch in executedChanges" :key="ch.id" size="small">
            <n-space align="center" wrap>
              <span>改单ID: {{ ch.id }}</span>
              <n-tag type="success">{{ getAdOrderChangeStatusLabel(ch.status) }}</n-tag>
              <span>审批人: {{ getUserName(ch.approverId) }}</span>
              <span>审批时间: {{ fmtDateTime(ch.approvedAt) }}</span>
              <span>创建人: {{ getUserName(ch.createUser) }}</span>
              <span>创建时间: {{ fmtDateTime(ch.createTime) }}</span>
            </n-space>
            <div style="margin-top: 6px">原因: {{ ch.reason || '-' }}</div>
            <n-table :bordered="true" size="small" :single-line="false" style="margin-top: 8px">
              <thead>
                <tr>
                  <th style="width: 160px">变更字段</th>
                  <th>原值（改前）</th>
                  <th>新值（改后）</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, idx) in changeCompareRows(ch)" :key="idx">
                  <td>{{ row.label }}</td>
                  <td class="before-value">{{ row.before }}</td>
                  <td class="after-value">{{ row.after }}</td>
                </tr>
              </tbody>
            </n-table>
          </n-card>
        </n-space>

        <n-divider title-placement="left">{{ t('advertising.order.detail.tab.action') }}</n-divider>
        <n-empty
          v-if="!detail.allowedActions || detail.allowedActions.length === 0"
          :description="t('advertising.order.detail.allowedActions.empty')"
        />
        <n-space v-else>
          <n-tag v-for="(act, i) in detail.allowedActions" :key="i" :type="act.allowed ? 'success' : 'default'">
            {{ act.label || act.trigger }}
          </n-tag>
        </n-space>

        <n-divider title-placement="left">{{ t('advertising.order.detail.tab.log') }}</n-divider>
        <n-empty v-if="!detail.logs || detail.logs.length === 0" description="暂无操作记录" />
        <n-timeline v-else>
          <n-timeline-item v-for="log in detail.logs" :key="log.id" :time="fmtDateTime(log.createTime)">
            <template #header>
              <span class="log-header">{{ getLogActionLabel(log.action) }}</span>
              <span class="log-operator">操作人：{{ getUserName(log.operatorId) }}</span>
            </template>
            <div class="log-content">{{ formatLogChange(log) }}</div>
          </n-timeline-item>
        </n-timeline>
      </n-card>
    </n-spin>

    <n-modal v-model:show="actionModal.show" :title="actionModalTitle" preset="card" style="width: 480px">
      <n-space vertical>
        <div v-if="actionModal.type !== 'forceArchive'">
          <div class="action-modal-label">
            备注
            <span v-if="actionModal.type === 'reject'" class="required-mark">*</span>
          </div>
          <n-input
            v-model:value="actionModal.remark"
            type="textarea"
            :rows="3"
            :status="actionModal.type === 'reject' && !actionModal.remark.trim() ? 'error' : undefined"
            :placeholder="actionModal.type === 'reject' ? '请输入驳回原因（必填）' : '请输入备注'"
          />
        </div>
        <n-input-number
          v-else
          v-model:value="actionModal.badDebt"
          :min="0"
          :precision="2"
          :show-button="false"
          placeholder="坏账金额"
          style="width: 100%"
        />
      </n-space>
      <template #footer>
        <n-space justify="end">
          <n-button @click="closeModal">取消</n-button>
          <n-button type="primary" @click="handleActionConfirm">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDescriptions,
    NDescriptionsItem,
    NDivider,
    NEmpty,
    NInput,
    NInputNumber,
    NModal,
    NSpace,
    NSpin,
    NTable,
    NTag,
    NTimeline,
    NTimelineItem,
    NUpload,
    useDialog,
    useMessage,
  } from 'naive-ui';

  import {
    AD_ORDER_CHANGE_FIELD_META,
    AdModeOptions,
    AdOrderTypeOptions,
    AdPaymentMethodOptions,
    AdPostpayTriggerOptions,
    AdReceiptMethodOptions,
    getAdOrderChangeStatusLabel,
    getAdOrderStatusLabel,
    getAdOrderTypeLabel,
    getAdPaymentMethodLabel,
    getAdReceiptMethodLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderChange, AdOrderDetail } from '@lib/shared/models/advertising';

  import {
    approveAdOrder,
    confirmExecuteAdOrder,
    deleteAdOrderAttachment,
    forceArchiveAdOrder,
    getAdBusinessEntityPage,
    getAdCustomerPage,
    getAdDictPage,
    getAdDownstreamMediaPage,
    getAdOrderDetail,
    submitAdOrder,
    uploadAdOrderAttachment,
    voidAdOrder,
  } from '@/api/modules';
  import useUserStore from '@/store/modules/user';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtAmount, fmtDate, fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const { loadUserMap, getUserName } = useUserMap();
  const message = useMessage();
  const dialog = useDialog();
  const userStore = useUserStore();

  const orderId = route.params.id as string;
  const loading = ref(false);
  const uploading = ref(false);
  const detail = ref<AdOrderDetail | null>(null);

  const actionModal = reactive<{
    show: boolean;
    type: '' | 'approve' | 'reject' | 'void' | 'forceArchive';
    remark: string;
    badDebt: number | null;
  }>({
    show: false,
    type: '',
    remark: '',
    badDebt: null,
  });

  const actionModalTitle = computed(() => {
    switch (actionModal.type) {
      case 'approve':
        return '审核通过';
      case 'reject':
        return '驳回';
      case 'void':
        return '作废订单';
      case 'forceArchive':
        return '强制归档';
      default:
        return '';
    }
  });

  function statusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    switch (status) {
      case 0:
        return 'default';
      case 10:
        return 'warning';
      case 45:
      case 50:
      case 60:
        return 'info';
      case 80:
      case 90:
        return 'success';
      case 100:
        return 'error';
      default:
        return 'default';
    }
  }

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdOrderDetail(orderId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  /** 实体名称缓存：type-key-id → name（用于详情页"业务主体/客户"ID 转名称） */
  const entityNameCache = reactive<Record<string, string>>({});

  /** 同步读取缓存，未命中时返回 id 兜底 */
  function getEntityName(type: 'entity' | 'customer', id?: string | null) {
    if (!id) return '';
    const key = `${type}-${id}`;
    return entityNameCache[key] || id;
  }

  /** 预加载业务主体/客户列表到缓存（页面挂载后调用） */
  async function loadEntityNames() {
    try {
      const [beRes, cuRes] = await Promise.all([
        getAdBusinessEntityPage({ current: 1, pageSize: 200 }),
        getAdCustomerPage({ current: 1, pageSize: 200 }),
      ]);
      (beRes.list || []).forEach((it: any) => {
        if (it.id) entityNameCache[`entity-${it.id}`] = it.name || '';
      });
      (cuRes.list || []).forEach((it: any) => {
        if (it.id) entityNameCache[`customer-${it.id}`] = it.customerName || it.name || '';
      });
    } catch {
      // 静默失败
    }
  }

  /** 下游媒体名称缓存 */
  const downstreamMediaNameCache = reactive<Record<string, string>>({});

  /** 加载下游媒体名称到缓存 */
  async function loadDownstreamMediaNames() {
    try {
      const res = await getAdDownstreamMediaPage({ current: 1, pageSize: 200 });
      (res.list || []).forEach((it: any) => {
        if (it.id) downstreamMediaNameCache[it.id] = it.name || '';
      });
    } catch {
      // 静默失败
    }
  }

  /** 行业类别字典缓存：dictValue → dictLabel */
  const industryLabelMap = reactive<Record<string, string>>({});

  /** 加载行业类别字典 */
  async function loadIndustryDict() {
    try {
      const res = await getAdDictPage({ current: 1, pageSize: 200, dictCode: 'industry' });
      (res.list || []).forEach((it: any) => {
        const value = it.dictValue || it.id;
        if (value) industryLabelMap[value] = it.dictLabel || value;
      });
    } catch {
      // 静默失败
    }
  }

  /** 下游媒体名称（逗号分隔） */
  const downstreamMediaNames = computed(() => {
    const ids = detail.value?.downstreamMediaIds;
    if (!ids || ids.length === 0) return '';
    return ids.map((id) => downstreamMediaNameCache[id] || id).join('、');
  });

  /** 操作记录 action 中文映射 */
  function getLogActionLabel(action?: string): string {
    const map: Record<string, string> = {
      SUBMIT: '提交审核',
      APPROVE: '审核通过',
      REJECT: '驳回',
      CHANGE: '提交改单',
      APPLY_CHANGE: '申请改单',
      CHANGE_APPROVED: '改单通过',
      CHANGE_REJECTED: '改单驳回',
      CHANGE_EXECUTED: '改单执行',
      CHANGE_MONEY_SIDE: '金额差异',
      CONFIRM_EXECUTE: '确认执行',
      AUTO_OVERDUE: '到期自动结算',
      AUTO_ARCHIVE: '自动归档',
      AUTO_PREPAY: '自动推算预付',
      AUTO_EXECUTE: '自动进入执行',
      COMPLETE_EXECUTE: '完成执行',
      ARCHIVE: '归档',
      FORCE_ARCHIVE: '强制归档',
      SUBMIT_ARCHIVE: '提交归档',
      APPROVE_ARCHIVE: '归档通过',
      REJECT_ARCHIVE: '归档驳回',
      UPLOAD_DOUBLE_SEAL: '上传双盖',
      VOID: '作废',
    };
    return action && map[action] ? map[action] : action || '-';
  }

  /** 改单变更字段：把英文 key 转中文 label（用 AD_ORDER_CHANGE_FIELD_META 映射） */
  function formatChangeFields(value?: string | null): string {
    if (!value) return '-';
    let fields: string[] = [];
    if (value.trim().startsWith('[')) {
      try {
        const arr = JSON.parse(value);
        if (Array.isArray(arr)) fields = arr;
      } catch {
        // ignore
      }
    } else {
      fields = value
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);
    }
    if (!fields.length) return '-';
    return fields.map((f) => AD_ORDER_CHANGE_FIELD_META.find((m) => m.field === f)?.label || f).join('、');
  }

  /** 只展示已执行(40)的改单记录 */
  const executedChanges = computed(() => {
    const changes = detail.value?.changes;
    if (!changes || !changes.length) return [];
    return changes.filter((c) => c.status === 40);
  });

  /** 枚举 Options 映射（用于改单字段前后值转名称） */
  const enumOptionsMap: Record<string, { label: string; value: number }[]> = {
    'enum-orderType': AdOrderTypeOptions as { label: string; value: number }[],
    'enum-rebateMode': AdModeOptions as { label: string; value: number }[],
    'enum-receiptMethod': AdReceiptMethodOptions as { label: string; value: number }[],
    'enum-paymentMethod': AdPaymentMethodOptions as { label: string; value: number }[],
    'enum-prepayMode': AdModeOptions as { label: string; value: number }[],
    'enum-postpayTrigger': AdPostpayTriggerOptions as { label: string; value: number }[],
  };

  function changeValLabel(field: string, v: any): string {
    if (v === null || v === undefined || v === '') return '-';
    const meta = AD_ORDER_CHANGE_FIELD_META.find((m) => m.field === field);
    if (!meta) return String(v);
    if (meta.type === 'date') {
      const ts = Number(v);
      if (!Number.isNaN(ts) && String(v).length >= 10) {
        const d = new Date(ts < 1e12 ? ts * 1000 : ts);
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
      }
      return String(v).slice(0, 10);
    }
    if (meta.control === 'select-customer') return entityNameCache[`customer-${v}`] || String(v);
    if (meta.control === 'select-businessEntity') return entityNameCache[`entity-${v}`] || String(v);
    if (meta.control && enumOptionsMap[meta.control]) {
      const found = enumOptionsMap[meta.control].find((o) => String(o.value) === String(v));
      return found ? found.label : String(v);
    }
    return String(v);
  }

  function parseChangeFields(value?: string | null): string[] {
    if (!value) return [];
    if (value.trim().startsWith('[')) {
      try {
        const arr = JSON.parse(value);
        return Array.isArray(arr) ? arr : [];
      } catch {
        // ignore
      }
    }
    return value
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
  }

  function parseSnapshotJson(value?: string | null): Record<string, any> {
    if (!value) return {};
    try {
      return JSON.parse(value);
    } catch {
      return {};
    }
  }

  /** 单个改单的字段前后值对比行 */
  function changeCompareRows(ch: AdOrderChange): { label: string; before: string; after: string }[] {
    const before = parseSnapshotJson(ch.snapshotBefore);
    const after = parseSnapshotJson(ch.snapshotAfter);
    const fields = parseChangeFields(ch.changeFields);
    return fields.map((field) => {
      const meta = AD_ORDER_CHANGE_FIELD_META.find((m) => m.field === field);
      return {
        label: meta?.label || field,
        before: changeValLabel(field, before[field]),
        after: changeValLabel(field, after[field]),
      };
    });
  }

  /** 解析 before/after JSON 中的 status 字段为可读文字 */
  function getStatusLabelFromJson(json?: string): string {
    if (!json) return '';
    try {
      const obj = JSON.parse(json);
      if (obj.status != null) return getAdOrderStatusLabel(obj.status);
      return '';
    } catch {
      return json;
    }
  }

  /** 操作记录描述（解析 status + 驳回 remark） */
  function formatLogChange(log: any): string {
    const before = getStatusLabelFromJson(log.beforeValue);
    const after = getStatusLabelFromJson(log.afterValue);
    let text = '';
    if (before && after && before !== after) {
      text = `${before} → ${after}`;
    } else if (after && before !== after) {
      text = `→ ${after}`;
    } else if (before && before !== after) {
      text = before;
    }
    // 驳回原因（写入 afterValue.remark）
    if (log.afterValue) {
      try {
        const obj = JSON.parse(log.afterValue);
        if (obj.remark) {
          text = `${text}  原因：${obj.remark}`;
        }
      } catch {
        // ignore
      }
    }
    return text || '-';
  }

  /** 附件类型标签 */
  function attTypeLabel(type?: number): string {
    switch (type) {
      case 10:
        return '盖章排期';
      case 20:
        return '邮件截图';
      case 30:
        return '合同';
      case 40:
        return '补充协议';
      case 50:
        return '改单附件';
      default:
        return '其他';
    }
  }

  /** 上传附件（type: 10盖章排期 / 20邮件截图 / 40补充协议） */
  async function handleUpload(type: number, opts: { file: any; onFinish: () => void; onError: () => void }) {
    uploading.value = true;
    try {
      // n-upload 的 custom-request 传入的 file 是 UploadFileInfo，原生 File 在 .file 属性里
      const rawFile = opts.file.file as File;
      await uploadAdOrderAttachment(orderId, type, rawFile);
      message.success('上传成功');
      opts.onFinish();
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '上传失败');
      opts.onError();
    } finally {
      uploading.value = false;
    }
  }

  /** 删除附件 */
  async function handleDeleteAttachment(attachmentId: string) {
    try {
      await deleteAdOrderAttachment(orderId, attachmentId);
      message.success('删除成功');
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '删除失败');
    }
  }

  /** 附件预览 */
  function handlePreviewAtt(fileUrl: string) {
    const previewUrl = `/attachment/preview/${fileUrl}?userId=${userStore.userInfo?.id || ''}`;
    window.open(previewUrl, '_blank');
  }

  /** 附件下载 */
  function handleDownloadAtt(att: any) {
    const downloadUrl = `/attachment/download/${att.fileUrl}?userId=${userStore.userInfo?.id || ''}`;
    const a = document.createElement('a');
    a.href = downloadUrl;
    a.download = att.fileName || 'attachment';
    a.target = '_blank';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_ORDER });
  }

  function openModal(type: 'approve' | 'reject' | 'void' | 'forceArchive') {
    actionModal.type = type;
    actionModal.remark = '';
    actionModal.badDebt = null;
    actionModal.show = true;
  }
  function closeModal() {
    actionModal.show = false;
  }

  async function handleActionConfirm() {
    // 驳回时备注必填
    if (actionModal.type === 'reject' && !actionModal.remark.trim()) {
      message.warning('驳回时必须填写原因');
      return;
    }
    try {
      switch (actionModal.type) {
        case 'approve':
          await approveAdOrder(orderId, { action: 'APPROVE', remark: actionModal.remark });
          break;
        case 'reject':
          await approveAdOrder(orderId, { action: 'REJECT', remark: actionModal.remark });
          break;
        case 'void':
          await voidAdOrder(orderId, { reason: actionModal.remark });
          break;
        case 'forceArchive':
          await forceArchiveAdOrder(orderId, { badDebtAmount: actionModal.badDebt ?? undefined });
          break;
        default:
          break;
      }
      message.success(t('advertising.common.operateSuccess'));
      actionModal.show = false;
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  async function handleSubmit() {
    try {
      await submitAdOrder(orderId);
      message.success(t('advertising.common.operateSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }
  async function handleConfirmExecute() {
    try {
      await confirmExecuteAdOrder(orderId);
      message.success(t('advertising.common.operateSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  onMounted(async () => {
    await Promise.all([
      fetchDetail(),
      loadUserMap(),
      loadEntityNames(),
      loadDownstreamMediaNames(),
      loadIndustryDict(),
    ]);
  });
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
    height: calc(100vh - 64px);
    overflow-y: auto;
  }
  .before-value {
    color: #999;
    word-break: break-all;
  }
  .after-value {
    color: #18a058;
    word-break: break-all;
  }
</style>

<style scoped>
  .action-modal-label {
    margin-bottom: 4px;
    font-size: 12px;
    color: var(--text-n2);
  }
  .action-modal-label .required-mark {
    color: var(--error-red);
    margin-left: 2px;
  }
  .log-header {
    font-weight: 600;
    margin-right: 12px;
  }
  .log-operator {
    color: var(--text-n3);
    font-size: 12px;
    margin-left: 4px;
  }
  .log-content {
    color: var(--text-n2);
    font-size: 12px;
    margin-top: 4px;
  }
</style>
