package cn.cordys.crm.ad.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 改单申请请求（M3 POST /api/ad/order-change）。
 *
 * <p>携带变更字段清单 {@code changeFields} 与变更后快照 {@code after}（仅白名单内业务字段）。
 * 提交时校验 {@code orderType} 不可变更（L-24），未知字段将被拒绝。</p>
 */
@Data
public class AdOrderChangeSaveRequest {

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "变更原因")
    private String reason;

    @Schema(description = "变更字段清单(如 [\"totalAmount\",\"deliveryEndDate\"])")
    private List<String> changeFields;

    @Schema(description = "变更后快照(Map<字段名,新值>)，键与 changeFields 一一对应；"
            + "值按 Java 字段类型：金额=数值、日期=毫秒时间戳、枚举=整数、文本=字符串")
    private Map<String, Object> after;
}
