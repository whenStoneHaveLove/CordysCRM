package cn.cordys.crm.ad.contract.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合同附件（用印附件 / 双盖附件，支持多文件）。
 *
 * <p>type: 10=用印附件, 20=双盖附件。value(fileUrl) 为转存后的附件记录 id（即 sys_attachment.id）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdContractAttachment extends BaseModel {

    @Schema(description = "合同id")
    private String contractId;

    @Schema(description = "附件类型:10用印附件/20双盖附件")
    private Integer type;

    @Schema(description = "附件文件ID（sys_attachment.id）")
    private String fileUrl;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "组织id")
    private String organizationId;

    @Schema(description = "逻辑删除:0未删/1已删")
    private Integer deleted = 0;
}
