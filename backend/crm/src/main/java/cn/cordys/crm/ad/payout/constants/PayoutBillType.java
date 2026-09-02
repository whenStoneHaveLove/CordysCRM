package cn.cordys.crm.ad.payout.constants;

import lombok.Getter;

/**
 * 付款单类型枚举（订单类型 / 非订单类型）。
 *
 * <p>订单类型：必须关联订单，下游客户明细由订单带出（可多客户）。<br>
 * 非订单类型：无关联订单，下游客户手动选择，仅支持一条明细，本次付款金额 = 付款金额。</p>
 */
@Getter
public enum PayoutBillType {
    ORDER(10, "订单类型"),
    NON_ORDER(20, "非订单类型");

    private final int code;
    private final String label;

    PayoutBillType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PayoutBillType of(Integer code) {
        if (code == null) return null;
        for (PayoutBillType t : values()) {
            if (t.code == code) return t;
        }
        return null;
    }

    /** 判空取默认：订单类型。 */
    public static PayoutBillType ofOrDefault(Integer code) {
        PayoutBillType t = of(code);
        return t == null ? ORDER : t;
    }

    public static String labelOf(Integer code) {
        PayoutBillType t = of(code);
        return t == null ? null : t.getLabel();
    }

    public boolean isOrder() {
        return this == ORDER;
    }
}
