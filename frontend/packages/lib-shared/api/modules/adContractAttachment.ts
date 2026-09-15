import type { CordysAxios } from '@lib/shared/api/http/Axios';
import { AdContractAttachmentBaseUrl } from '@lib/shared/api/requrls/adContract';

export default function useAdContractAttachmentApi(CDR: CordysAxios) {
  // 上传合同附件（type:10用印附件/20双盖附件），tempFileId 为 /attachment/upload/temp 返回的临时文件ID
  function uploadAdContractAttachment(contractId: string, type: number, tempFileId: string, fileName?: string) {
    const params = new URLSearchParams();
    params.set('type', String(type));
    params.set('tempFileId', tempFileId);
    if (fileName) params.set('fileName', fileName);
    return CDR.post<any>({
      url: `${AdContractAttachmentBaseUrl}/${contractId}/attachment?${params.toString()}`,
    });
  }

  // 附件列表
  function getAdContractAttachments(contractId: string) {
    return CDR.get<any>({ url: `${AdContractAttachmentBaseUrl}/${contractId}/attachment` });
  }

  // 删除附件
  function deleteAdContractAttachment(contractId: string, attachmentId: string) {
    return CDR.delete({ url: `${AdContractAttachmentBaseUrl}/${contractId}/attachment/${attachmentId}` });
  }

  return { uploadAdContractAttachment, getAdContractAttachments, deleteAdContractAttachment };
}
