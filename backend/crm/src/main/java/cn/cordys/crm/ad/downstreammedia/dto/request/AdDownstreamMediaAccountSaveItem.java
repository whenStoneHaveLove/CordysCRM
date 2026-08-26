package cn.cordys.crm.ad.downstreammedia.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下游客户银行账户（保存项）。
 * id 为空表示新增；不为空表示保留/更新；前端通过不传该 id 来“软删除”（不在此列表内的原账户将被逻辑删除）。
 */
@Data
public class AdDownstreamMediaAccountSaveItem {

    @Schema(description = "账户id(编辑已有账户时传，新增不传)")
    private String id;

    @Schema(description = "收款人全称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String payeeName;

    @Schema(description = "开户行", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bankName;

    @Schema(description = "银行账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bankAccount;

    @Schema(description = "是否停用:0-启用/1-停用")
    private Integer disabled = 0;
}
