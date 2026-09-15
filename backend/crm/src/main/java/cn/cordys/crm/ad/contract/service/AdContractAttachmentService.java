package cn.cordys.crm.ad.contract.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.contract.domain.AdContractAttachment;
import cn.cordys.crm.ad.contract.dto.request.AdContractAttachmentItem;
import cn.cordys.crm.ad.contract.mapper.ExtAdContractAttachmentMapper;
import cn.cordys.crm.system.dto.request.UploadTransferRequest;
import cn.cordys.crm.system.service.AttachmentService;
import cn.cordys.security.SessionUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 合同附件服务（用印附件 / 双盖附件，支持多文件）。
 *
 * <p>附件以临时文件ID形式由前端上传，转存为正式附件（processTemp）后写入 ad_contract_attachment。
 * type: 10=用印附件, 20=双盖附件。</p>
 */
@Service
public class AdContractAttachmentService {

    public static final int TYPE_SEAL = 10;
    public static final int TYPE_DOUBLE_SEAL = 20;

    @Resource
    private ExtAdContractAttachmentMapper attachmentMapper;
    @Resource
    private AttachmentService attachmentService;

    /**
     * 上传单个附件：转存临时文件并写入子表。
     */
    public AdContractAttachment upload(String contractId, int type, String tempFileId, String fileName,
                                       String userId, String orgId) {
        if (tempFileId == null || tempFileId.isBlank()) {
            throw new GenericException("临时文件ID不能为空");
        }
        UploadTransferRequest transferRequest =
                new UploadTransferRequest(orgId, contractId, userId, List.of(tempFileId));
        attachmentService.processTemp(transferRequest);

        AdContractAttachment att = new AdContractAttachment();
        att.setId(IDGenerator.nextStr());
        att.setContractId(contractId);
        att.setType(type);
        att.setFileUrl(tempFileId);
        att.setFileName(fileName);
        att.setOrganizationId(orgId);
        att.setCreateUser(userId);
        att.setCreateTime(System.currentTimeMillis());
        att.setUpdateUser(userId);
        att.setUpdateTime(System.currentTimeMillis());
        att.setDeleted(0);
        attachmentMapper.insert(att);
        return att;
    }

    /**
     * 按合同 + 类型，用给定文件集合重建该类型下附件：保留已存在的、删除不再存在的、新增之前没有的。
     * 仅对“新增项”调用 processTemp，避免对已转存的临时文件重复转存导致唯一键冲突。
     */
    public void reconcileByType(String contractId, int type, List<AdContractAttachmentItem> items,
                                String userId, String orgId) {
        final List<AdContractAttachmentItem> requestItems = items == null ? List.of() : items;
        List<AdContractAttachment> existing = attachmentMapper.selectByContractIdAndType(contractId, type);
        Set<String> existingUrls = existing.stream()
                .map(AdContractAttachment::getFileUrl).collect(Collectors.toSet());

        List<AdContractAttachmentItem> toAdd = requestItems.stream()
                .filter(it -> it.getTempFileId() != null && !it.getTempFileId().isBlank())
                .filter(it -> !existingUrls.contains(it.getTempFileId()))
                .toList();
        for (AdContractAttachmentItem it : toAdd) {
            upload(contractId, type, it.getTempFileId(), it.getFileName(), userId, orgId);
        }

        List<AdContractAttachment> toRemove = existing.stream()
                .filter(e -> requestItems.stream().noneMatch(it -> it.getTempFileId() != null
                        && it.getTempFileId().equals(e.getFileUrl())))
                .toList();
        for (AdContractAttachment e : toRemove) {
            e.setDeleted(1);
            e.setUpdateUser(userId);
            e.setUpdateTime(System.currentTimeMillis());
            attachmentMapper.updateById(e);
        }
    }

    /**
     * 逻辑删除单个附件（仅标记 deleted=1，不改 sys_attachment 实际文件）。
     */
    public void delete(String id) {
        AdContractAttachment att = attachmentMapper.selectByPrimaryKey(id);
        if (att == null) {
            throw new GenericException("附件不存在");
        }
        att.setDeleted(1);
        att.setUpdateUser(SessionUtils.getUserId());
        att.setUpdateTime(System.currentTimeMillis());
        attachmentMapper.updateById(att);
    }

    public List<AdContractAttachment> listByContractId(String contractId) {
        return attachmentMapper.selectByContractId(contractId);
    }
}
