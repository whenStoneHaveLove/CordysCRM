package cn.cordys.crm.ad.resource.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告资源类型枚举（M6，V3.1 广告资源管理）。
 * <ul>
 *   <li>10 = ONLINE_MEDIA - 线上媒体</li>
 *   <li>20 = OFFLINE_BILLBOARD - 线下广告牌</li>
 *   <li>30 = TV - 电视</li>
 *   <li>40 = RADIO - 广播</li>
 *   <li>50 = PRINT - 印刷</li>
 * </ul>
 */
public enum ResourceType {
    ONLINE_MEDIA(10, "线上媒体"),
    OFFLINE_BILLBOARD(20, "线下广告牌"),
    TV(30, "电视"),
    RADIO(40, "广播"),
    PRINT(50, "印刷");

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
