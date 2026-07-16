package cn.cordys.crm.ad.seal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 上传盖章版合同请求（M5，POST /api/ad/seal/{contractId}/upload）。
 *
 * <p>单笔合同「先申请后盖章」流程（L-07）：媒介申请用印后线下盖章，再上传盖章版回填合同 file_url。</p>
 */
@Data
public class AdSealUploadRequest {

    @Schema(description = "盖章版合同文件地址")
    private String fileUrl;
}
