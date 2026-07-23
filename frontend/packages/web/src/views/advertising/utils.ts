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

/**
 * 后端部分数值字段以字符串形式返回（如 rateCard：DB 中为 VARCHAR(512)，后端 DTO 定义为 String）。
 * 回填到 Naive UI 数值控件（n-input-number，要求 number 类型）前统一转换为 number | null，
 * 避免控件显示空值或 NaN。非数值字符串安全回退为 null。
 */
export function toNumberOrNull(value?: number | string | null): number | null {
  if (value == null || value === '') return null;
  const n = typeof value === 'string' ? Number(value) : value;
  return Number.isNaN(n) ? null : n;
}

/** ISO 字符串 / 时间戳 -> epoch ms（用于 NDatePicker 回填）。 */
export function toTimeStamp(value?: number | string | null): number | null {
  if (value == null || value === '') return null;
  const d = new Date(value as number | string);
  if (Number.isNaN(d.getTime())) return null;
  return d.getTime();
}
