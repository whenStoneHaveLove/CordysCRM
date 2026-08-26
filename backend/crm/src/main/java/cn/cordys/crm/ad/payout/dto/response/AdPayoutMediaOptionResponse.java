package cn.cordys.crm.ad.payout.dto.response;

import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaAccountItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 付款单选下拉项（订单的下游客户，JOIN ad_downstream_media 填充名称）。
 */
@Data
public class AdPayoutMediaOptionResponse {

    @Schema(description = "订单-下游客户关联id")
    private String id;

    @Schema(description = "字典id")
    private String mediaId;

    @Schema(description = "名称")
    private String mediaName;

    @Schema(description = "应付金额")
    private BigDecimal payableAmount;

    @Schema(description = "不记返金额")
    private BigDecimal noRebateAmount;

    @Schema(description = "返点方式:10-比例/20-固定金额")
    private Integer rebateMode;

    @Schema(description = "返点值")
    private BigDecimal rebateValue;

    @Schema(description = "返点金额")
    private BigDecimal rebateAmount;

    @Schema(description = "实际应付")
    private BigDecimal actualPayable;

    @Schema(description = "该客户累计已付金额(含草稿/审核中)")
    private BigDecimal paidAmount;

    @Schema(description = "该下游客户的银行账户列表(可用状态优先)")
    private List<AdDownstreamMediaAccountItem> accountList;
}