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
            <n-button v-if="detail.order.status === 0" type="primary" @click="handleSubmit">提交</n-button>
            <n-button v-if="detail.order.status === 10" type="primary" @click="openModal('approve')">审核通过</n-button>
            <n-button v-if="detail.order.status === 10" @click="openModal('reject')">驳回</n-button>
            <n-button v-if="[20, 30, 40].includes(detail.order.status || 0)" @click="handleConfirmExecute"
              >确认执行</n-button
            >
            <n-button v-if="detail.order.status === 50" @click="handleCompleteExecute">执行完成</n-button>
            <n-button v-if="detail.order.status === 20" @click="handleFinancialPreAction">财务前置</n-button>
            <n-button
              v-if="[0, 10, 50, 60, 70, 80].includes(detail.order.status || 0)"
              type="warning"
              @click="openModal('void')"
            >
              作废
            </n-button>
            <n-button v-if="detail.order.status === 80" type="error" @click="openModal('forceArchive')"
              >强制归档</n-button
            >
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <n-divider title-placement="left">{{ t('advertising.order.detail.tab.base') }}</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="订单编号">{{ detail.order.orderNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="订单名称">{{ detail.order.orderName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="业务主体">{{ detail.order.businessEntityId || '-' }}</n-descriptions-item>
          <n-descriptions-item label="客户ID">{{ detail.order.customerId || '-' }}</n-descriptions-item>
          <n-descriptions-item label="行业类别">{{ detail.order.industryCode || '-' }}</n-descriptions-item>
          <n-descriptions-item label="签约主体">{{ detail.order.signingEntity || '-' }}</n-descriptions-item>
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
          <n-descriptions-item label="下单人">{{ detail.order.creatorId || '-' }}</n-descriptions-item>
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
        <n-empty
          v-if="!detail.attachments || detail.attachments.length === 0"
          :description="t('advertising.order.detail.attachment.empty')"
        />
        <n-space v-else vertical>
          <div v-for="att in detail.attachments" :key="att.id">
            <a :href="att.fileUrl" target="_blank" rel="noopener">{{ att.fileName || att.fileUrl }}</a>
          </div>
        </n-space>

        <n-divider title-placement="left">{{ t('advertising.order.detail.tab.change') }}</n-divider>
        <n-empty v-if="!detail.changes || detail.changes.length === 0" description="暂无改单记录" />
        <n-space v-else vertical>
          <n-card v-for="ch in detail.changes" :key="ch.id" size="small">
            <n-space align="center">
              <span>改单ID: {{ ch.id }}</span>
              <n-tag>{{ getAdOrderChangeStatusLabel(ch.status) }}</n-tag>
              <span>审批人: {{ ch.approverId || '-' }}</span>
            </n-space>
            <div>原因: {{ ch.reason || '-' }}</div>
            <div>变更字段: {{ ch.changeFields || '-' }}</div>
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
          <n-timeline-item
            v-for="log in detail.logs"
            :key="log.id"
            :content="`${log.operatorId || ''} ${log.beforeValue || ''} -> ${log.afterValue || ''}`"
            :time="fmtDateTime(log.createTime)"
          >
            <template #header>{{ log.action }}</template>
          </n-timeline-item>
        </n-timeline>
      </n-card>
    </n-spin>

    <n-modal
      v-model:show="actionModal.show"
      :title="actionModalTitle"
      preset="card"
      positive-text="确定"
      negative-text="取消"
      style="width: 480px"
      @positive-click="handleActionConfirm"
      @negative-click="closeModal"
    >
      <n-space vertical>
        <n-input
          v-if="actionModal.type !== 'forceArchive'"
          v-model:value="actionModal.remark"
          type="textarea"
          :rows="3"
          :placeholder="t('advertising.common.remark')"
        />
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
    NTag,
    NTimeline,
    NTimelineItem,
    useDialog,
    useMessage,
  } from 'naive-ui';

  import {
    getAdOrderChangeStatusLabel,
    getAdOrderStatusLabel,
    getAdOrderTypeLabel,
    getAdPaymentMethodLabel,
    getAdReceiptMethodLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdOrderDetail } from '@lib/shared/models/advertising';

  import {
    approveAdOrder,
    completeExecuteAdOrder,
    confirmExecuteAdOrder,
    financialPreActionAdOrder,
    forceArchiveAdOrder,
    getAdOrderDetail,
    submitAdOrder,
    voidAdOrder,
  } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtAmount, fmtDate, fmtDateTime } from '../utils';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();
  const dialog = useDialog();

  const orderId = route.params.id as string;
  const loading = ref(false);
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
      case 20:
      case 50:
        return 'info';
      case 70:
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
  async function handleCompleteExecute() {
    try {
      await completeExecuteAdOrder(orderId);
      message.success(t('advertising.common.operateSuccess'));
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }
  async function handleFinancialPreAction() {
    try {
      const plan = await financialPreActionAdOrder(orderId);
      const lines = (plan.steps || []).map((s) => `${s.description || s.action}: ${fmtAmount(s.amount)}`).join('\n');
      dialog.info({
        title: '财务前置动作矩阵',
        content: `推导状态: ${plan.toStatus}\n预收金额: ${fmtAmount(
          plan.receiptPrepayAmount
        )}\n媒体预付金额: ${fmtAmount(plan.paymentPrepayAmount)}\n${lines}`,
      });
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  onMounted(fetchDetail);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
</style>
