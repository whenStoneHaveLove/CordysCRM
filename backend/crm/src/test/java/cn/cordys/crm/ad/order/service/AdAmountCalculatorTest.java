package cn.cordys.crm.ad.order.service;

import cn.cordys.crm.ad.common.constants.OrderStateMachine;
import cn.cordys.crm.ad.common.constants.PaymentMethod;
import cn.cordys.crm.ad.common.constants.ReceiptMethod;
import cn.cordys.crm.ad.order.domain.AdOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 纯逻辑单测（NO @SpringBootTest）：验证 M2 金额计算与 L-05 财务前置矩阵。
 *
 * <p>覆盖：L-02 应收=总额-返点；L-11 预收(基数=应收)；L-28 媒体预付(基数=媒体应付)；
 * L-05 依据收款/付款方式推导目标状态。</p>
 */
class AdAmountCalculatorTest {

    private final AdAmountCalculator calculator = new AdAmountCalculator();

    /** 忽略 scale 差异的金额相等断言。 */
    private void assertAmount(String msg, BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), msg);
    }

    private AdOrder baseOrder() {
        AdOrder o = new AdOrder();
        o.setTotalAmount(new BigDecimal("10000"));
        o.setNoRebateAmount(BigDecimal.ZERO);
        return o;
    }

    /* ---------------- L-02 返点 / 应收 ---------------- */

    @Test
    void l02_rebateRatioMode() {
        AdOrder o = baseOrder();
        o.setRebateMode(10); // 比例
        o.setRebateValue(new BigDecimal("10")); // 10%
        calculator.computeAmounts(o);
        assertAmount("返点=10000*10%", new BigDecimal("1000.00"), o.getRebateAmount());
        assertAmount("应收=10000-1000", new BigDecimal("9000.00"), o.getReceivableAmount());
    }

    @Test
    void l02_rebateFixedMode() {
        AdOrder o = baseOrder();
        o.setRebateMode(20); // 固定
        o.setRebateValue(new BigDecimal("500")); // 固定 500
        calculator.computeAmounts(o);
        assertAmount("返点=固定500", new BigDecimal("500.00"), o.getRebateAmount());
        assertAmount("应收=10000-500", new BigDecimal("9500.00"), o.getReceivableAmount());
    }

    @Test
    void l02_rebateExcludesNoRebateAmount() {
        AdOrder o = baseOrder();
        o.setNoRebateAmount(new BigDecimal("2000")); // 不记返 2000
        o.setRebateMode(10);
        o.setRebateValue(new BigDecimal("10"));
        calculator.computeAmounts(o);
        // 返点基数=10000-2000=8000 -> 800
        assertAmount("返点基数=8000", new BigDecimal("800.00"), o.getRebateAmount());
        assertAmount("应收=10000-800", new BigDecimal("9200.00"), o.getReceivableAmount());
    }

    @Test
    void l02_rebateModeNullYieldsZero() {
        AdOrder o = baseOrder();
        o.setRebateMode(null);
        o.setRebateValue(new BigDecimal("10"));
        calculator.computeAmounts(o);
        assertAmount("返点=0", BigDecimal.ZERO, o.getRebateAmount());
        assertAmount("应收=总额", new BigDecimal("10000.00"), o.getReceivableAmount());
    }

    @Test
    void l02_rebateRoundingHalfUp() {
        AdOrder o = new AdOrder();
        o.setTotalAmount(new BigDecimal("200"));
        o.setNoRebateAmount(BigDecimal.ZERO);
        o.setRebateMode(10);
        o.setRebateValue(new BigDecimal("3.333")); // 6.666 -> 四舍五入 6.67
        calculator.computeAmounts(o);
        assertAmount("返点四舍五入=6.67", new BigDecimal("6.67"), o.getRebateAmount());
        assertAmount("应收=200-6.67", new BigDecimal("193.33"), o.getReceivableAmount());
    }

    /* ---------------- L-11 预收（基数=应收） ---------------- */

    @Test
    void l11_receiptPrepayRatioOnReceivable() {
        AdOrder o = baseOrder();
        o.setRebateMode(10);
        o.setRebateValue(new BigDecimal("10")); // 应收=9000
        o.setReceiptPrepayMode(10);
        o.setReceiptPrepayRatio(new BigDecimal("30")); // 9000*30%=2700
        calculator.computeAmounts(o);
        assertAmount("预收=9000*30%", new BigDecimal("2700.00"), o.getReceiptPrepayAmount());
    }

    @Test
    void l11_receiptPrepayFixed() {
        AdOrder o = baseOrder();
        o.setRebateMode(10);
        o.setRebateValue(new BigDecimal("10"));
        o.setReceiptPrepayMode(20);
        o.setReceiptPrepayRatio(new BigDecimal("1000")); // 固定 1000
        calculator.computeAmounts(o);
        assertAmount("预收=固定1000", new BigDecimal("1000.00"), o.getReceiptPrepayAmount());
    }

    @Test
    void l11_receiptPrepayModeNullYieldsZero() {
        AdOrder o = baseOrder();
        o.setReceiptPrepayMode(null);
        o.setReceiptPrepayRatio(new BigDecimal("30"));
        calculator.computeAmounts(o);
        assertAmount("预收=0", BigDecimal.ZERO, o.getReceiptPrepayAmount());
    }

    /* ---------------- L-28 媒体预付（基数=媒体应付） ---------------- */

    @Test
    void l28_mediaPrepayRatioOnPayable() {
        AdOrder o = baseOrder();
        o.setMediaPayableAmount(new BigDecimal("5000"));
        o.setPaymentPrepayMode(10);
        o.setPaymentPrepayRatio(new BigDecimal("20")); // 5000*20%=1000
        calculator.computeAmounts(o);
        assertAmount("媒体预付=5000*20%", new BigDecimal("1000.00"), o.getPaymentPrepayAmount());
    }

    @Test
    void l28_mediaPrepayFixed() {
        AdOrder o = baseOrder();
        o.setMediaPayableAmount(new BigDecimal("5000"));
        o.setPaymentPrepayMode(20);
        o.setPaymentPrepayRatio(new BigDecimal("800")); // 固定 800
        calculator.computeAmounts(o);
        assertAmount("媒体预付=固定800", new BigDecimal("800.00"), o.getPaymentPrepayAmount());
    }

    @Test
    void l28_mediaPrepayModeNullYieldsZero() {
        AdOrder o = baseOrder();
        o.setMediaPayableAmount(new BigDecimal("5000"));
        o.setPaymentPrepayMode(null);
        o.setPaymentPrepayRatio(new BigDecimal("20"));
        calculator.computeAmounts(o);
        assertAmount("媒体预付=0", BigDecimal.ZERO, o.getPaymentPrepayAmount());
    }

    /* ---------------- L-05 财务前置矩阵（目标状态推导） ---------------- */

    @Test
    void l05_bothPrepayTargetsPendingPrepayConfirm() {
        AdOrder o = baseOrder();
        o.setReceiptMethod(ReceiptMethod.PREPAY.getCode());      // 10 预收
        o.setPaymentMethod(PaymentMethod.PREPAY_MEDIA.getCode()); // 10 媒体预付
        calculator.computeAmounts(o);
        var plan = calculator.buildFinancialPlan(o);
        assertEquals(OrderStateMachine.PENDING_PREPAY_CONFIRM, plan.getToStatus(), "双预付→30");
    }

    @Test
    void l05_onlyMediaPrepayTargetsPendingMediaPrepay() {
        AdOrder o = baseOrder();
        o.setReceiptMethod(null);
        o.setPaymentMethod(PaymentMethod.PREPAY_MEDIA.getCode()); // 10 媒体预付
        calculator.computeAmounts(o);
        var plan = calculator.buildFinancialPlan(o);
        assertEquals(OrderStateMachine.PENDING_MEDIA_PREPAY, plan.getToStatus(), "仅媒体预付→40");
    }

    @Test
    void l05_noPrepayTargetsExecuting() {
        AdOrder o = baseOrder();
        o.setReceiptMethod(null);
        o.setPaymentMethod(PaymentMethod.POSTPAY_MEDIA.getCode()); // 20 后付
        calculator.computeAmounts(o);
        var plan = calculator.buildFinancialPlan(o);
        assertEquals(OrderStateMachine.EXECUTING, plan.getToStatus(), "无预付→50");
    }
}
