package cn.cordys.crm.ad.order.service;

import cn.cordys.crm.ad.common.constants.PaymentMethod;
import cn.cordys.crm.ad.common.constants.PaymentPostpayTrigger;
import cn.cordys.crm.ad.common.constants.ReceiptMethod;
import cn.cordys.common.exception.GenericException;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.dto.response.AdOrderFinancialPlan;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 广告订单金额计算（M2，L-02/L-11/L-28）。
 *
 * <ul>
 *   <li>L-02 应收 = 总额 - 返点；返点 = 比例(应收基数×比例%) 或 固定。</li>
 *   <li>L-11 预收金额基数 = 应收；比例或固定（固定时比例字段存放固定额）。</li>
 *   <li>L-28 媒体预付金额基数 = 媒体应付；比例或固定。</li>
 * </ul>
 *
 * <p>同时提供 L-05 财务前置矩阵：依据收款方式 + 付款方式推导目标状态与所需财务步骤。</p>
 */
@Component
public class AdAmountCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /** 预收/预付 模式：10 比例 / 20 固定。 */
    private static final int MODE_RATIO = 10;
    private static final int MODE_FIXED = 20;

    /**
     * 原地计算并回填订单的金额字段（创建/提交/编辑草稿时调用）。
     */
    public void computeAmounts(AdOrder order) {
        BigDecimal total = nvl(order.getTotalAmount());
        BigDecimal noRebate = nvl(order.getNoRebateAmount());

        // L-02 返点（不记返部分不参与返点） — 比例不可超过100
        BigDecimal rebateBase = total.subtract(noRebate);
        BigDecimal rebateValue = nvl(order.getRebateValue());
        if (order.getRebateMode() != null && order.getRebateMode() == 10 && rebateValue.compareTo(new BigDecimal("100")) > 0) {
            throw new GenericException("返点比例不可超过 100%");
        }
        BigDecimal rebate = calcByMode(order.getRebateMode(), rebateBase, rebateValue);
        order.setRebateAmount(rebate);
        order.setReceivableAmount(total.subtract(rebate));

        // 媒体应付总额：前端未传则默认等于总额
        if (order.getMediaPayableAmount() == null) {
            order.setMediaPayableAmount(total);
        }

        // L-11 预收金额（基数=应收） — mode=10 用比例，mode=20 用固定金额
        BigDecimal receiptPrepay = order.getReceiptPrepayMode() != null && order.getReceiptPrepayMode() == 20
                ? nvl(order.getReceiptPrepayAmount())
                : calcByMode(order.getReceiptPrepayMode(), nvl(order.getReceivableAmount()), nvl(order.getReceiptPrepayRatio()));
        order.setReceiptPrepayAmount(receiptPrepay);

        // L-28 媒体预付金额（基数=媒体应付）
        BigDecimal mediaPrepay = order.getPaymentPrepayMode() != null && order.getPaymentPrepayMode() == 20
                ? nvl(order.getPaymentPrepayAmount())
                : calcByMode(order.getPaymentPrepayMode(), nvl(order.getMediaPayableAmount()), nvl(order.getPaymentPrepayRatio()));
        order.setPaymentPrepayAmount(mediaPrepay);
    }

    /**
     * L-05 财务前置矩阵：依据收款方式 + 付款方式推导目标状态与所需财务步骤。
     *
     * @param order 必须处于审核通过(20)状态
     * @return 财务计划（目标状态 + 步骤）
     */
    public AdOrderFinancialPlan buildFinancialPlan(AdOrder order) {
        boolean needReceiptPrepay = order.getReceiptMethod() != null
                && order.getReceiptMethod() == ReceiptMethod.PREPAY.getCode();
        boolean needMediaPrepay = order.getPaymentMethod() != null
                && order.getPaymentMethod() == PaymentMethod.PREPAY_MEDIA.getCode();

        List<AdOrderFinancialPlan.FinancialStep> steps = new ArrayList<>();

        if (needReceiptPrepay) {
            steps.add(new AdOrderFinancialPlan.FinancialStep("RECEIPT_PREPAY", nvl(order.getReceiptPrepayAmount())));
        }
        if (order.getPaymentMethod() != null && order.getPaymentMethod() == PaymentMethod.POSTPAY_MEDIA.getCode()) {
            Integer trigger = order.getPaymentPostpayTrigger();
            String desc;
            if (trigger != null && trigger == PaymentPostpayTrigger.ON_UPSTREAM_FULL_PAID.getCode()) {
                desc = "收到上游全款后付媒体尾款";
            } else {
                desc = "执行完成" + (order.getPaymentPostpayDays() == null ? "" : order.getPaymentPostpayDays() + "天") + "后付媒体尾款";
            }
            steps.add(new AdOrderFinancialPlan.FinancialStep("MEDIA_POSTPAY", BigDecimal.ZERO, desc));
        }
        if (needMediaPrepay) {
            steps.add(new AdOrderFinancialPlan.FinancialStep("MEDIA_PREPAY", nvl(order.getPaymentPrepayAmount())));
        }
        if (order.getReceiptMethod() != null && order.getReceiptMethod() == ReceiptMethod.ACCOUNT_PERIOD.getCode()) {
            steps.add(new AdOrderFinancialPlan.FinancialStep("RECEIPT_ACCOUNT_PERIOD", BigDecimal.ZERO,
                    "账期" + (order.getReceiptAccountPeriodDays() == null ? "" : order.getReceiptAccountPeriodDays() + "天") + "后收全款"));
        }

        int toStatus;
        if (needReceiptPrepay && needMediaPrepay) {
            toStatus = cn.cordys.crm.ad.common.constants.OrderStateMachine.PENDING_PREPAY_CONFIRM; // 30
        } else if (needMediaPrepay) {
            toStatus = cn.cordys.crm.ad.common.constants.OrderStateMachine.PENDING_MEDIA_PREPAY; // 40
        } else {
            toStatus = cn.cordys.crm.ad.common.constants.OrderStateMachine.EXECUTING; // 50
        }

        AdOrderFinancialPlan plan = new AdOrderFinancialPlan();
        plan.setToStatus(toStatus);
        plan.setReceiptPrepayAmount(nvl(order.getReceiptPrepayAmount()));
        plan.setPaymentPrepayAmount(nvl(order.getPaymentPrepayAmount()));
        plan.setSteps(steps);
        return plan;
    }

    /**
     * 按模式计算：10 比例（基数×值%）、20 固定（值即金额）。
     */
    private BigDecimal calcByMode(Integer mode, BigDecimal base, BigDecimal value) {
        if (mode == null) {
            return BigDecimal.ZERO;
        }
        if (mode == MODE_RATIO) {
            return base.multiply(nvl(value)).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        }
        if (mode == MODE_FIXED) {
            return nvl(value);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
