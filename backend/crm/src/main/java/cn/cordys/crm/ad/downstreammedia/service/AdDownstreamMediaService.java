package cn.cordys.crm.ad.downstreammedia.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.businessentity.domain.AdBusinessEntity;
import cn.cordys.crm.ad.common.AdEntityPermissionProvider;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.dict.domain.AdDict;
import cn.cordys.crm.ad.dict.service.AdDictService;
import cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMedia;
import cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMediaAccount;
import cn.cordys.crm.ad.downstreammedia.dto.request.AdDownstreamMediaAccountSaveItem;
import cn.cordys.crm.ad.downstreammedia.dto.request.AdDownstreamMediaPageRequest;
import cn.cordys.crm.ad.downstreammedia.dto.request.AdDownstreamMediaSaveRequest;
import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaAccountItem;
import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaDetailResponse;
import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaListResponse;
import cn.cordys.crm.ad.downstreammedia.mapper.ExtAdDownstreamMediaAccountMapper;
import cn.cordys.crm.ad.downstreammedia.mapper.ExtAdDownstreamMediaMapper;
import cn.cordys.mybatis.BaseMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdDownstreamMediaService {

    @Resource
    private BaseMapper<AdDownstreamMedia> mediaMapper;
    @Resource
    private BaseMapper<AdBusinessEntity> businessEntityMapper;
    @Resource
    private ExtAdDownstreamMediaMapper extDownstreamMediaMapper;
    @Resource
    private BaseMapper<AdDownstreamMediaAccount> accountMapper;
    @Resource
    private ExtAdDownstreamMediaAccountMapper extAccountMapper;
    @Resource
    private AdDictService adDictService;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;

    private static final int STATUS_NORMAL = 10;
    private static final int STATUS_DISABLED = 20;

    @OperationLog(module = "AD_DOWNSTREAM_MEDIA", action = "CREATE")
    public AdDownstreamMedia create(AdDownstreamMediaSaveRequest request, String userId, String orgId) {
        validate(request);
        AdDownstreamMedia media = new AdDownstreamMedia();
        media.setId(IDGenerator.nextStr());
        fillEntity(media, request, userId, orgId);
        media.setCreateUser(userId);
        media.setCreateTime(System.currentTimeMillis());
        mediaMapper.insert(media);
        saveAccounts(media.getId(), request.getAccounts(), userId, orgId);
        return media;
    }

    @OperationLog(module = "AD_DOWNSTREAM_MEDIA", action = "UPDATE", targetId = "#request.id")
    public AdDownstreamMedia update(AdDownstreamMediaSaveRequest request, String userId, String orgId) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("id不能为空");
        }
        AdDownstreamMedia existing = requireMedia(request.getId());
        fillEntity(existing, request, userId, orgId);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        mediaMapper.update(existing);
        saveAccounts(existing.getId(), request.getAccounts(), userId, orgId);
        return existing;
    }

    public AdDownstreamMediaDetailResponse detail(String id, String userId, String orgId) {
        AdDownstreamMedia media = requireMedia(id);
        AdDownstreamMediaDetailResponse resp = new AdDownstreamMediaDetailResponse();
        resp.setMedia(media);
        resp.setStatusLabel(media.getStatus() != null && media.getStatus() == STATUS_DISABLED ? "停用" : "正常");
        resp.setCooperationStatusLabel(media.getCooperationStatus() != null && media.getCooperationStatus() == STATUS_DISABLED ? "停用" : "正常");
        if (media.getMediaType() != null) {
            List<AdDict> dicts = adDictService.listByDictCode("media_type");
            Map<String, String> labelMap = dicts.stream().collect(Collectors.toMap(AdDict::getDictValue, AdDict::getDictLabel, (a, b) -> a));
            resp.setMediaTypeLabel(labelMap.getOrDefault(media.getMediaType(), media.getMediaType()));
        }
        if (media.getBusinessEntityId() != null && !media.getBusinessEntityId().isBlank()) {
            AdBusinessEntity be = businessEntityMapper.selectByPrimaryKey(media.getBusinessEntityId());
            resp.setBusinessEntityName(be == null ? null : be.getName());
        }
        resp.setAccountList(toAccountItems(extAccountMapper.selectByDownstreamMediaId(id)));
        return resp;
    }

    public List<AdDownstreamMediaAccountItem> listAccounts(String mediaId, String userId, String orgId) {
        requireMedia(mediaId);
        return toAccountItems(extAccountMapper.selectByDownstreamMediaId(mediaId));
    }

    public PagerWithOption<List<AdDownstreamMediaListResponse>> page(AdDownstreamMediaPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        request.setEntityIds(entityPermissionProvider.buildEntityFilter());
        Page<AdDownstreamMediaListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdDownstreamMediaListResponse> list = extDownstreamMediaMapper.pageList(request);
        List<AdDict> dicts = adDictService.listByDictCode("media_type");
        Map<String, String> labelMap = dicts.stream().collect(Collectors.toMap(AdDict::getDictValue, AdDict::getDictLabel, (a, b) -> a));
        for (AdDownstreamMediaListResponse r : list) {
            r.setStatusLabel(r.getStatus() != null && r.getStatus() == STATUS_DISABLED ? "停用" : "正常");
            r.setCooperationStatusLabel(r.getCooperationStatus() != null && r.getCooperationStatus() == STATUS_DISABLED ? "停用" : "正常");
            if (r.getMediaType() != null) {
                r.setMediaTypeLabel(labelMap.getOrDefault(r.getMediaType(), r.getMediaType()));
            }
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    @OperationLog(module = "AD_DOWNSTREAM_MEDIA", action = "DELETE", targetId = "#id")
    public void delete(String id, String userId, String orgId) {
        AdDownstreamMedia existing = requireMedia(id);
        existing.setDeleted(1);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        mediaMapper.update(existing);
    }

    // ========== helpers ==========

    private void fillEntity(AdDownstreamMedia media, AdDownstreamMediaSaveRequest request, String userId, String orgId) {
        media.setName(request.getName());
        media.setMediaType(request.getMediaType());
        media.setChannel(request.getChannel());
        media.setRateCard(request.getRateCard());
        media.setDiscountPolicy(request.getDiscountPolicy());
        media.setContactPerson(request.getContactPerson());
        media.setContactPhone(request.getContactPhone());
        media.setCooperationStatus(request.getCooperationStatus() != null ? request.getCooperationStatus() : STATUS_NORMAL);
        media.setStatus(request.getStatus() != null ? request.getStatus() : STATUS_NORMAL);
        media.setBusinessEntityId(request.getBusinessEntityId());
        media.setOrganizationId(orgId);
    }

    private AdDownstreamMedia requireMedia(String id) {
        AdDownstreamMedia media = mediaMapper.selectByPrimaryKey(id);
        if (media == null || (media.getDeleted() != null && media.getDeleted() == 1)) {
            throw new GenericException("下游客户不存在");
        }
        return media;
    }

    private void validate(AdDownstreamMediaSaveRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new GenericException("名称不能为空");
        }
    }

    // ========== 银行账户 ==========

    /**
     * 保存账户：accounts 为 null 表示本次不改动；传空列表表示清空该客户全部账户；
     * 列表内的项按 id 是否存在决定新增或更新，列表外的原账户逻辑删除。
     */
    private void saveAccounts(String mediaId, List<AdDownstreamMediaAccountSaveItem> accounts, String userId, String orgId) {
        if (accounts == null) {
            return;
        }
        List<AdDownstreamMediaAccount> existing = extAccountMapper.selectByDownstreamMediaId(mediaId);
        java.util.Set<String> keepIds = new java.util.HashSet<>();
        long now = System.currentTimeMillis();
        for (AdDownstreamMediaAccountSaveItem item : accounts) {
            if (item.getPayeeName() == null || item.getPayeeName().isBlank()
                    || item.getBankName() == null || item.getBankName().isBlank()
                    || item.getBankAccount() == null || item.getBankAccount().isBlank()) {
                continue;
            }
            if (item.getId() != null && !item.getId().isBlank()) {
                AdDownstreamMediaAccount acc = accountMapper.selectByPrimaryKey(item.getId());
                if (acc != null && acc.getDeleted() != null && acc.getDeleted() == 0) {
                    acc.setPayeeName(item.getPayeeName());
                    acc.setBankName(item.getBankName());
                    acc.setBankAccount(item.getBankAccount());
                    acc.setDisabled(item.getDisabled() == null ? 0 : item.getDisabled());
                    acc.setUpdateUser(userId);
                    acc.setUpdateTime(now);
                    accountMapper.update(acc);
                    keepIds.add(acc.getId());
                }
            } else {
                AdDownstreamMediaAccount acc = new AdDownstreamMediaAccount();
                acc.setId(IDGenerator.nextStr());
                acc.setDownstreamMediaId(mediaId);
                acc.setPayeeName(item.getPayeeName());
                acc.setBankName(item.getBankName());
                acc.setBankAccount(item.getBankAccount());
                acc.setDisabled(item.getDisabled() == null ? 0 : item.getDisabled());
                acc.setOrganizationId(orgId);
                acc.setCreateUser(userId);
                acc.setCreateTime(now);
                acc.setUpdateUser(userId);
                acc.setUpdateTime(now);
                acc.setDeleted(0);
                accountMapper.insert(acc);
                keepIds.add(acc.getId());
            }
        }
        // 逻辑删除不在列表内的原账户
        for (AdDownstreamMediaAccount acc : existing) {
            if (!keepIds.contains(acc.getId())) {
                acc.setDeleted(1);
                acc.setUpdateUser(userId);
                acc.setUpdateTime(now);
                accountMapper.update(acc);
            }
        }
    }

    private List<AdDownstreamMediaAccountItem> toAccountItems(List<AdDownstreamMediaAccount> list) {
        if (list == null) {
            return java.util.Collections.emptyList();
        }
        List<AdDownstreamMediaAccountItem> items = new java.util.ArrayList<>(list.size());
        for (AdDownstreamMediaAccount acc : list) {
            AdDownstreamMediaAccountItem item = new AdDownstreamMediaAccountItem();
            item.setId(acc.getId());
            item.setDownstreamMediaId(acc.getDownstreamMediaId());
            item.setPayeeName(acc.getPayeeName());
            item.setBankName(acc.getBankName());
            item.setBankAccount(acc.getBankAccount());
            item.setDisabled(acc.getDisabled());
            item.setCreateTime(acc.getCreateTime());
            items.add(item);
        }
        return items;
    }
}
