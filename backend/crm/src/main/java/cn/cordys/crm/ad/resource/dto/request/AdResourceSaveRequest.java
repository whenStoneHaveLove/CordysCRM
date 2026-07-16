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

    @Schema(description = "资源类型:10线上媒体/20线下广告牌/30电视/40广播/50印刷")
    private Integer resourceType;

    @Schema(description = "媒体渠道")
    private String mediaChannel;

    @Schema(description = "广告位")
    private String position;

    @Schema(description = "日均曝光量")
    private Long dailyImpressions;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "状态:0可用/10已占用/20维护中")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "归属业务主体id")
    private String businessEntityId;
}
