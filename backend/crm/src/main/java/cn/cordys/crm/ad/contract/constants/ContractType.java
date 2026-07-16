package cn.cordys.crm.ad.contract.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 合同类型（V3.1 §5.2.3 / PRD 5.2.3，L-10）。
 * <ul>
 *   <li>10 = 框架（多订单关联，走 ad_order_contract 中间表）</li>
 *   <li>20 = 单笔（直接关联订单 order_id）</li>
 * </ul>
 */
public enum ContractType {
    FRAMEWORK(10, "框架"),
    SINGLE(20, "单笔");

    private final int code;
    private final String label;

    ContractType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, ContractType> MAP = new HashMap<>();

    static {
        for (ContractType e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static ContractType of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        ContractType e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
