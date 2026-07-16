package cn.cordys.crm.ad.resource.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 广告资源（V3.1 §5.2.7，M6扩展）。
 *
 * <p>M1~M5 字段：id, resourceType, name, mediaType, channel, rateCard, discountPolicy,
 * creditCode, signingEntity, businessEntityId, status(10正常/20停用), organizationId, deleted。
 * M6 新增：position, dailyImpressions, unitPrice, remark（广告资源管理场景）。</p>
 */
@Data
@Table(name = "ad_resource")
public class AdResource extends BaseModel {

    /** M1~M5: 资源类型:10代理/20媒体 */
    @Schema(description = "资源类型:10代理/20媒体")
    private Integer resourceType;

    /** M1~M5: 名称（M6中视为resourceName） */
    @Schema(description = "名称")
    private String name;

    /** M1~M5: 媒体类型(字典) */
    @Schema(description = "媒体类型(字典)")
    private String mediaType;

    /** M1~M5: 渠道（M6中视为mediaChannel） */
    @Schema(description = "渠道")
    private String channel;

    /** M1~M5: 刊例价 */
    @Schema(description = "刊例价")
    private String rateCard;

    /** M1~M5: 折扣政策 */
    @Schema(description = "折扣政策")
    private String discountPolicy;

    /** M1~M5: 信用代码 */
    @Schema(description = "信用代码")
    private String creditCode;

    /** M1~M5: 签约主体 */
    @Schema(description = "签约主体")
    private String signingEntity;

    /** M1~M5: 归属业务主体 */
    @Schema(description = "归属业务主体")
    private String businessEntityId;

    /** M1~M5: 状态:10正常/20停用 */
    @Schema(description = "状态:10正常/20停用")
    private Integer status = 10;

    /** M6: 广告位 */
    @Schema(description = "广告位(M6)")
    private String position;

    /** M6: 日均曝光量 */
    @Schema(description = "日均曝光量(M6)")
    private Long dailyImpressions;

    /** M6: 单价 */
    @Schema(description = "单价(M6)")
    private BigDecimal unitPrice;

    /** M6: 备注 */
    @Schema(description = "备注(M6)")
    private String remark;

    /** M1~M5: 组织(租户)id */
    @Schema(description = "组织(租户)id")
    private String organizationId;

    /** 是否删除:0-否/1-是 */
    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
