package cn.cordys.crm.ad.dict.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.dict.constants.DictStatus;
import cn.cordys.crm.ad.dict.domain.AdDict;
import cn.cordys.crm.ad.dict.dto.request.AdDictPageRequest;
import cn.cordys.crm.ad.dict.dto.request.AdDictSaveRequest;
import cn.cordys.crm.ad.dict.dto.response.AdDictDetailResponse;
import cn.cordys.crm.ad.dict.dto.response.AdDictListResponse;
import cn.cordys.crm.ad.dict.mapper.ExtAdDictMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典服务（V3.1 §5.3/§4.3）。按 dict_code 查询启用的字典项。
 *
 * <p>B-4 补齐：新增 {@code create/update/detail/page} 以支撑 {@code AdDictController}
 * （POST/PUT/GET /{id}/DELETE/POST page）。分页复用 {@code ExtAdDictMapper.pageList}（PageHelper）。</p>
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AdDictService {

    @Resource
    private ExtAdDictMapper dictMapper;

    public String save(AdDict dict) {
        dictMapper.insert(dict);
        return dict.getId();
    }

    public void update(AdDict dict) {
        dictMapper.updateById(dict);
    }

    @OperationLog(module = "AD_DICT", action = "DELETE", targetId = "#id")
    public void remove(String id) {
        dictMapper.deleteByPrimaryKey(id);
    }

    public AdDict get(String id) {
        return dictMapper.selectByPrimaryKey(id);
    }

    /**
     * 按字典编码查询启用的字典项（用于下拉选项）。
     */
    public List<AdDict> listByDictCode(String dictCode) {
        return dictMapper.selectByDictCode(dictCode);
    }

    /** 新建字典项（B-4）。 */
    @OperationLog(module = "AD_DICT", action = "CREATE", targetId = "")
    public AdDict create(AdDictSaveRequest request, String userId, String orgId) {
        validate(request);
        AdDict dict = new AdDict();
        dict.setId(IDGenerator.nextStr());
        dict.setDictCode(request.getDictCode());
        dict.setDictValue(request.getDictValue());
        dict.setDictLabel(request.getDictLabel());
        dict.setParentValue(request.getParentValue());
        dict.setSort(request.getSort() != null ? request.getSort() : 0);
        dict.setStatus(request.getStatus() != null ? request.getStatus() : DictStatus.ENABLED.getCode());
        dict.setOrganizationId(orgId);
        dict.setCreateUser(userId);
        dict.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        dict.setCreateTime(now);
        dict.setUpdateTime(now);
        dictMapper.insert(dict);
        return dict;
    }

    /** 编辑字典项（B-4）。 */
    @OperationLog(module = "AD_DICT", action = "UPDATE", targetId = "#request.id")
    public AdDict update(AdDictSaveRequest request, String userId, String orgId) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("字典id不能为空");
        }
        AdDict existing = requireDict(request.getId());
        existing.setDictCode(request.getDictCode());
        existing.setDictValue(request.getDictValue());
        existing.setDictLabel(request.getDictLabel());
        existing.setParentValue(request.getParentValue());
        if (request.getSort() != null) {
            existing.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setOrganizationId(orgId);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        dictMapper.updateById(existing);
        return existing;
    }

    /** 字典项详情（B-4）。 */
    public AdDictDetailResponse detail(String id, String userId, String orgId) {
        AdDict dict = requireDict(id);
        AdDictDetailResponse resp = new AdDictDetailResponse();
        resp.setDict(dict);
        resp.setStatusLabel(DictStatus.labelOf(dict.getStatus()));
        return resp;
    }

    /** 字典项分页（B-4）。 */
    public cn.cordys.common.pager.PagerWithOption<List<AdDictListResponse>> page(
            AdDictPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        Page<AdDictListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdDictListResponse> list = dictMapper.pageList(request);
        for (AdDictListResponse r : list) {
            r.setStatusLabel(DictStatus.labelOf(r.getStatus()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    // ===================== 私有辅助 =====================

    private void validate(AdDictSaveRequest request) {
        if (request.getDictCode() == null || request.getDictCode().isBlank()) {
            throw new GenericException("字典编码不能为空");
        }
        if (request.getDictValue() == null || request.getDictValue().isBlank()) {
            throw new GenericException("字典值不能为空");
        }
        if (request.getDictLabel() == null || request.getDictLabel().isBlank()) {
            throw new GenericException("字典显示名不能为空");
        }
    }

    private AdDict requireDict(String id) {
        AdDict dict = dictMapper.selectByPrimaryKey(id);
        if (dict == null || (dict.getDeleted() != null && dict.getDeleted() == 1)) {
            throw new GenericException("字典项不存在");
        }
        return dict;
    }
}
