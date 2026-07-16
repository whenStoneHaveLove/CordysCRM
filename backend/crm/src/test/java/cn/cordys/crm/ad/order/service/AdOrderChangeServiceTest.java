package cn.cordys.crm.ad.order.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.domain.AdOrderLog;
import cn.cordys.mybatis.BaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

/**
 * 改单服务纯逻辑单测（NO @SpringBootTest / NO DB）：通过反射调用私有纯逻辑方法，
 * 依赖以 Mockito 注入。覆盖：
 *  - L-24 变更字段白名单守卫（禁止 order_type）；
 *  - 执行时快照应用 + 金额重算（applyAfter + AdAmountCalculator.computeAmounts）；
 *  - L-04 资金侧处理（应退款/待补收/待补开/红冲标记）。
 *
 * <p>不启动 Spring，不连库；角色守卫(SessionUtils)与父单状态机联动不在本测试范围。</p>
 */
class AdOrderChangeServiceTest {

    private AdOrderChangeService service;
    private BaseMapper<AdOrderLog> orderLogMapper;

    @BeforeEach
    void setUp() throws Exception {
        service = new AdOrderChangeService();
        orderLogMapper = mock(BaseMapper.class);
        when(orderLogMapper.insert(any())).thenReturn(1);
        setField("orderLogMapper", orderLogMapper);
        setField("amountCalculator", new AdAmountCalculator());
    }

    private void setField(String name, Object value) throws Exception {
        Field f = AdOrderChangeService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    /** 调用私有方法，并把目标异常（如 GenericException）解包后原样抛出，便于 assertThrows 捕获。 */
    private Object invoke(String method, Class<?>[] types, Object... args) throws Exception {
        Method m = AdOrderChangeService.class.getDeclaredMethod(method, types);
        m.setAccessible(true);
        try {
            return m.invoke(service, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw e;
        }
    }

    private void assertAmount(String msg, BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), msg);
    }

    /* ---------- L-24 变更字段守卫 ---------- */

    @Test
    void l24_orderTypeRejected() {
        List<String> fields = List.of("totalAmount", "orderType");
        assertThrows(GenericException.class,
                () -> invoke("validateChangeFields", new Class[]{List.class}, fields));
    }

    @Test
    void l24_order_type_snakeRejected() {
        List<String> fields = List.of("order_type");
        assertThrows(GenericException.class,
                () -> invoke("validateChangeFields", new Class[]{List.class}, fields));
    }

    @Test
    void l24_unknownFieldRejected() {
        List<String> fields = List.of("someUnsupportedField");
        assertThrows(GenericException.class,
                () -> invoke("validateChangeFields", new Class[]{List.class}, fields));
    }

    @Test
    void l24_whitelistedFieldAccepted() throws Exception {
        List<String> fields = List.of("totalAmount", "rebateValue", "orderName");
        invoke("validateChangeFields", new Class[]{List.class}, fields);
    }

    /* ---------- 执行时快照应用 + 金额重算 ---------- */

    @Test
    void recalc_applyAfterBigDecimalTypes() throws Exception {
        AdOrder order = new AdOrder();
        order.setTotalAmount(new BigDecimal("10000"));
        order.setNoRebateAmount(BigDecimal.ZERO);
        order.setRebateMode(10);
        order.setRebateValue(new BigDecimal("10")); // 10%

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("totalAmount", new BigDecimal("20000"));
        after.put("rebateValue", new BigDecimal("20")); // 20%
        invoke("applyAfter", new Class[]{AdOrder.class, Map.class}, order, after);

        assertAmount("应用后 totalAmount", new BigDecimal("20000"), order.getTotalAmount());
        assertAmount("应用后 rebateValue", new BigDecimal("20"), order.getRebateValue());

        new AdAmountCalculator().computeAmounts(order);
        // 20000 - 20000*20% = 16000
        assertAmount("重算后应收=16000", new BigDecimal("16000.00"), order.getReceivableAmount());
    }

