package cn.cordys.crm.ad.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 跨模块操作日志（V3.1 §5.2，L-18）。由 {@code @OperationLog} 切面写入。
 * 注：本表仅含 id/create_time 等实际列，不继承 BaseModel，避免 BaseMapper 生成表不存在的列。
 */
@Data
@Table(name = "ad_operation_log")
public class AdOperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private String id;

    @Schema(description = "模块(AD_ORDER/AD_CONTRACT/AD_PAYMENT...)")
    private String module;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "动作")
    private String action;

    @Schema(description = "目标id")
    private String targetId;

    @Schema(description = "操作人")
    private String operatorId;

    @Schema(description = "变更前(JSON)")
    private String beforeValue;

    @Schema(description = "变更后(JSON)")
    private String afterValue;

    @Schema(description = "IP")
    private String ip;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "创建时间")
    private Long createTime;
}
