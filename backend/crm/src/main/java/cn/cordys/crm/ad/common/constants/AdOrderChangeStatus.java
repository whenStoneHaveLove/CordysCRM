package cn.cordys.crm.ad.common.constants;

import lombok.Getter;

/**
 * 改单状态枚举（M3 T-20/T-21，V3.1 §6/§8）。
 *
 * <p>状态码采用 10 倍数间隔编码（与 {@link OrderStateMachine} 一致），标识改单生命周期：
 * 草稿(0) → 已提交/变更审核中(10) → 审批通过(20) → 已执行(40)；驳回(30) 为终态之一。
 * 与父订单联动：发起改单时父单切入改单审核中(60)并记录改单前状态(orderStatusBefore)，
 * 审批/执行/驳回后父单恢复为改单前状态。</p>
 */
@Getter
public enum AdOrderChangeStatus {

    DRAFT(0, "草稿"),
    SUBMITTED(10, "已提交"),
    APPROVED(20, "审批通过"),
    REJECTED(30, "已驳回"),
    EXECUTED(40, "已执行");

    private final int code;
    private final String label;

    AdOrderChangeStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 按状态码反查枚举。
     */
    public static AdOrderChangeStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (AdOrderChangeStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }

    /**
     * 状态中文标签；未知码回退为字符串。
     */
    public static String labelOf(Integer code) {
        AdOrderChangeStatus s = of(code);
        return s == null ? (code == null ? "" : String.valueOf(code)) : s.label;
    }
}
