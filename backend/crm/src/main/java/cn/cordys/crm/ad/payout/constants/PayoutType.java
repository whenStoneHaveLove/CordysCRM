package cn.cordys.crm.ad.payout.constants;

import lombok.Getter;

/**
 * 付款单类型枚举（保留坏账作为特殊类型）。
 */
@Getter
public enum PayoutType {
    NORMAL(10, "普通付款"),
    BAD_DEBT(20, "坏账");

    private final int code;
    private final String label;

    PayoutType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PayoutType of(Integer code) {
        if (code == null) return null;
        for (PayoutType t : values()) {
            if (t.code == code) return t;
        }
        return null;
    }

    public static String labelOf(Integer code) {
        PayoutType t = of(code);
        return t == null ? null : t.getLabel();
    }
}
