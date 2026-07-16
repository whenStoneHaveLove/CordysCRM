package cn.cordys.crm.ad.contract.dto.response;

import cn.cordys.crm.ad.contract.domain.AdContract;
import cn.cordys.crm.ad.contract.domain.AdSealRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 合同详情响应（M5 GET /api/ad/contract/{id}）：合同主信息 + 关联名称 + 状态标签 + 用印记录历史。
 */
@Data
public class AdContractDetailResponse {

    @Schema(description = "合同主信息")
    private AdContract contract;

    @Schema(description = "业务主体名称")
    private String businessEntityName;

    @Schema(description = "关联方名称")
    private String relatedPartyName;

    @Schema(description = "关联订单编号")
    private String orderNo;

    @Schema(description = "合同状态标签")
    private String statusLabel;

    @Schema(description = "用印状态标签")
    private String sealStatusLabel;

    @Schema(description = "合同方向标签")
    private String directionLabel;

    @Schema(description = "合同类型标签")
    private String typeLabel;

    @Schema(description = "用印记录历史（按申请时间倒序）")
    private List<AdSealRecord> sealRecords;
}
