<template>
  <div class="flex h-full flex-col overflow-hidden bg-[var(--text-n9)]">
    <van-nav-bar :title="t(isEdit ? 'advertising.downstreamMedia.form.title.edit' : 'advertising.downstreamMedia.form.title.create')" left-arrow @click-left="back" />
    <div class="flex-1 overflow-auto px-[12px] py-[12px]">
      <div class="rounded-lg bg-white px-[12px] py-[4px]">
        <van-field
          :label="t('advertising.downstreamMedia.form.name')"
          v-model="name"
          required
          :placeholder="t('advertising.downstreamMedia.form.name')"
        />
        <van-field
          :label="t('advertising.downstreamMedia.form.mediaType')"
          :model-value="labelOf(mediaTypeOptions, mediaType)"
          readonly
          is-link
          @click="openPicker('mediaType', mediaTypeOptions)"
        />
        <van-field
          :label="t('advertising.downstreamMedia.form.channel')"
          v-model="channel"
          :placeholder="t('advertising.downstreamMedia.form.channel')"
        />
        <van-field
          :label="t('advertising.downstreamMedia.form.rateCard')"
          v-model="rateCard"
          :placeholder="t('advertising.downstreamMedia.form.rateCard')"
        />
        <van-field
          :label="t('advertising.downstreamMedia.form.discountPolicy')"
          v-model="discountPolicy"
          :placeholder="t('advertising.downstreamMedia.form.discountPolicy')"
        />
        <van-field
          :label="t('advertising.downstreamMedia.form.contactPerson')"
          v-model="contactPerson"
          :placeholder="t('advertising.downstreamMedia.form.contactPerson')"
        />
        <van-field
          :label="t('advertising.downstreamMedia.form.contactPhone')"
          v-model="contactPhone"
          :placeholder="t('advertising.downstreamMedia.form.contactPhone')"
        />
        <van-field
          :label="t('advertising.downstreamMedia.form.cooperationStatus')"
          :model-value="labelOf(AdResourceStatusOptions as any, cooperationStatus)"
          readonly
          is-link
          @click="openPicker('cooperationStatus', AdResourceStatusOptions as any)"
        />
      </div>

      <!-- 银行账号子表 -->
      <div class="mt-[12px] rounded-lg bg-white px-[12px] py-[8px]">
        <div class="flex items-center justify-between py-[4px]">
          <span class="text-[14px] font-medium text-[var(--text-n1)]">{{ t('advertising.downstreamMedia.form.accounts') }}</span>
          <van-button size="mini" type="primary" plain @click="addAccount">{{ t('advertising.downstreamMedia.form.addAccount') }}</van-button>
        </div>
        <div
          v-for="(acc, idx) in accounts"
          :key="idx"
          class="border-t border-[var(--text-n8)] py-[8px]"
        >
          <div class="mb-[6px] flex items-center justify-between">
            <span class="text-[13px] font-medium text-[var(--text-n2)]">#{{ idx + 1 }}</span>
            <div class="flex items-center gap-[12px]">
              <van-switch v-model="acc.disabledOn" size="18px" />
              <span class="text-[12px] text-[var(--text-n3)]">{{ t('advertising.downstreamMedia.form.disabled') }}</span>
              <van-icon name="delete-o" class="text-[16px] text-[#ee0a24]" @click="removeAccount(idx)" />
            </div>
          </div>
          <van-field
            :label="t('advertising.downstreamMedia.form.payeeName')"
            v-model="acc.payeeName"
    
            :placeholder="t('advertising.downstreamMedia.form.payeeName')"
          />
          <van-field
            :label="t('advertising.downstreamMedia.form.bankName')"
            v-model="acc.bankName"
    
            :placeholder="t('advertising.downstreamMedia.form.bankName')"
          />
          <van-field
            :label="t('advertising.downstreamMedia.form.bankAccount')"
            v-model="acc.bankAccount"
    
            :placeholder="t('advertising.downstreamMedia.form.bankAccount')"
          />
        </div>
        <div v-if="!accounts.length" class="py-[12px] text-center text-[13px] text-[var(--text-n4)]">
          {{ t('advertising.downstreamMedia.form.noAccount') }}
        </div>
      </div>
    </div>

    <div class="flex gap-[12px] border-t border-[var(--text-n8)] bg-white p-[12px]">
      <van-button block plain @click="back">{{ t('advertising.form.cancel') }}</van-button>
      <van-button block type="primary" :loading="submitting" @click="save">{{ t('advertising.form.save') }}</van-button>
    </div>

    <van-popup v-model:show="pickerShow" position="bottom" round>
      <van-picker
        :title="pickerTitle"
        :columns="pickerColumns"
        :model-value="pickerValue"
        show-toolbar
        @confirm="onPickerConfirm"
        @cancel="pickerShow = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { showToast } from 'vant';

  import { AdResourceStatusOptions } from '@lib/shared/enums/advertisingEnum';

  import {
    createAdDownstreamMedia,
    getAdDictPage,
    getAdDownstreamMediaDetail,
    updateAdDownstreamMedia,
  } from '@/api/modules';

  interface Option {
    label: string;
    value: number | string;
  }
  interface Account {
    id?: string;
    payeeName: string;
    bankName: string;
    bankAccount: string;
    disabled?: number;
    disabledOn: boolean;
  }

  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();
  const editId = ref<string>((route.query.id as string) || '');
  const isEdit = computed(() => !!editId.value);
  const submitting = ref(false);

  const name = ref('');
  const mediaType = ref<number | string>('');
  const channel = ref('');
  const rateCard = ref('');
  const discountPolicy = ref('');
  const contactPerson = ref('');
  const contactPhone = ref('');
  const cooperationStatus = ref<number>(10);
  const accounts = ref<Account[]>([]);
  const mediaTypeOptions = ref<Option[]>([]);

  const pickerShow = ref(false);
  const pickerTitle = ref('');
  const pickerColumns = ref<{ text: any; value: any }[]>([]);
  const pickerValue = ref<any[]>([]);
  const pickerField = ref('');

  function labelOf(options: Option[], value: any): string {
    if (value === '' || value === null || value === undefined) return '';
    return options.find((o) => o.value === value)?.label || String(value);
  }

  function back() {
    router.back();
  }

  function openPicker(field: string, options: Option[]) {
    pickerField.value = field;
    pickerTitle.value = t(field === 'cooperationStatus' ? 'advertising.downstreamMedia.form.cooperationStatus' : 'advertising.downstreamMedia.form.mediaType');
    pickerColumns.value = options.map((o) => ({ text: o.label, value: o.value }));
    const cur = field === 'cooperationStatus' ? cooperationStatus.value : mediaType.value;
    pickerValue.value = [cur];
    pickerShow.value = true;
  }
  function onPickerConfirm({ selectedValues }: any) {
    const v = selectedValues?.[0];
    if (pickerField.value === 'cooperationStatus') cooperationStatus.value = v;
    else mediaType.value = v;
    pickerShow.value = false;
  }

  function addAccount() {
    accounts.value.push({ payeeName: '', bankName: '', bankAccount: '', disabled: 0, disabledOn: false });
  }
  function removeAccount(idx: number) {
    accounts.value.splice(idx, 1);
  }

  async function save() {
    if (!name.value) {
      showToast(`${t('advertising.downstreamMedia.form.name')} ${t('advertising.form.required')}`);
      return;
    }
    const payload: Record<string, any> = {
      name: name.value,
      mediaType: mediaType.value === '' ? undefined : String(mediaType.value),
      channel: channel.value,
      rateCard: rateCard.value,
      discountPolicy: discountPolicy.value,
      contactPerson: contactPerson.value,
      contactPhone: contactPhone.value,
      cooperationStatus: cooperationStatus.value,
      accounts: accounts.value.map((a) => ({
        id: a.id,
        payeeName: a.payeeName,
        bankName: a.bankName,
        bankAccount: a.bankAccount,
        disabled: a.disabledOn ? 1 : 0,
      })),
    };
    submitting.value = true;
    try {
      if (isEdit.value) await updateAdDownstreamMedia({ id: editId.value, ...payload });
      else await createAdDownstreamMedia(payload);
      showToast(t('advertising.common.saveSuccess'));
      router.back();
    } finally {
      submitting.value = false;
    }
  }

  onMounted(async () => {
    const dict: any = await getAdDictPage({ keyword: '', pageSize: 200, current: 1, dictCode: 'media_type' });
    mediaTypeOptions.value = (dict?.list || []).map((i: any) => ({ label: i.dictLabel || i.dictValue, value: i.dictValue }));
    if (isEdit.value) {
      const d: any = await getAdDownstreamMediaDetail(editId.value);
      const m = d?.media || {};
      name.value = m.name || '';
      mediaType.value = m.mediaType ?? '';
      channel.value = m.channel || '';
      rateCard.value = m.rateCard || '';
      discountPolicy.value = m.discountPolicy || '';
      contactPerson.value = m.contactPerson || '';
      contactPhone.value = m.contactPhone || '';
      cooperationStatus.value = m.cooperationStatus ?? 10;
      accounts.value = (d?.accountList || []).map((a: any) => ({
        id: a.id,
        payeeName: a.payeeName || '',
        bankName: a.bankName || '',
        bankAccount: a.bankAccount || '',
        disabled: a.disabled,
        disabledOn: a.disabled === 1,
      }));
    }
  });
</script>
