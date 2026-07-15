package cn.cordys.crm.ad.dashboard.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 广告报表配置（V3.1 §4.6）。本期仅占位，报表 SQL 后端硬编码。
 */
@Data
@Table(name = "ad_report_config")
public class AdReportConfig extends BaseModel {

    @Schema(description = "报表编码(order-execution/receivable-payable/...)")
    private String reportCode;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "筛选字段配置(JSON)")
    private String filterFields;

    @Schema(description = "SQL模板标识(映射后端固定SQL)")
    private String sqlTemplateKey;

    @Schema(description = "状态:10启用/20停用")
    private Integer status = 10;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
