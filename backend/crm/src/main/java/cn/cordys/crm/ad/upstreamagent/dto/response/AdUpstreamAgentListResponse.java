package cn.cordys.crm.ad.upstreamagent.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdUpstreamAgentListResponse {

    @Schema(description = "id")
    private String id;

    @Schema(description = "代理名称")
    private String name;

    @Schema(description = "社会信用代码")
    private String creditCode;

    @Schema(description = "签约主体")
    private String signingEntity;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "电话")
    private String contactPhone;

    @Schema(description = "合作状态")
    private Integer cooperationStatus;

    @Schema(description = "合作状态标签")
    private String cooperationStatusLabel;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "业务主体")
    private String businessEntityId;

    @Schema(description = "业务主体名称")
    private String businessEntityName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Long createTime;
}
