package cn.cordys.crm.ad.payout.constants;

import lombok.Getter;

/**
 * 付款单状态枚举。
 *
 * <p>V3.1.1 流程调整：去掉「驳回」中间态，驳回直接回到草稿。
 * 「审核通过」即「待付款」，由「付款」动作回写订单金额并置为「已付款」。</p>
 */
@Getter
public enum PayoutStatus {
    DRAFT(0, "草稿"),
    PENDING_APPROVAL(10, "待审核"),
    /** 审批通过后状态——待付款（不直接改订单，由「付款」动作回写订单）。 */
    PENDING_PAYMENT(20, "待付款"),
    PAID(30, "已付款");

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