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
    void resourceType_allFiveCodes() {
        assertEquals(10, ResourceType.ONLINE_MEDIA.getCode());
        assertEquals(20, ResourceType.OFFLINE_BILLBOARD.getCode());
        assertEquals(30, ResourceType.TV.getCode());
        assertEquals(40, ResourceType.RADIO.getCode());
        assertEquals(50, ResourceType.PRINT.getCode());
    }

    @Test
    void resourceType_allFiveLabels() {
        assertEquals("线上媒体", ResourceType.ONLINE_MEDIA.getLabel());
        assertEquals("线下广告牌", ResourceType.OFFLINE_BILLBOARD.getLabel());
        assertEquals("电视", ResourceType.TV.getLabel());
        assertEquals("广播", ResourceType.RADIO.getLabel());
        assertEquals("印刷", ResourceType.PRINT.getLabel());
    }

    @Test
    void resourceType_of_validCodes() {
        assertEquals(ResourceType.ONLINE_MEDIA, ResourceType.of(10));
        assertEquals(ResourceType.OFFLINE_BILLBOARD, ResourceType.of(20));
        assertEquals(ResourceType.TV, ResourceType.of(30));
        assertEquals(ResourceType.RADIO, ResourceType.of(40));
        assertEquals(ResourceType.PRINT, ResourceType.of(50));
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
        assertEquals("线上媒体", ResourceType.labelOf(10));
        assertEquals("印刷", ResourceType.labelOf(50));
        assertEquals("99", ResourceType.labelOf(99));  // unknown → String.valueOf(code)
        assertNull(ResourceType.labelOf(null));         // null → null
    }

    @Test
    void resourceType_valuesCount() {
        assertEquals(5, ResourceType.values().length);
    }

    // ==================== ResourceStatus ====================

    @Test
    void resourceStatus_allThreeCodes() {
        assertEquals(0, ResourceStatus.AVAILABLE.getCode());
        assertEquals(10, ResourceStatus.OCCUPIED.getCode());
        assertEquals(20, ResourceStatus.MAINTENANCE.getCode());
    }

    @Test
    void resourceStatus_allThreeLabels() {
        assertEquals("可用", ResourceStatus.AVAILABLE.getLabel());
        assertEquals("已占用", ResourceStatus.OCCUPIED.getLabel());
        assertEquals("维护中", ResourceStatus.MAINTENANCE.getLabel());
    }

    @Test
    void resourceStatus_of_validCodes() {
        assertEquals(ResourceStatus.AVAILABLE, ResourceStatus.of(0));
        assertEquals(ResourceStatus.OCCUPIED, ResourceStatus.of(10));
        assertEquals(ResourceStatus.MAINTENANCE, ResourceStatus.of(20));
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
        assertEquals("可用", ResourceStatus.labelOf(0));
        assertEquals("已占用", ResourceStatus.labelOf(10));
        assertEquals("维护中", ResourceStatus.labelOf(20));
        assertEquals("99", ResourceStatus.labelOf(99));
        assertNull(ResourceStatus.labelOf(null));
    }

    @Test
    void resourceStatus_valuesCount() {
        assertEquals(3, ResourceStatus.values().length);
    }
}
