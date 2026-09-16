package cn.cordys.crm.ad.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 广告订单复制请求。
 *
 * <p>按用户勾选的字段把源订单的内容复制到一张新的草稿订单：
 * 勾选字段取源订单值，未勾选字段一律置空（金额派生列按 0 写入）。</p>
 */
@Data
public class AdOrderCopyRequest {

    @Schema(description = "源订单id（被复制的订单）")
    private String id;

    @Schema(description = "需要复制的字段key集合（见 AdOrderService.CopyField）；为空表示不复制任何业务字段")
    private List<String> fields;

    @Schema(description = "新订单名称（可选，为空则按「源订单名称-复制」生成）")
    private String orderName;
}
