package cn.cordys.crm.ad.payout.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 付款单发票信息（单文件）。
 *
 * <p>fileUrl 即 sys_attachment.id，前端预览走 /attachment/preview/{fileUrl}，
 * 下载走 /attachment/download/{fileUrl}。</p>
 */
@Data
public class AdPayoutInvoiceResponse {

    @Schema(description = "发票记录id")
    private String id;

    @Schema(description = "发票文件ID（sys_attachment.id，用于预览/下载）")
    private String fileUrl;

    @Schema(description = "发票文件名")
    private String fileName;

    @Schema(description = "发票号码")
    private String invoiceNo;

    @Schema(description = "备注")
    private String remark;
}
