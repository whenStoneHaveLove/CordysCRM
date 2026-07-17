package cn.cordys.crm.ad.businessentity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 业务主体保存请求（V3.1 §4.1，创建/编辑共用）。
 * <p>创建时 {@code id} 留空，由服务生成；编辑时必须携带 {@code id}。</p>
 */
@Data
public class AdBusinessEntitySaveRequest {

    @Schema(description = "主体id（编辑时必填，创建时留空）")
    private String id;

    @Schema(description = "主体名称")
    private String name;

    @Schema(description = "主体代码(JS/TH)")
    private String code;

    @Schema(description = "状态:10启用/20停用")
    private Integer status;
}
