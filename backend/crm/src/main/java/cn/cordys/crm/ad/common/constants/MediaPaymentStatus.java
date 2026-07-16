package cn.cordys.crm.ad.common.constants;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 媒体付款进度枚举（V3.1 §6.3/§13.4，0未付/10部分/20全额）。
 *
 * <p>对应 {@code ad_order.media_payment_status} 字段，与 {@link InvoiceStatus} 同源的 0/10/20 进度语义。
 * 退票/退款不作为状态值，而是以 {@code ad_payment_record.type=50(退款)/60(坏账)} 明细行表达
 * （见 {@code cn.cordys.crm.ad.payment.constants.PaymentType}）。</p>
 *
 * <p>状态推导：{@link #compute(BigDecimal, BigDecimal)} 由「累计已付」与「媒体应付」计算 0/10/20，
 * 与上游开票/收款进度推导共用同一数学口径（见 {@code AdPaymentRecordService}）。</p>
 */
@Getter
public enum MediaPaymentStatus {

    NONE(0, "待付"),
    PARTIAL(10, "部分付"),
    PAID(20, "已付");

    private final int code;
    private final String label;

    MediaPaymentStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MediaPaymentStatus of(Integer code) {
        if (code == null) {
            return NONE;
        }
        for (MediaPaymentStatus e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return NONE;
    }

    public static String labelOf(Integer code) {
        return of(code).label;
    }

    /**
     * 由累计已付/媒体应付推导状态（0未付/10部分付/20全额付）。
     */
    public static int compute(BigDecimal paid, BigDecimal payable) {
        BigDecimal p = paid == null ? BigDecimal.ZERO : paid;
        BigDecimal t = payable == null ? BigDecimal.ZERO : payable;
        if (t.compareTo(BigDecimal.ZERO) <= 0) {
            return p.compareTo(BigDecimal.ZERO) > 0 ? PAID.getCode() : NONE.getCode();
        }
        if (p.compareTo(BigDecimal.ZERO) <= 0) {
            return NONE.getCode();
        }
        if (p.compareTo(t) >= 0) {
            return PAID.getCode();
        }
        return PARTIAL.getCode();
    }
}
