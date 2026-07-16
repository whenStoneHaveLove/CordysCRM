package cn.cordys.crm.ad.seal.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 用印记录状态（ad_seal_record.status，V3.1 §5.2.4 / PRD 5.2.4，L-07/L-19）。
 * <ul>
 *   <li>0 = 审批中</li>
 *   <li>10 = 通过</li>
 *   <li>20 = 驳回</li>
 * </ul>
 */
public enum AdSealRecordStatus {
    APPROVING(0, "审批中"),
    APPROVED(10, "通过"),
    REJECTED(20, "驳回");

    private final int code;
    private final String label;

    AdSealRecordStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, AdSealRecordStatus> MAP = new HashMap<>();

    static {
        for (AdSealRecordStatus e : values()) {
            MAP.put(e.getCode(), e);
        }
    }

    public static AdSealRecordStatus of(Integer code) {
        return code == null ? null : MAP.get(code);
    }

    public static String labelOf(Integer code) {
        AdSealRecordStatus e = of(code);
        return e == null ? (code == null ? null : String.valueOf(code)) : e.getLabel();
    }
}
