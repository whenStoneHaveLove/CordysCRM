package cn.cordys.crm.ad.contract.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 合同摘要（收款/付款详情里展示订单关联的合同信息）。
 */
@Data
public class AdContractBriefResponse {

    @Schema(description = "合同id")
    private String id;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合同名称")
    private String contractName;

    @Schema(description = "合同类型:10框架/20单笔/30服务/40其他")
    private Integer contractType;

    @Schema(description = "合同方向:10上游/20下游")
    private Integer contractDirection;

    @Schema(description = "合同金额")
    private BigDecimal amount;

    @Schema(description = "用印状态")
    private Integer sealStatus;
}
