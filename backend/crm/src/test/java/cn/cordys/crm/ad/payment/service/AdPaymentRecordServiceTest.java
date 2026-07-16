package cn.cordys.crm.ad.payment.service;

import cn.cordys.crm.ad.common.constants.MediaPaymentStatus;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.payment.constants.PaymentDirection;
import cn.cordys.crm.ad.payment.constants.PaymentType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 收付款金额核销纯逻辑单测（NO @SpringBootTest / NO DB / NO Mockito）：经反射调用私有核心方法
 * {@code applyFinanceEffect(order, direction, type, amount, invoiceOnly, receiptOnly)}，校验
 * confirmPrepay/mediaPrepay/invoice/receive/cancel 的累计与进度推导是否正确
 * （覆盖 L-05 后付媒体、L-11 预付基数=应收、L-28 media_payable_amount 口径）。
 *
 * <p>applyFinanceEffect 仅修改传入的 AdOrder 对象（nvl / computeProgress / MediaPaymentStatus.compute），
 * 不触碰任何 mapper，故无需 Spring、亦无需打桩即可纯单测核销数学。这与 AdOrderChangeServiceTest
 * 的「反射私有纯逻辑方法」思路一致，但本方法无需 mockStatic（无静态依赖）。</p>
 */
class AdPaymentRecordServiceTest {

    private final AdPaymentRecordService service = new AdPaymentRecordService();

    private void apply(AdOrder o, int direction, int type, BigDecimal amount,
                       boolean invoiceOnly, boolean receiptOnly) throws Exception {
        Method m = AdPaymentRecordService.class.getDeclaredMethod(
                "applyFinanceEffect", AdOrder.class, int.class, int.class,
                BigDecimal.class, boolean.class, boolean.class);
        m.setAccessible(true);
        m.invoke(service, o, direction, type, amount, invoiceOnly, receiptOnly);
    }

