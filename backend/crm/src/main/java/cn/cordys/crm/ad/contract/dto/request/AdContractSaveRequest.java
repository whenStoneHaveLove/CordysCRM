package cn.cordys.crm.ad.contract.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 合同保存请求（M5，创建/编辑共用；POST /api/ad/contract 创建，PUT /api/ad/contract 编辑）。
 *
 * <p>创建时 {@code id} 留空，由服务生成；编辑时必须携带 {@code id}。
 * 合同编号 {@code contractNo} 留空时由服务按规则自动生成（CN{yyyyMMdd}-{3位流水}）。</p>
 */
@Data
public class AdContractSaveRequest {

    @Schema(description = "合同id（编辑时必填，创建时留空）")
    private String id;

    @Schema(description = "合同编号（留空则由系统生成）")
    private String contractNo;

    @Schema(description = "合同名称")
    private String contractName;

    @Schema(description = "业务主体id")
    private String businessEntityId;

    @Schema(description = "合同方向:10上游/20下游")
    private Integer contractDirection;

    @Schema(description = "合同类型:10框架/20单笔")
    private Integer contractType;

    @Schema(description = "关联方id")
    private String relatedPartyId;

    @Schema(description = "关联方类型:10客户/20上游代理/30下游客户")
    private Integer relatedPartyType;

    @Schema(description = "关联订单id列表(统一走 ad_order_contract 中间表，支持一对多)")
    private List<String> orderIds;

    @Schema(description = "关联变更单id(可选)")
    private String changeOrderId;

    @Schema(description = "签约主体")
    private String signingEntity;

    @Schema(description = "有效期起")
    private Date validFrom;

    @Schema(description = "有效期止")
    private Date validTo;

    @Schema(description = "合同金额")
    private BigDecimal amount;

    @Schema(description = "返点条款")
    private String rebateTerms;

    @Schema(description = "合同文件(单笔用印前可空,L-07)")
    private String fileUrl;
}
