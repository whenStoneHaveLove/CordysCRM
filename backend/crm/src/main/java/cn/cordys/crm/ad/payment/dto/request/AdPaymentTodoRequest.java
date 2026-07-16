package cn.cordys.crm.ad.payment.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 财务待办分页请求（M4 T-33，GET /api/ad/payment/todo/page）。
 */
@Data
public class AdPaymentTodoRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null 表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "待办类型:PREPAY_CONFIRM/MEDIA_PREPAY/MEDIA_POSTPAY/RED_INVOICE/INVOICE_RECEIVE")
    private String todoType;

    @Schema(description = "关键字（订单号）")
    private String keyword;
}
