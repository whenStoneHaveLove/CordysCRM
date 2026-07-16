package cn.cordys.crm.ad.payment.constants;

import lombok.Getter;

/**
 * 收付款类型枚举（V3.1 §13.4，10预收/20预付/30开票收款/40媒体尾款/50退款/60坏账）。
 */
@Getter
public enum PaymentType {

    PRE_RECEIPT(10, "预收"),
    PRE_PAY(20, "预付"),
    INVOICE_RECEIPT(30, "开票收款"),
    MEDIA_POSTPAY(40, "媒体尾款"),
    REFUND(50, "退款"),
    BAD_DEBT(60, "坏账");

    private final int code;
    private final String label;

    PaymentType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PaymentType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentType e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        PaymentType e = of(code);
        return e == null ? (code == null ? "" : String.valueOf(code)) : e.label;
    }
}
