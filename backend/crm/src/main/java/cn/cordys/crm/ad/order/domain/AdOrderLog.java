package cn.cordys.crm.ad.order.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * 广告订单操作记录（详情 Tab，V3.1 §5.2）。
 * 注：ad_order_log 表无 deleted/createUser/updateUser/updateTime 列，故不继承 BaseModel。
 */
@Data
@Table(name = "ad_order_log")
public class AdOrderLog implements Serializable {

    @Schema(description = "id")
    private String id;

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

    @Schema(description = "创建时间")
    private Long createTime;
}
