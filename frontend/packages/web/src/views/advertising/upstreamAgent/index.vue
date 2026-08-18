<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          placeholder="搜索代理名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.cooperationStatus"
          :options="cooperationOptions"
          placeholder="合作状态"
          clearable
          style="width: 130px"
        />
        <n-select
          v-model:value="searchForm.status"
          :options="statusOptions"
          placeholder="状态"
          clearable
          style="width: 120px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button v-permission="['AD_UPSTREAM_AGENT:CREATE']" type="primary" @click="openCreate">新建代理</n-button>
      </n-space>
    </n-card>

    <n-card :bordered="false" class="mt-4">
      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :pagination="pagination"
        :row-key="(r: any) => r.id"
        remote
      />
    </n-card>

    <n-modal
      v-model:show="modal.show"
      :title="modal.editId ? '编辑上游代理' : '新建上游代理'"
      preset="card"
      style="width: 560px"
    >
      <n-form ref="formRef" :model="form" label-placement="left" label-width="100">
        <n-form-item label="代理名称" required>
          <n-input v-model:value="form.name" placeholder="请输入代理名称" />
        </n-form-item>
        <n-form-item label="社会信用代码">
          <n-input v-model:value="form.creditCode" placeholder="社会信用代码" />
        </n-form-item>
        <n-form-item label="签约主体">
          <n-input v-model:value="form.signingEntity" placeholder="签约主体" />
        </n-form-item>
        <n-form-item label="联系人">
          <n-input v-model:value="form.contactPerson" placeholder="联系人" />
        </n-form-item>
        <n-form-item label="电话">
          <n-input v-model:value="form.contactPhone" placeholder="电话" />
        </n-form-item>
        <n-form-item v-if="false" label="业务主体">
          <n-select
            v-model:value="form.businessEntityId"
            :options="businessEntityOptions"
            filterable
            placeholder="请选择"
          />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="form.remark" type="textarea" placeholder="备注" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="closeModal">取消</n-button>
          <n-button
            v-permission="[modal.editId ? 'AD_UPSTREAM_AGENT:UPDATE' : 'AD_UPSTREAM_AGENT:CREATE']"
            type="primary"
            :loading="saving"
            @click="handleSave"
            >保存</n-button
          >
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, resolveDirective, withDirectives } from 'vue';
import { useRouter } from 'vue-router';
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
  NTag,
  useMessage,
} from 'naive-ui';

import { useI18n } from '@lib/shared/hooks/useI18n';

import {
  createAdUpstreamAgent,
  deleteAdUpstreamAgent,
  getAdBusinessEntityPage,
  getAdUpstreamAgentPage,
  updateAdUpstreamAgent,
} from '@/api/modules';

import { AdvertisingRouteEnum } from '@/enums/routeEnum';

import type { DataTableColumn } from 'naive-ui';

const { t } = useI18n();
const router = useRouter();
const message = useMessage();

const permissionDirective = resolveDirective('permission');

const loading = ref(false);
const saving = ref(false);
const list = ref<any[]>([]);
const searchForm = reactive({
  keyword: '',
  cooperationStatus: null as number | null,
  status: null as number | null,
});

const cooperationOptions = [
  { label: '正常', value: 10 },
  { label: '停用', value: 20 },
];
const statusOptions = cooperationOptions;
const businessEntityOptions = ref<{ label: string; value: string }[]>([]);

const modal = reactive({ show: false, editId: '' });
const form = reactive({
  name: '',
  creditCode: '',
  signingEntity: '',
  contactPerson: '',
  contactPhone: '',
  businessEntityId: null as string | null,
  remark: '',
});

/* eslint-disable no-use-before-define */
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage: (p: number) => {
    pagination.page = p;
    fetchData();
  },
  onUpdatePageSize: (s: number) => {
    pagination.pageSize = s;
    pagination.page = 1;
    fetchData();
  },
});
/* eslint-enable no-use-before-define */

