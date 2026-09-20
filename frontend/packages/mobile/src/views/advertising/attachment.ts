import { ref } from 'vue';
import { showImagePreview } from 'vant';

import useUserStore from '@/store/modules/user';

const IMAGE_EXT = /\.(png|jpe?g|gif|webp|bmp|svg)$/i;

/** 附件预览 / 下载地址（与 web 端保持一致：/attachment/preview|download/{fileUrl}?userId=） */
function buildUrl(base: string, fileUrl?: string): string {
  if (!fileUrl) return '';
  const userId = useUserStore().userInfo?.id || '';
  return `${base}/${fileUrl}?userId=${userId}`;
}

export function previewAttachmentUrl(fileUrl?: string): string {
  return buildUrl('/attachment/preview', fileUrl);
}

export function downloadAttachmentUrl(fileUrl?: string): string {
  return buildUrl('/attachment/download', fileUrl);
}

// 应用内 iframe 预览（非图片类，如 PDF）
const previewIframeVisible = ref(false);
const previewIframeUrl = ref('');
const previewIframeName = ref('');
const previewIframeFileUrl = ref('');

export function useAttachmentPreview() {
  return {
    previewIframeVisible,
    previewIframeUrl,
    previewIframeName,
    previewIframeFileUrl,
  };
}

export function previewAttachment(fileUrl?: string, fileName?: string) {
  if (!fileUrl) return;
  const url = previewAttachmentUrl(fileUrl);
  if (fileName && IMAGE_EXT.test(fileName)) {
    showImagePreview([url]);
    return;
  }
  previewIframeUrl.value = url;
  previewIframeName.value = fileName || '';
  previewIframeFileUrl.value = fileUrl;
  previewIframeVisible.value = true;
}

export function downloadAttachment(fileUrl?: string) {
  if (!fileUrl) return;
  window.open(downloadAttachmentUrl(fileUrl), '_blank');
}
