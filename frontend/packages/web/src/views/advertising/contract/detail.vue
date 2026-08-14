<template>
  <div class="advertising-page">
    <n-spin :show="loading">
      <n-card v-if="detail" :bordered="false">
        <template #header>
          <n-space align="center">
            <span>{{ detail.contract.contractName || detail.contract.contractNo }}</span>
            <n-tag :type="sealStatusTagType(detail.contract.sealStatus)">
              {{ detail.sealStatusLabel || '-' }}
            </n-tag>
          </n-space>
        </template>
        <template #header-extra>
          <n-space>
            <!-- 已用印 / 归档审批驳回 → 上传双盖附件 + 提交归档审批 -->
            <template v-if="detail.contract.sealStatus === 20 || detail.contract.sealStatus === 50">
              <n-button v-permission="['AD_CONTRACT:UPDATE']" type="primary" @click="showDoubleSealUpload = true"
                >上传双盖附件</n-button
              >
              <n-button v-permission="['AD_CONTRACT:UPDATE']" type="info" @click="handleQuickSubmitArchive"
                >提交归档审批</n-button
              >
            </template>
            <!-- 归档审批中 → 老板可见：归档审核 -->
            <template v-if="detail.contract.sealStatus === 40 && isBoss">
              <n-button v-permission="['AD_CONTRACT:ARCHIVE_APPROVE']" type="warning" @click="showArchiveAudit = true"
                >归档审核</n-button
              >
            </template>
            <n-button @click="goBack">返回</n-button>
          </n-space>
        </template>

        <!-- 基本信息 -->
        <n-divider title-placement="left">基本信息</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="合同编号">{{ detail.contract.contractNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="合同名称">{{ detail.contract.contractName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="业务主体">{{ detail.businessEntityName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="合同方向">{{ detail.directionLabel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="合同类型">{{ detail.typeLabel || '-' }}</n-descriptions-item>
          <n-descriptions-item label="关联方">{{ detail.relatedPartyName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="关联订单">{{ detail.orderNo || '-' }}</n-descriptions-item>
          <n-descriptions-item label="签约主体">{{ detail.contract.signingEntity || '-' }}</n-descriptions-item>
          <n-descriptions-item label="有效期起">{{ fmtDate(detail.contract.validFrom) }}</n-descriptions-item>
          <n-descriptions-item label="有效期止">{{ fmtDate(detail.contract.validTo) }}</n-descriptions-item>
          <n-descriptions-item label="合同金额">{{ fmtAmount(detail.contract.amount) }}</n-descriptions-item>
          <n-descriptions-item label="用印附件">
            <template v-if="detail.contract.fileUrl">
              <n-space>
                <n-button size="tiny" type="primary" ghost @click="handlePreview(detail.contract.fileUrl)"
                  >预览</n-button
                >
                <n-button size="tiny" type="primary" ghost @click="handleDownload(detail.contract.fileUrl)"
                  >下载</n-button
                >
              </n-space>
            </template>
            <span v-else>-</span>
          </n-descriptions-item>
          <n-descriptions-item label="双盖附件">
            <template v-if="detail.contract.doubleSealFileUrl">
              <n-space>
                <n-button size="tiny" type="primary" ghost @click="handlePreview(detail.contract.doubleSealFileUrl!)"
                  >预览</n-button
                >
                <n-button size="tiny" type="primary" ghost @click="handleDownload(detail.contract.doubleSealFileUrl!)"
                  >下载</n-button
                >
              </n-space>
            </template>
            <span v-else>-</span>
          </n-descriptions-item>
          <n-descriptions-item label="状态">{{ detail.statusLabel || '-' }}</n-descriptions-item>
        </n-descriptions>

        <!-- 返点条款 -->
        <template v-if="detail.contract.rebateTerms">
          <n-divider title-placement="left">返点条款</n-divider>
          <n-descriptions label-placement="left" :column="1" bordered size="small">
            <n-descriptions-item label="返点条款">{{ detail.contract.rebateTerms }}</n-descriptions-item>
          </n-descriptions>
        </template>

        <!-- 审计信息 -->
        <n-divider title-placement="left">审计信息</n-divider>
        <n-descriptions label-placement="left" :column="3" bordered size="small">
          <n-descriptions-item label="创建人">{{ getUserName(detail.contract.createUser) }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ fmtDateTime(detail.contract.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="修改人">{{ getUserName(detail.contract.updateUser) }}</n-descriptions-item>
          <n-descriptions-item label="修改时间">{{ fmtDateTime(detail.contract.updateTime) }}</n-descriptions-item>
        </n-descriptions>

        <!-- 归档审批信息 -->
        <template v-if="detail.contract.archiveApproveRemark || detail.contract.archiveApproveUser">
          <n-divider title-placement="left">归档审批信息</n-divider>
          <n-descriptions label-placement="left" :column="3" bordered size="small">
            <n-descriptions-item label="审批人">{{ getUserName(detail.contract.archiveApproveUser) }}</n-descriptions-item>
            <n-descriptions-item label="审批时间">{{ fmtDateTime(detail.contract.archiveApproveTime) }}</n-descriptions-item>
            <n-descriptions-item label="审批备注">{{ detail.contract.archiveApproveRemark || '-' }}</n-descriptions-item>
          </n-descriptions>
        </template>

        <!-- 用印记录 -->
        <n-divider title-placement="left">用印记录</n-divider>
        <n-empty v-if="!detail.sealRecords || detail.sealRecords.length === 0" description="暂无用印记录" />
        <n-data-table
          v-else
          :columns="sealColumns"
          :data="detail.sealRecords"
          :row-key="(row: any) => row.id"
          :pagination="false"
          size="small"
        />
      </n-card>
    </n-spin>

    <!-- 上传双盖附件弹窗 -->
    <n-modal v-model:show="showDoubleSealUpload" preset="card" title="上传双盖附件" style="width: 520px">
      <div class="double-seal-upload">
        <n-upload
          :custom-request="handleDoubleSealUpload"
          :show-file-list="false"
          accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.png"
        >
          <n-button :loading="doubleSealUploading">
            {{ doubleSealFileUrl ? '重新上传' : '选择文件' }}
          </n-button>
        </n-upload>
        <div v-if="doubleSealFileUrl" class="text-n3 mt-2 text-[12px]">
          {{ doubleSealFileName || '已选择文件' }}
        </div>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDoubleSealUpload = false">取消</n-button>
          <n-button :loading="doubleSealUploading" @click="handleSaveDoubleSeal">仅保存</n-button>
          <n-button type="primary" :loading="doubleSealUploading" @click="handleSubmitArchive">提交归档审批</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 归档审核对话框 -->
    <n-modal v-model:show="showArchiveAudit" preset="card" title="归档审核" style="width: 480px">
      <n-space vertical>
        <div>
          <div class="action-modal-label">审批备注</div>
          <n-input
            v-model:value="archiveAuditRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入审批备注"
          />
        </div>
      </n-space>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showArchiveAudit = false">取消</n-button>
          <n-button type="success" @click="handleApproveArchive">通过</n-button>
          <n-button type="error" @click="handleRejectArchive">驳回</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { h, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    NButton,
    NCard,
    NDataTable,
    NDescriptions,
    NDescriptionsItem,
    NDivider,
    NEmpty,
    NInput,
    NModal,
    NSpace,
    NSpin,
    NTag,
    NUpload,
    useMessage,
  } from 'naive-ui';

  import { getAdSealRecordStatusLabel, getAdSealTypeLabel } from '@lib/shared/enums/advertisingEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { AdContractDetailResponse, AdSealRecordInfo } from '@lib/shared/models/advertising';

  import {
    approveArchive,
    getAdContractDetail,
    rejectArchive,
    submitArchive,
    uploadDoubleSeal,
    uploadTempAttachment,
  } from '@/api/modules';
  import useUserStore from '@/store/modules/user';

  import { AdvertisingRouteEnum } from '@/enums/routeEnum';

  import useUserMap from '../useUserMap';
  import { fmtAmount, fmtDate, fmtDateTime } from '../utils';
  import type { DataTableColumn } from 'naive-ui';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const message = useMessage();
  const userStore = useUserStore();

  const contractId = route.params.id as string;
  const loading = ref(false);
  const detail = ref<AdContractDetailResponse | null>(null);
  const showDoubleSealUpload = ref(false);
  const doubleSealFileUrl = ref('');
  const doubleSealFileName = ref('');
  const doubleSealUploading = ref(false);
  const showArchiveAudit = ref(false);
  const archiveAuditRemark = ref('');
  const isBoss = ref(true); // TODO: 根据当前用户角色判断

  function sealStatusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    if (status === 20 || status === 60) return 'success';
    if (status === 10 || status === 40) return 'warning';
    if (status === 30 || status === 50) return 'error';
    return 'default';
  }

  function sealRecordStatusTagType(status?: number): 'success' | 'warning' | 'error' | 'info' | 'default' {
    if (status === 10) return 'success';
    if (status === 20) return 'error';
    return 'warning';
  }

  const { loadUserMap, getUserName } = useUserMap();

  const sealColumns: DataTableColumn<AdSealRecordInfo>[] = [
    { key: 'sealType', title: '用印类型', width: 100, render: (row) => h('span', getAdSealTypeLabel(row.sealType)) },
    {
      key: 'status',
      title: '状态',
      width: 90,
      render: (row) =>
        h(
          NTag,
          { type: sealRecordStatusTagType(row.status) },
          { default: () => getAdSealRecordStatusLabel(row.status) }
        ),
    },
    { key: 'appliedCopies', title: '申请份数', width: 90 },
    { key: 'actualCopies', title: '实际份数', width: 90 },
    { key: 'applicantId', title: '申请人', width: 120, render: (row) => h('span', getUserName(row.applicantId)) },
    { key: 'applyRemark', title: '申请备注', width: 150, ellipsis: { tooltip: true } },
    { key: 'approverId', title: '审批人', width: 120, render: (row) => h('span', getUserName(row.approverId)) },
    { key: 'approveRemark', title: '审批备注', width: 150, ellipsis: { tooltip: true } },
    { key: 'approvedAt', title: '审批时间', width: 160, render: (row) => h('span', fmtDateTime(row.approvedAt)) },
  ];

  async function fetchDetail() {
    loading.value = true;
    try {
      detail.value = await getAdContractDetail(contractId);
    } catch (e) {
      message.error((e as Error).message || '加载失败');
    } finally {
      loading.value = false;
    }
  }

  /** 上传双盖附件文件 */
  async function handleDoubleSealUpload(opts: { file: any; onFinish: () => void; onError: () => void }) {
    doubleSealUploading.value = true;
    try {
      const rawFile = opts.file.file as File;
      const res: any = await uploadTempAttachment(rawFile);
      const fileId = res?.data?.[0] || res?.data || '';
      if (!fileId) throw new Error('上传返回异常');
      doubleSealFileUrl.value = fileId;
      doubleSealFileName.value = rawFile.name;
      message.success('文件上传成功');
      opts.onFinish();
    } catch (e) {
      message.error((e as Error).message || '上传失败');
      opts.onError();
    } finally {
      doubleSealUploading.value = false;
    }
  }

  async function handleSaveDoubleSeal() {
    if (!doubleSealFileUrl.value) {
      message.warning('请先上传文件');
      return;
    }
    try {
      await uploadDoubleSeal(contractId, doubleSealFileUrl.value);
      message.success('保存成功');
      showDoubleSealUpload.value = false;
      doubleSealFileUrl.value = '';
      doubleSealFileName.value = '';
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '保存失败');
    }
  }

  async function handleSubmitArchive() {
    const fileUrl = doubleSealFileUrl.value || detail.value?.contract.doubleSealFileUrl;
    if (!fileUrl) {
      message.warning('请先上传双盖附件');
      return;
    }
    try {
      await submitArchive(contractId, fileUrl);
      message.success('已提交归档审批');
      showDoubleSealUpload.value = false;
      doubleSealFileUrl.value = '';
      doubleSealFileName.value = '';
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '提交失败');
    }
  }

  /** 右上角快捷提交归档审批：校验是否已有双盖附件 */
  function handleQuickSubmitArchive() {
    if (!detail.value?.contract.doubleSealFileUrl) {
      message.warning('请先上传双盖附件');
      return;
    }
    handleSubmitArchive();
  }

  async function handleApproveArchive() {
    try {
      await approveArchive(contractId, archiveAuditRemark.value);
      message.success('归档审批通过');
      showArchiveAudit.value = false;
      archiveAuditRemark.value = '';
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '审批失败');
    }
  }

  async function handleRejectArchive() {
    try {
      await rejectArchive(contractId, archiveAuditRemark.value);
      message.success('已驳回');
      showArchiveAudit.value = false;
      archiveAuditRemark.value = '';
      await fetchDetail();
    } catch (e) {
      message.error((e as Error).message || '操作失败');
    }
  }

  /** 附件预览 */
  function handlePreview(fileUrl: string) {
    const previewUrl = `/attachment/preview/${fileUrl}?userId=${userStore.userInfo?.id || ''}`;
    window.open(previewUrl, '_blank');
  }

  /** 附件下载 */
  function handleDownload(fileUrl: string) {
    const downloadUrl = `/attachment/download/${fileUrl}?userId=${userStore.userInfo?.id || ''}`;
    const a = document.createElement('a');
    a.href = downloadUrl;
    a.click();
  }

  function goBack() {
    router.push({ name: AdvertisingRouteEnum.ADVERTISING_CONTRACT });
  }

  onMounted(async () => {
    await Promise.all([fetchDetail(), loadUserMap()]);
  });
</script>

<style scoped>
  .advertising-page {
    padding: 16px;
  }
  .action-modal-label {
    margin-bottom: 4px;
    font-size: 12px;
    color: var(--text-n2);
  }
</style>
