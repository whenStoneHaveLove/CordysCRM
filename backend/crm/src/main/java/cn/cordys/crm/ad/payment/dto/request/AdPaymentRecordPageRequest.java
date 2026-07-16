package cn.cordys.crm.ad.payment.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 收付款记录分页请求（M4，POST /api/ad/payment/page）。
 * 继承 {@link BasePageRequest} 获得 current/pageSize/sort；其余为本模块筛选字段。
 */
@Data
public class AdPaymentRecordPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null 表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "关联订单id")
    private String orderId;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "方向:10上游收款/20下游付款")
    private Integer direction;

    @Schema(description = "类型:10预收/20预付/30开票收款/40媒体尾款/50退款/60坏账")
    private Integer type;

    @Schema(description = "关键字（订单号/客户名/发票号）")
    private String keyword;

    @Schema(description = "发生日期-起（含）")
    private Date occurDateFrom;

    @Schema(description = "发生日期-止（含）")
    private Date occurDateTo;
}
