package cn.cordys.crm.ad.resource.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.resource.constants.ResourceStatus;
import cn.cordys.crm.ad.resource.constants.ResourceType;
import cn.cordys.crm.ad.resource.domain.AdResource;
import cn.cordys.crm.ad.resource.dto.request.AdResourceSaveRequest;
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
 * 广告资源服务纯逻辑单测（NO @SpringBootTest / NO DB）：通过反射调用私有方法 +
 * Mockito 注入依赖，覆盖创建校验、默认状态等核心契约（M6，V3.1 广告资源管理）。
 */
class AdResourceServiceTest {

    private AdResourceService service;
    private BaseMapper<AdResource> resourceMapper;

    @BeforeEach
    void setUp() throws Exception {
        service = new AdResourceService();
        resourceMapper = mock(BaseMapper.class);
        when(resourceMapper.insert(any())).thenReturn(1);
        setField("resourceMapper", resourceMapper);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = AdResourceService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    /** 调用私有方法并解包 InvocationTargetException，便于 assertThrows 捕获。 */
    private Object invokePrivate(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method m = AdResourceService.class.getDeclaredMethod(methodName, paramTypes);
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

    /** 构造一个含"媒体"角色的 SessionUser 并 mockStatic SessionUtils。 */
    private MockedStatic<SessionUtils> mockMediaRole() {
        MockedStatic<SessionUtils> sessionMock = mockStatic(SessionUtils.class);
        SessionUser mockUser = mock(SessionUser.class);
        RoleDataScopeDTO roleDto = mock(RoleDataScopeDTO.class);
        when(roleDto.getName()).thenReturn("媒体运营");
        when(mockUser.getRoles()).thenReturn(List.of(roleDto));
        sessionMock.when(SessionUtils::getUser).thenReturn(mockUser);
        return sessionMock;
    }

    // ==================== validate 私有方法（纯逻辑，无需 Mock） ====================

    @Test
    void validate_blankResourceName_throws() {
        AdResourceSaveRequest req = new AdResourceSaveRequest();
        req.setResourceName("  ");
        req.setResourceType(ResourceType.ONLINE_MEDIA.getCode());

        GenericException ex = assertThrows(GenericException.class,
                () -> invokePrivate("validate", new Class[]{AdResourceSaveRequest.class}, req));
        assertTrue(ex.getMessage().contains("资源名称不能为空"));
    }

    @Test
    void validate_nullResourceName_throws() {
        AdResourceSaveRequest req = new AdResourceSaveRequest();
        req.setResourceName(null);
        req.setResourceType(ResourceType.ONLINE_MEDIA.getCode());

        GenericException ex = assertThrows(GenericException.class,
                () -> invokePrivate("validate", new Class[]{AdResourceSaveRequest.class}, req));
        assertTrue(ex.getMessage().contains("资源名称不能为空"));
    }

    @Test
    void validate_invalidResourceType_throws() {
        AdResourceSaveRequest req = new AdResourceSaveRequest();
        req.setResourceName("测试资源");
        req.setResourceType(99);  // 无效类型

        GenericException ex = assertThrows(GenericException.class,
                () -> invokePrivate("validate", new Class[]{AdResourceSaveRequest.class}, req));
        assertTrue(ex.getMessage().contains("资源类型不合法"));
    }

    @Test
    void validate_nullResourceType_throws() {
        AdResourceSaveRequest req = new AdResourceSaveRequest();
        req.setResourceName("测试资源");
        req.setResourceType(null);

        GenericException ex = assertThrows(GenericException.class,
                () -> invokePrivate("validate", new Class[]{AdResourceSaveRequest.class}, req));
        assertTrue(ex.getMessage().contains("资源类型不合法"));
    }

    @Test
    void validate_validRequest_passes() throws Exception {
        AdResourceSaveRequest req = new AdResourceSaveRequest();
        req.setResourceName("测试资源");
        req.setResourceType(ResourceType.TV.getCode());

        // 不应抛出异常
        invokePrivate("validate", new Class[]{AdResourceSaveRequest.class}, req);
    }

    // ==================== create 公共方法 ====================

    @Test
    void create_nullStatus_defaultsToAvailable() throws Exception {
        AdResourceSaveRequest req = new AdResourceSaveRequest();
        req.setResourceName("新资源");
        req.setResourceType(ResourceType.ONLINE_MEDIA.getCode());
        req.setStatus(null);  // 未设置状态

        try (MockedStatic<SessionUtils> sessionMock = mockMediaRole();
             MockedStatic<IDGenerator> idMock = mockStatic(IDGenerator.class)) {

            idMock.when(IDGenerator::nextStr).thenReturn("res-001");

            AdResource result = service.create(req, "user-1", "org-1");

            assertNotNull(result);
            assertEquals("新资源", result.getName());
            assertEquals(ResourceType.ONLINE_MEDIA.getCode(), result.getResourceType());
            assertEquals(ResourceStatus.AVAILABLE.getCode(), result.getStatus(),
                    "未传状态时应默认为 AVAILABLE(0)");
            assertEquals("user-1", result.getCreateUser());
            assertEquals("org-1", result.getOrganizationId());
            verify(resourceMapper, times(1)).insert(any(AdResource.class));
        }
    }

    @Test
    void create_explicitStatus_preserved() throws Exception {
        AdResourceSaveRequest req = new AdResourceSaveRequest();
        req.setResourceName("维护中的资源");
        req.setResourceType(ResourceType.RADIO.getCode());
        req.setStatus(ResourceStatus.MAINTENANCE.getCode());

        try (MockedStatic<SessionUtils> sessionMock = mockMediaRole();
             MockedStatic<IDGenerator> idMock = mockStatic(IDGenerator.class)) {

            idMock.when(IDGenerator::nextStr).thenReturn("res-002");

            AdResource result = service.create(req, "user-2", "org-2");

            assertEquals(ResourceStatus.MAINTENANCE.getCode(), result.getStatus(),
                    "显式传入 MAINTENANCE(20) 时应保留");
        }
    }

    // ==================== update 公共方法 ====================

    @Test
    void update_blankId_throws() {
        AdResourceSaveRequest req = new AdResourceSaveRequest();
        req.setId("  ");

        try (MockedStatic<SessionUtils> sessionMock = mockMediaRole()) {
            GenericException ex = assertThrows(GenericException.class,
                    () -> service.update(req, "user-1", "org-1"));
            assertTrue(ex.getMessage().contains("资源id不能为空"));
        }
    }

    @Test
    void update_nullId_throws() {
        AdResourceSaveRequest req = new AdResourceSaveRequest();
        req.setId(null);

        try (MockedStatic<SessionUtils> sessionMock = mockMediaRole()) {
            GenericException ex = assertThrows(GenericException.class,
                    () -> service.update(req, "user-1", "org-1"));
            assertTrue(ex.getMessage().contains("资源id不能为空"));
        }
    }
}
