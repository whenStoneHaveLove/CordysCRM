package cn.cordys.crm.ad.common.constants;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 纯逻辑单测（NO Spring）：锁定付款进度枚举 {@link MediaPaymentStatus} 的状态推导契约
 * （V3.1 §6.3/§13.4，对应 L-28 「media_paid_amount >= media_payable_amount → 全额付(20)」口径，
 * 且无应付金额/已付为零时不触发除零）。
 */
class MediaPaymentStatusTest {

    // ---------- compute(paid, payable) 边界 ----------

    @Test
    void compute_paidZero_isNone() {
        assertEquals(MediaPaymentStatus.NONE.getCode(),
                MediaPaymentStatus.compute(BigDecimal.ZERO, new BigDecimal("100")));
    }

    @Test
    void compute_paidLessThanPayable_isPartial() {
        assertEquals(MediaPaymentStatus.PARTIAL.getCode(),
                MediaPaymentStatus.compute(new BigDecimal("50"), new BigDecimal("100")));
    }

    @Test
    void compute_paidEqualsPayable_isPaid() {
        assertEquals(MediaPaymentStatus.PAID.getCode(),
                MediaPaymentStatus.compute(new BigDecimal("100"), new BigDecimal("100")));
    }

    @Test
    void compute_paidGreaterThanPayable_isPaid() {
        assertEquals(MediaPaymentStatus.PAID.getCode(),
                MediaPaymentStatus.compute(new BigDecimal("150"), new BigDecimal("100")));
    }

    @Test
    void compute_payableZero_paidZero_isNone() {
        assertEquals(MediaPaymentStatus.NONE.getCode(),
                MediaPaymentStatus.compute(BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void compute_payableZero_paidPositive_isPaid_noDivByZero() {
        assertEquals(MediaPaymentStatus.PAID.getCode(),
                MediaPaymentStatus.compute(new BigDecimal("100"), BigDecimal.ZERO));
    }

    @Test
    void compute_nullPaid_treatedAsZero() {
        assertEquals(MediaPaymentStatus.NONE.getCode(),
                MediaPaymentStatus.compute(null, new BigDecimal("100")));
    }

    @Test
    void compute_nullPayable_paidPositive_isPaid() {
        assertEquals(MediaPaymentStatus.PAID.getCode(),
                MediaPaymentStatus.compute(new BigDecimal("50"), null));
    }

    @Test
    void compute_bothNull_isNone() {
        assertEquals(MediaPaymentStatus.NONE.getCode(),
                MediaPaymentStatus.compute(null, null));
    }

    // ---------- of / labelOf 反查 ----------

    @Test
    void of_reverseLookup() {
        assertEquals(MediaPaymentStatus.NONE, MediaPaymentStatus.of(0));
        assertEquals(MediaPaymentStatus.PARTIAL, MediaPaymentStatus.of(10));
        assertEquals(MediaPaymentStatus.PAID, MediaPaymentStatus.of(20));
    }

    @Test
    void of_null_returnsNone() {
        assertEquals(MediaPaymentStatus.NONE, MediaPaymentStatus.of(null));
    }

    @Test
    void of_unknown_returnsNone() {
        assertEquals(MediaPaymentStatus.NONE, MediaPaymentStatus.of(99));
    }

    @Test
    void labelOf_known() {
        assertEquals("待付", MediaPaymentStatus.labelOf(0));
        assertEquals("部分付", MediaPaymentStatus.labelOf(10));
        assertEquals("已付", MediaPaymentStatus.labelOf(20));
    }
}
