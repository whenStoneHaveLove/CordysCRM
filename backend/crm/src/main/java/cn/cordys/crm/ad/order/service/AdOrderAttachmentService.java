package cn.cordys.crm.ad.order.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.common.constants.AdAttachmentType;
import cn.cordys.crm.ad.order.domain.AdOrderAttachment;
import cn.cordys.crm.ad.order.dto.response.AdEmlPreviewResponse;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderAttachmentMapper;
import cn.cordys.crm.system.dto.request.UploadTransferRequest;
import cn.cordys.crm.system.service.AttachmentService;
import jakarta.annotation.Resource;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimePart;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Service
@Slf4j
public class AdOrderAttachmentService {

    /** 邮件记录（eml）附件的允许后缀 */
    private static final String EML_SUFFIX = ".eml";

    /** eml 内嵌图片转 data URL 的大小上限，避免响应体过大 */
    private static final long INLINE_IMAGE_MAX_SIZE = 2 * 1024 * 1024L;

    @Resource
    private ExtAdOrderAttachmentMapper attachmentMapper;
    @jakarta.annotation.Resource(name = "attachmentService")
    private AttachmentService systemAttachmentService;

    /** 上传附件（type: 10盖章排期/20邮件截图/30合同/40过程附件/50改单附件/60邮件记录eml） */
    public AdOrderAttachment upload(String orderId, int type, MultipartFile file, String userId, String orgId) {
        if (file.isEmpty()) {
            throw new GenericException("文件不能为空");
        }
        if (type == AdAttachmentType.EMAIL_RECORD.getCode() && !isEml(file.getOriginalFilename())) {
            throw new GenericException("【邮件记录】仅支持上传 " + EML_SUFFIX + " 文件");
        }
        List<String> urls = systemAttachmentService.uploadTemp(List.of(file));
        String fileUrl = urls.isEmpty() ? null : urls.getFirst();

        // 转存到正式目录：临时目录(tmp)会被定时任务(CleanTempResourceListener)清理，
        // 而订单附件属于业务凭证必须长期保留。转存后会生成同 ID 的 sys_attachment 记录，
        // 原预览/下载 URL(/attachment/preview/{fileId})保持不变，仅底层存储位置变化。
        if (fileUrl != null) {
            systemAttachmentService.appendTemp(new UploadTransferRequest(orgId, orderId, userId, List.of(fileUrl)));
        }

        AdOrderAttachment att = new AdOrderAttachment();
        att.setId(IDGenerator.nextStr());
        att.setOrderId(orderId);
        att.setType(type);
        att.setFileUrl(fileUrl);
        att.setFileName(file.getOriginalFilename());
        att.setOrganizationId(orgId);
        att.setCreateUser(userId);
        att.setCreateTime(System.currentTimeMillis());
        att.setDeleted(0);
        attachmentMapper.insert(att);
        return att;
    }

    private boolean isEml(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(EML_SUFFIX);
    }

    /** 删除附件 */
    public void delete(String id) {
        AdOrderAttachment att = attachmentMapper.selectByPrimaryKey(id);
        if (att == null) {
            throw new GenericException("附件不存在");
        }
        att.setDeleted(1);
        attachmentMapper.updateById(att);
    }

    /** 查询订单附件 */
    public List<AdOrderAttachment> listByOrderId(String orderId) {
        return attachmentMapper.selectByOrderId(orderId);
    }

    /**
     * 预览邮件记录(eml)附件：解析邮件内容，浏览器无法直接渲染 eml，故服务端解析后返回结构化内容
     *
     * @param attachmentId 附件ID
     *
     * @return 邮件预览内容
     */
    public AdEmlPreviewResponse previewEml(String attachmentId) {
        AdOrderAttachment att = attachmentMapper.selectByPrimaryKey(attachmentId);
        if (att == null || (att.getDeleted() != null && att.getDeleted() == 1)) {
            throw new GenericException("附件不存在");
        }
        if (att.getType() == null || att.getType() != AdAttachmentType.EMAIL_RECORD.getCode()) {
            throw new GenericException("仅【邮件记录(eml)】支持此预览方式");
        }
        ResponseEntity<org.springframework.core.io.Resource> resource = systemAttachmentService.getResource(att.getFileUrl(), true);
        if (resource == null || resource.getBody() == null) {
            throw new GenericException("文件不存在或已被删除");
        }
        try (InputStream inputStream = resource.getBody().getInputStream()) {
            return parseEml(att, inputStream);
        } catch (GenericException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 eml 文件失败, file={}", att.getFileName(), e);
            throw new GenericException("邮件文件解析失败，请确认上传的是有效的 eml 文件");
        }
    }

