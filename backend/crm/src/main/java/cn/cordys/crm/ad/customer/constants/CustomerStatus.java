package cn.cordys.crm.ad.customer.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户状态枚举（M6，V3.1 广告客户管理）。
 * <ul>
 *   <li>0 = ACTIVE - 活跃</li>
 *   <li>10 = INACTIVE - 非活跃</li>
 *   <li>20 = BLACKLIST - 黑名单</li>
 * </ul>
 */
public enum CustomerStatus {
    ACTIVE(0, "活跃"),
    INACTIVE(10, "非活跃"),
    BLACKLIST(20, "黑名单");

    private final int code;
    private final String label;

    CustomerStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, CustomerStatus> MAP = new HashMap<>();

    static {
        for (CustomerStatus e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static CustomerStatus of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        CustomerStatus e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