    private void assertAmount(String msg, BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual == null ? BigDecimal.ZERO : actual), msg);
    }

    // ---------- L-11 确认预收款：UPSTREAM + PRE_RECEIPT(10) → received += amount ----------

    @Test
    void l11_confirmPrepay_addsReceived() throws Exception {
        AdOrder o = order(received("0"), receivable("10000"), mediaPayable("5000"));
        apply(o, PaymentDirection.UPSTREAM.getCode(), PaymentType.PRE_RECEIPT.getCode(),
                new BigDecimal("3000"), false, false);
        assertAmount("received=3000", new BigDecimal("3000"), o.getReceivedAmount());
        assertEquals(MediaPaymentStatus.PARTIAL.getCode(), o.getReceiptStatus());   // 3000<10000 部分收
        assertAmount("invoiced unchanged", BigDecimal.ZERO, o.getInvoicedAmount());
        assertAmount("mediaPaid unchanged", BigDecimal.ZERO, o.getMediaPaidAmount());
    }

    // ---------- L-02 开票：UPSTREAM + INVOICE_RECEIPT(30, invoiceOnly) → invoiced only ----------

    @Test
    void invoice_onlyAddsInvoiced() throws Exception {
        AdOrder o = order(received("0"), receivable("10000"), invoiced("0"));
        apply(o, PaymentDirection.UPSTREAM.getCode(), PaymentType.INVOICE_RECEIPT.getCode(),
                new BigDecimal("5000"), true, false);
        assertAmount("invoiced=5000", new BigDecimal("5000"), o.getInvoicedAmount());
        assertEquals(MediaPaymentStatus.PARTIAL.getCode(), o.getInvoiceStatus());
        assertAmount("received unchanged", BigDecimal.ZERO, o.getReceivedAmount());
    }

    // ---------- 开票+收款：UPSTREAM + INVOICE_RECEIPT(30, 双向) → 同时累加 ----------

    @Test
    void invoiceAndReceipt_bothIncrease() throws Exception {
        AdOrder o = order(received("0"), receivable("10000"), invoiced("0"));
        apply(o, PaymentDirection.UPSTREAM.getCode(), PaymentType.INVOICE_RECEIPT.getCode(),
                new BigDecimal("5000"), false, false);
        assertAmount("received=5000", new BigDecimal("5000"), o.getReceivedAmount());
        assertAmount("invoiced=5000", new BigDecimal("5000"), o.getInvoicedAmount());
        assertEquals(MediaPaymentStatus.PARTIAL.getCode(), o.getReceiptStatus());
        assertEquals(MediaPaymentStatus.PARTIAL.getCode(), o.getInvoiceStatus());
    }

    // ---------- 收款登记：UPSTREAM + INVOICE_RECEIPT(30, receiptOnly) → received only ----------

    @Test
    void receive_onlyAddsReceived() throws Exception {
        AdOrder o = order(received("0"), receivable("10000"), invoiced("0"));
        apply(o, PaymentDirection.UPSTREAM.getCode(), PaymentType.INVOICE_RECEIPT.getCode(),
                new BigDecimal("4000"), false, true);
        assertAmount("received=4000", new BigDecimal("4000"), o.getReceivedAmount());
        assertAmount("invoiced unchanged", BigDecimal.ZERO, o.getInvoicedAmount());
        assertEquals(MediaPaymentStatus.PARTIAL.getCode(), o.getReceiptStatus());
        assertEquals(MediaPaymentStatus.NONE.getCode(), o.getInvoiceStatus());
    }

    // ---------- L-28 付媒体预付款：DOWNSTREAM + PRE_PAY(20) → mediaPaid += amount ----------

    @Test
    void l28_mediaPrepay_addsMediaPaid_andStatus() throws Exception {
        AdOrder o = order(mediaPaid("0"), mediaPayable("5000"), receivable("10000"));
        apply(o, PaymentDirection.DOWNSTREAM.getCode(), PaymentType.PRE_PAY.getCode(),
                new BigDecimal("5000"), false, false);
        assertAmount("mediaPaid=5000", new BigDecimal("5000"), o.getMediaPaidAmount());
        assertEquals(MediaPaymentStatus.PAID.getCode(), o.getMediaPaymentStatus());   // 全额付清
        assertAmount("received unchanged", BigDecimal.ZERO, o.getReceivedAmount());
    }

    // ---------- L-05/L-28 媒体尾款部分付：DOWNSTREAM + MEDIA_POSTPAY(40) → mediaPaid += amount ----------

    @Test
    void l05_mediaPostpay_partial() throws Exception {
        AdOrder o = order(mediaPaid("2000"), mediaPayable("5000"), receivable("10000"));
        apply(o, PaymentDirection.DOWNSTREAM.getCode(), PaymentType.MEDIA_POSTPAY.getCode(),
                new BigDecimal("2000"), false, false);
        assertAmount("mediaPaid=4000", new BigDecimal("4000"), o.getMediaPaidAmount());
        assertEquals(MediaPaymentStatus.PARTIAL.getCode(), o.getMediaPaymentStatus());  // 4000<5000 部分付
    }

    // ---------- L-28 边界：mediaPaid 恰好 == mediaPayable → PAID(20) ----------

    @Test
    void l28_mediaPaidEqualsPayable_isPaid() throws Exception {
        AdOrder o = order(mediaPaid("4000"), mediaPayable("5000"), receivable("10000"));
        apply(o, PaymentDirection.DOWNSTREAM.getCode(), PaymentType.PRE_PAY.getCode(),
                new BigDecimal("1000"), false, false);
        assertAmount("mediaPaid=5000", new BigDecimal("5000"), o.getMediaPaidAmount());
        assertEquals(MediaPaymentStatus.PAID.getCode(), o.getMediaPaymentStatus());
    }

    // ---------- payable=0 边界：不应除零（无应付但已付>0 → PAID） ----------

    @Test
    void payableZero_paidPositive_isPaid_noDivByZero() throws Exception {
        AdOrder o = order(mediaPaid("0"), mediaPayable("0"), receivable("10000"));
        apply(o, PaymentDirection.DOWNSTREAM.getCode(), PaymentType.PRE_PAY.getCode(),
                new BigDecimal("1000"), false, false);
        assertAmount("mediaPaid=1000", new BigDecimal("1000"), o.getMediaPaidAmount());
        assertEquals(MediaPaymentStatus.PAID.getCode(), o.getMediaPaymentStatus());
    }

    // ---------- 退款：UPSTREAM + REFUND(50) → received -= amount, 不为负 ----------

    @Test
    void refund_subtractsReceived() throws Exception {
        AdOrder o = order(received("8000"), receivable("10000"));
        apply(o, PaymentDirection.UPSTREAM.getCode(), PaymentType.REFUND.getCode(),
                new BigDecimal("3000"), false, false);
        assertAmount("received=5000", new BigDecimal("5000"), o.getReceivedAmount());
    }

    @Test
    void refund_floorsAtZero() throws Exception {
        AdOrder o = order(received("3000"), receivable("10000"));
        apply(o, PaymentDirection.UPSTREAM.getCode(), PaymentType.REFUND.getCode(),
                new BigDecimal("5000"), false, false);
        assertAmount("received floored to 0", BigDecimal.ZERO, o.getReceivedAmount());
    }

    // ---------- 坏账：DOWNSTREAM + BAD_DEBT(60) → badDebt += amount ----------

    @Test
    void badDebt_addsBadDebtAmount() throws Exception {
        AdOrder o = order(badDebt("0"));
        apply(o, PaymentDirection.DOWNSTREAM.getCode(), PaymentType.BAD_DEBT.getCode(),
                new BigDecimal("1000"), false, false);
        assertAmount("badDebt=1000", new BigDecimal("1000"), o.getBadDebtAmount());
    }

    // ---------- 撤销冲销（cancel）：传入金额取负 → received -= amount ----------

    @Test
    void cancel_prepay_reversesReceived() throws Exception {
        AdOrder o = order(received("8000"), receivable("10000"));
        // cancel 内部以 rec.getAmount().negate() 调用：这里直接传负值模拟反向核销
        apply(o, PaymentDirection.UPSTREAM.getCode(), PaymentType.PRE_RECEIPT.getCode(),
                new BigDecimal("-3000"), false, false);
        assertAmount("received=5000 (reversed)", new BigDecimal("5000"), o.getReceivedAmount());
    }

    // ---------- 测试辅助 ----------

    @FunctionalInterface
    private interface FieldSetter {
        void set(AdOrder o);
    }

    private AdOrder order(FieldSetter... setters) {
        AdOrder o = new AdOrder();
        for (FieldSetter s : setters) {
            s.set(o);
        }
        return o;
    }

    private FieldSetter received(String v) {
        return o -> o.setReceivedAmount(new BigDecimal(v));
    }

    private FieldSetter invoiced(String v) {
        return o -> o.setInvoicedAmount(new BigDecimal(v));
    }

    private FieldSetter mediaPaid(String v) {
        return o -> o.setMediaPaidAmount(new BigDecimal(v));
    }

    private FieldSetter mediaPayable(String v) {
        return o -> o.setMediaPayableAmount(new BigDecimal(v));
    }

    private FieldSetter receivable(String v) {
        return o -> o.setReceivableAmount(new BigDecimal(v));
    }

    private FieldSetter badDebt(String v) {
        return o -> o.setBadDebtAmount(new BigDecimal(v));
    }
}
