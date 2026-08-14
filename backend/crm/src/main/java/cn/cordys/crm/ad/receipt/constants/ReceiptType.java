package cn.cordys.crm.ad.receipt.constants;

import lombok.Getter;

/**
 * 收款单类型枚举（保留退款作为特殊类型）。
 */
@Getter
public enum ReceiptType {
    NORMAL(10, "普通收款"),
    REFUND(20, "退款");

    private final int code;
    private final String label;

    ReceiptType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReceiptType of(Integer code) {
        if (code == null) return null;
        for (ReceiptType t : values()) {
            if (t.code == code) return t;
        }
        return null;
    }

    public static String labelOf(Integer code) {
        ReceiptType t = of(code);
        return t == null ? null : t.getLabel();
    }
}
