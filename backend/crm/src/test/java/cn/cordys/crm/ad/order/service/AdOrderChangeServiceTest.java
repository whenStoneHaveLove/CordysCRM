package cn.cordys.crm.ad.order.service;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.permission.PermissionUtils;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.common.constants.AdOrderChangeStatus;
import cn.cordys.crm.ad.common.constants.OrderStateMachine;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.domain.AdOrderChange;
import cn.cordys.crm.ad.order.domain.AdOrderLog;
import cn.cordys.mybatis.BaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 改单服务纯逻辑单测（NO @SpringBootTest / NO DB）：通过反射调用私有纯逻辑方法，
 * 依赖以 Mockito 注入。覆盖：
 *  - L-24 变更字段白名单守卫（禁止 order_type）；
 *  - 执行时快照应用 + 金额重算（applyAfter + AdAmountCalculator.computeAmounts）；
 *  - 执行编排：订单金额/返点比例变更后「实际应付/应付返点/订单收入」必须重算
 *    （未带下游明细时也要刷新，不能停留在旧值），且顺序为「明细同步 → 应收侧 → 收入侧」
 *    （基数依赖敏感：预付基数=应付、收入基数=应收）。
 *
 * <p>不启动 Spring，不连库；角色守卫(SessionUtils)与父单状态机联动不在本测试范围。
 * L-04 资金侧（红冲标记/应退款/待补收）本期不实现，故不设对应用例。</p>
 */
class AdOrderChangeServiceTest {

    /** 改单执行动作（与 AdOrderChangeService.ACTION_CHANGE_EXECUTED 一致）。 */
    private static final String ACTION_CHANGE_EXECUTED = "CHANGE_EXECUTED";

    private AdOrderChangeService service;
    private BaseMapper<AdOrderLog> orderLogMapper;
    private BaseMapper<AdOrderChange> orderChangeMapper;
    private BaseMapper<AdOrder> adOrderMapper;
    private AdOrderService adOrderService;
    private AdAmountCalculator amountCalculator;

