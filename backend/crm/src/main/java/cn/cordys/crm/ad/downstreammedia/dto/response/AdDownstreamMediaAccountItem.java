package cn.cordys.crm.ad.downstreammedia.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdDownstreamMediaAccountItem {

    @Schema(description = "账户id")
    private String id;

    @Schema(description = "下游客户id")
    private String downstreamMediaId;

    @Schema(description = "收款人全称")
    private String payeeName;

    @Schema(description = "开户行")
    private String bankName;

    @Schema(description = "银行账号")
    private String bankAccount;

    @Schema(description = "是否停用:0-启用/1-停用")
    private Integer disabled;

    @Schema(description = "创建时间(毫秒)")
    private Long createTime;
}