    @Test
    void recalc_applyAfterStringAndIntTypes() throws Exception {
        AdOrder order = new AdOrder();
        order.setTotalAmount(new BigDecimal("10000"));
        order.setNoRebateAmount(BigDecimal.ZERO);
        order.setRebateMode(10);
        order.setRebateValue(new BigDecimal("10"));

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("totalAmount", "30000");   // String -> toBigDecimal
        after.put("rebateMode", 10);         // Integer -> toInt
        after.put("rebateValue", "15");      // String -> toBigDecimal (15%)
        invoke("applyAfter", new Class[]{AdOrder.class, Map.class}, order, after);

        assertAmount("应用后 totalAmount", new BigDecimal("30000"), order.getTotalAmount());
        assertEquals(10, order.getRebateMode());
        assertAmount("应用后 rebateValue", new BigDecimal("15"), order.getRebateValue());

        new AdAmountCalculator().computeAmounts(order);
        // 30000 - 30000*15% = 25500
        assertAmount("重算后应收=25500", new BigDecimal("25500.00"), order.getReceivableAmount());
    }

    /* ---------- L-04 资金侧 ---------- */

    @Test
    void l04_refundAndSupplementInvoice() throws Exception {
        AdOrder order = new AdOrder();
        order.setReceivableAmount(new BigDecimal("9000"));
        order.setReceivedAmount(new BigDecimal("10000")); // 多收 1000
        order.setInvoicedAmount(new BigDecimal("8000"));  // 少开 1000

        // handleMoneySide 内部用 IDGenerator.nextStr() 生成日志主键（静态、通常由 Spring 初始化）；
        // 纯单测下以 mockStatic 桩接，避免依赖 Spring。
        try (MockedStatic<IDGenerator> idGen = mockStatic(IDGenerator.class)) {
            idGen.when(IDGenerator::nextStr).thenReturn("test-log-id");
            invoke("handleMoneySide", new Class[]{AdOrder.class, String.class, String.class}, order, "u1", "o1");
        }

        // 已开票(8000) <= 应收(9000) -> 不红冲
        assertEquals(0, order.getNeedsRedInvoice(), "不应置红冲");
        ArgumentCaptor<AdOrderLog> captor = ArgumentCaptor.forClass(AdOrderLog.class);
        verify(orderLogMapper, times(1)).insert(captor.capture());
        String note = captor.getValue().getAfterValue();
        assertTrue(note.contains("应退款=1000"), "应记录应退款: " + note);
        assertTrue(note.contains("待补开=1000"), "应记录待补开: " + note);
    }

    @Test
    void l04_redInvoiceFlag() throws Exception {
        AdOrder order = new AdOrder();
        order.setReceivableAmount(new BigDecimal("9000"));
        order.setReceivedAmount(new BigDecimal("9000"));
        order.setInvoicedAmount(new BigDecimal("12000")); // 多开 3000

        try (MockedStatic<IDGenerator> idGen = mockStatic(IDGenerator.class)) {
            idGen.when(IDGenerator::nextStr).thenReturn("test-log-id");
            invoke("handleMoneySide", new Class[]{AdOrder.class, String.class, String.class}, order, "u1", "o1");
        }

        assertEquals(1, order.getNeedsRedInvoice(), "已开票>应收应置红冲");
        ArgumentCaptor<AdOrderLog> captor = ArgumentCaptor.forClass(AdOrderLog.class);
        verify(orderLogMapper, times(1)).insert(captor.capture());
        assertTrue(captor.getValue().getAfterValue().contains("需红冲"),
                "应记录红冲: " + captor.getValue().getAfterValue());
    }

    @Test
    void l04_noMoneySide_noLog() throws Exception {
        AdOrder order = new AdOrder();
        order.setReceivableAmount(new BigDecimal("9000"));
        order.setReceivedAmount(new BigDecimal("9000"));
        order.setInvoicedAmount(new BigDecimal("9000"));

        invoke("handleMoneySide", new Class[]{AdOrder.class, String.class, String.class}, order, "u1", "o1");

        assertEquals(0, order.getNeedsRedInvoice(), "持平不红冲");
        verify(orderLogMapper, times(0)).insert(any());
    }
}
