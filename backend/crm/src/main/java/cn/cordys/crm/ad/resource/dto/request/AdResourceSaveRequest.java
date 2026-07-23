package cn.cordys.crm.ad.resource.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 广告资源保存请求（M6，创建/编辑共用）。
 * <p>创建时 {@code id} 留空，由服务生成；编辑时必须携带 {@code id}。</p>
 */
@Data
public class AdResourceSaveRequest {

    @Schema(description = "资源id（编辑时必填，创建时留空）")
    private String id;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "资源类型:10上游代理/20下游媒体")
    private Integer resourceType;

    @Schema(description = "媒体类型")
    private String mediaType;

    @Schema(description = "渠道")
    private String channel;

    @Schema(description = "刊例价")
    private String rateCard;

    @Schema(description = "折扣政策")
    private String discountPolicy;

    @Schema(description = "信用代码")
    private String creditCode;

    @Schema(description = "签约主体")
    private String signingEntity;

    @Schema(description = "广告位")
    private String position;

    @Schema(description = "日均曝光量")
    private Long dailyImpressions;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "状态:10正常/20停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "归属业务主体id")
    private String businessEntityId;
}
