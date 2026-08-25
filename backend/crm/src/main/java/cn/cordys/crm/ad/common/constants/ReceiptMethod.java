package cn.cordys.crm.ad.common.constants;

import lombok.Getter;

/**
 * 收款方式枚举（V3.1 §13.4）。
 */
@Getter
public enum ReceiptMethod {
    PREPAY(10, "预收"),
    ACCOUNT_PERIOD(20, "账期");

    private final int code;
    private final String label;

    ReceiptMethod(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReceiptMethod of(int code) {
        for (ReceiptMethod e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        if (code == null) return null;
        ReceiptMethod e = of(code);
        return e == null ? String.valueOf(code) : e.getLabel();
    }
}
