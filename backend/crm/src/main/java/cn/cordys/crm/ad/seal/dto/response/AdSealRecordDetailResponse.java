package cn.cordys.crm.ad.seal.dto.response;

import cn.cordys.crm.ad.contract.domain.AdSealRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用印记录详情响应（M5 GET /api/ad/seal/{id}）：用印记录 + 所属合同/订单编号 + 业务主体名称 + 状态标签。
 */
@Data
public class AdSealRecordDetailResponse {

    @Schema(description = "用印记录")
    private AdSealRecord record;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "业务主体名称")
    private String businessEntityName;

    @Schema(description = "关联订单编号")
    private String orderNo;

    @Schema(description = "关联订单名称")
    private String orderName;

    @Schema(description = "状态标签")
    private String statusLabel;
}
