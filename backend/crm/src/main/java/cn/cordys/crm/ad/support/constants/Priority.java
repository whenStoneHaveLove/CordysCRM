package cn.cordys.crm.ad.support.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 工单优先级枚举（M6，V3.1 广告支持工单）。
 * <ul>
 *   <li>10 = LOW - 低</li>
 *   <li>20 = MEDIUM - 中</li>
 *   <li>30 = HIGH - 高</li>
 *   <li>40 = URGENT - 紧急</li>
 * </ul>
 */
public enum Priority {
    LOW(10, "低"),
    MEDIUM(20, "中"),
    HIGH(30, "高"),
    URGENT(40, "紧急");

    private final int code;
    private final String label;

    Priority(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, Priority> MAP = new HashMap<>();

    static {
        for (Priority e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static Priority of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        Priority e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
