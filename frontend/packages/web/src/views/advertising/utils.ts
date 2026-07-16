/**
 * 广告模块通用格式化工具（日期可能为 number(epoch ms) 或 string(ISO)，统一处理）。
 */

export function fmtDate(value?: number | string | null): string {
  if (value == null || value === '') return '-';
  const d = new Date(value as number | string);
  if (Number.isNaN(d.getTime())) return String(value);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

export function fmtDateTime(value?: number | string | null): string {
  if (value == null || value === '') return '-';
  const d = new Date(value as number | string);
  if (Number.isNaN(d.getTime())) return String(value);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function fmtAmount(value?: number | string | null): string {
  if (value == null || value === '') return '-';
  const n = typeof value === 'string' ? Number(value) : value;
  if (Number.isNaN(n)) return String(value);
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

/** ISO 字符串 / 时间戳 -> epoch ms（用于 NDatePicker 回填）。 */
export function toTimeStamp(value?: number | string | null): number | null {
  if (value == null || value === '') return null;
  const d = new Date(value as number | string);
  if (Number.isNaN(d.getTime())) return null;
  return d.getTime();
}
