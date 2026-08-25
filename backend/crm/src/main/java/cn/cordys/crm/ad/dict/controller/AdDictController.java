package cn.cordys.crm.ad.dict.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.dict.domain.AdDict;
import cn.cordys.crm.ad.dict.dto.request.AdDictPageRequest;
import cn.cordys.crm.ad.dict.dto.request.AdDictSaveRequest;
import cn.cordys.crm.ad.dict.dto.response.AdDictDetailResponse;
import cn.cordys.crm.ad.dict.dto.response.AdDictListResponse;
import cn.cordys.crm.ad.dict.service.AdDictService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告字典控制器（V3.1 §5.3 / §4.3，B-4 后端补齐）。
 *
 * <p>运营可维护的枚举项（行业/类型/用印类型/收付款方式等）。
 * 接口级权限由 {@link CsPermission} 依据 {@link PermissionConstants} 的 AD_DICT_* 码校验。</p>
 */
@Tag(name = "广告字典")
@RestController
@RequestMapping("/api/ad/dict")
public class AdDictController {

    @Resource
    private AdDictService adDictService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @CsPermission(PermissionConstants.AD_DICT_CREATE)
    @Operation(summary = "新建字典项")
    public AdDict create(@RequestBody AdDictSaveRequest request) {
        return adDictService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_DICT_UPDATE)
    @Operation(summary = "编辑字典项")
    public AdDict update(@RequestBody AdDictSaveRequest request) {
        return adDictService.update(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_DICT_READ)
    @Operation(summary = "字典项详情")
    public AdDictDetailResponse detail(@PathVariable("id") String id) {
        return adDictService.detail(id, userId(), orgId());
    }

    @DeleteMapping("/{id}")
    @CsPermission(PermissionConstants.AD_DICT_DELETE)
    @Operation(summary = "删除字典项")
    public void delete(@PathVariable("id") String id) {
        adDictService.remove(id);
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_DICT_READ)
    @Operation(summary = "字典项分页（字典编码+关键字+状态筛选）")
    public PagerWithOption<List<AdDictListResponse>> page(@RequestBody AdDictPageRequest request) {
        return adDictService.page(request, userId(), orgId());
    }
}
