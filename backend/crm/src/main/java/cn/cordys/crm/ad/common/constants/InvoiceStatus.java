package cn.cordys.crm.ad.common.constants;

import lombok.Getter;

/**
 * 资金进度枚举（开票/收款/媒体付款，0未/10部分/20全额，V3.1 §13.4）。
 */
@Getter
public enum InvoiceStatus {
    NONE(0, "未开"),
    PARTIAL(10, "部分"),
    FULL(20, "全额");

    private final int code;
    private final String label;

    InvoiceStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InvoiceStatus of(int code) {
        for (InvoiceStatus e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
