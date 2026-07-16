package cn.cordys.crm.ad.contract.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 合同方向（V3.1 §5.2.3 / PRD 5.2.3，L-10）。
 * <ul>
 *   <li>10 = 上游</li>
 *   <li>20 = 下游</li>
 * </ul>
 */
public enum ContractDirection {
    UPSTREAM(10, "上游"),
    DOWNSTREAM(20, "下游");

    private final int code;
    private final String label;

    ContractDirection(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, ContractDirection> MAP = new HashMap<>();

    static {
        for (ContractDirection e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static ContractDirection of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        ContractDirection e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
