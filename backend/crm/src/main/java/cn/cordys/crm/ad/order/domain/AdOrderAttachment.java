package cn.cordys.crm.ad.order.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 广告订单附件（V3.1 §5.2.6）。
 * 注：本表按设计仅含 create_time/create_user（无 update_time/update_user），仍继承 BaseModel 以获得主键与组织隔离字段。
 */
@Data
@Table(name = "ad_order_attachment")
public class AdOrderAttachment extends BaseModel {

    @Schema(description = "订单id")
    private String orderId;

    @Schema(description = "类型:10排期/20邮件截图/30合同/40过程附件/50改单附件")
    private Integer type;

    @Schema(description = "文件地址")
    private String fileUrl;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
