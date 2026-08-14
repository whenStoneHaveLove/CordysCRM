package cn.cordys.crm.ad.payout.constants;

import lombok.Getter;

/**
 * 付款单状态枚举。
 */
@Getter
public enum PayoutStatus {
    DRAFT(0, "草稿"),
    PENDING_APPROVAL(10, "待审核"),
    APPROVED(20, "审核通过"),
    REJECTED(30, "驳回");

    private final int code;
    private final String label;

    PayoutStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PayoutStatus of(Integer code) {
        if (code == null) return null;
        for (PayoutStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }

    public static String labelOf(Integer code) {
        PayoutStatus s = of(code);
        return s == null ? null : s.getLabel();
    }
}
