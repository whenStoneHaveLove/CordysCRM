package cn.cordys.crm.ad.seal.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 用印记录分页请求（M5，POST /api/ad/seal/page）。继承 {@link BasePageRequest} 获得 current/pageSize/sort。
 */
@Data
public class AdSealRecordPageRequest extends BasePageRequest {

    @Schema(description = "组织(租户)id（后端填充）")
    private String organizationId;

    @Schema(description = "主体隔离过滤集合；null 表示跨主体全量")
    private List<String> entityIds;

    @Schema(description = "合同id")
    private String contractId;

    @Schema(description = "用印类型:10公章/20合同章")
    private Integer sealType;

    @Schema(description = "用印记录状态:0审批中/10通过/20驳回")
    private Integer status;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "关键字（合同编号/合同名称）")
    private String keyword;

    @Schema(description = "申请日期-起（含）")
    private Date applyDateFrom;

    @Schema(description = "申请日期-止（含）")
    private Date applyDateTo;
}
