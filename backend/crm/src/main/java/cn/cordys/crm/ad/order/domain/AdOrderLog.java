package cn.cordys.crm.ad.order.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 广告订单操作记录（详情 Tab，V3.1 §5.2）。
 */
@Data
@Table(name = "ad_order_log")
public class AdOrderLog extends BaseModel {

    @Schema(description = "订单id")
    private String orderId;

    @Schema(description = "动作(SUBMIT/APPROVE/REJECT/CHANGE/VOID/ARCHIVE...)")
    private String action;

    @Schema(description = "操作人")
    private String operatorId;

    @Schema(description = "变更前(L-30 仅变更字段)")
    private String beforeValue;

    @Schema(description = "变更后")
    private String afterValue;

    @Schema(description = "IP")
    private String ip;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
