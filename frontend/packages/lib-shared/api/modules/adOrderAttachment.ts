import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
    AdOrderAttachmentDeleteUrl,
    AdOrderAttachmentEmlPreviewUrl,
    AdOrderAttachmentListUrl,
    AdOrderAttachmentUploadUrl,
} from '@lib/shared/api/requrls/adOrderAttachment';
import type { AdEmlPreview } from '@lib/shared/models/advertising';

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

    // 预览邮件记录(eml)：浏览器无法直接渲染 eml，由服务端解析后返回结构化内容
    function previewAdOrderEmlAttachment(orderId: string, attachmentId: string) {
        return CDR.get<AdEmlPreview>({
            url: `${AdOrderAttachmentEmlPreviewUrl}/${orderId}/attachment/${attachmentId}/eml-preview`,
        });
    }

    return { uploadAdOrderAttachment, getAdOrderAttachments, deleteAdOrderAttachment, previewAdOrderEmlAttachment };
}
