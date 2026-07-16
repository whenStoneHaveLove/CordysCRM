package cn.cordys.crm.ad.support.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 工单状态枚举（M6，V3.1 广告支持工单）。
 * <ul>
 *   <li>0 = OPEN - 待处理</li>
 *   <li>10 = IN_PROGRESS - 处理中</li>
 *   <li>20 = RESOLVED - 已解决</li>
 *   <li>30 = CLOSED - 已关闭</li>
 * </ul>
 */
public enum TicketStatus {
    OPEN(0, "待处理"),
    IN_PROGRESS(10, "处理中"),
    RESOLVED(20, "已解决"),
    CLOSED(30, "已关闭");

    private final int code;
    private final String label;

    TicketStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, TicketStatus> MAP = new HashMap<>();

    static {
        for (TicketStatus e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static TicketStatus of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        TicketStatus e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
