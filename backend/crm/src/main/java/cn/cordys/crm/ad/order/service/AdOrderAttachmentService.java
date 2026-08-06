package cn.cordys.crm.ad.order.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.order.domain.AdOrderAttachment;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderAttachmentMapper;
import cn.cordys.crm.system.service.AttachmentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AdOrderAttachmentService {

    @Resource
    private ExtAdOrderAttachmentMapper attachmentMapper;
    @jakarta.annotation.Resource(name = "attachmentService")
    private AttachmentService systemAttachmentService;

    /** 上传附件（type: 10盖章排期/20邮件截图/30合同/40过程附件/50改单附件） */
    public AdOrderAttachment upload(String orderId, int type, MultipartFile file, String userId, String orgId) {
        if (file.isEmpty()) {
            throw new GenericException("文件不能为空");
        }
        List<String> urls = systemAttachmentService.uploadTemp(List.of(file));
        String fileUrl = urls.isEmpty() ? null : urls.getFirst();

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
}
