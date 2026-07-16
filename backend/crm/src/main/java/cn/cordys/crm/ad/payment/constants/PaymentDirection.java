package cn.cordys.crm.ad.payment.constants;

import lombok.Getter;

/**
 * 收付款方向枚举（V3.1 §13.4，10上游收款/20下游付款）。
 */
@Getter
public enum PaymentDirection {

    UPSTREAM(10, "上游收款"),
    DOWNSTREAM(20, "下游付款");

    private final int code;
    private final String label;

    PaymentDirection(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PaymentDirection of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentDirection e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        PaymentDirection e = of(code);
        return e == null ? (code == null ? "" : String.valueOf(code)) : e.label;
    }
}
