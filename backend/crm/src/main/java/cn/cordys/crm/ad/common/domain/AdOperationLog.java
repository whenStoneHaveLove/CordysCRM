package cn.cordys.crm.ad.common.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 跨模块操作日志（V3.1 §5.2，L-18）。由 {@code @OperationLog} 切面写入。
 * 注：本表按设计仅含 create_time（无 update_time/update_user），仍继承 BaseModel 以获得主键与组织隔离字段。
 */
@Data
@Table(name = "ad_operation_log")
public class AdOperationLog extends BaseModel {

    @Schema(description = "模块(ORDER/CONTRACT/PAYMENT/SYSTEM...)")
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

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
