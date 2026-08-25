package cn.cordys.crm.ad.order.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 订单-下游客户中间表。
 * 一个订单可关联多个下游客户，一个下游客户可被多个订单关联。
 * 注意：不继承 BaseModel，因为表里没有 create_user/update_user/update_time 列。
 */
@Data
@Table(name = "ad_order_downstream_media")
public class AdOrderDownstreamMedia {

    @Schema(description = "id")
    private String id;

    @Schema(description = "订单id")
    private String orderId;

    @Schema(description = "下游客户id")
    private String downstreamMediaId;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
