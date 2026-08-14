package cn.cordys.crm.ad.receipt.constants;

import lombok.Getter;

/**
 * 收款单状态枚举。
 */
@Getter
public enum ReceiptStatus {
    DRAFT(0, "草稿"),
    PENDING_APPROVAL(10, "待审核"),
    APPROVED(20, "审核通过"),
    REJECTED(30, "驳回");

    private final int code;
    private final String label;

    ReceiptStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReceiptStatus of(Integer code) {
        if (code == null) return null;
        for (ReceiptStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }

    public static String labelOf(Integer code) {
        ReceiptStatus s = of(code);
        return s == null ? null : s.getLabel();
    }
}
