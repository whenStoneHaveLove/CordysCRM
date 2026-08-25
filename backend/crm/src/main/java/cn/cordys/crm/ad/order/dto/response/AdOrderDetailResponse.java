package cn.cordys.crm.ad.order.dto.response;

import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.domain.AdOrderAttachment;
import cn.cordys.crm.ad.order.domain.AdOrderChange;
import cn.cordys.crm.ad.order.domain.AdOrderLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 广告订单详情（M2 GET /api/ad/order/{id}）。
 * 包含：订单主信息 + 附件列表 + 改单历史 + 操作记录 + 当前角色可见的允许动作。
 */
@Data
public class AdOrderDetailResponse {

    @Schema(description = "订单主信息")
    private AdOrder order;

    @Schema(description = "关联合同ID（详情时从 ad_order_contract 查询填充）")
    private String contractId;

    @Schema(description = "关联合同编号")
    private String contractNo;

    @Schema(description = "关联合同名称")
    private String contractName;

    @Schema(description = "下游客户id列表（详情时从 ad_order_downstream_media 查询填充）")
    private List<String> downstreamMediaIds;

    @Schema(description = "附件列表")
    private List<AdOrderAttachment> attachments;

    @Schema(description = "改单记录（M3，可空）")
    private List<AdOrderChange> changes;

    @Schema(description = "操作记录（时间线）")
    private List<AdOrderLog> logs;

    @Schema(description = "当前角色允许执行的动作")
    private List<AdOrderAllowedAction> allowedActions;
}
