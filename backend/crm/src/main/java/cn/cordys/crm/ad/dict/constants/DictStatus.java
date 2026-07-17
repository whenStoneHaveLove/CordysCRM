package cn.cordys.crm.ad.dict.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告字典状态枚举（V3.1 §5.3 / §4.3）。
 * <ul>
 *   <li>10 = ENABLED - 启用</li>
 *   <li>20 = DISABLED - 停用</li>
 * </ul>
 */
public enum DictStatus {
    ENABLED(10, "启用"),
    DISABLED(20, "停用");

    private final int code;
    private final String label;

    DictStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, DictStatus> MAP = new HashMap<>();

    static {
        for (DictStatus e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static DictStatus of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        DictStatus e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
