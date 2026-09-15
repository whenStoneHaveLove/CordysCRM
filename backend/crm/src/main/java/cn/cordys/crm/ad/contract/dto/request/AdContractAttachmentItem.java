package cn.cordys.crm.ad.contract.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 合同附件项（前端上传时携带临时文件ID + 文件名）。
 */
@Data
public class AdContractAttachmentItem {

    @Schema(description = "临时文件ID（/attachment/upload/temp 返回）")
    private String tempFileId;

    @Schema(description = "文件名")
    private String fileName;
}
