package cn.cordys.crm.ad.common.constants;

import lombok.Getter;

/**
 * 订单主状态枚举（10 倍数编码，V3.1 §6.1）。
 */
@Getter
public enum OrderStatus {
    DRAFT(OrderStateMachine.DRAFT, "草稿"),
    PENDING_BOSS_APPROVAL(OrderStateMachine.PENDING_BOSS_APPROVAL, "待老板审核"),
    APPROVED(OrderStateMachine.APPROVED, "审核通过"),
    PENDING_PREPAY_CONFIRM(OrderStateMachine.PENDING_PREPAY_CONFIRM, "待确认预收款"),
    PENDING_MEDIA_PREPAY(OrderStateMachine.PENDING_MEDIA_PREPAY, "待付媒体预付款"),
    EXECUTING(OrderStateMachine.EXECUTING, "执行中"),
    CHANGE_APPROVING(OrderStateMachine.CHANGE_APPROVING, "变更审核中"),
    EXECUTION_COMPLETED(OrderStateMachine.EXECUTION_COMPLETED, "执行完成"),
    SETTLEMENT(OrderStateMachine.SETTLEMENT, "结算中"),
    ARCHIVED(OrderStateMachine.ARCHIVED, "已归档"),
    VOIDED(OrderStateMachine.VOIDED, "已作废");

    private final int code;
    private final String label;

    OrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrderStatus of(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        if (code == null) return null;
        OrderStatus s = of(code);
        return s == null ? String.valueOf(code) : s.getLabel();
    }
}
