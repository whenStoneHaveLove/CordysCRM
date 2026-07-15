package cn.cordys.crm.ad.common.constants;

import lombok.Getter;

/**
 * 付款方式枚举（V3.1 §13.4）。
 */
@Getter
public enum PaymentMethod {
    PREPAY_MEDIA(10, "预付媒体"),
    POSTPAY_MEDIA(20, "后付媒体");

    private final int code;
    private final String label;

    PaymentMethod(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PaymentMethod of(int code) {
        for (PaymentMethod e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
