<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
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
        <n-button @click="handleReset">重置</n-button>
        <n-button type="primary" @click="openCreate">{{ t('advertising.businessEntity.new') }}</n-button>
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

    <n-modal
      v-model:show="showModal"
      :title="modalTitle"
      preset="card"
      style="width: 560px"
    >
      <n-form :model="form" label-placement="left" :label-width="120">
        <n-grid :cols="2" :x-gap="16" item-responsive>
          <n-form-item-gi :span="1" :label="t('advertising.businessEntity.form.name')" path="name">
            <n-input v-model:value="form.name" placeholder="请输入主体名称" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.businessEntity.form.code')" path="code">
            <n-input v-model:value="form.code" placeholder="如 JS / TH" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.businessEntity.form.status')" path="status">
            <n-select v-model:value="form.status" :options="statusOptions" placeholder="请选择" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.businessEntity.form.isCrossEntity')">
            <n-switch v-model:value="isCrossEntity" />
          </n-form-item-gi>
          <n-form-item-gi :span="2" :label="t('advertising.businessEntity.form.remark')">
            <n-input v-model:value="form.remark" type="textarea" placeholder="备注" />
          </n-form-item-gi>
        </n-grid>
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
  import { computed, h, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDataTable,
    NForm,
    NFormItemGi,
    NGrid,
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
  import type { AdBusinessEntityListItem, AdBusinessEntityPageParams } from '@lib/shared/models/advertising';

  import {
    createAdBusinessEntity,
    deleteAdBusinessEntity,
    getAdBusinessEntityDetail,
    getAdBusinessEntityPage,
    updateAdBusinessEntity,
  } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import { fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const statusOptions = AdBusinessEntityStatusOptions;

  const loading = ref(false);
  const saving = ref(false);
  const list = ref<AdBusinessEntityListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
    status: null as number | null,
  });

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
      const params: AdBusinessEntityPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        status: searchForm.status,
      };
      const res = await getAdBusinessEntityPage(params);
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
  const editId = ref('');
  const modalTitle = computed(() =>
    editId.value ? t('advertising.businessEntity.form.title.edit') : t('advertising.businessEntity.form.title.create')
  );

  interface BeForm {
    name?: string;
    code?: string;
    status?: number | null;
    isCrossEntity?: number;
    remark?: string;
  }
  const form = reactive<BeForm>({
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

  async function openEdit(row: AdBusinessEntityListItem) {
    editId.value = row.id;
    showModal.value = true;
    try {
      const res = await getAdBusinessEntityDetail(row.id);
      form.name = res.entity?.name;
      form.code = res.entity?.code;
      form.status = res.entity?.status ?? 10;
      form.isCrossEntity = res.entity?.isCrossEntity ?? 0;
      form.remark = res.entity?.remark;
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    }
  }

  async function handleSave() {
    if (!form.name) {
      message.warning(
        `${t('advertising.businessEntity.form.name')} ${t('advertising.businessEntity.form.required')}`
      );
      return;
    }
    if (!form.code) {
      message.warning(
        `${t('advertising.businessEntity.form.code')} ${t('advertising.businessEntity.form.required')}`
      );
      return;
    }
    saving.value = true;
    try {
      const payload: any = {
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
      message.error((e as Error).message || '保存失败');
    } finally {
      saving.value = false;
    }
  }

  function statusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    return status === 10 ? 'success' : 'default';
  }

  function openDetail(row: AdBusinessEntityListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_BUSINESS_ENTITY_DETAIL, params: { id: row.id } });
  }
  async function handleDisable(row: AdBusinessEntityListItem) {
    try {
      await deleteAdBusinessEntity(row.id);
      message.success(t('advertising.common.operateSuccess'));
      fetchData();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  const columns: DataTableColumn<AdBusinessEntityListItem>[] = [
    { key: 'name', title: t('advertising.businessEntity.column.name'), minWidth: 140, ellipsis: { tooltip: true } },
    { key: 'code', title: t('advertising.businessEntity.column.code'), width: 120, ellipsis: { tooltip: true } },
    {
      key: 'status',
      title: t('advertising.businessEntity.column.status'),
      width: 100,
      render: (row) =>
        h(NTag, { type: statusTagType(row.status) }, { default: () => getAdBusinessEntityStatusLabel(row.status) }),
    },
    {
      key: 'isCrossEntity',
      title: t('advertising.businessEntity.column.isCrossEntity'),
      width: 110,
      render: (row) =>
        row.isCrossEntity === 1
          ? h(NTag, { type: 'warning' }, { default: () => t('advertising.common.yes') })
          : h('span', t('advertising.common.no')),
    },
    { key: 'remark', title: t('advertising.businessEntity.column.remark'), minWidth: 160, ellipsis: { tooltip: true } },
    {
      key: 'userCount',
      title: t('advertising.businessEntity.column.userCount'),
      width: 100,
      render: (row) => h('span', row.userCount ?? '-'),
    },
    {
      key: 'orderCount',
      title: t('advertising.businessEntity.column.orderCount'),
      width: 100,
      render: (row) => h('span', row.orderCount ?? '-'),
    },
    {
      key: 'createTime',
      title: t('advertising.businessEntity.column.createTime'),
      width: 160,
      render: (row) => h('span', fmtDateTime(row.createTime)),
    },
    {
      key: 'action',
      title: t('advertising.businessEntity.action'),
      width: 200,
      fixed: 'right' as const,
      render: (row) =>
        h(
          NSpace,
          { wrap: false },
          {
            default: () => [
              h(
                NButton,
                { size: 'small', onClick: () => openDetail(row) },
                { default: () => t('advertising.businessEntity.detail') }
              ),
              h(
                NButton,
                { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                { default: () => t('advertising.businessEntity.edit') }
              ),
              h(
                NButton,
                { size: 'small', type: 'error', onClick: () => handleDisable(row) },
                { default: () => t('advertising.businessEntity.disable') }
              ),
            ],
          }
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

  onMounted(fetchData);
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