    /**
     * 解析 eml 邮件内容
     */
    private AdEmlPreviewResponse parseEml(AdOrderAttachment att, InputStream inputStream) throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()), inputStream);
        AdEmlPreviewResponse response = new AdEmlPreviewResponse();
        response.setId(att.getId());
        response.setFileName(att.getFileName());
        response.setSubject(message.getSubject());
        response.setFrom(String.join(", ", formatAddresses(message.getFrom())));
        response.setTo(formatAddresses(message.getRecipients(Message.RecipientType.TO)));
        response.setCc(formatAddresses(message.getRecipients(Message.RecipientType.CC)));

        Date sentDate = message.getSentDate() != null ? message.getSentDate() : message.getReceivedDate();
        response.setSentTime(sentDate != null ? sentDate.getTime() : null);

        StringBuilder html = new StringBuilder();
        StringBuilder text = new StringBuilder();
        Map<String, String> inlineImages = new HashMap<>();
        List<AdEmlPreviewResponse.EmlAttachment> attachments = new ArrayList<>();
        collectPart(message, html, text, inlineImages, attachments);

        response.setHtml(replaceInlineImage(html.toString(), inlineImages));
        response.setText(text.toString());
        response.setAttachments(attachments);
        return response;
    }

    /**
     * 递归收集邮件各分部：正文HTML、纯文本、内嵌图片、附件清单
     */
    private void collectPart(Part part,
                             StringBuilder html,
                             StringBuilder text,
                             Map<String, String> inlineImages,
                             List<AdEmlPreviewResponse.EmlAttachment> attachments) throws Exception {
        if (part.isMimeType("multipart/*")) {
            if (part.getContent() instanceof MimeMultipart multipart) {
                for (int i = 0; i < multipart.getCount(); i++) {
                    collectPart(multipart.getBodyPart(i), html, text, inlineImages, attachments);
                }
            }
            return;
        }

        String disposition = part.getDisposition();
        boolean isAttachment = Part.ATTACHMENT.equalsIgnoreCase(disposition);
        String contentId = part instanceof MimePart mimePart ? mimePart.getContentID() : null;
        boolean isInline = Part.INLINE.equalsIgnoreCase(disposition);

        if (!isAttachment && part.isMimeType("text/html")) {
            if (html.length() == 0 && part.getContent() instanceof String content) {
                html.append(content);
            }
            return;
        }
        if (!isAttachment && part.isMimeType("text/plain")) {
            if (text.length() == 0 && part.getContent() instanceof String content) {
                text.append(content);
            }
            return;
        }

        // 内嵌图片(cid)转为 data URL，供正文直接展示
        if (isInline && contentId != null && part.getSize() <= INLINE_IMAGE_MAX_SIZE) {
            byte[] bytes = part.getInputStream().readAllBytes();
            String baseType = part.getContentType() == null ? "image/png" : part.getContentType().split(";")[0].trim();
            inlineImages.put(normalizeContentId(contentId), "data:" + baseType + ";base64," + Base64.getEncoder().encodeToString(bytes));
        }

        if (isAttachment || isInline || disposition != null) {
            AdEmlPreviewResponse.EmlAttachment emlAttachment = new AdEmlPreviewResponse.EmlAttachment();
            emlAttachment.setName(decodeFileName(part.getFileName()));
            emlAttachment.setSize(part.getSize() > 0 ? (long) part.getSize() : null);
            attachments.add(emlAttachment);
        }
    }

    /**
     * 将正文中的 cid:xxx 替换为内嵌图片的 data URL
     */
    private String replaceInlineImage(String html, Map<String, String> inlineImages) {
        String result = html;
        for (Map.Entry<String, String> entry : inlineImages.entrySet()) {
            result = result.replace("cid:" + entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String normalizeContentId(String contentId) {
        return contentId.replace("<", "").replace(">", "");
    }

    private String decodeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        try {
            return MimeUtility.decodeText(fileName);
        } catch (Exception e) {
            return fileName;
        }
    }

    private List<String> formatAddresses(Address[] addresses) {
        if (addresses == null) {
            return List.of();
        }
        return Arrays.stream(addresses)
                .map(address -> {
                    if (address instanceof InternetAddress internetAddress) {
                        String personal = internetAddress.getPersonal();
                        return personal != null ? personal + " <" + internetAddress.getAddress() + ">" : internetAddress.getAddress();
                    }
                    return address.toString();
                })
                .toList();
    }
}
