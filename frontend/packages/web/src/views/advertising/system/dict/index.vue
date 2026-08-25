<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" justify="space-between">
        <n-space align="end" wrap>
          <n-input
            v-model:value="searchForm.keyword"
            :placeholder="t('advertising.businessEntity.search')"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
          <n-select
            v-model:value="searchForm.status"
            :placeholder="t('advertising.businessEntity.filter.status')"
            :options="statusOptions"
            clearable
            style="width: 150px"
          />
          <n-button type="primary" @click="handleSearch">查询</n-button>
          <n-button @click="handleReset">{{ t('advertising.businessEntity.form.cancel') }}</n-button>
        </n-space>
        <n-button v-permission="['AD_DICT:CREATE']" type="primary" @click="openCreate">{{
          t('advertising.system.dict.new')
        }}</n-button>
      </n-space>
    </n-card>

    <n-card :bordered="false" class="mt-4">
      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: any) => row.id"
        remote
      />
    </n-card>

    <n-modal v-model:show="showModal" :title="modalTitle" preset="card" style="width: 520px" :bordered="false">
      <n-form :model="form" label-placement="left" :label-width="100">
        <n-form-item :label="t('advertising.system.dict.form.dictCode')" path="dictCode">
          <n-input v-model:value="form.dictCode" placeholder="如 industry" />
        </n-form-item>
        <n-form-item :label="t('advertising.system.dict.form.dictLabel')" path="dictLabel">
          <n-input v-model:value="form.dictLabel" placeholder="显示名称" />
        </n-form-item>
        <n-form-item :label="t('advertising.system.dict.form.dictValue')" path="dictValue">
          <n-input v-model:value="form.dictValue" placeholder="字典值" />
        </n-form-item>
        <n-form-item :label="t('advertising.system.dict.form.sort')" path="sort">
          <n-input-number v-model:value="form.sort" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item :label="t('advertising.system.dict.form.status')" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="请选择" />
        </n-form-item>
        <n-form-item :label="t('advertising.system.dict.form.remark')">
          <n-input v-model:value="form.remark" type="textarea" placeholder="备注" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">{{ t('advertising.businessEntity.form.cancel') }}</n-button>
          <n-button
            v-permission="[editId ? 'AD_DICT:UPDATE' : 'AD_DICT:CREATE']"
            type="primary"
            :loading="saving"
            @click="handleSave"
            >{{ t('advertising.businessEntity.form.save') }}</n-button
          >
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, reactive, ref, resolveDirective, withDirectives } from 'vue';
  import {
    NButton,
    NCard,
    NDataTable,
    NForm,
    NFormItem,
    NInput,
    NInputNumber,
    NModal,
    NSelect,
    NSpace,
    NTag,
    useMessage,
  } from 'naive-ui';

  import { AdDictStatusOptions, getAdDictStatusLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdDictInfo, AdDictSaveParams } from '@lib/shared/models/advertising';

  import { createAdDict, getAdDictPage, updateAdDict } from '@/api/modules';

  import { fmtDateTime } from '../../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const message = useMessage();
  const permissionDirective = resolveDirective('permission');

  const statusOptions = AdDictStatusOptions;

  const loading = ref(false);
  const list = ref<AdDictInfo[]>([]);
  const searchForm = reactive({ keyword: '', status: null as number | null });

  /* eslint-disable no-use-before-define */
  const pagination = reactive({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    showSizePicker: true,
    pageSizes: [10, 20, 50],
    onUpdatePage: (page: number) => {
      pagination.page = page;
      fetchData();
    },
    onUpdatePageSize: (size: number) => {
      pagination.pageSize = size;
      pagination.page = 1;
      fetchData();
    },
  });
  /* eslint-enable no-use-before-define */

  async function fetchData() {
    loading.value = true;
    try {
      const res = await getAdDictPage({
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        status: searchForm.status,
      });
      list.value = res.list || [];
      pagination.itemCount = res.total || 0;
    } catch (e) {
      message.error((e as Error).message || '查询失败');
    } finally {
      loading.value = false;
    }
  }

  const showModal = ref(false);
  const saving = ref(false);
  const editId = ref('');
  const modalTitle = computed(() =>
    editId.value ? t('advertising.businessEntity.form.title.edit') : t('advertising.system.dict.new')
  );

  interface AdDictForm {
    dictCode?: string;
    dictLabel?: string;
    dictValue?: string;
    sort?: number | null;
    status?: number | null;
    remark?: string;
  }
  const form = reactive<AdDictForm>({
    dictCode: undefined,
    dictLabel: undefined,
    dictValue: undefined,
    sort: 0,
    status: 10,
    remark: undefined,
  });

  function resetForm() {
    form.dictCode = undefined;
    form.dictLabel = undefined;
    form.dictValue = undefined;
    form.sort = 0;
    form.status = 10;
    form.remark = undefined;
    editId.value = '';
  }
  function openCreate() {
    resetForm();
    showModal.value = true;
  }
  function openEdit(row: AdDictInfo) {
    editId.value = row.id;
    form.dictCode = row.dictCode;
    form.dictLabel = row.dictLabel;
    form.dictValue = row.dictValue;
    form.sort = row.sort ?? 0;
    form.status = row.status ?? 10;
    form.remark = row.remark;
    showModal.value = true;
  }

  async function handleSave() {
    if (!form.dictCode) {
      message.warning(`${t('advertising.system.dict.form.dictCode')} ${t('advertising.system.dict.form.required')}`);
      return;
    }
    if (!form.dictLabel) {
      message.warning(`${t('advertising.system.dict.form.dictLabel')} ${t('advertising.system.dict.form.required')}`);
      return;
    }
    try {
      saving.value = true;
      const payload: AdDictSaveParams = {
        dictCode: form.dictCode,
        dictLabel: form.dictLabel,
        dictValue: form.dictValue,
        sort: form.sort ?? 0,
        status: form.status ?? 10,
        remark: form.remark,
      };
      if (editId.value) {
        payload.id = editId.value;
        await updateAdDict(payload);
      } else {
        await createAdDict(payload);
      }
      message.success(t('advertising.common.saveSuccess'));
      showModal.value = false;
      fetchData();
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    } finally {
      saving.value = false;
    }
  }

  const columns: DataTableColumn<AdDictInfo>[] = [
    { key: 'dictCode', title: t('advertising.system.dict.column.dictCode'), width: 140, ellipsis: { tooltip: true } },
    {
      key: 'dictLabel',
      title: t('advertising.system.dict.column.dictLabel'),
      minWidth: 140,
      ellipsis: { tooltip: true },
    },
    { key: 'dictValue', title: t('advertising.system.dict.column.dictValue'), width: 120, ellipsis: { tooltip: true } },
    {
      key: 'sort',
      title: t('advertising.system.dict.column.sort'),
      width: 80,
      render: (row) => h('span', row.sort ?? '-'),
    },
    {
      key: 'status',
      title: t('advertising.system.dict.column.status'),
      width: 100,
      render: (row) =>
        h(
          NTag,
          { type: row.status === 10 ? 'success' : 'default' },
          { default: () => getAdDictStatusLabel(row.status) }
        ),
    },
    { key: 'remark', title: t('advertising.system.dict.column.remark'), minWidth: 160, ellipsis: { tooltip: true } },
    {
      key: 'action',
      title: t('advertising.businessEntity.edit'),
      width: 100,
      fixed: 'right' as const,
      render: (row) =>
        withDirectives(
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => openEdit(row) },
            { default: () => t('advertising.businessEntity.edit') }
          ),
          [[permissionDirective, ['AD_DICT:UPDATE']]]
        ),
    },
  ];

  function handleSearch() {
    pagination.page = 1;
    fetchData();
  }
  function handleReset() {
    searchForm.keyword = '';
    searchForm.status = null;
    handleSearch();
  }

  fetchData();
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .search-card {
    margin-bottom: 0;
  }
  .mt-4 {
    margin-top: 16px;
  }
</style>
