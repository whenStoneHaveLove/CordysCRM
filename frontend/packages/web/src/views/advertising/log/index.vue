<template>
  <n-scrollbar x-scrollable :content-style="{ 'min-width': '1000px', 'width': '100%', 'height': '100%' }">
    <CrmCard no-content-padding hide-footer auto-height class="mb-[16px]">
      <CrmTab v-model:active-tab="activeTab" no-content :tab-list="tabList" type="line" />
    </CrmCard>

    <CrmCard hide-footer auto-height class="form-card mb-[16px] min-w-[1000px]">
      <n-form
        ref="formRef"
        label-placement="left"
        label-width="auto"
        :model="form"
        class="grid grid-cols-3 gap-x-[24px]"
      >
        <n-form-item :label="t('common.operator')" path="operator">
          <CrmUserSelect
            v-model:value="form.operator"
            value-field="id"
            label-field="name"
            mode="remote"
            :fetch-api="getUserOptions"
            filterable
            clearable
          />
        </n-form-item>
        <n-form-item :label="t('advertising.log.operationTime')" path="time">
          <n-date-picker
            v-model:value="form.time"
            type="datetimerange"
            :is-date-disabled="dataDisabled"
            class="w-full"
          />
        </n-form-item>
        <template v-if="activeTab === 'operation'">
          <n-form-item :label="t('advertising.log.operationType')" path="action">
            <n-select
              v-model:value="form.action"
              :options="actionOptions"
              :placeholder="t('common.pleaseSelect')"
              clearable
            />
          </n-form-item>
          <n-form-item :label="t('advertising.log.operationScope')" path="module">
            <n-select
              v-model:value="form.module"
              :options="moduleOptions"
              :placeholder="t('common.pleaseSelect')"
              clearable
            />
          </n-form-item>
          <n-form-item :label="t('advertising.log.operationTarget')" path="keyword">
            <n-input v-model:value="form.keyword" :placeholder="t('common.pleaseInput')" clearable />
          </n-form-item>
        </template>
        <n-form-item>
          <n-button ghost class="mr-[12px]" type="primary" @click="searchData">
            {{ t('advanceFilter.filter') }}
          </n-button>
          <n-button type="default" class="outline--secondary" @click="handleReset">
            {{ t('common.reset') }}
          </n-button>
        </n-form-item>
      </n-form>
    </CrmCard>

    <CrmCard
      v-if="activeTab === 'operation'"
      no-content-bottom-padding
      hide-footer
      :special-height="licenseStore.expiredDuring ? 272 : 0"
    >
      <CrmTable
        ref="crmTableRef"
        v-bind="propsRes"
        class="crm-ad-operation-log-table"
        @page-change="propsEvent.pageChange"
        @page-size-change="propsEvent.pageSizeChange"
        @sorter-change="propsEvent.sorterChange"
        @filter-change="propsEvent.filterChange"
      />
    </CrmCard>
    <LoginLog v-if="activeTab === 'login'" ref="loginLogRef" />

    <!-- 日志详情抽屉 -->
    <CrmDrawer
      v-model:show="showDetail"
      :footer="false"
      :show-mask="false"
      :title="t('advertising.log.detail')"
      :width="720"
    >
      <div v-if="activeDetail" class="log-detail">
        <div class="log-detail-header">
          <div class="log-detail-time">{{ dayjs(activeDetail.createTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
          <div class="log-detail-meta">
            <span class="log-detail-tag">{{ actionLabel(activeDetail.action) }}</span>
            <span class="log-detail-tag">{{ moduleLabel(activeDetail.module) }}</span>
            <span class="text-n4 text-[12px]">{{ activeDetail.operatorName }} | IP: {{ activeDetail.ip || '-' }}</span>
          </div>
        </div>

        <div v-if="activeDetail.beforeValue || activeDetail.afterValue" class="log-detail-diff">
          <div v-if="activeDetail.beforeValue" class="log-detail-block">
            <div class="log-detail-block-title before-title">{{ t('advertising.log.beforeChange') }}</div>
            <pre class="log-detail-json">{{ formatJson(activeDetail.beforeValue) }}</pre>
          </div>
          <div v-if="activeDetail.afterValue" class="log-detail-block">
            <div class="log-detail-block-title after-title">{{ t('advertising.log.afterChange') }}</div>
            <pre class="log-detail-json">{{ formatJson(activeDetail.afterValue) }}</pre>
          </div>
        </div>
        <div v-else class="log-detail-empty">{{ t('advertising.log.noChangeDetail') }}</div>
      </div>
    </CrmDrawer>
  </n-scrollbar>
</template>

<script setup lang="ts">
  import { NButton, NDatePicker, NForm, NFormItem, NInput, NScrollbar, NSelect } from 'naive-ui';
  import dayjs from 'dayjs';

  import { TableKeyEnum } from '@lib/shared/enums/tableEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmCard from '@/components/pure/crm-card/index.vue';
  import CrmDrawer from '@/components/pure/crm-drawer/index.vue';
  import CrmTab from '@/components/pure/crm-tab/index.vue';
  import CrmTable from '@/components/pure/crm-table/index.vue';
  import { CrmDataTableColumn } from '@/components/pure/crm-table/type';
  import useTable from '@/components/pure/crm-table/useTable';
  import CrmTableButton from '@/components/pure/crm-table-button/index.vue';
  import CrmUserSelect from '@/components/business/crm-user-select/index.vue';
  import LoginLog from './components/loginLog.vue';

  import { getUserOptions } from '@/api/modules';
  import type { AdOperationLogItem, AdOperationLogParams } from '@/api/modules/ad/log';
  import { adOperationLogList } from '@/api/modules/ad/log';
  import { adLogActionOption, adLogModuleOption } from '@/config/adLog';
  import useLicenseStore from '@/store/modules/setting/license';

  const { t } = useI18n();
  const licenseStore = useLicenseStore();

  const activeTab = ref('operation');
  const tabList = [
    { name: 'operation', tab: t('advertising.log.operationLog') },
    { name: 'login', tab: t('advertising.log.loginLog') },
  ];

  const moduleOptions = computed(() => adLogModuleOption.map((e) => ({ ...e, label: t(e.label) })));
  const actionOptions = computed(() => adLogActionOption.map((e) => ({ ...e, label: t(e.label) })));
  const moduleLabelMap = computed(() => new Map(adLogModuleOption.map((e) => [e.value, e.label])));
  const actionLabelMap = computed(() => new Map(adLogActionOption.map((e) => [e.value, e.label])));

  function moduleLabel(module: string) {
    const label = moduleLabelMap.value.get(module);
    return label ? t(label) : module || '-';
  }
  function actionLabel(action: string) {
    const label = actionLabelMap.value.get(action);
    return label ? t(label) : action || '-';
  }

  // 时间约束
  function dataDisabled(ts: number, type: 'start' | 'end', range: [number, number] | null) {
    const currentDate = new Date();
    const selectedDate = new Date(ts);
    const month = 30 * 24 * 60 * 60 * 1000;
    if (selectedDate > currentDate) {
      return true;
    }
    if (range) {
      const [startTime, endTime] = range;
      const start = new Date(startTime);
      const end = new Date(endTime);
      const diffMonths =
        start.valueOf() > end.valueOf()
          ? Math.abs((selectedDate.getTime() - end.getTime()) / month)
          : Math.abs((selectedDate.getTime() - start.getTime()) / month);
      if (type === 'start' && diffMonths > 6) {
        return true;
      }
      if (type === 'end' && diffMonths > 6) {
        return true;
      }
    }
    return false;
  }

  const defaultForm = {
    action: null as string | null,
    module: null as string | null,
    operator: null as string | null,
    keyword: null as string | null,
    time: [dayjs().subtract(1, 'M').valueOf(), dayjs().valueOf()] as [number, number],
  };
  const form = ref<typeof defaultForm>({ ...defaultForm });

  // 详情抽屉
  const showDetail = ref(false);
  const activeDetail = ref<AdOperationLogItem | null>(null);

  function openDetail(row: AdOperationLogItem) {
    activeDetail.value = row;
    showDetail.value = true;
  }

  function formatJson(jsonStr: string): string {
    try {
      return JSON.stringify(JSON.parse(jsonStr), null, 2);
    } catch {
      return jsonStr;
    }
  }

  const columns: CrmDataTableColumn[] = [
    {
      title: t('common.operator'),
      key: 'operatorName',
      ellipsis: { tooltip: true },
      render: (row: AdOperationLogItem) => row.operatorName || '-',
    },
    {
      title: t('advertising.log.operationScope'),
      key: 'module',
      width: 200,
      render: (row: AdOperationLogItem) => moduleLabel(row.module),
    },
    {
      title: t('advertising.log.operationType'),
      key: 'action',
      width: 150,
      render: (row: AdOperationLogItem) => actionLabel(row.action),
    },
    {
      title: t('advertising.log.operationTarget'),
      key: 'targetId',
      ellipsis: { tooltip: true },
      render: (row: AdOperationLogItem) =>
        h(
          CrmTableButton,
          { onClick: () => openDetail(row) },
          { default: () => row.targetId || '-', trigger: () => row.targetId || '-' }
        ),
    },
    {
      title: 'IP',
      key: 'ip',
      width: 140,
      render: (row: AdOperationLogItem) => row.ip || '-',
    },
    {
      title: t('advertising.log.operationTime'),
      key: 'createTime',
      sortOrder: false,
      sorter: true,
      resizable: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable<AdOperationLogItem>(
    (params: AdOperationLogParams) => adOperationLogList(params),
    {
      showSetting: false,
      columns,
      tableKey: TableKeyEnum.LOG,
      hiddenRefresh: true,
      containerClass: '.crm-ad-operation-log-table',
    }
  );

  const crmTableRef = ref<InstanceType<typeof CrmTable>>();
  const loginLogRef = ref<InstanceType<typeof LoginLog>>();

  async function searchData() {
    const { time, ...otherForm } = form.value;
    if (activeTab.value === 'operation') {
      setLoadListParams({ ...otherForm, startTime: time[0], endTime: time[1] });
      await loadList();
      crmTableRef.value?.scrollTo({ top: 0 });
    } else {
      nextTick(() => {
        loginLogRef.value?.searchData({ operator: otherForm.operator, startTime: time[0], endTime: time[1] });
      });
    }
  }

  function handleReset() {
    form.value = { ...defaultForm };
    searchData();
  }

  watch(
    () => activeTab.value,
    () => {
      searchData();
    },
    { immediate: true }
  );

  onMounted(() => {
    searchData();
  });
</script>

<style lang="less" scoped>
  .form-card {
    :deep(.n-card__content) {
      padding: 24px 24px 8px;
    }
  }
  :deep(.n-form-item-feedback-wrapper) {
    min-height: 16px;
  }

  .log-detail {
    .log-detail-header {
      margin-bottom: 20px;
      .log-detail-time {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-n2);
        margin-bottom: 8px;
      }
      .log-detail-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }
      .log-detail-tag {
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
        background: var(--text-n9);
        color: var(--text-n3);
      }
    }

    .log-detail-diff {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .log-detail-block {
      border: 1px solid var(--text-n8);
      border-radius: 8px;
      overflow: hidden;
    }

    .log-detail-block-title {
      padding: 8px 12px;
      font-size: 13px;
      font-weight: 500;
      &.before-title {
        background: var(--error-5);
        color: var(--error-1);
      }
      &.after-title {
        background: var(--success-5);
        color: var(--success-1);
      }
    }

    .log-detail-json {
      padding: 12px;
      margin: 0;
      font-size: 12px;
      line-height: 1.6;
      color: var(--text-n2);
      background: var(--text-n10);
      white-space: pre-wrap;
      word-break: break-all;
      max-height: 400px;
      overflow-y: auto;
    }

    .log-detail-empty {
      padding: 24px;
      text-align: center;
      color: var(--text-n4);
      font-size: 14px;
    }
  }
</style>
