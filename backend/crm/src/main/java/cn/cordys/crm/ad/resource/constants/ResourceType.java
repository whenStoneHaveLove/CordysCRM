package cn.cordys.crm.ad.resource.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告资源类型枚举（M6，V3.1 广告资源管理）。
 * <ul>
 *   <li>10 = UPSTREAM_AGENT - 上游代理</li>
 *   <li>20 = DOWNSTREAM_MEDIA - 下游媒体</li>
 * </ul>
 */
public enum ResourceType {
    UPSTREAM_AGENT(10, "上游代理"),
    DOWNSTREAM_MEDIA(20, "下游媒体");

    private final int code;
    private final String label;

    ResourceType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, ResourceType> MAP = new HashMap<>();

    static {
        for (ResourceType e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static ResourceType of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        ResourceType e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
