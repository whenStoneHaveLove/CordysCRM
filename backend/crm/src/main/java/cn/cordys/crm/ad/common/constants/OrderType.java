package cn.cordys.crm.ad.common.constants;

import lombok.Getter;

/**
 * 订单类型枚举（V3.1 §13.4）。
 */
@Getter
public enum OrderType {
    FRAMEWORK(10, "框架合同"),
    SINGLE(20, "单笔合同");

    private final int code;
    private final String label;

    OrderType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrderType of(int code) {
        for (OrderType e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
