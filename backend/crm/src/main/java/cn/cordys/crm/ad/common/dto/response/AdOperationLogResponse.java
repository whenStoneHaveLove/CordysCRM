package cn.cordys.crm.ad.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 广告操作日志列表响应。
 */
@Data
public class AdOperationLogResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private String id;

    @Schema(description = "模块")
    private String module;

    @Schema(description = "动作")
    private String action;

    @Schema(description = "目标id")
    private String targetId;

    @Schema(description = "操作人id")
    private String operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "IP")
    private String ip;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "变更前值(JSON)")
    private String beforeValue;

    @Schema(description = "变更后值(JSON)")
    private String afterValue;
}
