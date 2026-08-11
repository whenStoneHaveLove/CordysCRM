package cn.cordys.crm.ad.common.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告操作日志分页查询请求。
 */
@Data
public class AdOperationLogPageRequest extends BasePageRequest {

    @Schema(description = "操作人id")
    private String operator;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "动作，如 CREATE/UPDATE/DELETE/SUBMIT/APPROVE/VOID...")
    private String action;

    @Schema(description = "模块，如 AD_ORDER/AD_CONTRACT...")
    private String module;

    @Schema(description = "目标id/关键字")
    private String keyword;
}
