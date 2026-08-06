import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
    AdOrderAttachmentUploadUrl,
    AdOrderAttachmentDeleteUrl,
    AdOrderAttachmentListUrl,
} from '@lib/shared/api/requrls/adOrderAttachment';

export default function useAdOrderAttachmentApi(CDR: CordysAxios) {
    // 上传附件：走系统 uploadFile（内部用 axiosInstance.request 直连，不走 request() 的 cloneDeep 破坏链）
    function uploadAdOrderAttachment(orderId: string, type: number, file: File) {
        return CDR.uploadFile<any>(
            { url: `${AdOrderAttachmentUploadUrl}/${orderId}/attachment?type=${type}` },
            { fileList: [file] },
            'file',
            false
        );
    }

    // 附件列表
    function getAdOrderAttachments(orderId: string) {
        return CDR.get<any>({ url: `${AdOrderAttachmentListUrl}/${orderId}/attachment` });
    }

    // 删除附件
    function deleteAdOrderAttachment(orderId: string, attachmentId: string) {
        return CDR.delete({ url: `${AdOrderAttachmentDeleteUrl}/${orderId}/attachment/${attachmentId}` });
    }

    return { uploadAdOrderAttachment, getAdOrderAttachments, deleteAdOrderAttachment };
}
