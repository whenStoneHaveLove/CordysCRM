package cn.cordys.crm.ad.contract.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 合同用印状态（ad_contract.seal_status，V3.1 §5.2.3 / PRD 5.2.3，L-07）。
 * <ul>
 *   <li>0 = 未申请</li>
 *   <li>10 = 审批中</li>
 *   <li>20 = 已用印</li>
 *   <li>30 = 已驳回</li>
 * </ul>
 */
public enum SealStatus {
    NOT_APPLIED(0, "未申请"),
    APPROVING(10, "审批中"),
    SEALED(20, "已用印"),
    REJECTED(30, "已驳回");

    private final int code;
    private final String label;

    SealStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, SealStatus> MAP = new HashMap<>();

    static {
        for (SealStatus e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static SealStatus of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        SealStatus e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
