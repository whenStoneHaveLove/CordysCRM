package cn.cordys.crm.ad.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 财务前置动作矩阵结果（M2 L-05，POST /financial-pre-action）。
 * 依据收款方式 + 付款方式推导下一步状态与所需财务步骤。
 */
@Data
public class AdOrderFinancialPlan {

    @Schema(description = "推导出的目标状态：30待确认预收款/40待付媒体预付款/50执行中")
    private Integer toStatus;

    @Schema(description = "预收金额(基数=应收,L-11)")
    private BigDecimal receiptPrepayAmount;

    @Schema(description = "媒体预付金额(基数=media_payable,L-28)")
    private BigDecimal paymentPrepayAmount;

    @Schema(description = "所需财务步骤")
    private List<FinancialStep> steps;

    @Data
    public static class FinancialStep {

        @Schema(description = "步骤动作:RECEIPT_PREPAY/MEDIA_PREPAY/RECEIPT_ACCOUNT_PERIOD/MEDIA_POSTPAY")
        private String action;

        @Schema(description = "步骤金额（预估）")
        private BigDecimal amount;

        @Schema(description = "步骤说明")
        private String description;

        public FinancialStep() {
        }

        public FinancialStep(String action, BigDecimal amount) {
            this.action = action;
            this.amount = amount;
        }

        public FinancialStep(String action, BigDecimal amount, String description) {
            this.action = action;
            this.amount = amount;
            this.description = description;
        }
    }
}
