package cn.cordys.crm.ad.contract.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 合同分页请求（M5，POST /api/ad/contract/page）。继承 {@link BasePageRequest} 获得 current/pageSize/sort。
 */
@Data
public class AdContractPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null 表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "合同方向:10上游/20下游")
    private Integer contractDirection;

    @Schema(description = "合同类型:10框架/20单笔")
    private Integer contractType;

    @Schema(description = "关联方类型:10客户/20上游代理/30下游媒体")
    private Integer relatedPartyType;

    @Schema(description = "合同状态:10生效/20失效/30已作废")
    private Integer status;

    @Schema(description = "用印状态:0未申请/10审批中/20已用印/30已驳回")
    private Integer sealStatus;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "关联变更单id")
    private String changeOrderId;

    @Schema(description = "关键字（合同编号/合同名称）")
    private String keyword;

    @Schema(description = "有效期起-起（含）")
    private Date validFromFrom;

    @Schema(description = "有效期止-止（含）")
    private Date validFromTo;
}