async function fetchData() {
  loading.value = true;
  try {
    const res = await getAdUpstreamAgentPage({
      current: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      cooperationStatus: searchForm.cooperationStatus,
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

async function loadBusinessEntities() {
  try {
    const res = await getAdBusinessEntityPage({ current: 1, pageSize: 200 });
    businessEntityOptions.value = (res.list || []).map((it: any) => ({
      label: it.name || it.id,
      value: it.id,
    }));
  } catch (e) {
    /* ignore */
  }
}

function resetForm() {
  form.name = '';
  form.creditCode = '';
  form.signingEntity = '';
  form.contactPerson = '';
  form.contactPhone = '';
  form.businessEntityId = null;
  form.remark = '';
}

function openCreate() {
  modal.editId = '';
  resetForm();
  modal.show = true;
}

function openDetail(row: any) {
  router.push({ name: AdvertisingRouteEnum.ADVERTISING_UPSTREAM_AGENT_DETAIL, params: { id: row.id } });
}

function openEdit(row: any) {
  modal.editId = row.id;
  form.name = row.name;
  form.creditCode = row.creditCode || '';
  form.signingEntity = row.signingEntity || '';
  form.contactPerson = row.contactPerson || '';
  form.contactPhone = row.contactPhone || '';
  form.businessEntityId = row.businessEntityId || null;
  form.remark = row.remark || '';
  modal.show = true;
}

function closeModal() {
  modal.show = false;
}

async function handleDisable(row: any) {
  try {
    await updateAdUpstreamAgent({ id: row.id, status: 20 });
    message.success('已停用');
    fetchData();
  } catch (e) {
    message.error((e as Error).message || '操作失败');
  }
}

async function handleEnable(row: any) {
  try {
    await updateAdUpstreamAgent({ id: row.id, status: 10 });
    message.success('已启用');
    fetchData();
  } catch (e) {
    message.error((e as Error).message || '操作失败');
  }
}

const statusTag = (status?: number) => (status === 20 ? ('error' as const) : ('success' as const));

const columns: DataTableColumn<any>[] = [
  { key: 'name', title: '代理名称', minWidth: 140, ellipsis: { tooltip: true } },
  { key: 'signingEntity', title: '签约主体', width: 140, ellipsis: { tooltip: true } },
  { key: 'contactPerson', title: '联系人', width: 100 },
  { key: 'contactPhone', title: '电话', width: 120 },
  {
    key: 'cooperationStatus',
    title: '合作状态',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: row.cooperationStatus === 20 ? 'error' : 'success' },
        { default: () => (row.cooperationStatus === 20 ? '停用' : '正常') }
      ),
  },
  {
    key: 'status',
    title: '状态',
    width: 80,
    render: (row) =>
      h(NTag, { type: statusTag(row.status) }, { default: () => (row.status === 20 ? '停用' : '正常') }),
  },
  {
    key: 'action',
    title: '操作',
    width: 180,
    fixed: 'right' as const,
    render: (row) =>
      h(NSpace, { wrap: false }, {
        default: () => [
          h(
            NButton,
            { size: 'small', onClick: () => openDetail(row) },
            { default: () => '详情' }
          ),
          withDirectives(
            h(
              NButton,
              { size: 'small', type: 'primary', onClick: () => openEdit(row) },
              { default: () => '编辑' }
            ),
            [[permissionDirective, ['AD_UPSTREAM_AGENT:UPDATE']]]
          ),
          row.status === 20
            ? withDirectives(
                h(
                  NButton,
                  { size: 'small', type: 'success', onClick: () => handleEnable(row) },
                  { default: () => '启用' }
                ),
                [[permissionDirective, ['AD_UPSTREAM_AGENT:UPDATE']]]
              )
            : withDirectives(
                h(
                  NButton,
                  { size: 'small', type: 'error', onClick: () => handleDisable(row) },
                  { default: () => '停用' }
                ),
                [[permissionDirective, ['AD_UPSTREAM_AGENT:UPDATE']]]
              ),
        ],
      }),
  },
];

async function handleSave() {
  if (!form.name) {
    message.warning('代理名称不能为空');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      name: form.name,
      creditCode: form.creditCode,
      signingEntity: form.signingEntity,
      contactPerson: form.contactPerson,
      contactPhone: form.contactPhone,
      businessEntityId: form.businessEntityId || undefined,
      remark: form.remark,
    };
    if (modal.editId) {
      await updateAdUpstreamAgent({ id: modal.editId, ...payload });
    } else {
      await createAdUpstreamAgent(payload);
    }
    message.success('保存成功');
    modal.show = false;
    fetchData();
  } catch (e) {
    message.error((e as Error).message || '保存失败');
  } finally {
    saving.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function handleReset() {
  searchForm.keyword = '';
  searchForm.cooperationStatus = null;
  searchForm.status = null;
  handleSearch();
}

onMounted(async () => {
  await loadBusinessEntities();
  fetchData();
});
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
