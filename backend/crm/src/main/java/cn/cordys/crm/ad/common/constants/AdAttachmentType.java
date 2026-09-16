package cn.cordys.crm.ad.common.constants;

import lombok.Getter;

/**
 * 广告订单附件类型（V3.1 §5.2.6）。
 *
 * <p>提交守卫规则：上传了 {@link #EMAIL_RECORD}（完整 eml 邮件记录）时，
 * 可豁免 {@link #SCHEDULE} + {@link #EMAIL_SCREENSHOT}；否则两者仍需齐备。</p>
 */
@Getter
public enum AdAttachmentType {
    SCHEDULE(10, "盖章排期"),
    EMAIL_SCREENSHOT(20, "邮件截图"),
    CONTRACT(30, "合同"),
    PROCESS(40, "过程附件"),
    CHANGE(50, "改单附件"),
    EMAIL_RECORD(60, "邮件记录(eml)");

    private final int code;
    private final String label;

    AdAttachmentType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdAttachmentType of(int code) {
        for (AdAttachmentType e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        if (code == null) return null;
        AdAttachmentType e = of(code);
        return e == null ? String.valueOf(code) : e.getLabel();
    }
}
