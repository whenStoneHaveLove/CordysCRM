package cn.cordys.crm.ad.support.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 工单类型枚举（M6，V3.1 广告支持工单）。
 * <ul>
 *   <li>10 = ORDER_ISSUE - 订单问题</li>
 *   <li>20 = PAYMENT_ISSUE - 付款问题</li>
 *   <li>30 = CONTRACT_ISSUE - 合同问题</li>
 *   <li>40 = MATERIAL_ISSUE - 素材问题</li>
 *   <li>50 = OTHER - 其他</li>
 * </ul>
 */
public enum TicketType {
    ORDER_ISSUE(10, "订单问题"),
    PAYMENT_ISSUE(20, "付款问题"),
    CONTRACT_ISSUE(30, "合同问题"),
    MATERIAL_ISSUE(40, "素材问题"),
    OTHER(50, "其他");

    private final int code;
    private final String label;

    TicketType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, TicketType> MAP = new HashMap<>();

    static {
        for (TicketType e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static TicketType of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        TicketType e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
