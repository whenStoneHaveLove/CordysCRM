package cn.cordys.crm.ad.customer.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户等级枚举（M6，V3.1 广告客户管理）。
 * <ul>
 *   <li>10 = VIP</li>
 *   <li>20 = REGULAR - 普通</li>
 *   <li>30 = POTENTIAL - 潜力</li>
 * </ul>
 */
public enum CustomerLevel {
    VIP(10, "VIP"),
    REGULAR(20, "普通"),
    POTENTIAL(30, "潜力");

    private final int code;
    private final String label;

    CustomerLevel(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, CustomerLevel> MAP = new HashMap<>();

    static {
        for (CustomerLevel e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static CustomerLevel of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        CustomerLevel e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
