package cn.cordys.crm.ad.businessentity.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务主体状态枚举（V3.1 §5.2.7 / §13.4）。
 * <ul>
 *   <li>10 = ENABLED - 启用</li>
 *   <li>20 = DISABLED - 停用</li>
 * </ul>
 */
public enum BusinessEntityStatus {
    ENABLED(10, "启用"),
    DISABLED(20, "停用");

    private final int code;
    private final String label;

    BusinessEntityStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, BusinessEntityStatus> MAP = new HashMap<>();

    static {
        for (BusinessEntityStatus e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static BusinessEntityStatus of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        BusinessEntityStatus e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
