package cn.cordys.crm.ad.upstreamagent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdUpstreamAgentSaveRequest {

    @Schema(description = "id(编辑时必填)")
    private String id;

    @Schema(description = "代理名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "社会信用代码")
    private String creditCode;

    @Schema(description = "签约主体")
    private String signingEntity;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "电话")
    private String contactPhone;

    @Schema(description = "合作状态:10正常/20停用")
    private Integer cooperationStatus;

    @Schema(description = "状态:10正常/20停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "业务主体")
    private String businessEntityId;
}
