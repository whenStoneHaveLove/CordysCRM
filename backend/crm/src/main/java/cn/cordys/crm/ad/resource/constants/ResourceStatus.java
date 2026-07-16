package cn.cordys.crm.ad.resource.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告资源状态枚举（M6，V3.1 广告资源管理）。
 * <ul>
 *   <li>0 = AVAILABLE - 可用</li>
 *   <li>10 = OCCUPIED - 已占用</li>
 *   <li>20 = MAINTENANCE - 维护中</li>
 * </ul>
 */
public enum ResourceStatus {
    AVAILABLE(0, "可用"),
    OCCUPIED(10, "已占用"),
    MAINTENANCE(20, "维护中");

    private final int code;
    private final String label;

    ResourceStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, ResourceStatus> MAP = new HashMap<>();

    static {
        for (ResourceStatus e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static ResourceStatus of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        ResourceStatus e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
