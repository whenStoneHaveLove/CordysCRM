<template>
  <div class="advertising-page">
    <n-card :bordered="false" class="search-card">
      <n-space align="end" wrap>
        <n-input
          v-model:value="searchForm.keyword"
          :placeholder="t('advertising.customer.search')"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <n-input
          v-model:value="searchForm.industryCode"
          :placeholder="t('advertising.customer.filter.industry')"
          clearable
          style="width: 150px"
        />
        <n-select
          v-model:value="searchForm.customerLevel"
          :placeholder="t('advertising.customer.filter.level')"
          :options="levelOptions"
          clearable
          style="width: 150px"
        />
        <n-select
          v-model:value="searchForm.status"
          :placeholder="t('advertising.customer.filter.status')"
          :options="statusOptions"
          clearable
          style="width: 150px"
        />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">{{ t('advertising.order.reset') }}</n-button>
        <n-button type="primary" @click="openCreate">{{ t('advertising.customer.new') }}</n-button>
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
      style="width: 640px"
    >
      <n-form ref="formRef" :model="form" label-placement="left" :label-width="120">
        <n-grid :cols="2" :x-gap="16" item-responsive>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.name')" path="customerName">
            <n-input v-model:value="form.customerName" placeholder="请输入客户名称（全局唯一）" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.brand')">
            <n-input v-model:value="form.brand" placeholder="品牌" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.industryCode')">
            <n-select
              v-model:value="form.industryCode"
              :options="industryOptions"
              filterable
              placeholder="请选择行业类别"
            />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.signingEntity')">
            <n-input v-model:value="form.signingEntity" placeholder="签约主体" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.contactPerson')">
            <n-input v-model:value="form.contactPerson" placeholder="联系人" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.contactPhone')">
            <n-input v-model:value="form.contactPhone" placeholder="联系电话" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.email')">
            <n-input v-model:value="form.email" placeholder="邮箱" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.address')">
            <n-input v-model:value="form.address" placeholder="地址" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.customerLevel')" path="customerLevel">
            <n-select v-model:value="form.customerLevel" :options="levelOptions" placeholder="请选择" />
          </n-form-item-gi>
          <n-form-item-gi :span="1" :label="t('advertising.customer.form.status')" path="status">
            <n-select v-model:value="form.status" :options="statusOptions" placeholder="请选择" />
          </n-form-item-gi>
          <n-form-item-gi :span="2" :label="t('advertising.customer.form.remark')">
            <n-input v-model:value="form.remark" type="textarea" placeholder="备注" />
          </n-form-item-gi>
        </n-grid>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">{{ t('advertising.customer.form.cancel') }}</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">{{
            t('advertising.customer.form.save')
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
    NTag,
    useMessage,
  } from 'naive-ui';

  import {
    AdCustomerLevelOptions,
    AdCustomerStatusOptions,
    getAdCustomerLevelLabel,
    getAdCustomerStatusLabel,
  } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdCustomerListItem, AdCustomerPageParams } from '@lib/shared/models/advertising';

  import { createAdCustomer, getAdCustomerDetail, getAdCustomerPage, getAdDictPage, updateAdCustomer } from '@/api/modules';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const router = useRouter();
  const message = useMessage();

  const levelOptions = AdCustomerLevelOptions;
  const statusOptions = AdCustomerStatusOptions;

  type SelectItem = { label: string; value: string };

  const industryOptions = ref<SelectItem[]>([]);

  const loading = ref(false);
  const saving = ref(false);
  const list = ref<AdCustomerListItem[]>([]);
  const searchForm = reactive({
    keyword: '',
    industryCode: '',
    customerLevel: null as number | null,
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
      const params: AdCustomerPageParams = {
        current: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        industryCode: searchForm.industryCode || undefined,
        customerLevel: searchForm.customerLevel,
        status: searchForm.status,
      };
      const res = await getAdCustomerPage(params);
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
    editId.value ? t('advertising.customer.form.title.edit') : t('advertising.customer.form.title.create')
  );

  interface CustomerForm {
    customerName?: string;
    brand?: string;
    industryCode?: string;
    signingEntity?: string;
    contactPerson?: string;
    contactPhone?: string;
    email?: string;
    address?: string;
    customerLevel?: number | null;
    status?: number | null;
    remark?: string;
  }
  const form = reactive<CustomerForm>({
    customerName: undefined,
    brand: undefined,
    industryCode: undefined,
    signingEntity: undefined,
    contactPerson: undefined,
    contactPhone: undefined,
    email: undefined,
    address: undefined,
    customerLevel: 20,
    status: 0,
    remark: undefined,
  });

  function resetForm() {
    form.customerName = undefined;
    form.brand = undefined;
    form.industryCode = undefined;
    form.signingEntity = undefined;
    form.contactPerson = undefined;
    form.contactPhone = undefined;
    form.email = undefined;
    form.address = undefined;
    form.customerLevel = 20;
    form.status = 0;
    form.remark = undefined;
    editId.value = '';
  }

  async function loadIndustryOptions() {
    try {
      const res = await getAdDictPage({ current: 1, pageSize: 200, dictCode: 'industry' });
      industryOptions.value = (res.list || []).map((it: any) => ({
        label: it.dictLabel || it.dictValue || it.id,
        value: it.dictValue || it.id,
      }));
    } catch (e) {
      // ignore
    }
  }

  function openCreate() {
    resetForm();
    showModal.value = true;
  }

  async function openEdit(row: AdCustomerListItem) {
    editId.value = row.id;
    showModal.value = true;
    try {
      const res = await getAdCustomerDetail(row.id);
      form.customerName = res.customer?.name;
      form.brand = res.customer?.brand;
      form.industryCode = res.customer?.industryCode;
      form.signingEntity = res.customer?.signingEntity;
      form.contactPerson = res.customer?.contactPerson;
      form.contactPhone = res.customer?.contactPhone;
      form.email = res.customer?.email;
      form.address = res.customer?.address;
      form.customerLevel = res.customer?.customerLevel ?? 20;
      form.status = res.customer?.status ?? 0;
      form.remark = res.customer?.remark;
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    }
  }

  async function handleSave() {
    if (!form.customerName) {
      message.warning(`${t('advertising.customer.form.name')} ${t('advertising.customer.form.required')}`);
      return;
    }
    saving.value = true;
    try {
      const payload: any = {
        customerName: form.customerName,
        brand: form.brand,
        industryCode: form.industryCode,
        signingEntity: form.signingEntity,
        contactPerson: form.contactPerson,
        contactPhone: form.contactPhone,
        email: form.email,
        address: form.address,
        customerLevel: form.customerLevel ?? undefined,
        status: form.status ?? undefined,
        remark: form.remark,
      };
      if (editId.value) {
        payload.id = editId.value;
        await updateAdCustomer(payload);
      } else {
        await createAdCustomer(payload);
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
    switch (status) {
      case 0:
        return 'success';
      case 10:
        return 'warning';
      case 20:
        return 'error';
      default:
        return 'default';
    }
  }

  function openDetail(row: AdCustomerListItem) {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CUSTOMER_DETAIL, params: { id: row.id } });
  }

  const columns: DataTableColumn<AdCustomerListItem>[] = [
    { key: 'customerName', title: t('advertising.customer.column.name'), minWidth: 160, ellipsis: { tooltip: true } },
    {
      key: 'brand',
      title: t('advertising.customer.column.brand'),
      width: 140,
      ellipsis: { tooltip: true },
    },
    {
      key: 'industryLabel',
      title: t('advertising.customer.column.industry'),
      width: 140,
      ellipsis: { tooltip: true },
    },
    {
      key: 'customerLevel',
      title: t('advertising.customer.column.level'),
      width: 100,
      render: (row) => h('span', getAdCustomerLevelLabel(row.customerLevel)),
    },
    {
      key: 'status',
      title: t('advertising.customer.column.status'),
      width: 100,
      render: (row) =>
        h(NTag, { type: statusTagType(row.status) }, { default: () => getAdCustomerStatusLabel(row.status) }),
    },
    {
      key: 'signingEntity',
      title: t('advertising.customer.form.signingEntity'),
      width: 140,
      ellipsis: { tooltip: true },
      render: (row) => h('span', row.signingEntity || '-'),
    },
    {
      key: 'action',
      title: t('advertising.customer.action'),
      width: 150,
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
                { default: () => t('advertising.customer.detail') }
              ),
              h(
                NButton,
                { size: 'small', type: 'primary', onClick: () => openEdit(row) },
                { default: () => t('advertising.customer.edit') }
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
    searchForm.industryCode = '';
    searchForm.customerLevel = null;
    searchForm.status = null;
    handleSearch();
  }

  onMounted(async () => {
    await loadIndustryOptions();
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
