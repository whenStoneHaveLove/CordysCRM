package cn.cordys.crm.ad.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 改单列表项（M3 pageList 映射）。
 * 字段别名与 ExtAdOrderChangeMapper.xml 中 SELECT 的 AS 保持一致。
 */
@Data
public class AdOrderChangeListResponse {

    @Schema(description = "改单id")
    private String id;

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "订单名称")
    private String orderName;

    @Schema(description = "变更字段清单(JSON 数组)")
    private String changeFields;

    @Schema(description = "变更原因")
    private String reason;

    @Schema(description = "改单状态")
    private Integer status;

    @Schema(description = "状态中文标签")
    private String statusLabel;

    @Schema(description = "审批人")
    private String approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "审批时间")
    private Date approvedAt;

    @Schema(description = "创建人")
    private String creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "创建时间")
    private Long createTime;
}
