package cn.cordys.crm.ad.support.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.support.constants.Priority;
import cn.cordys.crm.ad.support.constants.TicketStatus;
import cn.cordys.crm.ad.support.constants.TicketType;
import cn.cordys.crm.ad.support.domain.AdSupportTicket;
import cn.cordys.crm.ad.support.dto.request.AdSupportTicketPageRequest;
import cn.cordys.crm.ad.support.dto.request.AdSupportTicketSaveRequest;
import cn.cordys.crm.ad.support.dto.response.AdSupportTicketDetailResponse;
import cn.cordys.crm.ad.support.dto.response.AdSupportTicketListResponse;
import cn.cordys.crm.ad.support.mapper.ExtAdSupportTicketMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUser;
import cn.cordys.common.dto.RoleDataScopeDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 广告支持工单服务（M6，V3.1 广告支持工单管理）。
 *
 * <p>工单 CRUD + 分页 + 详情 + 关闭。写操作标注 {@link OperationLog}。</p>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AdSupportTicketService {

    @Resource
    private BaseMapper<AdSupportTicket> ticketMapper;
    @Resource
    private ExtAdSupportTicketMapper extAdSupportTicketMapper;

    private static final String ROLE_MEDIA = "ROLE_MEDIA";

    /** 新建工单。自动生成工单编号。 */
    @OperationLog(module = "SUPPORT", action = "CREATE", targetId = "")
    public AdSupportTicket create(AdSupportTicketSaveRequest request, String userId, String orgId) {
        assertRole(ROLE_MEDIA);
        validate(request);

        AdSupportTicket t = new AdSupportTicket();
        t.setId(IDGenerator.nextStr());
        t.setTicketNo(generateTicketNo());
        t.setTitle(request.getTitle());
        t.setDescription(request.getDescription());
        t.setTicketType(request.getTicketType());
        t.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM.getCode());
        t.setStatus(TicketStatus.OPEN.getCode());
        t.setRelatedOrderId(request.getRelatedOrderId());
        t.setOrganizationId(orgId);
        t.setCreateUser(userId);
        t.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        t.setCreateTime(now);
        t.setUpdateTime(now);
        ticketMapper.insert(t);
        return t;
    }

    /** 编辑工单（指派/解决/状态变更）。 */
    @OperationLog(module = "SUPPORT", action = "UPDATE", targetId = "#request.id")
    public AdSupportTicket update(AdSupportTicketSaveRequest request, String userId, String orgId) {
        assertRole(ROLE_MEDIA);
        if (request.getId() == null || request.getId().isBlank()) {
            throw new GenericException("工单id不能为空");
        }
        AdSupportTicket existing = requireTicket(request.getId());
        String org0 = existing.getOrganizationId();
        String no0 = existing.getTicketNo();
        String cu0 = existing.getCreateUser();
        Long ct0 = existing.getCreateTime();
        Integer del0 = existing.getDeleted();

        if (request.getTitle() != null) existing.setTitle(request.getTitle());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getTicketType() != null) existing.setTicketType(request.getTicketType());
        if (request.getPriority() != null) existing.setPriority(request.getPriority());
        if (request.getRelatedOrderId() != null) existing.setRelatedOrderId(request.getRelatedOrderId());
        if (request.getAssignedTo() != null) existing.setAssignedTo(request.getAssignedTo());
        if (request.getResolution() != null) existing.setResolution(request.getResolution());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
            if (request.getStatus() == TicketStatus.CLOSED.getCode()) {
                existing.setCloseTime(System.currentTimeMillis());
            }
        }
        existing.setOrganizationId(org0);
        existing.setTicketNo(no0);
        existing.setCreateUser(cu0);
        existing.setCreateTime(ct0);
        existing.setDeleted(del0);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(System.currentTimeMillis());
        ticketMapper.update(existing);
        return existing;
    }

    /** 关闭工单。 */
    @OperationLog(module = "SUPPORT", action = "CLOSE", targetId = "#id")
    public AdSupportTicket close(String id, String userId, String orgId) {
        assertRole(ROLE_MEDIA);
        AdSupportTicket t = requireTicket(id);
        if (t.getStatus() == TicketStatus.CLOSED.getCode()) {
            throw new GenericException("工单已关闭");
        }
        long now = System.currentTimeMillis();
        t.setStatus(TicketStatus.CLOSED.getCode());
        t.setCloseTime(now);
        t.setUpdateUser(userId);
        t.setUpdateTime(now);
        ticketMapper.update(t);
        return t;
    }

    /** 详情。 */
    public AdSupportTicketDetailResponse detail(String id, String userId, String orgId) {
        AdSupportTicket t = requireTicket(id);
        AdSupportTicketDetailResponse resp = new AdSupportTicketDetailResponse();
        resp.setTicket(t);
        resp.setTicketTypeLabel(TicketType.labelOf(t.getTicketType()));
        resp.setPriorityLabel(Priority.labelOf(t.getPriority()));
        resp.setStatusLabel(TicketStatus.labelOf(t.getStatus()));
        return resp;
    }

    /** 分页列表。 */
    public PagerWithOption<List<AdSupportTicketListResponse>> page(AdSupportTicketPageRequest request, String userId, String orgId) {
        request.setOrganizationId(orgId);
        Page<AdSupportTicketListResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<AdSupportTicketListResponse> list = extAdSupportTicketMapper.pageList(request);
        for (AdSupportTicketListResponse r : list) {
            r.setTicketTypeLabel(TicketType.labelOf(r.getTicketType()));
            r.setPriorityLabel(Priority.labelOf(r.getPriority()));
            r.setStatusLabel(TicketStatus.labelOf(r.getStatus()));
        }
        return PageUtils.setPageInfoWithOption(page, list, null);
    }

    // ===================== 私有辅助 =====================

    private AdSupportTicket requireTicket(String id) {
        AdSupportTicket t = ticketMapper.selectByPrimaryKey(id);
        if (t == null || (t.getDeleted() != null && t.getDeleted() == 1)) {
            throw new GenericException("工单不存在");
        }
        return t;
    }

    private void validate(AdSupportTicketSaveRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new GenericException("工单标题不能为空");
        }
        if (TicketType.of(request.getTicketType()) == null) {
            throw new GenericException("工单类型不合法(10订单/20付款/30合同/40素材/50其他)");
        }
    }

    /** 工单编号：TK-yyyyMMdd-NNNN。 */
    private String generateTicketNo() {
        Calendar cal = Calendar.getInstance();
        String ymd = String.format("%04d%02d%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        int r = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "TK" + ymd + "-" + r;
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
