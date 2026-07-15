package cn.cordys.crm.ad.order.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

/**
 * 广告订单改单记录（V3.1 §5.2.2）。
 */
@Data
@Table(name = "ad_order_change")
public class AdOrderChange extends BaseModel {

    @Schema(description = "订单id")
    private String orderId;

    @Schema(description = "变更字段清单(JSON 数组，如 [\"total_amount\",\"delivery_end_date\"])")
    private String changeFields;

    @Schema(description = "变更原因")
    private String reason;

    @Schema(description = "变更前快照(L-30 仅变更字段)")
    private String snapshotBefore;

    @Schema(description = "变更后快照")
    private String snapshotAfter;

    @Schema(description = "状态:0待审批/10通过/20驳回")
    private Integer status = 0;

    @Schema(description = "审批人")
    private String approverId;

    @Schema(description = "审批时间")
    private Date approvedAt;

    @Schema(description = "审批备注")
    private String approveRemark;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
