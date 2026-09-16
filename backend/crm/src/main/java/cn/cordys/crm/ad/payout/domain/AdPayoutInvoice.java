package cn.cordys.crm.ad.payout.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 付款单发票（单文件）。
 *
 * <p>发票文件以临时文件ID形式由前端上传，转存为正式附件（processTemp）后写入本表。
 * fileUrl 为转存后的附件记录 id（即 sys_attachment.id），可直接走 /attachment/preview/{id} 预览。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdPayoutInvoice extends BaseModel {

    @Schema(description = "付款单id")
    private String payoutId;

    @Schema(description = "发票文件ID（sys_attachment.id）")
    private String fileUrl;

    @Schema(description = "发票文件名")
    private String fileName;

    @Schema(description = "发票号码")
    private String invoiceNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "组织id")
    private String organizationId;
}
