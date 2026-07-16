package cn.cordys.crm.ad.payment.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 纯逻辑单测（NO Spring）：锁定收付款方向 {@link PaymentDirection} 与类型 {@link PaymentType} 的
 * code/label 契约（V3.1 §13.4；类型 50=退款/60=坏账，详见 L-10/L-28）。
 *
 * <p>注意：本枚举 of() 对未知/空值返回 null（与 MediaPaymentStatus 返回 NONE 不同），故 labelOf
 * 对未知 code 返回字面量、对 null 返回空串，这里一并锁定。</p>
 */
class PaymentEnumTest {

    @Test
    void paymentDirection_codesAndLabels() {
        assertEquals(10, PaymentDirection.UPSTREAM.getCode());
        assertEquals("上游收款", PaymentDirection.UPSTREAM.getLabel());
        assertEquals(20, PaymentDirection.DOWNSTREAM.getCode());
        assertEquals("下游付款", PaymentDirection.DOWNSTREAM.getLabel());
    }

    @Test
    void paymentDirection_ofAndLabelOf() {
        assertEquals(PaymentDirection.UPSTREAM, PaymentDirection.of(10));
        assertEquals(PaymentDirection.DOWNSTREAM, PaymentDirection.of(20));
        assertNull(PaymentDirection.of(99));
        assertNull(PaymentDirection.of(null));
        assertEquals("上游收款", PaymentDirection.labelOf(10));
        assertEquals("99", PaymentDirection.labelOf(99));
        assertEquals("", PaymentDirection.labelOf(null));
    }

    @Test
    void paymentType_allSixCodesAndLabels() {
        assertEquals(10, PaymentType.PRE_RECEIPT.getCode());
        assertEquals("预收", PaymentType.PRE_RECEIPT.getLabel());
        assertEquals(20, PaymentType.PRE_PAY.getCode());
        assertEquals("预付", PaymentType.PRE_PAY.getLabel());
        assertEquals(30, PaymentType.INVOICE_RECEIPT.getCode());
        assertEquals("开票收款", PaymentType.INVOICE_RECEIPT.getLabel());
        assertEquals(40, PaymentType.MEDIA_POSTPAY.getCode());
        assertEquals("媒体尾款", PaymentType.MEDIA_POSTPAY.getLabel());
        assertEquals(50, PaymentType.REFUND.getCode());
        assertEquals("退款", PaymentType.REFUND.getLabel());
        assertEquals(60, PaymentType.BAD_DEBT.getCode());
        assertEquals("坏账", PaymentType.BAD_DEBT.getLabel());
    }

    @Test
    void paymentType_refundAndBadDebtSemantics() {
        // L-28/L-10：退款(50)/坏账(60) 以明细行表达退票/坏账，而非状态值
        assertEquals(PaymentType.REFUND, PaymentType.of(50));
        assertEquals(PaymentType.BAD_DEBT, PaymentType.of(60));
        assertEquals("退款", PaymentType.labelOf(50));
        assertEquals("坏账", PaymentType.labelOf(60));
    }

    @Test
    void paymentType_ofAndLabelOf_unknown() {
        assertNull(PaymentType.of(99));
        assertNull(PaymentType.of(null));
        assertEquals("99", PaymentType.labelOf(99));
        assertEquals("", PaymentType.labelOf(null));
    }
}
