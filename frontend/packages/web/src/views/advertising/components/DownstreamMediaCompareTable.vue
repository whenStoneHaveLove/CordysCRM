<template>
  <div class="dm-compare">
    <n-table v-if="rows.length" :bordered="true" size="small" :single-line="false" class="dm-compare-table">
      <thead>
        <tr>
          <th style="width: 130px">下游客户</th>
          <th style="width: 120px">字段</th>
          <th>原值（改前）</th>
          <th>新值（改后）</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, idx) in rows" :key="idx" :class="{ 'dm-diff-row': row.changed }">
          <td>{{ row.mediaName }}</td>
          <td>{{ row.label }}</td>
          <td class="before-value">{{ row.before }}</td>
          <td :class="row.changed ? 'after-value' : ''">{{ row.after }}</td>
        </tr>
      </tbody>
    </n-table>
    <n-empty v-else size="small" description="无下游客户明细" />
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { NEmpty, NTable } from 'naive-ui';

  import { AdModeOptions, AdPaymentMethodOptions, AdPostpayTriggerOptions } from '@lib/shared/enums/advertisingEnum';

  import { fmtAmount } from '../utils';

  /** 下游客户付款返点明细快照行（后端 buildDownstreamPayablesSnapshot 产出的结构） */
  interface PayableRow {
    downstreamMediaId?: string | number;
    payableAmount?: number | null;
    noRebateAmount?: number | null;
    rebateMode?: number | null;
    rebateValue?: number | null;
    paymentMethod?: number | null;
    paymentPrepayMode?: number | null;
    paymentPrepayRatio?: number | null;
    paymentPrepayDeadline?: string | number | null;
    paymentPostpayTrigger?: number | null;
    paymentPostpayDays?: number | null;
  }

  const props = withDefaults(
    defineProps<{
      /** 改前快照：下游客户明细数组（可能是 JSON 字符串或已解析数组） */
      before?: PayableRow[] | string | null;
      /** 改后快照：下游客户明细数组 */
      after?: PayableRow[] | string | null;
      /** 下游客户 id → 名称 映射；缺失时回落为 id */
      mediaNameMap?: Record<string, string>;
    }>(),
    {
      before: null,
      after: null,
      mediaNameMap: () => ({}),
    }
  );

  /** actualPayable 为前端计算列（不在快照里），故 key 用联合类型 */
  type FieldKey = keyof PayableRow | 'actualPayable';

  /** 明细字段展示定义，顺序即表格顺序 */
  const FIELD_DEFS: Array<{
    key: FieldKey;
    label: string;
    type?: 'amount' | 'percent' | 'enum' | 'date';
    options?: any[];
  }> = [
    { key: 'payableAmount', label: '应付金额', type: 'amount' },
    { key: 'noRebateAmount', label: '不记返金额', type: 'amount' },
    { key: 'rebateMode', label: '返点方式', type: 'enum', options: AdModeOptions },
    { key: 'rebateValue', label: '返点值', type: 'amount' },
    { key: 'actualPayable', label: '实际应付', type: 'amount' },
    { key: 'paymentMethod', label: '付款方式', type: 'enum', options: AdPaymentMethodOptions },
    { key: 'paymentPrepayMode', label: '预付模式', type: 'enum', options: AdModeOptions },
    { key: 'paymentPrepayRatio', label: '预付比例%', type: 'amount' },
    { key: 'paymentPrepayDeadline', label: '预付截止日', type: 'date' },
    { key: 'paymentPostpayTrigger', label: '后付触发', type: 'enum', options: AdPostpayTriggerOptions },
    { key: 'paymentPostpayDays', label: '后付天数' },
  ];

  function parseRows(value?: PayableRow[] | string | null): PayableRow[] {
    if (!value) return [];
    if (Array.isArray(value)) return value;
    const text = String(value).trim();
    if (!text.startsWith('[')) return [];
    try {
      const arr = JSON.parse(text);
      return Array.isArray(arr) ? arr : [];
    } catch {
      return [];
    }
  }

  function enumLabel(options: any[] | undefined, value: any): string {
    if (value === null || value === undefined || value === '') return '-';
    const found = (options || []).find((o: any) => String(o.value) === String(value));
    return found ? String(found.label) : String(value);
  }

  /** 实际应付 = (应付金额 - 不记返金额) - 返点（比例 / 固定金额） */
  function actualPayable(row: PayableRow): number {
    const base = Number(row.payableAmount || 0) - Number(row.noRebateAmount || 0);
    if (row.rebateMode === 10) return base - base * (Number(row.rebateValue || 0) / 100);
    if (row.rebateMode === 20) return base - Number(row.rebateValue || 0);
    return base;
  }

  /** 把时间戳 / 日期字符串统一格式化为 YYYY-MM-DD。
   *  后端存的是 Long 时间戳：10 位为秒、13 位为毫秒，都需转成日期 */
  function fmtDateStr(raw: any): string {
    const num = Number(raw);
    if (raw !== null && raw !== undefined && raw !== '' && !Number.isNaN(num) && String(raw).length >= 10) {
      const d = new Date(num < 1e12 ? num * 1000 : num);
      if (!Number.isNaN(d.getTime())) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
      }
    }
    const text = String(raw);
    return text.length >= 10 ? text.slice(0, 10) : text;
  }

  function fmtCell(def: (typeof FIELD_DEFS)[number], row?: PayableRow): string {
    if (!row) return '-';
    const raw = def.key === 'actualPayable' ? actualPayable(row) : (row as any)[def.key];
    if (raw === null || raw === undefined || raw === '') return '-';
    if (def.type === 'enum') return enumLabel(def.options, raw);
    if (def.type === 'amount') return fmtAmount(Number(raw));
    if (def.type === 'date') return fmtDateStr(raw);
    return String(raw);
  }

  const rows = computed(() => {
    const beforeRows = parseRows(props.before);
    const afterRows = parseRows(props.after);
    const beforeMap = new Map<string, PayableRow>();
    const afterMap = new Map<string, PayableRow>();
    beforeRows.forEach((r) => beforeMap.set(String(r.downstreamMediaId), r));
    afterRows.forEach((r) => afterMap.set(String(r.downstreamMediaId), r));

    // 顺序：以改后为准（新增客户排在后面），改前独有的客户也一并展示
    const orderedIds: string[] = [];
    afterRows.forEach((r) => {
      const id = String(r.downstreamMediaId);
      if (!orderedIds.includes(id)) orderedIds.push(id);
    });
    beforeRows.forEach((r) => {
      const id = String(r.downstreamMediaId);
      if (!orderedIds.includes(id)) orderedIds.push(id);
    });

    const result: Array<{
      mediaName: string;
      label: string;
      before: string;
      after: string;
      changed: boolean;
    }> = [];

    orderedIds.forEach((id) => {
      const b = beforeMap.get(id);
      const a = afterMap.get(id);
      const rawName = props.mediaNameMap[id] || id;
      // 改前无、改后有 → 新增；改前有、改后无 → 移除；都存在 → 名称
      let mediaName = rawName;
      if (!b) mediaName = `${rawName}（新增）`;
      else if (!a) mediaName = `${rawName}（移除）`;

      FIELD_DEFS.forEach((def, fieldIdx) => {
        const beforeText = !b ? '-' : fmtCell(def, b);
        const afterText = !a ? '-' : fmtCell(def, a);
        result.push({
          mediaName: fieldIdx === 0 ? mediaName : '',
          label: def.label,
          before: beforeText,
          after: afterText,
          changed: beforeText !== afterText,
        });
      });
    });

    return result;
  });
</script>

<style scoped>
  .dm-compare-table :deep(td) {
    vertical-align: middle;
    word-break: break-all;
  }
  .before-value {
    color: #999;
  }
  .after-value {
    color: #18a058;
    font-weight: 600;
  }
  .dm-diff-row {
    background-color: #f7fdf9;
  }
</style>
