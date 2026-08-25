package cn.cordys.crm.ad.support.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.support.constants.Priority;
import cn.cordys.crm.ad.support.constants.TicketStatus;
import cn.cordys.crm.ad.support.constants.TicketType;
import cn.cordys.crm.ad.support.domain.AdSupportTicket;
import cn.cordys.crm.ad.support.dto.request.AdSupportTicketSaveRequest;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUser;
import cn.cordys.security.SessionUtils;
import cn.cordys.common.dto.RoleDataScopeDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 广告支持工单服务纯逻辑单测（NO @SpringBootTest / NO DB）：通过反射调用私有方法 +
 * Mockito 注入依赖，覆盖校验、工单编号生成、状态转换守卫等核心契约（M6，V3.1 广告支持工单管理）。
 */
class AdSupportTicketServiceTest {

    private AdSupportTicketService service;
    private BaseMapper<AdSupportTicket> ticketMapper;

    @BeforeEach
    void setUp() throws Exception {
        service = new AdSupportTicketService();
        ticketMapper = mock(BaseMapper.class);
        when(ticketMapper.insert(any())).thenReturn(1);
        when(ticketMapper.update(any())).thenReturn(1);
        setField("ticketMapper", ticketMapper);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = AdSupportTicketService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    /** 调用私有方法并解包 InvocationTargetException。 */
    private Object invokePrivate(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method m = AdSupportTicketService.class.getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        try {
            return m.invoke(service, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            if (cause instanceof Exception ex) throw ex;
            throw e;
        }
    }

    private MockedStatic<SessionUtils> mockMediaRole() {
        MockedStatic<SessionUtils> sessionMock = mockStatic(SessionUtils.class);
        SessionUser mockUser = mock(SessionUser.class);
        RoleDataScopeDTO roleDto = mock(RoleDataScopeDTO.class);
        when(roleDto.getName()).thenReturn("运营");
        when(mockUser.getRoles()).thenReturn(List.of(roleDto));
        sessionMock.when(SessionUtils::getUser).thenReturn(mockUser);
        return sessionMock;
    }

    // ==================== validate 私有方法（纯逻辑，无需 Mock） ====================

    @Test
    void validate_blankTitle_throws() {
        AdSupportTicketSaveRequest req = new AdSupportTicketSaveRequest();
        req.setTitle("  ");
        req.setTicketType(TicketType.ORDER_ISSUE.getCode());

        GenericException ex = assertThrows(GenericException.class,
                () -> invokePrivate("validate", new Class[]{AdSupportTicketSaveRequest.class}, req));
        assertTrue(ex.getMessage().contains("工单标题不能为空"));
    }

    @Test
    void validate_nullTitle_throws() {
        AdSupportTicketSaveRequest req = new AdSupportTicketSaveRequest();
        req.setTitle(null);
        req.setTicketType(TicketType.ORDER_ISSUE.getCode());

        GenericException ex = assertThrows(GenericException.class,
                () -> invokePrivate("validate", new Class[]{AdSupportTicketSaveRequest.class}, req));
        assertTrue(ex.getMessage().contains("工单标题不能为空"));
    }

    @Test
    void validate_invalidTicketType_throws() {
        AdSupportTicketSaveRequest req = new AdSupportTicketSaveRequest();
        req.setTitle("测试工单");
        req.setTicketType(99);  // 无效类型

        GenericException ex = assertThrows(GenericException.class,
                () -> invokePrivate("validate", new Class[]{AdSupportTicketSaveRequest.class}, req));
        assertTrue(ex.getMessage().contains("工单类型不合法"));
    }

    @Test
    void validate_validRequest_passes() throws Exception {
        AdSupportTicketSaveRequest req = new AdSupportTicketSaveRequest();
        req.setTitle("测试工单");
        req.setTicketType(TicketType.OTHER.getCode());

        // 不应抛出异常
        invokePrivate("validate", new Class[]{AdSupportTicketSaveRequest.class}, req);
    }

    // ==================== generateTicketNo 私有方法 ====================

    @Test
    void generateTicketNo_matchesPattern() throws Exception {
        String ticketNo = (String) invokePrivate("generateTicketNo", new Class[]{});

        assertNotNull(ticketNo);
        // 格式：TK-yyyyMMdd-NNNN，例如 TK20250321-1234
        assertTrue(ticketNo.startsWith("TK"), "应以 TK 开头: " + ticketNo);
        assertTrue(ticketNo.contains("-"), "应包含分隔符: " + ticketNo);

        String[] parts = ticketNo.split("-");
        assertEquals(2, parts.length, "应由一个 '-' 分隔: " + ticketNo);
        assertEquals(10, parts[0].length(), "前缀应为 TK+8位日期=10位: " + ticketNo);

        // 日期部分：TK + 8位数字 = TKyyyyMMdd
        String datePart = parts[0].substring(2); // 去掉"TK"
        assertEquals(8, datePart.length(), "日期部分应为8位: " + ticketNo);
        assertTrue(datePart.matches("\\d{8}"), "日期部分应为数字: " + ticketNo);

        // 序号部分：4位数字
        assertEquals(4, parts[1].length(), "序号应为4位: " + ticketNo);
        assertTrue(parts[1].matches("\\d{4}"), "序号应为数字: " + ticketNo);

        int seq = Integer.parseInt(parts[1]);
        assertTrue(seq >= 1000 && seq <= 9999, "序号应在 1000~9999 之间: " + seq);
    }

    // ==================== create 公共方法 ====================

    @Test
    void create_defaultStatusIsOpen() throws Exception {
        AdSupportTicketSaveRequest req = new AdSupportTicketSaveRequest();
        req.setTitle("测试工单");
        req.setTicketType(TicketType.PAYMENT_ISSUE.getCode());
        req.setPriority(null);  // 不设置优先级

        try (MockedStatic<SessionUtils> sessionMock = mockMediaRole();
             MockedStatic<IDGenerator> idMock = mockStatic(IDGenerator.class)) {

            idMock.when(IDGenerator::nextStr).thenReturn("ticket-001");

            AdSupportTicket result = service.create(req, "user-1", "org-1");

            assertNotNull(result);
            assertEquals("测试工单", result.getTitle());
            assertEquals(TicketStatus.OPEN.getCode(), result.getStatus(),
                    "新建工单状态应为 OPEN(0)");
            assertEquals(Priority.MEDIUM.getCode(), result.getPriority(),
                    "未传优先级时应默认为 MEDIUM(20)");
            assertNotNull(result.getTicketNo(), "应自动生成工单编号");
            assertTrue(result.getTicketNo().startsWith("TK"), "工单编号应以 TK 开头");
            verify(ticketMapper, times(1)).insert(any(AdSupportTicket.class));
        }
    }

    @Test
    void create_explicitPriority_preserved() throws Exception {
        AdSupportTicketSaveRequest req = new AdSupportTicketSaveRequest();
        req.setTitle("紧急工单");
        req.setTicketType(TicketType.ORDER_ISSUE.getCode());
        req.setPriority(Priority.URGENT.getCode());

        try (MockedStatic<SessionUtils> sessionMock = mockMediaRole();
             MockedStatic<IDGenerator> idMock = mockStatic(IDGenerator.class)) {

            idMock.when(IDGenerator::nextStr).thenReturn("ticket-002");

            AdSupportTicket result = service.create(req, "user-1", "org-1");

            assertEquals(Priority.URGENT.getCode(), result.getPriority(),
                    "显式传入 URGENT(40) 时应保留");
        }
    }

    // ==================== close 公共方法 ====================

    @Test
    void close_alreadyClosed_throws() throws Exception {
        AdSupportTicket alreadyClosed = new AdSupportTicket();
        alreadyClosed.setId("ticket-closed");
        alreadyClosed.setStatus(TicketStatus.CLOSED.getCode());
        alreadyClosed.setTicketNo("TK20250301-1000");
        alreadyClosed.setDeleted(0);

        when(ticketMapper.selectByPrimaryKey("ticket-closed")).thenReturn(alreadyClosed);

        try (MockedStatic<SessionUtils> sessionMock = mockMediaRole()) {
            GenericException ex = assertThrows(GenericException.class,
                    () -> service.close("ticket-closed", "user-1", "org-1"));
            assertTrue(ex.getMessage().contains("工单已关闭"));
        }
    }

    @Test
    void close_openTicket_succeeds() throws Exception {
        AdSupportTicket openTicket = new AdSupportTicket();
        openTicket.setId("ticket-open");
        openTicket.setStatus(TicketStatus.OPEN.getCode());
        openTicket.setTicketNo("TK20250301-2000");
        openTicket.setDeleted(0);

        when(ticketMapper.selectByPrimaryKey("ticket-open")).thenReturn(openTicket);

        try (MockedStatic<SessionUtils> sessionMock = mockMediaRole()) {
            AdSupportTicket result = service.close("ticket-open", "user-1", "org-1");

            assertEquals(TicketStatus.CLOSED.getCode(), result.getStatus(),
                    "关闭后状态应为 CLOSED(30)");
            assertNotNull(result.getCloseTime(), "关闭时应设置关闭时间");
            assertEquals("user-1", result.getUpdateUser());
            verify(ticketMapper, times(1)).update(any(AdSupportTicket.class));
        }
    }
}
