<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="filter-card">
      <n-space align="center" wrap>
        <n-space :size="8" align="center">
          <n-switch v-model:value="mine" />
          <span>{{ t('advertising.report.filter.mine') }}</span>
        </n-space>
        <n-date-picker
          v-model:value="yearValue"
          type="year"
          clearable
          :placeholder="t('advertising.report.filter.year')"
          style="width: 160px"
          @update:value="onYearChange"
        />
        <n-button type="primary" @click="fetchData">{{ t('advertising.common.cancel') }}</n-button>
      </n-space>
    </n-card>

    <div class="report-grid">
      <!-- 订单执行 -->
      <n-card :title="t('advertising.report.card.orderExecution')" :bordered="false">
        <n-spin :show="loading">
          <template v-if="orderSummary">
            <n-space vertical :size="8">
              <n-statistic :label="t('advertising.report.summary.orderCount')" :value="orderSummary.total || 0" />
              <n-statistic
                :label="t('advertising.report.summary.totalAmount')"
                :value="fmtAmount(orderSummary.totalAmount)"
              />
              <n-list v-if="orderSummary.items && orderSummary.items.length" bordered>
                <n-list-item v-for="(it, i) in orderSummary.items" :key="i">
                  <n-space justify="space-between">
                    <span>{{ it.statusLabel }}</span>
                    <n-space :size="12">
                      <span>{{ it.count }}</span>
                      <span class="amount">{{ fmtAmount(it.amount) }}</span>
                    </n-space>
                  </n-space>
                </n-list-item>
              </n-list>
            </n-space>
          </template>
          <n-empty v-else :description="t('advertising.report.pending')" />
        </n-spin>
      </n-card>

      <!-- 应收应付 -->
      <n-card :title="t('advertising.report.card.receivablePayable')" :bordered="false">
        <n-spin :show="loading">
          <template v-if="paymentSummary">
            <n-space vertical :size="8">
              <n-statistic
                :label="t('advertising.report.summary.receivable')"
                :value="fmtAmount(paymentSummary.receivableAmount)"
              />
              <n-statistic
                :label="t('advertising.report.summary.mediaPayable')"
                :value="fmtAmount(paymentSummary.mediaPayableAmount)"
              />
              <n-statistic
                :label="t('advertising.report.summary.paid')"
                :value="fmtAmount(paymentSummary.paidAmount)"
              />
              <n-statistic
                :label="t('advertising.report.summary.unpaid')"
                :value="fmtAmount(paymentSummary.unpaidAmount)"
              />
              <n-statistic
                :label="t('advertising.report.summary.overdue')"
                :value="fmtAmount(paymentSummary.overdueAmount)"
              />
            </n-space>
          </template>
          <n-empty v-else :description="t('advertising.report.pending')" />
        </n-spin>
      </n-card>

      <!-- 回款追踪 -->
      <n-card :title="t('advertising.report.card.collection')" :bordered="false">
        <n-spin :show="loading">
          <report-item
            :item="collection"
            :pending-text="t('advertising.report.pending')"
            :amount-label="t('advertising.report.summary.totalAmount')"
            :count-label="t('advertising.report.summary.orderCount')"
          />
        </n-spin>
      </n-card>

      <!-- 投放 -->
      <n-card :title="t('advertising.report.card.media')" :bordered="false">
        <n-spin :show="loading">
          <report-item
            :item="media"
            :pending-text="t('advertising.report.pending')"
            :amount-label="t('advertising.report.summary.totalAmount')"
            :count-label="t('advertising.report.summary.orderCount')"
          />
        </n-spin>
      </n-card>

      <!-- 缺合同 -->
      <n-card :title="t('advertising.report.card.contractMissing')" :bordered="false">
        <n-spin :show="loading">
          <report-item
            :item="contractMissing"
            :pending-text="t('advertising.report.pending')"
            :amount-label="t('advertising.report.summary.totalAmount')"
            :count-label="t('advertising.report.summary.missingCount')"
          />
        </n-spin>
      </n-card>

      <!-- 用印统计 -->
      <n-card :title="t('advertising.report.card.seal')" :bordered="false">
        <n-spin :show="loading">
          <report-item
            :item="seal"
            :pending-text="t('advertising.report.pending')"
            :amount-label="t('advertising.report.summary.totalAmount')"
            :count-label="t('advertising.report.summary.sealCount')"
          />
        </n-spin>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { defineComponent, h, onMounted, PropType, ref } from 'vue';
  import { NButton, NCard, NDatePicker, NEmpty, NList, NListItem, NSpace, NSpin, NStatistic, NSwitch } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type {
    AdReportItem,
    AdReportMonthlyTrendResult,
    AdReportOrderSummary,
    AdReportPaymentSummary,
  } from '@lib/shared/models/advertising';

  import {
    getAdReportByCode,
    getAdReportMonthlyTrend,
    getAdReportOrderSummary,
    getAdReportPaymentSummary,
  } from '@/api/modules';

  import { fmtAmount } from '../utils';

  const { t } = useI18n();

  const loading = ref(false);
  const mine = ref(false);
  const year = ref(new Date().getFullYear());
  const yearValue = ref<number | null>(new Date(year.value, 0, 1).getTime());

  const orderSummary = ref<AdReportOrderSummary | null>(null);
  const paymentSummary = ref<AdReportPaymentSummary | null>(null);
  const monthlyTrend = ref<AdReportMonthlyTrendResult | null>(null);
  const collection = ref<AdReportItem | null>(null);
  const media = ref<AdReportItem | null>(null);
  const contractMissing = ref<AdReportItem | null>(null);
  const seal = ref<AdReportItem | null>(null);

  // 6 类报表通用渲染组件（后端待补时展示“后端接口待补”）
  const ReportItem = defineComponent({
    name: 'ReportItem',
    props: {
      item: { type: Object as PropType<AdReportItem | null>, default: null },
      pendingText: { type: String, default: '' },
      countLabel: { type: String, default: '' },
      amountLabel: { type: String, default: '' },
    },
    setup(props) {
      return () => {
        const { item } = props;
        return item
          ? h(NSpace, { vertical: true, size: 8 }, () => [
              h(NStatistic, { label: props.countLabel, value: item.total || 0 }),
              h(NStatistic, { label: props.amountLabel, value: fmtAmount(item.totalAmount) }),
              item.items && item.items.length
                ? h(NList, { bordered: true }, () =>
                    item.items!.map((sub: Record<string, any>, i: number) =>
                      h(NListItem, { key: i }, () => sub.label ?? sub.name ?? sub.code ?? JSON.stringify(sub))
                    )
                  )
                : null,
            ])
          : h(NEmpty, { description: props.pendingText });
      };
    },
  });

  function onYearChange(val: number | null) {
    year.value = val ? new Date(val).getFullYear() : new Date().getFullYear();
  }

  async function fetchData() {
    loading.value = true;
    const params = mine.value ? { mine: true } : {};
    try {
      const [orderRes, payRes, trendRes, colRes, mediaRes, missRes, sealRes] = await Promise.allSettled([
        getAdReportOrderSummary(params),
        getAdReportPaymentSummary(params),
        getAdReportMonthlyTrend({ year: year.value }),
        getAdReportByCode('collection', params),
        getAdReportByCode('media', params),
        getAdReportByCode('contract-missing', params),
        getAdReportByCode('seal', params),
      ]);
      orderSummary.value = orderRes.status === 'fulfilled' ? (orderRes.value as AdReportOrderSummary) : null;
      paymentSummary.value = payRes.status === 'fulfilled' ? (payRes.value as AdReportPaymentSummary) : null;
      monthlyTrend.value = trendRes.status === 'fulfilled' ? (trendRes.value as AdReportMonthlyTrendResult) : null;
      collection.value = colRes.status === 'fulfilled' ? (colRes.value as AdReportItem) : null;
      media.value = mediaRes.status === 'fulfilled' ? (mediaRes.value as AdReportItem) : null;
      contractMissing.value = missRes.status === 'fulfilled' ? (missRes.value as AdReportItem) : null;
      seal.value = sealRes.status === 'fulfilled' ? (sealRes.value as AdReportItem) : null;
    } finally {
      loading.value = false;
    }
  }

  onMounted(fetchData);
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .filter-card {
    margin-bottom: 16px;
  }
  .report-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
  .amount {
    font-weight: 600;
    color: #d03050;
  }
</style>
