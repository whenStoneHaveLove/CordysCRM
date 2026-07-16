package cn.cordys.crm.ad.support.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 广告支持工单枚举纯逻辑单测（NO Spring）：锁定 {@link TicketType}、{@link Priority}、
 * {@link TicketStatus} 的 code/label/of/labelOf 契约（M6，V3.1 广告支持工单管理）。
 */
class SupportEnumTest {

    // ==================== TicketType ====================

    @Test
    void ticketType_allFiveCodes() {
        assertEquals(10, TicketType.ORDER_ISSUE.getCode());
        assertEquals(20, TicketType.PAYMENT_ISSUE.getCode());
        assertEquals(30, TicketType.CONTRACT_ISSUE.getCode());
        assertEquals(40, TicketType.MATERIAL_ISSUE.getCode());
        assertEquals(50, TicketType.OTHER.getCode());
    }

    @Test
    void ticketType_allFiveLabels() {
        assertEquals("订单问题", TicketType.ORDER_ISSUE.getLabel());
        assertEquals("付款问题", TicketType.PAYMENT_ISSUE.getLabel());
        assertEquals("合同问题", TicketType.CONTRACT_ISSUE.getLabel());
        assertEquals("素材问题", TicketType.MATERIAL_ISSUE.getLabel());
        assertEquals("其他", TicketType.OTHER.getLabel());
    }

    @Test
    void ticketType_of_validCodes() {
        assertEquals(TicketType.ORDER_ISSUE, TicketType.of(10));
        assertEquals(TicketType.PAYMENT_ISSUE, TicketType.of(20));
        assertEquals(TicketType.CONTRACT_ISSUE, TicketType.of(30));
        assertEquals(TicketType.MATERIAL_ISSUE, TicketType.of(40));
        assertEquals(TicketType.OTHER, TicketType.of(50));
    }

    @Test
    void ticketType_of_invalidCode_returnsNull() {
        assertNull(TicketType.of(99));
        assertNull(TicketType.of(0));
        assertNull(TicketType.of(-1));
    }

    @Test
    void ticketType_of_null_returnsNull() {
        assertNull(TicketType.of(null));
    }

    @Test
    void ticketType_labelOf_validAndInvalid() {
        assertEquals("订单问题", TicketType.labelOf(10));
        assertEquals("其他", TicketType.labelOf(50));
        assertEquals("99", TicketType.labelOf(99));
        assertNull(TicketType.labelOf(null));
    }

    @Test
    void ticketType_valuesCount() {
        assertEquals(5, TicketType.values().length);
    }

    // ==================== Priority ====================

    @Test
    void priority_allFourCodes() {
        assertEquals(10, Priority.LOW.getCode());
        assertEquals(20, Priority.MEDIUM.getCode());
        assertEquals(30, Priority.HIGH.getCode());
        assertEquals(40, Priority.URGENT.getCode());
    }

    @Test
    void priority_allFourLabels() {
        assertEquals("低", Priority.LOW.getLabel());
        assertEquals("中", Priority.MEDIUM.getLabel());
        assertEquals("高", Priority.HIGH.getLabel());
        assertEquals("紧急", Priority.URGENT.getLabel());
    }

    @Test
    void priority_ordering_byCode() {
        // 代码越大表示优先级越高（LOW=10 < MEDIUM=20 < HIGH=30 < URGENT=40）
        assertTrue(Priority.LOW.getCode() < Priority.MEDIUM.getCode());
        assertTrue(Priority.MEDIUM.getCode() < Priority.HIGH.getCode());
        assertTrue(Priority.HIGH.getCode() < Priority.URGENT.getCode());
    }

    @Test
    void priority_of_validCodes() {
        assertEquals(Priority.LOW, Priority.of(10));
        assertEquals(Priority.MEDIUM, Priority.of(20));
        assertEquals(Priority.HIGH, Priority.of(30));
        assertEquals(Priority.URGENT, Priority.of(40));
    }

    @Test
    void priority_of_invalidCode_returnsNull() {
        assertNull(Priority.of(99));
        assertNull(Priority.of(0));
    }

    @Test
    void priority_of_null_returnsNull() {
        assertNull(Priority.of(null));
    }

    @Test
    void priority_labelOf_validAndInvalid() {
        assertEquals("低", Priority.labelOf(10));
        assertEquals("中", Priority.labelOf(20));
        assertEquals("高", Priority.labelOf(30));
        assertEquals("紧急", Priority.labelOf(40));
        assertEquals("99", Priority.labelOf(99));
        assertNull(Priority.labelOf(null));
    }

    @Test
    void priority_valuesCount() {
        assertEquals(4, Priority.values().length);
    }

    // ==================== TicketStatus ====================

    @Test
    void ticketStatus_allFourCodes() {
        assertEquals(0, TicketStatus.OPEN.getCode());
        assertEquals(10, TicketStatus.IN_PROGRESS.getCode());
        assertEquals(20, TicketStatus.RESOLVED.getCode());
        assertEquals(30, TicketStatus.CLOSED.getCode());
    }

    @Test
    void ticketStatus_allFourLabels() {
        assertEquals("待处理", TicketStatus.OPEN.getLabel());
        assertEquals("处理中", TicketStatus.IN_PROGRESS.getLabel());
        assertEquals("已解决", TicketStatus.RESOLVED.getLabel());
        assertEquals("已关闭", TicketStatus.CLOSED.getLabel());
    }

    @Test
    void ticketStatus_forwardFlow_order() {
        // 正向流转：OPEN(0) → IN_PROGRESS(10) → RESOLVED(20) → CLOSED(30)
        assertTrue(TicketStatus.OPEN.getCode() < TicketStatus.IN_PROGRESS.getCode());
        assertTrue(TicketStatus.IN_PROGRESS.getCode() < TicketStatus.RESOLVED.getCode());
        assertTrue(TicketStatus.RESOLVED.getCode() < TicketStatus.CLOSED.getCode());
    }

    @Test
    void ticketStatus_of_validCodes() {
        assertEquals(TicketStatus.OPEN, TicketStatus.of(0));
        assertEquals(TicketStatus.IN_PROGRESS, TicketStatus.of(10));
        assertEquals(TicketStatus.RESOLVED, TicketStatus.of(20));
        assertEquals(TicketStatus.CLOSED, TicketStatus.of(30));
    }

    @Test
    void ticketStatus_of_invalidCode_returnsNull() {
        assertNull(TicketStatus.of(99));
        assertNull(TicketStatus.of(5));
    }

    @Test
    void ticketStatus_of_null_returnsNull() {
        assertNull(TicketStatus.of(null));
    }

    @Test
    void ticketStatus_labelOf_validAndInvalid() {
        assertEquals("待处理", TicketStatus.labelOf(0));
        assertEquals("处理中", TicketStatus.labelOf(10));
        assertEquals("已解决", TicketStatus.labelOf(20));
        assertEquals("已关闭", TicketStatus.labelOf(30));
        assertEquals("99", TicketStatus.labelOf(99));
        assertNull(TicketStatus.labelOf(null));
    }

    @Test
    void ticketStatus_valuesCount() {
        assertEquals(4, TicketStatus.values().length);
    }
}
