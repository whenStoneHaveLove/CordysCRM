package cn.cordys.crm.ad.common.constants;

import lombok.Getter;

/**
 * 媒体后付触发枚举（V3.1 §13.4）。
 */
@Getter
public enum PaymentPostpayTrigger {
    ON_UPSTREAM_FULL_PAID(10, "收到上游全款后"),
    ON_EXECUTION_COMPLETED_DAYS(20, "执行完成X天后");

    private final int code;
    private final String label;

    PaymentPostpayTrigger(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PaymentPostpayTrigger of(int code) {
        for (PaymentPostpayTrigger e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
