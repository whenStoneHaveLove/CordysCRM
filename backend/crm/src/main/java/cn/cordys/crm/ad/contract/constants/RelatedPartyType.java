package cn.cordys.crm.ad.contract.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 合同关联方类型（ad_contract.related_party_type，V3.1 §5.2.3 / PRD 5.2.3）。
 * <ul>
 *   <li>10 = 客户</li>
 *   <li>20 = 上游代理</li>
 *   <li>30 = 下游媒体</li>
 * </ul>
 */
public enum RelatedPartyType {
    CUSTOMER(10, "客户"),
    UPSTREAM_AGENT(20, "上游代理"),
    DOWNSTREAM_MEDIA(30, "下游媒体");

    private final int code;
    private final String label;

    RelatedPartyType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, RelatedPartyType> MAP = new HashMap<>();

    static {
        for (RelatedPartyType e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static RelatedPartyType of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        if (code == null) return null;
        RelatedPartyType e = of(code);
        return e == null ? String.valueOf(code) : e.getLabel();
    }
}
