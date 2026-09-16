package cn.cordys.crm.ad.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 邮件记录(eml)预览内容
 */
@Data
public class AdEmlPreviewResponse {

    @Schema(description = "附件ID")
    private String id;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "邮件主题")
    private String subject;

    @Schema(description = "发件人")
    private String from;

    @Schema(description = "收件人")
    private List<String> to;

    @Schema(description = "抄送人")
    private List<String> cc;

    @Schema(description = "邮件发送时间(时间戳)")
    private Long sentTime;

    @Schema(description = "正文HTML，内嵌图片(cid)已替换为 data URL；前端需用 iframe sandbox 渲染以防 XSS")
    private String html;

    @Schema(description = "正文纯文本，无HTML正文时使用")
    private String text;

    @Schema(description = "邮件内附件名称列表")
    private List<EmlAttachment> attachments;

    @Data
    public static class EmlAttachment {

        @Schema(description = "附件名称")
        private String name;

        @Schema(description = "附件大小(字节)")
        private Long size;
    }
}
