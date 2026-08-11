package cn.cordys.crm.ad.common.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.Pager;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.common.dto.request.AdOperationLogPageRequest;
import cn.cordys.crm.ad.common.dto.response.AdOperationLogResponse;
import cn.cordys.crm.ad.common.service.AdOperationLogQueryService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告操作日志控制器。
 */
@RestController
@RequestMapping("/ad/operation/log")
@Tag(name = "广告操作日志")
public class AdOperationLogController {

    @Resource
    private AdOperationLogQueryService operationLogQueryService;

    @PostMapping("/list")
    @Operation(summary = "广告操作日志-列表查询")
    @RequiresPermissions(PermissionConstants.OPERATION_LOG_READ)
    public Pager<List<AdOperationLogResponse>> list(@Validated @RequestBody AdOperationLogPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page,
                operationLogQueryService.list(request, OrganizationContext.getOrganizationId()));
    }
}
