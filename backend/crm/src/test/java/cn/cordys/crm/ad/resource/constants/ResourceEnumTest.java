package cn.cordys.crm.ad.resource.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 广告资源枚举纯逻辑单测（NO Spring）：锁定 {@link ResourceType} 与 {@link ResourceStatus}
 * 的 code/label/of/labelOf 契约（M6，V3.1 广告资源管理）。
 */
class ResourceEnumTest {

    // ==================== ResourceType ====================

    @Test
    void resourceType_allCodes() {
        assertEquals(10, ResourceType.UPSTREAM_AGENT.getCode());
        assertEquals(20, ResourceType.DOWNSTREAM_MEDIA.getCode());
    }

    @Test
    void resourceType_allLabels() {
        assertEquals("上游代理", ResourceType.UPSTREAM_AGENT.getLabel());
        assertEquals("下游媒体", ResourceType.DOWNSTREAM_MEDIA.getLabel());
    }

    @Test
    void resourceType_of_validCodes() {
        assertEquals(ResourceType.UPSTREAM_AGENT, ResourceType.of(10));
        assertEquals(ResourceType.DOWNSTREAM_MEDIA, ResourceType.of(20));
    }

    @Test
    void resourceType_of_invalidCode_returnsNull() {
        assertNull(ResourceType.of(99));
        assertNull(ResourceType.of(0));
        assertNull(ResourceType.of(-1));
    }

    @Test
    void resourceType_of_null_returnsNull() {
        assertNull(ResourceType.of(null));
    }

    @Test
    void resourceType_labelOf_validAndInvalid() {
        assertEquals("上游代理", ResourceType.labelOf(10));
        assertEquals("下游媒体", ResourceType.labelOf(20));
        assertEquals("99", ResourceType.labelOf(99));  // unknown → String.valueOf(code)
        assertNull(ResourceType.labelOf(null));         // null → null
    }

    @Test
    void resourceType_valuesCount() {
        assertEquals(2, ResourceType.values().length);
    }

    // ==================== ResourceStatus ====================

    @Test
    void resourceStatus_allCodes() {
        assertEquals(10, ResourceStatus.NORMAL.getCode());
        assertEquals(20, ResourceStatus.DISABLED.getCode());
    }

    @Test
    void resourceStatus_allLabels() {
        assertEquals("正常", ResourceStatus.NORMAL.getLabel());
        assertEquals("停用", ResourceStatus.DISABLED.getLabel());
    }

    @Test
    void resourceStatus_of_validCodes() {
        assertEquals(ResourceStatus.NORMAL, ResourceStatus.of(10));
        assertEquals(ResourceStatus.DISABLED, ResourceStatus.of(20));
    }

    @Test
    void resourceStatus_of_invalidCode_returnsNull() {
        assertNull(ResourceStatus.of(99));
        assertNull(ResourceStatus.of(5));
    }

    @Test
    void resourceStatus_of_null_returnsNull() {
        assertNull(ResourceStatus.of(null));
    }

    @Test
    void resourceStatus_labelOf_validAndInvalid() {
        assertEquals("正常", ResourceStatus.labelOf(10));
        assertEquals("停用", ResourceStatus.labelOf(20));
        assertEquals("99", ResourceStatus.labelOf(99));
        assertNull(ResourceStatus.labelOf(null));
    }

    @Test
    void resourceStatus_valuesCount() {
        assertEquals(2, ResourceStatus.values().length);
    }
}
