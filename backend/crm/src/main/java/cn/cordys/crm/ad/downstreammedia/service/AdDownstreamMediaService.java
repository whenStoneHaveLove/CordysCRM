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
import cn.cordys.crm.ad.downstreammedia.dto.request.AdDownstreamMediaPageRequest;
import cn.cordys.crm.ad.downstreammedia.dto.request.AdDownstreamMediaSaveRequest;
import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaDetailResponse;
import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaListResponse;
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
    private AdDictService adDictService;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;

    private static final int STATUS_NORMAL = 10;
    private static final int STATUS_DISABLED = 20;

    @OperationLog(module = "DOWNSTREAM_MEDIA", action = "CREATE")
    public AdDownstreamMedia create(AdDownstreamMediaSaveRequest request, String userId, String orgId) {
        validate(request);
        AdDownstreamMedia media = new AdDownstreamMedia();
        media.setId(IDGenerator.nextStr());
        fillEntity(media, request, userId, orgId);
        media.setCreateUser(userId);
        media.setCreateTime(System.currentTimeMillis());
        mediaMapper.insert(media);
        return media;
    }

    @OperationLog(module = "DOWNSTREAM_MEDIA", action = "UPDATE", targetId = "#request.id")
    public AdDownstreamMedia update(AdDownstreamMediaSaveRequest request, String userId, String orgId) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("id不能为空");
        }
        AdDownstreamMedia existing = requireMedia(request.getId());
        fillEntity(existing, request, userId, orgId);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        mediaMapper.update(existing);
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
        return resp;
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

    @OperationLog(module = "DOWNSTREAM_MEDIA", action = "DELETE", targetId = "#id")
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
            throw new GenericException("下游媒体不存在");
        }
        return media;
    }

    private void validate(AdDownstreamMediaSaveRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new GenericException("媒体名称不能为空");
        }
    }
}
