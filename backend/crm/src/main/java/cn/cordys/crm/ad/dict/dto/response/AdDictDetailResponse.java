package cn.cordys.crm.ad.dict.dto.response;

import cn.cordys.crm.ad.dict.domain.AdDict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告字典详情响应（V3.1 §4.3 GET /api/ad/dict/{id}）：字典主信息 + 状态标签。
 */
@Data
public class AdDictDetailResponse {

    @Schema(description = "字典主信息")
    private AdDict dict;

    @Schema(description = "状态标签")
    private String statusLabel;
}