    @BeforeEach
    void setUp() throws Exception {
        service = new AdOrderChangeService();
        orderLogMapper = mock(BaseMapper.class);
        when(orderLogMapper.insert(any())).thenReturn(1);
        orderChangeMapper = mock(BaseMapper.class);
        adOrderMapper = mock(BaseMapper.class);
        adOrderService = mock(AdOrderService.class);
        amountCalculator = spy(new AdAmountCalculator());
        setField("orderLogMapper", orderLogMapper);
        setField("orderChangeMapper", orderChangeMapper);
        setField("adOrderMapper", adOrderMapper);
        setField("adOrderService", adOrderService);
        setField("amountCalculator", amountCalculator);
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

    /* ---------- 执行编排：收入侧派生列必须随新金额/比例刷新 ---------- */

    /**
     * 只改订单金额（未勾选下游客户明细）：应付侧不整表覆盖，但收入侧派生列必须重算，
     * 否则「订单收入」停留在旧值（= 实际应收 − 实际应付不自洽）。
     */
    @Test
    void execute_onlyAmountChanged_recalcIncomeDerived() throws Exception {
        AdOrder order = baseOrder();
        AdOrderChange change = approvedChange("{\"totalAmount\":20000}");
        when(orderChangeMapper.selectByPrimaryKey("c1")).thenReturn(change);
        when(adOrderMapper.selectByPrimaryKey("o1")).thenReturn(order);

        runExecute("c1");

        // 应收侧随新总额重算：20000 - 20000*10% = 18000
        assertAmount("执行后应收=18000", new BigDecimal("18000.00"), order.getReceivableAmount());
        // 未变更下游明细：不得整表覆盖子表，但必须以库中明细收口重算收入三列
        verify(adOrderService, never()).syncOrderDownstreamMedia(any(), any(), any(), any(), any());
        verify(adOrderService).recomputeIncomeFromDetails(order);
        // 顺序：应收侧重算 → 收入侧重算（收入基数=新应收）
        InOrder inOrder = inOrder(amountCalculator, adOrderService);
        inOrder.verify(amountCalculator).computeAmounts(order);
        inOrder.verify(adOrderService).recomputeIncomeFromDetails(order);
        // 父单恢复改单前状态并记一条 CHANGE_EXECUTED 日志
        assertEquals(OrderStateMachine.EXECUTING, order.getStatus(), "应恢复改单前状态(执行中)");
        verify(orderLogMapper, times(1)).insert(any());
    }

    /**
     * 同时改订单金额 + 下游客户明细：顺序必须为「明细同步（基数=应付）→ 应收侧 → 收入侧（基数=应收）」，
     * 前置顺序错会导致收入用旧应收、预付用旧应付。
     */
    @Test
    void execute_withDownstream_recalcIncomeAfterSync() throws Exception {
        AdOrder order = baseOrder();
        AdOrderChange change = approvedChange("{\"totalAmount\":20000,\"downstreamMediaIds\":[\"m1\"],"
                + "\"downstreamMediaPayables\":[{\"downstreamMediaId\":\"m1\",\"payableAmount\":5000,"
                + "\"rebateMode\":20,\"rebateValue\":0}]}");
        when(orderChangeMapper.selectByPrimaryKey("c1")).thenReturn(change);
        when(adOrderMapper.selectByPrimaryKey("o1")).thenReturn(order);

        runExecute("c1");

        verify(adOrderService, times(1)).syncOrderDownstreamMedia(any(), any(), any(), any(), any());
        InOrder inOrder = inOrder(adOrderService, amountCalculator);
        inOrder.verify(adOrderService).syncOrderDownstreamMedia(any(), any(), any(), any(), any());
        inOrder.verify(amountCalculator).computeAmounts(order);
        inOrder.verify(adOrderService).recomputeIncomeFromDetails(order);
    }

    /** 跑一次 execute，桩掉权限校验与 ID 生成（纯单测无 Spring 环境）。 */
    private void runExecute(String changeId) throws Exception {
        try (MockedStatic<PermissionUtils> perm = mockStatic(PermissionUtils.class);
             MockedStatic<IDGenerator> idGen = mockStatic(IDGenerator.class)) {
            perm.when(() -> PermissionUtils.hasPermission(PermissionConstants.AD_ORDER_CHANGE_SUBMIT))
                    .thenReturn(true);
            idGen.when(IDGenerator::nextStr).thenReturn("test-log-id");
            service.execute(changeId, "u1", "o1");
        }
    }

    /** 基线订单：总额 10000、返点 10%（应收 9000），应付 6000 / 实际应付 5000 / 收入 4000，当前处于改单审核中(60)。 */
    private AdOrder baseOrder() {
        AdOrder order = new AdOrder();
        order.setId("o1");
        order.setOrganizationId("o1");
        order.setStatus(OrderStateMachine.CHANGE_APPROVING);
        order.setTotalAmount(new BigDecimal("10000"));
        order.setNoRebateAmount(BigDecimal.ZERO);
        order.setRebateMode(10);
        order.setRebateValue(new BigDecimal("10"));
        order.setReceivableAmount(new BigDecimal("9000"));
        order.setMediaPayableAmount(new BigDecimal("6000"));
        order.setActualMediaPayableAmount(new BigDecimal("5000"));
        order.setOrderIncomeAmount(new BigDecimal("4000"));
        return order;
    }

    /** 审批通过(20) 的改单，父单改单前状态=执行中(50)。 */
    private AdOrderChange approvedChange(String snapshotAfter) {
        AdOrderChange change = new AdOrderChange();
        change.setId("c1");
        change.setOrderId("o1");
        change.setStatus(AdOrderChangeStatus.APPROVED.getCode());
        change.setSnapshotAfter(snapshotAfter);
        change.setOrderStatusBefore(OrderStateMachine.EXECUTING);
        return change;
    }
}
