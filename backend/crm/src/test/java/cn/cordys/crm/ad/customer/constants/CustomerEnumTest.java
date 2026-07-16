package cn.cordys.crm.ad.customer.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 广告客户枚举纯逻辑单测（NO Spring）：锁定 {@link CustomerLevel} 与 {@link CustomerStatus}
 * 的 code/label/of/labelOf 契约（M6，V3.1 广告客户管理）。
 */
class CustomerEnumTest {

    // ==================== CustomerLevel ====================

    @Test
    void customerLevel_allCodes() {
        assertEquals(10, CustomerLevel.VIP.getCode());
        assertEquals(20, CustomerLevel.REGULAR.getCode());
        assertEquals(30, CustomerLevel.POTENTIAL.getCode());
    }

    @Test
    void customerLevel_allLabels() {
        assertEquals("VIP", CustomerLevel.VIP.getLabel());
        assertEquals("普通", CustomerLevel.REGULAR.getLabel());
        assertEquals("潜力", CustomerLevel.POTENTIAL.getLabel());
    }

    @Test
    void customerLevel_of_validCodes() {
        assertEquals(CustomerLevel.VIP, CustomerLevel.of(10));
        assertEquals(CustomerLevel.REGULAR, CustomerLevel.of(20));
        assertEquals(CustomerLevel.POTENTIAL, CustomerLevel.of(30));
    }

    @Test
    void customerLevel_of_invalidCode_returnsNull() {
        assertNull(CustomerLevel.of(99));
        assertNull(CustomerLevel.of(0));
        assertNull(CustomerLevel.of(-1));
    }

    @Test
    void customerLevel_of_null_returnsNull() {
        assertNull(CustomerLevel.of(null));
    }

    @Test
    void customerLevel_labelOf_validAndInvalid() {
        assertEquals("VIP", CustomerLevel.labelOf(10));
        assertEquals("普通", CustomerLevel.labelOf(20));
        assertEquals("潜力", CustomerLevel.labelOf(30));
        assertEquals("99", CustomerLevel.labelOf(99));
        assertNull(CustomerLevel.labelOf(null));
    }

    @Test
    void customerLevel_valuesCount() {
        assertEquals(3, CustomerLevel.values().length);
    }

    // ==================== CustomerStatus ====================

    @Test
    void customerStatus_allCodes() {
        assertEquals(0, CustomerStatus.ACTIVE.getCode());
        assertEquals(10, CustomerStatus.INACTIVE.getCode());
        assertEquals(20, CustomerStatus.BLACKLIST.getCode());
    }

    @Test
    void customerStatus_allLabels() {
        assertEquals("活跃", CustomerStatus.ACTIVE.getLabel());
        assertEquals("非活跃", CustomerStatus.INACTIVE.getLabel());
        assertEquals("黑名单", CustomerStatus.BLACKLIST.getLabel());
    }

    @Test
    void customerStatus_of_validCodes() {
        assertEquals(CustomerStatus.ACTIVE, CustomerStatus.of(0));
        assertEquals(CustomerStatus.INACTIVE, CustomerStatus.of(10));
        assertEquals(CustomerStatus.BLACKLIST, CustomerStatus.of(20));
    }

    @Test
    void customerStatus_of_invalidCode_returnsNull() {
        assertNull(CustomerStatus.of(99));
        assertNull(CustomerStatus.of(5));
    }

    @Test
    void customerStatus_of_null_returnsNull() {
        assertNull(CustomerStatus.of(null));
    }

    @Test
    void customerStatus_labelOf_validAndInvalid() {
        assertEquals("活跃", CustomerStatus.labelOf(0));
        assertEquals("非活跃", CustomerStatus.labelOf(10));
        assertEquals("黑名单", CustomerStatus.labelOf(20));
        assertEquals("99", CustomerStatus.labelOf(99));
        assertNull(CustomerStatus.labelOf(null));
    }

    @Test
    void customerStatus_valuesCount() {
        assertEquals(3, CustomerStatus.values().length);
    }
}
