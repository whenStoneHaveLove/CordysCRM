package cn.cordys.crm.ad.customer.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.common.AdEntityPermissionProvider;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.customer.constants.CustomerLevel;
import cn.cordys.crm.ad.customer.constants.CustomerStatus;
import cn.cordys.crm.ad.customer.domain.AdCustomer;
import cn.cordys.crm.ad.customer.dto.request.AdCustomerPageRequest;
import cn.cordys.crm.ad.customer.dto.request.AdCustomerSaveRequest;
import cn.cordys.crm.ad.customer.dto.response.AdCustomerDetailResponse;
import cn.cordys.crm.ad.customer.dto.response.AdCustomerListResponse;
import cn.cordys.crm.ad.customer.mapper.ExtAdCustomerMapper;
import cn.cordys.crm.ad.dict.domain.AdDict;
import cn.cordys.crm.ad.dict.service.AdDictService;
import cn.cordys.crm.ad.order.mapper.ExtAdOrderMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUser;
import cn.cordys.common.dto.RoleDataScopeDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 广告客户服务（M6，V3.1 广告客户管理）。
 *
 * <p>客户 CRUD + 分页 + 详情。写操作标注 {@link OperationLog}。</p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdCustomerService {

    @Resource
    private BaseMapper<AdCustomer> customerMapper;
    @Resource
    private ExtAdCustomerMapper extAdCustomerMapper;
    @Resource
    private ExtAdOrderMapper extAdOrderMapper;
    @Resource
    private AdDictService adDictService;
    @Resource
    private AdEntityPermissionProvider entityPermissionProvider;

    private static final String ROLE_MEDIA = "ROLE_MEDIA";

    /** 新建客户。 */
    @OperationLog(module = "CUSTOMER", action = "CREATE", targetId = "")
    public AdCustomer create(AdCustomerSaveRequest request, String userId, String orgId) {
        assertRole(ROLE_MEDIA);
        validate(request);

        AdCustomer c = new AdCustomer();
        c.setId(IDGenerator.nextStr());
        c.setName(request.getCustomerName());
        c.setContactPerson(request.getContactPerson());
        c.setContactPhone(request.getContactPhone());
        c.setEmail(request.getEmail());
        c.setAddress(request.getAddress());
        c.setIndustry(request.getIndustry());
        c.setBrand(request.getBrand());
        c.setIndustryCode(request.getIndustryCode());
        c.setSigningEntity(request.getSigningEntity());
        c.setCustomerLevel(request.getCustomerLevel());
        c.setStatus(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE.getCode());
        c.setRemark(request.getRemark());
        c.setOrganizationId(orgId);
        c.setCreateUser(userId);
        c.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        c.setCreateTime(now);
        c.setUpdateTime(now);
        customerMapper.insert(c);
        return c;
    }

    /** 编辑客户。 */
    @OperationLog(module = "CUSTOMER", action = "UPDATE", targetId = "#request.id")
    public AdCustomer update(AdCustomerSaveRequest request, String userId, String orgId) {
        assertRole(ROLE_MEDIA);
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("客户id不能为空");
        }
        AdCustomer existing = requireCustomer(request.getId());
        String org0 = existing.getOrganizationId();
        String cu0 = existing.getCreateUser();
        Long ct0 = existing.getCreateTime();
        Integer del0 = existing.getDeleted();

        existing.setName(request.getCustomerName());
        existing.setContactPerson(request.getContactPerson());
        existing.setContactPhone(request.getContactPhone());
        existing.setEmail(request.getEmail());
        existing.setAddress(request.getAddress());
        existing.setIndustry(request.getIndustry());
        existing.setBrand(request.getBrand());
        existing.setIndustryCode(request.getIndustryCode());
        existing.setSigningEntity(request.getSigningEntity());
        existing.setCustomerLevel(request.getCustomerLevel());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setRemark(request.getRemark());
        existing.setOrganizationId(org0);
        existing.setCreateUser(cu0);
        existing.setCreateTime(ct0);
        existing.setDeleted(del0);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        customerMapper.update(existing);
        return existing;
    }

    /** 详情。 */
    public AdCustomerDetailResponse detail(String id, String userId, String orgId) {
        AdCustomer c = requireCustomer(id);
        AdCustomerDetailResponse resp = new AdCustomerDetailResponse();
        resp.setCustomer(c);
        resp.setCustomerLevelLabel(CustomerLevel.labelOf(c.getCustomerLevel()));
        resp.setStatusLabel(CustomerStatus.labelOf(c.getStatus()));
        // 关联订单数：按 customerId 统计同租户、未删除的订单
        long orderCount = extAdOrderMapper.countByCustomerId(id, orgId);
        resp.setOrderCount(orderCount);
        return resp;
    }

    /** 分页列表。 */
    public PagerWithOption<List<AdCustomerListResponse>> page(AdCustomerPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        Page<AdCustomerListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdCustomerListResponse> list = extAdCustomerMapper.pageList(request);

        // 行业字典翻译
        List<AdDict> industryDicts = adDictService.listByDictCode("industry");
        Map<String, String> industryLabelMap = industryDicts.stream()
                .collect(Collectors.toMap(AdDict::getDictValue, AdDict::getDictLabel, (a, b) -> a));

        for (AdCustomerListResponse r : list) {
            r.setCustomerLevelLabel(CustomerLevel.labelOf(r.getCustomerLevel()));
            r.setStatusLabel(CustomerStatus.labelOf(r.getStatus()));
            if (r.getIndustryCode() != null) {
                r.setIndustryLabel(industryLabelMap.getOrDefault(r.getIndustryCode(), r.getIndustryCode()));
            }
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    /** 逻辑删除客户（B-7）。 */
    @OperationLog(module = "CUSTOMER", action = "DELETE", targetId = "#id")
    public void delete(String id, String userId, String orgId) {
        AdCustomer existing = requireCustomer(id);
        existing.setDeleted(1);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        customerMapper.update(existing);
    }

    // ===================== 私有辅助 =====================

    private AdCustomer requireCustomer(String id) {
        AdCustomer c = customerMapper.selectByPrimaryKey(id);
        if (c == null || (c.getDeleted() != null && c.getDeleted() == 1)) {
            throw new GenericException("客户不存在");
        }
        return c;
    }

    private void validate(AdCustomerSaveRequest request) {
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new GenericException("客户名称不能为空");
        }
        if (CustomerLevel.of(request.getCustomerLevel()) == null) {
            throw new GenericException("客户等级不合法(10VIP/20普通/30潜力)");
        }
    }

    // ===================== 角色守卫 =====================

    private void assertRole(String requiredRole) {
        if (requiredRole == null) return;
        if (!hasRole(requiredRole)) {
            throw new GenericException("当前角色无权执行该操作: " + requiredRole);
        }
    }

    private boolean hasRole(String requiredRole) {
        if (requiredRole == null) return true;
        SessionUser user = cn.cordys.security.SessionUtils.getUser();
        if (user == null) return false;
        List<String> roleNames = user.getRoles() == null ? Collections.emptyList()
                : user.getRoles().stream()
                .map(RoleDataScopeDTO::getName)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        boolean isAdmin = roleNames.stream()
                .anyMatch(n -> n.contains("admin") || n.contains("超级") || n.contains("管理员"));
        switch (requiredRole) {
            case ROLE_MEDIA:
                return isAdmin || roleNames.stream()
                        .anyMatch(n -> n.contains("媒体") || n.contains("media") || n.contains("运营"));
            default:
                return false;
        }
    }
}
