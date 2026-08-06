package cn.cordys.crm.ad.upstreamagent.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "ad_upstream_agent")
public class AdUpstreamAgent extends BaseModel {

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

    @Schema(description = "合作状态:10正常/20停用")
    private Integer cooperationStatus;

    @Schema(description = "状态:10正常/20停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "业务主体")
    private String businessEntityId;

    @Schema(description = "组织(租户)id")
    private String organizationId;

    @Schema(description = "是否删除:0-否/1-是")
    private Integer deleted = 0;
}
