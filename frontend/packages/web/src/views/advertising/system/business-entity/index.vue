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
        <n-button type="primary" @click="openCreate">{{ t('advertising.system.businessEntity.new') }}</n-button>
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
        <n-form-item :label="t('advertising.system.businessEntity.form.name')" path="name">
          <n-input v-model:value="form.name" placeholder="请输入主体名称" />
        </n-form-item>
        <n-form-item :label="t('advertising.system.businessEntity.form.code')" path="code">
          <n-input v-model:value="form.code" placeholder="如 JS / TH" />
        </n-form-item>
        <n-form-item :label="t('advertising.system.businessEntity.form.status')" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="请选择" />
        </n-form-item>
        <n-form-item :label="t('advertising.system.businessEntity.form.isCrossEntity')">
          <n-switch v-model:value="isCrossEntity" />
        </n-form-item>
        <n-form-item :label="t('advertising.system.businessEntity.form.remark')">
          <n-input v-model:value="form.remark" type="textarea" placeholder="备注" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">{{ t('advertising.businessEntity.form.cancel') }}</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">{{
            t('advertising.businessEntity.form.save')
          }}</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, reactive, ref } from 'vue';
  import {
    NButton,
    NCard,
    NDataTable,
    NForm,
    NFormItem,
    NInput,
    NModal,
    NSelect,
    NSpace,
    NSwitch,
    NTag,
    useMessage,
  } from 'naive-ui';

  import { AdBusinessEntityStatusOptions, getAdBusinessEntityStatusLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdBusinessEntityListItem, AdBusinessEntitySaveParams } from '@lib/shared/models/advertising';

  import { createAdBusinessEntity, getAdBusinessEntityPage, updateAdBusinessEntity } from '@/api/modules';

  import { fmtDateTime } from '../../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const message = useMessage();

  const statusOptions = AdBusinessEntityStatusOptions;

  const loading = ref(false);
  const list = ref<AdBusinessEntityListItem[]>([]);
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
      const res = await getAdBusinessEntityPage({
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

  // ---- modal ----
  const showModal = ref(false);
  const saving = ref(false);
  const editId = ref('');
  const modalTitle = computed(() =>
    editId.value ? t('advertising.businessEntity.form.title.edit') : t('advertising.businessEntity.form.title.create')
  );

  interface AdBeForm {
    name?: string;
    code?: string;
    status?: number | null;
    isCrossEntity?: number;
    remark?: string;
  }
  const form = reactive<AdBeForm>({
    name: undefined,
    code: undefined,
    status: 10,
    isCrossEntity: 0,
    remark: undefined,
  });
  const isCrossEntity = computed({
    get: () => form.isCrossEntity === 1,
    set: (v: boolean) => {
      form.isCrossEntity = v ? 1 : 0;
    },
  });

  function resetForm() {
    form.name = undefined;
    form.code = undefined;
    form.status = 10;
    form.isCrossEntity = 0;
    form.remark = undefined;
    editId.value = '';
  }
  function openCreate() {
    resetForm();
    showModal.value = true;
  }
  function openEdit(row: AdBusinessEntityListItem) {
    editId.value = row.id;
    form.name = row.name;
    form.code = row.code;
    form.status = row.status ?? 10;
    form.isCrossEntity = row.isCrossEntity ?? 0;
    form.remark = row.remark;
    showModal.value = true;
  }

  async function handleSave() {
    if (!form.name) {
      message.warning(
        `${t('advertising.system.businessEntity.form.name')} ${t('advertising.businessEntity.form.required')}`
      );
      return;
    }
    if (!form.code) {
      message.warning(
        `${t('advertising.system.businessEntity.form.code')} ${t('advertising.businessEntity.form.required')}`
      );
      return;
    }
    try {
      saving.value = true;
      const payload: AdBusinessEntitySaveParams = {
        name: form.name,
        code: form.code,
        status: form.status ?? 10,
        isCrossEntity: form.isCrossEntity ?? 0,
        remark: form.remark,
      };
      if (editId.value) {
        payload.id = editId.value;
        await updateAdBusinessEntity(payload);
      } else {
        await createAdBusinessEntity(payload);
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

  const columns: DataTableColumn<AdBusinessEntityListItem>[] = [
    {
      key: 'name',
      title: t('advertising.system.businessEntity.column.name'),
      minWidth: 140,
      ellipsis: { tooltip: true },
    },
    { key: 'code', title: t('advertising.system.businessEntity.column.code'), width: 120 },
    {
      key: 'status',
      title: t('advertising.system.businessEntity.column.status'),
      width: 100,
      render: (row) =>
        h(
          NTag,
          { type: row.status === 10 ? 'success' : 'default' },
          { default: () => getAdBusinessEntityStatusLabel(row.status) }
        ),
    },
    {
      key: 'isCrossEntity',
      title: t('advertising.system.businessEntity.column.isCrossEntity'),
      width: 110,
      render: (row) =>
        row.isCrossEntity === 1
          ? h(NTag, { type: 'warning' }, { default: () => t('advertising.common.yes') })
          : h('span', t('advertising.common.no')),
    },
    {
      key: 'createTime',
      title: t('advertising.businessEntity.column.createTime'),
      width: 160,
      render: (row) => h('span', fmtDateTime(row.createTime)),
    },
    {
      key: 'action',
      title: t('advertising.businessEntity.edit'),
      width: 100,
      fixed: 'right' as const,
      render: (row) =>
        h(
          NButton,
          { size: 'small', type: 'primary', onClick: () => openEdit(row) },
          { default: () => t('advertising.businessEntity.edit') }
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
